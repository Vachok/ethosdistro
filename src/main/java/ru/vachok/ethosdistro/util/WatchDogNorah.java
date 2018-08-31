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
    private final MessageToUser eSender = new ESender(RCPTS);

    private final MessageToUser local = new MessageCons();

    private final boolean test;

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

    /*Private metsods*/
    private void schedulerGetDelay() {
        ECheck.getI();
        long delay = TimeUnit.SECONDS.toMillis(ConstantsFor.DELAY);
        Timer timer = new Timer("ParsingStart");
        Map<String, Integer> sendOrHow = ECheck.isShouldISend();
        int stopHours = sendOrHow.get("hrs");
        Integer s = sendOrHow.get("boolean");
        boolean b = true;
        if(s==0) b = false;
        Properties properties = getProperties();
        Runnable sendMailToMe = () -> {
            List<String> rcpt = new ArrayList<>();
            rcpt.add(ConstantsFor.MY_MAIL);
            MessageToUser messageToUser = new ESender(rcpt);
            messageToUser.info(SOURCE_CLASS + " 8", "stopHours = " + stopHours, timer.toString());
        };
        if(!properties.isEmpty()){
            delay = ( long ) properties.get("delay");
            timer.scheduleAtFixedRate(new ParsingStart(test), new Date(), TimeUnit.HOURS.toMillis(delay));
        }
        if(stopHours > 0){
            properties.put("send", b);
            delay = TimeUnit.HOURS.toSeconds(stopHours);
            properties.put("delay", delay);
            properties.put("startstamp", System.currentTimeMillis());
            setPropertiesToFile(properties);
            timer.scheduleAtFixedRate(new ParsingStart(test), new Date(), TimeUnit.HOURS.toMillis(delay));
        }
        else{
            if(stopHours < 0 || !b){
                timer.cancel();
                timer.purge();
            }
            else{
                timer.scheduleAtFixedRate(new ParsingStart(test), new Date(),
                        TimeUnit.SECONDS.toMillis(ConstantsFor.DELAY));
                local.info(SOURCE_CLASS, "9", delay + " delay");
                sendMailToMe.run(); //todo 31.08.2018 (22:18)
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