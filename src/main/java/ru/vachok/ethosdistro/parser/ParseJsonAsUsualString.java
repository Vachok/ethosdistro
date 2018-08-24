package ru.vachok.ethosdistro.parser;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ParseJsonAsUsualString implements Parsers {

   private static final String SOURCE_CLASS = ParseJsonAsUsualString.class.getSimpleName();

   private static final Logger LOGGER = Logger.getLogger(SOURCE_CLASS);

   private URL url;

   private String resultAsString;

   @Override
   public String startParsing(URL url) {
      this.url = url;
      try{
         this.resultAsString = getStr();
         sendResult(resultAsString);
         return resultAsString;
      }
      catch(IOException e){
         LOGGER.throwing(SOURCE_CLASS, "startParsing", e);
         return e.getMessage();
      }
   }

   @Override
   public boolean sendResult(String result) {
      if(resultAsString==null || ConstantsFor.RCPT.size()==0){
         LOGGER.log(Level.WARNING, "NO EMAIL  " + SOURCE_CLASS);
         return false;
      }
      else{
         MessageToUser messageToUser = new ESender(ConstantsFor.RCPT);
         messageToUser.info(SOURCE_CLASS, LocalDateTime.now().toString(), resultAsString);
         return true;
      }
   }

   private String getStr() throws IOException {
      StringBuilder bytesStr = new StringBuilder();
      URLConnection urlConnection = Objects.requireNonNull(url).openConnection();
      InputStream inputStream = urlConnection.getInputStream();
      byte[] streamBytes;
      while(inputStream.available() > 0){
         streamBytes = new byte[inputStream.read()];
         bytesStr.append(new String(streamBytes));
      }
      String lengthBytes = "It " + bytesStr.toString().length() + " by JSON read\n" + bytesStr.toString();
      LOGGER.log(Level.INFO, lengthBytes);
      return bytesStr.toString();
   }

}
