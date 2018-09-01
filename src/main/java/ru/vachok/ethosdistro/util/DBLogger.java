package ru.vachok.ethosdistro.util;


import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.logging.FileHandler;
import java.util.logging.Logger;


/**
 @since 25.08.2018 (22:22) */
public class DBLogger implements MessageToUser {

    private String logString;

    private String className;

    private String mistype;

    private String dbName = "ru_vachok_ethosdistro";

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = DBLogger.class.getSimpleName();

    /*Constru*/
    public DBLogger(String dbName) {
        this.dbName = dbName;
    }

    public DBLogger() {

    }

    @Override
    public void errorAlert(String s, String s1, String s2) {
        info(s, s1, s2);
    }

    @Override
    public void info(String s, String s1, String s2) {
        this.className = s;
        this.mistype = s1;
        this.logString = s2;
        sendLogs();
    }

    @Override
    public void infoNoTitles(String s) {
        this.mistype = "INFO no titles";
        this.className = SOURCE_CLASS;
        this.logString = s;

        sendLogs();
    }

    @Override
    public void infoTimer(int i, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String confirm(String s, String s1, String s2) {
        throw new UnsupportedOperationException();
    }

    private void sendLogs() {
        String format = MessageFormat
                .format("Sending to database (ru_vachok_ethosdistro) : {0} | {1} | {2}", className, mistype, logString);
        Logger logger = Logger.getLogger(SOURCE_CLASS);

        logger.warning(format);
        String sql = String.format("insert into %s (classname, msgtype, msgvalue) values (?,?,?)", dbName);
        DataConnectTo dataConnectTo = new RegRuMysql();
        try(Connection connection = dataConnectTo.getDefaultConnection("u0466446_webapp");
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            logger.addHandler(new FileHandler(SOURCE_CLASS + ".log"));
            preparedStatement.setString(1, className);
            preparedStatement.setString(2, mistype);

            preparedStatement.setString(3, logString);
            preparedStatement.executeUpdate();
            logger.info("Send to DB is " + true);
        }
        catch(SQLException | IOException ignore){
            //
        }
    }
    /*Private metsods*/
}