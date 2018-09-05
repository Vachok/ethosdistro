package ru.vachok.ethosdistro.util;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.email.ECheck;
import ru.vachok.ethosdistro.parser.ParsingStart;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;

import java.io.*;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;


/**
 @since 30.08.2018 (21:29) */
public class WatchDogNorah extends Thread implements Runnable {


    /*Fields*/
    private static final List<String> RCPTS = new ArrayList<>();

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = WatchDogNorah.class.getSimpleName();

    private static final MessageToUser MESSAGE_TO_USER = new DBLogger();

    private static Timer timer = new Timer("TimerParse");

    private static boolean test;

    private long delayIsSec;

    private static TimerTask parseFile = new ParsingStart(test);

    /**
     {@link }
     */
    private final MessageToUser eSender = new ESender(RCPTS);

    /*Get&*/
    private void setPropertiesToFile(Properties properties) {
        try(OutputStream outputStream = new FileOutputStream(SOURCE_CLASS + ".properties")){
            properties.store(outputStream,
                    TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() -
                            ConstantsFor.START_TIME_IN_MILLIS) + " minutes work");
        }
        catch(IOException e){
            MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().toStringFromArray(e.getStackTrace()));
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
        else{
            return new Properties();
        }
    }

    /*Constru*/
    public WatchDogNorah(boolean test) {
        WatchDogNorah.test = test;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("NORAH");
        MESSAGE_TO_USER.info(SOURCE_CLASS,
                "parsing scheduled at ",
                new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(schedulerGetDelay())).toString());
    }

    private static int schedulerGetDelay() throws RejectedExecutionException {
        int stopMinutes = ECheck.getStopHours();
        boolean startOrFalse = ECheck.getShould();
        if(stopMinutes > 0){
            if(startOrFalse){ return parseMe(stopMinutes); }
            else{ return stopMinutes; }
        }
        else{
            if(stopMinutes==0){
                MESSAGE_TO_USER.errorAlert(SOURCE_CLASS, "EXECUTION STOP!", new Date().toString());
                Thread.currentThread().interrupt();
                timer.cancel();
                return 0;
            }
            else{
                long period = ConstantsFor.DELAY_IN_SECONDS - 45;
                timer.cancel();
                try{
                    timer = new Timer(period + " of seconds");
                    parseFile = new ParsingStart(test);
                    MESSAGE_TO_USER.infoNoTitles("Starting mine parser with " + period + " of seconds. Last SENT DATE change = " + stopMinutes + " minutes");
                    period = TimeUnit.SECONDS.toMillis(ConstantsFor.DELAY_IN_SECONDS - 30);
                    timer.schedule(parseFile, new Date(), period);
                    return ( int ) period;
                }
                catch(IllegalStateException e){
                    Timer timerAfterCancel = new Timer("After Cancel");
                    timerAfterCancel.schedule(parseFile, new Date(), period);
                    MESSAGE_TO_USER.info("IllegalStateException", e.getMessage(), "catching NEW Timer. " + timerAfterCancel.toString());
                    return stopMinutes;
                }
            }
        }
    }

    private static int parseMe(int stopMinutes) {
        long periodMillis = TimeUnit.MINUTES.toMillis(stopMinutes);
        timer.cancel();
        MESSAGE_TO_USER.infoNoTitles("Canceling old timer " + parseFile.cancel());
        timer = new Timer(stopMinutes + " min.");
        parseFile = new ParsingStart(test);
        try{
            timer.schedule(parseFile, new Date(ConstantsFor.START_TIME_IN_MILLIS), periodMillis);
            MESSAGE_TO_USER.infoNoTitles(new Date(parseFile.scheduledExecutionTime()) + " scheduledExecutionTime");
        }
        catch(IllegalStateException e){
            e.printStackTrace();
        }
        MESSAGE_TO_USER.infoNoTitles("Start new timer with " + stopMinutes + " min period ");
        ECheck.setShouldOrFalse(false);
        return stopMinutes;
    }

    /*Private metsods*/
}