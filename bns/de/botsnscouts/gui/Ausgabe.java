package de.botsnscouts.gui;

import de.botsnscouts.comm.*;
import de.botsnscouts.util.*;
import de.botsnscouts.board.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import org.apache.log4j.Category;

/**
 * view logic
 * @author Lukasz Pekacki
 */
public class Ausgabe extends Thread {
    static Category CAT = Category.getInstance(Ausgabe.class);

   // private HumanPlayer human;
    private KommClientAusgabe kommClient;
    private ClientAntwort kommAntwort = new ClientAntwort();
    private AusgabeView ausgabeView;
    private Splash splashScreen;
    private Hashtable robots = new Hashtable(8);
    private View view;

    // ---------- class variables ------------

    // game constants
    private Dimension boardDimension;
    private Ort[] flags;

    private String host, name;
    private int port;
    private boolean spielEnde = false;
    private boolean nosplash = false;
    private boolean registered = false;
    /** the last phase of the current turn */
    private int lastPhase=1;

    public Ausgabe() {
	this("localhost",8077,false);
    }

    public Ausgabe(String host, int port, Object o, boolean nosplash) {
	this (host,port,nosplash);
    }

    public Ausgabe(String host, int port, boolean nosplash,/* HumanPlayer human,*/ View v) {
	this(host, port, nosplash);
	//this.human = human;
	this.view =v;
    }

    public Ausgabe(String host, int port, boolean nosplash) {
	this.nosplash=nosplash;
	this.host = host;
	this.port = port;
	name = KrimsKrams.randomName();

	showSplash(Message.say("AusgabeFrame","msplashWarte"));
	kommClient = new KommClientAusgabe();
    }


    public void run() {

	if (! registered) {initialize();}

	// ---- entering game  ---------

	while (!spielEnde) {
	    try {
		kommAntwort = kommClient.warte();
	    }
	    catch (KommFutschException kE) {
		System.err.println("KE: "+kE.getMessage());
		return;
	    }
	    catch (KommException ke) {
		System.err.println("ke: "+ke.getMessage());
		return;
	    }
	    // what did the server send?
	    switch (kommAntwort.typ) {
	    case (ClientAntwort.MESSAGE):{
		Global.debug(this,"Server send me: some messsage.");
		String[] tmpstr=new String[kommAntwort.namen.length-1];

		for (int i=0;i<tmpstr.length;i++)
		    tmpstr[i]=kommAntwort.namen[i+1];

		if (!(kommAntwort.namen[0].substring(0,5).equals("mAusw"))) {
		    d("kommAntowrtnamen[0] ist: "+kommAntwort.namen[0]+ "tmpstr ist: "+tmpstr[0]);
		    showActionMessage(Message.say("MSG",kommAntwort.namen[0],tmpstr));
		}

		if (kommAntwort.namen[0].equals("mRobLaser")){
		    Roboter r1=null;
		    Roboter r2=null;
		    try {
			r1=kommClient.getRobStatus(kommAntwort.namen[1]);// schiessender Roboter
		    }
		    catch (KommException k) {
			k.printStackTrace();
		    }
		    try {
			r2=kommClient.getRobStatus(kommAntwort.namen[2]);// getroffener Roboter
		    }
		    catch (KommException k) {
			k.printStackTrace();
		    }
		    ausgabeView.showRobLaser(r1, r2);
		}
		else if (kommAntwort.namen[0].equals("mBoardLaser")){
		    Roboter r1=null;
		    Ort r1Pos = null;
		    // get damaged Roboter
		    try {
			r1=kommClient.getRobStatus(kommAntwort.namen[1]);
			r1Pos= new Ort (r1.getX(), r1.getY());
		    }
		    catch (KommException k) {
			k.printStackTrace();
		    }
		    // get the Laser-Position
		    Ort laserPos = new Ort(0,0);
		    int facing=-1;
		    int strength=-1;
		    try {
	 		strength   = Integer.parseInt(kommAntwort.namen[2]);
			laserPos.x = Integer.parseInt(kommAntwort.namen[3]);
			laserPos.y = Integer.parseInt(kommAntwort.namen[4]);
			facing     = Integer.parseInt(kommAntwort.namen[5]);
       		    }
		    catch (NumberFormatException nfe) {
			System.err.println("Ausgabe: BoardLaser: NumberFormatException:");
			nfe.printStackTrace();
		    }
		    if ((laserPos!=null)&&(facing>=0)&&(r1Pos!=null)&&(strength>=0))
			ausgabeView.showBoardLaser(laserPos, facing, strength, r1Pos);
		    else {
			System.err.println("Ausgabe: unable to calculate Laseranimation: ");
			System.err.println("laserPos: "+laserPos);
			System.err.println("facing: "+facing);
			System.err.println("r1Pos: "+r1Pos);
			System.err.println("strength: "+strength);
		    }
		}

		try{
		    kommClient.acknowledgeMsg();
		} catch (KommFutschException ke) {
		    System.err.println("ke2: "+ke.getMessage());
		    return;
		}
		catch (KommException kE) {
		    System.err.println(kE.getMessage());
		    return;
		}

		break;
	    }

	    case (ClientAntwort.AENDERUNG): {
		Global.debug(this,"Server send me: change occured.");

		// ------- get changes  -----------
		Global.debug(this,kommAntwort.namen.length+" robs have been updated.");
		try {
		    String[] playerNames = kommAntwort.namen;
		    for (int i=0; i < playerNames.length; i++) {
			robots.put(playerNames[i],kommClient.getRobStatus(playerNames[i]));
		    }

		    ausgabeView.showUpdatedRobots(getRoboterArray());

		    // --------- Neue Roboter-Position an Spielfeld senden ---------
		    try {
			Thread.sleep(100);
		    } // Verzögerung der Ausgabegeschwindigkeit
		    catch (Exception e) {
			System.err.println(e.getMessage());
		    }

		    // --------- get other information from the server
		    Status[] stArray = kommClient.getSpielstatus();
		    if (stArray != null) {
			if (stArray[0].aktPhase != lastPhase) {
			    showActionMessage(Message.say("AusgabeFrame","phase")+stArray[0].aktPhase);
			    lastPhase = stArray[0].aktPhase;
			}
		    }
		    // --------- has somebody already reached the final flag?
		    String[] winnerStateList = kommClient.getSpielstand();
		    ausgabeView.showWinnerState(winnerStateList);
		}
		catch (KommFutschException ke) {
		    System.err.println("ke2: "+ke.getMessage());
		    return;
		}
		catch (KommException kE) {
		    System.err.println(kE.getMessage());
		    return;
		}

		kommClient.aenderungFertig();
		break;
	    }

	    case (ClientAntwort.ENTFERNUNG): {
		Global.debug(this,"the game is over");

		try {
		    String[] spielErgebnis = kommClient.getSpielstand();
		    if (spielErgebnis != null) {
			Global.debug(this,"We have "+spielErgebnis.length+" winners");
			ausgabeView.showWinnerlist(spielErgebnis);
		    }
		    else Global.debug(this,"No winner exists");
		}
		catch (KommException e) {
		    System.err.println(e.getMessage());
		    return;
		}
		try {
		    Thread.sleep(2000);
		}
		catch (InterruptedException e) {
		    System.err.println("Ausgabe: Interrupted by "+e.toString());
		}
		kommClient.spielstart();
		spielEnde = true;
	    }
	    }

	}
	Global.debug(this,"I reached the end of my run() method");
	showActionMessage(Message.say("AusgabeFrame","spielende"));
	return;
    }


    protected void initialize() {
	registerAtServer();
	try {
	    kommAntwort = kommClient.warte();
	}
	catch (KommFutschException kE) {
	    System.err.println("KE: "+kE.getMessage());
	    return;
	}
	catch (KommException ke) {
	    System.err.println("ke: "+ke.getMessage());
	    return;
	}

	if (kommAntwort.typ == kommAntwort.SPIELSTART) {
	    Global.debug(this,"Server send me: game start.");

	    // ------- fetching the board -----
	    try {
		String[] playerNames = kommClient.getNamen();
		String[] playerColors = kommClient.getFarben();

		Hashtable playerColorHash = new Hashtable(playerColors.length);
		for (int i = 0; i < playerColors.length; i++) {
		    if (! playerColors[i].equals("0")) {
			playerColorHash.put(playerColors[i],new Integer(i));
		    }
		}
                BotVis.initBotVis( playerColorHash );

		Ort boardDim = kommClient.getSpielfeldDim();
		boardDimension = new Dimension(boardDim.x,boardDim.y);
		flags = kommClient.getFahnenPos();
		// !!HACK!!

		Color[] robotsDefaultColor = SACanvas.robocolor;
		Color[] robotsNewColor = new Color[8];

		int[] colorMap = new int[8];

		int j=0;
		for (int i=0;i<8;i++) {
		    if(!playerColors[i].equals("0")) {
			robotsNewColor[j]=robotsDefaultColor[i];
			colorMap[j]=i;
			j++;
		    }
		}

		// !! END OF HACK !!

		for (int i=0; i < playerNames.length; i++) {
		    d("Hole Roboterstatus von: "+playerNames[i]);
		    Roboter tempRob = kommClient.getRobStatus(playerNames[i]);
		    tempRob.setBotVis(((Integer)playerColorHash.get(tempRob.getName())).intValue());
		    robots.put(playerNames[i], tempRob);
		}


		SpielfeldSim sim = new SpielfeldSim(boardDimension.width,
						    boardDimension.height,
						    kommClient.getSpielfeld(),
						    flags);


		ausgabeView = new AusgabeView(new SACanvas(sim,robotsNewColor),getRoboterArray(),this);

		if (view == null) {
		    view=new View(ausgabeView);
		}
		else {
                    view.addAusgabeView(ausgabeView);
		    view.makeVisible();
                }

		removeSplash();

		// send OK to server
		kommClient.spielstart();

		scrollFlag(1);

	    }
	    catch (KommException kE) {
		System.err.println("Ausgabe: Beim Versuch, die Roboter zu holen, erhalte ich: "+
				   kE.getMessage());
		return;
	    }
	    catch (FormatException e) {
		System.err.println(e.getMessage());
	    }
	    catch (FlaggenException e){
		System.err.println(e.getMessage());
	    }
	}
	else {
	    // Problem: the server sends shit
	    Global.debug(this, "server does not send a game start at game start... fui!");
	}

	registered = true;

    }



    /**
     * Fordert die View auf, eine ActionMessage anzuzeigen
     */
    protected void showActionMessage(String s){
	if (ausgabeView !=null) {
	    ausgabeView.showActionMessage(s);
	}
    }

    protected Roboter getRob(String name){
	return (Roboter) robots.get(name);
    }


    protected void showScout(int chosen, Roboter[] robs) {
	ausgabeView.showScout(chosen,robs);
    }


    /**
     * Zeigt einen Text im Splashscreen an
     * (erzeugt den Splashscreen, falls nötig)
     */
    private void showSplash(String s) {
	if(!nosplash) {
	    if (splashScreen==null){
		splashScreen = new Splash();
	    }
	    splashScreen.showSplash(s);
	}

    }

    /**
     * Zeigt einen Text im Splashscreen an
     * (erzeugt den Splashscreen, falls nötig)
     */
    private void removeSplash() {
	if(!nosplash) {
	    splashScreen.noSplash();
	}

    }

    private Roboter[] getRoboterArray(){
	Roboter[] robs = new Roboter[robots.size()];
	int i=0;
	Iterator iter = robots.values().iterator();
	while (iter.hasNext()) {
	    robs[i] = (Roboter) iter.next();
	    i++;
	}
	return robs;
    }


    private void registerAtServer() {
	boolean anmeldungErfolg = false;
	int versuche = 0;


	showActionMessage(Message.say("AusgabeFrame","Anmeldung"));
	while ((!anmeldungErfolg)&&(versuche < 3)) {
	    try{
		anmeldungErfolg = kommClient.anmelden2(host,port,name);
	    }
	    catch (KommException kE) {
		System.err.println(kE.getMessage());
		showSplash(Message.say("AusgabeFrame","msplashFehlerAnmeldung"));
		versuche++;
		try {Thread.sleep(3000);} catch (Exception e) {System.err.println(e.getMessage());}
	    }
	}


	if (anmeldungErfolg) {
	    Global.debug(this,"registered for game as new view with name: "+name);
	}
	else {
	    Global.debug(this, "could not register at the server: "+host);
	    showSplash(Message.say("AusgabeFrame","msplashEnde"));
	    try {Thread.sleep(2000);} catch (Exception e) {System.err.println(e.getMessage());}
	    removeSplash();
	}




    }


    /**
     * Zentriert den Robi und folgt ihm.
     */
    public void trackRob (String rName) {
	Roboter r = (Roboter) robots.get(rName);
	trackPos(r.getX(), r.getY());
    }

    public void setTracking( String name ) {
      /** @todo: track the given robot permanently */
    }


    /**
     * Zentriert die angegebenen Flagge
     */
    public void scrollFlag (int nr) {
	trackPos(flags[(nr-1)].getX(),flags[(nr-1)].getY());
    }

    public void trackPos (int x, int y) {
	ausgabeView.showPos(x,y);
    }

    public AusgabeView getAusgabeView() {
	return ausgabeView;
    }

    private void d(String s){
	Global.debug(this, s);
    }

    public void abmelden() {
      kommClient.abmelden( name );
    }

/*




    }


    private class MenuBarListener implements ActionListener {

	public void actionPerformed (ActionEvent e) {
	    String mPunkt = e.getActionCommand();
	    if (mPunkt.equals(Message.say("AusgabeFrame","mFinish"))) {

		dispose();
	    }
	}
    }


    private class SpeedMenuListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    Global.debug(this,"Speed-Menu klicked");
	    if (e.getSource() == lSpeed) {
		speed=LANGSAM;
		showMessage(Message.say("AusgabeFrame","gAufLang"));
	    }
	    else if (e.getSource() == mSpeed) {
		speed=MITTEL;
                if (humanView!=null)
                    ausgabe.showMessage(Message.say("AusgabeFrame","gAufMitt"));
                if (ausgabeView!=null)
		  ausgabeView.showMessage(Message.say("AusgabeFrame","gAufMitt"));

            }
	    else {
		speed=UNGEBREMST;
	        showMessage(Message.say("AusgabeFrame","gAufUn"));
	    }

	}
    }

     private void initMenus(JFrame frame, JMenu trackMenu) {

	// Menüleiste einfügen
	JMenuBar hauptMenu = new JMenuBar();
	MenuBarListener mBL = new MenuBarListener();

	JMenu dateiMenu = new JMenu(Message.say("AusgabeFrame","mFile"));
	JMenuItem mBeenden = new JMenuItem((Message.say("AusgabeFrame","mFinish")));
	mBeenden.addActionListener(mBL);
	dateiMenu.add (mBeenden);
	hauptMenu.add(dateiMenu);

	JMenu optionenMenu = new JMenu((Message.say("AusgabeFrame","mOptions")));
	JMenu optSpeed = new JMenu((Message.say("AusgabeFrame","mSpeed")));


	optionenMenu.add(soundMenu);
	SpeedMenuListener speedListener= new SpeedMenuListener();

	ButtonGroup speedGroup = new ButtonGroup();

	lSpeed = new JRadioButtonMenuItem(Message.say("AusgabeFrame","mSlow"),false);
	lSpeed.addActionListener(speedListener);
	speedGroup.add(lSpeed);
	optSpeed.add(lSpeed);
	mSpeed = new JRadioButtonMenuItem(Message.say("AusgabeFrame","mMiddle"),true);
	mSpeed.addActionListener(speedListener);
	speedGroup.add(mSpeed);
	optSpeed.add(mSpeed);
	hSpeed = new JRadioButtonMenuItem(Message.say("AusgabeFrame","mFast"),false);
	hSpeed.addActionListener(speedListener);
	optSpeed.add(hSpeed);
	speedGroup.add(hSpeed);
	optionenMenu.add(optSpeed);

	JMenu scrollFlag = new JMenu (Message.say("AusgabeFrame","mflagMenu"));
	optionenMenu.add(scrollFlag);

	//JMenu optTrack = new JMenu((Message.say("AusgabeFrame","mRoboTrack")));
	optionenMenu.add(trackMenu);

	hauptMenu.add(optionenMenu);
	hauptMenu.add( new ZoomMenu() );
	JMenu help = new JMenu(Message.say("AusgabeFrame","mHelpMenuName"));
	JMenuItem about = new JMenuItem(Message.say("AusgabeFrame","mAbout"));
	about.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    new AboutFenster();
		}
	    });
	help.add(about);
	hauptMenu.add(help);


	view.setJMenuBar(hauptMenu);
    }
*/
}



