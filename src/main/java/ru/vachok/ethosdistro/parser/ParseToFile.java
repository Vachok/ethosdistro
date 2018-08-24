package ru.vachok.ethosdistro.parser;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 <h1>Парсер сайта</h1>
 <a href="http://hous01.ethosdistro.com/" target=_blank>http://hous01.ethosdistro.com/</a>

 @since 23.08.2018 (15:53) */
public class ParseToFile implements Parsers {

   private static final String SOURCE_CLASS = ParseToFile.class.getName();

   private static final Logger LOGGER = Logger.getLogger(SOURCE_CLASS);

   private URL urlToParse;

   private String fileName;

   public ParseToFile(String fileName) {
      this.fileName = fileName;
   }

   public ParseToFile() {
      this.fileName = "answer.json";
   }

   @Override
   public String startParsing(URL urlToParse) {
      this.urlToParse = urlToParse;
      String s = "Parsed url = " + urlToParse.toString() + "\nhere: " + saveToFile();
      this.fileName = s;
      return s;
   }

   private String saveToFile() {
      File file = new File(fileName);
      try(InputStream inputStream = urlToParse.openStream();
          OutputStream outputStream = new FileOutputStream(file)){
         while(inputStream.available() > 0){
            outputStream.write(inputStream.read());
         }
         return file.getAbsolutePath() + " \nChange time: " + new Date(file.lastModified());
      }
      catch(IOException e){
         LOGGER.throwing(SOURCE_CLASS, "getJSON", e);
         return e.getMessage();
      }
   }

   @Override
   public boolean sendResult(String result) {
      this.fileName = result;
      if(result==null || ConstantsFor.RCPT.size()==0){
         LOGGER.log(Level.WARNING, "NO EMAIL! " + SOURCE_CLASS);
         return false;
      }
      else{
         MessageToUser messageToUser = new ESender(ConstantsFor.RCPT);
         messageToUser.info(SOURCE_CLASS, "IN FILE", result);
         return true;
      }
   }
}
