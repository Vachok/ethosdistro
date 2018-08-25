package ru.vachok.ethosdistro.parser;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;


/**
 @since 25.08.2018 (14:39) */
public class ParsingFinalize implements Callable<Boolean> {

   /**
    Simple Name класса, для поиска настроек
    */
   private static final String SOURCE_CLASS = ParsingFinalize.class.getSimpleName();

   /**
    {@link }
    */
   private static MessageToUser messageToUser = new MessageCons();


   @Override
   public Boolean call() {

      return jsonAsList();
   }

   private boolean jsonAsList() {
      JSONObject parse;
      File jsonFile = new File("answer.json");
      if(!jsonFile.exists()){
         messageToUser.errorAlert(SOURCE_CLASS, "readFile", jsonFile.getAbsolutePath() + " is " + false);
      }
      else{
         try(InputStream inputStream = new FileInputStream(jsonFile);
             DataInputStream dataInputStream = new DataInputStream(inputStream);
             InputStreamReader reader = new InputStreamReader(dataInputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)){
            while(bufferedReader.ready()){
               JSONParser parser = new JSONParser();
               parse = ( JSONObject ) parser.parse(bufferedReader);
               Object rigs = ( JSONObject ) parse.get("rigs");
               return checkCond(parse, rigs);
            }
         }
         catch(IOException | ParseException e){
            messageToUser.errorAlert(SOURCE_CLASS, e.getMessage(),
                  Arrays.stream(e.getStackTrace())
                        .sorted().toString());
         }
      }
      return false;
   }

   private boolean checkCond(JSONObject parse, Object rigs) throws ParseException {
      List<Object> coList = new ArrayList<>();
      for(String s : ConstantsFor.DEVICES){
         Object o = (( JSONObject ) rigs).get(s);
         String s1 = parse
               .toJSONString(( Map ) o);
         JSONParser parser = new JSONParser();
         parse = ( JSONObject ) parser.parse(s1);

         Object condition = parse.get("condition");
         coList.add(condition);
      }
      if(coList.size()==3){
         messageToUser.info(LocalDateTime.now().toString(),"condition = " , coList.toString()+"");
         return true;
      }
      else{
         ConstantsFor.RCPT.add(ConstantsFor.KIR_MAIL);
         messageToUser.errorAlert(System.currentTimeMillis()+" time", "\ncondition = " , coList.toString()+"");
         return false;
      }
   }

}