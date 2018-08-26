package ru.vachok.ethosdistro.parser;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.messenger.MessageToUser;

import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 @since 26.08.2018 (14:02) */
public class CheckOn implements Runnable  {

   /**
    Simple Name класса, для поиска настроек
    */
   private static final String SOURCE_CLASS = CheckOn.class.getSimpleName();

   /**
    {@link }
    */
   private static MessageToUser messageToUser = new DBLogger();

   /**
    When an object implementing interface <code>Runnable</code> is used
    to create a thread, starting the thread causes the object's
    <code>run</code> method to be called in that separately executing
    thread.
    <p>
    The general contract of the method <code>run</code> is that it may
    take any action whatsoever.

    @see Thread#run()
    */
   @Override
   public void run() {
      messageToUser.infoNoTitles("NOT MINING!");
   }
}