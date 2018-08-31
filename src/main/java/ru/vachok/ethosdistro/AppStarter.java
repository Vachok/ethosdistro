package ru.vachok.ethosdistro;


import ru.vachok.ethosdistro.email.ECheck;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.TForms;
import ru.vachok.messenger.MessageToUser;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Logger;


/*Private metsods*/

/**
 <h1>Стартовый класс приложения</h1>

 @since 23.08.2018 (15:34) */
public class AppStarter {

    private static boolean test = false;


    /**
     Class Simple Name
     */
    private static final String SOURCE_CLASS = AppStarter.class.getSimpleName();

    private static final Logger logger = Logger.getLogger(SOURCE_CLASS);


    /**
     {@link DBLogger}
     */
    private static final MessageToUser MESSAGE_TO_USER = new DBLogger();

    /*PS Methods*/

    /**
     <b>Старт.</b>
     <p>
     1. {@link TForms#toStringFromArray(String[])}
     2. {@link #argsReader(String[])}
     3. {@link #mailAdd(String)}

     @param args the input arguments
     */
    public static void main(String[] args) {
        if(args.length > 0){
            MESSAGE_TO_USER
                    .info(SOURCE_CLASS,
                            "Arguments",
                            "Starting at: " + new Date() + "\n" + new TForms().toStringFromArray(args));
            argsReader(args);
        }
        else{
            ConstantsFor.RCPT.add(ConstantsFor.KIR_MAIL);
            MESSAGE_TO_USER.info(SOURCE_CLASS, "Argument - none", new Date() + "   " + ECheck.scheduleStart(test));
        }
    }

    /**
     <b>Парсер параметров запуска</b>
     1. {@link #mailAdd(String)}
     2. {@link ECheck#scheduleStart(boolean)}

     @param args параметры запуска. Через {@code "-par} <i>val"</i>
     */
    private static void argsReader(String[] args) {
        String stringArgs = Arrays.toString(args)
                .replaceAll(ConstantsFor.AR_SEMI_PATTERN.pattern(), ":");

        args = stringArgs.split("-");
        for(String argument : args){
            try{
                String key = argument.split(":")[0];
                String value = argument.split(":")[1];
                if(key.equalsIgnoreCase("d")){
                    ECheck.delay = Long.parseLong(value);
                }
                else{
                    ECheck.delay = ConstantsFor.DELAY;
                }
                if(key.equalsIgnoreCase("t")){
                    ConstantsFor.RCPT.add(ConstantsFor.MY_MAIL);
                    test = true;
                }
                if(key.equalsIgnoreCase("e")){
                    mailAdd(value);
                }
                else{
                    MESSAGE_TO_USER.infoNoTitles(ECheck.scheduleStart(test));
                }
            }
            catch(Exception e){
                MESSAGE_TO_USER.info(SOURCE_CLASS, "Starting", "with args");
            }
        }
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
}
