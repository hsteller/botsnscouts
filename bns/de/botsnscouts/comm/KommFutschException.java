package de.spline.rr;

/** KommFutschExceptions werden von den Komm-Klassen geworfen, falls
a) mehrmals hintereinander "null" ueber den BufferedReader kommt
b) eine IOException beim Lesen auftritt
c) der Client, der das Komm-Objekt benutzt, aus dem Spiel entfernt wurde, 
   ohne dass er mit 'warte' darauf gewartet hat;
   d.h. falls er z.B einen InfoRequest ausgefuehrt hat und statt der Antwort
   ein REN erhielt.
   Der Entfernungsgrund steht in der Exception-Message.
   

  @author Hendrik
 */ 
  
public class KommFutschException extends KommException {
  
	public KommFutschException () {
    		super();
  	}


  /** Der String s wird als Message der Exception ausgegeben.
   */
	public KommFutschException (String s) {
    		super (s);
  	}
  
}
