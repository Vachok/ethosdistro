package ru.vachok.email;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;

import javax.mail.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MessagesFromServer implements Callable<Message[]> {

    private static final String SOURCE_CLASS = MessagesFromServer.class.getSimpleName();

    private static final String S_N_N_S = "%s%n%n%s";

    private boolean cleanMBox;

    public MessagesFromServer(boolean cleanMBox) {
        this.cleanMBox = cleanMBox;
        MessageToUser messageToUser = new ESender(ConstantsFor.RCPT);
        messageToUser.info(SOURCE_CLASS, "cleanMBox is", cleanMBox + ".");
    }

    public MessagesFromServer() {
    }

    @Override
    public Message[] call() {
        Message[] messages = new Message[0];
        try{
            if(cleanMBox){
                throw new UnsupportedOperationException("Not Ready Yet");
            }
            messages = getInbox().getMessages();
        }
        catch(MessagingException e){

            Logger.
                    getLogger(SOURCE_CLASS)
                    .log(Level.WARNING, String.format(S_N_N_S, e.getMessage(), Arrays
                            .toString(e.getStackTrace())));
        }
        return messages;
    }

    protected static Folder getInbox() {
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
            Logger.getLogger(SOURCE_CLASS).log(Level.WARNING, String.format(S_N_N_S, e.getMessage(), Arrays.toString(e.getStackTrace())));
        }
        Folder inBox = null;
        try{
            inBox = Objects.requireNonNull(store).getFolder("Inbox");
            inBox.open(Folder.READ_WRITE);

            return inBox;
        }
        catch(MessagingException e){
            Logger.getLogger(SOURCE_CLASS).log(Level.WARNING, String.format(S_N_N_S, e.getMessage(), Arrays.toString(e.getStackTrace())));
        }
        throw new UnsupportedOperationException("Inbox not available :(");
    }

    private static Properties getSessionProps() {
        InitProperties initProperties = new DBRegProperties("mail-regru");
        Properties sessionProps = initProperties.getProps();
        sessionProps.setProperty("NewSessionStarted", new Date().toString());
        saveProps(sessionProps);
        return sessionProps;
    }

    private static void saveProps(Properties sessionProps) {
        InitProperties initProperties = new FileProps(SOURCE_CLASS);
        initProperties.setProps(sessionProps);
        initProperties.getProps();
        initProperties.setProps(sessionProps);
    }

}