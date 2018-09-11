package ru.vachok.ethosdistro.email;


import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;

import javax.mail.*;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.logging.Logger;


public class MessagesFromServer implements Callable<Message[]> {

    /*Fields*/
    private static final String SOURCE_CLASS = MessagesFromServer.class.getSimpleName();

    private static final Logger LOGGER = Logger.getLogger(SOURCE_CLASS);

    protected static Folder getInbox() {
        Properties mailProps = getSessionProps();
        Authenticator authenticator = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailProps.getProperty("user"), mailProps.getProperty("password"));
            }
        };
        Session chkSess = Session.getDefaultInstance(mailProps, authenticator);
        try{
            Store store;
            Folder inBox;
            store = chkSess.getStore();
            store.connect(
                    mailProps.getProperty("host"),
                    mailProps.getProperty("user"),
                    mailProps.getProperty("password"));
            inBox = Objects.requireNonNull(store).getFolder("Inbox");
            inBox.open(Folder.READ_WRITE);
            return inBox;
        }
        catch(MessagingException e){
            LOGGER.throwing(SOURCE_CLASS, "getInbox", e);
        }
        return null;
    }

    MessagesFromServer() {
    }

    /*Instances*/
    @Override
    public Message[] call() throws MessagingException {
        return getInbox().getMessages();
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