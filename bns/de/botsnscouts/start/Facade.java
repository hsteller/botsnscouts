package de.botsnscouts.start;

import java.awt.*;
import java.util.*;
import de.botsnscouts.util.*;
import de.botsnscouts.board.*;
import de.botsnscouts.comm.*;
import de.botsnscouts.gui.*;
// Diese Klasse dient der Entkopplung der GUI-Schicht (StartSpieler) 
// von der Fachkonzeptschicht(...) und der Datenhaltungsschicht (...)

public class Facade{

    private KachelRaster kachelRaster, kachelRasterSave;
    private Launcher launcher;
    private KommSpPr com;
    private KachelFactory kachelFactory;
    private int thumbGR;

    private static final String DIP="127.0.0.1";
    private static final int DPORT=8077;
    private static final int LPORT=8889;
    private static final int DPLAYERS=8;
    private static final int DTO=200;

////////////////////////////////////////
    // instanziiert alle ben�tigten Klassen
    public Facade(){
	this(180);
    }
    public Facade(int gr){
	thumbGR=gr;
	kachelFactory = new KachelFactory(thumbGR);
	kachelFactory.start();
	kachelRaster = new KachelRaster(kachelFactory);
	launcher = new Launcher();
	com=new KommSpPr();
    }

    public int getThumbGR(){
	return thumbGR;
    }

    //*KachelRaster*//
    //setzt Spielfeldgr��e
    public void setSpielfeldDim(int x, int y){
	kachelRaster.setSpielfeldDim(x,y);
    }

    //gibt Spielfeldgr��e zur�ck
    public Ort getSpielfeldDim(){
	return kachelRaster.getSpielfeldDim();
    }

    // aktualisiert das Spielfeld
    public void setKachel(int x, int y, String kachel) throws FlaggenVorhandenException{
	// to be written......
	//Global.debug(this,"setKachel "+x+","+y+" "+kachel);
	kachelRaster.setKachel(x,y,kachel);
    }

    //dreht die Kachel um r*90� nach rechts
    public void rotKachel(int x, int y){
	kachelRaster.rotKachel(x,y);
    }

    //l�scht die Kachel at (x,y)
    public void delKachel(int x, int y){
	// to be written......
	kachelRaster.delKachel(x,y);
    }

    //pr�ft ob auf der Kachel at (x,y) Flaggen stehen
    public boolean sindFlaggen(int x, int y){
	return kachelRaster.sindFlaggen(x,y);
    }

    // pr�ft ob eine Flagge hinzugef�gt werden kann
    public boolean checkFlaggePos(int x,int y){
	return kachelRaster.checkFlaggePos(x,y);
    }

    // pr�ft ob eine Flagge hinzugef�gt werden kann
    public boolean checkFlaggeMovePos(int x,int y){
	return kachelRaster.checkFlaggeMovePos(x,y);
    }

    // gibt ein String zur�ck falls die FlaggenPosition ung�nstig ist
    //null sonst
    public String getFlaggeKomment(int x,int y){
	return kachelRaster.getFlaggeKomment(x,y);
    }

    // f�gt eine Flagge hinzu 
    public void addFlagge(int x,int y) throws FlaggenException{
	kachelRaster.addFlagge(x,y);
    }

    // l�scht eine Flagge hinzu 
    public void delFlagge(int nr){
	kachelRaster.delFlagge(nr);
    }
    
    //l�scht Flagge mit koordinaten ax,ay
    public void delFlagge(int ax,int ay){
	kachelRaster.delFlagge(ax,ay);
    }

    //pr�ft, ob eine Flagge da ist
    public boolean istFlagge(int ax,int ay){
	return kachelRaster.istFlagge(ax,ay);
    }

    // versetzt eine Flagge 
    public void moveFlagge(int nr, int x,int y) throws FlaggenException{
	kachelRaster.moveFlagge(nr,x,y);
    }

    // versetzt eine Flagge 
    public void moveFlagge(int ax,int ay, int x,int y) throws FlaggenException{
	kachelRaster.moveFlagge(ax,ay,x,y);
    }

    //gibt die Flaggen zur�ck
    public Ort[] getFlaggen(){
	return kachelRaster.getFlaggen();
    }

    //gibt Kacheln als 2-dim Array von Spielfeld zur�ck
    public Tile[][] getKacheln(){
	return kachelRaster.getKacheln();
    }

    //gibt eine Kachel an der gegebenen Position
    public Tile getKachelAt(int x, int y){
	return kachelRaster.getKachelAt(x,y);
    }

    //speichert den Zustand des KachelRasters
    public void saveKachelRaster(){
	kachelRasterSave=kachelRaster.getClone();
    }

    //stellt den gespeicherten Zustand des KachelRastors wieder her
    public void restorKachelRaster(){
	kachelRaster=kachelRasterSave;
    }

    //pr�ft ob Spielfeld g�ltig ist (plausibilit�tstests)
    public boolean checkSpielfeld() throws OneFlagException, NichtZusSpfException{
	return kachelRaster.checkSpielfeld();
    }

    //gibt das aktuelle Spielfeld als Properties zur�ck
    public Properties getSpfProp(){
	Properties spfProp=new Properties();
	Tile[][] kach=getKacheln();
	for (int i=0;i<kach.length;i++){
	    for (int j=0;j<kach[0].length;j++){
		if (kach[i][j]!=null){
		    spfProp.setProperty("kach"+i+","+j,kach[i][j].getName());
		    spfProp.setProperty("dreh"+i+","+j,""+kach[i][j].getRotation());
		}
	    }
	}
	Ort[] flag=getFlaggen();
	for (int i=0;i<flag.length;i++){
	    if (flag[i]!=null){
		spfProp.setProperty("flag"+i+"x",""+flag[i].x);
		spfProp.setProperty("flag"+i+"y",""+flag[i].y);
	    }
	}
	return spfProp;
    }

    //l�dt das durch Properties beschriebene Spielfeld
    public void loadSpfProp(Properties spfProp){
	Ort dim=getSpielfeldDim();
	for (int i=0;i<dim.x;i++){
	    for (int j=0;j<dim.y;j++){
		//lese kachelname ein
		String name=spfProp.getProperty("kach"+i+","+j);
		delKachel(i,j);
		if(name!=null){
		    //falls vorhanden
		    try{
			//setze Kachel
			setKachel(i,j,name);
		    }catch(FlaggenVorhandenException e){
		    }
		    try{
			//lese Drehung aus
			int dr=Integer.parseInt(spfProp.getProperty("dreh"+i+","+j));
			//drehe Kachel
			for (int d=0;d<dr;d++){
			    rotKachel(i,j);
			}
		    }catch(NumberFormatException e){
		    }
		}
	    }
	}
	int flAnz=kachelRaster.getMaxFlag();
	int cntr=0;
	int flx,fly;
	//l�sche evtl. vorhandene Flaggen
	for (int i=0;i<flAnz;i++){
	    delFlagge(i);
	}
	//f�ge die Flaggen hinzu
	for (int i=0;i<flAnz;i++){
	    try{
		if (spfProp.getProperty("flag"+i+"x")!=null){
		    flx=Integer.parseInt(spfProp.getProperty("flag"+i+"x"));
		    fly=Integer.parseInt(spfProp.getProperty("flag"+i+"y"));
		    addFlagge(flx,fly);
		    cntr++;
		}
	    }catch(Exception e){}
	}
    }

    //*KachelFactory*//
    //gibt KachelInfos zur�ck
    public KachelInfo[] getKachelInfos(){
	return kachelFactory.getKachelInfos();
    }

    //*Launcher*//
    // startet ein AusgabeKanal
    public Thread einemSpielZuschauen(String ip, int port){
	return launcher.einemSpielZuschauen(ip, port,false);
    }
 
    // startet ein AusgabeKanal mit default parameter
    public Thread einemSpielZuschauen(){
	return launcher.einemSpielZuschauen(DIP, DPORT,false);
    }
 
    // startet ein AusgabeKanal mit default parameter und keinem Splashscreen
    public Thread einemSpielZuschauenNoSplash(){
	return launcher.einemSpielZuschauen(DIP, DPORT,true);
    }
 
    // startet einen SpielerMensch
    public Thread amSpielTeilnehmen(String ip, int port, String name, int farbe){
	return launcher.amSpielTeilnehmen(ip, port, name, farbe,false);
    }

    // startet einen SpielerMensch mit default parameter
    public Thread amSpielTeilnehmen(String name, int farbe){
	return launcher.amSpielTeilnehmen(DIP, DPORT, name, farbe,false);
    }

    // startet einen SpielerMensch mit default parameter und ohne Splash Screen
    public Thread amSpielTeilnehmenNoSplash(String name, int farbe){
	return launcher.amSpielTeilnehmen(DIP, DPORT, name, farbe,true);
    }

    // startet K�nstliche Spieler
    public Thread  kuenstlicheSpielerStarten(String ip, int port, boolean local, int iq){
	return launcher.kuenstlicheSpielerStarten(ip, port, local, iq, com);
    }

    // startet K�nstliche Spieler local
    public Thread  kuenstlicheSpielerStarten(String ip, int port, int iq){
	return launcher.kuenstlicheSpielerStarten(ip, port, true, iq, com);
    }

    // startet K�nstliche Spieler local mit default parametern
    public Thread  kuenstlicheSpielerStarten(int iq){
	return launcher.kuenstlicheSpielerStarten(DIP, DPORT, true, iq, com);
    }

    // launch the game
    public boolean startGame() throws OneFlagException, NichtZusSpfException{
	return startGame(DIP, DPORT, DPLAYERS, DTO, LPORT);
    }

    public boolean startGame(String ip, int port, int timeOut, int lisPort) throws OneFlagException, NichtZusSpfException{
	return startGame(ip, port, DPLAYERS, timeOut, lisPort);
    }

    public boolean startGame(String ip, int port, int pnum, int timeOut, int lisPort) throws OneFlagException, NichtZusSpfException{
	boolean ret;
	ret=launcher.startGame(com, kachelRaster, ip, port, pnum, timeOut, lisPort);

	return ret;
    }

    //startet das Spiel tats�chlich
    public boolean spielGehtLos(){
	return launcher.spielGehtLos(com,DIP,DPORT);
    }

    public void killStartServer(){
	com.sendString("back",DIP);
    }

    public void killServer(int port){
	com.cancelGame(DIP,port);
    }

}
