package ru.vachok.ethosdistro;


import ru.vachok.ethosdistro.parser.ParsingStart;
import ru.vachok.ethosdistro.util.EmailsList;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.parse.DecoderEnc;
import ru.vachok.messenger.email.parse.UTF8;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 <h1>Стартовый класс приложения</h1>

 @since 23.08.2018 (15:34) */
public class AppStarter {

   private static final Long START_LONG = System.currentTimeMillis();

   private static final String SOURCE_CLASS = AppStarter.class.getSimpleName();

   private static final Logger logger = Logger.getLogger(SOURCE_CLASS);

   private static MessageToUser messageToUser = new MessageCons();
   private static DecoderEnc decoderEnc = new UTF8();

   private static long initialDelay = ConstantsFor.INITIAL_DELAY;

   private static long delay = ConstantsFor.DELAY;

   public static Long getStartLong() {
      return START_LONG;
   }

   public static void main(String[] args) {
      argsReader(args);
   }

   private static void argsReader(String[] args) {
      DateTimeFormatter dateTimeFormatter = DateTimeFormatter
            .ofPattern("yyyy-MMM-dd hh:mm");
      String startTime = dateTimeFormatter.format(LocalDateTime.now());
      messageToUser.infoNoTitles("VERSION 0.4 |25.08.2018 (21:37)|");
      String argString = Arrays.toString(args)
            .replaceAll(", ", ":");
      logger.info(argString);
      args = argString.split("-");
      for(String argument : args){
         try{
            String key = argument.split(":")[0];
            String value = argument.split(":")[1];

            if (key.equalsIgnoreCase("d")) {
               delay = Long.parseLong(value);
            }
            if (key.equalsIgnoreCase("e")) {
               mailAdd(value);
            }
         }
         catch(Exception e){
            logger.warning(decoderEnc.toAnotherEnc("Пустой агрумент!"));
         }
      }
      messageToUser.info(AppStarter.class.getName(), "Initializing " +
            ParsingStart.class.getName() + " with " + delay +
            " seconds delay...", startTime);

      scheduleStart();
   }

   private static void mailAdd(String value) {
      ConstantsFor.RCPT.add(ConstantsFor.KIR_MAIL);
      String values[] = value.split(",");
      for(String mailAddr:values){
         ConstantsFor.RCPT.add(mailAddr.replaceAll("\\Q]\\E", ""));
         logger.info("emails: "+ConstantsFor.RCPT.toString());
      }
   }


   private static void scheduleStart() {
      ScheduledExecutorService scheduledExecutorService =
            Executors.unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
      Runnable parseRun = new ParsingStart("http://hous01.ethosdistro.com/?json=yes");
      scheduledExecutorService.scheduleWithFixedDelay(parseRun,
            initialDelay, delay, TimeUnit.SECONDS);
   }
}
