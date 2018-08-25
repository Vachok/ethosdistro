package ru.vachok.ethosdistro.parser;


import jdk.internal.jline.internal.Nullable;
import ru.vachok.ethosdistro.ConstantsFor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 <h1>Запуск парсера</h1>

 @since 23.08.2018 (16:48) */
public class ParsingStart implements Runnable {

   private static final String SOURCE_CLASS = ParsingStart.class.getSimpleName();

   private static final Logger LOGGER = Logger.getLogger(SOURCE_CLASS);

   private Parsers parsers;

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
      URL url = getUrlFromStr();
      String s = "MESSAGE FROM " + this.getClass().getTypeName() + "\n\n\n" + parsers.startParsing(url);
      if(false){
         LOGGER.log(Level.INFO, SOURCE_CLASS+" sending mail");
      }else {
         LOGGER.log(Level.WARNING, SOURCE_CLASS+" no sending mail");
         ConstantsFor.RCPT.clear();
      }
      boolean sends = parsers.sendResult(s);
      LOGGER.log(Level.INFO, () -> "Start Parsing for " + url.toString() + sends + " send");
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

}
