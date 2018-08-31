package ru.vachok.ethosdistro.email;


import ru.vachok.email.MessagesFromServer;
import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.TForms;
import ru.vachok.messenger.MessageToUser;

import javax.mail.*;
import java.io.*;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


/**
 @since 28.08.2018 (21:42) */
public class ECheck extends MessagesFromServer implements Serializable {

    /**
     {@link }
     */
    private static final transient MessageToUser MESSAGE_TO_USER = new DBLogger();

    /**
     {@link ConstantsFor#DELAY}
     */
    private long delay = ConstantsFor.DELAY;

    /**
     <b>Наличие паузы отравки сообщений</b>
     */
    private boolean shouldISend = true;

    /**
     <b>Время, в часах, для приостановки отправки почты</b>
     */
    private int stopHours;

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

    /*Get&*/
    public static void setDelay(long delay) {
        IT_INST.delay = delay;
    }

    public static ECheck getI() {
        return getItInst();
    }

    private static ECheck getItInst() {
        try(InputStream fileInput = new FileInputStream(objFile.getAbsolutePath());
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInput)){
            MESSAGE_TO_USER.info(SOURCE_CLASS, "3", fileInput.toString());
            ECheck o = ( ECheck ) objectInputStream.readObject();
            MESSAGE_TO_USER.info(SOURCE_CLASS,
                    IT_INST.shouldISend + " should i send 3.5 ",
                    IT_INST.stopHours + " stop hours\n" + IT_INST.delay + " delay");
            return o;
        }
        catch(IOException | ClassNotFoundException e){
            MESSAGE_TO_USER.errorAlert("OBJECT IN", new Date().toString(), e.getMessage() + "\n" +
                    new TForms().toStringFromArray(e.getStackTrace()));
            MESSAGE_TO_USER.info(SOURCE_CLASS, IT_INST.shouldISend + "",
                    IT_INST.stopHours +
                            " stop hours\n" + IT_INST.delay +
                            " delay\n" + objFile.exists() +
                            " exists " + objFile.getAbsolutePath());
            return new ECheck();
        }
    }

    public static int getStopHours() {
        MESSAGE_TO_USER.info(SOURCE_CLASS, "4", "getStopHours");
        if(IT_INST.shouldISend){
            return IT_INST.stopHours;
        }
        else{
            return -1;
        }
    }

    /*PS Methods*/
    public static Map<String, Integer> isShouldISend() {
        MESSAGE_TO_USER.info(SOURCE_CLASS, "4.1", IT_INST.shouldISend + " should send");
        int i = scheduledChkMailbox();
        Map<String, Integer> map = new ConcurrentHashMap<>();
        map.put("hrs", i);
        if(IT_INST.shouldISend){
            map.put("boolean", 1);
        }
        else{
            map.put("boolean", 0);
        }
        writeO();
        return map;
    }

    /**
     <b>Планирование проверки почтового ящика</b>
     */
    private static int scheduledChkMailbox() {
        Runnable mailInform = () -> {
            MESSAGE_TO_USER.info(SOURCE_CLASS, "8", IT_INST.shouldISend + " send | " +
                IT_INST.stopHours + " stop hours.");
            MESSAGE_TO_USER.info(SOURCE_CLASS, "5", "Checking Mail");
        };

        try{
            MESSAGE_TO_USER.info(SOURCE_CLASS, "6", "Getting messages");
            Folder folder = getInbox();
            Message[] messages = folder.getMessages();
            if(messages.length <= 0){
                IT_INST.shouldISend = true;
                return 0;
            }
            else{
                String subj = getSubj(messages);
                IT_INST.stopHours = getStopHours(subj);
                folder.close(true);
                mailInform.run();
                writeO();
                return IT_INST.stopHours;
            }
        }
        catch(Exception e){
            MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().toStringFromArray(e.getStackTrace()));
            IT_INST.shouldISend = true;
        }
        mailInform.run();
        return 0;
    }

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

    private static int getStopHours(String messageSubj) {
        if(Objects.equals(messageSubj, "~") || messageSubj==null){
            IT_INST.shouldISend = true;
            return 0;
        }
        if(messageSubj.equals("~0")){
            IT_INST.shouldISend = false;
            return -1;
        }
        else{
            IT_INST.shouldISend = true;
            return Integer.parseUnsignedInt(messageSubj.replace("~", ""));
        }
    }

    private static String getSubj(Message[] messages) throws MessagingException {
        String messageSubj;
        for(Message message : messages){
            String s = message.getSubject();
            MESSAGE_TO_USER.info("Mailbox", "Content:", s);
            if(s.toLowerCase()
                    .toLowerCase()
                    .contains("mine~")){ // если делить по : тогда пересекаешься со стандартными мэйлерами!
                messageSubj = s.split("ine".toLowerCase())[1];

                if(messageSubj.equals("~")){
                    IT_INST.shouldISend = true;
                    return messageSubj;
                }
            }
            message.setFlag(Flags.Flag.DELETED, true);
        }
        return "";
    }
//unstat

    /*Private metsods*/

}