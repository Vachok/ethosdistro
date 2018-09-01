package ru.vachok.email;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;

import javax.mail.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;


public class MessagesFromServer implements Callable<Message[]> {

    private static final String SOURCE_CLASS = MessagesFromServer.class.getSimpleName();


    private boolean cleanMBox;

    public MessagesFromServer(boolean cleanMBox) {
        this.cleanMBox = cleanMBox;
        MessageToUser messageToUser = new MessageCons();
        messageToUser.info(SOURCE_CLASS, "cleanMBox is", cleanMBox + ".");
    }

    public MessagesFromServer() {
        Logger.getLogger(SOURCE_CLASS).log(INFO, this.getClass().getTypeName());
    }

    @Override
    public Message[] call() {
        Message[] messages = new Message[0];
        try{
            if(cleanMBox) Cleaner.saveToDiskAndDelete(getInbox());
            messages = getInbox().getMessages();
        }
        catch(MessagingException | IOException e){
            Logger.getLogger(SOURCE_CLASS).log(Level.WARNING, String.format("%s%n%n%s", e.getMessage(), Arrays.toString(e.getStackTrace())));
        }
        return messages;
    }

    protected Folder getInbox() {
        Properties mailProps = getSessionProps();
        Authenticator authenticator = new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailProps.getProperty("user"), mailProps.getProperty("password"));
            }
        };
        Session chkSess = Session.getDefaultInstance(mailProps, authenticator);
        Store store = null;
        try{
            store = chkSess.getStore();
            store.connect(mailProps.getProperty("host"), mailProps.getProperty("user"), mailProps.getProperty("password"));
        }
        catch(MessagingException e){
            Logger.getLogger(SOURCE_CLASS).log(Level.WARNING, String.format("%s%n%n%s", e.getMessage(), Arrays.toString(e.getStackTrace())));
        }
        Folder inBox = null;
        try{
            inBox = Objects.requireNonNull(store).getFolder("Inbox");
            inBox.open(Folder.READ_WRITE);
            return inBox;
        }
        catch(MessagingException e){
            Logger.getLogger(SOURCE_CLASS).log(Level.WARNING, String.format("%s%n%n%s", e.getMessage(), Arrays.toString(e.getStackTrace())));
        }
        throw new UnsupportedOperationException("Inbox not available :(");
    }

    private Properties getSessionProps() {
        InitProperties initProperties = new DBRegProperties("mail-regru");
        Properties sessionProps = initProperties.getProps();
        sessionProps.setProperty("NewSessionStarted", new Date().toString());
        saveProps(sessionProps);
        return sessionProps;
    }


    private void saveProps(Properties sessionProps) {
        InitProperties initProperties = new FileProps(SOURCE_CLASS);
        initProperties.setProps(sessionProps);
        initProperties.getProps();
        initProperties.setProps(sessionProps);
    }

    /*END FOR CLASS*/
    public static class Cleaner extends MessagesFromServer {

        /**
         <h3>Разбор удалить/нет</h3>
         Эксепшены. по-теме сообщения.
         Содержащее этот паттерн удалено будет. Остальное нет.
         <p>
         Если содержит {@code all} - удалить всё.
         */
        private static String delMe = "";

        /**
         <h2>Конструктор с уточнением по-удалению.</h2>

         @param delMe что требуется удалить.
         */
        public Cleaner(String delMe) {
            Cleaner.delMe = delMe;
        }

        /**
         <h2>Конструктор умолч.</h2>
         */
        public Cleaner() {
        }


        /**
         <h2>Работа</h2>{@link #cleanMBox}

         @param inbox {@link #getInbox()}
         @return {@link Message}[], если остались.
         @throws MessagingException что-то случилось с почтой
         @throws IOException        сохраняет на диск.
         */
        static Message[] saveToDiskAndDelete(Folder inbox) throws MessagingException, IOException {
            Message[] mailMes = inbox.getMessages();
            if(mailMes.length <= 0) throw new RejectedExecutionException("No Mail Messages");
            for(Message message : mailMes){
                String fileName = "mail\\" + message.getMessageNumber() + "-" + System.currentTimeMillis() + ".eml";
                FileOutputStream outputStream = new FileOutputStream(fileName);
                message.writeTo(outputStream);
                if(!message.getSubject().toLowerCase().contains(delMe)){
                }
                else{ if(new File(fileName).exists() && new File(fileName).length() > 10) message.setFlag(Flags.Flag.DELETED, true); }
            }

            inbox.close(true);
            return mailMes;
        }
    }
}