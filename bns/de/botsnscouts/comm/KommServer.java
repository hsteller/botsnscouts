/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2001 scouties.                                    *
 * Contact botsnscouts-devel@sf.net                                *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 2 of the License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program, in a file called COPYING in the top
 directory of the Bots 'n' Scouts distribution; if not, write to
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 Boston, MA  02111-1307  USA

 *******************************************************************/

package de.botsnscouts.comm;

import de.botsnscouts.util.*;
import java.io.*;
import java.net.*;

import org.apache.log4j.Category;
/* STAND 20.7.99 2:55 ; sendFeldinhalt fehlt noch;
   getestet: - warte
              sendRobStatus
/**
Oberklasse für SERVER-Kommunikation <BR>
* Enthält Objekte und Methoden, die sowohl zur Kommunikation mit Spielern als auch zur Kommunikation mit Ausgabekanälen benutzt werden können
 *@author Hendrik<BR>

*/
public class KommServer {
  static Category CAT = Category.getInstance(KommServer.class);
  static final boolean LOG_RECEIVE = false;
  static final boolean LOG_SEND    = false;

  public KommServer(BufferedReader i, PrintWriter o) {
    in=i;
    out=o;
  }
  /** BufferedReader*/
  public BufferedReader in;
  /** PrintWriter*/
  public PrintWriter out;

  /** Warte dient zum 'Horchen' am Kommunikationskanal und Warten auf beliebige Kommunikation von Client-Seite.
   * Warte parst den über den BufferedReader hereinkommenden String und liefert ein Objekt der Klasse ServerAntwort zurück, über das abgefragt werden kann, was für eine Art Kommunikation vorliegt und welche Informationen mitgesandt wurden.
   @return Das Antwortobjekt mit den Daten
   @exception KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen.
   */
  public ServerAntwort warte () throws KommException{
    try {
      String s = in.readLine();
      // System.err.println("SERVER: warte erhielt: "+s);
      if (s==null) {
	try {
	  s=in.readLine();
	  if (s==null)
	    throw new KommException ("KS.warte: Zweimal null gelesen; vielleicht haengt der Client?!");
	}
	catch (Exception ex) {
	  throw new KommException ("Exception (Message:"+ex.getMessage()+")bei KommServer.warte(vorher 'null' gelesen)");
	}
      }
      if (s!=null)
	return wait2(s);
      else
	throw new KommException ("Ks.warte hat null gelesen");

    }
    catch (IOException ie)
      {
	throw new KommException ("Es trat eine IOException beim Aufruf von wait2 auf");
      }
  }
  /** Diese Methode ist eigentlich nicht da; sie ist nur zu Testzwecken public, also vergesst, dass Ihr sie gesehen habt. ;-)
    @exception KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen.
    */
  ServerAntwort wait2(String input) throws KommException {
    ServerAntwort back=new ServerAntwort();
    boolean error=false;
    String errormsg="";
    char st,nd,rd; /* Die drei ersten Zeichen des Strings:
		      firST, secoND, thiRD*/
    try {
      /* try wegen IndexOutOfBoundsExceptions, die entstehen, wenn in einem String verkehrt geklammert wurde (z.B. ")xyz(" statt "(xyz)");
	 leider zu spaet aufgefallen, um exakte Fehlerstelle (bearbeitete Kommunikationsart) ohne grossen Aufwand ausgeben zu koennen.
	 Dazu waeren noch viel mehr try/catch-Statements noetig, da diese Exceptions bei jedem Aufruf von "substring" oder "charAt" auftreten koennen.
       */
    try {
      st=Character.toUpperCase(input.charAt(0));
      nd=Character.toUpperCase(input.charAt(1));
    }
    catch(StringIndexOutOfBoundsException x) {
      // Exception kann wegen min. Laenge der Strings (=2 Zeichen) nicht auftreten
      error=true;
      errormsg="Weniger als zwei Zeichen im String";
      // zwei dummys, damit der Compiler nicht behauptet, st und nd würden (unter Umstaenden) nicht initialisiert werden
      st='~';
      nd='~';
    }
    /* abfangen des einzigen Falls, für den der String nicht mindestens drei Zeichen lang ist
     */
    if (st=='O') {
      if (nd=='K') {
	back.typ=back.AENDERUNGFERTIG;
	back.ok=true;
      }
      else {
	error=true;
	errormsg="erster Buchstabe='O', zweiter B. != 'K'";
      }
    }
    else {
      try {
	// dritten 'identifier' auslesen'
	rd=Character.toUpperCase(input.charAt(2));
      }
      catch (StringIndexOutOfBoundsException x) {
	// exception dürfte nicht auftreten..
	rd='~'; // wg. Compiler (s.o.)
	errormsg="String kuerzer als zwei Zeichen und nicht gleich \"OK\"";
	error=true;
      }


      switch (st) {

      case 'S': {
	if ((nd=='R') && (rd=='O')) {
	  //input: "SRO(Robbi Nummer 1)"
	  int klammeraufpos = input.indexOf('(');
	  int klammerzupos = input.lastIndexOf(')');
	  if (klammeraufpos==-1) {
	    errormsg="SRO->keine '(' gefunden";
	    error=true;
	    break;
	  }
	  if (klammerzupos==-1) {
	    errormsg="SRO->keine ')' gefunden";
	    error=true;
	     break;
	  }
	  back.typ=back.GIBROBOTERPOS;
	  back.name=URLDecoder.decode(input.substring (klammeraufpos+1,klammerzupos));
	 // System.out.println ("SRO-test");

	}
	else if ((nd=='F')&& (rd=='I')) {
	  // input: "SFI(37,123)" !!FALSCH!! -> SFI((37,123))
	  int x=-1;
	  int y=-1;
	  int klammeraufpos=input.indexOf('(');
	  if (klammeraufpos==-1) {
	    error=true;
	    errormsg="SFI->keine '(' gefunden";
	    break;
	  }
	  int klammerzupos=input.lastIndexOf(')');
	  if (klammerzupos==-1) {
	    error=true;
	    errormsg="SFI->keine ')' gefunden";
	    break;
	  }
	  int kommapos=input.indexOf(',');
	  if (kommapos==-1) {
	    errormsg="SFI->kein Komma gefunden";
	    error=true;
	    break;
	  }
	  String xs =input.substring (klammeraufpos+2,kommapos);
	// +2, um die innere Klammer zu erwischen
	  String ys =input.substring (kommapos+1,klammerzupos-1);
	// -1, um die innere Klammer zu erwischen
	  try{
	    x=Integer.parseInt(xs);
	    y=Integer.parseInt(ys);
	    back.typ=back.GIBFELDINHALT;
	    back.ort=new Location (x,y);
	  //  System.out.println("SFI-Ende");
	  }
	  catch (NumberFormatException xy){
	    errormsg="SFI(x,y) ->  x oder y keine Zahl oder ein Zeichen keine Ziffer, z.B. Space";
	    error=true;
	    return null;
	  }
	}
	else {
	  errormsg = "String beginnt mit S, danach ungueltig";
	  error=true;
	}
	//System.out.println("DEBUG");
	break;
      }

      case 'G': {
	if (nd=='R') {
	  if (rd=='S'){
	    //input "GRS(Robbis name)"
	    int klammeraufpos = input.indexOf('(');
	    int klammerzupos = input.lastIndexOf(')');
	    if (klammeraufpos==-1) {
	      errormsg="GRS->keine '(' gefunden";
	      error=true;
	      break;
	    }
	    if (klammerzupos==-1) {
	      error=true;
	      errormsg="GRS->keine ')' gefunden";
	      break;
	    }
	    back.typ=back.GIBROBSTATUS;
	    back.name=URLDecoder.decode(input.substring (klammeraufpos+1,klammerzupos));
	  }
	  else {
	    error=true;
	    errormsg="String begint mit \"GR\", danach ungueltig";
	    break;
	  }
	}
	else if (nd=='S')  {
	  if (rd=='A'){
	    back.typ=back.GIBAUSWERTUNGSSTATUS;
	  }
	  else if (rd=='N') {
	    back.typ=back.GIBNAMEN;
	  }
	  else if (rd=='D') {
	    back.typ=back.GIBSPIELFELDDIM;
	  }
	  else if (rd=='S') {
	    back.typ=back.GIBSPIELSTAND;
	  }
	  else if (rd=='F'){
	      back.typ=back.GIBFARBEN;
	  }
	  else if (rd=='T'){
	      back.typ=back.STATS;
	  }
	  else {
	    errormsg="GS->danach nichts aus ['A','N','D','S','F','T'] gefunden";
	    error=true;
	    break;
	  }
	} // nd==s
	else if (nd=='F') {
	  if (rd=='L')
	    back.typ=back.GIBFAHNENPOS;
	  else {
	    errormsg="GF-> danach kein 'L' gefunden";
	    error=true;
	    break;
	  }
	}
	else if (nd=='T') {
	  if (rd=='O')
	    back.typ=back.GIBTIMEOUT;
	  else {
	    error=true;
	    errormsg="GT-> danach kein 'O' gefunden";
	    break;
	  }
	}
	else if (nd=='P') {
	  if (rd=='L')
	    back.typ=back.GIBSPIELFELD;
	  else {
	    error=true;
	    errormsg="GP-> danach kein 'L' gefunden";
	    break;
	  }
	}

	break;
      }
      case 'T': {
	if (nd=='R') {
	  if (rd=='P') {
	    //input: "TRP(robbis name,(4,8,3,),f)"
	    int klammerauf1=input.indexOf('(');
	    int klammerauf2=input.lastIndexOf('(');
	    int klammerzu2=input.lastIndexOf(')');
	    int klammerzu1=input.lastIndexOf(')', klammerzu2-1);
	    int kommaletzt=input.lastIndexOf(',');
	    int komma1=input.lastIndexOf(',',klammerauf2-1);

	    if ((klammerauf1!=-1)&&(klammerauf2!=-1)&&(klammerzu1!=-1)&&(klammerzu2!=-1)&&(klammerzu2!=-1)&&(komma1!=-1)&&(kommaletzt!=-1)) {
	      // Auslesen der Karten
	      String karten=new String (input.substring(klammerauf2+1,klammerzu1));
	      int [] kart=new int [5];
	      int zaehler=0;
	      boolean istLetztesZeichenZiffer=false; // Test auf 2-stellige Karten-Nummern, die ansonsten als zwei Karten verstanden würden
	      for (int i=0;i<karten.length();i++){
		if (Character.isDigit(karten.charAt(i))){
		  try {
		    if (istLetztesZeichenZiffer) {
		      error=true;
		      errormsg="Karten-Nummer >9 abgegeben";
		    }
		    else {
		      kart [zaehler]= Integer.parseInt(karten.substring(i,i+1));
		      istLetztesZeichenZiffer=true;
		      zaehler++;
		    }
		  }
		  catch (NumberFormatException should_not_happen) {
		    error = true;
		    errormsg="Kartenrueckgabe: NumberFormatException; sollte eigentlich selbst bei falschem String nicht moeglich sein";
		    break;
		  }
		  catch (ArrayIndexOutOfBoundsException falsche_Rueckgabe) {
		    error = true;
		    errormsg="Falsche Kartenrueckgabe, wahrscheinlich zu viele Karten";
		    break;
		  }
		}
		else
		  istLetztesZeichenZiffer=false;
	      }
	      if (!error) {
		// Programmierung zuweisen
		back.register=new int [zaehler];
		for (int i=0;i<zaehler;i++)
		  back.register[i]=kart [i];


		/* Auslesen des Namens; aufgrund der persoenlichen Komm-Objekte unseres Servers eigentlich ueberfluessig */
		back.name=URLDecoder.decode(new String(input.substring(klammerauf1+1,komma1)));
		// Auslesen des Power-Down-Booleans
		String deaktiviert=new String (input.substring(kommaletzt+1,klammerzu2));
		try {
		  char powerdown=deaktiviert.trim().toUpperCase().charAt(0);
		  if (powerdown=='F')
		    back.ok=false;
		  else if (powerdown=='T')
		    back.ok=true;
		  else {
		    error=true;
		    errormsg="Kartenrueckgabe: falscher Bool (nicht f/t) fuer powerdown zurueckgegeben";
		  }
		}
		catch (java.lang.Exception kein_bool) {
		  error=true;
		  errormsg="Kartenrueckgabe: vermutlich kein Bool fuer powerdown zurueckgegeben (d.h. letztes Zeichen in der Klammer nicht t oder f) ";
		}
		// setzen des Typs
		back.typ=back.PROGRAMMIERUNG;
		/*System.err.print ("SERVER: gebe Kartenzahlen zurueck: ");
		for (int i=0;i<back.register.length;i++)
		  System.err.print (back.register[i]+", ");
		System.err.println();
		*/
	      }
	    }
	    else {
	      error=true;
	      errormsg="Kartenrueckgabe: falsch aufgebauter Rueckgabestring; vermutlich falsche Klammerung oder falsche Kommasetzung";
	    }
	  }
	  else if (rd=='R') {
	    int klammerauf=input.indexOf('(');
	    int klammerzu=input.lastIndexOf(')');
	    int komma1=input.indexOf(',');

	    if ((klammerauf!=-1)&&(klammerzu!=-1)&&(komma1!=-1)) {
	      try {
		String regStr=input.substring(komma1+1,klammerzu); /* Der Teilstring der die Registernummern enthaelt
								  */
		int [] regs=new int [9]; // Die Registernummern
		int zaehler=0;
		boolean istLetztesZeichenZiffer=false; // Zum Test auf 2-stellige Karten-Nummern, die ansonsten als zwei Karten verstanden würden
		for (int i=0;i<regStr.length();i++){
		  if (Character.isDigit(regStr.charAt(i))){
		    try {
		      if (istLetztesZeichenZiffer) {
			error=true;
		      errormsg="Register-Reparatur: zweistellige Registernummer angegeben";
		      }
		      else {
			regs [zaehler]= Integer.parseInt(regStr.substring(i,i+1));
			istLetztesZeichenZiffer=true;
			zaehler++;
		      }
		    }
		    catch (NumberFormatException should_not_happen) {
		      error = true;
		      errormsg="Register-Reparatur: NumberFormatException; sollte eigentlich selbst bei falschem String nicht moeglich sein";
		      break;
		    }
		    catch (ArrayIndexOutOfBoundsException falsche_Rueckgabe) {
		      error = true;
		      errormsg="Falsche Registeranzahl, wahrscheinlich zu viele Register-Nummern";
		      break;
		    }
		  }
		  else
		    istLetztesZeichenZiffer=false;
		}
	      	// Registernummern zuweisen
		back.register=new int [zaehler];
		for (int i=0;i<zaehler;i++)
		  back.register[i]=regs [i];


		/* Auslesen des Namens; aufgrund der persoenlichen Komm-Objekte unseres Servers eigentlich ueberfluessig */
		back.name=URLDecoder.decode(new String(input.substring(klammerauf+1,komma1)));
		back.typ=back.REPARATUR;
	      }
	      catch (StringIndexOutOfBoundsException xe) {
		error = true;
		errormsg="Reparatur: ungueltiger String";
	      }
	    }
	    else {
	      error=true;
	      errormsg="TRR - Falscher String";
	      break;
	    }

	  }
	  else {
	    error=true;
	    errormsg="GR-> danach weder P noch R";
	    break;
	  }
	}
	else if (nd=='N'){
	  if (rd=='R') {
	    // TNR(<Name>,<Richtung>)
	    int klammerauf=input.indexOf('(');
	    int klammerzu = input.indexOf(')');
	    int komma1 = input.indexOf(',');
	    if ((komma1!=-1)&&(klammerauf!=-1)&&(klammerzu!=-1)){
	      try {
		String richtstr=input.substring(komma1+1,klammerzu);
		back.name=URLDecoder.decode(input.substring(klammerauf+1,komma1));
		back.typ=back.AUSRICHTUNG;
		/* Es koennte eine Exception kommen, falls nach 'trim' keine
		   Zeichen mehr in 'richtstr' sind
		   TESTERGEBNIS:  SIE KOMMT AUCH!
		   */
		char richtung=Character.toUpperCase(richtstr.trim().charAt(0));

		if (richtung=='N')
		  back.wohin=0;
		else if (richtung=='S')
		  back.wohin=2;
		else if ((richtung=='E')||(richtung=='O'))
		  back.wohin=1;
		else if (richtung=='W')
		  back.wohin=3;
		else {
		  error=true;
		  errormsg="TNR -> keine gueltige Richtung angegeben";
		}
	      }
	      catch (Exception e) {
		error = true;
		errormsg="TNR -> keinen RichtungsCharacter gefunden (!?)";
	      }
	    }
	    else {
	      error= true;
	      errormsg="TNR -> kein gueltiger String";
	    }
	  }
	  else {
	    error=true;
	    errormsg="TN -> danach kein R";
	  }
	}
	else if (nd=='B') { //FEHLT FEHLT FEHLT gleich nicht mehr
	  if (rd=='D') {
	    //TBD(<Name>,<Bool>)
	    int klammerauf=input.indexOf('(');
	    int klammerzu=input.lastIndexOf(')');
	    int komma1=input.indexOf(',');
	    if ((komma1!=-1)&&(klammerauf!=-1)&&(klammerzu!=-1)){
	      back.name = URLDecoder.decode(input.substring (klammerauf+1,komma1));
	      back.typ=back.REAKTIVIERUNG;
	      try {
	      char stayPDown=Character.toUpperCase(input.substring(komma1+1,klammerzu).trim().charAt(0));
	      /* Die Rueckgabewerte sind invertiert, d.h., wenn der Spieler f
		 zurueckgibt, wird ok auf true gesetzt und nicht auf false.
		 Gibt der Spieler t zurueck, wird ok auf false gesetzt.
		 Dieses geschieht aufgrund der Definition der Rueckgabewerte von
		 KommServerRoboter.reaktivierung():
		 Dort wird nicht zurueckgegeben, ob der Spieler deaktiviert bleiben
		 moechte, sondern ob er wieder aktiviert werden moechte.
	      */
	      if (stayPDown=='F')
		 back.ok=true;
	       else if (stayPDown=='T')
		 back.ok=false;
	       else {
		 error=true;
		 errormsg="TBD -> kein Boolean gefunden :-(";
	       }
		}
	      catch (Exception abc) {
		error=true;
		errormsg="TBD -> Vermutlich kein Bool an der richtigen Stelle";
	      }
	    }
	    else {
	      error=true;
	      errormsg="TBD -> kein gueltiger String";
	    }
	  }
	  else {
	    error=true;
	    errormsg="TB-> danach kein gueltiges Zeichen gefunden";
	  }
	}
	else {
	  errormsg="T-> danach kein R.N oder B";
	  error=true;
	}
	break;
      }
      case 'R': {
	if ((nd=='L')&&(rd=='E')) {
	  // input: "RLE(Client_sein_Name)"
	  int klammeraufpos = input.indexOf('(');
	  int klammerzupos = input.lastIndexOf(')');
	  if (klammeraufpos==-1) {
	    error=true;
	    errormsg="RLE -> keine '('";
	    break;
	  }
	  if (klammerzupos==-1) {
	    error=true;
	    errormsg="RLE -> keine ')'";
	    break;
	  }
	  back.typ=back.ABMELDUNG;
	  back.name=input.substring (klammeraufpos+1,klammerzupos);
	}
	else {
	  errormsg="R -> danach kein LE";
	  error=true;
	}
	break;
      } //
      case 'M': {
	  if ((nd=='S')&&(rd=='G')){
	      back.typ=back.MESSAGE;
	      int k1 = input.indexOf('(');
	      String input1 = input.substring (k1+1,input.length()-1);
	      java.util.StringTokenizer sto= new java.util.StringTokenizer(input1,",)");
	      back.msg= new String [sto.countTokens()];
	      int i=0;
	      while (sto.hasMoreTokens()){
		  back.msg[i]=URLDecoder.decode(sto.nextToken());
		  i++;
	      }
	      break;
	  }
	  if ((nd=='O')&&(rd=='K')){
	      back.typ=back.MSG_ACK;
	      break;
	  }
      }
      default : {
	error=true;
	errormsg="Default: kein gueltiger String(-anfang)";
      }
      } // switch
    }
    } /* try wegen IndexOutOfBoundsExceptions; leider zu spaet aufgefallen, um exakte Fehlerstelle (bearbeitete Kommunikationsart) ausgeben zu koennen.
       */
    catch (StringIndexOutOfBoundsException falsche_Klammerung_vermutlich) {
      error = true;
      errormsg = "Beim Parsen trat eine StringIndexOutOfBoundsException auf.\nVermutlich wegen einer falschen Klammerung der Art ' )xyz( '";
    }
    if (!error)
      return back;
    else
      throw new KommException(errormsg);
  } // wait2


  /** Dient dazu, einem Client mitzuteilen, daß er vom Spiel ausgeschlossen wurde.
   * Bekommt den Grund als String übergeben, am besten schon in der laut Protokoll vorgesehenen Syntax.
   @param grund Der String mit der Begründung des Aussschlusses (in 'richtiger' syntax)
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat
   */
  public void entfernen (String grund) throws KommException{
    try {
      out.println ("REN("+grund+")");
    }
    catch (Exception gibt_keine_aber_server_will_es_lieber_so) {
      throw new KommException ("Exception wegen Fehler beim Entfernen");
  }
  }
  /** Zur Bestätigung der Anmeldung.
   *
   @param ok 'true', falls die Anmeldung erfolgreich war, ansonsten 'false'
   @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat
   */
  public void anmeldeBestaetigung (boolean ok) throws KommException {
    try {
      String raus="";  // ueberfluessig, aber zum Testen hilfreich
      if (ok==true)
	raus="ok";
      else
	raus="error";
      out.println (raus);
    }
    catch (Exception gibt_keine_aber_server_will_es_lieber_so) {
      throw new KommException ("Exception wegen Fehler bei anmeldeBestaetigung");
    }
  }


  /** Zur Antwort auf Info-Request 'gibSpielfeldDim'.
    @param nordostecke  Die Koordinaten der Nordostecke des Spielfeldes in Form eines Ortes
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat
    */
  public void sendSpielfeldDim (Location nordostecke) throws KommException  {
    sendSpielfeldDim (nordostecke.x, nordostecke.y);
  }
  /** Alternative Antwort auf Info-Request 'gibSpielfeldDim'.
    @param x Die x-Koordinate der Nordostecke des Spielfeldes
    @param y Die y-Koordinate der Nordostecke des Spielfeldes
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat
    */
  public void sendSpielfeldDim (int x, int y) throws KommException {
   try {
     String raus="";
     raus="("+x+","+y+")";
     out.println (raus);
   }
   catch (Exception gibt_keine_aber_server_will_es_lieber_so) {
     throw new KommException ("Exception wegen Fehler bei sendeSpielfelddim");
   }
  }
  /** Zur Antwort auf Info-Request 'gibSpielfeld'.
    @param spielfeld Das Board in Form eines Strings, der dem Protokoll entspricht
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat
    */
  public void sendSpielfeld (String spielfeld) throws KommException {
  try {
    String raus=spielfeld;
    int punkt = raus.lastIndexOf('.');
    if (punkt==-1)
      throw new KommException ("sendSpielfeld: Board nicht durch Punkt terminiert!");
    raus=raus.substring(0,punkt+1);
    out.println (raus);
  }
   catch (Exception gibt_keine_aber_server_will_es_lieber_so) {
     throw new KommException ("Exception wegen Fehler bei sendSpielfeld");
   }
  }
  /** Zur Antwort auf Info-Request 'gibFahnenPos'.
   * Erhält die Fahnenpositionen als ein geordnetes Feld-Array, d.h. die Position der ersten Fahne steht an erster Stelle, die Position der zweiten Fahne an zweiter Stelle usw..
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat
   */
  public void sendFahnenpos (Location [] fahnen) throws KommException {
    try {
      String raus="";
      for (int i=0;i<fahnen.length;i++)
	raus+="("+fahnen[i].x+","+fahnen[i].y+")";
      out.println (raus);

    }
    catch (Exception gibt_keine_aber_server_will_es_lieber_so) {
      throw new KommException ("Exception wegen Fehler bei sendFahnenPos");
    }
  }
  /** Zur Antwort auf Info-Request 'gibNamen'.
   * Erhält ein String-Array mit den Namen der Mitspieler
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat
   */
  public void sendNamen (String [] namen) throws KommException {
    // 2.7.99
    // System.err.println ("SERVER: sendNamen aufgerufen");
    try {
      String raus="(";
      for (int i=0;i<namen.length;i++)
	raus+=URLEncoder.encode(namen[i])+",";
      raus+=")";
      // System.err.println("SERVER: sende Namen: "+raus);
      out.println (raus);
    }
    catch (Exception youNeverKnow) {
      throw new KommException ("Exception bei sendNamen");
    }
  }

    /** Neue Methode, die fuer die Uebermittelung der Farben der Spieler
	zustaendig ist.
	Das Array enthaelt die Namen der Spieler an speziellen Positionen.
	Alle anderen Elemente sind null.
    */
    public void sendFarben (String [] namen) throws KommException{
	try {
	    String raus="(";
	    for (int i=0;i<namen.length;i++) {
		if (namen[i]==null)
		    raus+="0,";
		else
		    raus+=URLEncoder.encode(namen[i])+",";
	    }
	    raus+=")";
	    // System.err.println("SERVER: sende Namen: "+raus);
	    out.println (raus);
	}
	catch (Exception youNeverKnow) {
	    throw new KommException ("Exception bei sendFarben");
	}
    }





  /** Zur Antwort auf Info-Request 'gibRoboterPos'.
   * Erhält den Location des Roboters, der gesucht wurde.
@exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat
   */
  public void sendRobpos (Location ort) throws KommException {
    // 2.7.99
    try {
      sendRobpos (ort.x, ort.y);
    }
     catch (Exception youNeverKnow) {
      throw new KommException ("Exception bei sendRobPos(Location)");
    }
  }

  /** Alternative zur Antwort auf Info-Request 'gibRoboterPos'.
   * Erhält die koordinaten als Integers
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat*/
  public void sendRobpos (int x, int y)  throws KommException{
    // 2.7.99
    try {
      String raus="("+x+","+y+")";
      out.println (raus);
    }
     catch (Exception youNeverKnow) {
       throw new KommException ("Exception bei sendRobPos(int,int)");
    }

  }


  /**Zur Antwort auf Info-Request 'gibRobStatus'.
   * Erhält ein Bot-Objekt mit den 'Daten' des gewünschten Roboters.
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat
   */
  public void sendRobStatus (Bot r)  throws KommException{

   try {
     String raus="RS(";
     switch (r.getFacing()) {
     case 0 : {
       raus=raus +"N,";
       break;
     }
     case 1 : {
       raus=raus +"E,";
       break;
     }
     case 2 : {
       raus=raus + "S,";
       break;
     }
     case 3 : {
       raus=raus + "W,";
       break;
     }
     default : throw new KommException ("SendRobStatus: Falsche Richtung");
     }
     raus=raus+"("+r.getX()+","+r.getY()+"),";
     raus=raus+(r.getNextFlag()-1)+",";
     raus=raus+"("+r.getArchiveX()+","+r.getArchiveY()+"),";
     raus=raus+r.getDamage()+",";
     raus=raus+(3-r.getLivesLeft() )+",";
     raus=raus+"("; // Gerspregister
     if (r.getLockedRegisters()!=null) {
       for (int i=0;i<5;i++) {
	 if (r.getLockedRegister(i)!=null)
	   raus = raus+"("+(i+1)+","+"PK("+r.getLockedRegister(i).getaktion()+","+r.getLockedRegister(i).getprio()+")"+")";


       }
     }
     raus=raus+"),"; // Gespregister
     if (r.isActivated())
       raus+="t,";
     else
       raus+="f,";
     if (r.isVirtual())
       raus+="t,";
     else
       raus+="f,";


     raus +=")"; // Komma wegen RSreserviert (Optionskarten) schon gesetzt
     out.println(raus);
   }
   catch (Exception e) {
     throw new KommException ("Exception bei SendRobStatus");
   }
  }

  /** Für die Protokollerweiterung: Wurde der Status eines entfernten Roboters abgefragt, wird vom Server sendRobStatus() aufgerufen.
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat*/
  public void sendRobStatus ()  throws KommException{
    try {
      out.println ("RSE");
    }
    catch (Exception sinnlos) {
      throw new KommException ("Exception bei sendRobStatus");
    }
  }

  /**Zur Antwort auf Info-Request 'gibSpielstand'.
   * Boolean 'laeuft' gibt an, ob das Spiel noch läuft oder schon beendet ist (true = Spiel läuft, false=Spiel beendet).
   * ANMERKUNG: Das 'laeuft' ein Boolean-Objekt und kein boolean ist, war nur ein Tippfehler; ist jetzt aber nicht mehr zu ändern.
   * Falls das Spiel beendet ist, enthält das Array die Namen der Spieler, wobei der des Gewinners an erster Stelle steht
   * Falls nicht, wird 'null' anstelle des Arrays übergeben.
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat
   */

  public void spielstand (Boolean laeuft, String [] endplazierung) throws KommException {
    // 2.7.99
    try {
      String raus="";
      if (laeuft.booleanValue())
	raus = "SS(LAU,";
      else {
	raus = "SS(END,";
      }
      if (endplazierung != null)
          for (int i=0;i<endplazierung.length;i++)
               if (endplazierung[i] != null)
                 raus+=URLEncoder.encode(endplazierung[i])+",";
               else
                 raus+=endplazierung[i]+",";
      raus +=")";

      out.println (raus);
    }
    catch (Exception gibtsnicht) {
      CAT.error(gibtsnicht.getMessage(), gibtsnicht);
      throw new KommException ("Exception bei spielstand-Uebermittlung");
    }
  }
  /** Zur Antwort auf die Abfrage des Timeouts.Erhält als Argument die Dauer des TimeOut in Sekunden.
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat*/
  public void sendTimeOut (int sekunden)  throws KommException{
    try {
      // 2.7.99
      String raus="TO("+sekunden+")";
      out.println (raus);
    }
    catch (Exception e ) {
      throw new KommException ("Exception bei sendTimeOut aufgetreten");
    }
  }


  /**Zur Antwort auf Info-Request 'gibAuswertungsstatus'.
   * Erhält für jeden Bot ein Status-Objekt, das den Namen des Roboters, die in der laufenden Runde bereits ausgewerteten Register und die aktuelle Phase der Auswertung enthält.
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat
   */
  public void spielStatus (Status [] robbis) throws KommException {
    // 2.7.99
    try {
      // debug-part
	/*    System.err.println("SERVER: Status: ");
	      for (int i=0;i<robbis.length;i++)
	      System.err.println(robbis[i].toString());
	*/
	// d-p ende
      // String zusammenbauen
      if (robbis[0].aktPhase==0) {
	out.println("SA()");
	return;
      }
      String raus="SA("+robbis[0].aktPhase+",";
      //  System.err.println ("raus"+raus);

      for (int i=0;i<robbis.length;i++){
	raus+="("+URLEncoder.encode(robbis[i].robName)+",";
	//  System.err.println ("raus aeussere For: "+raus);
	if (robbis[i].register!=null) {
	  for (int j=0;j<(robbis[i].register.length);j++) {
	    raus = raus+"PK("+robbis[i].register[j].getaktion()+","+robbis[i].register[j].getprio()+")";
	    //     System.err.println ("raus innere  For: "+raus);
          }
	}
	raus+=")";
      }
      raus+=")";
      // System.err.println ("raus fertig: "+raus);
      out.println (raus);
    }
          catch (Exception e ) {
          throw new KommException ("Exception bei sendspielStatus aufgetreten");
         }
  }
  /*
  Zur Benachrichtigung der Spieler, dass das Spiel anfängt.
   (true=ok, false=error)
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat
  public boolean spielstart() throws KommException{
    out.println ("NTS");
    try{
      String a = in.readLine();
      if ((a.equals("ok"))||(a.equals("OK")))
	return true;
      else if ((a.equals("error"))||(a.equals("ERROR")))
	return false;
      else
	throw new KommException ("Fehler bei der Antwort auf spielstart: "+a);
    }
    catch (IOException ioe) {
      throw new KommException ("IOException bei spielstart");
    }

  }
  */

  /** Zur Benachrichtigung der Spieler, dass das Spiel anfängt.
   Ob ein ok zurückkkommt, kann mithilfe der Warte-Methode bestimmt werden (Fall ServerAntwort.typ = AENDERUNGFERTIG).
 @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat*/
   public void spielstart() throws KommException{
    try {
      out.println ("NTS");
    }
   catch (Exception ioe) {
      throw new KommException ("Exception bei spielstart (KommServerAusgabe); Message: "+ioe.getMessage());
    }

  }

 /** Finalizer, der die Streams zu macht.
     * (Wird vom garbage collector aufgerufen.)
     * @author Miriam
     */
    protected void finalize() throws Throwable {
        super.finalize();
	if (in != null) in.close();
	if(out != null) out.close();
    }











    /**
       DEPRECATED - has never been used
     */
    //  public void sendFeldinhalt (de.botsnscouts.old.Feld f)  throws KommException{}
//     /**Zur Antwort auf Info-Request 'gibFeldinhalt'.
//    * erhält ein Objekt des Typs Feld, das dem Feld an der gewünschten Stelle entspricht
//    * Feld enthält den Bodeninhalt und die Wandgeräte.
//  @exception KommException wird geworfen, falls beim Senden ein Fehler (z.B. IOException) auftrat
//    */
//   public void sendFeldinhalt (Feld f)  throws KommException{
//     // IST NOCH NICHT FERTIG
// try {
//       String raus="(";
//       String boden = "";
//       String wandLinks="";
//       String wandRechts="";
//       String wandUnten="";
//       String wandOben="";


//       // DIE WAENDE:
//       // obere Wand:
//       if (f.o_exist) { // gibt es eine Wamd ?
// 	String oben = wandgeraet (f.o_WandEl2,f.o_WandEl2Spez);
// 	String unten =  wandgeraet (f.o_WandEl1, f.o_WandEl1Spez);

// 	if (oben.length()>1) // gibt es ueberhaupt ein Wandgeraet?
// 	  oben = "["+oben;
// 	if (unten.length()>1) // gibt es ueberhaupt ein Wandgeraet?
// 	  unten = unten + "]";
// 	wandOben=oben+"#"+unten;	// zusammensetzen der oberen Wand
//       }
//       else {
// 	wandOben="_"; // keine Wand
//       }

//       // rechte Wand:
//       if (f.r_exist) {
// 	String links = wandgeraet (f.r_WandEl1, f.r_WandEl1Spez);
// 	String rechts = wandgeraet (f.r_WandEl2, f.r_WandEl2Spez);

// 	if (links.length()>1) // gibt es ueberhaupt ein Wandgeraet?
// 	  links = "["+links;
// 	if (rechts.length()>1) // gibt es ueberhaupt ein Wandgeraet?
// 	  rechts = rechts + "]";

// 	wandRechts = links+"#"+rechts;
//       }
//       else {
// 	wandRechts="_";
//       }

//       // untere Wand:
//       if (f.u_exist) {
// 	String oben = wandgeraet (f.u_WandEl1, f.u_WandEl1Spez);
// 	String unten = wandgeraet (f.u_WandEl2, f.u_WandEl2Spez);

// 	if (oben.length()>1) // gibt es ueberhaupt ein Wandgeraet?
// 	  oben = "["+oben;
// 	if (unten.length()>1) // gibt es ueberhaupt ein Wandgeraet?
// 	  unten = unten + "]";

// 	wandUnten = oben+"#"+unten;
//       }
//       else {
// 	wandUnten="_";
//       }

//       // linke Wand
//       if (f.l_exist) {
// 	String links = wandgeraet (f.l_WandEl2, f.l_WandEl2Spez);
// 	String rechts = wandgeraet (f.l_WandEl1 ,f.l_WandEl1Spez );

// 	if (links.length()>1) // gibt es ueberhaupt ein Wandgeraet?
// 	  links = "["+links;
// 	if (rechts.length()>1) // gibt es ueberhaupt ein Wandgeraet?
// 	   rechts = rechts + "]";

// 	wandLinks = links+"#"+rechts;
//       }
//       else {
// 	wandLinks="_";
//       }
//       // Die vier Waende wurden erstellt

//       // jetzt folgt DER BODEN:

//       boden = bodeninhalt(f.bodenTyp, f.bodenSpez);

//       // raus zuEnde basteln
//       raus+=boden+","+wandOben+wandRechts+wandUnten+wandLinks;
//       raus+=")";
//       out.println (raus);
//     }
//     catch (Exception e) {
//       throw new KommException ("sendFeldinhalt fuehrte zu einer Exception:"+e.getMessage());
//     }

//   }
//   /** liefert die Richtung (N,E,S,W) zurueck, die vom Wert x beschrieben wird(zwischen 0 und 3); die Methode haette sich vor zwei Wochen schon eher gelohnt :-)
//    */
//   private static String getRichtung (int x) {
//     x%=4; // nur zur Sicherheit
//    if (x==0)
//      return "N";
//    else if (x==1)
//      return "E";
//    else if (x==2)
//      return "S";
//    else
//      return "W";
//   }

//   /** Hilfsmethode, die für ein abbiegendes Fliessband das ´woher´ ermittelt.
//     Dieses müsste irgendwie mithilfe der Richtung und der Drehrichtung möglich sein
//     */
//   private static String fromDirection (int richtung, int art) {
//     // art=2 => Linksdrehung; art=3 => rechtsdrehung
//     int from = richtung;
//     if ((art==2)&&(richtung>0))
//       from--;
//     else if ((art==2)&&(richtung==0))
//       from=3;
//     if (art==3) {
//       from++;
//       from%=4;
//     }

//     return getRichtung(from);
//   }

//   /** Hilfsmethode, die einen <Bodeninhalt>-String laut "Protokolle und Datenformate" zurückgibt.
//     typ und spez sind der Typ und die Spezifikationszahl (siehe Klasse Feld) des Boden(feldes)
//    */

//     private static String bodeninhalt (int typ, int spez)throws KommException {
//     Feld f = new Feld();
//     String boden = ""; // soll zurueckgegeben werden
//     boolean set=false;
//     if (typ>=100) {// dann Fliessband
//       boolean crusher=false;
//       if (spez>0)
// 	crusher=true;
//       int tempo=typ/100; // hunderter-Stelle
//       int richtung=typ%10; // einer-Stelle
//       int art =(typ/10)%10; // zehner-Stelle

//       	boden = "F(";
// 	boden+=getRichtung(richtung)+","+tempo+",";
//       if (art==0) { // band geradeaus
// 	boden+="()"; // kein Dreher
//       }
//       /*      // GIBTS NICHT MEHR! Feld-Spezifikation falsch!
// 	      else if (art==1) { // band mit crusher
// 	System.out.println("CRUSHER !!!");
// 	boden+="()"+"("; // kein Dreher
// 	for (int i=1;i<6;i++) {
// 	  if (f.isCrusherActive(spez, i))
// 	    boden+=i+","; // aktive Phasen anhaengen
// 	}
// 	boden += "))"; // Crusher zu, Fliessband zu
//       }
//       */
//       else if (art==2) { // linkskurve
// 	boden+="(("+fromDirection(richtung, art)+","+"D(L)))"; // dreher nach links

//       }
//       else if (art==3) { // rechtskurve
// 	boden+="(("+fromDirection(richtung, art)+","+"D(R)))"; // dreher nach rechts
//       }
//       else if (art==5) { // einbiegen aus zwei Richtungen => zwei Dreher, aus jeder Richtung einer
// 	boden+="(("+fromDirection(richtung, 2)+","+"D(L))"; // erster Dreher
// 	boden+="("+fromDirection(richtung, 3)+","+"D(R)))"; // zweiter Dreher
//       }
//       else {
// 	throw new KommException ("sendeFeldInhalt: kein gueltiger Bodeninhalt");
//       }

//       if (crusher) {
// 	boden+="(";
// 	for (int i=1;i<6;i++) {
// 	  if (f.isCrusherActive(spez, i))
// 	    boden+=i+","; // aktive Phasen anhaengen
// 	}
// 	boden += "))"; // Crusher zu, Fliessband zu
//       }
//       else //kein Crusher
// 	boden+="())"; // kein Crusher, Fliessband zu

//       return boden;
//     } // Ende Fall Fliessband
//     else {
//       switch (typ) {
//       case Feld.BDGRUBE: {
// 	boden="G";
// 	break;
//       }
//       case Feld.BDNORMAL: {
// 	boden="B";
// 	break;
//       }
//       case Feld.BDREPA: {
// 	boden="R("+spez+")";
// 	break;
//     }
//       case Feld.BDDREHEL: {
// 	boden ="D(";
// 	if (spez==Feld.DUHRZ)
// 	  boden+="R";
// 	else
// 	  boden+="L";
// 	boden+=")";
// 	break;
//       }
//       default: {throw new KommException ("sendeFeldInhlt: bodentyp <100 und nicht definiert");
//       }
//       }
//     }
//     return boden;
//   }


//   /** Hilfsmethode für sendFeldInhalt, die das durch elem und spez definierte Wandgerät (siehe Klasse Feld) in Form eines Strings laut "Protokolle und Datenformate" zurückgibt.
//    */
//   private static String wandgeraet (int elem, int spez) throws KommException{
//     Feld f=new Feld();
//     String back="";
//     switch (elem) {
//     case Feld.WKEINS: {
//       back="";
//       break;
//     }
//     case Feld.WLASER:{
//       back="L("+spez+")";
//       break;
//     }
//     case Feld.WPUSHER: {
//       back="S(";
//       for (int i=1;i<6;i++) {
// 	if (f.isPusherActive(spez,i))
// 	    back+=i+",";
//       }
//       back+=")";
//       break;
//     }
//     default: throw new KommException ("SFI-Antwort: Weder 'Laser', 'Pusher' noch 'kein' Wandelement");
//     }
//    return back;
//   }


}


