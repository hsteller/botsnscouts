package de.botsnscouts.util;

/**
 * LanguageLoadException wird bei Abweichungen vom messages-FileFormat
 * oder fehlendem messages.<sprache> geworfen.
 *
 */

public class LanguageLoadException extends Exception {
  public LanguageLoadException () {
    super();
  }
  public LanguageLoadException (String s) {
    super (s);
  }
  
}
