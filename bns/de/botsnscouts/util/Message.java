package de.botsnscouts.util;

import java.io.*;
import java.util.*;
import java.lang.*;
/**
* Diese Klasse setzt Meldungen aller Art in verschiedene Sprachen um,
* abhaengig vom File conf/messages.language
*/


    public class Message{


	public static String language = "empty"; 
	private static Hashtable Sectiontable = new Hashtable(); // Die "offizielle" Übersetzungstabelle

	/**
	 * Sprache mit setLanguage waehlen
	 */
	public static void setLanguage(String lang) throws LanguageLoadException{
	    BufferedReader messages;
	    // System.err.println();
	    // System.err.println("**********************************");
	    // System.err.println("SETLANGUAGE");
	    // System.err.println("**********************************");
	    try {

		InputStream istr=Message.class.getResourceAsStream("conf/messages."+lang);//Ludmila

		messages = new BufferedReader(new InputStreamReader(istr));//Ludmila
		
		//System.out.print("öffnen...");
		//messages = new BufferedReader(new FileReader("conf/messages."+lang));
		//System.out.println(" OK");
	    }
	    catch(Exception e){
		// System.err.println("EXCEPTION setLanguage: "+e.getMessage());
		if (lang=="deutsch")  // ohh, ohh, das sollte nicht passiert sein, da fehlt wohl selbst conf/message.deutsch
		    {throw new LanguageLoadException("serious error: cannot load conf/messages.deutsch - Schwerwiegender Fehler: Kann conf/messages.deutsch nicht laden!!");}
		else
		    throw new LanguageLoadException("cannot load conf/messages."+lang+" - "+Message.say("Message","xCanNotLoadFile","conf/messages."+lang));
	    }

	    Hashtable Sektionen = new Hashtable();  // Hierdrin liegen die Sektionen, die wiederum die Meldungen enthalten
            int zeile=0;
	    try{	// syntax-Prüfung & Laden language-Datei
		String line=null;
		String sectionName="";
		int AnzahlSektionen=0;
		// messages.mark(4); // aktuelle Pos. (Anfang) markieren.
		while((line=messages.readLine())!=null) {
		    zeile++; // Zeilennummer für Fehlermeldung mitzählen
		    // System.err.println("Zeile: "+zeile);
		    if ((line.length()>0)&& (line.charAt(0)!='#') && (line.charAt(0)!=' ')) // kein "#" und kein Leerzeichen am Anfang
			if (line.charAt(0)=='[') // Aha, eine Sektion...
			    {
				sectionName="";
				int i=1;			    
				while(line.charAt(i)!=']'){     // Sektions-Name ermitteln
				    sectionName=sectionName+line.charAt(i);
				    i++;
				}
				Hashtable Elemente = new Hashtable();  // Elemente-Hashtable erzeugen
				Sektionen.put(sectionName,Elemente);   // Sektionsname und Sektions-Elemente-Hashtable in Sektions-Hashtable ablegen
			    }
			else   // der Inhalt einer Sektion folgt....
			    {
				Hashtable Element =(Hashtable) Sektionen.get(sectionName);
				int i=0;
				String MeldungsID="";
				String MeldungsText="";
				// System.err.println("zeile: "+ line);
				while((line.charAt(i)!=' ') &&(line.charAt(i)!='\t')){    // Meldungs-ID ermitteln
				    // System.err.print(line.charAt(i));
				    MeldungsID=MeldungsID+line.charAt(i);
				    i++;
				}
				while((line.charAt(i)==' ')||(line.charAt(i)=='\t')){i++;} // Leerzeichen zwischen ID und Text entfernen
				MeldungsText=line.substring(i); // Der Rest ist Meldungs-Text
				Element.put(MeldungsID,MeldungsText);
			    }
		}
	    }
	    catch(Exception e){
		// System.err.println("\n\n\n EXCEPTION!!!!  "+e.getMessage()+"\n\n\n");
		if (lang=="deutsch") 
		    {System.err.println("serious error: cannot load conf/messages.deutsch, error in line: "+zeile+"\nSchwerwiegender Fehler: Kann conf/messages.deutsch nicht laden, Fehler in Zeile: "+zeile+"\n Details: "+e.getMessage());
		    System.exit(1);
		    }
		else throw new LanguageLoadException("error in language-file conf/messages."+lang+" - Line "+zeile);
	    }
	    language=lang;        // hat wohl alles geklappt, also setzen wir die Sprache
	    Sectiontable=Sektionen;  // und uebertragen die Daten in die "offizielle" Hashtable "Sectiontable"
	} // Ende setLanguage


	/**
	 * Umsetzen von Meldungs-IDs in Strings.
	 * Der erste Parameter ist die Sektion (String),
	 * der zweite Parameter ist die Meldungs-ID (String)
	 * beginnend mit "m" (Meldung),"x" (Exception), oder "e" (Error).
	 *
	 * Danach folgen null(0) bis zu vier(4) weitere Parameter, einzusetzen in die Platzhalter $1,$2,$3,$4
	 * Diese bis zu 4 Parameter dürfen wahlweise vom Typ String oder int sein. (Toll, was?) ;-)
	 * 
	 * say dient der Optionalität der vier Parametern und der Ermöglichung der Typauswahl String/int per Overloading.
	 * Die Umsetzung der Meldungs-IDs in die richtige Sprache findet in sayIt statt.
	 */

	public static String say(String callerSection,String id,String[] args)
	{
	    switch (args.length){
	    case 0: return say(callerSection,id);
	    case 1: return say(callerSection,id,args[0]);
	    case 2: return say(callerSection,id,args[0],args[1]);
	    case 3: return say(callerSection,id,args[0],args[1],args[2]);
	    default: return say(callerSection,id,args[0],args[1],args[2],args[3]);
	    }
	}
	
	public static String say(String callerSection,String id){ 
	   return(sayIt(callerSection,id,"","","",""));
	}
	public static String say(String callerSection,String id,String P1){ // Parameter String
	   return(sayIt(callerSection,id,P1,"","",""));
	}
	public static String say(String callerSection,String id,int P1){    // Parameter Int   
	   return(sayIt(callerSection,id,Integer.toString(P1),"","",""));
	}
	public static String say(String callerSection,String id,String P1,String P2){  // Parameter String String
	   return(sayIt(callerSection,id,P1,P2,"",""));
	}
	public static String say(String callerSection,String id,String P1,int P2){     // Parameter String Int   
	   return(sayIt(callerSection,id,P1,Integer.toString(P2),"",""));
	}
	public static String say(String callerSection,String id,int P1,String P2){     // Parameter Int    String
	   return(sayIt(callerSection,id,Integer.toString(P1),P2,"",""));
	}
	public static String say(String callerSection,String id,int P1,int P2){        // Parameter Int    Int
	   return(sayIt(callerSection,id,Integer.toString(P1),Integer.toString(P2),"",""));
	}
	public static String say(String callerSection,String id,String P1,String P2,String P3){ // Parameter String String String
	   return(sayIt(callerSection,id,P1,P2,P3,""));
	}

	public static String say(String callerSection,String id,int    P1,String P2,String P3){ // Parameter Int    String String
	   return(sayIt(callerSection,id,Integer.toString(P1),P2,P3,""));
	}
	public static String say(String callerSection,String id,String P1,int    P2,String P3){ // Parameter String Int    String
	   return(sayIt(callerSection,id,P1,Integer.toString(P2),P3,""));
	}
	public static String say(String callerSection,String id,String P1,String P2,int    P3){ // Parameter String String Int   
	   return(sayIt(callerSection,id,P1,P2,Integer.toString(P3),""));
	}

	public static String say(String callerSection,String id,String P1,int    P2,int    P3){ // Parameter String Int    Int   
	   return(sayIt(callerSection,id,P1,Integer.toString(P2),Integer.toString(P3),""));
	}
	public static String say(String callerSection,String id,int    P1,String P2,int    P3){ // Parameter Int    String Int   
	   return(sayIt(callerSection,id,Integer.toString(P1),P2,Integer.toString(P3),""));
	}
	public static String say(String callerSection,String id,int    P1,int    P2,String P3){ // Parameter Int    Int    String
	   return(sayIt(callerSection,id,Integer.toString(P1),Integer.toString(P2),P3,""));
	}

	public static String say(String callerSection,String id,int    P1,int    P2,int    P3){ // Parameter Int    Int    Int   
	   return(sayIt(callerSection,id,Integer.toString(P1),Integer.toString(P2),Integer.toString(P3),""));
	}

	public static String say(String callerSection,String id,String P1,String P2,String P3,String P4){ // Parameter S S S S
	   return(sayIt(callerSection,id,P1,P2,P3,P4));
	}
	public static String say(String callerSection,String id,String P1,String P2,String P3,int    P4){ // Parameter S S S I
	   return(sayIt(callerSection,id,P1,P2,P3,Integer.toString(P4)));
	}
	public static String say(String callerSection,String id,String P1,String P2,int    P3,String P4){ // Parameter S S I S
	   return(sayIt(callerSection,id,P1,P2,Integer.toString(P3),P4));
	}
	public static String say(String callerSection,String id,String P1,String P2,int    P3,int    P4){ // Parameter S S I I
	   return(sayIt(callerSection,id,P1,P2,Integer.toString(P3),Integer.toString(P4)));
	}
	public static String say(String callerSection,String id,String P1,int    P2,String P3,String P4){ // Parameter S I S S
	   return(sayIt(callerSection,id,P1,Integer.toString(P2),P3,P4));
	}
	public static String say(String callerSection,String id,String P1,int    P2,String P3,int    P4){ // Parameter S I S I
	   return(sayIt(callerSection,id,P1,Integer.toString(P2),P3,Integer.toString(P4)));
	}
	public static String say(String callerSection,String id,String P1,int    P2,int    P3,String P4){ // Parameter S I I S
	   return(sayIt(callerSection,id,P1,Integer.toString(P2),Integer.toString(P3),P4));
	}
	public static String say(String callerSection,String id,String P1,int    P2,int    P3,int    P4){ // Parameter S I I I
	   return(sayIt(callerSection,id,P1,Integer.toString(P2),Integer.toString(P3),Integer.toString(P4)));
	}
	public static String say(String callerSection,String id,int    P1,String P2,String P3,String P4){ // Parameter I S S S
	   return(sayIt(callerSection,id,Integer.toString(P1),P2,P3,P4));
	}
	public static String say(String callerSection,String id,int    P1,String P2,String P3,int    P4){ // Parameter I S S I
	   return(sayIt(callerSection,id,Integer.toString(P1),P2,P3,Integer.toString(P4)));
	}
	public static String say(String callerSection,String id,int    P1,String P2,int    P3,String P4){ // Parameter I S I S
	   return(sayIt(callerSection,id,Integer.toString(P1),P2,Integer.toString(P3),P4));
	}
	public static String say(String callerSection,String id,int    P1,String P2,int    P3,int    P4){ // Parameter I S I I
	   return(sayIt(callerSection,id,Integer.toString(P1),P2,Integer.toString(P3),Integer.toString(P4)));
	}
	public static String say(String callerSection,String id,int    P1,int    P2,String P3,String P4){ // Parameter I I S S
	   return(sayIt(callerSection,id,Integer.toString(P1),Integer.toString(P2),P3,P4));
	}
	public static String say(String callerSection,String id,int    P1,int    P2,String P3,int    P4){ // Parameter I I S I
	   return(sayIt(callerSection,id,Integer.toString(P1),Integer.toString(P2),P3,Integer.toString(P4)));
	}
	public static String say(String callerSection,String id,int    P1,int    P2,int    P3,String P4){ // Parameter I I I S
	   return(sayIt(callerSection,id,Integer.toString(P1),Integer.toString(P2),Integer.toString(P3),P4));
	}
	public static String say(String callerSection,String id,int    P1,int    P2,int    P3,int    P4){ // Parameter I I I I
	   return(sayIt(callerSection,id,Integer.toString(P1),Integer.toString(P2),Integer.toString(P3),Integer.toString(P4)));
	}
	

	/**
	 * sayIt führt die Umsetzung von Meldungs-IDs in Meldungen(in der richtigen Sprache) 
	 * und das Einsetzen der (bis zu) vier Parameter in die Platzhalter im Text dann wirklich aus:
	 *
	 * Der erste Parameter ist die Sektion "Section" (String),
	 * der zweite Parameter ist die Meldungs-ID "id" (String)
	 * beginnend mit "m" (Meldung),"x" (Exception), oder "e" (Error) gefolgt von der Bezeichnung der Meldungs-ID.
	 * (zum Beispiel: "xFileNotFound")
	 * danach folgen vier weitere Parameter mit in Platzhalter 
	 * einzusetzenden Strings.
	 * Ein Beispiel:
	 *   Die Ausgabe: "Sie haben noch 6 Leben." 
	 *   würde hier als  sayIt("MeineKlasse",mAnzLebenMitteilen,"6","","","") aufgerufen.
	 *   Die Datei "messages.deutsch" müßte dann folgendes beinhalten:
	 *   
	 *   [MeineKlasse]
	 *   ....hier kann irgendwas stehen...
	 *   mAnzLebenMitteilen Sie haben noch $1 Leben.
	 *
	 *  Anmerkung: 
	 *  In der Klasse "MeineKlasse.java" muesst der Aufruf dann
	 *  z.B. System.out.println(Message.say("MeineKlasse",mAnzLebenMitteilen,6));
	 *  lauten.                         ^^^
	 */
        public static String sayIt(String Section, String id, String Param1, String Param2, String Param3, String Param4) {
	    try{
		if (language.equals("empty")){
		    System.err.println("Warning! No language set! Using german...");
		    setLanguage("deutsch");
		} // sollte eigentlich schongesetzt sein...
	    }
	    catch (LanguageLoadException e) // Falls hier was falsch läuft haben wir eh verloren
	    	{System.err.println("Serious error: basic language file messages.deutsch (german) not found!!!");System.exit(1);}

	    String strSrc="";  // Source-String (ohne eingesetzte Parameter)
	    String strDest=""; // Destination-String (mit eingesetzten Parametern
	    Hashtable ErgebnisSektion =(Hashtable)Sectiontable.get(Section);       // Hashtable der betreffenden Sektion holen
	    if (ErgebnisSektion==null) 
		{
		    System.err.println("section '"+Section+"' not found in language-file messages."+language+" - ");
		    System.err.println(Message.say("Message","xSectionNotFound",Section));
		    return("["+Section+"] not in language-file - "+Message.say("Message","xSectionNotFound",Section));
		}
	    strSrc=(String)ErgebnisSektion.get(id);                                  // Ausgabe-String in der Sektion raussuchen
	    if (strSrc==null) 
		{
		    System.err.println("Message-ID '"+id+"' not found in language-file messages."+language+" \n"+Message.say("Message","xKeyNotFound",id,Section));
		    return(id+" not in language-file section ["+Section+"] - "+Message.say("Message","xKeyNotFound",id,Section));
		}
	    int i=0;                  // Parameter einsetzen
	    for (i=0;i<strSrc.length();i++)  // durch den String laufen (um nach Parameter-Platzhaltern ("$1","$2","$3","$4") zu suchen)
		{
		    if ((strSrc.charAt(i)=='$') && ((i+1)<strSrc.length()) && ((strSrc.charAt(i+1)=='1')||(strSrc.charAt(i+1)=='2')||(strSrc.charAt(i+1)=='3')||(strSrc.charAt(i+1)=='4')))   // Parameter gefunden?
			{
			    int n = Integer.parseInt(""+strSrc.charAt(i+1));
			    switch (n){
			    case 1:  // $1 erkannt
				strDest=strDest+Param1;
				i++;
				break;
			    case 2: // $2 erkannt
				strDest=strDest+Param2;
				i++;
				break;
			    case 3: // $3 erkannt
				strDest=strDest+Param3;
				i++;
				break;
			    case 4: // $4 erkannt
				strDest=strDest+Param4;
				i++;
				break;
			    }
			} // ende if (Parameter-Erkennung)

		    else 
			{
			    if ( ((strSrc.charAt(i)=='\\') && ((i+1)<strSrc.length())) && (strSrc.charAt(i+1)=='n') ) //  'new line' ("\n") gefunden / Zeilenumbruch einfuegen.
				{
				    strDest=strDest+"\n";  // Zeilenumbruch einfuegen
				    i++;                   // naechstes Zeichen ueberspringen ("\n" besteht aus zwei Zeichen :)
				} // ende if ('new line')
			    else {strDest=strDest+strSrc.charAt(i);};  // falls kein Parameter, dann einfach Zeichen zufuegen.
			}
		} // ende for (durch den String laufen)

	    return(strDest);
	} // Ende sayIt

	public static void printMessageTable(){
	    for (Enumeration enA = Sectiontable.keys(); enA.hasMoreElements() ;) {
		String elem=(String)enA.nextElement();
		System.out.println("["+elem+"]");
		Hashtable h = (Hashtable)Sectiontable.get(elem);
		for (Enumeration enB = h.keys(); enB.hasMoreElements() ;)
		    {
			String elem2=(String)enB.nextElement();
			System.out.print(" "+elem2+" -> ");
			System.out.println(h.get(elem2));
		    }
	    }
	} // Ende debugTableWriter   
	
    } // Ende Klasse "Message"
