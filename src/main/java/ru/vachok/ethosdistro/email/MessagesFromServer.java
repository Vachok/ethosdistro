package ru.vachok.email;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.email.Cleaner;
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

import static java.util.logging.Level.INFO;


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

            Logger.getLogger(SOURCE_CLASS).log(Level.WARNING, String.format(S_N_N_S, e.getMessage(), Arrays.toString(e.getStackTrace())));
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
            Logger.getLogger(SOURCE_CLASS).log(Level.WARNING, String.format(S_N_N_S, e.getMessage(), Arrays.toString(e.getStackTrace())));
        }
        Folder inBox = null;
        try{
            inBox = Objects.requireNonNull(store).getFolder("Inbox");
            inBox.open(Folder.READ_WRITE);
            Logger.getLogger(Cleaner.class.getSimpleName()).log(INFO, inBox.getMessageCount() + " inbox size");
            return inBox;
        }
        catch(MessagingException e){
            Logger.getLogger(SOURCE_CLASS).log(Level.WARNING, String.format(S_N_N_S, e.getMessage(), Arrays.toString(e.getStackTrace())));
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

}