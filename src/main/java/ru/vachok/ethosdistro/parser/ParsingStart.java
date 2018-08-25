package ru.vachok.ethosdistro.parser;


import jdk.internal.jline.internal.Nullable;
import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.util.UTF8;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import sun.awt.geom.AreaOp;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 <h1>Запуск парсера</h1>

 @since 23.08.2018 (16:48) */
public class ParsingStart implements Runnable {

   private static final String SOURCE_CLASS = ParsingStart.class.getSimpleName();

   private static final Logger LOGGER = Logger.getLogger(SOURCE_CLASS);

   private Parsers parsers;
   private static MessageToUser messageToUser = new MessageCons();

   private String urlAsString;

   public ParsingStart(String urlAsString) {
      this.parsers = new ParseJsonAsUsualString();
      this.urlAsString = urlAsString;
      this.parsers = new ParseJsonAsUsualString();
   }

   public ParsingStart(String urlAsString, @Nullable String fileName) {
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

   @Override
   public void run() {
      this.parsers = new ParseToFile();
      URL url = getUrlFromStr();
      parsers.startParsing(url);
      sendRes();
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

   private void sendRes(){
      File file = new File("answer.json");
      MessageToUser emailS = new ESender(ConstantsFor.RCPT);
      System.out.println("ConstantsFor.RCPT.size() = " + ConstantsFor.RCPT.size());
      if(new ParsingFinalize().call()) {
         messageToUser.info(file.getAbsolutePath(),new Date(file.lastModified())+"", file.getFreeSpace()+" free space");
      }
      else {
         emailS.errorAlert("ALARM!", "Condition not mining", new UTF8()
               .fromString("Что-то идёт не так: ")+ urlAsString);
         ConstantsFor.RCPT.clear();
      }
   }

}
