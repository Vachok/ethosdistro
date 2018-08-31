package ru.vachok.ethosdistro.util;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.email.ECheck;
import ru.vachok.ethosdistro.parser.ParsingStart;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 @since 30.08.2018 (21:29) */
public class WatchDogNorah implements Runnable {

    /**
     {@link }
     */
    private MessageToUser eSender = new ESender(RCPTS);

    private MessageToUser local = new MessageCons();

    private boolean test;

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = WatchDogNorah.class.getSimpleName();

    private static final List<String> RCPTS = new ArrayList<>();

    /*Constru*/
    public WatchDogNorah(boolean test) {
        this.test = test;
    }

    @Override
    public void run() {
        schedulerGetDelay();
        local.info(SOURCE_CLASS, "7", " end");
    }

    private void schedulerGetDelay() {
        ECheck.getI();
        long delay = TimeUnit.SECONDS.toMillis(ConstantsFor.DELAY);
        Timer timer = new Timer("ParsingStart", true);
        boolean b;

        int stopHours = ECheck.getStopHours();

        Properties properties = getProperties();
        if(!properties.isEmpty()){
            delay = ( long ) properties.get("delay");
            timer.scheduleAtFixedRate(new ParsingStart(test), new Date(), TimeUnit.HOURS.toMillis(delay));
        }
        if(stopHours > 0){
            b = ECheck.isShouldISend();
            properties.put("send", b);
            delay = TimeUnit.HOURS.toSeconds(stopHours);
            properties.put("delay", delay);
            properties.put("startstamp", System.currentTimeMillis());
            setPropertiesToFile(properties);
            timer.scheduleAtFixedRate(new ParsingStart(test), new Date(), TimeUnit.HOURS.toMillis(delay));
        }
        else{
            if(stopHours < 0){
                timer.cancel();
                timer.purge();
            }
            else{
                timer.scheduleAtFixedRate(new ParsingStart(test), new Date(), TimeUnit.SECONDS.toMillis(ConstantsFor.DELAY));
            }
        }

    }

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