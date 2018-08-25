package ru.vachok.ethosdistro;


import java.util.ArrayList;
import java.util.List;


/**
 <h1>Список констант</h1>

 @since 23.08.2018 (17:12) */
public enum ConstantsFor {
   ;

   /**
    1 мегабайт в байтах
    */
   public static final int MEGABYTE = 1024 * 1024;

   public static final long INITIAL_DELAY = 10L;

   public static final long DELAY = 60L;

   public static final List<String> RCPT = new ArrayList<>();

   public static final String[] DEVICES = {"9cee2f","3d7d93", "6f9b07"};
   public static final String KIR_MAIL = "pivovarov.kirill@gmail.com";
}
