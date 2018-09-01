package ru.vachok.ethosdistro.parser;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.TForms;
import ru.vachok.messenger.MessageToUser;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;


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
    private static final MessageToUser messageToUser = new DBLogger();

    private static final String falseString = "false";


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
                    return checkCond(parse, rigs);
                }
            }
            catch(IOException | ParseException e){
                ConstantsFor.sendMailAndDB.accept(e.getMessage(), new TForms().toStringFromArray(e.getStackTrace()));
            }
        }

        return falseString;
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
            Object ip = parse.get("ip");
            if(condition.toString().equalsIgnoreCase("mining")){
                coList.add(condition);
                return condition + "~~" + parse.toJSONString();
            }
            else{
                messageToUser.info(SOURCE_CLASS, "NO MINING IN", parse.toJSONString());
                return ip.toString() + "~~" + parse.toJSONString();
            }
        }
        if(coList.size()==3){
            messageToUser.info(LocalDateTime.now().toString(), "condition = ", coList.toString() + "");
            return LocalDateTime.now().toString() + "\ncondition = " + "\n" + new TForms().toStringFromArray(coList);
        }
        else{
            messageToUser.errorAlert(System.currentTimeMillis() + " time", "\ncondition = ",
                    new TForms().toStringFromArray(coList));
            return falseString;
        }
    }

}