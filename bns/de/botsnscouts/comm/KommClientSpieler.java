package de.spline.rr;

import java.io.*;
/** Klasse fuer die Dinge, die nicht in 'KommClient' vorkommen und die die Ausgabe nix angehen
*@author Hendrik<BR>*/
public class KommClientSpieler extends KommClient  {
  
  
   

  public KommClientSpieler () {
    super ();
  }
  /** 'anmelden' ist eine Methode zur Anmeldung beim Server. Die Anmeldung erfolgt mittels der Daten des Spielers  [Server-IP-Nr, Portnummer,Spielername]. Falls die Anmeldung erfolgreich war, wird true zurueckgegeben, sonst false.
Trat ein nicht-technischer Fehler auf (d.h. beim Parsen), so wird eine Exception geworfen.
@exception KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen. 
*/
    public boolean anmelden (String ipnr, int portnr, String name) throws KommException{
	return super.anmelden (ipnr, portnr, name, "RGS");
    }
    /** Anmeldung; eine Farbe zwischen 1 und 8 waehlen*/
    public boolean anmelden2 (String ipnr, int portnr, String name, int farbe) throws KommException{
	if ((farbe>0)&&(farbe<9))
	    return super.anmelden (ipnr, portnr, (name+","+farbe), "RS2");
	else 
	    return super.anmelden (ipnr, portnr, name, "RGS");
    }


  /** Antwort-Methode, die uebermittelt, wie der Roboter nach einer Zerstoerung aufs Feld gesetzt werden soll. Sie erhaelt den Namen des Roboters und einen Integer fuer die Richtung (n=0, w=3, e=1, s=2)*/
  public void respZerstoert (String robName, int richtung){
    String back = "TNR("+robName+",";
    //richtung%=4;
    //System.err.println ("CLIENT: zerstoert erhielt Richtung: "+richtung);
    if (richtung==0)
      back+="N)";
    else if (richtung==1)
      back+="E)";
    else if (richtung==2)
      back+="S)";
    else 
      back+="W)";
    try {
      this.senden (back);
    }
    catch (KommFutschException k) {
      System.err.println ("Exception bei respReaktivierung: \n Message: "+k.getMessage());
    }
  }
  /** Antwort-Methode, die uebermittelt, welche kaputten Register der Roboter repariert haben moechte. Erhaelt als Argument die Nummern der gewuenschten Register*/
  public void respReparatur (String robName, int [] registerNr) {
    String back ="TRR("+robName+",";
    for (int i=0;i<registerNr.length;i++)
      back+=registerNr[i]+",";
    back+=")";
    try {
      this.senden(back);
    }
    catch (KommFutschException k) {
      System.err.println ("Exception bei respReaktivierung: \n Message: "+k.getMessage());
    }
  }
  /** Methode zur Abgabe der Registerprogrammierung. Die Methode erhaelt als Argumente (abgesehen vom Namen) zum einen einen Boolean, der angibt, ob ein Power-Down fuer die naechste Runde geplant ist (ja=true), zum anderen ein int-Array, dass die Registerprogrammierung enthaelt (z.B. wuerde {3,7,4,1,9} bedeuten, dass das erste Register die dritte ausgeteilte Karte enthaelt, das zweite Register die siebte ausgeteilte Karte usw..).
Falls Register gesperrt sind, wird ein entsprechend kuerzeres Array uebergeben
(siehe "Protokolle und Datenformate").
   */
  public void registerProg (String name, int [] programmierung, boolean powerDown) {
    String back="TRP("+name+",(";
    for (int i=0;i<programmierung.length;i++)
     back+=programmierung[i]+",";
    back+="),"; // Ende des Karten-Teils
    if (powerDown)
      back+="t)";
    else
      back+="f)";
    try {
      this.senden (back);
    }
    catch (KommFutschException k) {
      System.err.println ("Exception bei respReaktivierung: \n Message: "+k.getMessage());
    }
    // System.err.println ("CLIENT: schicke folgende Programmierung: "+back);
  }
  /** DIESE METHODE (mit dieser Signatur) IST VERALTET;
    ES MUSS DER NAME MIT UEBERGEBEN WERDEN !
    Sie existiert nur noch, damit die Version auf jeden Fall kompilierfaehig bleibt.
    */
  public void registerProg (int [] programmierung, boolean powerDown) {
    System.err.println("KommClientSpieler.registerProg(int [],bool) deprecated: \nAENDERUNG: Der Name muss mit uebergeben werden");
  }
  /** Diese Methode dient zur Antwort auf die Frage, ob der powerdown-Roboter deaktiviert bleiben will.
Abgesehen von seinem Namen übergibt der Roboter einen boolean; ist er true, heisst das, dass der Roboter deaktiviert bleiben möchte
*/
  public void respReaktivierung (String name, boolean bleibeDeaktiv) {
    String raus="TBD(";
    raus+=name;
    raus+=",";
    if (bleibeDeaktiv)
      raus+="t";
    else 
      raus+="f";
    raus+=")";
    try {
      this.senden (raus);
    }
    catch (KommFutschException k) {
      System.err.println ("Exception bei respReaktivierung: \n Message: "+k.getMessage());
    }
  }
/** Schickt eine Nachricht an alle anderen Spieler */
     public void message (String id, String [] namen) {
	 String send="MSG("+id;
	 if (namen!=null) {
	     send+=",";
	     for (int i=0;i<namen.length;i++) {
		 send+=namen[i]+",";
	     }
	 }
	 send+=")";
	 try{
	     this.senden(send);
	 } catch (KommFutschException k){
	     System.err.println("Exception bei Message: \n Message: "+k.getMessage());
	 }
     }
}


