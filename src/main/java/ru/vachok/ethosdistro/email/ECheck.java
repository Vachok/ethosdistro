package ru.vachok.ethosdistro.email;


import ru.vachok.email.MessagesFromServer;
import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.TForms;
import ru.vachok.messenger.MessageToUser;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import java.io.*;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.*;
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

    public static ECheck getI() {
        return getItInst();
    }

    public static void setDelay(long delay) {
        IT_INST.delay = delay;
    }

    public static int getStopHours() {
        MESSAGE_TO_USER.info(SOURCE_CLASS, "4", "getStopHours");
        if(isShouldISend()){
            return IT_INST.stopHours;
        }
        else{
            return -1;
        }
    }

    /*PS Methods*/

    public static boolean isShouldISend() {
        MESSAGE_TO_USER.info(SOURCE_CLASS, "4.1", IT_INST.shouldISend + " should send");
        scheduledChkMailbox();
        writeO();
        return IT_INST.shouldISend;
    }

    /**
     <b>Планирование проверки почтового ящика</b>
     */
    private static int scheduledChkMailbox() {
        Runnable r = () -> MESSAGE_TO_USER.info(SOURCE_CLASS, "8", IT_INST.shouldISend + " send | " +
                IT_INST.stopHours + " stop hours.");
        MESSAGE_TO_USER.info(SOURCE_CLASS, "5", "Checking Mail");
        Callable<Message[]> mailMessages = new MessagesFromServer();
        ScheduledExecutorService scheduledExecutorService =
                Executors.unconfigurableScheduledExecutorService(Executors
                        .newSingleThreadScheduledExecutor());
        ScheduledFuture<Message[]> scheduledFuture = scheduledExecutorService
                .schedule(mailMessages, ConstantsFor.INITIAL_DELAY + 5, TimeUnit.SECONDS);

        String messageSubj = "~";
        try{
            MESSAGE_TO_USER.info(SOURCE_CLASS, "6", "Getting messages");
            Message[] messages = scheduledFuture.get();
            if(messages.length <= 0){ return 0; }
            else for(Message message : messages){
                int i = message.getMessageNumber();
                String s = message.getSubject();
                MESSAGE_TO_USER.info("Mailbox", "Content:", s);
                if(s.toLowerCase().toLowerCase().contains("mine~")){ // если делить по : тогда пересекаешься со стандартными мэйлерами!
                    messageSubj = s.split("ine".toLowerCase())[1];
                    MESSAGE_TO_USER.info(SOURCE_CLASS, "7", "Got mail: " + new TForms().toStringFromArray(message.getFrom()));
                    if(messageSubj.equals("~")){
                        IT_INST.shouldISend = true;
                        r.run();
                        continue;
                    }
                }
                IT_INST.stopHours = getStopHours(messageSubj);
                Folder folder = getInbox();
                Message delMSG = folder.getMessage(i);
                delMSG.setFlag(Flags.Flag.DELETED, true);
                folder.close(true);
                r.run();
                return IT_INST.stopHours;
            }
            r.run();
            return IT_INST.stopHours;
        }
        catch(Exception e){
            MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().toStringFromArray(e.getStackTrace()));
            IT_INST.shouldISend = true;
            r.run();
        }
        r.run();
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

    private static ECheck getItInst() {
        try(InputStream fileInput = new FileInputStream(objFile.getAbsolutePath());
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInput)){
            MESSAGE_TO_USER.info(SOURCE_CLASS, "3", fileInput.toString());
            ECheck o = ( ECheck ) objectInputStream.readObject();
            MESSAGE_TO_USER.info(SOURCE_CLASS, IT_INST.shouldISend + " should i send 3.5 ", IT_INST.stopHours + " stop hours\n" + IT_INST.delay + " delay");
            return o;
        }
        catch(IOException | ClassNotFoundException e){
            MESSAGE_TO_USER.errorAlert("OBJECT IN", new Date().toString(), e.getMessage() + "\n" +
                    new TForms().toStringFromArray(e.getStackTrace()));
            MESSAGE_TO_USER.info(SOURCE_CLASS, IT_INST.shouldISend + "",
                    IT_INST.stopHours + " stop hours\n" + IT_INST.delay + " delay\n" + objFile.exists() + " exists " + objFile.getAbsolutePath());
            return new ECheck();
        }
    }
//unstat
    /*Private metsods*/

}