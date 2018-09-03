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
     <b>Наличие паузы отравки сообщений</b>
     */
    private boolean shouldOrFalse = true;

    /**
     Property-name для properties
     */
    private static final String STOP_MINUTES = "stopMinutes";

    private static final long serialVersionUID = 1984L;

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = ECheck.class.getSimpleName();

    /**
     <b>Следящий за почтой класс</b>
     */
    private static final ECheck IT_INST = getItInst();

    /**
     <b>checker.obj</b> файл.
     */
    private static final File objFile = new File("checker.obj");

    /**
     <b>Время, в минутах, для приостановки отправки почты</b>
     */
    private int stopMinutes;

    /**
     {@link FileProps}
     */
    private static transient InitProperties initProperties = new FileProps(SOURCE_CLASS);

    private static transient Properties properties = new Properties();

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
        int mailChk;
        try{
            mailChk = firstMBCheck();
            long sentDateLong = Long.parseLong(properties.getProperty(SENTDATE));
            if(System.currentTimeMillis() > sentDateLong){
                return -1;
            }
            else{
                return mailChk;
            }
        }
        catch(MessagingException | NumberFormatException e){
            properties.setProperty(OR_FALSE, "1");
            properties.setProperty(STOP_MINUTES, "-1");
            properties.setProperty(SENTDATE, System.currentTimeMillis() + "");
            initProperties.setProps(properties);
            MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().fromArray(e.getStackTrace()));
            return -1;
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
        Folder folder = getInbox();
        Message[] messages = folder.getMessages();
        if(messages.length <= 0 && !properties.isEmpty()){
            try{
                IT_INST.stopMinutes = Integer.parseInt(properties.getProperty(STOP_MINUTES));
            }
            catch(NumberFormatException e){
                return -1;
            }
            return IT_INST.stopMinutes;
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
            return -1;
        }
        else{
            IT_INST.shouldOrFalse = false;
            String stopHRSString;
            try{
                stopHRSString = split[1];
                Long timeToPause = Long.valueOf(stopHRSString);
                if(timeToPause==0) return zeroMine(message.getSentDate().getTime());
                properties.setProperty(SENTDATE, System.currentTimeMillis() + timeToPause + "");
                properties.setProperty(STOP_MINUTES, stopHRSString + "");
                properties.setProperty(OR_FALSE, "0");
                initProperties.setProps(properties);
            }
            catch(ArrayIndexOutOfBoundsException e){
                return badSplitCheck(message, folder);
            }
            int stopHRSInt = Integer.parseInt(stopHRSString);

        }
        return -1;
    }

    private static void writeO() {
        try(OutputStream fileOutputStream = new FileOutputStream(objFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)){
            objectOutputStream.writeObject(IT_INST);
            properties.setProperty("uptime",
                    ( float ) (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_TIME_IN_MILLIS) / 60) + "");
            properties.setProperty(STOP_MINUTES, IT_INST.stopMinutes + "");
            if(IT_INST.shouldOrFalse){ properties.setProperty(OR_FALSE, "1"); }
            else{ properties.setProperty(OR_FALSE, "0"); }
            initProperties.setProps(properties);
        }
        catch(IOException e){
            MESSAGE_TO_USER.errorAlert(new Date().toString() + " error!",
                    System.currentTimeMillis() +
                            " timestamp", "SYSTEM IS DOWN" + e.getMessage() + "\n" +
                            new TForms().toStringFromArray(e.getStackTrace()));
        }
    }

    private static int zeroMine(long time) {                                                                         // Если Mine~0
        IT_INST.shouldOrFalse = false;
        time = time + TimeUnit.DAYS.toMillis(1);
        properties.setProperty(SENTDATE, time + "");
        properties.setProperty(OR_FALSE, "0");
        initProperties.setProps(properties);
        return 0;
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

    private static int badSplitCheck(Message message, Folder folder) throws MessagingException {
        message.setFlag(Flags.Flag.DELETED, true);
        IT_INST.shouldOrFalse = true;
        IT_INST.stopMinutes = -1;
        properties.setProperty(STOP_MINUTES, "-1");
        checkTimeToStart(-1, message, folder, message.getReceivedDate());
        properties.setProperty(OR_FALSE, "1");
        initProperties.setProps(properties);
        return -1;
    }

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
                    return 0;
                }
            }
            if(stopHRSInt==0){ return 0; }
            else{
                message.setFlag(Flags.Flag.DELETED, true);
                IT_INST.shouldOrFalse = true;
                IT_INST.stopMinutes = -1;
                properties.setProperty(OR_FALSE, "1");
                folder.close(true);
                initProperties.setProps(properties);
                return -1;
            }
        }
        catch(MessagingException e){
            MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().fromArray(e.getStackTrace()));
            properties.setProperty(OR_FALSE, "1");
            properties.setProperty(STOP_MINUTES, "-1");
            initProperties.setProps(properties);
            return -1;
        }
    }

    public static boolean getShould() {
        return IT_INST.shouldOrFalse;
    }

    private static ECheck getItInst() {
        if(objFile!=null){
            try(InputStream fileInput = new FileInputStream(objFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInput)){
                initProperties.getProps();
                if(!properties.isEmpty()){
                    IT_INST.stopMinutes = Integer.parseInt(properties.getProperty(STOP_MINUTES));
                }
                MESSAGE_TO_USER.info(SOURCE_CLASS, "3", fileInput.toString());
                ECheck o = ( ECheck ) objectInputStream.readObject();
                return o;
            }
            catch(IOException | ClassNotFoundException e){
                MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().fromArray(e.getStackTrace()));
            }
        }
        return new ECheck();
    }

    /*Private metsods*/
//unstat

    private static void getTimeWaiting(Date sentDate, Folder folder, Message message) {
        long dateLong = sentDate.getTime();
        if(System.currentTimeMillis() > dateLong){
            MESSAGE_TO_USER.infoNoTitles(TimeUnit
                    .MILLISECONDS.toMinutes(System.currentTimeMillis() - dateLong) + " hrs");
        }
    }

}