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

    private TileRaster tileRaster, tileRasterSave;
    private Launcher launcher;
    private KommSpPr com;
    private TileFactory tileFactory;
    private int thumbGR;

    private static final String DIP="127.0.0.1";
    private static final int DPORT=8077;
    private static final int LPORT=8889;
    private static final int DPLAYERS=8;
    private static final int DTO=200;

////////////////////////////////////////
    // instanziiert alle benötigten Klassen
    public Facade(){
	this(180);
    }
    public Facade(int gr){
	thumbGR=gr;
	tileFactory = new TileFactory(thumbGR);
	tileFactory.start();
	tileRaster = new TileRaster(tileFactory);
	launcher = new Launcher();
	com=new KommSpPr();
    }

    public int getThumbGR(){
	return thumbGR;
    }

   //*TileRaster*//
    //setzt Spielfeldgröße
    public void setSpielfeldDim(int x, int y){
	tileRaster.setSpielfeldDim(x,y);
    }

    //gibt Spielfeldgröße zurück
    public Ort getSpielfeldDim(){
	return tileRaster.getSpielfeldDim();
    }

    // aktualisiert das Spielfeld
    public void setTile(int x, int y, String tile) throws FlaggenVorhandenException{
	// to be written......
	//Global.debug(this,"setTile "+x+","+y+" "+tile);
	tileRaster.setTile(x,y,tile);
    }

    //dreht die Tile um r*90° nach rechts
    public void rotTile(int x, int y){
	tileRaster.rotTile(x,y);
    }

    //löscht die Tile at (x,y)
    public void delTile(int x, int y){
	// to be written......
	tileRaster.delTile(x,y);
    }

    //prüft ob auf der Tile at (x,y) Flaggen stehen
    public boolean sindFlaggen(int x, int y){
	return tileRaster.sindFlaggen(x,y);
    }

    // prüft ob eine Flagge hinzugefügt werden kann
    public boolean checkFlaggePos(int x,int y){
	return tileRaster.checkFlaggePos(x,y);
    }

    // prüft ob eine Flagge hinzugefügt werden kann
    public boolean checkFlaggeMovePos(int x,int y){
	return tileRaster.checkFlaggeMovePos(x,y);
    }

    // gibt ein String zurück falls die FlaggenPosition ungünstig ist
    //null sonst
    public String getFlaggeKomment(int x,int y){
	return tileRaster.getFlaggeKomment(x,y);
    }

    // fügt eine Flagge hinzu 
    public void addFlagge(int x,int y) throws FlaggenException{
	tileRaster.addFlagge(x,y);
    }

    // löscht eine Flagge hinzu 
    public void delFlagge(int nr){
	tileRaster.delFlagge(nr);
    }
    
    //löscht Flagge mit koordinaten ax,ay
    public void delFlagge(int ax,int ay){
	tileRaster.delFlagge(ax,ay);
    }

    //prüft, ob eine Flagge da ist
    public boolean istFlagge(int ax,int ay){
	return tileRaster.istFlagge(ax,ay);
    }

    // versetzt eine Flagge 
    public void moveFlagge(int nr, int x,int y) throws FlaggenException{
	tileRaster.moveFlagge(nr,x,y);
    }

    // versetzt eine Flagge 
    public void moveFlagge(int ax,int ay, int x,int y) throws FlaggenException{
	tileRaster.moveFlagge(ax,ay,x,y);
    }

    //gibt die Flaggen zurück
    public Ort[] getFlaggen(){
	return tileRaster.getFlaggen();
    }

    //gibt Tilen als 2-dim Array von Spielfeld zurück
    public Tile[][] getTiles(){
	return tileRaster.getTiles();
    }

    //gibt eine Tile an der gegebenen Position
    public Tile getTileAt(int x, int y){
	return tileRaster.getTileAt(x,y);
    }

    //speichert den Zustand des TileRasters
    public void saveTileRaster(){
	tileRasterSave=tileRaster.getClone();
    }

    //stellt den gespeicherten Zustand des TileRastors wieder her
    public void restorTileRaster(){
	tileRaster=tileRasterSave;
    }

    //prüft ob Spielfeld gültig ist (plausibilitätstests)
    public boolean checkSpielfeld() throws OneFlagException, NichtZusSpfException{
	return tileRaster.checkSpielfeld();
    }

    //gibt das aktuelle Spielfeld als Properties zurück
    public Properties getSpfProp(){
	Properties spfProp=new Properties();
	Tile[][] kach=getTiles();
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

    //lädt das durch Properties beschriebene Spielfeld
    public void loadSpfProp(Properties spfProp){
	Ort dim=getSpielfeldDim();
	for (int i=0;i<dim.x;i++){
	    for (int j=0;j<dim.y;j++){
		//lese tilename ein
		String name=spfProp.getProperty("kach"+i+","+j);
		delTile(i,j);
		if(name!=null){
		    //falls vorhanden
		    try{
			//setze Tile
			setTile(i,j,name);
		    }catch(FlaggenVorhandenException e){
		    }
		    try{
			//lese Drehung aus
			int dr=Integer.parseInt(spfProp.getProperty("dreh"+i+","+j));
			//drehe Tile
			for (int d=0;d<dr;d++){
			    rotTile(i,j);
			}
		    }catch(NumberFormatException e){
		    }
		}
	    }
	}
	int flAnz=tileRaster.getMaxFlag();
	int cntr=0;
	int flx,fly;
	//lösche evtl. vorhandene Flaggen
	for (int i=0;i<flAnz;i++){
	    delFlagge(i);
	}
	//füge die Flaggen hinzu
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

    //*TileFactory*//
    //gibt TileInfos zurück
    public TileInfo[] getTileInfos(){
	return tileFactory.getTileInfos();
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
    // startet einen SpielerMensch with default port num
    public Thread amSpielTeilnehmen(String ip, String name, int farbe){
	return launcher.amSpielTeilnehmen(ip, DPORT, name, farbe,false);
    }

    // startet einen SpielerMensch mit default parameter
    public Thread amSpielTeilnehmen(String name, int farbe){
	return launcher.amSpielTeilnehmen(DIP, DPORT, name, farbe,false);
    }

    // startet einen SpielerMensch mit default parameter und ohne Splash Screen
    public Thread amSpielTeilnehmenNoSplash(String name, int farbe){
	return launcher.amSpielTeilnehmen(DIP, DPORT, name, farbe,true);
    }

    // startet Künstliche Spieler
    public Thread  kuenstlicheSpielerStarten(String ip, int port, boolean local, int iq){
	return launcher.kuenstlicheSpielerStarten(ip, port, local, iq, com);
    }

    // startet Künstliche Spieler local
    public Thread  kuenstlicheSpielerStarten(String ip, int port, int iq){
	return launcher.kuenstlicheSpielerStarten(ip, port, true, iq, com);
    }

    // startet Künstliche Spieler local mit default parametern
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
	ret=launcher.startGame(com, tileRaster, ip, port, pnum, timeOut, lisPort);

	return ret;
    }

    //startet das Spiel tatsächlich
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
