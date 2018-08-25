package ru.vachok.ethosdistro.parser;

import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;


import java.io.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;


/**
 @since 25.08.2018 (14:39) */
public class ParsingFinalize implements Callable<Boolean>{

   /**
    Simple Name класса, для поиска настроек
    */
   private static final String SOURCE_CLASS = ParsingFinalize.class.getSimpleName();

   /**
    {@link }
    */
   private static MessageToUser messageToUser = new MessageCons();


   @Override
   public Boolean call()  {
      boolean b = readFile();
      return b;
   }

   private boolean readFile() {
      File jsonFile = new File("answer.json");
      if(!jsonFile.exists()) {
         messageToUser.errorAlert(SOURCE_CLASS, "readFile", jsonFile.getAbsolutePath() + " is "+ false);
      }else {
         List<String> fileAsList = new ArrayList<>();
         try(InputStream inputStream = new FileInputStream(jsonFile);
         InputStreamReader reader = new InputStreamReader(inputStream);
         BufferedReader bufferedReader = new BufferedReader(reader)){
            while(bufferedReader.ready()){
               fileAsList.add(bufferedReader.readLine());
            }
         }catch(IOException e){
            messageToUser.errorAlert(SOURCE_CLASS, e.getMessage(),
                  Arrays.stream(e.getStackTrace())
                        .sorted().toString());
         }
         messageToUser.info(SOURCE_CLASS, "readFile", fileAsList.toString());
         return true;
      }
      return false;
   }

}