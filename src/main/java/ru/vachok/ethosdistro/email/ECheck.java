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
import java.util.Objects;
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
     <b>Наличие паузы отравки сообщений</b>
     */
    private boolean shouldOrFalse = true;

    /**
     <b>Время, в часах, для приостановки отправки почты</b>
     */
    private int stopHours;

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
     Property-name для properties
     */
    private static final String STOP_HRS_PARAM = "STOP_HRS_PARAM";

    /**
     {@link FileProps}
     */
    private static transient InitProperties initProperties = new FileProps(SOURCE_CLASS);

    private static transient Properties properties = new Properties();

    public static int getStopHours() {
        properties = initProperties.getProps();
        try{
            return firstMBCheck();
        }
        catch(MessagingException e){
            properties.setProperty(OR_FALSE, "1");
            properties.setProperty(STOP_HRS_PARAM, "-1");
            initProperties.setProps(properties);
            MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().fromArray(e.getStackTrace()));
            return -1;
        }
    }


    /**
     <b>Планирование проверки почтового ящика</b>
     */
    private static int firstMBCheck() throws MessagingException {
        Folder folder = getInbox();
        Message[] messages = folder.getMessages();
        if(messages.length <= 0 && !properties.isEmpty()){
            try{
                IT_INST.stopHours = Integer.parseInt(properties.getProperty(STOP_HRS_PARAM));
            }
            catch(NumberFormatException e){
                return -1;
            }
            return IT_INST.stopHours;
        }
        else{
            return secondMBCheck(folder, messages);
        }
    }

    private static int secondMBCheck(Folder folder, Message[] messages) throws MessagingException {
        Message message = messageSubjectCheck(messages);
        String[] split = message.getSubject().split("~");
        if(split.length <= 0){
            message.setFlag(Flags.Flag.DELETED, true);
            checkTimeToStart(-1, message, folder, message.getReceivedDate());
            properties.setProperty(STOP_HRS_PARAM, "-1");
            properties.setProperty(OR_FALSE, "1");
            folder.close(true);
            writeO();
            MESSAGE_TO_USER.infoNoTitles(IT_INST.shouldOrFalse + " returned -1. Starting the PARSER.");
            initProperties.setProps(properties);
            return -1;
        }
        else{
            IT_INST.shouldOrFalse = false;
            String stopHRSString = "";
            try{
                stopHRSString = split[1];
            }
            catch(ArrayIndexOutOfBoundsException e){
                return badSplitCheck(message, folder);
            }
            int stopHRSInt = Integer.parseInt(stopHRSString);
            if(stopHRSInt==0){
                IT_INST.shouldOrFalse = false;
                message.setFlag(Flags.Flag.SEEN, true);
                folder.close(true);
                properties.setProperty(OR_FALSE, "0");
                initProperties.setProps(properties);
                return 0;
            }
            else{
                message.setFlag(Flags.Flag.DELETED, true);
                return checkTimeToStart(stopHRSInt, message, folder, message.getReceivedDate());
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

    private static int checkTimeToStart(int stopHRSInt, Message message, Folder folder, Date receivedDate) {
        try{
            if(receivedDate!=null){
                long startLong = receivedDate.getTime() + TimeUnit.HOURS.toMillis(stopHRSInt);
                if(startLong > System.currentTimeMillis()){
                    IT_INST.shouldOrFalse = false;
                    IT_INST.stopHours = stopHRSInt;
                    folder.close(true);
                    properties.setProperty(STOP_HRS_PARAM, stopHRSInt + "");
                    properties.setProperty(OR_FALSE, "0");
                    initProperties.setProps(properties);
                    return 0;
                }
            }
            if(stopHRSInt==0){ return 0; }
            else{
                message.setFlag(Flags.Flag.DELETED, true);
                IT_INST.shouldOrFalse = true;
                IT_INST.stopHours = -1;
                properties.setProperty(OR_FALSE, "1");
                folder.close(true);
                initProperties.setProps(properties);
                return -1;
            }
        }
        catch(MessagingException e){
            MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().fromArray(e.getStackTrace()));
            properties.setProperty(OR_FALSE, "1");
            properties.setProperty(STOP_HRS_PARAM, "-1");
            initProperties.setProps(properties);
            return -1;
        }
    }

    private static void writeO() {
        try(OutputStream fileOutputStream = new FileOutputStream(objFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)){
            objectOutputStream.writeObject(IT_INST);
            properties.setProperty("uptime",
                    ( float ) (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - ConstantsFor.START_TIME_IN_MILLIS) / 60) + "");
            properties.setProperty(STOP_HRS_PARAM, IT_INST.stopHours + "");
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

    public static boolean getShould() {
        return IT_INST.shouldOrFalse;
    }

    private static int badSplitCheck(Message message, Folder folder) throws MessagingException {
        message.setFlag(Flags.Flag.DELETED, true);
        IT_INST.shouldOrFalse = true;
        IT_INST.stopHours = -1;
        properties.setProperty(STOP_HRS_PARAM, "-1");
        checkTimeToStart(-1, message, folder, message.getReceivedDate());
        properties.setProperty(OR_FALSE, "1");
        initProperties.setProps(properties);
        return -1;
    }

    /*Private metsods*/
//unstat

    private static ECheck getItInst() {
        if(objFile!=null){
            try(InputStream fileInput = new FileInputStream(objFile);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInput)){
                initProperties.getProps();
                if(!properties.isEmpty()){
                    Objects.requireNonNull(IT_INST).stopHours = Integer.parseInt(properties.getProperty(STOP_HRS_PARAM));
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

}