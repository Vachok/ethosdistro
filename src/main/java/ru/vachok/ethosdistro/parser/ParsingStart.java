package ru.vachok.ethosdistro.parser;


import ru.vachok.ethosdistro.AppStarter;
import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.email.ECheck;
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

   /**
    {@link Parsers}
    */
   private Parsers parsers;

   /**
    Параметр {@code -t on}
    */
   private boolean test;

   /**
    URL, как строка
    */
   private final String urlAsString;

   /**
    Class Simple Name
    */
   private static final String SOURCE_CLASS = ParsingStart.class.getSimpleName();

   /**
    {@link Logger}
    */
   private static final Logger LOGGER = Logger.getLogger(SOURCE_CLASS);

   /**
    {@link DBLogger}
    */
   private static final MessageToUser MESSAGE_TO_USER = new DBLogger();

   /*Constru*/

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

    @param s    url как строка
    @param test test - запуск с <i>обратным !</i> условием.
    */
   public ParsingStart(String s, boolean test) {
      this.urlAsString = s;
      this.test = test;
      final ECheck eCheck = ECheck.getI();
      eCheck.run();
   }

   @Override
   public void run() {
      boolean work = ECheck.isShouldIWork();
      MESSAGE_TO_USER.info(SOURCE_CLASS, work + " work", ECheck.offSender());
      this.parsers = new ParseToFile();
      URL url = getUrlFromStr();
      parsers.startParsing(url);
      String s = new TForfs().toStringFromArray(ConstantsFor.RCPT);
      MESSAGE_TO_USER.info(SOURCE_CLASS, "email recep", s);
      if(work){
         sendRes(this.test);
      }
      else{
         MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, work + "work", ECheck.offSender());
         Thread.currentThread().interrupt();
      }
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

   private void sendRes(boolean callTest) {
      Boolean call;
      if(callTest){
         call = !new ParsingFinalize().call();
      }
      else{
         call = new ParsingFinalize().call();
      }
      File file = new File("answer.json");
      MessageToUser emailS = new ESender(ConstantsFor.RCPT);
      MessageToUser log = new FileLogger();
      MESSAGE_TO_USER.info(SOURCE_CLASS, "INFO", "ConstantsFor.RCPT.size() = " + ConstantsFor.RCPT.size() + "\n" +
            "Uptime = "
            + TimeUnit.MILLISECONDS
            .toHours(System.currentTimeMillis() -
                  AppStarter.getStartLong()) +
            " started at " +
            new Date(AppStarter.getStartLong()));
      log.info(SOURCE_CLASS, "ConstantsFor.RCPT.size() = ", ConstantsFor.RCPT.size() + "");
      if(call){
         MESSAGE_TO_USER.info(SOURCE_CLASS, file
                     .getFreeSpace() / ConstantsFor.MEGABYTE +
                     " free space",
               new Date(file.lastModified()) + "\n" + file.getAbsolutePath());
         log.info(SOURCE_CLASS, file
                     .getFreeSpace() / ConstantsFor.MEGABYTE +
                     " free space",
               new Date(file.lastModified()) + "\n" + file.getAbsolutePath());
         Thread.currentThread().interrupt();
      }
      else{
         emailS.errorAlert("ALARM!", "Condition not mining", "NO MINING! " + urlAsString);
         log.errorAlert("ALARM!", "Condition not mining", "NO MINING! " + urlAsString);
      }
      MESSAGE_TO_USER.infoNoTitles(ECheck.getI().toString());
      Thread.currentThread().interrupt();
   }
}

