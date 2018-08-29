package ru.vachok.ethosdistro.email;


import org.junit.Assert;
import ru.vachok.email.MessagesFromServer;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.messenger.MessageToUser;

import javax.mail.Message;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;


/**
 @since 28.08.2018 (21:42) */
public class ECheck {

    /**
     {@link }
     */
    private static final MessageToUser MESSAGE_TO_USER = new DBLogger();

    private static final ECheck IT_INST = new ECheck();

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = ECheck.class.getSimpleName();

    /**
     <b>Наличие паузы отравки сообщений</b>
     */
    private static boolean shouldIWork;

    public static ECheck getI() {
        return IT_INST;
    }

    public static boolean isShouldIWork() {
        scheduledChkMailbox();
        return shouldIWork;
    }

    /**
     <b>Планирование проверки почтового ящика</b>
     */
    private static int scheduledChkMailbox() {
        ScheduledExecutorService executorService = Executors
                .unconfigurableScheduledExecutorService(Executors
                        .newSingleThreadScheduledExecutor());
        Callable<Message[]> mailMessages = new MessagesFromServer();
        String messageSubj = "";
        try{
            Message[] call = mailMessages.call();
            for(Message message : call){
                String s = message.getSubject();
                if(s.toLowerCase().contains("mine:")) messageSubj = s.split(":")[1];
            }
        }
        catch(Exception e){
            Assert.assertNull(e.getMessage(), e);
        }
        boolean shouldIWork;
        if(messageSubj==null){
            shouldIWork = true;
        }
        if(messageSubj.equals("0")){ shouldIWork = false; }
        else{ Integer.parseUnsignedInt(messageSubj); }
        throw new RejectedExecutionException();
    }

    private static void setShouldIWork() {
        ECheck.shouldIWork = shouldIWork;
    }
//unstat

    private static void offSender() {

    }
}