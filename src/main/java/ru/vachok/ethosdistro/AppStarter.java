package ru.vachok.ethosdistro;


import ru.vachok.ethosdistro.parser.ParsingStart;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.FileLogger;
import ru.vachok.ethosdistro.util.TForfs;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.parse.DecoderEnc;
import ru.vachok.messenger.email.parse.UTF8;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
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

   private static final MessageToUser MESSAGE_TO_USER = new DBLogger();

   private static final DecoderEnc UTF_8 = new UTF8();

   private static long initialDelay = ConstantsFor.INITIAL_DELAY;

   private static long delay = ConstantsFor.DELAY;

   private static boolean test;

   public static Long getStartLong() {
      return START_LONG;
   }

   public static void main(String[] args) {
      if(args.length>0) {
         MESSAGE_TO_USER
               .info(SOURCE_CLASS,
                     "Arguments",
                     "Starting at: "+new Date()+"\n"+new TForfs().toStringFromArray(args));
         argsReader(args);
      }
      else {
         ConstantsFor.RCPT.add(ConstantsFor.MY_MAIL);
         ConstantsFor.RCPT.add(ConstantsFor.KIR_MAIL);
         MESSAGE_TO_USER.info(SOURCE_CLASS, "Argument - none", scheduleStart(false));
      }
   }

   private static void argsReader(String[] args) {
      DateTimeFormatter dateTimeFormatter = DateTimeFormatter
            .ofPattern("yyyy-MMM-dd hh:mm");
      String startTime = dateTimeFormatter.format(LocalDateTime.now());
      MESSAGE_TO_USER.infoNoTitles("VERSION 0.4 |25.08.2018 (23:24)|");
      String stringArgs = Arrays.toString(args).replaceAll(", ", ":");
      logger.info(stringArgs);
      args = stringArgs.split("-");
      for(String argument : args){
         try{
            String key = argument.split(":")[0];
            String value = argument.split(":")[1];
            if(key.equalsIgnoreCase("d")){
               delay = Long.parseLong(value);
               continue;
            }else {
               delay = ConstantsFor.DELAY;
            }
            if(key.equalsIgnoreCase("i")){
               initialDelay = Long.parseLong(value);
               continue;
            }else {
               initialDelay = ConstantsFor.INITIAL_DELAY;
            }
            if(key.equalsIgnoreCase("t")){
               test = true;
               continue;
            }else {
               test = false;
            }
            if(key.equalsIgnoreCase("e")){
               mailAdd(value);
            }else {
               MESSAGE_TO_USER.infoNoTitles(scheduleStart(test));
            }
         }
         catch(Exception e){
            MESSAGE_TO_USER.info(SOURCE_CLASS, "Starting", "with args");
         }

      }
      MESSAGE_TO_USER.info(AppStarter.class.getName(), startTime, "Initializing " +
            ParsingStart.class.getName() + " with " + delay +
            " seconds delay..."+scheduleStart(test));
   }

   private static void mailAdd(String value) {
      ConstantsFor.RCPT.clear();
      String[] values = value.split(",");
      for(String mailAddr : values){
         ConstantsFor.RCPT.add(mailAddr
               .replaceAll("\\Q]\\E", ""));
         String s = ConstantsFor.RCPT.toString();
         logger.info(MessageFormat
               .format("emails: {0}", s));
      }
   }


   private static String scheduleStart(boolean test) {
      MessageToUser messageToUser = new FileLogger();
      ScheduledExecutorService scheduledExecutorService =
            Executors.unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
      Runnable parseRun = new ParsingStart("http://hous01.ethosdistro.com/?json=yes", test);
      scheduledExecutorService.scheduleWithFixedDelay(parseRun,
            initialDelay, delay, TimeUnit.SECONDS);
      messageToUser.info(SOURCE_CLASS, "scheduleStart", parseRun.toString());
      return "Runnable parseRun = new ParsingStart(\"http://hous01.ethosdistro.com/?json=yes\", "+test+");";
   }
}
