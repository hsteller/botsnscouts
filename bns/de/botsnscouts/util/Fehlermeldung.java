/** Ausgabe von Fehlermeldungen
    zunächst nur auf die Konsole, erweiterbar auf Fenster
    @author Miriam
*/

package de.spline.rr;

import java.io.*;

public class Fehlermeldung{

    protected PrintStream out=System.err;
    
    public Fehlermeldung(String s) {
	out.println(Message.say("Fehlermeldung","eError", s));
    }

}
