package ru.vachok.ethosdistro.email;


import ru.vachok.email.MessagesFromServer;
import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.TForfs;
import ru.vachok.messenger.MessageToUser;

import javax.mail.Message;
import java.io.*;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.*;
import java.util.logging.Logger;


/**
 @since 28.08.2018 (21:42) */
public class ECheck implements Serializable {

    private static final long serialVersionUID = 1984L;

    /**
     {@link }
     */
    private static final transient MessageToUser MESSAGE_TO_USER = new DBLogger();

    /**
     <b>Следящий за почтой класс</b>
     */
    private static final ECheck IT_INST = new ECheck();
    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = ECheck.class.getSimpleName();

    private static final Logger LOGGER = Logger.getLogger(serialVersionUID + SOURCE_CLASS);

    /**
     <b>Наличие паузы отравки сообщений</b>
     */
    private boolean schouldISend;

    /**
     <b>Время, в часах, для приостановки отправки почты</b>
     */
    private int stopHours;

    public static ECheck getI() {
        return IT_INST;
    }

    public static int getStopHours() {
        if(isShouldISend()){
            writeO();
            return IT_INST.stopHours;
        }
        else{ return -1; }
    }

    static boolean isShouldISend() {
        scheduledChkMailbox();
        return IT_INST.schouldISend;
    }

    private static void writeO() {
        try(OutputStream fileOutputStream = new FileOutputStream("checker.obj");
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)){
            objectOutputStream.defaultWriteObject();
            LOGGER.info(new File("checker.obj").getAbsolutePath());
        }
        catch(IOException e){
            MESSAGE_TO_USER.errorAlert(new Date().toString() + " error!",
                    System.currentTimeMillis() +
                            " timestamp", "SYSTEM IS DOWN" +
                            new TForfs().toStringFromArray(e.getStackTrace()));
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
        ScheduledFuture<Message[]> scheduledFuture = scheduledExecutorService
                .schedule(mailMessages, new Random(ConstantsFor.DELAY / 2).nextInt(), TimeUnit.SECONDS);
        String messageSubj = "";
        try{
            Message[] messages = scheduledFuture.get();
            for(Message message : messages){
                String s = message.getSubject();
                MESSAGE_TO_USER.info("Mailbox", "Content:", s);
                if(s.toLowerCase().contains("mine:")) messageSubj = s.split(":")[1];
                IT_INST.stopHours = getStopHours(messageSubj);
            }
        }
        catch(Exception e){
            MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, e.getMessage(), new TForfs()
                    .toStringFromArray(e.getStackTrace()));
        }
        if(IT_INST.stopHours > 0){
            IT_INST.schouldISend = false;
            writeO();
            return IT_INST.stopHours;
        }
        else{
            IT_INST.schouldISend = true;
            return 0;
        }
    }

    private static int getStopHours(String messageSubj) {
        if(messageSubj=="" || messageSubj==null){
            return -1;
        }
        if(messageSubj.equals("0")){
            return 0;
        }
        else{
            return Integer.parseUnsignedInt(messageSubj);
        }
    }
}