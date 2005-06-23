package de.botsnscouts.util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;


/** Eine Klasse nit mehr oder minder hilfreichen Methoden*/
public class H {

    /** Diese Methode laesst den aktuellen Thread f&uuml;r 'millisekunden' Millisekunden warten.*/
    public static void warte(int millisekunden){
	synchronized (Thread.currentThread()){
	    try {
		Thread.currentThread().wait (millisekunden);
	    }
	    catch (InterruptedException ie){
	    }
	}
    }

    /** Diese Methode erzeugt einen String, der aus 'anzahl' Leerzeichen besteht.*/
    public static String whiteSpace(int anzahl) {
	return getZeichen(' ', anzahl);
    }
    /** Diese Methode erzeugt einen String, der 'anzahl' mal aus dem Zeichen 'z' besteht.*/
    public static String getZeichen (char z, int anzahl) {
	StringBuffer back=new StringBuffer(anzahl);
	for (int i=0;i<anzahl;i++)
	    back.append(z);
	return back.toString();
    }
    /** Zentriert den String s auf einer 'laenge' langen Zeile,
	d.h. es wird die entsprechende Anzahl Spaces davorgesetzt.
	@param s Der zu zentrierende String
	@param laenge Die L&auml;nge der Zeile, auf der s zentriert werden soll
	@return Den String s mit einer entsprechenden Anzahl Spaces davor.
    */
    public static String center (String s, int laenge){
	if (s.length()>=laenge-1)
	    return s;
	else {
	    int indent=laenge/2 -s.length()/2;
	    return whiteSpace(indent)+s;
	}
    }


    /** Liest einen String von der Standardeingabe ein
	@param str Dieser String wird als Eingabeaufforderung ausgegeben(kein Doppelpunkt)
    */
    public static String readString (String str) {
	InputStreamReader isrIn=new InputStreamReader(System.in);
	BufferedReader in=new BufferedReader(isrIn);
	String str2="";
	System.out.print(str);
	try {

	    str2=in.readLine();
	}
	catch
	    (IOException bloed)
	    {
		abbruch();
	    }
	return str2;

    }
    /** Liest einen String von der Standardeingabe ein*/
    public static String readString() {
	return readString("");
    }

    /** Diese Methode rundet eine positive Zahl x auf drei Stellen nach dem Komma*/
    public static double runde (double x ) {
	return runde (x,3);
    }

    /** Rundet eine positive Zahl  x auf i Stellen nach dem Komma*/
    public static double runde (double x, int i)  {
	if (i>=0)
	    {
		int t=1;
		for (int j=0;j<i;j++)
		    t=t*10;
		x=t*x;
		int t2= (int) x;
		if ((x-t2)>=0.5)
		    t2++;
		return ((double)t2/(double)t);
	    }
	else return x;
    }

    /** Diese Methode testet, ob die Datei mit dem Namen filename existiert*/
    public static boolean fileExists (String filename)  {
	try {
	    BufferedReader in = new BufferedReader(new FileReader(filename));
	}
	catch (java.io.FileNotFoundException ex)
	    {return false;}
	return true;
    }
    /** Pseudo ClearScreen-Befehl f&uuml;r die Textconsole;
	Die Methode gibt 80 Leerzeilen aus*/
    public static void cls () {
	for (int i=0;i<20;i++)
	    H.pln ("\n\n\n");
    }

    /** Diese Methode Liest eine Integer-Zahl von der Standardeinfgabe*/
    public static int readInt ( ) {
	return readInt("");
    }

    /** Versucht, dass File 'foo' zu erzeugen.
	Eventuell fehlende Verzeichnisse werden (hoffentlich :-)) erzeugt.
    */
    public static void createFile (File foo) throws IOException {
	 if (!foo.exists()){ // erster Versuch; erfolgreich, falls kein Verzeichnis erzeugt werden muss
		try {
		    PrintWriter out = new PrintWriter (new FileWriter(foo));
		    out.println();
		    out.close();
		}
		catch (java.io.IOException ioe2){
		    // in der Regel muss in diesem Fall erst noch das Verzeichnis erstellt werden
		    System.err.println("FEHLER beim Erzeugen der Datei"+foo.toString());
		    System.err.println("Versuche evtl. fehlendes Verzeichnis zu erzeugen...");
		    try {
			// zerhacken des Pfadnamens
			StringTokenizer bar = new StringTokenizer (foo.toString(),File.separator);
			String [] foobar = new String[bar.countTokens()];
			for (int i2=0;i2<foobar.length;i2++)
			    foobar [i2]=bar.nextToken();
			// foobar muesste jetzt alle Elemente des angegebenen Dateipfades beinhalten
			String newpath="";
			if (foobar[0].indexOf(':')>0){ // wir haben in diesem Fallvermutlich Dos-Dateiname mit "c:"
			    newpath=foobar[0]; // Verzeichniswurzel
			    for (int i2=1;i2<foobar.length-1;i2++) {
				newpath+=foobar[i2]+File.separator;//stueckweiser Aufbau des neuen Pfades
				File temp = new File (newpath);
				if (!temp.exists())
				    temp.mkdir(); //erzeugen des Verzeichnisses, falls noch nicht vorhanden
			    }
			}
			else { // vermutlich Linux/Unix Dateipfad
			    newpath=File.separator;
			    for (int i2=0;i2<foobar.length-1;i2++) {
				newpath+=foobar[i2]+File.separator;
				File temp = new File (newpath);
				if (!temp.exists())
				    temp.mkdir();
			    }
			    // jetzt muesste der Pfad soweit erstellt worden sein;
			    // fehlt noch die Datei selber:
			}
			PrintWriter out = new PrintWriter (new FileWriter(foo));
			out.println();
			out.close();
		    }
		    catch (java.io.IOException ioe3){ /* Wenn die kommt, wars wohl ein anderer Fehler
							 oder ich habe einen Fall nicht beruecksichtigt..
							 BUG: ..zum Beispiel, dass jemandem die Permissons fehlen..
						      */
			ioe3.printStackTrace();
			throw new IOException ("... War erfolglos.\nIOException beim Erzeugen der nicht vorhandenen Datei \""+foo.toString()+"\" aufgetreten: "+ioe3.getMessage());

		    }
		    System.err.println("..erfolgreich!");
		}
	    }
    }


    /** Diese Methode Liest eine Integer-Zahl von der Standardeinfgabe;
	@param str Dieser String wird als Eingabeaufforderung ausgegeben(kein Doppelpunkt)
    */
    public static int readInt (String str) {
	InputStreamReader isrIn=new InputStreamReader(System.in);
	BufferedReader in=new BufferedReader(isrIn);
	int m=0;
	boolean okay2=false;
	while (!okay2){
	    System.out.print (str);
	    okay2=true;
	    try{
		m=Integer.parseInt(in.readLine());
	    }
	    catch (NumberFormatException se){
		okay2=false;
	    }
	    catch (IOException se){
		abbruch();
	    }
	}
	return m;
    }

    /** Diese Methode Liest eine Integer-Zahl von der Standardeinfgabe;
	Diese muss gr&ouml;sser als a und kleiner als b sein.
	@param str Dieser String wird als Eingabeaufforderung ausgegeben(kein Doppelpunkt)
	@param a Nur gr&ouml;ssere Zahlen werden angenommen
	@param b Nur kleinere Zahlen werden angenommen
    */
    public static int readInt (String str,int a, int b) {
	InputStreamReader isrIn=new InputStreamReader(System.in);
	BufferedReader in=new BufferedReader(isrIn);
	int m=0;
	boolean okay2=false;
	while (!okay2){
	    System.out.print (str);
	    okay2=true;
	    try{
		m=Integer.parseInt(in.readLine());
		if (!((m>a)&&(m<b))){
		    okay2=false;
		    System.out.println ("Please enter a value between "+(a+1)+" and "+(b-1));
		}
	    }
	    catch (NumberFormatException se){
		okay2=false;
	    }
	    catch (IOException se){
		abbruch();
	    }
	}
	return m;
    }

    /** Diese Methode liest einen einzelnen Buchstaben von der Standardeingabe;
	wird mehr als ein Zeichen eingegeben, so wird der erste Buchstabe eingelesen
	und der Rest der Eingabe ignoriert.
	@param str Dieser String wird als Eingabeaufforderung ausgegeben(kein Doppelpunkt)
    */
    public static char readChar (String str) {
	BufferedReader menuIn = new BufferedReader(new InputStreamReader(System.in));
	String w="";
	char wahl=' ';
	System.out.print(str);
	try{
   	    w=menuIn.readLine();
	}
	catch (IOException se){
	    abbruch();
	}

	try{ wahl=w.charAt(0);
	}
	catch (StringIndexOutOfBoundsException se){
	    wahl=' ';
	}
	return wahl;
    }
    public static char readChar () {
	return readChar("");
    }
    /** F&uuml;r Schreibfaule;
	Gibt s auf der Standardausgabe aus*/

    public static void p (String s) {
	System.out.print(s);
    }
    /** Gibt eine Leerzeile aus*/
    public static void pln () {
	System.out.println();
    }
    /** Gibt s aus und springt in eine neue Zeile*/
    public static void pln(String  s) {
	System.out.println(s);
    }
    /** Ein altes &Uuml;berbleibsel;
	wird von den read-Funktionen benutzt.
    */
    private static void abbruch()
    {
	System.err.println("Es ist eine I/O-Exception aufgetreten.");
	System.err.println("Das sollte eigentlich nicht passieren.");
	System.exit(5);
    }
    /** Gibt einen Datumsstring der Form "Fri Nov 12 01:34:19 GMT+01:00 1999" zur&uuml;ck.*/
    public static String date () throws NumberFormatException, StringIndexOutOfBoundsException {

	Calendar c=Calendar.getInstance();
	Date d =c.getTime();
	String s=d.toString();
	StringTokenizer st= new StringTokenizer(s," ");
	String [] sa = new String [st.countTokens()];
	int i=0;
	while (st.hasMoreTokens()) {
	    sa [i]=st.nextToken();
	    i++;
	}
	s = sa[0]+", "+sa[2]+"_"+sa[1]+"_"+sa[5]+"  "+sa[3]+" ("+sa[4]+")";
	// s:= "Fri Nov 12 01:34:19 GMT+01:00 1999"
	// -> Fri, 12_Nov_1999
	//in JAVA 1.2: String s = DateFormat.getDateInstance(DateFormat.FULL,Locale.GERMANY);
	return s;

    }
    /** Diese Methode pr&uuml;ft, ob der String a lexikographisch kleiner ist
	als der String b.
	@return true, wenn a lexikographisch kleiner als b ist.
    */
    public static  boolean lessString (String a, String b) {
	if (a.compareTo(b)<0)
	    return true;
	else
	    return false;
    }
    /** Diese Methode pr&uuml;ft zwei Strings auf Gleichheit.
	@return true, falls a und b lexikographisch gleich sind.
     */
    public static boolean eqString (String a, String b ) {
	if (a.compareTo(b)==0)
	    return true;
	else
	    return false;
    }

    /** Diese Methode pr&uuml;ft, ob der String a lexikographisch kleiner oder gleich
	dem String b ist.
	@return true, wenn der String a lexikographisch kleiner oder gleich String b ist.
    */
    public static boolean lesseqString (String a, String b) {
	if (a.compareTo(b)<=0)
	    return true;
	else
	    return false;
    }

    
   
    
    


}
