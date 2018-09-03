package ru.vachok.ethosdistro.email;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.TForms;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;

import javax.mail.*;
import java.io.*;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;


/**
 <h1>Проверка электронной почты</h1>

 @since 28.08.2018 (21:42) */
public class ECheck extends MessagesFromServer implements Serializable {

    /*Fields*/
    /**
     {@link DBLogger}
     */
    private static final transient MessageToUser MESSAGE_TO_USER = new DBLogger();

    /**
     <b>Property-name как строка</b>
     */
    private static final String OR_FALSE = "shouldOrFalse";

    /**
     <b>Property-name</b>
     */
    private static final String SENTDATE = "sentdate";

    /**
     <b>Следящий за почтой класс</b>
     */
    private static final ECheck IT_INST = new ECheck();

    /**
     Simple Name класса, для поиска настроек
     */
    private static final transient String SOURCE_CLASS = ECheck.class.getSimpleName();

    /**
     Property-name для properties
     */
    private static final String STOP_MINUTES = "stopMinutes";

    private static final transient InitProperties initProperties = new FileProps(SOURCE_CLASS);

    /**
     <b>checker.obj</b> файл.
     */
    private static final File objFile = new File("checker.obj");

    private static final long serialVersionUID = 1984L;

    private static transient Properties properties = new Properties();

    /**
     <b>Наличие паузы отравки сообщений</b>
     */
    private boolean shouldOrFalse = true;

    /**
     <b>Время, в минутах, для приостановки отправки почты</b>
     */
    private int stopMinutes;

    /**
     <b>Публичный {@code int}, определяющий время задержки почтового отправителя.</b>
     <p>
     Загружает {@link #properties}. Оттуда пытается спарсить время, когда было {@code Mine~0}.<br>
     Вызывает проверку {@link #firstMBCheck()}. Если она падает в {@link MessagingException} или {@link NumberFormatException},
     ставит {@link #stopMinutes} в {@link #properties} как {@code -1}.

     @return {@link #stopMinutes} - кол-во минут до запуска парсера.
     */
    public static int getStopHours() {
        properties = initProperties.getProps();
        long curTime = System.currentTimeMillis();
        int mailChk;
        try{
            mailChk = firstMBCheck();
            long sentDateLong = Long.parseLong(properties.getProperty(SENTDATE));
            boolean startParse = curTime > sentDateLong;
            if(startParse){
                return 0;
            }
            else{
                MESSAGE_TO_USER.info(SOURCE_CLASS, "Left", mailChk + " minutes to Start");
                return mailChk;
            }
        }
        catch(MessagingException | NumberFormatException e){
            properties.setProperty(OR_FALSE, "1");
            properties.setProperty(STOP_MINUTES, "-1");
            properties.setProperty(SENTDATE, System.currentTimeMillis() + "");
            initProperties.setProps(properties);
            MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().fromArray(e.getStackTrace()));
            return 0;
        }
    }

    /**
     <b>Первый этап проверок почты</b>
     <p>
     Проверка на наличие почты в ящике.
     <i>Если</i> сообщений нет и {@link #properties} не пусты - пытается взять {@link #stopMinutes} из {@link #properties}.<br>
     <i>Если</i> в {@link #properties} нету значения {@link #stopMinutes} - возвращает значение {@code -1}.
     <p>
     <i>В другом</i> случае отдаёт {@link #secondMBCheck(Folder, Message[])}
     */
    private static int firstMBCheck() throws MessagingException {
        initProperties.getProps();
        Folder folder = getInbox();
        Message[] messages = folder.getMessages();
        if(messages.length <= 0){
            long timeToStart = Long.parseLong(properties.getProperty(SENTDATE));
            long l = timeToStart - System.currentTimeMillis();
            l = TimeUnit.MILLISECONDS.toMinutes(l);
            IT_INST.shouldOrFalse = true;
            properties.setProperty(OR_FALSE, "1");
            properties.setProperty(STOP_MINUTES, l + "");
            initProperties.setProps(properties);
            return ( int ) l;

        }
        else{
            return secondMBCheck(folder, messages);
        }
    }

    private static int secondMBCheck(Folder folder, Message[] messages) throws MessagingException {
        Message message = messageSubjectCheck(messages);
        String[] split = message.getSubject().split("~");
        if(split.length <= 0){                                                                          // Если mine~
            message.setFlag(Flags.Flag.DELETED, true);
            properties.setProperty(SENTDATE, message.getSentDate().getTime() + "");
            properties.setProperty(STOP_MINUTES, "-1");
            properties.setProperty(OR_FALSE, "1");
            folder.close(true);
            MESSAGE_TO_USER.infoNoTitles(IT_INST.shouldOrFalse + " returned -1. Starting the PARSER. " +
                    message.getSentDate());
            initProperties.setProps(properties);
            writeO();
            return 0;
        }
        else{
            IT_INST.shouldOrFalse = false;
            String stopHRSString;
            try{
                stopHRSString = split[1];
                int timeToPause = Integer.parseInt(stopHRSString);
                message.setFlag(Flags.Flag.DELETED, true);
                if(timeToPause==0){
                    long pauseFor = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
                    properties.setProperty(SENTDATE, pauseFor + "");
                    properties.setProperty(STOP_MINUTES, TimeUnit.MILLISECONDS
                            .toMinutes(pauseFor - System.currentTimeMillis()) + "");
                    message.setFlag(Flags.Flag.DELETED, true);
                    folder.close(true);
                    return -1;
                }
                properties.setProperty(SENTDATE, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(timeToPause)
                        + "");
                properties.setProperty(STOP_MINUTES, stopHRSString + "");
                properties.setProperty(OR_FALSE, "0");
                initProperties.setProps(properties);
                folder.close(true);
                return timeToPause;
            }
            catch(ArrayIndexOutOfBoundsException e){
                IT_INST.shouldOrFalse = true;
                return badSplitCheck(message, folder);
            }
        }
    }

    private static Message messageSubjectCheck(Message[] messages) throws MessagingException {
        for(Message m : messages){
            try{
                if(m.getSubject().toLowerCase().contains("mine~")){
                    return m;
                }
            }
            catch(MessagingException ignore){
                //
            }
        }
        throw new MessagingException("No messages");
    }

    private static void writeO() {
        try(OutputStream fileOutputStream = new FileOutputStream(objFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)){
            objectOutputStream.writeObject(IT_INST);
            properties.setProperty("uptime",
                    ( float ) (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_TIME_IN_MILLIS) / 60) + "");
            properties.setProperty(STOP_MINUTES, IT_INST.stopMinutes + "");
            if(IT_INST.shouldOrFalse){
                properties.setProperty(OR_FALSE, "1");
            }
            else{
                properties.setProperty(OR_FALSE, "0");
            }
            initProperties.setProps(properties);
        }
        catch(IOException e){
            MESSAGE_TO_USER.errorAlert(new Date().toString() + " error!",
                    System.currentTimeMillis() +
                            " timestamp", "SYSTEM IS DOWN" + e.getMessage() + "\n" +
                            new TForms().toStringFromArray(e.getStackTrace()));
        }
    }

    private static int badSplitCheck(Message message, Folder folder) throws MessagingException {
        message.setFlag(Flags.Flag.DELETED, true);
        IT_INST.shouldOrFalse = true;
        IT_INST.stopMinutes = 0;
        properties.setProperty(STOP_MINUTES, "0");
        properties.setProperty(OR_FALSE, "1");
        initProperties.setProps(properties);
        return 0;
    }

    public static boolean getShould() {
        initProperties.getProps();
        String sOf = properties.getProperty(OR_FALSE);
        if(sOf.equalsIgnoreCase("0")){
            IT_INST.shouldOrFalse = false;
        }
        return IT_INST.shouldOrFalse;
    }

    /*Private metsods*/
    private static int checkTimeToStart(int stopHRSInt, Message message, Folder folder, Date receivedDate) {
        try{
            if(receivedDate!=null){
                long startLong = receivedDate.getTime() + TimeUnit.HOURS.toMillis(stopHRSInt);
                if(startLong > System.currentTimeMillis()){
                    IT_INST.shouldOrFalse = false;
                    IT_INST.stopMinutes = stopHRSInt;
                    folder.close(true);
                    properties.setProperty(STOP_MINUTES, stopHRSInt + "");
                    properties.setProperty(OR_FALSE, "0");
                    initProperties.setProps(properties);
                    return -1;
                }
            }
            else{
                message.setFlag(Flags.Flag.DELETED, true);
                IT_INST.shouldOrFalse = true;
                IT_INST.stopMinutes = -1;
                properties.setProperty(OR_FALSE, "1");
                folder.close(true);
                initProperties.setProps(properties);
                return 0;
            }
        }
        catch(MessagingException e){
            MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().fromArray(e.getStackTrace()));
            properties.setProperty(OR_FALSE, "1");
            properties.setProperty(STOP_MINUTES, "-1");
            initProperties.setProps(properties);
        }
        return 0;
    }
//unstat
    private static void getTimeWaiting(Date sentDate, Folder folder, Message message) {
        long dateLong = sentDate.getTime();
        if(System.currentTimeMillis() > dateLong){
            MESSAGE_TO_USER.infoNoTitles(TimeUnit
                    .MILLISECONDS.toMinutes(System.currentTimeMillis() - dateLong) + " hrs");
        }
    }

}