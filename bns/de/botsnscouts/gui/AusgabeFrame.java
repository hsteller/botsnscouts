package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import de.botsnscouts.comm.*;
import de.botsnscouts.comm.*;
import de.botsnscouts.util.*;
import de.botsnscouts.board.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import java.net.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * Diese Klasse erledigt die Ausgabe von Spielfeld und Status
 * @author Lukasz Pekacki
 */

public class AusgabeFrame extends JFrame implements Runnable, SACanvas.ClickListener {


    // -----------  Konstanten -----------
    protected final Dimension screendim = Toolkit.getDefaultToolkit().getScreenSize();
    protected final int LANGSAM = 2000;
    protected final int MITTEL = 500;
    protected final int UNGEBREMST = 0;
    protected final boolean NURAUSGABE = true;
    protected final boolean MENSCHAUSGABE = false;

    // Einstellungen fuers Zoom-Menu
    protected static final int MIN_ZOOM = 40;
    protected static final int MAX_ZOOM = 100;
    protected static final int ZOOM_STEP = 10;

    // ---- Variablen
    private boolean spielerErhalten;
    private int lastPhase = -1;
    protected boolean ausgabeTyp = NURAUSGABE;
    protected TextArea ta = new TextArea(Message.say("AusgabeFrame","afLog"),5,40);

    // Verzögerung der Phasendarstellung (! solange bleibt der Server blockiert !)
    private int speed = MITTEL;

    // ---- Farben
    private static final Color backColor = new Color(4,64,4);
    private static final Color foreColor2 = new Color(140,255,140);
    private static final Color foreColor = new Color(110,240,110);
    protected final Color[] robocolor = SACanvas.robocolor;
    private Color[] roboNcolor = new Color[8];

    private String roboOnTrack ="";

    private boolean aeFertig,spielFeldErhalten = false;
    
    // --- Objekte

    // Das Log-Fenster
    LogFrame lF;
    // Komm-Objekt der Ausgabe
    protected KommClientAusgabe kCA;
    // Antwortobjekt des Kommobjektes
    protected ClientAntwort cA = new ClientAntwort();
    // StatusLabel-Panel
    protected StatusBarPanel stBP;
    protected Label stBar;
    // Menu samt Listener
    protected JMenuBar hauptMenu;
    protected ButtonGroup speedGroup;
    protected JMenu optionenMenu;
    protected JMenu optTrack;
    protected JMenu scrollFlag;
    // Flaggenort
    Ort[] flagsPosition;
    JMenuItem[] flagPos;
    protected MenuBarListener mBL;
    protected SpeedMenuListener speedListener;
    protected JRadioButtonMenuItem lSpeed;
    protected JRadioButtonMenuItem mSpeed;
    protected JRadioButtonMenuItem hSpeed;
    // Panel, in dem die Roboter-Stati enthalten sind
    protected StatusPanel statusLine;
    // Objekt, das das Spielfeld in einem Scrollfenster darstellt
    protected JScrollPane mP;
    protected SACanvas spielFeld;
    // Objekt, das Warte-Splash-Screen anzeigt, bevor die Ausgabe angemeldet ist
    Splash warteSplashScreen; 
    // Kommunikation und Authentifizierung mit dem Server
    private String host, name;
    private int port;
    private boolean nosplash = false;
    private SpielerMensch spieler;
	
    protected boolean spielEnde = false;

    /** @args SpielerMensch spielerref ist Referenz auf umgebenden 
     *  MenschlichenSpieler, falls Ausgabe zu einem Spieler gehoert,
     *  null sonst.
     */
    public AusgabeFrame(String host, int port, SpielerMensch spielerref) {
	this(host,port,spielerref,false);
    }


    public AusgabeFrame(String host, int port, SpielerMensch spielerref, boolean nosplash) {
	super(Message.say("AusgabeFrame","gameName"));
	this.nosplash=nosplash;
	// Splash-Screen anzeigen
	if (!nosplash) { 
	    warteSplashScreen=new Splash();
	    warteSplashScreen.showSplash(Message.say("AusgabeFrame","msplashWarte"));
	}

	spieler = spielerref;
	//Aus Kompatibilitaetsgruenden:
	if (spielerref==null)
	    this.ausgabeTyp = true;
	else
	    this.ausgabeTyp = false;
	// Namen ausdenken
	name = createName();
	
	// Fenstergröße auf Vollbild setzen

	Toolkit tk=Toolkit.getDefaultToolkit();

	setSize(tk.getScreenSize().width-8,tk.getScreenSize().height-8);
	setLocation(4,4);

	/*

	if((screendim.width > 800) && ((screendim.width*5/6) > 800))
	    {
		setSize(screendim.width*5/6,screendim.height*5/6);
	    }
	else setSize(screendim.width,screendim.height);

	*/
	// Fentster-Schließen ermöglichen
	addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e){
		    if (spieler!=null)
			spieler.abmelden();
		    if (kCA!=null) 
			kCA.abmelden(name);

		    dispose();
		}});


	// Menüleiste einfügen
	hauptMenu = new JMenuBar();
	mBL = new MenuBarListener();

	JMenu dateiMenu = new JMenu(Message.say("AusgabeFrame","mFile"));
	JMenuItem mBeenden = new JMenuItem((Message.say("AusgabeFrame","mFinish")));
	mBeenden.addActionListener(mBL);
	dateiMenu.add(mBeenden);
	hauptMenu.add(dateiMenu);

	optionenMenu = new JMenu((Message.say("AusgabeFrame","mOptions")));
	JMenu optSpeed = new JMenu((Message.say("AusgabeFrame","mSpeed")));
	speedListener= new SpeedMenuListener();

	speedGroup = new ButtonGroup();
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

	scrollFlag = new JMenu (Message.say("AusgabeFrame","mflagMenu"));
	optionenMenu.add(scrollFlag);

	optTrack = new JMenu((Message.say("AusgabeFrame","mRoboTrack")));
	optionenMenu.add(optTrack);

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

	setJMenuBar(hauptMenu);

	// Layout erzeugen
	BorderLayout bL = new BorderLayout();
	getContentPane().setLayout(bL);

	// Parameter übernehmen
	this.host = host;
	this.port = port;

	// Ausgabe-Fenster erzeugen
	setTitle((Message.say("AusgabeFrame","gameName")));
	// Mainpanel erzeugen
	mP = new JScrollPane();
	mP.getHorizontalScrollBar().setUnitIncrement(64);
	mP.getVerticalScrollBar().setUnitIncrement(64);

	// Inittialisierung der Statuszeile
	stBar = new Label((Message.say("AusgabeFrame","ready")));
	stBar.setSize(this.getSize().width,20);
	stBP = new StatusBarPanel();
	statusLine = new StatusPanel();
	getContentPane().add(statusLine,BorderLayout.NORTH);
	getContentPane().add(mP,BorderLayout.CENTER);
	getContentPane().add(stBP,BorderLayout.SOUTH);
	// KommClientAusgabe erzeugen
	kCA = new KommClientAusgabe();
    }


    /** 
     * Konstruktor  zum Testen der Ausgabe
     */ 
    public AusgabeFrame(){
	this ("localhost",8077,null);
    }

    /**
     * Methode, die sich einen Namen für den Ausgabekanal ausdenkt
     **/
    private String createName(){
	return KrimsKrams.randomName(); // liefert einen Phantasienamen
    }

    /**
     * Schreibt in die Statuszeile einen Text
     */
    protected void setStatus(String s){
	// Logframe instanziieren, falls nötig
	if(lF==null) lF = new LogFrame();

	stBar.setText(s);
	ta.append("\n"+s);
    }
    /**
     * Liefert die aktuelle Position des AusgabeFrames
     */
    protected Point ausgabeFrameLoc() {
	return getLocationOnScreen();
    }
    /**
     * Liefert die aktuelle Größe des AugabeFrames
     */
    protected Dimension ausgabeFrameSize() {
	return getSize();
    }

    /** 
     * needed for beeing used as a SACanvas.ClickListener
     * @see SACanvas.ClickListener, MouseEvent.getModifiers()
     */
    public void feldClicked(int x, int y, int modifiers ) {
	System.out.println("tracking button at " + x + " " + y);
	trackPos(x,y);
    }


    public void trackRob (String rName) {

	    JViewport jV= mP.getViewport();
	    
	    //	    Global.debug(this,"Versuche, Robi zu tracken: "+rName);
	    int robix=0;
	    int robiy=0;
	    
	    for (int i = 0; i < statusLine.sC.length; i++) {
		if (rName.equals(statusLine.sC[i].r.getName())) {
		    if(statusLine.sC[i].r.getSchaden() < 10) {
		    robix=statusLine.sC[i].r.getX();
		    robiy=statusLine.sC[i].r.getY();
		    }
		    else {
		    robix=statusLine.sC[i].r.getArchivX();
		    robiy=statusLine.sC[i].r.getArchivY();
		    }
		}
	    }

	    trackPos(robix, robiy);
    }


    public void scrollFlag (int nr) {
	JViewport view= mP.getViewport();
	Global.debug(this,"Scrolle zur Flagge: "+nr);
	int x = flagsPosition[nr-1].x*64;
	int y = spielFeld.getHeight()-(flagsPosition[nr-1].y*64);
	//trackPos(x,y);

 /******************************************************************/
	Dimension sz = view.getExtentSize();
	int w2 = sz.width/2;
	int h2 = sz.height/2;
	

	// make sure we dont want to scoll 'out' to
	// the left and top
	int x1 = Math.max( x - w2 , 0);
	int y1 = Math.max( y - h2 , 0);

	// ... and right and bottom
       	x1 = Math.min( x1, (spielFeld.getWidth() - sz.width) );
	y1 = Math.min( y1, (spielFeld.getHeight() - sz.height) );

	if ((view.getViewPosition().x != x1) || (view.getViewPosition().y != y1))  {
	    view.setViewPosition(new Point(x1, y1));
	}
/************************************************************************/


    }

    public void trackPos (int robix, int robiy) {

	    JViewport jV= mP.getViewport();
	    
	    //	Global.debug(this,"Er steht an Pos x: "+robix+" y: "+robiy);
	    int x = robix*64;
	    int y = spielFeld.getHeight()-(robiy*64);
		
	    Dimension sz = jV.getExtentSize();
	    int w2 = sz.width/2;
	    int h2 = sz.height/2;
	

	    // make sure we dont want to scoll 'out' to
	    // the left and top
	    int x1 = Math.max( x - w2 , 0);
	    int y1 = Math.max( y - h2 , 0);

	    // soll ich überhaupt scrollen?
	    // in X-Richtung
	    if ((x < jV.getViewPosition().x) || 
		x > (jV.getViewPosition().x+sz.width)) {
		x1 = Math.min( x1, (spielFeld.getWidth() - sz.width) );
	    }
	    else x1 = jV.getViewPosition().x;

	    // in Y-Richtung
	    if ((y < jV.getViewPosition().y) || 
		y > (jV.getViewPosition().y+sz.height)) {
		y1 = Math.min( y1, (spielFeld.getHeight() - sz.height) );
	    }
	    else y1 = jV.getViewPosition().y;
	
	    jV.setViewPosition(new Point(x1, y1));
    }


    /**
     * Statusleiste der spielenden Roboter
     */
    protected class StatusPanel extends TJPanel {
	StatusCanvas[] sC = new StatusCanvas[8];	  
	TJPanel schlaf = new TJPanel(new FlowLayout(FlowLayout.RIGHT));
	TJPanel stat = new TJPanel(new FlowLayout(FlowLayout.LEFT));
	
	public StatusPanel(){
	    this.setSize(ausgabeFrameSize().width,60);
	    this.setLayout(new BorderLayout());
	    for (int i = 0; i < sC.length; i++) {
		sC[i] = new StatusCanvas(robocolor[i]); 
		stat.add(sC[i]);
	    }
	    this.add(stat,BorderLayout.WEST);
	}
	public void setRobStatus(String rname, Roboter rob) {
	    for (int i = 0; i < sC.length; i++) 
		if (rname.equals(sC[i].r.getName())) {
		    sC[i].setRob(rob,i);
		}
	}
	public void makeRobStatus(Roboter rob,int i) {
	    sC[i].setRob(rob,i);
	}
	public void weitereStati(Status[] aktStatus) {
	}
    }

    /**
     * Status-Canvas
     */
    protected class StatusCanvas extends JComponent implements MouseListener {
	ScopeStat stc;
	Roboter r;
	Color robcolor;
	int gewinnerNr = 0;
	int xsize=75, ysize=60;
	
	StatusCanvas () {
	    this.addMouseListener(this);
	    r = Roboter.getNewInstance("foobar");
	    r.setLeben(0);
	    this.setSize(75,60);
	    this.setBackground(backColor);
	    this.setForeground(foreColor);
	    this.setBorder( new EtchedBorder(2) );
	}

	StatusCanvas (Color c) {
	    this();
	    robcolor = c;
	}
	StatusCanvas (Roboter rin, Color c) {
	    r = rin;
	    robcolor = c;
	}
	
	
	public Dimension getMinimumSize() {
	    return new Dimension(xsize,ysize);
	}
	
	public Dimension getPreferredSize() {
	    return new Dimension(xsize,ysize);
	}
	

	public void setName (String s) {
	    //	    r.setName(s);
	    this.repaint();
	}

	public void setRob (Roboter rob,int color) {
	    // --- hat sich wirklich was veraendert?
	    if ((r.getLeben() != rob.getLeben()) || 
		(r.getNaechsteFlagge() != rob.getNaechsteFlagge()) ||
		(r.getSchaden() != rob.getSchaden()) ||
		(r.istAktiviert() != rob.istAktiviert())) {
		r = rob;
		robcolor = roboNcolor[color];
		this.repaint();
	    }
	    else {r=rob;}
	}
	
	// Malt die Gewinnernummer für diesen Rob, falls er
	// das Spiel schon beendet hat
	public void setGewinnerNr(int n){
	    gewinnerNr=n;
	    this.repaint();
	}
	public void paint(Graphics g)
	{
	    if (!r.istAktiviert()) this.setBackground(Color.gray);
	    else this.setBackground(backColor);
	    // Name
	    g.setFont(new Font("SansSerif",Font.BOLD,12));
	    g.setColor(robcolor);
	    g.drawString(r.getName(),2,13);
	    g.setColor(foreColor);
	    if (r.getLeben() <= 0){
		g.setColor(Color.red);
		int[] x1 = {5,10,45,40};
		int[] y1 = {30,25,45,50};
		g.fillPolygon(x1,y1,4);
		int[] x2 = {5,10,45,40};
		int[] y2 = {45,50,30,25};
		g.fillPolygon(x2,y2,4);
	    }
	    else if (gewinnerNr > 0){
		switch (gewinnerNr){
		case 1:
		    g.setColor(new Color(226,249,47));
		    break;
		case 2:
		    g.setColor(new Color(217,217,217));
		    break;
		case 3:
		    g.setColor(new Color(249,121,49));
		    break;
		default:
		    g.setColor(new Color(22,204,246));
		}
		g.setFont(new Font("Serif",Font.BOLD,36));
		g.drawString(gewinnerNr+".",16,50);
	    }
	    else {
		g.setFont(new Font("SansSerif",0,10));
		g.drawString(Message.say("AusgabeFrame","goalFlag")+r.getNaechsteFlagge(),2,28);
		g.drawString(Message.say("AusgabeFrame","lifes")+r.getLeben(),2,40);
		g.drawString(Message.say("AusgabeFrame","hurt")+r.getSchaden(),2,53);
		
	       
	    }
	}
    
	public void mouseClicked(MouseEvent e){
	    int mods = e.getModifiers();
	    // mouse button 3: scroll to  clicked rob
	    if( (mods & MouseEvent.BUTTON3_MASK) != 0 ) {
		trackRob( r.getName() );
		return;
	    } 
	    if(stc==null) stc = new ScopeStat(r); 
	    else {
		stc.dispose();
		stc=null;
	    } 
	}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){
	    if (stc!=null) {
		stc.dispose();
		stc=null;
	    }
	}
	public void mouseReleased(MouseEvent e){}
	public void mousePressed(MouseEvent e){}

    }

    /**
     * Canvas für den Klugscheisser - aktiv
     */
    protected class KlugSCanvas extends Canvas {
	int xsize=60, ysize=60;

	KlugSCanvas() {
	    super();
	    this.setBackground(Color.white);
	    this.setName(Message.say("AusgabeFrame","klugScheisser"));
	    this.setSize(60,60);
	}

	public Dimension getMinimumSize() {
	    return new Dimension(xsize,ysize);
	}
	
	public Dimension getPreferredSize() {
	    return new Dimension(xsize,ysize);
	}
	


	public void paint(Graphics g) {
	    g.setColor(backColor);
	    g.fillRect(0,0,60,60);
	    g.setColor(Color.white);
	    g.fillRect(3,3,54,54);
	    g.setColor(backColor);
	    g.fillRect(0,53,60,60);
	    g.setColor(Color.orange);
	    for (int i = 0; i < 12;i++) {
		int[] x = new int[4];
		int[] y = new int[4];
		x[0] = 5*i;
		y[0] = 60;
		x[1] = 5*i+3;
		y[1] = 60;
		x[2] = 5*i+3;
		y[2] = 53;
		x[3] = 5*i+6;
		y[3] = 53;
		g.fillPolygon(x,y,4);
	    }
	    g.setColor(Color.black);
	    g.setFont(new Font("SansSerif",0,8));
	    g.drawString(Message.say("AusgabeFrame","klugScheisser"),6,25);
	    g.drawString(Message.say("AusgabeFrame","klatte"),22,38);
	}
    }
    /**
     * Canvas für den Klugscheisser - schlafend
     */
    protected class SchlafKlugSCanvas extends Canvas {
	int xsize=60, ysize=60;
	SchlafKlugSCanvas() {
	    super();
	    this.setBackground(Color.white);
	    this.setName(Message.say("AusgabeFrame","klugScheisser"));
	    this.setSize(60,60);
	}

	public Dimension getMinimumSize() {
	    return new Dimension(xsize,ysize);
	}
	
	public Dimension getPreferredSize() {
	    return new Dimension(xsize,ysize);
	}


	public void paint(Graphics g) {
	    g.setColor(backColor);
	    g.fillRect(0,0,60,60);
	    g.setColor(Color.white);
	    g.fillRect(3,3,54,54);
	    g.setColor(backColor);
	    g.fillRect(0,53,60,60);
	    g.setColor(Color.orange);
	    for (int i = 0; i < 12;i++) {
		int[] x = new int[4];
		int[] y = new int[4];
		x[0] = 5*i;
		y[0] = 60;
		x[1] = 5*i+3;
		y[1] = 60;
		x[2] = 5*i+3;
		y[2] = 53;
		x[3] = 5*i+6;
		y[3] = 53;
		g.fillPolygon(x,y,4);
	    }
	    g.setColor(Color.black);
	    g.drawImage(Images.KSCHLAF,5,5,43,45,this);
	}

    }
    /**
     * Canvas für den Scoutschlafplatz
     */
    protected class SchlafScoutCanvas extends Canvas {
	int xsize=60, ysize=60;
	
	SchlafScoutCanvas() {
	    super();
	    this.setName(Message.say("AusgabeFrame","scout"));
	    this.setSize(60,60);
	}

	public Dimension getMinimumSize() {
	    return new Dimension(xsize,ysize);
	}
	
	public Dimension getPreferredSize() {
	    return new Dimension(xsize,ysize);
	}

	public void paint(Graphics g) {
	    g.setColor(backColor);
	    g.fillOval(0,0,60,60);
	    g.setColor(Color.white);
	    g.fillOval(4,4,52,52);
	    g.drawImage(Images.SCOUTSCHLAF,7,7,45,40,this);
	}
    }
    /**
     * Canvas für den Scoutschlafplatz
     */
    protected class ScoutCanvas extends Canvas {
	int xsize=60, ysize=60;

	ScoutCanvas() {
	    super();
	    this.setName(Message.say("AusgabeFrame","scout"));
	    this.setSize(60,60);
	}

	public Dimension getMinimumSize() {
	    return new Dimension(xsize,ysize);
	}
	
	public Dimension getPreferredSize() {
	    return new Dimension(xsize,ysize);
	}


	public void paint(Graphics g) {
	    g.setColor(backColor);
	    g.fillOval(0,0,60,60);
	    g.setColor(Color.white);
	    g.fillOval(4,4,52,52);
	    g.setColor(Color.black);
	    g.setFont(new Font("SansSerif",0,8));
	    g.drawString(Message.say("AusgabeFrame","scout"),15,25);
	    g.drawString(Message.say("AusgabeFrame","schlafplatz"),10,35);
	}
    }
    /**
     * Fenster der Logmeldungen
     */
    protected class LogFrame extends Frame implements WindowListener,MouseListener {
	boolean first = true;
	public LogFrame() {
	    this.setTitle(Message.say("AusgabeFrame","ereigLog"));
	    this.add(ta);
	    this.addWindowListener(this);
	    this.addMouseListener(this);
	    ta.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent e){ LogFrame.this.setVisible(false);}
		    public void mousePressed(MouseEvent e){ LogFrame.this.setVisible(false);}
		});
	    this.setSize(500,150);
	    this.setBackground(Color.lightGray);
	    this.setResizable(false);
	}
	public void windowDeactivated(WindowEvent e) {this.setVisible(false);}
	public void windowOpened(WindowEvent e)      {}
	public void windowClosing(WindowEvent e) {this.setVisible(false);}
	public void windowIconified(WindowEvent e)   { this.setVisible(false);}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e)   {}
	public void windowClosed(WindowEvent e)   { this.setVisible(false);}
	
	public void mouseClicked(MouseEvent e){this.setVisible(false);}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){this.setVisible(false);}
	public void mouseReleased(MouseEvent e){}
	public void mousePressed(MouseEvent e){this.setVisible(false);}

	public void makeVisible(){
	    if (first){
		int startx = ausgabeFrameLoc().x;
		int starty = ausgabeFrameLoc().y;
		this.setLocation((startx+ausgabeFrameSize().width-500),(starty+ausgabeFrameSize().height-150));
		this.setVisible(true);
		first = false;
	    }
	    else{
		this.setVisible(true);
	    }
	}
	public void finalize(){
	    this.dispose();
	}

    }
    /**
     * Fenster, das weitere Statusinfos anzeigt
     */
    
    protected class ScopeStat extends Frame {

	Label name, gesperrt, gelegt, archivpos, aktiviert, virtuell, pos;
	int xsize = 350;
	int ysize = 200;

	public ScopeStat (Roboter r){
	    this.setBackground(backColor);
	    this.setForeground(foreColor);
	    this.setTitle(Message.say("AusgabeFrame","statusVon")+r.getName());
	    int startx = ausgabeFrameLoc().x;
	    int starty = ausgabeFrameLoc().y;
	    this.setLocation((startx+ausgabeFrameSize().width-350),(starty+ausgabeFrameSize().height-200));
	    // setLayout(null);
	    this.setSize(xsize,ysize);

	    // ---- gesperrte Regisster toString
	    String gespReg = "[ ";
	    if (r.gesperrteRegs() > 0) 
		for (int i = 0; i < r.gesperrteRegs(); i++) 
		    if (r.getGesperrteRegister()[i] != null) gespReg+= r.getGesperrteRegister()[i].getaktion() + " | ";
	    gespReg += "]";

	    // ---- gelegte Karten toString
	    String gelegtKarte = "[ ";
	    if (r.getZug() != null) 
		for (int i = 0; i < r.getZug().length; i++) 
		    if (r.getZug()[i] != null) gelegtKarte+= r.getZug()[i].getaktion() + " | ";
	    gelegtKarte += "]";

	    // ---- Label erzeugen
	    gesperrt = new Label(Message.say("AusgabeFrame","gespReg")+gespReg);
	    gesperrt.setSize(xsize,ysize/7);
	    gesperrt.setLocation(5,ysize/7);
	    this.add(gesperrt);
	    gelegt = new Label(Message.say("AusgabeFrame","gelKarte")+gelegtKarte);
	    gelegt.setSize(xsize,ysize/7);
	    gelegt.setLocation(5,ysize/7*2);
	    this.add(gelegt);
	    archivpos = new Label(Message.say("AusgabeFrame","archPos")+" x: "+r.getArchivX()+" y: "+r.getArchivY());
	    archivpos.setSize(xsize,ysize/7);
	    archivpos.setLocation(5,ysize/7*3);
	    this.add(archivpos);
	    aktiviert = new Label(Message.say("AusgabeFrame","aktiviert")+r.istAktiviert());
	    aktiviert.setSize(xsize,ysize/7);
	    aktiviert.setLocation(5,ysize/7*4);
	    this.add(aktiviert);
	    virtuell = new Label(Message.say("AusgabeFrame","virtuell")+r.istVirtuell());
	    virtuell.setSize(xsize,ysize/7);
	    virtuell.setLocation(5,ysize/7*5);
	    this.add(virtuell);
	    pos = new Label(Message.say("AusgabeFrame","pos")+" x: "+r.getX()+" y: "+r.getY());
	    pos.setSize(xsize,ysize/7);
	    pos.setLocation(5,ysize/7*6);
	    this.add(pos);

	    this.setVisible(true);
	}

    }
    

    /*+
     * Panel für die Statuszeile
     */
    protected class StatusBarPanel extends TJPanel implements ActionListener{
	public StatusBarPanel() {
	    BorderLayout bl = new BorderLayout();
	    bl.setHgap(10);
	    this.setLayout(bl);
	    Button log = new Button(Message.say("AusgabeFrame","afLog"));
	    log.addActionListener(this);
	    this.add(log,BorderLayout.WEST);
	    this.add(stBar,BorderLayout.CENTER);
	}
	public void actionPerformed(ActionEvent e) {
	    lF.makeVisible();
	}
    }

    private class SpeedMenuListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    Global.debug(this,"Speed-Menu klicked");
	    if (e.getSource() == lSpeed) {
		speed=LANGSAM;
		setStatus(Message.say("AusgabeFrame","gAufLang"));
	    }
	    else if (e.getSource() == mSpeed) {
		speed=MITTEL;
		setStatus(Message.say("AusgabeFrame","gAufMitt"));
	    }
	    else {
		speed=UNGEBREMST;
	        setStatus(Message.say("AusgabeFrame","gAufUn"));
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
			spielFeld.setScale( sc );
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
		if (spieler!=null) {
		    spieler.abmelden();
		}
		if (kCA!=null) kCA.abmelden(name);
		dispose();
	    }
	}
    }


    class RoboTrackListener implements ActionListener {
	Roboter r;
	RoboTrackListener(Roboter r) {
	    this.r = r;
	}
	
	public void actionPerformed(ActionEvent e) {
	    roboOnTrack=r.getName();
	    trackRob( r.getName() );
	}
    }

    public void run() {



	// ------- Anmeldung am Server -------

	boolean anmeldungErfolg = false;

	//	setStatus(Message.say("AusgabeFrame","Anmeldung"));
	int versuche = 0;
	while ((!anmeldungErfolg)&&(versuche < 3)) { 
	    try{
		anmeldungErfolg = kCA.anmelden2(host,port,name); 
	    } 
	    catch (KommException kE) {
		System.err.println(kE.getMessage()); 
		if(!nosplash)warteSplashScreen.setText(Message.say("AusgabeFrame","msplashFehlerAnmeldung"));
		Global.debug("Ausgabe: Anmeldung fehlgeschlagen."); } 
	    versuche++; 
	    try {Thread.sleep(3000);} catch (Exception e) {System.err.println(e.getMessage());}
	}
	if (!anmeldungErfolg) {
	    new Fehlermeldung(Message.say("AusgabeFrame","eAnmeldung"));
	    Global.debug(this, "Ausgabe: Beende Versuche.");  
	    // entferne das Splash-Screen
	    if(!nosplash)warteSplashScreen.setText(Message.say("AusgabeFrame","msplashEnde"));
	    try {Thread.sleep(1000);} catch (Exception e) {System.err.println(e.getMessage());}
	    if(!nosplash)warteSplashScreen.noSplash();
	    
	    return;
	}
	else {Global.debug("Ausgabe: Anmeldung erfolgt.");
	}


	
	// ---- Einstieg in die grosse Schleife ---------
	while (!spielEnde) {
	    
	    // ------- Empfang einer Server-Meldung  -------
	    try {
		cA = kCA.warte();
	    }
	    catch (KommFutschException kE) {
		System.err.println("KE: "+kE.getMessage());
		return;
	    }
	    catch (KommException ke) {
		System.err.println("ke: "+ke.getMessage());
	    }
	
	    switch (cA.typ) {
		
				// ---------- Das Spiel beginnt ------
	    case (cA.SPIELSTART): { 
		break;
	    }

	    case (cA.MESSAGE):{
		String[] tmpstr=new String[cA.namen.length-1];
		for (int i=0;i<tmpstr.length;i++)
		    tmpstr[i]=cA.namen[i+1];

		if (!(cA.namen[0].substring(0,5).equals("mAusw")))
		    setStatus(Message.say("MSG",cA.namen[0],tmpstr));

		if (cA.namen[0].equals("mRobLaser")){
		    Roboter r1=null;
		    Roboter r2=null;
		    try {
			r1=kCA.getRobStatus(cA.namen[1]);// schiessender Roboter
		    }
		    catch (KommException k) {
			k.printStackTrace();
		    }
		    try {
			r2=kCA.getRobStatus(cA.namen[2]);// getroffener Roboter
		    }
		    catch (KommException k) {
			k.printStackTrace();
		    }
		     spielFeld.doRobLaser(r1, r2);
		}
		else if (cA.namen[0].equals("mBoardLaser")){
		    Roboter r1=null;
		    Ort r1Pos = null;
		    // get damaged Roboter
		    try {
			r1=kCA.getRobStatus(cA.namen[1]);
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
	 		strength   = Integer.parseInt(cA.namen[2]);
			laserPos.x = Integer.parseInt(cA.namen[3]);
			laserPos.y = Integer.parseInt(cA.namen[4]);
			facing     = Integer.parseInt(cA.namen[5]); 
       		    }
		    catch (NumberFormatException nfe) {
			System.err.println("AusgabeFrame: BoardLaser: NumberFormatException:");
			nfe.printStackTrace();
		    }
		    if ((laserPos!=null)&&(facing>=0)&&(r1Pos!=null)&&(strength>=0))
			spielFeld.doBordLaser(laserPos, facing, strength, r1Pos,mP.getViewport());
		    else {
			System.err.println("AusgabeFrame: unable to calculate Laseranimation: ");
			System.err.println("laserPos: "+laserPos);
			System.err.println("facing: "+facing);
			System.err.println("r1Pos: "+r1Pos);
			System.err.println("strength: "+strength);
		    }
		}
		
		
		kCA.aenderungFertig();
                Global.debug(this,"Nachricht bearbeitet.");
		break;
	    }
		// --------- Aenderung eingetroffen -----------
	    case (cA.AENDERUNG): {
		Global.debug("Ausgabe: Aenderung eingetroffen.");


				// ------- Infos ueber die Robis einholen -----------
		Global.debug("Ausgabe: Bei "+cA.namen.length+" Robotern hat sich was geändert.");
		try { String[] spNamen = cA.namen;
		Roboter robsAnSpielfeld[] = new Roboter[statusLine.stat.getComponentCount()];
		
		for (int i = 0; i < spNamen.length; i++) {
		    if (spNamen[i].equals(roboOnTrack)) {
			trackRob(roboOnTrack);
		    }
		    statusLine.setRobStatus(spNamen[i],kCA.getRobStatus(spNamen[i]));
		}    
		for (int i = 0; i < statusLine.stat.getComponentCount(); i++) {
		    robsAnSpielfeld[i] = statusLine.sC[i].r;
		}
		Global.debug("Ausgabe: Habe"+robsAnSpielfeld.length+" Roboter an Spielfeld geschickt.");
		
		
		// --------- Neue Roboter-Position an Spielfeld senden ---------
		try {Thread.sleep(speed);} // Verzögerung der Ausgabegeschwindigkeit
		catch (Exception e) {System.err.println(e.getMessage());} 
		
		// --------- weitere Aenderungen einholen
		Global.debug("Ausgabe: Hole Spielstatus...");
		try {
		    Status[] stArray = kCA.getSpielstatus();
		    if (stArray != null) {
			statusLine.weitereStati(stArray);
			// Phase ausgeben
			if (stArray[0].aktPhase != lastPhase) {
			    setStatus(Message.say("AusgabeFrame","phase")+stArray[0].aktPhase);
			    lastPhase = stArray[0].aktPhase;
			}
		    }

		    // --------- hat schon jemand seine Zielfahne erreicht?
		    String[] spStand = kCA.getSpielstand();
		    
		    if (spStand != null) {
			Global.debug(this,"Es gibt schon Spieler, die am Ziel sind; hole Gewinnerliste...");
			for (int j = 0; j < spStand.length; j++) {
			    for (int i = 0; i < statusLine.sC.length; i++) {
				if (spStand[j].equals(statusLine.sC[i].r.getName())) {
				    statusLine.sC[i].setGewinnerNr(j+1);
				}
			    }
			}
		    }

		    
		}
		//	catch (KommException kE) {System.err.println(kE.getMessage());}
		catch (KommFutschException ke) {System.err.println("kE3: "+ke.getMessage());return;}
		catch (KommException kE) {System.err.println(kE.getMessage());}
		
		spielFeld.ersetzeRobos(robsAnSpielfeld);
		}
		catch (KommFutschException ke) {System.err.println("ke2: "+ke.getMessage());
		return;}
		catch (KommException kE) {System.err.println(kE.getMessage());}
		
	
		// Info-Requests beenden
		Global.debug(this, "Ausgabe: Sende Aenderung fertig an Server.");
		kCA.aenderungFertig();
		break;
	    }
	    case (cA.ENTFERNUNG): { 
		
		Global.debug(this,"Das Spiel ist beendet.");
		
		// Gewinnerliste ausgeben
		mP.setVisible(false);
		mP.removeAll();
		remove(mP);
		try {
		    String[] spielErgebnis = kCA.getSpielstand();
		    if (spielErgebnis != null) {
			Global.debug(this,"Es gibt Gewinner!");
			getContentPane().add(new Abspann(spielErgebnis),BorderLayout.CENTER);
			validate();
		    }
		    else Global.debug(this,"Die Gewinnerliste ist LEER!");
		}
		catch (KommException e) {System.err.println(e.getMessage());}
		try {
		    Thread.sleep(2000);
		}
		catch (InterruptedException e) {
		    System.err.println("AusgabeFrame: Interrupted by "+e.toString());
		}
		kCA.spielstart(); // Bestaetigung an den Server senden
		spielEnde = true;
	    }
	    }
	    
	    // ------- Einmaliges Holen des Spielfeldes und ermitteln der Spieler -----
	    if (!spielFeldErhalten) {

				// ------------- Spieler erfragen und den Status anlegen ------------
		try { 
		    Global.debug(this,"Versuche, Namen zu holen...");
		    String[] spNamen = kCA.getNamen();
		    String[] spColor = kCA.getFarben();
		    int nco=0;
		    for (int co=0;co<8;co++){
			if(!spColor[co].equals("0")) {
			    spNamen[nco] = spColor[co];
			    roboNcolor[nco]=robocolor[co];
			    nco++;
			}
		    }

		    Global.debug("Ausgabe: Hole Spielfeld...");
		    spielFeld = new SACanvas(new SpielfeldSim(kCA.getSpielfeldDim().x,
							      kCA.getSpielfeldDim().y,
							      kCA.getSpielfeld(),
							      kCA.getFahnenPos()),roboNcolor);
		    Global.debug("Ausgabe: Spielfeld erhalten und aktualisiert.");
		    spielFeldErhalten = true; // haben Spielfeld true-en
		    
		    // ------------- unnoetige entfernen -------------
		    for (int i = 8; i > spNamen.length; i--) statusLine.stat.remove(statusLine.stat.getComponentCount()-1);
		    Roboter robsAnSpielfeld[] = new Roboter[statusLine.stat.getComponentCount()];
		    
		    // --------- Status erzeugen  und RoboTrack-Menü erweitern  -----------------
		    JMenuItem trackItem = new JMenuItem(Message.say("AusgabeFrame","trackAus"));
		    trackItem.addActionListener( new ActionListener() {
			    public void actionPerformed(ActionEvent ae) {
				if (roboOnTrack != "") roboOnTrack="";
			    }
			});
		    optTrack.add(trackItem);
		    for (int i = 0; i < spNamen.length; i++) {
			statusLine.makeRobStatus(kCA.getRobStatus(spNamen[i]),i);
			trackItem = new JMenuItem(spNamen[i]);
			trackItem.addActionListener( new RoboTrackListener(statusLine.sC[i].r));
			optTrack.add(trackItem);
		    }    

		    spielFeld.addClickListener( this );

		    // ----------- Flaggen in das Menü eintragen
		    flagsPosition = kCA.getFahnenPos();
		    ActionListener flagListener = new ActionListener() {
			    public void actionPerformed(ActionEvent ae) {
				Global.debug(this,"Fahnenscroll gewünscht!, Command ist: "+ae.getActionCommand());
				scrollFlag(Integer.parseInt(ae.getActionCommand()));
			    }
			};
		    flagPos = new JMenuItem[flagsPosition.length];
		    for (int i = 0; i < flagsPosition.length; i++) {
			flagPos[i] = new JMenuItem(""+(i+1));
			flagPos[i].addActionListener(flagListener);
			scrollFlag.add(flagPos[i]);
		    }
		    
		    
		    // ------ Roboter an das Spielfeld senden und Listener aktivieren --------
		    for (int i = 0; i < statusLine.stat.getComponentCount(); i++) {
			robsAnSpielfeld[i] = statusLine.sC[i].r;
		    }
		    spielFeld.ersetzeRobos(robsAnSpielfeld); // Spielfeld die Robos geben
		}
		catch (KommException kE) {
		    System.err.println("Ausgabe: Beim Versuch, die Roboter zu holen, erhalte ich: "+
				       kE.getMessage());}
		catch (FormatException e) {System.err.println(e.getMessage());}
		catch (FlaggenException e){System.err.println(e.getMessage());}

      		// Fuege das Spielfeld ein
		//		mP.add(spielFeld);
		mP.setViewportView(spielFeld);
		mP.getViewport().setBackingStoreEnabled(true);
		spielFeld.setScrollPane(mP);
		mP.validate();
		kCA.spielstart(); // Bestaetigung an den Server senden
		Global.debug("Ausgabe: Bestaetigung des Spielstarts abgeschickt...");
		Global.debug("Ausgabe: Beende InfoRequests.");
		mP.repaint();
		// entferne das Splash-Screen
		if(!nosplash)warteSplashScreen.noSplash();
		
		setVisible(true);
		scrollFlag(1);

	    }
	}
	Global.debug(this,"Habe Ende der run()-Methode erreicht!");
	setStatus(Message.say("AusgabeFrame","spielende"));
	return;
    }


}



