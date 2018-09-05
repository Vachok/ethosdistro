package ru.vachok.ethosdistro.util;


import com.mysql.jdbc.CommunicationsException;
import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
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

    /**
     По-умолчанию имя таблицы ={@code ru_vachok_ethosdistro}
     */
    public DBLogger() {
        final String pcName = ConstantsFor.PC_NAME;
        if(pcName.equalsIgnoreCase("home") || pcName.toLowerCase().contains("no0027")){
            dbName = "ru_vachok_ethosdistro_tests";
        }
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
        try{
            sendLogs();
        }
        catch(CommunicationsException ignore){
            //
        }
    }

    @Override
    public void infoNoTitles(String s) {
        this.mistype = "INFO no titles";
        this.className = SOURCE_CLASS;
        this.logString = s;
        try{
            sendLogs();
        }
        catch(CommunicationsException ignore){
            //
        }
    }

    @Override
    public void infoTimer(int i, String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String confirm(String s, String s1, String s2) {
        throw new UnsupportedOperationException();
    }

    /*Private metsods*/
    private void sendLogs() throws CommunicationsException {
        String format = MessageFormat
                .format("Sending to database = {0}\n{1}\n{2}", className, mistype, logString);
        Logger logger = Logger.getLogger(SOURCE_CLASS);
        logger.warning(format);
        String sql = String.format("insert into %s (classname, msgtype, msgvalue) values (?,?,?)", dbName);
        DataConnectTo dataConnectTo = new RegRuMysql();
        try(Connection connection = dataConnectTo.getDefaultConnection("u0466446_webapp");
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, className);
            preparedStatement.setString(2, mistype);

            preparedStatement.setString(3, logString);
            preparedStatement.executeUpdate();
            logger.info("Send to DB is " + true);
        }
        catch(SQLException ignore){
            //
        }
    }
}