package ru.vachok.ethosdistro.util;


/**
 * @since 25.08.2018 (12:04)
 */
public interface Charsets {

   String fromString(String toDecode);
   String fromBytes(byte[] toDecode);

}
