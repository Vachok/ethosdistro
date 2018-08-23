package ru.vachok.ethosdistro.parser;


import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;


/**
 <h1>Запуск парсера</h1>

 @since 23.08.2018 (16:48) */
public class ParsingStart implements Runnable {

   private static MessageToUser messageToUser = new MessageCons();

   @Override
   public void run() {
      String parsedSite = ParseMe.getInstance().call();
      messageToUser.infoNoTitles(parsedSite);
   }
}
