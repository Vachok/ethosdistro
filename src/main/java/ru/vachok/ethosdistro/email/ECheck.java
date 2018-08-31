package ru.vachok.ethosdistro.email;


import ru.vachok.email.MessagesFromServer;
import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.parser.ParsingStart;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.TForms;
import ru.vachok.messenger.MessageToUser;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import java.io.*;
import java.util.Date;
import java.util.Random;
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
    public static long delay = ConstantsFor.DELAY;

    /**
     <b>Наличие паузы отравки сообщений</b>
     */
    private boolean shouldISend;

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

    private static ECheck getItInst() {
        try(InputStream fileInput = new FileInputStream(objFile.getAbsolutePath());
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInput)){
            ECheck o = ( ECheck ) objectInputStream.readObject();
            return o;
        }
        catch(IOException | ClassNotFoundException e){
            objFile.delete();
            MESSAGE_TO_USER.errorAlert("OBJECT IN", new Date().toString(), e.getMessage() + "\n" +
                    new TForms().toStringFromArray(e.getStackTrace()));
        }
        return new ECheck();
    }

    public static int getStopHours() {
        if(isShouldISend()){
            writeO();
            return IT_INST.stopHours;
        }
        else{
            return -1;
        }
    }

    /*PS Methods*/

    /**
     @param test инвертор для правильного условия.
     @return {@code "Runnable parseRun = new ParsingStart(\"http://hous01.ethosdistro.com/?json=yes\", " + test + ");";}
     */
    public static String scheduleStart(boolean test) {
        try{
            int stopHours = getStopHours();
            String msg = stopHours + " stopHours";
            MESSAGE_TO_USER.infoNoTitles(msg);
            if(stopHours==-1){
                delay = ConstantsFor.DELAY;
            }
            if(stopHours==0){
                delay = ConstantsFor.DELAY / 2;
            }
            if(stopHours > 0){
                delay = TimeUnit.HOURS.toSeconds(stopHours);
            }
            else{
                delay = ConstantsFor.DELAY;
            }
        }
        catch(ExceptionInInitializerError e){
        }
        ScheduledExecutorService scheduledExecutorService =
                Executors
                        .unconfigurableScheduledExecutorService(Executors
                                .newScheduledThreadPool(2));
        Runnable parseRun = new ParsingStart("http://hous01.ethosdistro.com/?json=yes", test);

        getI();
        if(isShouldISend() && getStopHours() > 0){
            delay = getStopHours();
            String msg = delay + " is delay";
            MESSAGE_TO_USER.infoNoTitles(msg);
        }
        else{
            delay = ConstantsFor.DELAY;
            String msg = delay + " is delay";
            MESSAGE_TO_USER.infoNoTitles(msg);
        }
        scheduledExecutorService.scheduleWithFixedDelay(parseRun,
                ConstantsFor
                        .INITIAL_DELAY, delay, TimeUnit
                        .SECONDS);

        return "Runnable parseRun = new ParsingStart(\"http://hous01.ethosdistro.com/?json=yes\", " + test + ");";
    }

    public static boolean isShouldISend() {
        scheduledChkMailbox();
        writeO();
        return IT_INST.shouldISend;
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
        if(messageSubj=="" || messageSubj==null){
            IT_INST.shouldISend = true;
            return -1;
        }
        if(messageSubj.equals("0")){
            IT_INST.shouldISend = false;
            return 0;
        }
        else{
            return Integer.parseUnsignedInt(messageSubj);
        }
    }

    /**
     <b>Планирование проверки почтового ящика</b>
     */
    private static int scheduledChkMailbox() {
        Callable<Message[]> mailMessages = new MessagesFromServer();
        ScheduledExecutorService scheduledExecutorService =
                Executors.unconfigurableScheduledExecutorService(Executors
                        .newSingleThreadScheduledExecutor());
        int seed = ( int ) (ConstantsFor.DELAY / 3);
        ScheduledFuture<Message[]> scheduledFuture = scheduledExecutorService
                .schedule(mailMessages, new Random(seed).nextInt(), TimeUnit.SECONDS);
        String messageSubj = "";
        try{
            Message[] messages = scheduledFuture.get();
            for(Message message : messages){
                int i = message.getMessageNumber();
                String s = message.getSubject();
                MESSAGE_TO_USER.info("Mailbox", "Content:", s);
                if(s.toLowerCase().toLowerCase().contains("mine:")){
                    messageSubj = s.split(":")[1];
                }
                IT_INST.stopHours = getStopHours(messageSubj);
                Folder folder = getInbox();
                Message delMSG = folder.getMessage(i);
                delMSG.setFlag(Flags.Flag.DELETED, true);
                folder.close(true);
                writeO();
            }
        }
        catch(Exception e){
            MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms()
                    .toStringFromArray(e.getStackTrace()));
        }
        if(IT_INST.stopHours > 0){
            IT_INST.shouldISend = false;
            writeO();
            return IT_INST.stopHours;
        }
        else{
            IT_INST.shouldISend = true;
            writeO();
            return 0;
        }
    }
//unstat
    /*Private metsods*/

}