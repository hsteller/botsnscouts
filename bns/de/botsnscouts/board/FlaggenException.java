package de.botsnscouts.util;

/**
 * FlaggenException wird bei Abweichungen vom Spielfelddatenformat geworfen.
 * @author: Dirk Materlik, Gero Eggers
 */

public class FlaggenException extends Exception {
  public FlaggenException () {
    super();
  }
  public FlaggenException (String s) {
    super (s);
  }
  
}
