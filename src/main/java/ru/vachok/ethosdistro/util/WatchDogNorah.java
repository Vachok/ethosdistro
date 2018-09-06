package ru.vachok.ethosdistro.util;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.email.ECheck;
import ru.vachok.ethosdistro.parser.ParsingStart;
import ru.vachok.messenger.MessageToUser;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;


/**
 @since 30.08.2018 (21:29) */
public class WatchDogNorah extends Thread implements Runnable {


    /**
     Simple Name класса, для поиска настроек
     */
    private static final String SOURCE_CLASS = WatchDogNorah.class.getSimpleName();

    private static final MessageToUser MESSAGE_TO_USER = new FileLogger();

    private static Timer timer = new Timer("TimerParse");

    private static boolean test;

    private static TimerTask parseFile = new ParsingStart(test);

    /*Constru*/
    public WatchDogNorah(boolean test) {
        WatchDogNorah.test = test;
    }

    /*Instances*/
    public WatchDogNorah() {
        test = false;
        MESSAGE_TO_USER.infoNoTitles(this.getName());
    }

    @Override
    public void run() {
        Thread.currentThread().setName("NORAH");
        MESSAGE_TO_USER.info(SOURCE_CLASS,
                "parsing scheduled at ",
                new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(schedulerGetDelay())).toString());
        Thread.dumpStack();
    }

    private static int schedulerGetDelay() throws RejectedExecutionException {
        int stopMinutes = ECheck.getStopHours();
        boolean startOrFalse = ECheck.isGetShould();
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
                    MESSAGE_TO_USER.infoNoTitles(
                            "Starting mine parser with " +
                                    period +
                                    " of seconds. Last SENT DATE change = " +
                                    stopMinutes +
                                    " minutes");
                    period = TimeUnit.SECONDS.toMillis(ConstantsFor.DELAY_IN_SECONDS - 30);
                    timer.schedule(parseFile, new Date(), period);
                    return ( int ) period;
                }
                catch(IllegalStateException e){
                    Timer timerAfterCancel = new Timer("After Cancel");
                    timerAfterCancel.schedule(parseFile, new Date(), period);
                    MESSAGE_TO_USER.info(
                            "IllegalStateException", e.getMessage(),
                            "catching NEW Timer. " + timerAfterCancel.toString());
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
            MessageToUser messageToUser = new FileLogger();
            messageToUser.errorAlert(
                    SOURCE_CLASS, "sendLogs", e.getMessage() + "\n" + new TForms().fromArray(e.getStackTrace()));
        }
        MESSAGE_TO_USER.infoNoTitles("Start new timer with " + stopMinutes + " min period ");
        ECheck.setShouldOrFalse(false);
        return stopMinutes;
    }

    /*Private metsods*/

    /**
     Returns the state of this thread.
     This method is designed for use in monitoring of the system state,
     not for synchronization control.

     @return this thread's state.
     @since 1.5
     */
    @Override
    public State getState() {
        State state = Thread.currentThread().getState();
        MESSAGE_TO_USER.infoNoTitles(state.toString());
        return state;
    }
}