package ru.vachok.ethosdistro.email;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 @since 28.08.2018 (21:42) */
public class ECheck implements Runnable {

   private static boolean shouldIWork;

   /**
    {@link }
    */
   private static final MessageToUser MESSAGE_TO_USER = new MessageCons();

   private static final ECheck IT_INST = new ECheck();

   private static final long STARTS = System.currentTimeMillis();

   /**
    Simple Name класса, для поиска настроек
    */
   private static final String SOURCE_CLASS = ECheck.class.getSimpleName();

   public static ECheck getI() {
      return IT_INST;
   }

   public static boolean isShouldIWork() {
      return shouldIWork;
   }

   private static void setShouldIWork(boolean shouldIWork) {
      ECheck.shouldIWork = shouldIWork;
   }

   /*PS Methods*/
   public static String offSender() {
      MESSAGE_TO_USER.info(SOURCE_CLASS, "start = ", new Date(STARTS).toString());
      chkMailbox();
      String s = "shouldIWork = " +
            shouldIWork + " and uptime is " +
            TimeUnit.MILLISECONDS
                  .toMinutes(System.currentTimeMillis() - STARTS) + " minutes";
      MESSAGE_TO_USER.infoNoTitles(s);
      return s;
   }
//unstat

   @Override
   public void run() {
      chkMailbox();
   }

   private static void chkMailbox() {

      setShouldIWork(true);
      MESSAGE_TO_USER.info(SOURCE_CLASS, "MAIL CHECKED", "shouldIWork is " + shouldIWork);
   }
}