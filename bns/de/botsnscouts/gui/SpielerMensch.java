package de.botsnscouts.gui;


import java.awt.*;
import java.io.*;
import java.net.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.metal.*;
/**
 * Diese Klasse uebernimmt die Funktion der Benutzerschnittstelle.
 * @author Lukasz Pekacki
 */
public class SpielerMensch extends Ausgabe {

    // -----------  Konstanten -----------
    static Class c = SpielerMensch.class;
    private final static  Image RUECK = Toolkit.getDefaultToolkit().getImage(c.getResource("images/karterueck.gif"));
    private final static  Image M1 = Toolkit.getDefaultToolkit().getImage(c.getResource("images/m1.gif"));
    private final static  Image M2 = Toolkit.getDefaultToolkit().getImage(c.getResource("images/m2.gif"));
    private final static  Image M3 = Toolkit.getDefaultToolkit().getImage(c.getResource("images/m3.gif"));
    private final static  Image BU = Toolkit.getDefaultToolkit().getImage(c.getResource("images/bu.gif"));
    private final static  Image RL = Toolkit.getDefaultToolkit().getImage(c.getResource("images/rl.gif"));
    private final static  Image RR = Toolkit.getDefaultToolkit().getImage(c.getResource("images/rr.gif"));
    private final static  Image UT = Toolkit.getDefaultToolkit().getImage(c.getResource("images/ut.gif"));
    private final static  Image RLEER = Toolkit.getDefaultToolkit().getImage(c.getResource("images/register-leer.gif"));


    private static final Color backColor = new Color(4,64,4);
    private static final Color foreColor = new Color(84,158,73);

    private final static int FREI = 0;
    private final static int BELEGT = 1;
    private final static int GELEGT = 1;
    private final static int GESPERRT = 2;

    static final int NORD = 0;
    static final int OST = 1;
    static final int SUED = 2;
    static final int WEST = 3;

    // Klassenvariablen
    private int myRobIndex;
    private boolean abgabeFertig;
    private boolean scoutAllowed=false;
    private boolean kartenErhalten = false;
    private boolean spielEnde = false;
    private boolean indexGefunden = false;
    private int globalTimeout = -1;
    private boolean scoutAktiv = false;
    private boolean klugAktiv = false;
    private int myColor =-1;
    private int reparaturPunkte;

    // Attribut-Klassen
    private KlugscheisserLatte klugscheisserLatte;
    private SpielfeldKS meinSpielfeld;
    private Permu wirbel;
    private UserInterfacePanel uI;
    private IstPowerDownPanel powerDownPanel;
    private GebeRichtung gR;
    private ZielfahneErreicht zErreichtGewinner;
    private ZielfahneErreicht zErreichtTot;
    private Panel userInterfaceContainer;
    private WiederPowerDown wPD;
    private ZuReparieren zuRepPanel;
    private CardLayout uICardLayout;
    private KommClientSpieler kCS;
    private KartenKlickListener KListener = new KartenKlickListener();
    private RegisterKlickListener rKL = new RegisterKlickListener();
    private SendeKartenListener sKL = new SendeKartenListener();
    private PowerDownListener   pDL = new PowerDownListener();
    private ClientAntwort cA = new ClientAntwort();

    // -----------  Konstruktoren  -----------
    public SpielerMensch(String host, int port, String name) {
	this(host,port,name,-1);
    }

    public SpielerMensch(){
	this ("localhost",8077,createName());
    }
    public SpielerMensch(String host, int port, String name, int color) {
	this (host, port, name, color, false);
    }
    public SpielerMensch(String host, int port, String name, int color, boolean nosplash) {
	super(host,port,nosplash);
	this.name = name;
	myColor=color;
	uICardLayout = new CardLayout();
	userInterfaceContainer = new Panel(uICardLayout) {
		public Dimension getMaximumSize() {
		    return new Dimension(220,550);
		}
		
		public Dimension getPreferredSize() {
		    return new Dimension(220,550);
	}
		
	    };
	uI = new UserInterfacePanel();
	gR = new GebeRichtung();
	wPD = new WiederPowerDown();
	powerDownPanel=new IstPowerDownPanel();
	zErreichtTot = new ZielfahneErreicht(Message.say("SpielerMensch","mkilled"),true);
	zErreichtGewinner= new ZielfahneErreicht(Message.say("SpielerMensch","mflagreached"),false);
	zuRepPanel = new  ZuReparieren();
	userInterfaceContainer.add(new JPanel(),"leer");
	userInterfaceContainer.add(gR,"richtung");
	userInterfaceContainer.add(zErreichtTot,"zieltot");
	userInterfaceContainer.add(zErreichtGewinner,"zielgewinner");
	userInterfaceContainer.add(uI,"karten");
	userInterfaceContainer.add(wPD,"wiederpowerdown");
	userInterfaceContainer.add(zuRepPanel,"zureparieren");
	userInterfaceContainer.add(powerDownPanel,"istpowerdown");
	f.setTitle(Message.say("SpielerMensch","gameName")+" - "+name);
	f.getContentPane().add(userInterfaceContainer,BorderLayout.EAST);
	// Menüleiste erweitern
	kCS = new KommClientSpieler();



	// Klugscheisser-Latte einsetzen
	/*
	f.statusLine.kSC.addMouseListener(klugScheisserKlickListener);
	f.statusLine.SkSC.addMouseListener(klugScheisserKlickListener);
	f.statusLine.Kschlaf.add(f.statusLine.SkSC);
	*/
	// Scout-Vertiefung einsetzen
	/*	f.statusLine.sSC.addMouseListener(scoutKlickListener);
	f.statusLine.SsSC.addMouseListener(scoutKlickListener);
	f.statusLine.Sschlaf.add(f.statusLine.SsSC);
	*/
	klugscheisserLatte = new KlugscheisserLatte(this);
	f.statusLine.schlaf.add(klugscheisserLatte);
	f.statusLine.schlaf.add(new ScoutVertiefung(this));
	f.statusLine.add(f.statusLine.schlaf);
    }

    // -----------  Klassenmethoden  -----------

    private void sendeReparaturWunsch() {
		int[] repa = new int[reparaturPunkte];
		for (int i = 0; i < repa.length; i++) {
		    repa[i] = uI.regZuReparieren[i]; 
		    uI.register[(repa[i]-1)].status = FREI;
		}
		kCS.respReparatur(name,repa);
		setStatus(Message.say("SpielerMensch","sendregrep"));
		uICardLayout.show(userInterfaceContainer,"leer");
    }
    

    private void sendeWunschRichtung(int r) {
		kCS.respZerstoert(name,r); 
    }


    private void wiederPowerDown(boolean down) {
		kCS.respReaktivierung(name,down);
		// enferne die Power-Down-Markierung
		if (!down) {
		uICardLayout.show(userInterfaceContainer,"leer");
		}
		else {
		uICardLayout.show(userInterfaceContainer,"istpowerdown");
		}

    }



    /**
     * Wartet auf einen Menuklick
     */
    public void klugscheisserClicked(boolean klugscheisserWecken) {
	    klugAktiv = klugscheisserWecken;
	    if(klugscheisserWecken) {
		//		Global.debug(this,"Klugscheisser wach auf!");
		// An alle petzen
		String[] tmp=new String[1];
		tmp[0]=name;
		kCS.message("mKlugSchKlick",tmp);
		// Benutze den Klugscheisser
		fragKlug();
	    }
	    else {
		//		Global.debug("Klugscheisser geh schlafen...");
		wegMitKlug();
	    }
	    
    }
	
    


    public void scoutClicked(boolean scoutWecken) {
	    scoutAktiv = scoutWecken;
	    //	    Global.debug(this,"Was soll der Scout sein? : "+scoutAktiv);
	    // Wechsle die Canvases
	    if(scoutWecken) {
		//		Global.debug(this,"Scout, wach auf!");
		if(!spielEnde) doScout();
	    }
	    else {
		//		Global.debug(this,"Scout, geh schlafen!");
		if(!spielEnde) removeScout();
	    }
    }
    
    



    /**
     * Methode, die sich einen eindeutigen Namen für den Ausgabekanal ausdenkt
     **/
    private static String createName(){
	return KrimsKrams.randomName(); // liefert einen Phantasienamen
    }





    /**
     * Setzt den Scout für das Feld, das den gelegten Karten entspricht
     **/
    private void doScout() {
	if (scoutAktiv) {
	    // ------- Scout tanzen lassen
	    Roboter[] doPhaseRob = new Roboter[1];
	    doPhaseRob[0] = new Roboter("blafasel");
	    doPhaseRob[0] = new Roboter(f.statusLine.sC[myRobIndex].r);
	    doPhaseRob[0].zeige_Roboter();
	    int moeglichePhasen = 0;
	    for (int j = 0; j < uI.register.length;j++) {
		if (uI.register[j].status == FREI) break;
		moeglichePhasen++;
	    }

	    // ------- belegte Register in den Robbi einsetzen
	    for (int m = 0; m < moeglichePhasen; m++) {
		Karte ka = new Karte(uI.register[m].kartePrio,uI.register[m].karteName);
		doPhaseRob[0].setZug(m, ka);
	    }
	    doPhaseRob[0].zeige_Roboter();
	    f.spielFeld.vorschau(moeglichePhasen,doPhaseRob);
	}
    }


    /**
     * Setzt den Scout für das Feld, das den gelegten Karten entspricht
     **/
    private void removeScout() {
	// -------- entferne Scout 
	Roboter[] doPhaseRob = new Roboter[1];

	doPhaseRob[0] = new Roboter(f.statusLine.sC[myRobIndex].r);
	doPhaseRob[0].zeige_Roboter();
	int moeglichePhasen = 0;
	f.spielFeld.vorschau(moeglichePhasen,doPhaseRob);
    }
    


    // aktivert den Klugscheisser
    private void fragKlug() {
	KlugKarte[] kk = new KlugKarte[9];
	Roboter r = new Roboter(name);
	try {
	    r = kCS.getRobStatus(name);
	}
	catch(KommException e){
	    System.err.println("Keinen Robi bekommen!"+e);
	}
	// Karte-Canvas in kk einlesen, gesperrte Karten werden nicht berücksichtigt
	int j=0;
	for (int i = 0; i < 9; i++) {
	    if(uI.karten[i].status == FREI) {
		kk[j] = new KlugKarte(uI.karten[i]);
		j++;
	    }
	}
	// gelegte Karten in das enstprechende gesperrte Register des Robis packen
	for (int l = 0; l<5;l++) {
	    if (!uI.register[l].istFrei()) {
		r.sperreRegister(l,  new Karte(uI.register[l].kartePrio,uI.register[l].karteName));
	    }
	}
	// gesperrte Register in r.zug schreiben
	for (int i = 0; i < r.getGesperrteRegister().length; i++)
	    r.setZug(i, r.getGesperrteRegister(i));

	meinSpielfeld.debugmeldungen=false;
	Karte[] vonPermut = wirbel.permutiere(kk, r);
	meinSpielfeld.debugmeldungen=true;

	// in vonPermut steht die vorgeschlagene Registerprogrammierung, so wie sie ggf. z.T.schon
	// in den Registern steht
	int nextprio =-1;
	for (int su=0; su<5;su++) 
	    if ((uI.register[su].kartePrio) != (vonPermut[su].getprio())){
                nextprio = vonPermut[su].getprio();
                break;
            }
	
	for (int ka=0; ka<9;ka++) 
            if (uI.karten[ka].prio == nextprio) {uI.karten[ka].setKlug(true);break;}
    }

    // entfernt den Klugscheisser
    private void wegMitKlug() {
	for (int weg=0; weg<9;weg++) 
	    if(uI.karten[weg].klugScheisser) uI.karten[weg].setKlug(false); 
	// f.statusLine.Kschlaf.removeAll();
	// f.statusLine.Kschlaf.add(f.statusLine.SkSC);
	//	klugAktiv = false;
	
    }

    // ------------ Innere Klassen -------------


    /**
     * Wartet auf einen Mausklick auf einer ausgeteilten Karte
     */
    private class KartenKlickListener extends MouseAdapter {

	public void mousePressed (MouseEvent e) {
	    // möglichen Klugscheisser von der Karte wegjagen
	    for (int i = 0; i < 9; i++) {
		uI.karten[i].setKlug(false);
	    }

	    KarteCanvas k = (KarteCanvas) e.getSource();
	    for (int i = 0; i < 5; i++) {
		if ((k.status != GELEGT) && (uI.register[i].status==FREI)) {
		    uI.register[i].setzeKarte(k);

		    k.umdrehen();
		    // lass den Scout schon mal vorgehen
		    if(scoutAktiv) doScout();
		    if(klugAktiv) fragKlug();
		    // Absende-Button anzeigen/verstecken
		    boolean alleBelegt = true;
		    for (int j = 0; j < 5; j++) if (uI.register[j].status == FREI) {alleBelegt = false; break;}
		    if (alleBelegt) {
			uI.uP.absenden.setVisible(true);
		    }
		    else uI.uP.absenden.setVisible(false);
		    break;
		}
	    }
	}

    }
    
    /**
     * Wartet auf einen Mausklick auf einem Register
     */
    private class RegisterKlickListener extends MouseAdapter {

	public void mousePressed (MouseEvent e) {
	    //	     möglichen Klugscheisser von der Karte wegjagen
	    	    for (int i = 0; i < 9; i++) {
	    		uI.karten[i].setKlug(false);
	    	    }
	    RegisterCanvas r = (RegisterCanvas) e.getSource();
	    if (r.status != GESPERRT && r.status != FREI){
		uI.karten[r.karteAusteilNum-1].zurueckdrehen();
		r.resetRegister();
		// lass den Scout schon mal vorgehen
		if(scoutAktiv) doScout();
		if(klugAktiv) fragKlug();
		// Absende-Button anzeigen/verstecken
		boolean alleBelegt = true;
		for (int j = 0; j < 5; j++) if (uI.register[j].status == FREI) {alleBelegt = false; break;}
		if (alleBelegt) uI.uP.absenden.setVisible(true);
		else uI.uP.absenden.setVisible(false);
	    }
	}
    }

    /**
     * Wartet auf Betaetigung des Senden-Buttons
     */
    private class SendeKartenListener implements ActionListener {
	public void actionPerformed (ActionEvent ae) {
	    // sind alle Register belegt -> Karten abgeben
	    if (!abgabeFertig) {
		boolean alleBelegt = true;
		for (int i = 0; i < 5; i++) if (uI.register[i].status == FREI) {alleBelegt = false; break;}
		if (alleBelegt) {
		    setStatus(Message.say("SpielerMensch","kartenabgabe"));
		    for (int i = 0; i < 5; i++) uI.register[i].gesandtRegister();
		    for (int i = 0; i < 9; i++) uI.karten[i].umdrehen();
		    abgabeFertig = true;
		}
	    }
	}
    }

    /**
     * Wartet auf Betaetigung der Power-Down-Checkbox
     */
    private class PowerDownListener implements ItemListener {
	public void itemStateChanged (ItemEvent e) {
	    uI.powerDown= ((JCheckBox) e.getSource()).getModel().isSelected();
	}
    }

    /**
     * Verwaltet und stellt dar die Benutzerschnittstelle
     */    
    private class UserInterfacePanel extends JPanel {

	KarteCanvas karten[] = new KarteCanvas[9];
	RegisterCanvas register[] = new RegisterCanvas[5];
	UserPanel uP = new UserPanel();
	Label timeout;

	boolean powerDown = false;
	int wunschRichtung = -1;
	int dummy = 0;

	int regZuReparieren[] = new int[5];

	public UserInterfacePanel(){

	    setLayout(new GridLayout(5,3));
	    // Karten und Register auf dem Panel anordnen
	    int j = 0;
	    for (int i = 0; i < 5; i++) {
		register[i] = new RegisterCanvas(i);
		add(register[i]);
		if ( i < 4) j = 2; else j = 1;
		for (int k = 0; k < j; k++) {
		    karten[(2*i)+k] = new KarteCanvas((2*i)+k);
		    add(karten[(2*i)+k]);
		}
	    }
	    add (uP);

	}


	/**
	 * Ereugt die Registerprogrammierung, die an den Server gesandt wird
	 */
	public int[] getProg() {
	    int gesperrteRegister = 0;
	    for (int i = 0;i < 5; i++) if (register[i].status == GESPERRT) gesperrteRegister++;

	    int[] prog = new int[(5-gesperrteRegister)];
	    int j = 0;
	    for (int i = 0; i < 5; i++) {
		if (register[i].status != GESPERRT) {
		    prog[j] = register[i].karteAusteilNum; 
		    j++; }
	    }
	    return prog;
	}

    }

    /**
     * Kuemmert sich um die Absenden, Ende und PowerDown buttons
     */
    private class UserPanel extends JPanel {
	JButton absenden;
	JCheckBox powerDownBox;
	
	UserPanel() {
	    setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));		
	    powerDownBox = new JCheckBox(Message.say("SpielerMensch","powerdown"),false);
	    powerDownBox.setFont(new Font("Sans",0,8));
	    powerDownBox.addItemListener(pDL);
	    add(powerDownBox);
	    add(Box.createVerticalStrut(20));
	    // ------ Sende Button anlegen
	    absenden = new JButton(Message.say("SpielerMensch","senden"));
	    //	    absenden.setEnabled(true);
	    absenden.addActionListener(sKL);
      	    absenden.setVisible(false);
	    add(absenden);
	}
	

    }

    /**
     * Klugscheisser-Karte, die auf ein KartenCanvas verweist
     */
    private class KlugKarte extends Karte{
	KarteCanvas k;
	public KlugKarte(KarteCanvas kin){
	    super(kin.prio,kin.ktyp);
	    k = kin;
	}
    }
    /**
     * Verwaltet und stellt dar: eine Karte
     */
    private class KarteCanvas extends GrafikComponent {
	int status, prio, bornprio, austeilNum;
	int num;
	String ktyp;
	Image born;
	boolean klugScheisser = false;

	KarteCanvas (int n) {
	    super(RUECK);
	    num = n;
	    prio = 0;
	    bornprio = prio;
	    status = FREI;
	    born = img;
	}
	

	public void paint(Graphics g)
	{
	    g.drawImage(img,0,0,this);
	    g.setFont(new Font("SansSerif",0,8));
	    g.setColor(Color.darkGray);
	    if ((status == FREI) && (prio>0)) g.drawString(""+prio,23,19);
	    if (klugScheisser) g.drawImage(Images.KSCHEISSER,25,42,this);
	}

	/**
	 * Hat der Benutzer diese Karte in ein Register gelegt, dann wir diese Methode aufgerufen
	 * Sie "dreht" die Karte auf den Ruecken und markiert sie als "gelegt".
	 */	public void umdrehen() {
	     img = RUECK;
	     //	     prio = 0;
	     if (klugScheisser) klugScheisser=false;
	     status = GELEGT;
	     repaint();
	 }

	/**
	 * Wurden Karten < 9 ausgeteilt, dann wir diese Methode aufgerufen
	 */  public void sperren() {
	     born = RUECK;
	     bornprio = 0;
	     img = RUECK;
	     prio = 0;
	    
	     status = GESPERRT;
	     repaint();
	 }

	/**
	 * Hat der Benutzer fuenf Register belegt und klickt nicht auf Absenden, sondern auf eine beliebige Karte
	 * dann setzt diese Methode eine Karte auf den Austeilungszustand zurueck
	 */
	public void zurueckdrehen(){
	    img = born;
	    prio = bornprio;
	    status = FREI;
	    repaint();
	}

	/**
	 * Setzt neu augeteilte Karte ein
	 */
	public void neueKarte(Karte k, int austeilNummer)
	{
	    prio = (10*k.getprio());
	    bornprio = prio;
	    austeilNum = (austeilNummer+1);
	    ktyp = k.getaktion();
	    if (ktyp.equals("M1")) born = M1;
	    else if (ktyp.equals("M2")) born = M2;
	    else if (ktyp.equals("M3")) born = M3;
	    else if (ktyp.equals("BU")) born = BU;
	    else if (ktyp.equals("RL")) born = RL;
	    else if (ktyp.equals("RR")) born = RR;
	    else if (ktyp.equals("UT")) born = UT;
	    img = born;
	    status = FREI;
	    repaint();
	}
	public void setKlug(boolean b){
	    klugScheisser = b;
	    repaint();
	}
    }
 
    /**
     * Verwaltet und stellt dar die Register
     */
    private class RegisterCanvas extends GrafikComponent{
	int status = 0, bornnum, num;
	boolean gesandt = false;
	protected int karteNum, kartePrio, karteAusteilNum;
	protected String karteName;
	RegisterCanvas(int n){
	    super(RLEER);
	    num = n+1;
	    bornnum = num;
	}

	public void paint(Graphics g)
	{
	    g.drawImage(img,1,1,this);
	    if (num > 0) {
		g.setFont(new Font("SansSerif",0,10));
		g.drawString(Message.say("SpielerMensch","register"),13,40);
		g.drawString(""+num,28,60);
	    }
	    if (kartePrio > 0) {
		g.setFont(new Font("SansSerif",0,8));
		g.setColor(Color.darkGray);
		g.drawString(""+kartePrio,23,19);}
	    if (status == GESPERRT) {
		g.setFont(new Font("SansSerif",Font.BOLD,10));
		g.setColor(Color.red);
		g.drawString(Message.say("SpielerMensch","gesperrt"),5,44);
	    }
	}

	/**
	 * Ist das Register noch frei?
	 */
	public boolean istFrei() {return (status == FREI);}

	/**
	 * Karte in das Register programmieren
	 */
	public void setzeKarte(KarteCanvas k){
	    gesandt=false;
	    img = k.img;
	    kartePrio = k.prio;
	    status = BELEGT;
	    karteNum = k.num;
	    karteAusteilNum = k.austeilNum;
	    karteName = k.ktyp;
	    num = 0;
	    repaint();
	}

	/**
	 * Register auf leer zuruecksetzen
	 */
	public void resetRegister(){
	    gesandt=false;
	    if (status != GESPERRT) {
		img = RLEER;
		num = bornnum;
		kartePrio = 0;
		status = FREI;
		repaint();
	    }
	}

	/**
	 * Register auf leer zuruecksetzen
	 */
	public void gesandtRegister(){
	    gesandt = true;
	    repaint();
	}

	/**
	 * Register sperren
	 */
	public void sperren(){
	    status = GESPERRT;
	    repaint();
	}
    }

    /**
     * Fenster, das auf die Zerstoerungserungs-Meldung des Servers fragt, in welche Richtung der Robo kucken soll.
     */
    protected class GebeRichtung extends JPanel implements ActionListener {
	private int ergebnis = NORD;


	public GebeRichtung() {
	    setLayout(new BorderLayout());
	    
	    JLabel titel = new JLabel(Message.say("SpielerMensch","richtungwahl"));
	    add(titel,BorderLayout.NORTH);
	    TJPanel p = new TJPanel();
	    p.setBorder(new javax.swing.border.EtchedBorder(4));
	    p.setLayout(new GridLayout(3,3));
	    p.setSize(200,200);
	    //	    p.setBackground(Color.lightGray);
	    p.add(Box.createGlue());
	    PfeilC no = new PfeilC("NORD",NORD);
	    no.addActionListener(this);
	    p.add(no);
	    p.add(Box.createGlue());
	    PfeilC we = new PfeilC("WEST",WEST);
	    we.addActionListener(this);
	    p.add(we);
	    p.add(new JButton(new ImageIcon((ImageMan.getImages(ImageMan.SCOUT))[2])));
	    PfeilC os = new PfeilC("OST",OST);
	    os.addActionListener(this);
	    p.add(os);
	    p.add(Box.createGlue());
	    PfeilC su = new PfeilC("SUED",SUED);
	    su.addActionListener(this);
	    p.add(su);
	    p.add(Box.createGlue());
	    JPanel pp = new JPanel();
	    pp.add(p);
	    add(pp, BorderLayout.CENTER);
	}


	public void actionPerformed(ActionEvent e){
	    uICardLayout.show(userInterfaceContainer,"leer");
	    sendeWunschRichtung(((PfeilC) e.getSource()).richt);
	}
	
	private class PfeilC extends JButton {
	    private int richt = NORD;
	    PfeilC(String s, int r){
		this.setOpaque(false);
		richt = r;
	    }

	    public Dimension getPreferredSize() {
		return new Dimension(60,60);
	    }

	    public Dimension getMinimumSize() {
		return new Dimension(60,60);
	    }

	    public void paintComponent(Graphics g){
		g.setColor(Color.lightGray);
		g.setColor(this.getForeground());
		switch(richt){
		case(NORD):{
		    int[] x = {5,55,30};
		    int[] y = {55,55,5};
		    g.fillPolygon(x,y,3);
		    break;
		}
		case(SUED):{
		    int[] x = {5,55,30};
		    int[] y = {5,5,55};
		    g.fillPolygon(x,y,3);
		    break;
		}
		case(SpielerMensch.WEST):{
		    int[] x = {55,55,5};
		    int[] y = {5,55,30};
		    g.fillPolygon(x,y,3);
		    break;
		}
		case(OST):{
		    int[] x = {5,55,5};
		    int[] y = {5,30,55};
		    g.fillPolygon(x,y,3);
		    break;
		}

		}
	    }
	}
    }



    protected class IstPowerDownPanel extends JPanel {
	public IstPowerDownPanel(){
	    setBorder(new EmptyBorder(0,40,0,0));
	    add(Box.createVerticalStrut(300));
	    BoxLayout b = new BoxLayout(this,BoxLayout.Y_AXIS);
	    setLayout(b);
	    //	    ImageIcon im = new ImageIcon("./de/spline/rr/images/zzz.gif");
	    //	    add(new JLabel(im));
	    JLabel l = new JLabel(Message.say("SpielerMensch","istPowerDown"));
	    l.setForeground(Color.red);
	    add(l);
	}
    }
    



    /**
     * Fenster, das nach der Reaktivierung fragt, ob noch ein PowerDown gewuenscht wird
     */
    protected class WiederPowerDown extends JPanel implements ActionListener{

	private JButton wieder;
	private JButton weiter;

	public WiederPowerDown(){
	    setBorder(new EmptyBorder(0,10,0,0));
	    add(Box.createVerticalStrut(40));
	    BoxLayout b = new BoxLayout(this,BoxLayout.Y_AXIS);
	    setLayout(b);
	    JLabel titel = new JLabel(Message.say("SpielerMensch","roboreaktwtitle"));

	    add(titel);
	    JLabel unter = new JLabel(Message.say("SpielerMensch","powerdownwieder"));

	    add(unter); 
	    add(Box.createVerticalStrut(10));
	    wieder=new JButton(Message.say("SpielerMensch","wiederPowerFrage"));
	    wieder.addActionListener(this);
	    add(wieder);
	    add(Box.createVerticalStrut(10));
	    weiter=new JButton(Message.say("SpielerMensch","weiterspielen"));
	    weiter.addActionListener(this);
	    add(weiter);
	}


	public void actionPerformed(ActionEvent e)
	{
	    wiederPowerDown((e.getSource() == wieder));
	    if (e.getSource() == weiter) {
	    uICardLayout.show(userInterfaceContainer,"leer");
	    }

	}
    }



    /**
     * Fenster, das nach der Reaktivierung fragt, ob noch ein PowerDown gewuenscht wird
     */
    protected class ZuReparieren extends JPanel implements ActionListener {
	private JButton fertig;
	private JLabel titel;

	JCheckBox cb1;
	JCheckBox cb2;
	JCheckBox cb3;
	JCheckBox cb4;
	JCheckBox cb5;
	private int zuVerteilen=0;



	public ZuReparieren() {
	    setBorder(new EmptyBorder(10,10,10,10));
	    setLayout(new GridLayout(9,1));
	    cb1 = new JCheckBox(Message.say("SpielerMensch","mcregister","1"),false);
	    cb1.setEnabled(false);
	    cb1.addActionListener(this);
	    add(cb1);
	    cb2 = new JCheckBox(Message.say("SpielerMensch","mcregister","2"),false);
	    cb2.setEnabled(false);
	    cb2.addActionListener(this);
	    add(cb2);
	    cb3 = new JCheckBox(Message.say("SpielerMensch","mcregister","3"),false);
	    cb3.setEnabled(false);
	    cb3.addActionListener(this);
	    add(cb3);
	    cb4 = new JCheckBox(Message.say("SpielerMensch","mcregister","4"),false);
	    cb4.setEnabled(false);
	    cb4.addActionListener(this);
	    add(cb4);
	    cb5 = new JCheckBox(Message.say("SpielerMensch","mcregister","5"),false);
	    cb5.setEnabled(false);
	    cb5.addActionListener(this);
	    add(cb5);

	    add(new JLabel(""));
	    titel = new JLabel(Message.say("SpielerMensch","mregwahl",reparaturPunkte)+" SIND:"+reparaturPunkte);
	    add(titel);

	    fertig=new JButton(Message.say("SpielerMensch","ok"));
	    fertig.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent e)
		    {
			int i = 0;
			if (cb1.getModel().isSelected()) {
			    cb1.getModel().setSelected(false);
			    uI.regZuReparieren[i]=1; i++;
			}
			if (cb2.getModel().isSelected()) {
			    cb2.getModel().setSelected(false);
			    uI.regZuReparieren[i]=2; i++;
			}
			if (cb3.getModel().isSelected()) {
			    cb3.getModel().setSelected(false);
			    uI.regZuReparieren[i]=3;i++;
			}
			if (cb4.getModel().isSelected()) {
			    cb4.getModel().setSelected(false);
			    uI.regZuReparieren[i]=4;i++;
			}
			if (cb5.getModel().isSelected()) {
			    cb5.getModel().setSelected(false);
			    uI.regZuReparieren[i]=5;i++;
			}
			sendeReparaturWunsch();
			
		    }
		});
	    add(fertig);
	    setVisible(true);
	}

	private void zeigeAuswahl() {	
	    zuVerteilen = reparaturPunkte;
	    titel.setText(Message.say("SpielerMensch","mregwahl",reparaturPunkte)+" SIND:"+reparaturPunkte);

	    if(uI.register[0].status == GESPERRT) {
		cb1.setEnabled(true);
	    }
	    else {
		cb1.setEnabled(false);
	    }
	    if(uI.register[1].status == GESPERRT){
		cb2.setEnabled(true);
	    }
	    else {
		cb2.setEnabled(false);
	    }
	    if(uI.register[2].status == GESPERRT){
		cb3.setEnabled(true);
	    }
	    else {
		cb3.setEnabled(false);
	    }
	    if(uI.register[3].status == GESPERRT){
		cb4.setEnabled(true);
	    }
	    else {
		cb4.setEnabled(false);
	    }
	    if(uI.register[4].status == GESPERRT){
		cb5.setEnabled(true);
	    }
	    else {
		cb5.setEnabled(false);
	    }
	}

	public void actionPerformed (ActionEvent e) {
	    if ( ((JCheckBox) e.getSource()).getModel().isSelected()) { 
		if (zuVerteilen > 0) { 
		    zuVerteilen--; 
		}
		else { 
		    ((JCheckBox) e.getSource()).getModel().setSelected(false);
		}
	    }
	    else if (zuVerteilen < reparaturPunkte) {
		zuVerteilen++;
	    }
	}
    }
    /**
     * erzeugt mit der Spielfelddimension, den Fahnenpositionen und dem 
     * Spielfeldstring das Spielfeld des kuenstlichen Spielers, ruft 
     * ausserdem die Entfernungsberechnung in SpielfeldKS auf
     */ 
    private void spielfeldkreieren()
    {
	int dimx, dimy;
	Ort dimension;
	try{
	    dimension=kCS.getSpielfeldDim();
	    dimx=dimension.x;
	    dimy=dimension.y;
	    Ort[] fahnen=kCS.getFahnenPos();
	    String spielfeldstring=kCS.getSpielfeld();
	    try{
		meinSpielfeld = SpielfeldKS.getInstance(dimx,dimy,spielfeldstring,fahnen);
	    }
		
	    catch(Exception e){
		System.err.println("Kein Spielfeld bekommen!"+e);
	    }			
	}
	catch(Exception e){
	    System.err.println("Kein Spielfeld bekommen!"+e);
	}
    }
    
    /** meldet den Spieler beim Server ab und beendet diesen Thread.
	(TODO)
    */
    protected void abmelden() {
	Global.debug(this, "Roboter "+name+" meldet sich ab.");
	kCS.abmelden(name);
	//Dafuer sorgen, dass Thread aufhoert 
	spielEnde = true; //War's das etwa?
    }

    /**
     * Start des Menschlichen Spielers
     */ 
    public void run(){

	// ------- Anmeldung am Server -------
	boolean anmeldungErfolg = false;
	int versuche = 0;
	//	setStatus("Anmeldung am Server...");
	while ((!anmeldungErfolg)&&(versuche < 5)) {
	    Global.debug(this,(versuche+1) + ".ter Versuch an "+host+" an Port "+port);
	    try {sleep(500);} catch (Exception e) {}
	    
	    // hies früher - ohne Farbe
	    try{
		anmeldungErfolg = kCS.anmelden2(host,port,name,myColor);
	    } 
	    catch (KommException kE){
		Global.debug(this,kE.getMessage()); 
	    } 
	    versuche++; 
	    try {sleep(100);} catch (Exception e) {}
	}
	if (!anmeldungErfolg) {
	    Global.debug(this,"Beende Versuche, mich anzumelden.");  
	    return;
	    
	}

	// ----------- Ausgabe als Thread starten
	frameThread = new Thread(f); // Ausgabeframe als Thread erzeugen
	frameThread.start(); // Ausgabe starten -> meldet sich selbstaendig an

	// ----------- Zuhoeren, ob der Spieler beim naechsten Mal ein Power Down wuenscht
	uI.uP.powerDownBox.addItemListener(pDL);




	// ------- einstieg in die grosse Schleife -------
	while (!spielEnde) {

	    // ------- Warten auf Server-Meldungen  -------
	    try {
		cA = kCS.warte();
	       		Global.debug(this,"Server sendet mir : " + cA.getTyp());
	    }
	    catch (KommException kE) {
		Global.debug(this,kE.getMessage());
	    }
	    if (isInterrupted()) return;

	    // ------  Servermeldung behandeln ---------
	    switch (cA.typ) {

				// Servermeldung: Karten wurden ausgeteilt
	    case (cA.MACHEZUG): {
		Global.debug(this,"Ich bekomme MACHEZUG");
		uICardLayout.show(userInterfaceContainer,"karten");
		klugscheisserLatte.reset();
		klugAktiv=false;
		setStatus(Message.say("SpielerMensch","mwartereg"));
				// Button deaktivieren
		uI.uP.absenden.setVisible(false);
		uI.uP.powerDownBox.getModel().setSelected(false);
		uI.powerDown=false;

				// --------- Index des eigenen Robbis herausfinden
		if (!indexGefunden) {
		    for (int i = 0; i < f.statusLine.stat.getComponentCount(); i++)
			if (f.statusLine.sC[i].r.getName().equals(name)) {
			    indexGefunden = true;
			    myRobIndex = i;
			    break;
			}
		}

				// ---------- gesperrte Register nachfragen
		try{
		   Global.debug(this,"Versuche, Robstatus von "+name+" zu bekommen.");
		    Roboter tempRob = kCS.getRobStatus(name);
		    // Absende-Button akitvieren, wenn alle Register belegt sind.
		    if 	(tempRob.getGesperrteRegister().length==5){
			uI.uP.absenden.setVisible(true);
		    }
		    for (int i=0; i < tempRob.getGesperrteRegister().length; i++) {
			if (tempRob.getGesperrteRegister(i) != null) {
			    uI.register[i].sperren();
			    Global.debug(this,"Server sagt: Register gesperrt: "+(i+1));
			    Global.debug(this,"Status von Register: "+(i+1)+" ist: "+uI.register[i].status);

			}
			else {uI.register[i].status=FREI; uI.register[i].resetRegister(); }
		    }
		}
		catch (KommException kE) {
		    System.err.println("SpielerMenschERROR: "+kE.getMessage());
		}

				// ----- Karten einsortieren  -----
		for (int i = 0; i < cA.karten.length; i++) {
		    uI.karten[i].neueKarte(cA.karten[i],i);
		    uI.karten[i].addMouseListener(KListener);
		}

				// ----- Karten umdrehen --------
		for (int i = cA.karten.length; i < 9; i++) uI.karten[i].umdrehen(); 

		for (int i = 0; i < 5; i++) uI.register[i].addMouseListener(rKL);
		scoutAllowed=true;

		// --- ist der Klugscheisser aktiv?
		if (klugAktiv) fragKlug();

				// ----- Abgabe der Programmierung -----
		abgabeFertig = false; // Kartenabgeabe fuer das naechste Mal zureucksetzen
		int temptimeout = globalTimeout-5;
		while ((!abgabeFertig) && (temptimeout > 0)) {
		    try { 
			sleep(1000);
			temptimeout--;

			if(temptimeout < 15) setStatus(
						       Message.say("SpielerMensch","zeitAblauf")
						       +temptimeout+ Message.say("SpielerMensch","sekunden")
						       );
		    } catch (Exception e) {System.err.println(e.getMessage());}		
		}

				// -------- falls Timeout -> gueltigen Zug abgeben
		if (temptimeout == 0) {
		    setStatus(Message.say("SpielerMensch","legalZug"));
		    int gesperrteRegister = 0;
		    for (int i = 0;i < 5; i++) if (uI.register[i].status == GESPERRT) gesperrteRegister++;
		    int[] prog = new int[(5-gesperrteRegister)];
		    for (int i = 0; i < prog.length; i++) prog[i] = (i+1);
		    kCS.registerProg(name,prog,false);
		}
		else {
		kCS.registerProg(name,uI.getProg(),uI.powerDown);
		}

		// setze die Power-Down-Markierung, falls nötig
		if (uI.powerDown) {
		    uICardLayout.show(userInterfaceContainer,"istpowerdown");
		}
		
		// Button deaktivieren
		uI.uP.absenden.setVisible(false);

				// ------- Mouse Listener entfernen  -------
		scoutAllowed=false;
		for (int i = 0; i < uI.karten.length; i++) uI.karten[i].removeMouseListener(KListener); 
		for (int i = 0; i < 5; i++) uI.register[i].removeMouseListener(rKL);

				// ------- PowerDown Box auf false setzen ----------
		uI.uP.powerDownBox.getModel().setSelected(false);

		break;
	    }
		
	    // Servermeldung: Spielbegin
	    case (cA.SPIELSTART): {
		setStatus(Message.say("SpielerMensch","spielgehtlos"));
		kCS.spielstart(); // ok an den Server senden
		break;
	    }

	    // Servermeldung: Robter zerstoert (bzw. bei Spielbegin aufs Feld gesetzt)
	    case (cA.ZERSTOERUNG): {
		uICardLayout.show(userInterfaceContainer,"richtung");
		Global.debug(this,"Habe einer Zerstörung bekommen.");
		setStatus(Message.say("SpielerMensch","roboauffeld"));
		// --- Spielfeld für den Klugscheisser holen
		if (meinSpielfeld==null) {
		    spielfeldkreieren();
		    wirbel = new Permu(meinSpielfeld,0);
		}
	       	// ----- Timeout erfragen -------
		if (globalTimeout < 0) {
		    try {
			globalTimeout = kCS.getTimeOut();
		    }
		    catch (KommException kE) {
			System.err.println("SpielerMenschKommunkationsERROR: wollte Timeout erfragen: "+kE.getMessage());
		    }
		}
		Global.debug(this,"Timeout ist: "+globalTimeout);
		//----- Ausrichtung abfragen und senden ----
		
		uI.wunschRichtung = -1;
		break;
	    }


	    // Servermeldung: Robter nach Power-Down reaktiviert
	    case (cA.REAKTIVIERUNG): { 
		setStatus(Message.say("SpielerMensch","roboreaktiviert"));
				// -------- Nochmaliges PowerDown abfragen und senden ------
		uICardLayout.show(userInterfaceContainer,"wiederpowerdown");
		break;
	    }

	    // Servermeldung: Register wurden Repariert
	    case (cA.REPARATUR):{ 
		    Global.debug(this,"Reparatur erhalten");

				// ---------- gesperrte Register nachfragen

		try {
		    Global.debug(this,"Reparatur erhalten; ersuche, Status von "+name+"  zu erfragen...");
		    Roboter tempRob = kCS.getRobStatus(name);

		    // Anzahl der Reparaturpunkte
		    reparaturPunkte = cA.zahl;


		    for (int i=0; i < tempRob.getGesperrteRegister().length; i++) {
			if (tempRob.getGesperrteRegister(i) != null) {

			    uI.register[i].sperren();
			    Global.debug(this,"Server sagt: Register gesperrt: "+i);
			}
			else {
			    uI.register[i].status=FREI; 
			uI.register[i].resetRegister(); 
			}
		    }
		
		}
		catch (KommException kE) {
		System.err.println("SpielerMensch: "+kE.getMessage());
		}

		  for (int i = 0; i < uI.register.length; i++) 
		  Global.debug(this,"SpM: Register "+i+" hat Status: "+uI.register[i].status);	

		setStatus(Message.say("SpielerMensch","registerrep"));
		Global.debug(this,"Anzahln der Reparaturpunkte: "+reparaturPunkte);
		
		// ----- Abfrage und Senden der Register, die repariert werden sollen ---

		// ergebnisArray auf null setzen
		for (int i = 0; i < 5; i++) uI.regZuReparieren[i] = -1;

		// Anfrage einblenden
		uICardLayout.show(userInterfaceContainer,"zureparieren");
		zuRepPanel.zeigeAuswahl();
		break;
	    }

	    // Servermeldung: Spielder wurde entfernt
	    case (cA.ENTFERNUNG): {
		// ------- Habe ich gewonnen / bin ich gestorben ----------
		boolean ichTot = true;
		try {
		    String[] gewinnerListe = kCS.getSpielstand();
		    if(gewinnerListe != null) {
			setStatus(Message.say("SpielerMensch","spielende")); 
			for (int i = 0; i < gewinnerListe.length; i++) {
			    if (gewinnerListe[i].equals(name)) ichTot=false;
			}
		    }
		    else {
			Global.debug(this,"Bin gestorben...");
			ichTot = true;
		    }
		}
		catch (KommException e) {Global.debug(this, e.getMessage());}
		//		Global.debug(this,"Und tschüß!");
		spielEnde=true;
		if (ichTot) {
		    uICardLayout.show(userInterfaceContainer,"zieltot");
		}
		else {
		    uICardLayout.show(userInterfaceContainer,"zielgewinner");
		}
		f.validate();
		break;
	    }
	    default : {
		Global.debug(this,"Habe vom Server Zeug bekommen, das ich nicht kenne");
		    uICardLayout.show(userInterfaceContainer,"leer");
	    }
	    }	
	}
	
	Global.debug(this," ende meiner run-Methode erreicht!");
	return;
    }
    
    /**
     * Main-Methode, die den menschlichen Spieler von der Shell aus als Thread startet
     */
    public static void main(String[] args){
	//1. name
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
	name = args[0];
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
}
