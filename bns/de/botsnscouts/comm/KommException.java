package de.botsnscouts.comm;

/** KommExceptions werden von den Komm-Klassen geworfen, falls diese einen String nicht parsen können oder eine Exception beim Parsen auftritt.
  @author Hendrik
 */ 
  
public class KommException extends Exception {
  
	public KommException () {
    		super();
  	}


  /** Der String s wird als Message der Exception ausgegeben.
   */
	public KommException (String s) {
    		super (s);
  	}
  
}
