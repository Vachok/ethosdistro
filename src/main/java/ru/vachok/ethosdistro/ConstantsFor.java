package ru.vachok.ethosdistro;


import ru.vachok.ethosdistro.util.DBLogger;
import ru.vachok.ethosdistro.util.TForms;
import ru.vachok.messenger.MessageToUser;
import ru.vachok.messenger.email.ESender;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;


/**
 <h1>Список констант</h1>

 @since 23.08.2018 (17:12) */
public enum ConstantsFor {
    ;

    /*Fields*/

    /**
     1 мегабайт в байтах
     */
    public static final int MEGABYTE = 1024 * 1024;

    /**
     Время первоначального простоя
     */
    public static final long INITIAL_DELAY = 4L;

    /**
     Задержка между проверками по-умолчанию
     */
    public static final long DELAY_IN_SECONDS = 33L;

    /**
     Список почтовых адресов
     */
    public static final List<String> RCPT = new ArrayList<>();

    /**
     ID майнинг-девайсов
     */
    public static final String[] DEVICES = {"9cee2f", "3d7d93", "6f9b07"};

    /**
     Кирина почта
     */
    public static final String KIR_MAIL = "pivovarov.kirill@gmail.com";

    /**
     Моя почта
     */
    public static final String MY_MAIL = "143500@gmail.com";

    /**
     Таймстэмп - старта.
     */
    public static final long START_TIME_IN_MILLIS = System.currentTimeMillis();

    public static final String URL_AS_STRING = "http://hous01.ethosdistro.com/?json=yes";

    /**
     <b>Оправить сообщение одновременно в почту и базу</b>
     */
    public static final BiConsumer<String, String> SEND_MAIL_AND_DB = (x, y) -> {
        List<String> rcpt = new ArrayList<>();
        rcpt.add(ConstantsFor.MY_MAIL);
        MessageToUser[] messagesToUser = {new ESender(rcpt), new DBLogger("ru_vachok_ethosdistro_tests")};
        for(MessageToUser messageToUser : messagesToUser){
            messageToUser.info(new Date() + "|  ", x, y);
        }
    };

    /**
     <b>Узнать имя локального ПК</b>
     */
    public static final String PC_NAME = getPCName();

    /**
     @return {@link #PC_NAME}
     */
    private static String getPCName() {
        try{
            return InetAddress.getLocalHost().getHostName();
        }
        catch(UnknownHostException e){
            ConstantsFor.SEND_MAIL_AND_DB.accept(e.getMessage(), new TForms().fromArray(e.getStackTrace()));
            return "NO NAME";
        }

    }
}
