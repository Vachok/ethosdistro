package ru.vachok.ethosdistro.email;


import ru.vachok.email.MessagesFromServer;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.TForms;
import ru.vachok.messenger.MessageToUser;

import javax.mail.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;


/**
 @since 29.08.2018 (16:33) */
public class Cleaner extends MessagesFromServer {

    private Message message;

    private static final MessageToUser MESSAGE_TO_USER = new DBLogger();

    private static final String SOURCE_CLASS = Cleaner.class.getSimpleName();

    /**
     <h3>Разбор удалить/нет</h3>
     Эксепшены. по-теме сообщения.
     Содержащее этот паттерн удалено будет. Остальное нет.
     <p>
     Если содержит {@code all} - удалить всё.
     */
    private String delMe = "";

    /*Constru*/
    /**
     <h2>Конструктор с уточнением по-удалению.</h2>

     @param subjectMessage что требуется удалить.
     */
    public Cleaner(String subjectMessage) {
        this.delMe = subjectMessage;
        Folder folder = MessagesFromServer.getInbox();
        try{
            folder.close(true);
        }
        catch(MessagingException e){
            MESSAGE_TO_USER
                    .errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().toStringFromArray(e.getStackTrace()));
        }
    }

    public Cleaner(Message message) {

        this.message = message;
    }
    /**
     <h2>Конструктор умолч.</h2>
     */
    private Cleaner() {
    }


    /*Private metsods*/
    /**
     <h2>Работа</h2>{@link #cleanMBox}

     @param inbox {@link #getInbox()}
     @return {@link Message}[], если остались.
     @throws MessagingException что-то случилось с почтой
     @throws IOException        сохраняет на диск.
     */
    private Message[] saveToDiskAndDelete(Folder inbox) throws MessagingException, IOException {
        Message[] mailMes = inbox.getMessages();
        if(mailMes.length <= 0) throw new RejectedExecutionException("No Mail Messages");
        for(Message message : mailMes){
            String fileName = "mail\\" + message.getMessageNumber() + "-" + System.currentTimeMillis() + ".eml";
            FileOutputStream outputStream = new FileOutputStream(fileName);
            message.writeTo(outputStream);
            if(!message.getSubject().toLowerCase().contains(delMe)){
                MESSAGE_TO_USER.info(SOURCE_CLASS, message.getSentDate().toString(), message.getSubject() +
                        "\nЭто сообщение оставлено на сервере без изменений.");
            }
            else{
                message.setFlag(Flags.Flag.DELETED, true);
            }
        }
        Logger.getLogger(Cleaner.class.getSimpleName())
                .log(INFO, inbox.getMessageCount() + " inbox size");
        inbox.close(true);
        return mailMes;
    }
}
