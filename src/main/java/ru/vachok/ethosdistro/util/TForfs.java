package ru.vachok.ethosdistro.util;


import java.util.Arrays;
import java.util.List;


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
      String sRet = Arrays.toString(stackTrace)
            .replaceAll(", ", "\n");
      sRet = sRet.replace("\\Q]\\E", "").replace("\\Q[\\E", "");
      return sRet;
   }

   public String toStringFromArray(List<String> listString) {
      return Arrays.toString(listString.toArray())
            .replaceAll(", ", "\n")
            .replace("\\Q]\\E", "")
            .replace("\\Q[\\E", "");
   }
}