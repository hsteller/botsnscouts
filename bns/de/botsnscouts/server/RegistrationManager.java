package de.botsnscouts.server;

import java.net.*;
import java.io.*;
import de.botsnscouts.util.Global;
import org.apache.log4j.Category;
import java.util.*;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import de.botsnscouts.util.*;
import de.botsnscouts.comm.*;



/** Erlaubt die nebenlaeufige Anmeldung von Robotern und Ausgaben
 *  Startet fuer jeden Anmeldeversuch einen ServerAnmeldeThread.
*/

class RegistrationManager implements Runnable
{
    static final Category CAT = Category.getInstance( RegistrationManager.class );
    static final String REGISTER_PATTERN = "(RGS|RGA|RS2|RA2)\\(([:alpha:]+)(,([1-8]))?\\)";

    Server server;
    ServerSocket seso;
    Set names = new HashSet();
    int anzSpieler = 0;
    Thread workingThread = new Thread( this, "RegMan" );


    public RegistrationManager(Server s)
    {
        //super("RegistrationManager");
	server=s;
    }

    public void beginRegistration() {
        workingThread.start();
    }

    public void endRegistration() {
        workingThread.interrupt();
    }

    public void run()
    {
      try {
        seso=new ServerSocket(server.anmeldePort);
        seso.setSoTimeout(0);

        Socket clientSocket;
        while(!Thread.interrupted()){
          clientSocket = seso.accept();
          if( CAT.isDebugEnabled() ) CAT.debug("new registration from " + clientSocket);
          register( clientSocket );
        }
      }catch( IOException io ) {
        CAT.debug( io );
      }
      finally {
        try { seso.close(); } catch (IOException io) {}
      }
    }

    /** Registriert Namen als benutzt - soll mit isLegalName() benutzt werden */
    private void addName(String s) throws RegistrationException {
      if( CAT.isDebugEnabled() ) {
        CAT.debug( "want to add " + s + ", right now we have:" );
        Iterator i = names.iterator();
        while( i.hasNext() )
          CAT.debug( i.next() );
      }
      if( names.contains( s ) )
        throw new RegistrationException("Name already registered");
      else
          names.add( s );
    }

    boolean isNameAvailable( String name ) {
        return ! names.contains( name );
    }


    /** Wartet server.anmeldeto auf eine Aktion, kreiert ggf. neue ServerRoboterThread-
     *  bzw ServerAusgabeThread-Objekte und haengt diese in die richtigen Vektoren ein.
     */

    private synchronized void register(Socket socket) {
        PrintWriter out=null;
        BufferedReader in=null;
	String clientName="";
	int farbe=-1;

	try{
	    CAT.debug("out = new PrintWriter    ...");
	    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true);
            CAT.debug("in  = new BufferedReader ...");
	    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            CAT.debug("... we are connected, now check if client sent s.th.");


	    socket.setSoTimeout(server.getSignUpTimeout());
	    String erhalten = in.readLine();
            if( CAT.isDebugEnabled() )
  	      CAT.debug("erhalten = "+erhalten);

	    socket.setSoTimeout(0);

            RE registerRE = new RE( REGISTER_PATTERN );

            if( !registerRE.match( erhalten ) )
              throw new RegistrationException("Register string not in the right format");

            String type = registerRE.getParen(1);
            clientName = registerRE.getParen(2);
            if( registerRE.getParenCount() >= 4 ) {
              farbe = Integer.parseInt( registerRE.getParen(4) );
            }

            if( type.equals("RGS") )
                registerPlayer(clientName, farbe, in, out, 1.0f );
            else if( type.equals("RGA") )
                registerOutput( clientName, in, out, 1.0f );
            else if( type.equals("RS2") )
                registerPlayer(clientName, farbe, in, out, 2.0f );
            else if( type.equals("RA2") )
                registerOutput( clientName, in, out, 2.0f );
            else {
                CAT.fatal("should never be here");
                throw new Error("should never be here");
            }
        } catch( Exception ee ) {
            CAT.debug( ee );
            try { in.close(); } catch( IOException e ) {}
            out.close();
        }
    }


    private void registerPlayer(String clientName, int farbe, BufferedReader in, PrintWriter out, float version )
    throws RegistrationException
    {
	    // darf ein Spieler sich anmelden?
        CAT.debug("Ein Spieler versucht sich an der Anmeldung.");
        // einfacher?: if(server.gameRunning())
        if ( server.isGameStarted() ){
            CAT.debug("Keine Roboteranmeldungen jetzt. Kille Verbindung");
            out.println("REN(SO(SpielLaeuftSchon))");
            throw new RegistrationException("No robot registrations allowed now.");
        }
        CAT.debug("Er darf jedenfalls, vom Server aus.");

        if (!isNameAvailable(clientName)){
            out.println("REN(SO(SchonVergebenerName))");
            throw new RegistrationException("Name already registered: "  + clientName);
        }
        CAT.debug("Der Name ist jedenfalls noch nicht vergeben.");
        addName( clientName );

        if (anzSpieler == server.getMaxPlayers()){
            CAT.debug("Zuviele Spieler. Kille Verbindung");
            out.println("REN(ZS)");
            throw new RegistrationException("Game is full!");
        } // Zuviele Spieler
        CAT.debug("Noch nicht zuviele Spieler.");

        farbe=server.allocateColor(farbe,clientName);
        CAT.debug("Farbe Nr. "+farbe+" zugeteilt.");

        Roboter h=Roboter.getNewInstance(clientName);
        h.setBotVis(farbe);
        KommServerRoboter komm = new KommServerRoboter(in,out);
        try{
            komm.anmeldeBestaetigung(true);
        }catch(KommException ke){
            CAT.debug("ok konnte nicht an roboter gesendet werden");
            throw new RegistrationException("Couldn't send OK to robot");
        }
        CAT.debug("ok an Spieler geschickt.");

        anzSpieler++;
        if( CAT.isDebugEnabled() )
          CAT.debug(""+anzSpieler+". Roboter mit Name "+clientName+" erzeugt.");

        ServerRoboterThread neu=new ServerRoboterThread(h,server.getOKListener(),server.getInfoRequestAnswerer(),server.getRobThreadMaintainer(),komm);
        server.addRobotThread(neu);
        CAT.debug("ServerRoboterThread erzeugt und einsortiert.");
        server.getStartServer().neuerSpieler(clientName,farbe,server);

        if (anzSpieler >= server.getMaxPlayers()){ // alle da
            try {
                Thread.sleep(5000);
            }
            catch(InterruptedException ex) {
                CAT.debug("InterruptedException "+ex);
            }
            CAT.debug("server: sending start command");
            server.startGame();
        } // if maxspieler angemeldet
    }

    private void registerOutput( String clientName, BufferedReader in, PrintWriter out, float version )
    throws RegistrationException
    {
        KommServerAusgabe ksa = new KommServerAusgabe(in, out);
        // sende 'ok' zur anmeldebestaetigung
        try{
            ksa.anmeldeBestaetigung(true);
        }catch(KommException ke){
            CAT.debug("ok konnte nicht an Ausgabekanal gesendet werden");
            throw new RegistrationException( ke );
        }

        try {
            addName("("+clientName+")");
            ServerAusgabeThread neu = new ServerAusgabeThread(ksa, server.getOKListener(), server.getMOKListener(), server.getInfoRequestAnswerer(), server.getOutputThreadMaintainer());
            neu.setVersion( version );
            server.addOutput(neu);
            CAT.debug("neuen Ausgabethread erzeugt");
        } catch( RegistrationException re ) {
            out.println("REN(SO(SchonAngemeldeterName))");
            throw re;
        }
    }

    static class RegistrationException extends Exception {
      Exception nested = null;
      RegistrationException() {};
      RegistrationException( String s ) {
          super( s );
      }
      RegistrationException( String s, Exception nested ) {
          super( s );
          this.nested = nested;
      }
      RegistrationException( Exception nested ) {
          this.nested = nested;
      }
      public String getMessage() {
          if( nested != null )
            return super.getMessage() + "[" + nested.getMessage() + "]";
          else
            return super.getMessage();
      }
    }
}

