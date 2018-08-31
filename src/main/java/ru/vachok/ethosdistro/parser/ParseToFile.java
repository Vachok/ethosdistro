package ru.vachok.ethosdistro.parser;


import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.TForms;
import ru.vachok.messenger.MessageToUser;

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;


/**
 <h1>Парсер сайта</h1>
 <a href="http://hous01.ethosdistro.com/" target=_blank>http://hous01.ethosdistro.com/</a>

 @since 23.08.2018 (15:53) */
public class ParseToFile implements Parsers {

   private static final String SOURCE_CLASS = ParseToFile.class.getName();

   private static final Logger LOGGER = Logger.getLogger(SOURCE_CLASS);

   private static final MessageToUser MESSAGE_TO_USER = new DBLogger();
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
      MESSAGE_TO_USER.info(SOURCE_CLASS, "startParsing", s);
      return s;
   }

   /*Private metsods*/
   private String saveToFile() {
      File file = new File(fileName);
      try(InputStream inputStream = urlToParse.openStream();
          DataInputStream dataInputStream = new DataInputStream(inputStream);
          OutputStream outputStream = new FileOutputStream(file);
          DataOutputStream dataOutputStream = new DataOutputStream(outputStream)){
         while(inputStream.available() > 0){
            dataOutputStream.write(dataInputStream.read());
         }
         return file.getAbsolutePath() + " \nChange time: " + new Date(file.lastModified());
      }
      catch(IOException e){
         LOGGER.throwing(SOURCE_CLASS, "getJSON", e);
         MESSAGE_TO_USER.info(SOURCE_CLASS, "IOException", new TForms().toStringFromArray(e.getStackTrace()));
         return e.getMessage();
      }
   }
}
