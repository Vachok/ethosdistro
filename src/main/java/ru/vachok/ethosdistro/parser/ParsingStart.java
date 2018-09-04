package ru.vachok.ethosdistro.parser;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.FileLogger;
import ru.vachok.ethosdistro.util.TForms;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.MessagesNull;
import ru.vachok.messenger.email.ESender;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


/**
 <h1>Запуск парсера</h1>

 @since 23.08.2018 (16:48) */
public class ParsingStart extends TimerTask {

    /**
     {@link Parsers}
     */
    private Parsers parsers;

    private final MessageToUser fileLogger = new MessagesNull();

    /**
     Параметр {@code -t on}
     */
    private final boolean test;

    /**
     URL, как строка
     */
    private final String urlAsString;

    /**
     Class Simple Name
     */
    private static final String SOURCE_CLASS = ParsingStart.class.getSimpleName();

    public ParsingStart(boolean isTest) {
        super();
        this.urlAsString = ConstantsFor.URL_AS_STRING;
        this.test = isTest;
    }

    @Override
    public boolean cancel() {
        Thread.currentThread().interrupt();
        return super.cancel();
    }

    /**
     {@link DBLogger}
     */
    private static final MessageToUser TO_USER_DATABASE = new DBLogger();

    @Override
    public long scheduledExecutionTime() {
        return System.currentTimeMillis() - super.scheduledExecutionTime();
    }

    private URL getUrlFromStr() {
        URL url = null;
        try{
            url = new URL(urlAsString);
        }
        catch(MalformedURLException e){
            ConstantsFor.SEND_MAIL_AND_DB.accept(e.getMessage(), new TForms().fromArray(e.getStackTrace()));
        }
        return url;
    }

    /*Constru*/
    @Override
    public void run() {
        String upTime = "END (Uptime = " + ( float ) (System.currentTimeMillis() - ConstantsFor
                .START_TIME_IN_MILLIS) / TimeUnit.HOURS.toMillis(1) + " hrs)";
        TO_USER_DATABASE.info(SOURCE_CLASS, "RUNTIME - " + upTime, "NOW TIME: " + new Date().toString());
        this.parsers = new ParseToFile();
        URL url = getUrlFromStr();
        parsers.startParsing(url);
        sendRes(this.test);
    }
    /*Private metsods*/
    private void sendRes(boolean testOn) {
        String returnString = new ParsingFinalize().call();
        boolean call = false;
        if(returnString.contains("false")) call = true;
        if(testOn) call = !call;
        File file = new File("answer.json");
        MessageToUser emailS = new ESender(ConstantsFor.RCPT);
        MessageToUser log = new FileLogger();
        log.info(SOURCE_CLASS, "ConstantsFor.RCPT.size() ", ConstantsFor.RCPT.size() + " address");
        TO_USER_DATABASE.infoNoTitles(ConstantsFor.RCPT.toString());
        if(call){
            String statisticsProb = new Date(file.lastModified()) + "\n" + file.getAbsolutePath() +
                    " last modified: " + new Date(file.lastModified());
            String freeSpaceOnDisk = file.getFreeSpace() / ConstantsFor.MEGABYTE + " free space in Megabytes";
            fileLogger.info(SOURCE_CLASS, freeSpaceOnDisk, statisticsProb);
        }
        else{
            String[] split = returnString.split("~~");
            String subjectIP = split[0];
            String bodyJSON;
            try{
                bodyJSON = split[1];
            }
            catch(ArrayIndexOutOfBoundsException e){
                bodyJSON = "NO INFO";
            }
            if(ConstantsFor.RCPT.isEmpty()){
                ConstantsFor.RCPT.add(ConstantsFor.KIR_MAIL);
            }
            new TForms().fromArray(ConstantsFor.RCPT);
            emailS.errorAlert(subjectIP, "Mine~ALARM",
                    new String(("ЙА ПРОГРАММО! \n" +
                                        "ЕСЛИ Я ТЕБЯ ЗАЕБАЛА, ПРОСТО ОТВЕТЬ В ТЕМЕ ПИСЬМА!\n" +
                                        " БЕЗ ПРОБЕЛОВ (сотри alarm и ок):\n" +
                                        "mine~0 - выключит меня на 24 часа\n" +
                                        "mine~10 - выключит на 10мин. " +
                                        "(тут в минутах можно выбрать на сколько, mine~20 = 20min, etc...\n" +
                                        "mine~ - приведёт меня в чувство в течении пары минут!)")
                            .getBytes(),
                            StandardCharsets.UTF_8) + "\n"
                            + new TForms().replaceChars(bodyJSON, ",", "\n") +
                    "   | NO MINING! " + urlAsString);
            TO_USER_DATABASE.errorAlert("ALARM!", "Condition not mining",

                    new String(("ЙА ПРОГРАММО! \n" +
                                        "ЕСЛИ Я ТЕБЯ ЗАЕБАЛА, ПРОСТО ОТВЕТЬ В ТЕМЕ ПИСЬМА!\n" +
                                        " БЕЗ ПРОБЕЛОВ (сотри alarm и ок):\n" +
                                        "mine~0 - выключит меня на 24 часа\n" +
                                        "mine~10 - выключит на 10мин. " +
                                        "(тут в минутах можно выбрать на сколько, mine~20 = 20min, etc...\n" +
                                        "mine~ - приведёт меня в чувство в течении пары минут!)")
                            .getBytes(),
                            StandardCharsets.UTF_8) +
                            returnString + "   | NO MINING! " + urlAsString);
        }
    }

}
