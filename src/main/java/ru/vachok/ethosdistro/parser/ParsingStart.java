package ru.vachok.ethosdistro.parser;


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
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 <h1>Запуск парсера</h1>

 @since 23.08.2018 (16:48) */
public class ParsingStart implements Runnable {

    private final MessageToUser fileLogger = new FileLogger();

    /**
    Class Simple Name
    */
   private static final String SOURCE_CLASS = ParsingStart.class.getSimpleName();

   /**
    {@link Logger}
    */
   private static final Logger LOGGER = Logger.getLogger(SOURCE_CLASS);

   /**
    * {@link DBLogger}
    */
   private static final MessageToUser TO_USER_DATABASE = new DBLogger();

   /**
    * URL, как строка
    */
   private final String urlAsString;

   /**
    * {@link Parsers}
    */
   private Parsers parsers;

   /**
    Параметр {@code -t on}
    */
   private boolean test;

   /**
    <b>Конструктор</b>

    @param urlAsString url как строка
    */
   public ParsingStart(String urlAsString) {
      this.parsers = new ParseJsonAsUsualString();
      this.urlAsString = urlAsString;
      this.parsers = new ParseJsonAsUsualString();
   }

   /**
    Конструктор

    @param urlAsString url как строка
    @param fileName    файл, для записи (имя)
    */
   public ParsingStart(String urlAsString, String fileName) {
      if(fileName==null){
         fileName = "answer.json";
      }
      this.urlAsString = urlAsString;
      this.parsers = new ParseToFile(fileName);
   }

   /**
    Конструктор

    @param parsers     чем парсим {@link Parsers}
    @param urlAsString url как строка
    */
   public ParsingStart(Parsers parsers, String urlAsString) {
      this.parsers = parsers;
      this.urlAsString = urlAsString;
   }

   /**
    Конструктор

    @param s url как строка
    @param test test - запуск с <i>обратным !</i> условием.
    */
   public ParsingStart(String s, boolean test) {
      this.urlAsString = s;
      this.test = test;
   }

   @Override
   public void run() {
       TO_USER_DATABASE.info(SOURCE_CLASS, "RUN", new Date().toString());
      this.parsers = new ParseToFile();
      URL url = getUrlFromStr();
      parsers.startParsing(url);
      String s = new TForfs().toStringFromArray(ConstantsFor.RCPT);
       TO_USER_DATABASE.info(SOURCE_CLASS, "email recep", s);
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
       String returnString = new ParsingFinalize().call();
      if(callTest){
          call = returnString.equalsIgnoreCase("false");
      }else{
          call = !returnString.equalsIgnoreCase("false");
      }
      File file = new File("answer.json");
      MessageToUser emailS = new ESender(ConstantsFor.RCPT);
      MessageToUser log = new FileLogger();
      log.info(SOURCE_CLASS, "ConstantsFor.RCPT.size() = " , ConstantsFor.RCPT.size()+"");
      if(call){
          String statisticsProb = new Date(file.lastModified()) + "\n" + file.getAbsolutePath() +
                  " last modified: " + new Date(file.lastModified());
          String freeSpaceOnDisk = file.getFreeSpace() / ConstantsFor.MEGABYTE + " free space in Megabytes";
          TO_USER_DATABASE.info(SOURCE_CLASS,
                  "END (Uptime = " + ( float ) (System.currentTimeMillis() - ConstantsFor
                          .START_TIME_IN_MILLIS) / TimeUnit.HOURS.toMillis(1) + " hrs)",
                  freeSpaceOnDisk + "\n" + statisticsProb);
          fileLogger.info(SOURCE_CLASS, freeSpaceOnDisk, statisticsProb);
          Thread.currentThread().interrupt();
      }
      else {
          String subjectIP = returnString.split("~~")[0];
          String bodyJSON = returnString.split("~~")[1];
          emailS.errorAlert(subjectIP, "Mine~ALARM", bodyJSON +
                  "   | NO MINING! " + urlAsString);
          fileLogger.errorAlert("ALARM!", "Condition not mining", returnString +
                  "   | NO MINING! " + urlAsString);
          Thread.currentThread().interrupt();
      }
   }

}
