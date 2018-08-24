package ru.vachok.ethosdistro;


import ru.vachok.ethosdistro.parser.ParsingStart;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 <h1>Стартовый класс приложения</h1>

 @since 23.08.2018 (15:34) */
public class AppStarter {

   private static MessageToUser messageToUser = new MessageCons();

   private static final Long START_LONG = System.currentTimeMillis();

   public static Long getStartLong() {
      return START_LONG;
   }

   public static void main(String[] args) {
      DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd hh:mm",
            Locale.forLanguageTag("RU"));
      String startTime = dateTimeFormatter.format(LocalDateTime.now());
      messageToUser.info(AppStarter.class.getName(), "Initializing " +
            ParsingStart.class.getName() + " with " + ConstantsFor.INITIAL_DELAY +
            " seconds delay...", startTime);
      scheduleStart();
   }

   private static void scheduleStart() {
      ScheduledExecutorService scheduledExecutorService =
            Executors.unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
      Runnable parseRun = new ParsingStart("http://hous01.ethosdistro.com/?json=yes");
      scheduledExecutorService.scheduleWithFixedDelay(parseRun,
            ConstantsFor.INITIAL_DELAY, ConstantsFor.DELAY, TimeUnit.SECONDS);
   }
}
