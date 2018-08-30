package ru.vachok.ethosdistro.util;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.email.ECheck;
import ru.vachok.ethosdistro.parser.ParsingStart;
import ru.vachok.messenger.MessageCons;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;


/**
 @since 30.08.2018 (21:29) */
public class WatchDogNorah implements Runnable {

    /**
     {@link }
     */
    private MessageToUser messageToUser = new ESender(RCPTS);

    private boolean test;

    private final Runnable goRun = () -> {
        ScheduledExecutorService scheduledExecutorService = Executors
                .newSingleThreadScheduledExecutor();
        Runnable parseRun = new ParsingStart("http://hous01.ethosdistro.com/?json=yes", test);
        scheduledExecutorService.scheduleWithFixedDelay(parseRun,
                ConstantsFor.INITIAL_DELAY,
                ECheck.delay,
                TimeUnit.SECONDS);
        scheduledExecutorService.schedule(parseRun, ConstantsFor.DELAY, TimeUnit.SECONDS);
    };


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
        Map<Long, Long> schedulerGetDelayMap = schedulerGetDelay();
        StringBuilder stringBuilder = new StringBuilder();
        schedulerGetDelayMap.forEach((x, y) -> {
            stringBuilder.append(x).append(" initDelay   ;   ").append(y).append(" delay");
        });
        if(ECheck.isShouldISend()){
            messageToUser.info(SOURCE_CLASS,
                    "run at " + new Date(), stringBuilder.toString());
        }
        else{
            messageToUser.info(SOURCE_CLASS,
                    ECheck.getStopHours() + " stophrs", new TForms().toStringFromArray(System.getenv()));
        }
    }

    private Map<Long, Long> schedulerGetDelay() {
        ECheck.getI();
        long delay = ConstantsFor.DELAY;
        long initDelay = new Random().nextInt(( int ) ConstantsFor.DELAY / 3);
        Map<Long, Long> map = new HashMap<>();
        ThreadPoolExecutor.AbortPolicy abortPolicy = new ThreadPoolExecutor.AbortPolicy();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        if(ECheck.isShouldISend()){
            goRun.run();
        }
        else{
            if(ECheck.getStopHours() > 0){
                Runnable command = new ParsingStart(test);
                long l = TimeUnit.HOURS.toSeconds(ECheck.getStopHours());
                scheduledExecutorService.scheduleWithFixedDelay(command,
                        initDelay,
                        l,
                        TimeUnit.SECONDS);

                if(!ECheck.isShouldISend()){
                    abortPolicy.rejectedExecution(command, ( ThreadPoolExecutor ) scheduledExecutorService);
                }
            }
            else{
                if(ECheck.getStopHours()==-1){
                    ECheck.getI();
                    goRun.run();
                }
                else{
                    map = new HashMap<>();
                    map.put(initDelay, delay);
                }
            }
        }
        return map;
    }

    /*Private metsods*/
    private void checkFile() {
        boolean b = ECheck.getI().isShouldISend();
        RCPTS.add(ConstantsFor.MY_MAIL);
        File ansJSON = new File("answer.json");
        long l = System.currentTimeMillis() - ansJSON.lastModified();
        if(l > TimeUnit.MINUTES.toMillis(5)){
            messageToUser.errorAlert(ConstantsFor.APP_NAME + "." + SOURCE_CLASS,
                    "ERROR. In Progress = " + b, new TForms().toStringFromArray(System.getenv()));
        }
        else{
            this.messageToUser = new MessageCons();
            messageToUser.info(ansJSON.getAbsolutePath(), "OK. In Progress = " + b,
                    " last mod: " + l + "  msec (" + TimeUnit.MILLISECONDS.toMinutes(l) + " min) ago");
        }
        ECheck.scheduleStart(test);
    }
}