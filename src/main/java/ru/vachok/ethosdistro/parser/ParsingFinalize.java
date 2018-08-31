package ru.vachok.ethosdistro.parser;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.messenger.MessageToUser;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;


/**
 @since 25.08.2018 (14:39) */
public class ParsingFinalize implements Callable<String> {

   /**
    Simple Name класса, для поиска настроек
    */
   private static final String SOURCE_CLASS = ParsingFinalize.class.getSimpleName();

   /**
    {@link }
    */
   private static MessageToUser messageToUser = new DBLogger();


   @Override
   public String call() {

      return jsonAsList();
   }

    private String jsonAsList() {
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
                Object rigs = parse.get("rigs");
                String checkCond = checkCond(parse, rigs);
                return checkCond;
            }
         }
         catch(IOException | ParseException e){
            messageToUser.errorAlert(SOURCE_CLASS, e.getMessage(),
                  Arrays.stream(e.getStackTrace())
                        .sorted().toString());
         }
      }
        return "false";
   }

    private String checkCond(JSONObject parse, Object rigs) throws ParseException {
      List<Object> coList = new ArrayList<>();
      for(String s : ConstantsFor.DEVICES){
         Object o = (( JSONObject ) rigs).get(s);
          String s1 = JSONObject
               .toJSONString(( Map ) o);
         JSONParser parser = new JSONParser();
         parse = ( JSONObject ) parser.parse(s1);


         Object condition = parse.get("condition");
         if(condition.toString().equalsIgnoreCase("mining")){
            coList.add(condition);
         }else {
             String jsonString = parse.get("ip").toString();
             messageToUser.info(SOURCE_CLASS, "NO MINING IN", jsonString);
             return jsonString;
         }
      }
        String s2 = coList.toString() + "";
      if(coList.size()==3){
          messageToUser.info(LocalDateTime.now().toString(), "condition = ", s2);
          return "false";
      }
      else{
         ConstantsFor.RCPT.add(ConstantsFor.KIR_MAIL);
          messageToUser.errorAlert(System.currentTimeMillis() + " time", "\ncondition = ", s2);
          return "false";
      }
   }
}