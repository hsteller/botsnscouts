package de.spline.rr;

import java.io.*;
import java.util.*;
import java.net.*;
/** Die Oberklasse f&uuml;r die Client-Kommunikation; enth&auml;lt Methoden, die sowohl Spieler als auch Ausgabekan&auml;le benoetigen
*@author Hendrik & Ourima<BR> 
*@version 13.7.99 00:23
*/

public class KommClient {
  /** Über in kommt der Input, d.h. die Nachrichten vom Server.
   */
  public BufferedReader in;
  /** Über out werden die Nachrichten zum Server geschickt
   */
  public PrintWriter out;
  /** Flag, das angibt, ob ein InfoRequest statt einer Antwort ein NTC vom Server erhielt
   */
  protected boolean gotNTC;
  /** Ein 'zur falschen Zeit' gesendeter NTC wird hier gespeichert.
   */ 
  protected String strNTC;
  /** Der Name des Clients zu Debug-Zwecken
   */
  protected String cn;
  protected final boolean log=false;
  public KommClient (){
    gotNTC=false;
    strNTC="";
    cn="";
    if (log) {
    try {
      PrintWriter debug2 = new PrintWriter(new BufferedWriter(new FileWriter((cn+"'s.Kommlog"), true)));
      debug2.println("neuer Client");
      debug2.close();
    }
    catch (IOException i) {
      System.err.println ("Konstruktor: Fehler beim Erstellen des Log-writers");
    }
    }
  }
  /** Diese Methode sendet den uebergebenen String an den Server.
   */
  protected  void senden (String s) throws KommFutschException {
    PrintWriter debug=null;
    if (log) {
      
      try {
	debug = new PrintWriter(new BufferedWriter(new FileWriter((cn+"'s.Kommlog"), true)));
      }
      catch (IOException ioe) {
	System.err.println ("senden: IOException beim Erstellen des Log-writers");  
      }
      if (debug!=null) {
	debug.println ("Sende: "+s);
	debug.close();
      }
    }
    try {
      Global.debug(this, "CLIENT "+cn+" sendet: "+s);
      out.println (s);
    }
    catch (NullPointerException npe) {
      throw new KommFutschException ("NullPointerException bei Client-Senden");
    }
    catch (Exception e) {
      throw new KommFutschException ("Client-Senden: Exception-Message: "+e.getMessage());
    }
    
  }

    /** 
	Diese Methode ist zum Einlesen von Serverantworten gedacht, falls
	eine bestimmte Antwort erwartet wird.
	Sie ist an Stelle der warte-Methohde (btw. genauer: wait2) zu verwenden.
	Hier werden gegebenenfalls zur falschen Zeit eintreffende RENs und NTCs abgefangen.
	Bei REN wird eine KommFutschException geworfen, 
	bei NTC der String in strNTC gespeichert und beim nächsten Aufruf von warte benutzt;
	stattdessen gibt die Methode den naechsten String, der kommt, zurueck. 
	
	
    */
  protected String einlesen() throws KommFutschException, KommException{
    PrintWriter debug=null;
    if (log) {
     
      try {
	debug = new PrintWriter(new BufferedWriter(new FileWriter((cn+"'s.Kommlog"), true)));
      }
      catch (IOException ioe) {
	System.err.println ("einlesen: IOException beim Erstellen des Log-writers"); 
      }
    }
    String back="";
    try {
      back = in.readLine();
      Global.debug(this, "CLIENT(einlesen) "+cn+" erhaelt: "+back);
      if ((debug!=null)&&(log))
	debug.println("einlesen erhielt: "+back);
    }
    catch (IOException ioe) {
      throw new KommFutschException ("Einlesen: IOException augetreten; Message: "+ioe.getMessage()); 
    }
    // teste, ob gelesener String==null
    if (back==null) {
      try {// wenn null lese nochmal
	back = in.readLine();
	if ((debug!=null)&&(log))
	  Global.debug(this,"einlesen (2.Versuch) erhielt: "+back);
	
	if (back==null)// wenn immernoch null, wirf Exception
	  throw new KommFutschException("Einlesen: zweimal hintereinander null gelesen");

      }
      catch (IOException ioe2) {
	throw new KommFutschException ("Einlesen: IOException beim 2ten Leseversuch (1ter = null)augetreten; Message: "+ioe2.getMessage()); 
      } 
    }
    // Habe jetzt zweimal probiert, einen String ungleich null einzulesen
    // Wenn man hier ankommt: back!=null
    
    // Teste nun back auf REN-Kommunikation
    if ((back.length()>=3)&&(back.substring(0,3).equals("REN"))) {
      ClientAntwort xyz= new ClientAntwort();
      try {
	xyz=wait2(back);
      }
      catch (Exception e) {
	throw new KommException ("einlesen: REN erhalten, aber der String \""+back+"\" verursachte in wait2 eine Exception;\n Message:"+e.getMessage());
      }
      if (xyz.typ==xyz.ENTFERNUNG)
	throw new KommFutschException ("Der Client wurde entfernt;\n Grund: "+xyz.str);
    }
    
    // Teste nun back auf NTC-Kommunikation
    else if ((back.length()>=3)&&(back.substring(0,3).equals("NTC"))) {
      // speichere String
      gotNTC=true;
      strNTC=new String(back);
      // hole vermutlich richtige Antwort
      
      back = einlesen(); // uiuiui, eine Rekursion beim Lesen, wenn das mal gutgeht
      if ((debug!=null)&&(log))
	debug.close();
      return back;
      }
    else { 
      if ((debug!=null)&&(log))
	debug.close();
      return back;
    }
    if ((debug!=null)&&(log))
      debug.close();
    return back; // kann eigentlich nicht erreicht werden
    
  }


  /** Erhaelt den &uuml;ber den BufferedReader eingegangenen String, parst ihn und gibt ein entsprechendes ClientAntwort-Objekt zur&uuml;ck.
@exception KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen.*/
  public ClientAntwort warte () throws KommFutschException, KommException {
    String rein;
    /*   try {
	 rein = in.readLine();
	 } 
	 catch (IOException ioe) {
	 throw new KommException ("IOException bei KommClient.warte");
	 }
	 if (rein==null)
	 throw new KommFutschException("warte erhielt null");
	 */
    PrintWriter debug=null;
    if (log) {
    try {
      debug = new PrintWriter(new BufferedWriter(new FileWriter((cn+"'s.Kommlog"), true)));
    }
    catch (IOException ioe) {
      System.err.println ("IOException beim Erstellen des Log-writers (warte)"); 
    }
    }
    /* if (debug!=null) {
       debug.println("warte erhielt: "+rein);
       debug.close();
       }
       */
    if (!gotNTC) {// Server hat sich normal verhalten
      try {
	rein = in.readLine();
	if ((debug!=null)&&(log)) {
	  debug.println("warte erhielt: "+rein);
	  debug.close();
	}
      }
      catch (IOException ioe) {
	throw new KommException ("IOException bei KommClient.warte");
      }
      if (rein==null)
	throw new KommFutschException("warte erhielt null");
      return wait2(rein);
    }
    else {// Server hat NTC dazwischengeballert!
    
      gotNTC=false;
      if ((debug!=null)&&(log)) {
	  debug.println("baearbeite altes NTC: "+strNTC);
	  debug.close();
	}
      return wait2(strNTC);
    }
  }
   ClientAntwort wait2 (String com) throws KommException {
    Global.debug(this,"CLIENT: warte erhielt: "+com);
    ClientAntwort back=new ClientAntwort();
    boolean error=false;
    String errormesg="";
    if ((com.equals("ok")) || (com.equals("OK"))){
      back.typ=back.ANGEMELDET; 
      back.ok = true;
    }
    else if ((com.equals("error")) || (com.equals("ERROR"))){
      back.typ=back.ANGEMELDET;
      back.ok=false;
    }
    //if (true){} // dummy
    else {
      if (com.length()>=3) {
	char st = com.charAt(0);
	char nd = com.charAt(1);

	  
	char rd = com.charAt(2);
	switch (st){
	case 'T': {
	  if (nd=='O') {
	    try {
	      int klammerauf=com.indexOf('(');
	      int klammerzu= com.indexOf (')');
	      if ((klammerzu==-1)||(klammerauf==-1)){
		error=true;
		errormesg="TO -> dann folgte nicht (  )";
	      }
	      try {
		back.typ=back.TIMEOUT;
		back.zahl=Integer.parseInt(com.substring(klammerauf+1,klammerzu));
	      }
	      catch (NumberFormatException n) {
		error=true;
		errormesg="TO-> NumberFormatException beim Parsen des TimeOuts";
	      }
	      break;
	      
	     
	    }
	    catch (Exception e) {
	     error= true;
	     errormesg="Exception bei KC.wait2.TO; Message:"+e.getMessage();
	     break;
	    }
	     
	  }
	  else  {
		error =true;
		errormesg="Vermutlich falscher String ";
		break;
	     }
	}
	case 'M': {
	    if ((nd=='S')&&(rd=='G')) {
		back.typ=back.MESSAGE;
		int k1 = com.indexOf('(');
		com = com.substring (k1+1,com.length()-1);
		StringTokenizer sto= new StringTokenizer(com,",)");
		back.namen= new String [sto.countTokens()];
		int i=0;
		while (sto.hasMoreTokens()){
		    back.namen[i]=sto.nextToken();
		    i++;
		}
		break;
	    }
	    else if (nd=='R'){
		if (rd=='P') {
		    try {
		try {
		  Karte [] karten1 = new Karte [9]; // max. Kartenzahl
		  int klammerauf=com.indexOf('(');
		  int klammerletzt=com.lastIndexOf(')');
		  com=com.substring (klammerauf+1);
		  //	com+="**"; // dummy, weil es sonst am Schleifenende eine Exception geben koennte
		  int count=0;
		 
		  while (com.length()>1) {
		    int klauf=com.indexOf('(');
		    int klzu=com.indexOf(')');
		    String work= com.substring(klauf+1,klzu); // vorderste Karte, ohne Klammern, dh. "M1,123"
		    int komma=work.indexOf(',');
		    String art=work.substring(0,komma);
		    String pri=work.substring(komma+1);
		    try {
		      int prio=Integer.parseInt(pri);
		      karten1 [count]= new Karte (prio, art);
		    }
		    catch (NumberFormatException nme) {
		      error=true;
		      errormesg="Fehler beim Parsen der kartenprioritaeten";
		      break;
		    }
		    count++;
		    com=com.substring(klzu+1); // hier gaebe es evtl. die Exception
		    // System.out.println (com + ", "+count);
		  }
		  // jetzt sollten count Karten in Karten1 sein;
	      back.typ=back.MACHEZUG;
	      back.karten=new Karte [count];
	      
	      for (int i=0; i<count;i++)
		back.karten[i]=karten1[i];
	      break;
		}
		catch (StringIndexOutOfBoundsException ex) {
		  error =true;
		  errormesg="Exception beim Kartenparsen; Message: "+ex.getMessage();
		}
	      }
	      catch (Exception e) {
		error= true;
		errormesg="Exception bei KC.wait2.MRP; Message:"+e.getMessage();
		break;
	      }
	      break; //Sicher is sicher; lieber einmal zuviel als zuwenig
	    }
	    else if (rd=='R') {
	      try {
		int klauf=com.indexOf('(');
		int klzu=com.indexOf(')');
		String zahl=com.substring(klauf+1,klzu);
		try {
		  back.typ=back.REPARATUR;
		  back.zahl=Integer.parseInt(zahl);
		}
		catch (NumberFormatException nfe) {
		  error=true;
		  throw new KommException ("MRR -> Fehler beim Parsen der Registerzahl");
		}
	      }
	      catch (Exception e) {
		error= true;
		errormesg="Exception bei KC.wait2.MRR; Message:"+e.getMessage();
		break;
	      }
	      break;
	    }
	    else {
	      error=true;
	      errormesg="KC.wait2: MR->danach kein P oder R";
	      break;
	    }
	    
	  }
	  else if (nd=='N') {
	    if (rd=='R') {
	      back.typ=back.ZERSTOERUNG;
	      break;
	    }
	    else {
	      error=true;
	      errormesg="MN-> danach kein R";
	      break;
	    }
	  }
	  else if (nd=='B') { 
	    if (rd=='D') {
	      back.typ=back.REAKTIVIERUNG;
	      break;
	    }
	    else {
	      error=true;
	      errormesg="MB-> danach kein D";
	      break;
	    }
	  }
	  else {
	    error =true;
	    errormesg="KC.wait2: M -> dann nicht R,N,B";
	    break;
	  }
	  
	}
	case 'N': {
	  if (nd=='T') {
	    if (rd=='S'){
	      back.typ=back.SPIELSTART;
	      back.ok=true;
	      
	    }
	    else if (rd=='C'){
	      try {
		back.typ=back.AENDERUNG;
		int klauf=com.indexOf('(');
		back.namen  = getNamen2(com.substring(klauf));
	      }
	      catch (Exception e) {
		error= true;
		errormesg="Exception bei KC.wait2.NTC; Message:"+e.getMessage();
		break;
	      }
	    }
	    else{
	      error=true;
	      errormesg="KC.wait2:NT -> dann kein S oder C danach";
	    } 
	          
	  }
	  else {
	    error=true;
	    errormesg="KC.wait2:N -> dann kein T danach"; 
	  }
	  break;
	}
	case 'S' : {
	  if (nd=='S') {
	    try {
	      back.typ=back.SPIELSTAND;
	      if (com.length()==7){
		back.namen=null;
		back.ok=true;
	      }
	      else {
		back.ok=false;
		int komma1=com.indexOf(',');
		com=com.substring(komma1+1);
		com="("+com; // jetzt hat der String GSN-Format
		back.namen=this.getNamen2(com);
	      }
	    }
	    catch (Exception e) {
	      error= true;
	      errormesg="Exception bei KC.wait2.SS; Message:"+e.getMessage();
	      break;
	    }
	    break;
	  }
	  else if (nd=='A') {
	    try {
	      back.typ=back.SPIELSTATUS;
	      if (com.length()<=4)
		back.ok=false; // => keine Auswertungsphase
	      else { 
		back.ok=true; // => Auswertungsphase
		int klauf=com.indexOf('('); // hinter SA
		int phase=0; // initialisieren
		try {
		  phase=Integer.parseInt(com.substring (klauf+1,klauf+2));// phase parsen	     
		}
		catch (NumberFormatException nfe) {
		  throw new KommException ("NumberFormatException bei getSpielstatus (Parsen der Registerphase); Message: "+nfe.getMessage());
		}
		int komma = com.indexOf(','); 
		com=com.substring(komma+1); // jetzt nur noch Namen
		Status [] stats = new Status [8]; // 8=maximale Spielerzahl
		int bloed = klazu2(com);
		int zaehler=0;
		while (bloed!=-1){
		  String erster = splitFirstRob(com);
		  stats [zaehler] = getStatusRegs(erster);
		  int bloed2 = klazu2(com);
		  if (bloed2!=-1) {
		    com=com.substring(bloed2);
		  }	
		  zaehler++;
		  bloed = klazu2(com);		    
		}
		zaehler++;
		stats [zaehler-1] = getStatusRegs(splitFirstRob(com));
		Status [] back2 = new Status [zaehler];
		
		for (int i=0;i<back2.length;i++){
		  back2 [i] = stats [i];		  
		  back2 [i].aktPhase =phase;
		}
		back.stati = back2;
		
		
		
	    
	      }
	    }
	    catch (Exception e) {
	      error= true;
	      errormesg="Exception bei KC.wait2.SA; Message:"+e.getMessage();
	      System.err.println("KommClient: "+errormesg);
	      break;
	    }
	  }
	  else {
	    error = true;
	    errormesg="KC.wait2 -> S danach kein S oder A";
	  }

	  break;
	}
	case 'R': {
	  if ((nd=='E') && (rd=='N')) {
	    try {
	      back.typ=back.ENTFERNUNG;
	      int klauf = com.indexOf('(');
	      int klazu = com.lastIndexOf(')');
	      String work = com.substring(klauf+1,klazu);
	      if (work.length()<=2) {
		if (work.equals("LL"))
		  back.str="Alle Leben verloren";
		else if (work.equals("TO"))
		  back.str="Das TimeOut wurde ueberschritten";
	      else if (work.equals("GO"))
		back.str="Das Spiel ist vorbei";
		else if (work.equals("RV"))
		  back.str="Es trat eine Regelverletzung auf";
		else if (work.equals("ZS"))
		  back.str="Die Anmeldung erfolgte zu spaet";
		else
		  back.str="unbekannter Grund fuer Entfernung";
	      }
	    

	      else {
		// den Grund bei "SO" finden 
		klauf = work.indexOf ('(');
		work = work.substring (klauf+1,work.length()-1);
		//	      System.out.println(work);
		back.str=work;
	      }
	    }
	    catch (Exception e) {
	      errormesg="Bei Entfernung trat eine Exception auf";
	      System.err.println("KommClient: "+errormesg);
	      error=true;
	      break;
	      
	    }
	    
	    
	    //break; // vermutlich redundant
	  }
	  else {
	    error =true;
	    errormesg="R ohne EN gefunden";
	    break;
	  }
	  break;
	}
	
	
      }
      }
      else {
	error=true;
	errormesg="KommClient.warte: String.length<3 && String!=('ok'||'TO')";
      }
      
    }
    if (!error)
      return back;
    else {
      throw new KommException ("Fehler bei kommClient-warte:\n"+errormesg);
    }

   }
  /** Hiermit k&ouml;nnen sich Clients beim Server abmelden.
    Die Methode erh&auml;lt den Namen des Clients als Argument
*/
  public void abmelden (String name) {
    String back="RLE("+name+")";
   
    if(out!=null) out.println (back);
  }
  /** Die Methode meldet einen Client beim Server an.
    @exception  KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), so wird eine KommException geworfen.
   */
  public boolean anmelden (String ipnr, int portnr, String clientName, String kuerzel)throws KommException{
    cn = clientName;
    try{ 
	    Socket socAnmeldung=new Socket(ipnr, portnr); 
	    in = new BufferedReader(new InputStreamReader(socAnmeldung.getInputStream())); 
	    out= new PrintWriter(new OutputStreamWriter(socAnmeldung.getOutputStream()),true); 
	  	if (in==null)
		    System.err.println("KommClient: in ist null (anmelden)");
		if (out==null)
		    System.err.println("KommClient: out ist null (anmelden)"); 
	    String raus = kuerzel+"("+clientName+")";
	    // out.println(raus);
	    this.senden(raus);
	  }
	  catch(UnknownHostException e){ 
	    System.err.println("Anmeldung: Der angegebene Server wurde nicht gefunden"); 
	    // evtl. stattdessen eine Exception
	    return false; 
	  } 
	  catch (java.io.IOException fehler) {
	    throw new KommException ("IOException bei der Anmeldung");
	  }
	  // einlesen der Antwort beginnen
	  try {
	    String antwort=in.readLine();
	    if (antwort==null)
	      System.err.println ("KommClient: antwort ist null");
	    if (antwort.equals("ok") || (antwort.equals("OK")))
	      return true;
	    else if (antwort.equals("error") || (antwort.equals("ERROR")))
	      return false;
	    else
	      throw new KommException("Falsche Rueckgabe (nicht ok/error)bei \"anmeldung\"");
		}
	  catch (IOException fehler_beim_Lesen_vom_BufferedReader) {
	    throw new KommException ("Fehler bei der Anmeldung: IOexception beim Lesen");
	  }
		
	  
	} 
  
  /** Info-Request zur Abfrage der Fahnenpositionen.
    Das zur&uuml;ckgegebene Ort-Array enth&auml;lt die Positionen der Fahnen in der Reihenfolge ihrer Numerierung, d.h. Fahne1 steht an erster Stelle, Fahne2 an zweiter Stelle usw.. 
@exception KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen.*/
  public Ort [] getFahnenPos ()throws KommException {
    // bekommt einen String der Art "(x,y)(a,b)(c,d)"
    // out.println ("GFL");
    this.senden("GFL");
    String rein="";
    /*try {
      rein=in.readLine;
    }
    catch (IOException ioe) {
      throw new KommException ("getFahnenPos: IOException beim Lesen");
    }*/
    //***********TEST***************TEST***************TEST**********
    rein = this.einlesen();
    // Anzahl der Orte anhand der Anzahl der Kommata ermitteln
    int pos=0;
    int zaehler=0;
    int start=0;
    while (pos!=-1) { // in dieser Schleife sollte keine Exception auftreten
      pos = rein.indexOf(',',start);
      if (pos!=-1)
	zaehler++;
      start=pos+1;	
    }// zaehler muesste beim Verlassen der Schleife die Anzahl der Kommata enthalten, die wiederum der Anzahl der Orte entspricht
    Ort [] back;
    rein+="**"; // dummy zur Vermeidung von Exceptions
    try {
      back = new Ort [zaehler];
      //int myI=0; // zaehler
      for (int myI=0;myI<zaehler;myI++){
	int klammerzu = rein.indexOf(')');
	int kommapos = rein.indexOf(',');
	String x=rein.substring(1,kommapos); // String mit x-Koordinate
	String y=rein.substring(kommapos+1,klammerzu); // String mit y-Koordinate
	int xk=Integer.parseInt(x); // die x-Koordinate
	int yk=Integer.parseInt(y); // die y-koordinate
	back [myI]=new Ort(xk, yk);
	rein=rein.substring(klammerzu+1);// geparsten Ort entfernen
	//myI++;
      }
    }
    // Da kann vieles schiefgehen.. :
    catch (StringIndexOutOfBoundsException sioob){
      throw new KommException ("getFahnenPos warf StringIndexOutOfBoundsException(Inhalt:"+sioob.getMessage()+"); Ursache: vermutlich falsch aufgebaute Antwort vom Server");
    }
    catch (NumberFormatException nfe) {
      throw new KommException("getFahnenPos: NumberFormatException: Parsen der koordinaten-substrings schlug fehl");
    }
    catch (ArrayIndexOutOfBoundsException aioob) {
      throw new KommException ("getFahnenPos warf ArrayIndexOutOfBoundsException (Inhalt: "+aioob.getMessage()+"); der Fehler liegt wahrscheinlich an KommClient)");
    }
    catch (Exception sonstige) {
      throw new KommException ("getFahnenPos warf beim Parsen eine unerwartete Exception; Message: "+sonstige.getMessage());
    }
    return back;
  }
  /** Info-Request zur Abfrage der Position des Roboters 'name'.
@exception KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen.*/
  public Ort getRobPos (String name) throws KommException {
    return this.fetchOrt(name, true); 
  }
  
  /** Info-Request zur Abfrage der Koordinaten der Nordostecke des Spielfeldes
@exception KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen.*/
  public Ort getSpielfeldDim () throws KommException{
    return this.fetchOrt("wirdIgnoriert", false);
  }
  
 /** Info-Request zur Abfrage des gesamten Spielfeldes.Gibt den String zurück, so wie er über das Netz läuft. 
@exception KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen.
*/
  public String getSpielfeld() throws KommException{
    String back="";
     try {
      this.senden ("GPL");
      //String rein= in.readLine();  
      String rein = this.einlesen();
      back=rein;
      if (rein.lastIndexOf('.')==-1) // Ende erreicht?!
	back+="\n"; // nein, also Zeilenumbruch
      while (rein.lastIndexOf('.')==-1){
	//rein= in.readLine();
	rein=this.einlesen();
	back+=rein; // anhaengen des naechsten Spielfeld-Teilstrings
	if (rein.lastIndexOf('.')==-1) // damit nicht hinter dem Punkt noch ein Zeilenumbruch landet
	  back+="\n";
	
      }
	
      
      }/*
  catch (IOException ioe) {
      throw new KommException ("IOException bei getSpielfeld; Message: "+ioe.getMessage());
    }
    */
 catch (Exception sonstige) {
      throw new KommException ("Exception bei getSpielfeld; Message: "+sonstige.getMessage());
    }
 return back;
  }


  /** Info-Request zur Abfrage der Namen aller Spieler.
@exception KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen.
*/
  public String [] getNamen () throws KommException{
    //  try {
      this.senden("GSN");
      //String rein = in.readLine();
      String rein = this.einlesen();
// System.err.println("getNamen erhielt: "+rein);
      return this.getNamen2(rein);
      /* }
    catch (IOException ioe){
      throw new KommException ("IOException bei getNamen;Message: "+ioe.getMessage());
    }*/
  }
  /** Eine Hilfsmethode zum Parsen einer Namensliste
   */
  private String [] getNamen2(String rein) throws KommException {
    String []raus=null;
    try {
      // System.err.println("CLIENT: getNamen2 erhielt:"+rein);
       // Anzahl der Namen anhand der Anzahl der Kommata ermitteln
      int pos=0;
      int zaehler=0;
      int start=0;
      while (pos!=-1) { // in dieser Schleife sollte keine Exception auftreten
	pos = rein.indexOf(',',start);
	if (pos!=-1)
	  zaehler++;
	start=pos+1;	
      }// zaehler muesste beim Verlassen der Schleife die Anzahl der Kommata enthalten=Anzahl der Namen
     
      
      raus = new String [zaehler];
      rein = rein.substring (1); // Klammerauf entfernen
      for (int i=0;i<zaehler;i++) {
	int kommapos = rein.indexOf(',');
	raus [i] = rein.substring(0,kommapos);
	rein = rein.substring(kommapos+1); // gelesenen Namen und komma entfernen
      }
    }/*
    catch (IOException ioe ) {
      throw new KommException ("IOException bei getNamen;Message: "+ioe.getMessage());
    }*/
    catch (Exception e) {
       throw new KommException ("Exception bei getNamen;Message: "+e.getMessage());
    }
  return raus;
  }

 /** Zur Frage nach den Farben der Spieler.
	Ein Array-Element ist entweder null oder einer der Namen
    */
    public String [] getFarben() throws KommException{
	this.senden("GSF");
	String rein = this.einlesen();

	String [] back = getNamen2(rein);
	/*for (int i=0;i<back.length;i++) {
	    if (back[i]=="0")
		back[i]=null;
		}
	*/
	return back;

    }
  /**Info-Request zur Abfrage des Status eines Roboters; gibt ein Roboter-objekt zur&uuml;ck, dass alle notwendigen Informationen enth&auml;lt.
Falls der Roboter entfernt wurde, wird Roboter.leben auf -1 gesetzt, die restlichen Attribute werden nicht gesetzt (d.h = null oder was_auch_immer).
@exception KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen.
    */
  public Roboter getRobStatus (String name) throws KommException {
    String com ="";
    Roboter robot=new Roboter(name);
        String raus ="GRS("+name+")";
    this.senden(raus); 
    //Server sends "RS(Richtung(N,O..), Ort(1,1), LFlag, LArchF, Schaden, VLeben, GespRegister, Aktiv, Virtuell, RSreserveiert)"
    /*try{
      com=in.readLine(); 
      // System.out.println ("GETROBSTATUS: gelesener String: "+com);
    }
    catch(IOException ioe){
      throw new KommException("IOException bei GetRobStatus");
    }
    */
    com=this.einlesen();
    if (com.equals("RSE")){
	robot.setLeben(-1);
    }
    else{
      char richtung =com.charAt(3); // Richtung auslesen
      	if (richtung=='N')
	  robot.setAusrichtung(0);
	else if (richtung=='S')
	  robot.setAusrichtung(2);
	else if ((richtung=='E')||(richtung=='O'))
	 robot.setAusrichtung(1);
	else if (richtung=='W')
	  robot.setAusrichtung(3);
      int kommapos=com.indexOf(',');
      if (kommapos==-1)
	throw new KommException("getrobStatus: kein Komma gefunden");
      com=com.substring(kommapos+1); // "RS(<Richtung>," entfernen
      kommapos=com.indexOf(',');
      int klazupos=com.indexOf(')');
      String xk=com.substring(1,kommapos);
      String yk=com.substring(kommapos+1,klazupos);
      try {
	  robot.setPos(Integer.parseInt(xk), Integer.parseInt(yk));
      }
      catch (NumberFormatException nfe) {
	throw new KommException ("getRobStatus: (Ort-Parsen) NumberFormatException(Message: "+nfe.getMessage());
      }
      com=com.substring(klazupos+2); //(x,y), abschneiden
      kommapos=com.indexOf(',');
      String flagge = com.substring(0,kommapos);
      try {
	robot.setNaechsteFlagge(Integer.parseInt(flagge)+1);// +1, weil unser Roboter die naechste und nicht die letzte Flagge haben will
      }
      catch (NumberFormatException nfe2) {
	throw new KommException ("getRobStatus: (L-Flag-Parsen) NumberFormatException(Message: "+nfe2.getMessage());
      }
      com=com.substring(kommapos+1); // LFlag und Komma entfernen
      
      kommapos=com.indexOf(',');
      klazupos=com.indexOf(')');
      xk=com.substring(1,kommapos); // Archiv - Ort
      yk=com.substring(kommapos+1,klazupos);
      try {
	  robot.setArchiv(Integer.parseInt(xk),Integer.parseInt(yk));
      }
      catch (NumberFormatException nfe3) {
	throw new KommException ("getRobStatus: NumberFormatException (Archiv-Parsen)(Message: "+nfe3.getMessage());
      }
      com=com.substring(klazupos+2); // "(x,y)," entfernen
      
      kommapos=com.indexOf(',');
      String schaden = com.substring(0,kommapos);
      try {
	  robot.setSchaden(Integer.parseInt(schaden));
      }
      catch (NumberFormatException nfe4) {
	throw new KommException ("getRobStatus: NumberFormatException (Schaden-Parsen)(Message: "+nfe4.getMessage());
      }
      
      com =com.substring(kommapos+1);// schaden und Komma entfernen
      
      
      kommapos=com.indexOf(',');
      String vLeben = com.substring(0,kommapos);
      try {
	  robot.setLeben(3-Integer.parseInt(vLeben));
      }
      catch (NumberFormatException nfe5) {
	throw new KommException ("getRobStatus: NumberFormatException (Leben-Parsen)(Message: "+nfe5.getMessage());
      }
      com =com.substring(kommapos+1,com.length()-1);// vLeben und Komma entfernen; sowie die abschliessende Klammer (zu)
      
      String cards = new String(com.substring(0,com.lastIndexOf(')')));
      
      // das muesste jetzt der String mit den Karten sein 
      // z.B.: "((1,PK(M1,123))(2,PK(M2,456)))"
      // oder "((,))"
      
      robot.sperreRegister(getRegister(cards));
      // Kartenstring und letzte Klammer entfernen:
      com=com.substring(com.lastIndexOf(')'));
      klazupos=com.lastIndexOf(')'); // das muesste jetzt die letzte Klammer des Karten-Teilstrings sein
      com=com.substring(klazupos+1); // Karten-Teilstring entfernen
      
		 // com sieht jetzt entweder "<Bool>,<Bool>" oder ",<Bool>,<Bool>" aus
      if (com.charAt(0)==',')
	com=com.substring(1); // eventuelles Komma vor erstem Boolean entfernen
      
      // JETZT sieht com mit Sicherheit so aus "<bool>,<bool>"
      kommapos = com.indexOf(',');
      char aktiv = com.charAt(0);
      char virtuell = com.charAt(2);
      if ((aktiv=='t')||(aktiv=='T'))
	  robot.setAktiviert(true);
      else
	robot.setAktiviert(false);
      if ((virtuell=='t')||(virtuell=='T'))
	robot.setVirtuell(true);
      else
	robot.setVirtuell(false);
      
      
      //return robot;
    }
    return robot;
  }

  /**
   * Info-Request zur Abfrage des Spielstandes. 
   * Falls das Spiel beendet wurde, enth&auml;lt das String-Array die Namen der Spieler, 
   * wobei der Name des Gewinners an erster Stelle steht. L&auml;uft das Spiel noch, 
   * wird null zur&uuml;ckgegeben. 
   * @exception KommException Tritt beim Parsen ein Fehler auf 
   * (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen.
   */
  public String [] getSpielstand () throws KommException{
    ClientAntwort xyz = new ClientAntwort();
    this.senden ("GSS");
    /*
      try {
      xyz = wait2(in.readLine());
      }
    catch (IOException ioe) {
    throw new KommException ("getspielstand: Fehler beim Lesen (IOException mit Message: "+ioe.getMessage());
    }
    */
    String rein = this.einlesen();
    xyz=wait2(rein);
    
    if (xyz.typ==xyz.SPIELSTAND) {
	/*
      if (xyz.ok==true)
	return null;
      else 
	return xyz.namen;
	*/
	return xyz.namen;
	    }
    else if (xyz.typ==0) {
      throw new KommException ("getSpielstand: keine gueltige Antwort");
    }
    else 
     throw new KommException ("getSpielstand: Antwortobjekt vom falschen Typ zurueckgegeben; Typ: "+xyz.typ);
    
  }
  /** Info-Request zur Abfrage des sogenannten Spielstatus.
 Sie gibt f&uuml;r jeden Roboter ein Statusobjekt zur&uuml;ck, das dessen Namen, seine bisher ausgewerteten Registerinhalte und die aktuelle Auswertungsphase als Attribute besitzt.
Falls gerade nicht ausgewertet wird, wird null zurückgegeben.
@exception KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen.*/
  public Status [] getSpielstatus() throws KommException{
    ClientAntwort xyz= new ClientAntwort();
    // try {
      this.senden ("GSA");
      //String rein = in.readLine();
      String rein = this.einlesen();
      xyz = wait2(rein);
      /* }
    catch (IOException ioe) {
      throw new KommException ("IOException bei getSpielstatus; Message: "+ioe.getMessage());
    }*/
      if (xyz.typ==xyz.SPIELSTATUS){
	if (xyz.ok=false)
	  return null;
	 else 
	   return xyz.stati;
      }
      else if (xyz.typ!=0)
	throw new KommException ("Falsche Antwort bei getSpielStatus: habe ClientAntwort vom Typ "+xyz.typ+" erhalten");
      else 
	throw new KommException ("Ungueltigen String bei getSpielstatus erhalten");
    
			       
      
  }
  

  /** Request zur Abfrage des Timeouts. Rueckgabewert: Timeout in Sekunden
@exception KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen.*/
  public int getTimeOut () throws KommException {
   ClientAntwort xyz= new ClientAntwort();
   String com="";
   // try {
   this.senden("GTO");
   //com=in.readLine();    
       /* }
    catch (IOException ioe) {
      throw new KommException ("IOException bei getTimeOut (lesen von 'in')");
    }*/
   com=this.einlesen();
   xyz=wait2(com);
   if (xyz.typ==xyz.TIMEOUT)
     return xyz.zahl;
   else 
     throw new KommException ("getTimeOut: falsche Antwort(Typ: "+xyz.typ+")");
  }
  /** Antwort auf spielstart*/
  public void spielstart ()  {
   try {
     this.senden ("ok");
   }    
   catch (KommFutschException k) {
     System.err.println ("Fehler bei spielstart: Exception beim Senden des ok\nMessage: "+k.getMessage());
   }
  }

    /** Bestaetigung auf irgendwas vom Server, was Bestaetigt werden muss. */ 
  public void bestaetigung ()  {
      this.spielstart();
  }

  // Jetzt kommen HILFSMETHODEN:

  // bearbeitet Anfragen nach einem Ort
 private  Ort fetchOrt (String name, boolean RobPos) throws KommException{
   Ort back= new Ort (-1,-1); // Initialisierung
    String method="noch fetchOrt";;
    try {
      if (RobPos) { // getRobPos ?!
	method="getRobPos";
	this.senden ("SRO("+name+")");

      }
      else { // nicht RobPos => nordostecke gefragt
	method="gibSpielfeldDim";
	this.senden ("GSD");
	
      }
      String rein = this.einlesen();
      //String rein = in.readLine();
      // rein= "(x,y)"
      int komma=rein.indexOf (',');
      int klammerzu=rein.indexOf(')');
      
      back.x = Integer.parseInt(rein.substring(1,komma));
      back.y = Integer.parseInt(rein.substring(1+komma, klammerzu));     
    }
    catch (StringIndexOutOfBoundsException sioob){
      throw new KommException (method+" warf StringIndexOutOfBoundsException(Inhalt:"+sioob.getMessage()+"); Ursache: vermutlich falsch aufgebaute Antwort vom Server");
    }
    catch (NumberFormatException nfe) {
      throw new KommException(method+": NumberFormatException: Parsen der koordinaten-substrings schlug fehl");
    }
    /*    catch (IOException ioe) {
       throw new KommException (method+" warf eine IOException; Message: "+ioe.getMessage());
    }*/
    catch (Exception sonstige) {
      throw new KommException (method+" warf eine Exception; Message: "+sonstige.getMessage());
    }
    return back;
  }

  /**Diese Methode parst den Programmkartenteil bei getRobStatus
   */
  public static Status getStatusRegs(String in)throws KommException {
    Status back=new Status();
    // zum Parsen der Register bei getRobStatus
    // in soll in etwa so aussehen: (name,PK(M1,123)PK(M2,456))
    // oder Spezialfall: (name,)
    int kommaName=in.indexOf(',');
    back.robName=new String (in.substring(1,kommaName));
    in = in.substring(kommaName+1); // nur noch die Karten und die Klammerzu
    String in2 = new String (in);
    int kpos = in2.indexOf(',');
    int ks = 0; // Anzahl kommas = Anzahl Karten
    
    while (kpos != -1) {// Karten zaehlen
      ks++;
      in2 = in2.substring(kpos+1);
      kpos = in2.indexOf (',');
    }
    //System.out.println ("Karten: "+ks);
    back.register= new Karte [ks];
    int i=0;
    while (in.length()>4) {
      // System.out.println ("While-Loop mit: "+in);
      if (i<ks) {
	int komma = in.indexOf(',');// das Komma zwischen Aktion und Prioritaet
	int klauf = in.indexOf('(');
	int klazu = in.indexOf(')');
	if ((komma!=-1)&&(klauf!=-1)&&(klazu!=-1)) {
	  String akt = in.substring(klauf+1,komma);
	  String prio = in.substring(komma+1,klazu);
	  int p=0;
	  try {
	    p=Integer.parseInt(prio);
	  }
	  catch (NumberFormatException nfe) {
	    throw new KommException ("NumberFormatException bei KC.getStatusRegs; Message: "+nfe.getMessage());
	  }
	  
	  back.register[i]= new Karte (p, akt);
	  i++;
	  in = in.substring(klazu+1);
	  // System.out.println ("Beende While-Loop mit: "+in);
	}
      }
    }
    return back; 
  }

 private static Karte [] getRegister (String str)throws KommException {
    Karte [] back = new Karte [5];
    for (int i=0;i<5;i++)
      back[i]=null;
    // z.B.: "((1,PK(M1,123))(2,PK(M2,456)))"
    // oder "((,))"
    //Fall ((,)):
    if (str.length()<=5)
      return back;
    else {
      String active = str.substring(2);
      active+="**";
      while (active.length()>4){

	int register=0;
	try {
	 
	  register = Integer.parseInt(active.substring(0,1));
	}
	catch (NumberFormatException nf) {
	  throw new KommException ("getRegister: NumberFormatException bei Parsen der Registernummer");
	}
	// Kartenwerte auslesen:
	int klauf=active.indexOf('(');
	//	int klzu=active.indexOf(')');
	active=active.substring (klauf-2); // alles vor PK wegschneiden
	klauf=active.indexOf('(');
	int klzu=active.indexOf(')');
	int komma=active.indexOf(',');

	String kartenaktion=active.substring(klauf+1,komma);

	int prioritaet=0;
	try {
	 
	  prioritaet=Integer.parseInt(active.substring(komma+1,klzu));// KLZU-2
	 
	}
	catch (NumberFormatException nf2) {
	  throw new KommException ("getRegister: NumberFormatException bei Parsen der Kartenprioritaet: Message: "+nf2.getMessage());
	}			 
	back [register-1]=new Karte(prioritaet,kartenaktion); // Karte eingeteilt
	//jetzt String aktualisieren:

	active=active.substring(klzu+3); // "1,PK(M1,123))(" abschneiden    KLZU+1

      }
     
      return back;
    }
  }
  /** Gibt die Position (in com) hinter der  ')' des ersten Vorkommens von ")("zurueck;
    gibt es keinen substring ")(", so wird -1 zurueckgegeben.
    */
  public static int klazu2 (String com) {
    String comLocal=new String (com);
    comLocal+="**"; // vermeidet spaeter StringIndexOutOfBoundsException
    int klazu=comLocal.indexOf (')');
    int raus=klazu+1;
    while (klazu!=-1) {
     
      if (comLocal.charAt(klazu+1)=='(')
	return raus;
      comLocal = comLocal.substring (klazu+1);
      klazu = comLocal.indexOf(')');
      raus+=klazu+1;
      
    }   
    return -1;
  }
  /** Hilfsmethode fuer GetRobStatus-Antwort.
    Liefert den String zurueck, der den ersten Roboter betrifft:
    Bekommt die Methode "(name1,PK(M1,123))(name2,))" uebergeben,so gibt sie 
    "(name1,PK(M1,123))" zurueck.
    Der uebergebene String wird nicht veraendert.
   */
  public static String splitFirstRob (String com) {
    int trennPos = klazu2(com);
    String back="";
    if (trennPos!=-1){
      back=com.substring(0,trennPos);
     
    }
    else {
      back=com;
    
    }
    return back;
  }
    /** Finalizer, der die Streams zu macht.
     * (Wird vom garbage collector aufgerufen.)
     * @author Miriam
     */
    protected void finalize() throws IOException {
	if (in != null) in.close();
	if(out != null) out.close();
    }
}












