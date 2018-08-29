package ru.vachok.ethosdistro;


import ru.vachok.ethosdistro.parser.ParsingStart;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.FileLogger;
import ru.vachok.ethosdistro.util.TForfs;
import ru.vachok.messenger.MessageToUser;

import java.text.MessageFormat;
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

    /**
     Class Simple Name
     */
    private static final String SOURCE_CLASS = AppStarter.class.getSimpleName();

    private static final Logger logger = Logger.getLogger(SOURCE_CLASS);

    private static final MessageToUser MESSAGE_TO_USER = new DBLogger();

    private static long delay = ConstantsFor.DELAY;

    private static boolean test = false;

    /*PS Methods*/

    /**
     <b>Старт.</b>
     <p>
     1. {@link TForfs#toStringFromArray(String[])}
     2. {@link #argsReader(String[])}
     3. {@link #mailAdd(String)}

     @param args the input arguments
     */
    public static void main(String[] args) {
        if(args.length > 0){
            MESSAGE_TO_USER
                    .info(SOURCE_CLASS,
                            "Arguments",
                            "Starting at: " + new Date() + "\n" + new TForfs().toStringFromArray(args));
            argsReader(args);
        }
        else{
            ConstantsFor.RCPT.add(ConstantsFor.KIR_MAIL);
            MESSAGE_TO_USER.info(SOURCE_CLASS, "Argument - none", new Date() + "   " + scheduleStart(test));
        }
    }

    private static void argsReader(String[] args) {
        String stringArgs = Arrays.toString(args)
                .replaceAll(ConstantsFor.AR_SEMI_PATTERN.pattern(), ":");
        logger.info(stringArgs);
        args = stringArgs.split("-");
        for(String argument : args){
            try{
                String key = argument.split(":")[0];
                String value = argument.split(":")[1];
                if(key.equalsIgnoreCase("d")){
                    delay = Long.parseLong(value);
                }
                else{
                    delay = ConstantsFor.DELAY;
                }
                if(key.equalsIgnoreCase("t")){
                    ConstantsFor.RCPT.add(ConstantsFor.MY_MAIL);
                    test = true;
                }
                if(key.equalsIgnoreCase("e")){
                    mailAdd(value);
                }
                else{
                    MESSAGE_TO_USER.infoNoTitles(scheduleStart(test));
                }
            }
            catch(Exception e){
                MESSAGE_TO_USER.info(SOURCE_CLASS, "Starting", "with args");
            }
        }
    }

    private static String scheduleStart(boolean test) {
        MessageToUser messageToUser = new FileLogger();
        ScheduledExecutorService scheduledExecutorService =
                Executors
                        .unconfigurableScheduledExecutorService(Executors
                                .newSingleThreadScheduledExecutor());
        Runnable parseRun = new ParsingStart("http://hous01.ethosdistro.com/?json=yes", test);
        scheduledExecutorService.scheduleWithFixedDelay(parseRun,
                ConstantsFor
                        .INITIAL_DELAY, delay, TimeUnit
                        .SECONDS);
        messageToUser.info(SOURCE_CLASS, "scheduleStart", parseRun.toString());
        return "Runnable parseRun = new ParsingStart(\"http://hous01.ethosdistro.com/?json=yes\", " + test + ");";
    }

    private static void mailAdd(String value) {
        ConstantsFor.RCPT.clear();
        String[] values = value.split(",");
        for(String mailAddr : values){
            ConstantsFor.RCPT.add(mailAddr
                    .replaceAll("\\Q]\\E", ""));
            String s = ConstantsFor.RCPT.toString();
            String format = MessageFormat.format("emails: {0}", s);
            logger.info(format);
        }
    }
//unstat
}
