package de.botsnscouts.util;

import java.io.*;
import java.util.*;
import java.lang.*;
/**
* Diese Klasse ermöglicht das Auslesen (read) und setzen(write)
* von Benutzereinstellungen 
* abhaengig vom File conf/config.txt
*/


    public class UserConfig{


	// public static String language = "empty"; 
	private static Hashtable Sectiontable = new Hashtable(); // Die "offizielle" Setup-Datenbank mit Sektionen

	/**
	 * Konfiguration aus config-File laden.
	 */
	public static void loadConfig() throws ConfigLoadException{
	    BufferedReader input;
	    /**
	    try {
		InputStream istr=UserConfig.class.getResourceAsStream("conf"+File.separator+"messages."+lang);//Ludmila
		input = new BufferedReader(new InputStreamReader(istr));//Ludmila
	    }
	    catch(Exception e){
		if (lang=="deutsch")  // ohh, ohh, das sollte nicht passiert sein, da fehlt wohl selbst conf/message.deutsch
		    {throw new LanguageLoadException("serious error: cannot load conf/messages.deutsch - Schwerwiegender Fehler: Kann conf/messages.deutsch nicht laden!!");}
		throw new LanguageLoadException("cannot load conf/messages."+lang+" - "+UserConfig.say("UserConfig","xCanNotLoadFile","conf/messages."+lang));
	    }

	    Hashtable Sektionen = new Hashtable();  // Hierdrin liegen die Sektionen, die wiederum die Meldungen enthalten

	    try{	// syntax-Prüfung & Laden language-Datei
		String line=null;
		String sectionName="";
		int AnzahlSektionen=0;
		// messages.mark(4); // aktuelle Pos. (Anfang) markieren.
		while((line=messages.readLine())!=null)
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
				while(line.charAt(i)!=' '){    // Meldungs-ID ermitteln
				    MeldungsID=MeldungsID+line.charAt(i);
				    i++;
				}
				while(line.charAt(i)==' '){i++;} // Leerzeichen zwischen ID und Text entfernen
				MeldungsText=line.substring(i); // Der Rest ist Meldungs-Text
				Element.put(MeldungsID,MeldungsText);
			    }
	    }
	    catch(Exception e){
		throw new LanguageLoadException("error in language-file conf/messages."+lang+" - "+UserConfig.say("UserConfig","xErrorInLanguageFile","conf/messages."+lang));
	    }
	    language=lang;        // hat wohl alles geklappt, also setzen wir die Sprache
	    Sectiontable=Sektionen;  // und uebertragen die Daten in die "offizielle" Hashtable "Sectiontable"
	    */
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
	 *  z.B. System.out.println(UserConfig.say("MeineKlasse",mAnzLebenMitteilen,6));
	 *  lauten.                         ^^^
	 */
        public static String read(String Section, String id) throws ConfigLoadException{
	    if (Sectiontable==null) {loadConfig();} // File auslesen falls DB noch nicht existiert.
	    /**
	       String value="";  // Wert-String
	       Hashtable ErgebnisSektion =(Hashtable)Sectiontable.get(Section);       // Hashtable der betreffenden Sektion holen
	       if (ErgebnisSektion==null) 
	       {
	       System.err.println("section '"+Section+"' not found in language-file messages."+language+" - ");
	       System.err.println(UserConfig.say("UserConfig","xSectionNotFound",Section));
	       return("["+Section+"] not in language-file - "+UserConfig.say("UserConfig","xSectionNotFound",Section));
	       }
	       value=(String)ErgebnisSektion.get(id);                                  // value-String in der Sektion raussuchen
	       if (value==null) 
	       {
	       System.err.println("UserConfig-ID '"+id+"' not found in language-file messages."+language+" - "+Message.say("UserConfig","xKeyNotFound",id,Section));
	       return(id+" not in language-file section ["+Section+"] - "+Message.say("UserConfig","xKeyNotFound",id));
	       }
	       
	       else {strDest=strDest+strSrc.charAt(i);};  // falls kein Parameter, dann einfach Zeichen zufuegen.
	       
	    return(strDest);
	    **/
	    return("");
	} // Ende sayIt


	/**
	   Einen Wert in die Setup-Datenbank schreiben und abspeichern.
	 */
	public static void write(String Section, String id, String value) throws ConfigLoadException{
	    // in DB schreiben
	    saveConfigTable();
	    return;
	}

	/**
	   Die Einstellungen aus der Datenbank auf den Datenträger schreiben
	 */
	public static void saveConfigTable(){
	    // geht noch nicht
	    for (Enumeration enA = Sectiontable.keys(); enA.hasMoreElements() ;) {
		String elem=(String)enA.nextElement();
		// System.out.println("["+elem+"]");
		Hashtable h = (Hashtable)Sectiontable.get(elem);
		for (Enumeration enB = h.keys(); enB.hasMoreElements() ;)
		    {
			String elem2=(String)enB.nextElement();
			// System.out.print(" "+elem2+" ");
			// System.out.println(h.get(elem2));
		    }
	    }
	} // Ende saveConfigTable   
	
    } // Ende Klasse "UserConfig"
