package ru.vachok.ethosdistro.util;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.email.ECheck;
import ru.vachok.ethosdistro.parser.ParsingStart;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;

import java.io.*;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;


/**
 @since 30.08.2018 (21:29) */
public class WatchDogNorah implements Runnable {


    private long delayIsSec;

    private static final Timer timer = new Timer("ParsingStart");

    /**
     {@link }
     */
    private final MessageToUser eSender = new ESender(RCPTS);

    /*Constru*/

    private final boolean test;

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = WatchDogNorah.class.getSimpleName();

    private final MessageToUser local = new MessageCons();
    private static final List<String> RCPTS = new ArrayList<>();

    public WatchDogNorah(boolean test, long delayInSec) {
        this.test = test;
        this.delayIsSec = delayInSec;
    }
    public WatchDogNorah(boolean test) {
        this.test = test;
    }

    @Override
    public void run() {
        try{
            schedulerGetDelay();
        }
        catch(RejectedExecutionException e){
            local.errorAlert(SOURCE_CLASS, e.getMessage(), "Check mail only. No parse miners");
            timer.cancel();
        }
    }

    private void schedulerGetDelay() throws RejectedExecutionException {
        boolean launchOrFalse = ECheck.getShould();
        int stopHours = ECheck.getStopHours();
        if(launchOrFalse && stopHours > 0){
            timer.cancel();
            timer.schedule(
                    new ParsingStart(test),
                    new Date(),
                    TimeUnit.HOURS.toMillis(stopHours));
        }
        else{
            if(stopHours==0){
                local.infoNoTitles("EXECUTION STOP!");
                Thread.currentThread().interrupt();
                throw new RejectedExecutionException();
            }
            else{
                timer.schedule(
                        new ParsingStart(test),
                        new Date(),
                        TimeUnit.SECONDS.toMillis(ConstantsFor.DELAY_IN_SECONDS - 50));
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