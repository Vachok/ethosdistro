package ru.vachok.ethosdistro.util;


import java.util.Arrays;


/**
 @since 26.08.2018 (13:50) */
public class TForfs {

   /**
    Simple Name класса, для поиска настроек
    */
   private static final String SOURCE_CLASS = TForfs.class.getSimpleName();

   public String toStringFromArray(String[] stringsArray) {
      return Arrays.toString(stringsArray)
            .replaceAll(", ", "\n")
            .replace("\\Q]\\E", "")
            .replace("\\Q[\\E", "");
   }

   public String toStringFromArray(StackTraceElement[] stackTrace) {
      return Arrays.toString(stackTrace)
            .replaceAll(", ", "\n")
            .replace("\\Q]\\E", "")
            .replace("\\Q[\\E", "");
   }
}