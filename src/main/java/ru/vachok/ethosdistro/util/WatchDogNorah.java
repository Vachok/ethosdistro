package ru.vachok.ethosdistro.util;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.email.ECheck;
import ru.vachok.ethosdistro.parser.ParsingStart;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;

import java.io.*;
import java.util.*;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;


/**
 @since 30.08.2018 (21:29) */
public class WatchDogNorah implements Runnable {


    /*Fields*/
    private static final List<String> RCPTS = new ArrayList<>();

    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = WatchDogNorah.class.getSimpleName();

    private static final MessageToUser local = new MessageCons();

    private static final Timer timer = new Timer("ParsingStart");

    private static boolean test;

    private long delayIsSec;

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
            local.errorAlert(SOURCE_CLASS, e.getMessage(), new TForms().toStringFromArray(e.getStackTrace()));
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
        local.info(SOURCE_CLASS,
                "parsing scheduled at ",
                new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(schedulerGetDelay())).toString());

    }

    private static int schedulerGetDelay() throws RejectedExecutionException {
        InitProperties initProperties = new FileProps(ECheck.class.getSimpleName());
        int stopMinutes = ECheck.getStopHours();
        boolean launchOrFalse = ECheck.getShould();
        if(launchOrFalse && stopMinutes > 0){
            long periodMillis = TimeUnit.MINUTES.toMillis(stopMinutes);
            timer.schedule(new ParsingStart(test), new Date(), periodMillis);
            Properties properties = initProperties.getProps();
            long whenStart = Long.parseLong(properties.getProperty("sentdate")) + periodMillis;
            if(System.currentTimeMillis() < whenStart){
                return stopMinutes;
            }
            throw new RejectedExecutionException();
        }
        else{
            if(stopMinutes < 0){
                local.infoNoTitles("EXECUTION STOP!");
                Thread.currentThread().interrupt();
                timer.cancel();
                return -1;
            }
            else{
                try{
                    timer.schedule( //fixme 04.09.2018 (2:46)
                            new ParsingStart(test),
                        new Date(),
                            TimeUnit.SECONDS.toMillis(ConstantsFor.DELAY_IN_SECONDS - 20));
                    return ( int ) (ConstantsFor.DELAY_IN_SECONDS - 20);
                }
                catch(IllegalStateException e){
                    e.printStackTrace();
                    return 0;
                }
            }
        }
    }

    /*Private metsods*/
}