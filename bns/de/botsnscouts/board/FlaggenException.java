package de.botsnscouts.board;

/**
 * FlaggenException wird bei Abweichungen vom Spielfelddatenformat geworfen.
 * @author: Dirk Materlik
 */

public class FlaggenException extends Exception {
  public FlaggenException () {
    super();
  }
  public FlaggenException (String s) {
    super (s);
  }
  
}
