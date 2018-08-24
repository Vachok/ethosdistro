package ru.vachok.ethosdistro;


import org.testng.annotations.Test;
import ru.vachok.ethosdistro.parser.ParseJsonAsUsualString;
import ru.vachok.ethosdistro.parser.Parsers;
import ru.vachok.ethosdistro.parser.ParsingStart;


public class AppStarterTest {

   @Test (testName = "File Saver")
   public void testMain() {
      String urlAsString = "http://hous01.ethosdistro.com/?json=yes";
      ParsingStart parsingStart = new ParsingStart(urlAsString, null);
      parsingStart.run();
      String fileName = "answer.html";
      ParsingStart parseAsJson = new ParsingStart("http://hous01.ethosdistro.com/", fileName);
      parseAsJson.run();
   }

   @Test (testName = "String shower")
   public void testShow() {
      String urlAsString = "http://hous01.ethosdistro.com/?json=yes";
      Parsers jsonAsUsualString = new ParseJsonAsUsualString();
      ParsingStart parsingStart = new ParsingStart(jsonAsUsualString, urlAsString);
      parsingStart.run();
   }
}