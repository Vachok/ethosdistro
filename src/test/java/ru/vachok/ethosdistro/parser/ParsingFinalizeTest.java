package ru.vachok.ethosdistro.parser;


import jdk.nashorn.internal.runtime.JSONFunctions;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.testng.annotations.Test;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class ParsingFinalizeTest {

   private static final String SOURCE_CLASS = ParsingFinalizeTest.class.getName();

   private static MessageToUser messageToUser = new MessageCons();

   @Test (testName = "Make JSON")
   public void jsonMaker() {
      String jsonListString = jsonAsList().toString();
   }

   private List<String> jsonAsList() {
      JSONObject parse = null;
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
               parse = (JSONObject)parser.parse(bufferedReader);
            }
            Object condition = Objects.requireNonNull(parse).get("condition");
            System.out.println("condition = " + condition);

         }
         catch(IOException|ParseException e){
            messageToUser.errorAlert(SOURCE_CLASS, e.getMessage(),
                  Arrays.stream(e.getStackTrace())
                        .sorted().toString());
         }
         return fileAsList;
      }
      throw new UnknownError("Cant find list...");
   }
}