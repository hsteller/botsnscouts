package de.botsnscouts.util;

/**
 * FormatException wird bei Abweichungen vom Spielfelddatenformat geworfen.
 * @author: Dirk Materlik
 */

public class FormatException extends Exception {
  public FormatException () {
    super();
  }
  public FormatException (String s) {
    super (s);
  }
  
}
