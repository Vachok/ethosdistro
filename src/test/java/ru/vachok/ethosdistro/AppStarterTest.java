package ru.vachok.ethosdistro;


import org.testng.annotations.Test;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.time.LocalDateTime;


public class AppStarterTest {

   @Test (testName = "AppStarterTest")
   public void testMain() {
      MessageToUser messageToUser = new MessageCons();
      messageToUser.info(this.getClass().getName(), "start TESTS", LocalDateTime.now().toString());
   }
}