package ru.vachok.ethosdistro.util;


import org.apache.commons.io.FileUtils;
import ru.vachok.messenger.MessageToUser;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;


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

   private void makeFile(String s, String s1, String s2) {
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