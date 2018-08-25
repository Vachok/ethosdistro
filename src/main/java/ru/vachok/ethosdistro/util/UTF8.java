package ru.vachok.ethosdistro.util;


import java.nio.charset.StandardCharsets;


/**
 @since 25.08.2018 (12:06) */
public class UTF8 implements Charsets {

   @Override
   public String fromString(String toDecode) {
      return new String(toDecode.getBytes(), StandardCharsets.UTF_8);
   }

   @Override
   public String fromBytes(byte[] toDecode) {
      return new String(toDecode, StandardCharsets.UTF_8);
   }

   /**
    Simple Name класса, для поиска настроек
    */
   private static final String SOURCE_CLASS = UTF8.class.getSimpleName();


}