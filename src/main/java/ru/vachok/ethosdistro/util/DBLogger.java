package ru.vachok.ethosdistro.util;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Logger;


/**
 @since 25.08.2018 (22:22) */
public class DBLogger implements MessageToUser {

   /**
    Simple Name класса, для поиска настроек
    */
   private static final String SOURCE_CLASS = DBLogger.class.getSimpleName();

   private String logString;

   private String className;

   private String mistype;

   @Override
   public void errorAlert(String s, String s1, String s2) {
      info(s,s1,s2);
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
      String sql = "insert into ru_vachok_ethosdistro (classname, msgtype, msgvalue) values (?,?,?)";
      DataConnectTo dataConnectTo = new RegRuMysql();
      try(Connection connection = dataConnectTo.getDefaultConnection("u0466446_webapp");
          PreparedStatement preparedStatement = connection.prepareStatement(sql)){
         preparedStatement.setString(1, className);
         preparedStatement.setString(2, mistype);

         preparedStatement.setString(3, logString);
         preparedStatement.executeUpdate();
      }catch(SQLException e){
         Logger.getLogger(SOURCE_CLASS).throwing(SOURCE_CLASS, e.getMessage(), e);
      }
   }
}