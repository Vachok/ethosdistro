package ru.vachok.ethosdistro.parser;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Test;
import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.io.*;
import java.util.*;


public class ParsingFinalizeTest {

   private static final String SOURCE_CLASS = ParsingFinalizeTest.class.getName();

   private static MessageToUser messageToUser = new MessageCons();

   @Test
   public void getCondit(){
      System.out.println(Arrays.toString(ConstantsFor.DEVICES));
      jsonAsList();
   }

   private List<String> jsonAsList() {
      JSONObject parse;
      File jsonFile = new File("answer.json");
      if(!jsonFile.exists()){
         messageToUser.errorAlert(SOURCE_CLASS, "readFile", jsonFile.getAbsolutePath() + " is " + false);
      }
      else{
         List<String> fileAsList = new ArrayList<>();
         try(InputStream inputStream = new FileInputStream(jsonFile);
             DataInputStream dataInputStream = new DataInputStream(inputStream);
             InputStreamReader reader = new InputStreamReader(dataInputStream);
             BufferedReader bufferedReader = new BufferedReader(reader)){
            while(bufferedReader.ready()){
               JSONParser parser = new JSONParser();
               parse = ( JSONObject ) parser.parse(bufferedReader);
               Object rigs =(JSONObject) parse.get("rigs");
               checkCond(parse, rigs);
            }
         }
         catch(IOException | ParseException e){
            messageToUser.errorAlert(SOURCE_CLASS, e.getMessage(),
                  Arrays.stream(e.getStackTrace())
                        .sorted().toString());
         }
         return fileAsList;
      }
      throw new UnknownError("Cant find list...");
   }

   private boolean checkCond(JSONObject parse, Object rigs) throws ParseException {
      for(String s:ConstantsFor.DEVICES){
         Object o = (( JSONObject ) rigs).get(s);
         String s1 = parse.toJSONString(( Map ) o);
         JSONParser parser = new JSONParser();
         parse = (JSONObject ) parser.parse(s1);

         Object condition = parse.get("condition");
         System.out.println(condition.toString());
      }
      return true;
   }
}