package de.botsnscouts.board;

/**
 * FormatException wird bei Abweichungen vom Spielfelddatenformat geworfen.
 * @author: Dirk Materlik, Gero Eggers
 */

public class FormatException extends Exception {
  public FormatException () {
    super();
  }
  public FormatException (String s) {
    super (s);
  }
  
}
