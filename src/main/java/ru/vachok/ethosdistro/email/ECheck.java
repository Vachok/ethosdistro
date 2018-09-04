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

    private static final transient InitProperties FILE_PROPS = new FileProps(SOURCE_CLASS);

    /**
     <b>checker.obj</b> файл.
     */
    private static final File OBJ_FILE = new File("checker.obj");

    private static final long serialVersionUID = 1984L;

    private static transient Properties properties;

    /**
     <b>Время, в минутах, для приостановки отправки почты</b>
     */
    private int stopMinutes;

    public static void setShouldOrFalse(boolean shouldOrFalse) {
        IT_INST.shouldOrFalse = shouldOrFalse;
        FILE_PROPS.getProps();
        if(shouldOrFalse){ properties.setProperty(OR_FALSE, "1"); }
        else{ properties.setProperty(OR_FALSE, "0"); }
        FILE_PROPS.setProps(properties);
    }

    /**
     <b>Публичный {@code int}, определяющий время задержки почтового отправителя.</b>
     <p>
     Загружает {@link #properties}. Оттуда пытается спарсить время, когда было {@code Mine~0}.<br>
     Вызывает проверку {@link #firstMBCheck()}. Если она падает в {@link MessagingException} или {@link NumberFormatException},
     ставит {@link #stopMinutes} в {@link #properties} как {@code -1}.

     @return {@link #stopMinutes} - кол-во минут до запуска парсера.
     */
    public static int getStopHours() {
        properties = FILE_PROPS.getProps();
        int mailChk;
        try{
            mailChk = firstMBCheck();
            MESSAGE_TO_USER.info(
                    SOURCE_CLASS,
                    new Date(Long.parseLong(properties.getProperty(SENTDATE))).toString(),
                    mailChk + " minutes to Start");
            return mailChk;
        }
        catch(MessagingException | NumberFormatException e){
            FILE_PROPS.getProps();
            IT_INST.shouldOrFalse = properties.getProperty(OR_FALSE).equalsIgnoreCase("1");
            IT_INST.stopMinutes = Integer.parseInt(properties.getProperty(STOP_MINUTES));
            long l = TimeUnit.MINUTES.toMillis(IT_INST.stopMinutes);
            properties.setProperty(SENTDATE, (ConstantsFor.START_TIME_IN_MILLIS + l) + "");
            FILE_PROPS.setProps(properties);
            MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().fromArray(e.getStackTrace()));
            return 0;
        }
    }

    /**
     <b>Наличие паузы отравки сообщений</b>
     */
    private boolean shouldOrFalse = true;

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
        FILE_PROPS.getProps();
        Folder folder = getInbox();
        Message[] messages = folder.getMessages();
        MESSAGE_TO_USER.errorAlert("Check mail.", "You have " + messages.length + " messages", new Date().toString());
        if(messages.length <= 0){
            long timeToStart = Long.parseLong(properties.getProperty(SENTDATE));
            long l = timeToStart - System.currentTimeMillis();
            l = TimeUnit.MILLISECONDS.toMinutes(l);
            properties.setProperty(STOP_MINUTES, l + "");
            properties.setProperty("sent-cur", l + "");
            FILE_PROPS.setProps(properties);
            return ( int ) l;
        }
        else{
            MESSAGE_TO_USER.infoNoTitles("You have " + messages.length + " messages. Trying to look info.");
            return secondMBCheck(folder, messages);
        }
    }

    private static int secondMBCheck(Folder folder, Message[] messages) throws MessagingException {
        Message message = messageSubjectCheck(messages);
        String[] split = message.getSubject().split("~");
        if(split.length <= 0){
            IT_INST.shouldOrFalse = true;       // Если mine~
            message.setFlag(Flags.Flag.DELETED, true);
            properties.setProperty(SENTDATE, message.getSentDate().getTime() + "");
            properties.setProperty(STOP_MINUTES, "0");
            properties.setProperty(OR_FALSE, "1");
            folder.close(true);
            MESSAGE_TO_USER.infoNoTitles(IT_INST.shouldOrFalse + " returned 0. Starting the PARSER. " +
                    message.getSentDate());
            FILE_PROPS.setProps(properties);
            writeO();
            return -1;
        }
        else{
            IT_INST.shouldOrFalse = false;
            String minToStopStr;
            message.setFlag(Flags.Flag.DELETED, true);
            try{
                minToStopStr = split[1];
                int minToStopInt = Integer.parseInt(minToStopStr);
                if(minToStopInt==0){
                    long pauseFor = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1);
                    int pauseInMin = ( int ) TimeUnit.MILLISECONDS
                            .toMinutes(pauseFor - System.currentTimeMillis());
                    IT_INST.shouldOrFalse = true;
                    IT_INST.stopMinutes = pauseInMin;
                    properties.setProperty(SENTDATE, pauseFor + "");
                    properties.setProperty(STOP_MINUTES, +pauseInMin + "");
                    properties.setProperty(OR_FALSE, "1");
                    FILE_PROPS.setProps(properties);

                    folder.close(true);
                    MESSAGE_TO_USER.info(SOURCE_CLASS,
                            "Mine~0", pauseInMin + " returned. Properties set, message deleted.");
                    return pauseInMin;
                }
                else{
                    properties.setProperty(SENTDATE, System.currentTimeMillis() + TimeUnit
                            .MINUTES.toMillis(minToStopInt) + "");
                    properties.setProperty(STOP_MINUTES, minToStopStr + "");
                    properties.setProperty(OR_FALSE, "1");
                    FILE_PROPS.setProps(properties);

                    folder.close(true);
                    return minToStopInt;
                }
            }
            catch(ArrayIndexOutOfBoundsException e){
                FILE_PROPS.getProps();
                properties.setProperty(SENTDATE, ConstantsFor.START_TIME_IN_MILLIS + "");
                properties.setProperty(OR_FALSE, "1");
                properties.setProperty(STOP_MINUTES, "-1");
                IT_INST.shouldOrFalse = true;
                IT_INST.stopMinutes = -1;
                FILE_PROPS.setProps(properties);
                folder.close(true);
                return -1;
            }
        }
    }
    /*Private metsods*/

    private static void writeO() {
        try(OutputStream fileOutputStream = new FileOutputStream(OBJ_FILE);
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
            FILE_PROPS.setProps(properties);
        }
        catch(IOException e){
            MESSAGE_TO_USER.errorAlert(new Date().toString() + " error!",
                    System.currentTimeMillis() +
                            " timestamp", "SYSTEM IS DOWN" + e.getMessage() + "\n" +
                            new TForms().toStringFromArray(e.getStackTrace()));
        }
    }

    public static boolean getShould() {
        FILE_PROPS.getProps();
        String sOf = properties.getProperty(OR_FALSE);
        IT_INST.shouldOrFalse = !sOf.equalsIgnoreCase("0");
        return IT_INST.shouldOrFalse;
    }

    static {
        properties = new Properties();
        if(!new File(SOURCE_CLASS + ".properties").exists()){
            properties.setProperty(OR_FALSE, "1");
            properties.setProperty(SENTDATE, ConstantsFor.START_TIME_IN_MILLIS + "");
            properties.setProperty(STOP_MINUTES, "-1");
            FILE_PROPS.setProps(properties);
        }
    }

    private static Message messageSubjectCheck(Message[] messages) throws MessagingException {
        for(Message m : messages){
            try{
                if(m.getSubject().toLowerCase().contains("mine~")){
                    return m;
                }
            }
            catch(MessagingException e){
                e.printStackTrace(); //fixme 04.09.2018 (21:51)
            }
        }
        throw new MessagingException("No valid messages");
    }
}