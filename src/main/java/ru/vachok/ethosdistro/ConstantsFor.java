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

   public static final long INITIAL_DELAY = 2L;

   public static final long DELAY = 60L;

   public static final List<String> RCPT = new ArrayList<>();

   public static final String[] DEVICES = {"9cee2f", "3d7d93", "6f9b07"};

   public static final String KIR_MAIL = "pivovarov.kirill@gmail.com";

   public static final String MY_MAIL = "143500@gmail.com";

   public static final long START_TIME_IN_MILLIS = System.currentTimeMillis();

   public static final Pattern AR_SEMI_PATTERN = Pattern.compile(", ");

   public static final double ONE_HOUR_IN_MIN = 60.0;
}
