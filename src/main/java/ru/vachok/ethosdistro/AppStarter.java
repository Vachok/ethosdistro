package ru.vachok.ethosdistro;


import ru.vachok.ethosdistro.parser.ParsingStart;
import ru.vachok.ethosdistro.util.*;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.mysqlandprops.props.DBRegProperties;
import ru.vachok.mysqlandprops.props.FileProps;
import ru.vachok.mysqlandprops.props.InitProperties;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 <h1>Стартовый класс приложения</h1>

 @since 23.08.2018 (15:34) */
public class AppStarter {

    private static long initialDelay = ConstantsFor.INITIAL_DELAY;

    private static long delay = ConstantsFor.DELAY_IN_SECONDS;

    private static boolean test = false;

    /**
     Class Simple Name
     */
    private static final String SOURCE_CLASS = AppStarter.class.getSimpleName();

    private static final Logger logger = Logger.getLogger(SOURCE_CLASS);

    private static MessageToUser messageToUser = new DBLogger();

    /*PS Methods*/

    /**
     <b>Старт.</b>
     <p>
     1. {@link #argsReader(String[])}

     @param args параметры запуска приложения
     @see ConstantsFor
     @see TForms
     */
    public static void main(String[] args) {
        messageToUser
                .info(
                        ConstantsFor.APP_NAME,
                        ConstantsFor.APP_VER + " app ver.",
                        "start at " + new Date(ConstantsFor.START_TIME_IN_MILLIS));
        if(args.length > 0){
            String msg = SOURCE_CLASS + " " +
                    "Arguments" + " " +
                    "Starting at: " +
                    new Date() + "\n" + new TForms().fromArray(args);
            logger.info(msg);
            argsReader(args);
        }
        else{
            ConstantsFor.RCPT.add(ConstantsFor.KIR_MAIL);
            try{
                String msg = SOURCE_CLASS + " " +
                        "Argument - none" + " " +
                        new Date() + "   " + scheduleStart(test);
                logger.info(msg);
            }
            catch(Exception e){
                messageToUser = new FileLogger();
                logger.throwing(SOURCE_CLASS, "main", e);
                messageToUser.errorAlert(SOURCE_CLASS, "main", e.getMessage());
            }
        }
    }

    /*Private metsods*/
    private static void argsReader(String[] args) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter
                .ofPattern("yyyy-MMM-dd hh:mm");
        String startTime = dateTimeFormatter.format(LocalDateTime.now());
        String stringArgs = Arrays.toString(args)
                .replaceAll(", ", ":");
        logger.info(stringArgs);
        args = stringArgs.split("-");
        for(String argument : args){
            try{
                String key = argument.split(":")[0];
                String value = argument.split(":")[1];
                if(key.equalsIgnoreCase("d")){
                    delay = Long.parseLong(value);
                }
                if(key.equalsIgnoreCase("i")){
                    initialDelay = Long.parseLong(value);
                    continue;
                }
                else{
                    initialDelay = ConstantsFor.INITIAL_DELAY;
                }
                if(key.equalsIgnoreCase("t")){
                    ConstantsFor.RCPT.add(ConstantsFor.MY_MAIL);
                    test = true;
                    continue;
                }
                else{
                    test = false;
                }
                if(key.equalsIgnoreCase("e")){
                    ConstantsFor.RCPT.add(value);
                }
                else{
                    messageToUser.infoNoTitles(scheduleStart(test));
                }
            }
            catch(Exception e){
                ConstantsFor.SEND_MAIL_AND_DB.accept(e.getMessage(), new TForms().fromArray(e.getStackTrace()));
            }

        }
        messageToUser.info(AppStarter.class.getName(), startTime, "Initializing " +
                ParsingStart.class.getName() + " with " + delay +
                " seconds delay..." + scheduleStart(test));
    }

    /**
     <b>Запуск планировщика отслеживания.</b> {@link #main(String[])}
     Параметры по-умолчанию:
     <p>
     delay - 60 sec
     <p>
     init - 2 sec

     @param test обращает условие срабатывания в противоположное
     @return {@code "Runnable parseRun = new ParsingStart(http://hous01.ethosdistro.com/?json=yes Test is "+test;) }
     */
    private static String scheduleStart(boolean test) {
        ScheduledExecutorService scheduledExecutorService =
                Executors.unconfigurableScheduledExecutorService(Executors.newSingleThreadScheduledExecutor());
        try{
            Runnable watchDogNorahIsCheckingMail = new WatchDogNorah(test);
            scheduledExecutorService.scheduleWithFixedDelay(
                    watchDogNorahIsCheckingMail,
                    initialDelay,
                    delay,
                    TimeUnit.SECONDS);
            scheduledExecutorService.scheduleWithFixedDelay(
                    watchDogNorahIsCheckingMail,
                    ConstantsFor.INITIAL_DELAY,
                    ConstantsFor.DELAY_IN_SECONDS,
                    TimeUnit.SECONDS);
        }
        catch(Exception e){
            logger.warning(e.getMessage());
        }
        return "Runnable watchDogNorahIsCheckingMail = new WatchDogNorah(test) Test is " + test;
    }

    private static void shutdownHook(ScheduledExecutorService scheduledExecutorService) {
        InitProperties[] propsInitors =
                {new DBRegProperties(ConstantsFor.APP_NAME + SOURCE_CLASS), new FileProps(SOURCE_CLASS)};
        for(InitProperties i : propsInitors){
            i.setProps(ConstantsFor.sysProperties);
        }
        String stopCauses = scheduledExecutorService.isShutdown() +
                " is shutdown, " +
                scheduledExecutorService.isTerminated() +
                " is term";
        logger.info(stopCauses);
    }
}
