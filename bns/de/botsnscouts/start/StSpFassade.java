package de.botsnscouts.start;

import java.awt.*;
import java.util.*;

// Diese Klasse dient der Entkopplung der GUI-Schicht (StartSpieler) 
// von der Fachkonzeptschicht(...) und der Datenhaltungsschicht (...)

public class StSpFassade{

    private SpielStarter spielStarter;
    private KachelRaster kachelRaster, kachelRasterSave;
    private Launcher launcher;
    private KommSpPr com;
    private KachelFactory kachelFactory;
    private int thumbGR;

////////////////////////////////////////
    // instanziiert alle benötigten Klassen
    public StSpFassade(){
	this(180);
    }
    public StSpFassade(int gr){
	thumbGR=gr;
	spielStarter = new SpielStarter();
	kachelFactory = new KachelFactory(thumbGR);
	kachelFactory.start();
	kachelRaster = new KachelRaster(kachelFactory);
	launcher = new Launcher();
	com=new KommSpPr();
    }

    public int getThumbGR(){
	return thumbGR;
    }

    //*SpielStarter*//
    // Startet einfach das Spiel
    public boolean startSpiel() throws OneFlagException, NichtZusSpfException{
	return startSpiel("127.0.0.1", 8077, 8, 200, 8889);
    }

    public boolean startSpiel(String ip, int port, int zugTimeOut, int lisPort) throws OneFlagException, NichtZusSpfException{
	return startSpiel(ip, port, 8, zugTimeOut);
    }

    public boolean startSpiel(String ip, int port, int anzahl, int zugTimeOut, int lisPort) throws OneFlagException, NichtZusSpfException{
	Global.debug(this,"starte spiel!");
	boolean ret;
	ret=spielStarter.startSpiel(com, kachelRaster, ip, port, anzahl, zugTimeOut, lisPort);

	Global.debug(this,"spiel gestartet!");

	return ret;
    }

    //*KachelRaster*//
    //setzt Spielfeldgröße
    public void setSpielfeldDim(int x, int y){
	kachelRaster.setSpielfeldDim(x,y);
    }

    //gibt Spielfeldgröße zurück
    public Ort getSpielfeldDim(){
	return kachelRaster.getSpielfeldDim();
    }

    // aktualisiert das Spielfeld
    public void setKachel(int x, int y, String kachel) throws FlaggenVorhandenException{
	// to be written......
	//Global.debug(this,"setKachel "+x+","+y+" "+kachel);
	kachelRaster.setKachel(x,y,kachel);
    }

    //dreht die Kachel um r*90° nach rechts
    public void rotKachel(int x, int y){
	kachelRaster.rotKachel(x,y);
    }

    //löscht die Kachel at (x,y)
    public void delKachel(int x, int y){
	// to be written......
	kachelRaster.delKachel(x,y);
    }

    //prüft ob auf der Kachel at (x,y) Flaggen stehen
    public boolean sindFlaggen(int x, int y){
	return kachelRaster.sindFlaggen(x,y);
    }

    // prüft ob eine Flagge hinzugefügt werden kann
    public boolean checkFlaggePos(int x,int y){
	return kachelRaster.checkFlaggePos(x,y);
    }

    // prüft ob eine Flagge hinzugefügt werden kann
    public boolean checkFlaggeMovePos(int x,int y){
	return kachelRaster.checkFlaggeMovePos(x,y);
    }

    // gibt ein String zurück falls die FlaggenPosition ungünstig ist
    //null sonst
    public String getFlaggeKomment(int x,int y){
	return kachelRaster.getFlaggeKomment(x,y);
    }

    // fügt eine Flagge hinzu 
    public void addFlagge(int x,int y) throws FlaggenException{
	kachelRaster.addFlagge(x,y);
    }

    // löscht eine Flagge hinzu 
    public void delFlagge(int nr){
	kachelRaster.delFlagge(nr);
    }
    
    //löscht Flagge mit koordinaten ax,ay
    public void delFlagge(int ax,int ay){
	kachelRaster.delFlagge(ax,ay);
    }

    //prüft, ob eine Flagge da ist
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

    //gibt die Flaggen zurück
    public Ort[] getFlaggen(){
	return kachelRaster.getFlaggen();
    }

    //gibt Kacheln als 2-dim Array von Spielfeld zurück
    public Kachel[][] getKacheln(){
	return kachelRaster.getKacheln();
    }

    //gibt eine Kachel an der gegebenen Position
    public Kachel getKachelAt(int x, int y){
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

    //prüft ob Spielfeld gültig ist (plausibilitätstests)
    public boolean checkSpielfeld() throws OneFlagException, NichtZusSpfException{
	return kachelRaster.checkSpielfeld();
    }

    //gibt das aktuelle Spielfeld als Properties zurück
    public Properties getSpfProp(){
	Properties spfProp=new Properties();
	Kachel[][] kach=getKacheln();
	for (int i=0;i<kach.length;i++){
	    for (int j=0;j<kach[0].length;j++){
		if (kach[i][j]!=null){
		    spfProp.setProperty("kach"+i+","+j,kach[i][j].getName());
		    spfProp.setProperty("dreh"+i+","+j,""+kach[i][j].getDrehung());
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

    //*KachelFactory*//
    //gibt KachelInfos zurück
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
	return launcher.einemSpielZuschauen("127.0.0.1", 8077,false);
    }
 
    // startet ein AusgabeKanal mit default parameter und keinem Splashscreen
    public Thread einemSpielZuschauenNoSplash(){
	return launcher.einemSpielZuschauen("127.0.0.1", 8077,true);
    }
 
    // startet einen SpielerMensch
    public Thread amSpielTeilnehmen(String ip, int port, String name, int farbe){
	return launcher.amSpielTeilnehmen(ip, port, name, farbe,false);
    }

    // startet einen SpielerMensch mit default parameter
    public Thread amSpielTeilnehmen(String name, int farbe){
	return launcher.amSpielTeilnehmen("127.0.0.1", 8077, name, farbe,false);
    }

    // startet einen SpielerMensch mit default parameter und ohne Splash Screen
    public Thread amSpielTeilnehmenNoSplash(String name, int farbe){
	return launcher.amSpielTeilnehmen("127.0.0.1", 8077, name, farbe,true);
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
	return launcher.kuenstlicheSpielerStarten("127.0.0.1", 8077, true, iq, com);
    }

    //startet das Spiel tatsächlich
    public boolean spielGehtLos(){
	return spielStarter.spielGehtLos(com,"127.0.0.1",8077);
    }

    public void killStartServer(){
	com.sendString("back","127.0.0.1");
    }

    public void killServer(int port){
	com.cancelGame("127.0.0.1",port);
    }

}
