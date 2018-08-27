package ru.vachok.ethosdistro.util;


import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


/**<h1>Переделывает архивы в читаемые строчки</h1>
 @since 26.08.2018 (13:50) */
public class TForfs {

   /**
    * Паттерн, для разбора и замены символов в стандарнтов выводе
    */
   private static final Pattern S_COL = Pattern.compile(", ");

   /**
    * Паттерн, для разбора и замены символов в стандарнтов выводе
    */
   private static final String BR_LEFT = "\\Q]\\E";

   /**
    Паттерн, для разбора и замены символов в стандарнтов выводе
    */
   private static final String BR_RIGHT = "\\Q[\\E";

   /**
    @param stringsArray простой массив строк
    @return строки для вывода
    */
   public String toStringFromArray(String[] stringsArray) {

      return Arrays.toString(stringsArray)
            .replaceAll(String.valueOf(S_COL), "\n")
            .replace(BR_LEFT, "")
            .replace(BR_RIGHT, "");
   }

   /**
    @param stackTrace стэк ошибок
    @return строки для вывода
    */
   public String toStringFromArray(StackTraceElement[] stackTrace) {
      String sRet = Arrays.toString(stackTrace)
            .replaceAll(String.valueOf(S_COL), "\n");
      sRet = sRet.replace(BR_LEFT, "").replace(BR_RIGHT, "");
      return sRet;
   }

   /**
    @param listString {@link List} строчек
    @return строки для вывода
    */
   public String toStringFromArray(List<String> listString) {
      return Arrays.toString(listString.toArray())
            .replaceAll(String.valueOf(S_COL), "\n")
            .replace(BR_RIGHT, "")
            .replace(BR_LEFT, "");
   }
}