package ru.vachok.ethosdistro.parser;


import ru.vachok.ethosdistro.AppStarter;
import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.FileLogger;
import ru.vachok.ethosdistro.util.TForfs;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;


/**
 <h1>Запуск парсера</h1>

 @since 23.08.2018 (16:48) */
public class ParsingStart implements Runnable {

   private static final String SOURCE_CLASS = ParsingStart.class.getSimpleName();

   private static final Logger LOGGER = Logger.getLogger(SOURCE_CLASS);

   private Parsers parsers;

   private static final MessageToUser MESSAGE_TO_USER = new DBLogger();

   private final String urlAsString;

   private boolean test;

   public ParsingStart(String urlAsString) {
      this.parsers = new ParseJsonAsUsualString();
      this.urlAsString = urlAsString;
      this.parsers = new ParseJsonAsUsualString();
   }

   public ParsingStart(String urlAsString, String fileName) {
      if(fileName==null){
         fileName = "answer.json";
      }
      this.urlAsString = urlAsString;
      this.parsers = new ParseToFile(fileName);
   }

   public ParsingStart(Parsers parsers, String urlAsString) {
      this.parsers = parsers;
      this.urlAsString = urlAsString;
   }

   public ParsingStart(String s, boolean test) {
      this.urlAsString = s;
      this.test = test;
   }

   @Override
   public void run() {
      MESSAGE_TO_USER.info(SOURCE_CLASS, "RUN", new Date().toString());
      this.parsers = new ParseToFile();
      URL url = getUrlFromStr();
      parsers.startParsing(url);
      String s = new TForfs().toStringFromArray(ConstantsFor.RCPT);
      MESSAGE_TO_USER.info(SOURCE_CLASS, "email recep", s);
      sendRes(this.test);
   }

   private URL getUrlFromStr() {
      URL url = null;
      try{
         url = new URL(urlAsString);
      }
      catch(MalformedURLException e){
         LOGGER.throwing(SOURCE_CLASS, "getSite", e);
      }
      return url;
   }

   private void sendRes(boolean callTest){
      Boolean call;
      if(callTest){
         call = !new ParsingFinalize().call();
      }else{
         call= new ParsingFinalize().call();
      }
      File file = new File("answer.json");
      MessageToUser emailS = new ESender(ConstantsFor.RCPT);
      MessageToUser log = new FileLogger();
      MESSAGE_TO_USER.info(SOURCE_CLASS, "ConstantsFor.RCPT.size() = ", ConstantsFor.RCPT.size() + "");
      log.info(SOURCE_CLASS, "ConstantsFor.RCPT.size() = " , ConstantsFor.RCPT.size()+"");
      if(call){
         MESSAGE_TO_USER.info(SOURCE_CLASS, file
                     .getFreeSpace() / ConstantsFor.MEGABYTE +
                     " free space",
               new Date(file.lastModified()) + "\n" + file.getAbsolutePath());
         log.info(SOURCE_CLASS, file
                     .getFreeSpace() / ConstantsFor.MEGABYTE +
                     " free space",
               new Date(file.lastModified()) + "\n" + file.getAbsolutePath());
         new AppStarter();
         Thread.currentThread().interrupt();
      }
      else {
         emailS.errorAlert("ALARM!", "Condition not mining", "NO MINING! " + urlAsString);
         log.errorAlert("ALARM!", "Condition not mining", "NO MINING! " + urlAsString);
         new AppStarter();
         Thread.currentThread().interrupt();
      }
   }

}
