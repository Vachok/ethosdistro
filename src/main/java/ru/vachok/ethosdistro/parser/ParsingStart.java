package ru.vachok.ethosdistro.parser;


import ru.vachok.ethosdistro.ConstantsFor;
import ru.vachok.ethosdistro.email.ECheck;
import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.FileLogger;
import ru.vachok.ethosdistro.util.TForfs;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;


/**
 <h1>Запуск парсера</h1>

 @since 23.08.2018 (16:48) */
public class ParsingStart implements Runnable {

    /**
     <b>Проверка наличия временной остановки</b>
     */
    static boolean shouldIWork = ECheck.isShouldIWork();

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

    /**
     URL, как строка
     */
    private final String urlAsString;

    /**
     {@link Parsers}
     */
    private Parsers parsers;

    /**
     Параметр {@code -t on}
     */
    private boolean test;

    /*Constru*/

    /**
     Конструктор

     @param parsers     чем парсим {@link Parsers}
     @param urlAsString url как строка
     */
    public ParsingStart(Parsers parsers, String urlAsString) {
        this.parsers = parsers;
        this.urlAsString = urlAsString;
        test = false;
    }

    /**
     Конструктор

     @param s    url как строка
     @param test test - запуск с <i>обратным !</i> условием.
     */
    public ParsingStart(String s, boolean test) {
        this.urlAsString = s;
        this.test = test;
    }

    /**
     <b>Старт парсинга</b>
     <p>
     1. Создание инстанции {@link ParseToFile} <br>
     2. Создание answer.json {@link Parsers#startParsing(URL)} <br>
     3. Вывод сообщения через {@link #TO_USER_DATABASE} - {@link ConstantsFor#RCPT} <br>
     4. {@link #sendRes(boolean)} <br>

     @see TForfs
     */
    @Override
    public void run() {
        this.parsers = new ParseToFile();
        URL url = getUrlFromStr();
        parsers.startParsing(url);
        String s = new TForfs().toStringFromArray(ConstantsFor.RCPT);
        TO_USER_DATABASE.info(SOURCE_CLASS, "email RCPTs", s);
        if(shouldIWork){
            sendRes(this.test);
        }
        else{
            Thread.currentThread().interrupt();
        }
    }

    private URL getUrlFromStr() {
        URL url = null;
        try{
            url = new URL(urlAsString);
        }
        catch(MalformedURLException e){
            LOGGER.throwing(SOURCE_CLASS, "getSite", e);
        }
        return url;
    }

    /**
     <b>Отправить уведомление</b>
     <p>
     Если {@link #test} правда. Проверить {@link ParsingFinalize#call()} и инвертировать его выдачу. <i>(ложь-на-правда; правда-на-ложь)</i><br>
     Если нет - продолжить с уловием {@link ParsingFinalize#call()}.
     <p>
     {@code MessageToUser emailS = new ESender(ConstantsFor.RCPT);} - опреденение способа отправки e-mail сообщения.<br>
     Если условия удовлетворяют ({@link ParsingFinalize#call()} = <b>true</b>, записать через {@link MessageToUser} 2 лога: в базу (<i>server202.reg</i>) и файл (<i>log.log</i>) <br>
     Если нет - отравить уведомнение списку {@link ConstantsFor#RCPT}

     @param callTest {@link #test}
     @see ParsingFinalize
     @see TForfs
     */
    private void sendRes(boolean callTest) {
        Boolean call;
        if(callTest){
            call = !new ParsingFinalize().call();
        }
        else{
            call = new ParsingFinalize().call();
        }
        File file = new File("answer.json");
        MessageToUser emailS = new ESender(ConstantsFor.RCPT);
        MessageToUser fileLogger = new FileLogger();
        fileLogger.info(SOURCE_CLASS, "ConstantsFor.RCPT", "Сообщение будет отправлено на этот список: " + ConstantsFor.RCPT.size() + "\n" + new TForfs().toStringFromArray(ConstantsFor.RCPT));
        if(call){
            String statisticsProg = new Date(file.lastModified()) + "\n" + file.getAbsolutePath();
            String freeSpaseOnDisk = file.getFreeSpace() / ConstantsFor.MEGABYTE + " free space in Megabytes";

            TO_USER_DATABASE.info(SOURCE_CLASS, freeSpaseOnDisk, statisticsProg);
            fileLogger.info(SOURCE_CLASS, freeSpaseOnDisk, statisticsProg);
            Thread.currentThread().interrupt();
        }
        else{
            emailS.errorAlert("ALARM!", "Condition not mining", "NO MINING! " + urlAsString);
            fileLogger.errorAlert("ALARM!", "Condition not mining", "NO MINING! " + urlAsString);
        }
        TO_USER_DATABASE.infoNoTitles(ECheck.getI().toString());
        Thread.currentThread().interrupt();
    }
}

