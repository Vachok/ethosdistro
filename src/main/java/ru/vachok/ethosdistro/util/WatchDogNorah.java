package ru.vachok.ethosdistro.util;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.email.ECheck;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 @since 30.08.2018 (21:29) */
public class WatchDogNorah implements Runnable {


    private long delayIsSec;

    /*Constru*/

    /**
     {@link }
     */
    private final MessageToUser eSender = new ESender(RCPTS);

    private final MessageToUser local = new MessageCons();

    private final boolean test;

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = WatchDogNorah.class.getSimpleName();

    private static final List<String> RCPTS = new ArrayList<>();

    public WatchDogNorah(boolean test, long delayInSec) {
        this.test = test;
        this.delayIsSec = delayInSec;
    }
    public WatchDogNorah(boolean test) {
        this.delayIsSec = ConstantsFor.DELAY_IN_SECONDS;
        this.test = test;
    }

    @Override
    public void run() {
        if(!test){
            schedulerGetDelay();
        }
        else{
            MessageToUser[] messagesToUser = {eSender, local};
            for(MessageToUser m : messagesToUser){
                m.info(SOURCE_CLASS, delayIsSec + " delay sec.", test + " test");
            }
        }

    }

    private void schedulerGetDelay() {
        ECheck.getI();
        long delayMillis = delayIsSec = TimeUnit.SECONDS.toMillis(delayIsSec);
        Timer timer = new Timer("ParsingStart");
        Map<String, Integer> sendOrHow = ECheck.isShouldISend();
        int stopHours = sendOrHow.get("hrs");
        Integer s = sendOrHow.get("boolean");
        boolean b = true;
        if(s==0){
            b = false;
        }
        Properties properties = getProperties();
        if(!properties.isEmpty()){
            delayMillis = ( long ) properties.get("delayMillis");

        }
        if(stopHours > 0){
            properties.put("send", b);

            properties.setProperty("delayMillis", delayMillis + "");
            properties.setProperty("startstamp", System.currentTimeMillis() + "");
            setPropertiesToFile(properties);

        }
        else{
            if(stopHours < 0 || !b){
                timer.cancel();
                timer.purge();
            }
            else{
                local.info(SOURCE_CLASS, "9", delayMillis + " delayMillis");
            }
        }
    }
    /*Private metsods*/

    private Properties getProperties() {
        File p = new File(SOURCE_CLASS + ".properties");
        Properties properties = new Properties();
        if(p.exists()){
            try(InputStream inputStream = new FileInputStream(p)){
                properties.load(inputStream);
                return properties;
            }
            catch(IOException e){
                eSender.errorAlert(SOURCE_CLASS + " properties", e.getMessage(),
                        new TForms().toStringFromArray(e.getStackTrace()));
                return new Properties();
            }
        }
        else{ return new Properties(); }
    }

    private void setPropertiesToFile(Properties properties) {
        try(OutputStream outputStream = new FileOutputStream(SOURCE_CLASS + ".properties")){
            properties.store(outputStream,
                    TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() -
                            ConstantsFor.START_TIME_IN_MILLIS) + " minutes work");
        }
        catch(IOException e){
            local.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().toStringFromArray(e.getStackTrace()));
        }
    }
}