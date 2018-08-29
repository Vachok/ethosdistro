package ru.vachok.ethosdistro;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/**
 <h1>Список констант</h1>

 @since 23.08.2018 (17:12) */
public enum ConstantsFor {
   ;

   /**
    1 мегабайт в байтах
    */
   public static final int MEGABYTE = 1024 * 1024;

   /**
    Время первоначального простоя
    */
   public static final long INITIAL_DELAY = 2L;

   /**
    Задержка между проверками по-умолчанию
    */
   public static final long DELAY = 60L;

   /**
    Список почтовых адресов
    */
   public static final List<String> RCPT = new ArrayList<>();

   /**
    ID майнинг-девайсов
    */
   public static final String[] DEVICES = {"9cee2f", "3d7d93", "6f9b07"};

   /**
    * Кирина почта
    */
   public static final String KIR_MAIL = "pivovarov.kirill@gmail.com";

   /**
    * Моя почта
    */
   public static final String MY_MAIL = "143500@gmail.com";

   /**
    * Таймстэмп - старта.
    */
   public static final long START_TIME_IN_MILLIS = System.currentTimeMillis();

   /**
    * Паттерн для {@code array.tostring}
    */
   public static final Pattern AR_SEMI_PATTERN = Pattern.compile(", ");
}
