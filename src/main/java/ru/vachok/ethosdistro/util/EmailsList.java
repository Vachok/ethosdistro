package ru.vachok.ethosdistro.util;


import ru.vachok.ethosdistro.ConstantsFor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;


/**
 @since 25.08.2018 (12:34) */
public class EmailsList implements Runnable {

   /**
    Simple Name класса, для поиска настроек
    */
   private static final String SOURCE_CLASS = EmailsList.class.getSimpleName();
   private static ExecutorService executorService;
   private static Logger logger = Logger.getLogger(SOURCE_CLASS);

   private String value;

   public EmailsList(String value) {
      this.value = value;
   }

   @Override
   public void run() {
      executorService = Executors.newSingleThreadExecutor();
      Runnable r = this::mailListAdd;
      executorService.execute(r);
   }
   void mailListAdd(){
      String values[] = value.split(",");
      for(String mailAddr:values){
         ConstantsFor.RCPT.add(mailAddr);
         logger.info("emails: "+ConstantsFor.RCPT.toString());
      }
   }

}