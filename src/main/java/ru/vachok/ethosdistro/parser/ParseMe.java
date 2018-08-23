package ru.vachok.ethosdistro.parser;


import org.json.simple.parser.ContainerFactory;
import ru.vachok.ethosdistro.AppStarter;
import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 <h1>Парсер сайта</h1>
 <a href="http://hous01.ethosdistro.com/" target=_blank>http://hous01.ethosdistro.com/</a>

 @since 23.08.2018 (15:53) */
public class ParseMe implements Callable<String> {

   private static final String URL_AS_STRING = "http://hous01.ethosdistro.com/";

   private static final String SOURCE_CLASS = ParseMe.class.getName();

   private static ParseMe parseMe = new ParseMe();

   private static MessageToUser messageToUser = new MessageCons();

   static void setParseMe(ParseMe parseMe) {
      ParseMe.parseMe = parseMe;
   }

   public static ParseMe getInstance() {
      return parseMe;
   }

   @Override
   public String call() {
      return getSite();
   }

   private String getSite() {
      String s = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() -
            AppStarter.getStartLong()) +
            " " + URL_AS_STRING +
            " parsing...";
      URL url = null;
      try{
         url = new URL(URL_AS_STRING);
      }
      catch(MalformedURLException e){
         messageToUser.errorAlert(SOURCE_CLASS, e.getMessage(), Arrays.toString(e.getStackTrace()));
      }
      URLConnection urlConnection;
      try{
         String bytesStr = "стрингабайты";
         urlConnection = Objects.requireNonNull(url).openConnection();
         boolean allowUserInteraction = urlConnection.getAllowUserInteraction();
         messageToUser.info(urlConnection.toString(), "allowUserInteraction", "is " + allowUserInteraction);
         try(InputStream inputStream = urlConnection.getInputStream()){
            byte[] streamBytes = new byte[ConstantsFor.MEGABYTE];
            while(inputStream.available() > 0){
               int readByte = inputStream.read(streamBytes);
               bytesStr = bytesStr + new String(streamBytes);
               messageToUser.infoNoTitles(readByte + " read bytes");
            }
            return bytesStr;
         }
      }
      catch(IOException e){
         return SOURCE_CLASS + e.getMessage() + Arrays.toString(e.getStackTrace());
      }
   }

   private void getJson() {
      ContainerFactory containerFactory = null;
      Map objectContainer = containerFactory.createObjectContainer();

   }

}
