package ru.vachok.ethosdistro.email;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.TForms;
import ru.vachok.messenger.MessageToUser;

import javax.mail.*;
import java.io.*;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.logging.Logger;


/**
 @since 28.08.2018 (21:42) */
public class ECheck extends MessagesFromServer implements Serializable {

    /**
     {@link }
     */
    private static final transient MessageToUser MESSAGE_TO_USER = new DBLogger();

    /**
     {@link ConstantsFor#DELAY_IN_SECONDS}
     */
    private long delay;

    /**
     <b>Наличие паузы отравки сообщений</b>
     */
    private boolean shouldOrFalse = true;

    /**
     <b>Время, в часах, для приостановки отправки почты</b>
     */
    private int stopHours;

    private static final BiConsumer<Boolean, String> MAIL_INFORM = (sendOrFalse, stopAndIDLine) -> {
        MESSAGE_TO_USER.info(ECheck.class.getSimpleName(), "Info", sendOrFalse + " send boolean | " +
                stopAndIDLine + " stop hours.");
    };

    private static final long serialVersionUID = 1984L;

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = ECheck.class.getSimpleName();

    private static final Logger LOGGER = Logger.getLogger(serialVersionUID + SOURCE_CLASS);

    /**
     <b>Следящий за почтой класс</b>
     */
    private static final ECheck IT_INST = new ECheck();

    private static final File objFile = new File("checker.obj");

    public static ECheck getI() {
        return getItInst();
    }

    public static int getStopHours() {
        try{
            return scheduledChkMailbox();
        }
        catch(MessagingException e){
            MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().fromArray(e.getStackTrace()));
        }
        throw new UnsupportedOperationException(SOURCE_CLASS + " 94");
    }

    /**
     <b>Планирование проверки почтового ящика</b>
     */
    private static int scheduledChkMailbox() throws MessagingException {
        MESSAGE_TO_USER.info(SOURCE_CLASS, "6", "Getting messages");
        Folder folder = getInbox();
        Message[] messages = folder.getMessages();
        if(messages.length <= 0){
            IT_INST.shouldOrFalse = true;
            writeO();
            MAIL_INFORM.accept(IT_INST.shouldOrFalse, IT_INST.stopHours + " 147");
            return -1;
        }
        else{
            Message message = getMessage(messages);
            String[] split = message.getSubject().split("~");
            if(split.length <= 1){
                message.setFlag(Flags.Flag.DELETED, true);
                folder.close(true);
                writeO();
                MAIL_INFORM.accept(IT_INST.shouldOrFalse, "returned -1");
                return -1;
            }
            else{
                IT_INST.shouldOrFalse = false;
                String stopHRSString = split[1];
                int stopHRSInt = Integer.parseInt(stopHRSString);
                if(stopHRSInt==0){
                    message.setFlag(Flags.Flag.DELETED, true);
                    folder.close(true);
                    return 0;
                }
                else{
                    checkTimeToStart(stopHRSInt, message);
                    writeO();
                    MAIL_INFORM.accept(IT_INST.shouldOrFalse, IT_INST.stopHours + " 147");
                    return IT_INST.stopHours;
                }
            }
        }
    }

    private static Message getMessage(Message[] messages) {
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
        throw new UnsupportedOperationException("No messages");
    }

    private static boolean checkTimeToStart(int stopHRSInt, Message message) {
        try{
            Flags flags = message.getFlags();
            String s = flags.toString();
            MESSAGE_TO_USER.info(SOURCE_CLASS, s, "flags");
        }
        catch(MessagingException e){
            MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().fromArray(e.getStackTrace()));
        }
        return true;
    }

    public static boolean getShould() {
        return IT_INST.shouldOrFalse;
    }

    private static ECheck getItInst() {
        try(InputStream fileInput = new FileInputStream(objFile.getAbsolutePath());
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInput)){
            MESSAGE_TO_USER.info(SOURCE_CLASS, "3", fileInput.toString());
            ECheck o = ( ECheck ) objectInputStream.readObject();
            MESSAGE_TO_USER.info(SOURCE_CLASS,
                    IT_INST.shouldOrFalse + " should i send 3.5 ",
                    IT_INST.stopHours + " stop hours\n" + IT_INST.delay + " delay");
            MAIL_INFORM.accept(IT_INST.shouldOrFalse, IT_INST.stopHours + "81 (getting prev instance)");
            return o;
        }
        catch(IOException | ClassNotFoundException e){
            MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().fromArray(e.getStackTrace()));
            return new ECheck();
        }
    }

    /*Private metsods*/
//unstat

    private static void writeO() {
        try(OutputStream fileOutputStream = new FileOutputStream(objFile);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)){
            objectOutputStream.writeObject(IT_INST);
            LOGGER.info(objFile.getAbsolutePath());
        }
        catch(IOException e){
            MESSAGE_TO_USER.errorAlert(new Date().toString() + " error!",
                    System.currentTimeMillis() +
                            " timestamp", "SYSTEM IS DOWN" + e.getMessage() + "\n" +
                            new TForms().toStringFromArray(e.getStackTrace()));
        }
    }

}