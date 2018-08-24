package ru.vachok.ethosdistro.parser;


import java.net.URL;


/**
 @since 24.08.2018 (14:09) */
public interface Parsers {

   String startParsing(URL url);

   boolean sendResult(String result);
}
