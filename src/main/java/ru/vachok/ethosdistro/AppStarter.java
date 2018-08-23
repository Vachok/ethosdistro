package ru.vachok.ethosdistro;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.time.LocalDateTime;


/**
 <h1>Стартовый класс приложения</h1>

 @since 23.08.2018 (15:34) */
public class AppStarter {

   private static MessageToUser messageToUser = new MessageCons();

   public static void main(String[] args) {
      messageToUser.info(AppStarter.class.getName(), "start at", LocalDateTime.now().toString());
   }
}
