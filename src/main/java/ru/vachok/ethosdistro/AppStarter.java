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

   private static final String SOURCE_CLASS = AppStarter.class.getSimpleName();

   private static MessageToUser messageToUser = new MessageCons();

   private static final Long START_LONG = System.currentTimeMillis();

   public static Long getStartLong() {
      return START_LONG;
   }

   public static void main(String[] args) {
      DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MMM-dd hh:mm",
            Locale.forLanguageTag("RU"));
      String startTime = dateTimeFormatter.format(LocalDateTime.now());
      messageToUser.info(AppStarter.class.getName(), "start at", startTime);
      ScheduledExecutorService scheduledExecutorService = Executors.unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
      Runnable parseRun = new ParsingStart();
      scheduledExecutorService.scheduleWithFixedDelay(parseRun, 15L, 60L, TimeUnit.SECONDS);
   }
}
