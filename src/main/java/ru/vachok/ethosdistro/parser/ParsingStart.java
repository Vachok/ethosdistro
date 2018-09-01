package ru.vachok.ethosdistro.parser;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.FileLogger;
import ru.vachok.ethosdistro.util.TForms;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


/**
 <h1>Запуск парсера</h1>

 @since 23.08.2018 (16:48) */
public class ParsingStart implements Runnable {

    /**
     {@link Parsers}
     */
    private Parsers parsers;

    /**
     Параметр {@code -t on}
     */
    private final boolean test;

    private final MessageToUser fileLogger = new FileLogger();

    /**
     URL, как строка
     */
    private final String urlAsString;

    /**
     Class Simple Name
     */
    private static final String SOURCE_CLASS = ParsingStart.class.getSimpleName();

    /**
     {@link Logger}
     */
    private static final Logger LOGGER = Logger.getLogger(SOURCE_CLASS);

    /**
     {@link DBLogger}
     */
    private static final MessageToUser TO_USER_DATABASE = new DBLogger();


    /*Constru*/
    public ParsingStart(boolean test) {
        this.test = test;
        this.urlAsString = ConstantsFor.URL_AS_STRING;
    }

    @Override
    public void run() {
        TO_USER_DATABASE.info(SOURCE_CLASS, "RUN", new Date().toString());
        this.parsers = new ParseToFile();
        URL url = getUrlFromStr();
        parsers.startParsing(url);
        String s = new TForms().toStringFromArray(ConstantsFor.RCPT);
        TO_USER_DATABASE.info(SOURCE_CLASS, "email recep", s);
        sendRes(this.test);
    }

    private URL getUrlFromStr() {
        URL url = null;
        try{
            url = new URL(urlAsString);
        }
        catch(MalformedURLException e){
            ConstantsFor.sendMailAndDB.accept(e.getMessage(), new TForms().toStringFromArray(e.getStackTrace()));
        }
        return url;
    }

    private void sendRes(boolean callTest) {
        Boolean call;
        String returnString = new ParsingFinalize().call();
        if(callTest){
            call = returnString.equalsIgnoreCase("false");
        }
        else{
            call = !returnString.equalsIgnoreCase("false");
        }
        File file = new File("answer.json");
        MessageToUser emailS = new ESender(ConstantsFor.RCPT);
        MessageToUser log = new FileLogger();
        log.info(SOURCE_CLASS, "ConstantsFor.RCPT.size() = ", ConstantsFor.RCPT.size() + "");
        if(call){
            String statisticsProb = new Date(file.lastModified()) + "\n" + file.getAbsolutePath() +
                    " last modified: " + new Date(file.lastModified());
            String freeSpaceOnDisk = file.getFreeSpace() / ConstantsFor.MEGABYTE + " free space in Megabytes";
            TO_USER_DATABASE.info(SOURCE_CLASS,
                    "END (Uptime = " + ( float ) (System.currentTimeMillis() - ConstantsFor
                            .START_TIME_IN_MILLIS) / TimeUnit.HOURS.toMillis(1) + " hrs)",
                    freeSpaceOnDisk + "\n" + statisticsProb);
            fileLogger.info(SOURCE_CLASS, freeSpaceOnDisk, statisticsProb);
        }
        else{
            String[] split = returnString.split("~~");
            String subjectIP = split[0];
            String bodyJSON = split[1];
            emailS.errorAlert(subjectIP, "Mine~ALARM", new TForms().replaceChars(bodyJSON, ",", "\n") +
                    "   | NO MINING! " + urlAsString);
            fileLogger.errorAlert("ALARM!", "Condition not mining", returnString +
                    "   | NO MINING! " + urlAsString);
        }
    }

}
