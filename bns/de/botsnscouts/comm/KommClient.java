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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

import org.apache.log4j.Category;

import de.botsnscouts.server.Deck;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Card;
import de.botsnscouts.util.Directions;
import de.botsnscouts.util.Encoder;
import de.botsnscouts.util.Global;
import de.botsnscouts.util.Location;
import de.botsnscouts.util.Message;
import de.botsnscouts.util.ShutdownListener;
import de.botsnscouts.util.Shutdownable;
import de.botsnscouts.util.ShutdownableSupport;
import de.botsnscouts.util.Status;



/** The parent class for all clientside communication.
    Very 'advanced' parsing of Strings.
 *@author Hendrik
*/

public class KommClient implements Shutdownable{
    static Category CAT = Category.getInstance(KommClient.class);
    static final boolean LOG_RECEIVE = true;
    static final boolean LOG_SEND    = true;

    private Socket socket;
    
    /** The client reads  messages of the server using this BufferedReader
     */
    protected BufferedReader in;
    /** The client sends messages to the server using this PrintWriter
     */
    protected PrintWriter out;
    /** Needed for a maybe deprecated hotfix.
	This is a flag that is set if the server sent an 'NTC' (notify change)
	at an unexpected time (while waiting for a request's response).
    */
    protected boolean gotNTC;
    /**
	Needed for a maybe deprecated hotfix.
	Saves the NTC-String sent at a wrong time.
	Together with 'gotNTC' it represensts a kind of an one-element-queue ;-)
    */
    protected String strNTC;

    /**
     * Used for communication with the server.
     *  The (URL-)encoded name of the client; will be used in methods instead of names
     *  that are given as parameters.
     *
     */
    protected String encodedName = "";

    /** The name of the client that owns this KommClient object.
        For debugging purposes.
    */
    protected String cn;

    /** For debugging purposes: if set to true,
	a logfile "<clientname>'s.Kommlog" will be created,
	saving all Strings the client sent or got.
	Can be changed in source code only => For activating/deactivating you'll have to
	recompile this class.
    */
    protected final boolean log=false;

    
    /** To implement the Shutdownable interface; will be set to true on the end of the shutdown() method */
    private boolean isShutDown = false; 
    
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
                CAT.error(i.getMessage(), i);
		CAT.error ("Constructor: error creating logwriter");
	    }
	}
    }

    /** This method sents a String to the server.
	@param s The String to be sent.
    */
    protected  void senden (String s) throws KommFutschException {
	PrintWriter debug=null;
	if (log) {

	    try {
		debug = new PrintWriter(new BufferedWriter(new FileWriter((cn+"'s.Kommlog"), true)));
	    }
	    catch (IOException ioe) {
                CAT.error(ioe.getMessage(), ioe);
		CAT.error("sending: IOException creating logwriter");
	    }
	    if (debug!=null) {
		debug.println ("sending: "+s);
		debug.close();
	    }
	}
	try {
	    if( LOG_SEND )
              Global.debug(this, "CLIENT "+cn+" sends: "+s);
	    out.println (s);
	}
	catch (NullPointerException npe) {
              CAT.error(npe.getMessage(), npe);
	    throw new KommFutschException ("NullPointerException while client is sending");
	}
	catch (Exception e) {
            CAT.error(e.getMessage(), e);
	    throw new KommFutschException ("Exception sending: Exception-Message: "+e.getMessage());
	}

    }

   class TmpParsedSeqNumMessage {
      int messageNum;
      String originalMessage;
    }

    private TmpParsedSeqNumMessage splitPossibleSeqNumAndMessage(String com) throws KommException{
        TmpParsedSeqNumMessage back = new TmpParsedSeqNumMessage();
       if (com.startsWith(OtherConstants.MESSAGE_NUMBER)){
          int kommaPos = com.indexOf(','); // end of sequence-number-part
          String seqString = com.substring(0, kommaPos); // contains the a String like "message_number=1234"
          int splitAt = seqString.indexOf('=');
          String onlyNumber = seqString.substring(splitAt+1);
          try {
            back.messageNum =Integer.parseInt(onlyNumber);
          }
          catch (NumberFormatException nfe){
            CAT.error(nfe.getMessage(), nfe);
            throw new KommException("failed to parse message sequence number");
          }

          back.originalMessage = com.substring(kommaPos+1);
        }
        else {
            back.originalMessage = com;
            back.messageNum = -1; // important to indicate that there was no sequence number
                                  // in front of this message
        }
        return back;

    }

    /**
	This method reads messages sent by the server, if a special request has been sent
	(i.e. used in all non-void get-Methods).
	It is to be used instead of 'warte' (or 'wait2').
	Unexpected sent 'REN' or 'NTC' will be treated here.
	If 'REN' (Client removed by server)  was read, a KommFutschException will be thrown;
	if 'NTC' was read, gotNTC will be set to true, the NTC-String will be saved and the next
	incoming String will be returned.
    */
    protected String einlesen() throws KommException {
	PrintWriter debug=null;
	if (log) {

	    try {
		debug = new PrintWriter(new BufferedWriter(new FileWriter((cn+"'s.Kommlog"), true)));
	    }
	    catch (IOException ioe) {
		CAT.error("einlesen: IOException creating logwriter");
	    }
	}
	String back="";
	try {
	    back = in.readLine();
	    if( LOG_RECEIVE )
              Global.debug(this, "CLIENT(einlesen) "+cn+" receives: "+back);
	    if ((debug!=null)&&(log))
		debug.println("einlesen() received: "+back);
	}
	catch (IOException ioe) {
	    throw new KommFutschException ("Einlesen: IOException occurred; Message: "+ioe.getMessage());
	}
	//
	if (back==null) {
	    try {// if null, try again
		back = in.readLine();
		if ((debug!=null)&&(log))
		    Global.debug(this,"einlesen (2nd try) received: "+back);

		if (back==null)// read null the second time => no connection anymore => exception
		    throw new KommFutschException("Einlesen: read 'null' two times");

	    }
	    catch (IOException ioe2) {
		throw new KommFutschException ("Einlesen: IOException while retrying to read; Message: "+ioe2.getMessage());
	    }
	}
	// Tried two times to read the String


        // removing possible sequence number part from back for testing for REN/NTC
        TmpParsedSeqNumMessage tmp  = splitPossibleSeqNumAndMessage(back);
        String test = tmp.originalMessage;

	// Now checking whether the server removed the client
	if ((test.length()>=3)&&(test.substring(0,3).equals("REN"))) {
	    ClientAntwort xyz= new ClientAntwort();
	    try {
		xyz=wait2(back);
	    }
	    catch (Exception e) {
		throw new KommException ("einlesen: REN erhalten, aber der String \""+back+"\" verursachte in wait2 eine Exception;\n Message:"+e.getMessage());
	    }
	    if (xyz.typ==ClientAntwort.ENTFERNUNG)
		throw new KommFutschException ("Client was removed;\n Reason: "+xyz.str);
	}
	// Hey, we made nothing wrong. The client is still alive :-)

	// Now checking whether we got a bad timed NTC (notify change)
	else if ((test.length()>=3)&&(test.substring(0,3).equals("NTC"))) {

	    gotNTC=true; // changing state; indicating that there is an NTC waiting
	    //strNTC=new String(back); // saving the message of the server
            strNTC=back; // don't save: strings are immutable

	    // now we will read the answer we are waiting for
	    back = einlesen();
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
	return back; // statement shouldn't be reached, but you (I) never know.

    }


    /** The main method of this class. It waits for messages sent from the server.
	@exception KommException Any error parsing a server-message (including most
	exceptions) will end up in throwing a KommException
    */
    public ClientAntwort warte () throws KommException {
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
	if (!gotNTC) {// normal case; server did nothing unexpected
	    if (in == null)
              throw new KommFutschException("Connection to server failed!");

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
	else {// we have an old NTC waiting for being processed

	    gotNTC=false;
	    if ((debug!=null)&&(log)) {
		debug.println("baearbeite altes NTC: "+strNTC);
		debug.close();
	    }
	    return wait2(strNTC);
	}
    }

    /** This method does all the String parsing.
     */
    ClientAntwort wait2 (String com) throws KommException {
	Global.debug(this,"CLIENT: warte erhielt: "+com);
        ClientAntwort back=new ClientAntwort();
	boolean error=false;
	String errormesg="";
        // <hack for synchronizing display stuff>
        // checking whether there is a sequence number in front of the message
        TmpParsedSeqNumMessage foo = splitPossibleSeqNumAndMessage(com);
        com = foo.originalMessage;
        back.messageSequenceNumber = foo.messageNum; // will be set to -1 if there
                                                     // was no sequence number


        // </hack for synchronizing display stuff>

	if ((com.equals("ok")) || (com.equals("OK"))){
	    back.typ=ClientAntwort.ANGEMELDET;
	    back.ok = true;
	}
	else if ((com.equals("error")) || (com.equals("ERROR"))){
	    back.typ=ClientAntwort.ANGEMELDET;
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
		    if (nd=='O') { // Timeout
			try {
			    int klammerauf=com.indexOf('(');
			    int klammerzu= com.indexOf (')');
			    if ((klammerzu==-1)||(klammerauf==-1)){
				error=true;
				errormesg="TO -> dann folgte nicht (  )";
			    }
			    try {
				back.typ=ClientAntwort.TIMEOUT;
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
		    if ((nd=='S')&&(rd=='G')) { // some generic info message
			back.typ=ClientAntwort.MESSAGE;
			int k1 = com.indexOf('(');
			com = com.substring (k1+1,com.length()-1);
			StringTokenizer sto= new StringTokenizer(com,",)");
			String [] tmp = new String [sto.countTokens()];
			int i=0;
			while (sto.hasMoreTokens()){
			    tmp[i]= Encoder.commDecode(sto.nextToken());
			    i++;
			}
                        int lastElem = tmp.length-1;
                        String seq = tmp[lastElem];
                        if (seq.startsWith(OtherConstants.MESSAGE_NUMBER)) {
                          int pos=seq.indexOf('=');
                          try {
                            back.messageSequenceNumber = Integer.parseInt(seq.substring(pos+1));
                            back.namen = new String [lastElem]; // namen.length==tmp.length-1
                            for (int j=0;j<lastElem;j++){
                              back.namen[j] = tmp[j]; // copy everything but the sequenznumber
                            }
                          }
                          catch (NumberFormatException nfe){
                            CAT.error(nfe.getMessage(), nfe);
                          }
                        }
                        else // no sequenznummber, use everything
                          back.namen = tmp;
                        if (CAT.isDebugEnabled()) {
                           CAT.debug("MSG: "+back.messageSequenceNumber);
                          for (int k=0;k<back.namen.length;k++){
                            CAT.debug("arg"+k+"="+back.namen[k]);

                          }
                        }
			break;
		    }
		    else if (nd=='R'){
			if (rd=='P') { // got cards 
			    try {
				try {
				    Card [] karten1 = new Card [9]; // max. number of cards
				    int klammerauf=com.indexOf('(');
				    int klammerletzt=com.lastIndexOf(')');
				    com=com.substring (klammerauf+1);
				    int count=0;

				    while (com.length()>1) {
					int klauf=com.indexOf('(');
					int klzu=com.indexOf(')');
					String work= com.substring(klauf+1,klzu); // first card, without parens, i.e. "M1,123"
					int komma=work.indexOf(',');
					String art=work.substring(0,komma);
					String pri=work.substring(komma+1);
					try {
					    int prio=Integer.parseInt(pri);
					    karten1 [count]= Deck.get(prio);
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
				    // now there should be 'count' cards in karten1
				    back.typ=ClientAntwort.MACHEZUG;
				    back.karten=new Card [count];

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
			    break;
			}
			else if (rd=='R') { // may repair a locked register
			    try {
				int klauf=com.indexOf('(');
				int klzu=com.indexOf(')');
				String zahl=com.substring(klauf+1,klzu);
				try {
				    back.typ=ClientAntwort.REPARATUR;
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
			if (rd=='R') { // robbot destroyed, have to send a new direction for reentering 
			    back.typ=ClientAntwort.ZERSTOERUNG;
			    break;
			}
			else {
			    error=true;
			    errormesg="MN-> danach kein R";
			    break;
			}
		    }
		    else if (nd=='B') {
			if (rd=='D') { // power up  again?
			    back.typ=ClientAntwort.REAKTIVIERUNG;
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
			if (rd=='S'){ // game starts
			    back.typ=ClientAntwort.SPIELSTART;
			    back.ok=true;

			}
			else if (rd=='C'){ // something has changed for one or more robots
			    try {
				back.typ=ClientAntwort.AENDERUNG;
				int klauf=com.indexOf('(');
                                // getNamen2 does the URL-decoding of the names for us..
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
		    if (nd=='S') { // current standings
			try {
			    back.typ=ClientAntwort.SPIELSTAND;
			    if (com.length()==7){
				back.namen=null;
				back.ok=true;
			    }
			    else {
				back.ok=false;
				int komma1=com.indexOf(',');
				com=com.substring(komma1+1);
				com="("+com; // now the String has GSN-formatting
				// getNamen2 does the URL-decoding of the names for us..
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
		    else if (nd=='A') { // game status, which phase are we in
			try {
			    back.typ=ClientAntwort.SPIELSTATUS;
			    if (com.length()<=4)
				back.ok=false; // => no phase processing
			    else {
				back.ok=true; // => phase processing
				int klauf=com.indexOf('('); // no we are behind 'SA'
				int phase=0; // init
				try {
				    phase=Integer.parseInt(com.substring (klauf+1,klauf+2));// parsing phase-number
				}
				catch (NumberFormatException nfe) {
				    throw new KommException ("NumberFormatException bei getSpielstatus (Parsen der Registerphase); Message: "+nfe.getMessage());
				}
				int komma = com.indexOf(',');
				com=com.substring(komma+1); // now there are only names left
				Status [] stats = new Status [8]; // 8=maximum number of players
				int bloed = klazu2(com);
				int zaehler=0;
				while (bloed!=-1){
				    String erster = splitFirstRob(com);
                                    // getStatusRegs does URL-decoding of the names for us..
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
		    if ((nd=='E') && (rd=='N')) { // REN == robot has been removed
			try {
			    back.typ=ClientAntwort.ENTFERNUNG;
			    int klauf = com.indexOf('(');
			    int klazu = com.lastIndexOf(')');
			    String work = com.substring(klauf+1,klazu);
		
			    if (work.length()<=2) {
			        if (work.equals("LL"))  {// lost all lives
			            back.str=Message.say("comm", "removalReasonLL");
			            back.zahl = ClientAntwort.REMOVAL_REASON_LOSTLIVES;
			        }
			        else if (work.equals("TO")) {
			            back.str=Message.say("comm", "removalReasonTO"); // timeout
			            back.zahl = ClientAntwort.REMOVAL_REASON_TIMEOUT;
			        }
			        else if (work.equals("GO")) {
			            back.str=Message.say("comm", "removalReasonGO"); // game is over
			            back.zahl = ClientAntwort.REMOVAL_REASON_GAMEOVER;
			        }
			        else if (work.equals("RV")) {
			            back.str=Message.say("comm", "removalReasonRV"); // rule violation
			            back.zahl = ClientAntwort.REMOVAL_REASON_RULEVIOLATION;
			        }
			        else if (work.equals("ZS")) {
			            back.str=Message.say("comm", "removalReasonZS"); // to late registered
			            back.zahl = ClientAntwort.REMOVAL_REASON_TOOLATE;
			        }
			        else {
			            back.str=Message.say("comm", "removalReasonOther", work); // unknown reason
			            back.zahl = ClientAntwort.REMOVAL_REASON_OTHER;
			        }
			    }
			    else {
			        //klauf = work.indexOf ('(');
			        // work = work.substring (klauf+1,work.length()-1);
			        back.str=work;
			    }
			}
			catch (Exception e) {
			    errormesg="Bei Entfernung trat eine Exception auf";
			    System.err.println("KommClient: "+errormesg);
			    error=true;
			    break;

			}

			//break;
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
    /** This method is used to tell the server that the client wants to quit
	@param name The name of the client UPDATE: WILL BE IGNORED

    */
    public void abmelden (String name) {

	String back="RLE("+encodedName+")";

	if(out!=null) out.println (back);
    }



    /** This method registers a client.
	Never used directly. Use child's method instead.
        DOES NOT ENCODE THE CLIENT NAME;
        ENCODING MUST HAPPEN IN THE DERIVED CLASSES ("anmelden*"-methods)
	@param ipnr IP of the server
	@param portnr Ip-port of the server
	@param clientName The name of the client
	@param kuerzel Indicates, whether the client is a robot or an output channel
	@exception  KommException Thrown, if parsing failed
    */
    protected boolean anmelden (String ipnr, int portnr,String clientName, String kuerzel)throws KommException{

        StringTokenizer st = new StringTokenizer(clientName, ",");

        encodedName = st.nextToken();//clientName; // now encoded in derived classes, only to lazy
                                  // to remove the variable again
        cn = encodedName;
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

	    throw new KommException(Message.say("KommClient", "nohost"));
	}
	catch (java.io.IOException fehler) {
	    throw new KommException (Message.say("KommClient","IOException"));
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

    /** Info-Request, asking for the positions of all flags.
	@return An array of 'Orts' (equal to Java's 'Point').
	They are ordered according to the flags' numbers.
	@exception KommException ..if an error occurs
    */
    public Location [] getFahnenPos ()throws KommException {
	// Gets a String like  "(x,y)(a,b)(c,d)"
	// out.println ("GFL");
	this.senden("GFL");
	String rein="";
	rein = this.einlesen();
	// Calculating number of flags by scanning for ","
	int pos=0;
	int zaehler=0;
	int start=0;
	while (pos!=-1) { // in dieser Schleife sollte keine Exception auftreten
	    pos = rein.indexOf(',',start);
	    if (pos!=-1)
		zaehler++;
	    start=pos+1;
	}// postcondition: zaehler == number of flags (Orts)
	Location [] back;
	rein+="**"; // dummy to avoid Exceptions
	try {
	    back = new Location [zaehler];
	    for (int myI=0;myI<zaehler;myI++){
		int klammerzu = rein.indexOf(')');
		int kommapos = rein.indexOf(',');
		String x=rein.substring(1,kommapos); // String containing  x-coordinate
		String y=rein.substring(kommapos+1,klammerzu); // String containing y-coordinate
		int xk=Integer.parseInt(x); //  x-coordinate
		int yk=Integer.parseInt(y); //  y-coordinate
		back [myI]=new Location(xk, yk);
		rein=rein.substring(klammerzu+1);// remove parsed Point (Location)
	    }
	}
	// A lot of errors might happen.. :
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


    /** This method asks the server for the position of the robot named 'name'
	@param name The robot's name
	@return The robot's coordinates
	@exception KommException ..if an error occurs
    */
/*   public Location getRobPos (String name) throws KommException {
	return this.fetchOrt(name, true);
    }
*/
    /** This method asks the server for the size of the board.
	@return An Location containing the bords Dimensions.
	@exception KommException ..if an error occurs
    */

    public Location getSpielfeldDim () throws KommException{
	return this.fetchOrt("wirdIgnoriert", false);
    }

    /** This method asks for the whole board
	@exception KommException ..if an error occurs
    */
    public String getSpielfeld() throws KommException{
	String back="";
	try {
	    this.senden ("GPL");
	    String rein = this.einlesen();
	    back=rein;
	    if (rein.lastIndexOf('.')==-1) // the end of the boards description reached (ending with a dot)?!
		back+="\n"; // no => new line
	    while (rein.lastIndexOf('.')==-1){
		rein=this.einlesen();
		back+=rein; // concat parts
		if (rein.lastIndexOf('.')==-1) // make sure that no line break is following the dot
		    back+="\n";
	    }


	}
	catch (Exception sonstige) {
	    throw new KommException ("Exception bei getBoard; Message: "+sonstige.getMessage());
	}
	return back;
    }


    /** This method asks for the robot's names
	@return The robot's names
	@exception KommException ..if an error occours
    */
    public String [] getNamen () throws KommException{
	this.senden("GSN");
	String rein = this.einlesen();
	return this.getNamen2(rein);

    }

    /** Supporting method for parsing a list of names..
	.. or other Strings formatted the same way: "(String1,String2,..,StringN)"
    */
    private String [] getNamen2(String rein) throws KommException {
	String []raus=null;
	try {
	    // Count the number of names (list parts) by counting ','
	    int pos=0;
	    int zaehler=0;
	    int start=0;
	    while (pos!=-1) {
		pos = rein.indexOf(',',start);
		if (pos!=-1)
		    zaehler++;
		start=pos+1;
	    }// now : zaehler==number of names == number of ','


	    raus = new String [zaehler];
	    rein = rein.substring (1); // remove '('
	    for (int i=0;i<zaehler;i++) {
		int kommapos = rein.indexOf(',');
		raus [i] = Encoder.commDecode(rein.substring(0,kommapos));
		rein = rein.substring(kommapos+1); // remove read names and ','
	    }


        }
	catch (Exception e) {
	    throw new KommException ("Exception at getNamen2;Message: "+e.getMessage());
	}

	return raus;
    }

    /** Zur Frage nach den Farben der Spieler.
	Ein Array-Element ist entweder null oder einer der Namen.
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
    /**Info-Request zur Abfrage des Status eines Roboters; gibt ein Bot-objekt zur&uuml;ck, dass alle notwendigen Informationen enth&auml;lt.
       Falls der Bot entfernt wurde, wird Bot.leben auf -1 gesetzt, die restlichen Attribute werden nicht gesetzt (d.h = null oder was_auch_immer).
       @exception KommException Tritt beim Parsen ein Fehler auf (z.B. wegen falsch aufgebauten Strings), wird eine KommException geworfen.
    */
    public Bot getRobStatus (String name) throws KommException {
        String com ="";
        Bot robot=Bot.getNewInstance(name);
        String raus ="GRS("+Encoder.commEncode(name)+")";
        this.senden(raus);
        //Server sends "RS(Richtung(N,O..), Location(1,1), LFlag, LArchF, Schaden, VLeben, GespRegister, Aktiv, Virtuell, RSreserveiert)"
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
	    robot.setLives(-1);
	}
	else{
	    char richtung =com.charAt(3); // Richtung auslesen
	    if (richtung=='N')
	        robot.setFacing(Directions.NORTH);
	    else if (richtung=='S')
	        robot.setFacing(Directions.SOUTH);
	    else if ((richtung=='E')||(richtung=='O'))
	        robot.setFacing(Directions.EAST);
	    else if (richtung=='W')
	        robot.setFacing(Directions.WEST);
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
		throw new KommException ("getRobStatus: (Location-Parsen) NumberFormatException(Message: "+nfe.getMessage());
	    }
	    com=com.substring(klazupos+2); //(x,y), abschneiden
	    kommapos=com.indexOf(',');
	    String flagge = com.substring(0,kommapos);
	    try {
		robot.setNextFlag(Integer.parseInt(flagge)+1);// +1, weil unser Bot die naechste und nicht die letzte Flagge haben will
	    }
	    catch (NumberFormatException nfe2) {
		throw new KommException ("getRobStatus: (L-Flag-Parsen) NumberFormatException(Message: "+nfe2.getMessage());
	    }
	    com=com.substring(kommapos+1); // LFlag und Komma entfernen

	    kommapos=com.indexOf(',');
	    klazupos=com.indexOf(')');
	    xk=com.substring(1,kommapos); // Archiv - Location
	    yk=com.substring(kommapos+1,klazupos);
	    try {
		robot.setArchive(Integer.parseInt(xk),Integer.parseInt(yk));
	    }
	    catch (NumberFormatException nfe3) {
		throw new KommException ("getRobStatus: NumberFormatException (Archiv-Parsen)(Message: "+nfe3.getMessage());
	    }
	    com=com.substring(klazupos+2); // "(x,y)," entfernen

	    kommapos=com.indexOf(',');
	    String schaden = com.substring(0,kommapos);
	    try {
		robot.setDamage(Integer.parseInt(schaden));
	    }
	    catch (NumberFormatException nfe4) {
		throw new KommException ("getRobStatus: NumberFormatException (Schaden-Parsen)(Message: "+nfe4.getMessage());
	    }

	    com =com.substring(kommapos+1);// schaden und Komma entfernen


	    kommapos=com.indexOf(',');
	    String vLeben = com.substring(0,kommapos);
	    try {
		robot.setLives(3-Integer.parseInt(vLeben));
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
		robot.setActivated(true);
	    else
		robot.setActivated(false);
	    if ((virtuell=='t')||(virtuell=='T'))
		robot.setVirtual(true);
	    else
		robot.setVirtual(false);


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

	if (xyz.typ==ClientAntwort.SPIELSTAND) {
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
	Sie gibt f&uuml;r jeden Bot ein Statusobjekt zur&uuml;ck, das dessen Namen, seine bisher ausgewerteten Registerinhalte und die aktuelle Auswertungsphase als Attribute besitzt.
	Falls gerade nicht ausgewertet wird, wird null zur�ckgegeben.
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
	if (xyz.typ==ClientAntwort.SPIELSTATUS){
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
    
    public boolean getIsScoutAllowed() throws KommException{
        this.senden("ISS");
        String value = this.einlesen();
        if (value != null){
            if (value.equals("true"))
                return true;
            else if (value.equals("false"))
                return false;     
        }
        throw new KommException("Question isScoutAllowed got unknown answer: "+value);
    }
    public boolean getIsWisenheimerAllowed() throws KommException{
        this.senden("ISW");
        String value = this.einlesen();
        if (value != null){
            if (value.equals("true"))
                return true;
            else if (value.equals("false"))
                return false;     
        }
        throw new KommException("Question isWisenheimerAllowed got unknown answer: "+value);
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
	if (xyz.typ==ClientAntwort.TIMEOUT)
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

    // HELPER METHODS

    // bearbeitet Anfragen nach einem Location

    /**Gets either the position of the robot (if RobPos==true)
     * or the dimensionj of the board (if RobPos== false)
     *
     * @param name WILL BE IGNORED */
    private  Location fetchOrt (String name, boolean RobPos) throws KommException{
	Location back= new Location (-1,-1); // Initialisierung
	String method="noch fetchOrt";;
	try {
	    if (RobPos) { // getRobPos ?!
		method="getRobPos";
		this.senden ("SRO("+encodedName+")");

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
	//catch (IOException ioe) {
	//      throw new KommException (method+" warf eine IOException; Message: "+ioe.getMessage());
	//      }
	catch (Exception sonstige) {
	    throw new KommException (method+" warf eine Exception; Message: "+sonstige.getMessage());
	}
	return back;
    }

    /**Diese Methode parst den Programmkartenteil bei getRobStatus
     * (URL-)decodes the names
     */
    public static Status getStatusRegs(String in)throws KommException {
	Status back=new Status();
	// zum Parsen der Register bei getRobStatus
	// in soll in etwa so aussehen: (name,PK(M1,123)PK(M2,456))
	// oder Spezialfall: (name,)
	int kommaName=in.indexOf(',');
	back.robName= Encoder.decode(new String (in.substring(1,kommaName)));
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
	back.register= new Card [ks];
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

		    back.register[i]= Deck.get(p);
		    i++;
		    in = in.substring(klazu+1);
		    // System.out.println ("Beende While-Loop mit: "+in);
		}
	    }
	}
	return back;
    }

    private static Card [] getRegister (String str)throws KommException {
	Card [] back = new Card [5];
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
		back [register-1]=Deck.get(prioritaet); // Card eingeteilt
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
	Liefert den String zurueck, der den ersten Bot betrifft:
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
    
    public void shutdown(boolean notifyListeners) {
        if (socket != null) {
            try {
                socket.close();
            }
            catch (Exception ioe){
                CAT.debug(ioe);
            }
        }
        if (in != null) {
            try {
                in.close();
            }
            catch (Exception ioe){
                CAT.debug(ioe);
            }
        }
        if (out != null) {
            try {
                out.close();
            }
            catch (Exception ioe){
                CAT.debug(ioe);
            }
        }
        isShutDown = true;
        if (notifyListeners) {
            shutdownSupport.shutdown();
        }
        
    }
    
    public boolean isShutDown() {
        return isShutDown();
    }
    
    /** Finalizer closes the streams.
     */
    protected void finalize() throws Throwable {
      super.finalize();
	  shutdown(true);
    }
    
    
    private ShutdownableSupport shutdownSupport = new  ShutdownableSupport(this);
    
    public void addShutdownListener(ShutdownListener l){
        shutdownSupport.addShutdownListener(l);
    }  
    
    public boolean removeShutdownListener(ShutdownListener l){ 
        return shutdownSupport.removeShutdownListener(l);
    }
}












