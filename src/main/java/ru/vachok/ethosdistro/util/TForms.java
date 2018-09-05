package ru.vachok.ethosdistro.util;


import java.util.List;


/**
 <h1>Переделывает архивы в читаемые строчки</h1>

 @since 26.08.2018 (13:50) */
public class TForms {

    /**
     * {@link StringBuilder}
     */
    private static final StringBuilder stringBuilder = new StringBuilder();

    /**
     @param stringsArray простой массив строк
     @return строки для вывода
     */
    public String fromArray(String[] stringsArray) {
        for(String s : stringsArray){
            stringBuilder.append(s).append("\n").trimToSize();
        }
        return stringBuilder.toString();
    }

    /**
     @param stackTrace стэк ошибок
     @return строки для вывода
     */
    public String fromArray(StackTraceElement[] stackTrace) {
        for(StackTraceElement stackTraceElement : stackTrace){
            String sTrE = stackTraceElement.toString();
            if(sTrE.toLowerCase().contains("ru.")){
                stringBuilder.append(">>>>     ");
            }
            stringBuilder.append(sTrE).append("\n").trimToSize();
        }
        return stringBuilder.toString();
    }

    public String collist(List<?> coList) {
        for(Object o : coList){
            stringBuilder.append(o.toString()).append("\n");
        }
        return "~~" + stringBuilder.toString();
    }

    public String replaceChars(String stringToRepl, String replaceThis, String replacement) {
        return stringToRepl.replaceAll(replaceThis, replacement);
    }

    public String fromArray(List<String> stringList) {
        for(String s : stringList){
            stringBuilder.append(s).append("\n");
        }
        return stringBuilder.toString();
    }

    public String toStringFromArray(StackTraceElement[] stackTrace) {
        for(StackTraceElement e : stackTrace){
            stringBuilder.append(e.toString()).append("\n");
        }
        return stringBuilder.toString();
    }
}