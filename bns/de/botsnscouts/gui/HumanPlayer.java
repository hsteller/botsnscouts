package de.botsnscouts.gui;

import  de.botsnscouts.*;
import  de.botsnscouts.util.*;
import  de.botsnscouts.comm.*;
import  de.botsnscouts.autobot.*;
import  de.botsnscouts.board.*;
import  de.botsnscouts.server.KartenStapel;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.metal.*;
/**
 * logic for the human player
 * @author Lukasz Pekacki
 */
public class HumanPlayer extends Thread {
    protected final int LANGSAM = 2000;
    protected final int MITTEL = 200;
    protected final int UNGEBREMST = 0;
    protected final boolean NURAUSGABE = true;
    protected final boolean MENSCHAUSGABE = false;

    // Einstellungen fuers Zoom-Menu
    protected static final int MIN_ZOOM = 40;
    protected static final int MAX_ZOOM = 100;
    protected static final int ZOOM_STEP = 10;

    protected final static int MODE_PROGRAM = 0;
    protected final static int MODE_OTHER = 1;

    protected int mode=MODE_OTHER;
    private HumanView humanView;
    private Ausgabe ausgabe;
    private KommClientSpieler comm;
    private View view;
    private ClientAntwort commAnswer = new ClientAntwort();
    private SpielfeldKS intelliBoard;

    private ArrayList cards = new ArrayList(9);
    private String host, name;
    private int port, myRobIndex, myColor, globalTimeout;
    private boolean gameOver = false, nosplash = false;
    private Wisenheimer wisenheimer;

    public HumanPlayer (String host, int port, String name) {
	this(host,port,name,-1);
    }

    public HumanPlayer(){
	this ("localhost",8077,KrimsKrams.randomName());
    }
    public HumanPlayer(String host, int port, String name, int color) {
	this (host, port, name, color, false);
    }
    public HumanPlayer(String host, int port, String name, int color, boolean nosplash) {
	this.host = host;
	this.port = port;
	this.name = name;
	this.nosplash=nosplash;
	myColor=color;
	comm = new KommClientSpieler();
    }


    /**
     * Start des Menschlichen Spielers
     */
    public void run(){

	// --- registering for game ---
	if (registerAtServer()) {
	    Global.debug(this,"registered for game as new humanplayer with name: "+name);
	}
	else {
	    Global.debug(this, "could not register at the server: "+host);
	    try {Thread.sleep(2000);} catch (Exception e) {System.err.println(e.getMessage());}
	    return;
	}

	initView();

	// ------- begin to play
	while (!gameOver) {

	    try {
		commAnswer = comm.warte();
		Global.debug(this,"Server sends : " + commAnswer.getTyp());
	    }
	    catch (KommException kE) {
		Global.debug(this,kE.getMessage());
	    }

	    switch (commAnswer.typ) {
	    case (ClientAntwort.MACHEZUG): {
		mode=MODE_PROGRAM;
		Global.debug(this,"I am requested to send cards");
		// card
		showMessage(Message.say("SpielerMensch","mwartereg"));

		try{
		    Roboter tempRob = comm.getRobStatus(name);
		    d("rob has the following locked registers: ");
			for (int i=0; i< tempRob.getGesperrteRegister().length; i++) d("index: "+i+" ist "+tempRob.getGesperrteRegister()[i]);
		    humanView.updateRegisters(tempRob.getGesperrteRegister());
		}
		catch (KommException kE) {
		    System.err.println("SpielerMenschERROR: "+kE.getMessage());
		}

				// ----- Karten einsortieren  -----
		cards.clear();
		for (int i = 0; i < commAnswer.karten.length; i++) {
		    cards.add(i, new HumanCard(commAnswer.karten[i]));
		}
		humanView.showCards(cards);
				// ----- Abgabe der Programmierung -----

		/*	start Timer
			if (temptimeout == 0) {
			showMessage(Message.say("SpielerMensch","legalZug"));
			int gesperrteRegister = 0;
			for (int i = 0;i < 5; i++) if (uI.register[i].status == GESPERRT) gesperrteRegister++;
			int[] prog = new int[(5-gesperrteRegister)];
			for (int i = 0; i < prog.length; i++) prog[i] = (i+1);
			comm.registerProg(name,prog,false);
			}
		*/
		break;
	    }

	    // start of the game
	    case (ClientAntwort.SPIELSTART): {
		showMessage(Message.say("SpielerMensch","spielgehtlos"));
		comm.spielstart();
		break;
	    }

	    // robot destroyed or initally set on the board
	    case (ClientAntwort.ZERSTOERUNG): {
		humanView.showGetDirection();
		Global.debug(this,"Habe einee Zerstörung bekommen.");
		showMessage(Message.say("SpielerMensch","roboauffeld"));
		   // --- Spielfeld für den Klugscheisser holen
		if (intelliBoard==null) {
		    initIntelligentBoard();
		    wisenheimer = new Wisenheimer(intelliBoard);
		}

	       	// ----- ask for timeout -------
		if (globalTimeout ==0) {
		    try {
			globalTimeout = comm.getTimeOut();
		    }
		    catch (KommException kE) {
			System.err.println("SpielerMenschKommunkationsERROR: wollte Timeout erfragen: "+kE.getMessage());
		    }
		}
		break;
	    }
	    // robot reaktivated
	    case (ClientAntwort.REAKTIVIERUNG): {
		showMessage(Message.say("SpielerMensch","roboreaktiviert"));
		// ask for powerDownagain
		humanView.showRePowerDown();


		break;
	    }

	    // repair your registers
	    case (ClientAntwort.REPARATUR):{
		Global.debug(this,"Reparatur erhalten");

		   try {
		       Global.debug(this,"Reparatur erhalten; ersuche, Status von "+name+"  zu erfragen...");
		       Roboter tempRob = comm.getRobStatus(name);
		       int repPoints = commAnswer.zahl;
		       humanView.showRegisterRepair(tempRob.getGesperrteRegister(), repPoints);
		   }
		   catch (KommException kE) {
		       System.err.println("SpielerMensch: "+kE.getMessage());
		   }

		break;
	    }

	    // removed from game
	    case (ClientAntwort.ENTFERNUNG): {
		// ------- Habe ich gewonnen / bin ich gestorben ----------
		boolean dead = true;
		int rating=0;
		try {
		    String[] gewinnerListe = comm.getSpielstand();
		    if(gewinnerListe != null) {
			showMessage(Message.say("SpielerMensch","spielende"));
			for (int i = 0; i < gewinnerListe.length; i++) {
			    if (gewinnerListe[i].equals(name)) {
				dead=false;
				rating=(i+1);
			    }
			}
		    }
		    else {
			Global.debug(this,"Bin gestorben...");
			dead = true;
		    }
		}
		catch (KommException e) {Global.debug(this, e.getMessage());}
		gameOver=true;
		humanView.showGameOver(dead, rating);
		break;
	    }
	    default : {
		Global.debug(this,"Unkonown message form server.");
	    }
	    }
	}

	Global.debug(this," this is my end");
	try {
	    ausgabe.join();
	} catch(InterruptedException e){e.printStackTrace();}
	return;
    }

    /**
     * Main-Methode, die den menschlichen Spieler von der Shell aus als Thread startet
     */
    public static void main(String[] args){
	//1. name (optional)
	//2. host (optional)
	//3. port  "
	//4. farbe "
	/*	int sPort = 0;
		SpielerMensch spM;
		if ((args.length > 0) &&(args[0] != "") && (args[1]) !="") {
		try {sPort = Integer.parseInt(args[1]); } catch (Exception e) {System.err.println(e.getMessage());}
		spM = new SpielerMensch(args[0],sPort,createName());
		}
		else spM = new SpielerMensch();
		spM.run();*/
	String name, host="127.0.0.1";
	int port=8077, farbe=0;
	if (args[0] == null) name = KrimsKrams.randomName();
	else   name = args[0];
	int tmpInt;
	switch(args.length){
	case 2: try{
	    tmpInt=Integer.parseInt(args[1]);
	    if (tmpInt<9){
		farbe=tmpInt;
		port=8077;
	    }else{
		port=tmpInt;
		farbe=0;
	    }
	    host="127.0.0.1";
	}catch(NumberFormatException e){
	    host=args[1];
	    port=8077;
	    farbe=0;
	}
	break;
	case 3: host=args[1];
	    try{
		tmpInt=Integer.parseInt(args[2]);
		if (tmpInt<9){
		    farbe=tmpInt;
		    port=8077;
		}else{
		    port=tmpInt;
		    farbe=0;
		}
	    }catch(NumberFormatException e){
		System.err.println(e);
	    }
	    break;
	case 4: host=args[1];
	    try{
		port=Integer.parseInt(args[2]);
		farbe=Integer.parseInt(args[3]);
	    }catch(NumberFormatException e){
		System.err.println(e);
	    }
	}
	MetalLookAndFeel.setCurrentTheme( new GreenTheme() );
	(new SpielerMensch(host,port,name,farbe)).start();
    }

    protected void sendCards(ArrayList registerCards, boolean nextTurnPowerDown) {
	mode=MODE_OTHER;
	int sendProg[] = new int[registerCards.size()];
	int index=0;

	d("meine Registerkarten: "+registerCards);
	d("die Karten, die der Server ausgeteilt hat:"+cards);


	for (int i=0; i < registerCards.size(); i++) {
	    for (int j=0; j < cards.size(); j++) {
		if ( ((HumanCard) registerCards.get(i)).equals((HumanCard)cards.get(j)) ) {
		    sendProg[index]=(j+1);
		    index++;
		    continue;
		}
	    }
	}
	comm.registerProg(name,sendProg,nextTurnPowerDown);
    }

    private boolean registerAtServer() {
	boolean anmeldungErfolg = false;
	int versuche = 0;

	while ((!anmeldungErfolg)&&(versuche < 3)) {
	    try{
		anmeldungErfolg = comm.anmelden2(host,port,name,myColor);
	    }
	    catch (KommException kE) {
		System.err.println(kE.getMessage());
		versuche++;
		try {Thread.sleep(1000);} catch (Exception e) {System.err.println(e.getMessage());}
	    }
	}
	return anmeldungErfolg;

    }

    /** meldet den Spieler beim Server ab und beendet diesen Thread.
     */
    private void quit() {
	Global.debug(this, "Roboter "+name+" is leaving the party.");
	comm.abmelden(name);
	//Dafuer sorgen, dass Thread aufhoert
	System.exit(0);
    }

    protected void passUpdatedScout(int chosen, Roboter[] robs) {
	ausgabe.showScout(chosen,robs);
    }


    protected void sendRepair(ArrayList respReparatur) {
	d("sende meinen Reparaturwunsch: "+respReparatur);
	int[] repa = new int[respReparatur.size()];
	for (int i = 0; i < respReparatur.size(); i++) {
	    repa[i] = ((Integer) respReparatur.get(i)).intValue();
	}
	comm.respReparatur(name,repa);
	showMessage(Message.say("SpielerMensch","sendregrep"));
    }

    /** Schickt eine ChatMessage von mir an den Server */
    protected void sendMessage(String msg){
	d("sende: "+msg);
	String[] tmp=new String[2];
	tmp[0]=name;
	tmp[1]=msg;
	comm.message("mChat",tmp);
    }

    protected Roboter getRob() {
	return ausgabe.getRob(name);

    }


    protected void sendDirection(int r) {
	comm.respZerstoert(name,r);
	// show wait message
    }


    protected void sendAgainPowerDown(boolean down) {
	comm.respReaktivierung(name,down);
    }

    protected int getNextPrediction(ArrayList registerList,ArrayList cardList){
	return wisenheimer.getNextPrediction(registerList,cardList);
    }
    protected int getPrediction(ArrayList registerList, ArrayList cardList) {
	return wisenheimer.getPrediction(registerList, cardList, ausgabe.getRob(name));
    }

    /**
     * Schreibt in die Statuszeile einen Text
     */
    private void showMessage(String s){
	// Logframe instanziieren, falls nötig
	ausgabe.showActionMessage(s);
    }


    private void initIntelligentBoard()
    {
	int dimx, dimy;
	Ort dimension;
	try{
	    dimension=comm.getSpielfeldDim();
	    dimx=dimension.x;
	    dimy=dimension.y;
	    Ort[] fahnen=comm.getFahnenPos();
	    String spielfeldstring=comm.getSpielfeld();
	    try{
		intelliBoard = SpielfeldKS.getInstance(dimx,dimy,spielfeldstring,fahnen);
	    }

	    catch(Exception e){
		System.err.println("HumanPlayer has a problem: No Board"+e);
	    }
	}
	catch(Exception e){
	    System.err.println("HumanPlayer has a problem: No Board!"+e);
	}
    }


    private void initView() {
	humanView = new HumanView(this);
	view=new View(humanView);          // adds the humanView to the JFrame
	ausgabe = new Ausgabe(host, port, nosplash, this, view);   //adds the AusgabeView to the JFrame
	ausgabe.initialize();
	ausgabe.start();
	ChatPane chatpane=new ChatPane(this);
	view.addChatPane(chatpane);
        initMenus( view, ausgabe.getAusgabeView().getTrackMenu() );
    }

    JMenuItem lSpeed;
    JMenuItem mSpeed;
    JMenuItem hSpeed;
    int speed = MITTEL;


    private void initMenus(JFrame frame, JMenu trackMenu) {

	// Menüleiste einfügen
	JMenuBar hauptMenu = new JMenuBar();
	MenuBarListener mBL = new MenuBarListener();

	JMenu dateiMenu = new JMenu(Message.say("AusgabeFrame","mFile"));
	JMenuItem mBeenden = new JMenuItem((Message.say("AusgabeFrame","mFinish")));
	mBeenden.addActionListener(mBL);
	dateiMenu.add(mBeenden);
	hauptMenu.add(dateiMenu);

	JMenu optionenMenu = new JMenu((Message.say("AusgabeFrame","mOptions")));
	JMenu optSpeed = new JMenu((Message.say("AusgabeFrame","mSpeed")));
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


    private class SpeedMenuListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    Global.debug(this,"Speed-Menu klicked");
	    if (e.getSource() == lSpeed) {
		speed=LANGSAM;
		showMessage(Message.say("AusgabeFrame","gAufLang"));
	    }
	    else if (e.getSource() == mSpeed) {
		speed=MITTEL;
		showMessage(Message.say("AusgabeFrame","gAufMitt"));
	    }
	    else {
		speed=UNGEBREMST;
	        showMessage(Message.say("AusgabeFrame","gAufUn"));
	    }

	}
    }


    private class ZoomMenu extends JMenu implements ActionListener {
	ZoomMenu() {
	    super("Zoom");
	    ButtonGroup group = new ButtonGroup();
	    JRadioButtonMenuItem item = null;
	    for(int d = MIN_ZOOM; d <= MAX_ZOOM; d += ZOOM_STEP ) {
		item = new JRadioButtonMenuItem( "" + d + "%" );
		item.setActionCommand("" + d);
		item.addActionListener( this );
		super.add( item );
		group.add( item );
	    }
	    if( item != null ) {
		group.setSelected( item.getModel(), true );
	    }
	}

	public void actionPerformed(ActionEvent e) {
	    int iScale;
	    try {
		String s = e.getActionCommand();
	        iScale = Integer.parseInt( s );
	    } catch( NumberFormatException ne ) {
	        iScale = 10;
		Global.debug(this, "bad zommmenu action command. using default 100%");
	    }
	    final double sc = iScale / 100.0;
	    SwingUtilities.invokeLater( new Runnable() {
		    public void run() {
			ausgabe.getAusgabeView().getSpielfeld().setScale( sc );
		    }
		});
	}
    }




    /**
     * Wartet auf einen Menuklick
     */
    private class MenuBarListener implements ActionListener {

	public void actionPerformed (ActionEvent e) {
	    String mPunkt = e.getActionCommand();
	    if (mPunkt.equals(Message.say("AusgabeFrame","mFinish"))) {
                comm.abmelden( name );
                ausgabe.abmelden();
		view.dispose();
	    }
	}
    }


    static class RoboTrackListener implements ActionListener {
	Roboter r;
	RoboTrackListener(Roboter r) {
	    this.r = r;
	}

	public void actionPerformed(ActionEvent e) {
//	    ausgabe.setTracking( r.getName() );
//	    ausgabe.trackRob( r.getName() );
	}
    }



    private void d(String s) {
	Global.debug(this,s);
    }

}
