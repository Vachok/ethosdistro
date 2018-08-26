package ru.vachok.ethosdistro.util;


import org.apache.commons.io.FileUtils;
import ru.vachok.messenger.MessageToUser;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 @since 25.08.2018 (23:03) */
public class FileLogger implements MessageToUser {

   /**
    Simple Name класса, для поиска настроек
    */
   private static final String SOURCE_CLASS = FileLogger.class.getSimpleName();

   @Override
   public void errorAlert(String s, String s1, String s2) {
      makeFile(s,s1,s2);
   }

   private static void makeFile(String s, String s1, String s2) {
      String format = MessageFormat
            .format("Writting to file: {0}\n{1}\n{2}", s, s1, s2);
      Logger.getLogger(SOURCE_CLASS).warning(format);
      File file = new File("log.log");
      try{
      if(!file.exists()){
         FileUtils.touch(file);
         FileUtils.writeStringToFile(file, ("\n"+s +
                                                  " " +
                                                  s1 +
                                                  "\n" +
                                                  s2 +
                                                  "\n----------------------------------------------"),
               "UTF-8", true);
      }else if(file.lastModified() < System.currentTimeMillis()- TimeUnit.HOURS.toMillis(1)){
            FileUtils.forceDelete(file);
         }
      else{
         FileUtils
               .writeStringToFile(file, ("\n"+s+
                                               " "+s1+
                                               "\n"+s2+
                                               "\n----------------------------------------------"),
                     "UTF-8", true);
      }
      }catch(IOException e){
         e.printStackTrace();
      }
   }

   @Override
   public void info(String s, String s1, String s2) {
      makeFile(s,s1,s2);
   }

   @Override
   public void infoNoTitles(String s) {
      makeFile(s,"","");
   }

   @Override
   public void infoTimer(int i, String s) {
      makeFile(s,"s1","s2");
   }

   @Override
   public String confirm(String s, String s1, String s2) {
      makeFile(s,s1,s2);
      return null;
   }
}