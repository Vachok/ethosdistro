package ru.vachok.ethosdistro.util;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.DataConnectTo;
import ru.vachok.mysqlandprops.RegRuMysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;


/**
 @since 25.08.2018 (22:22) */
public class DBLogger implements MessageToUser {

   /**
    Simple Name класса, для поиска настроек
    */
   private static final String SOURCE_CLASS = DBLogger.class.getSimpleName();

   /**
    {@link }
    */
   private static MessageToUser messageToUser = new MessageCons();

   private static DataConnectTo dataConnectTo = new RegRuMysql();



   private String logString;

   private String className;

   private String mistype;

   @Override
   public void errorAlert(String s, String s1, String s2) {
      this.className = s;
      this.mistype = s1;
      this.logString = s2;
      sendLogs();
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
      this.logString = s;
      sendLogs();
   }

   @Override
   public String confirm(String s, String s1, String s2) {
      return null;
   }

   private void sendLogs() {

      String sql = "insert into ru_vachok_ethosdistro (classname, msgtype, msgvalue) values (?,?,?)";
      try(Connection connection = dataConnectTo.getDefaultConnection("u0466446_webapp");
          PreparedStatement preparedStatement = connection.prepareStatement(sql)){
         preparedStatement.setString(1, className);
         preparedStatement.setString(2, mistype);

         preparedStatement.setString(3, logString);
         preparedStatement.executeUpdate();
      }catch(SQLException e){
         messageToUser
               .errorAlert(SOURCE_CLASS, e.getMessage(), Arrays.toString(e.getStackTrace()));
      }
   }
}