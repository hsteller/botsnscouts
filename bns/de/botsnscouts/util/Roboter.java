package de.botsnscouts.util;

/**
* Die Klasse enthaelt den Status des Roboters.
* @author Miriam (zumindest nach Refactoring...)
*/

public class Roboter {

    final static int ANZKARTEN = 9;
    final static int ANZREG = 5;

    protected final static Ort grube = new Ort(0,0);

    /** Roboter-Fabrik. Liefert neue Roboter */
    public static Roboter getNewInstance(String name) {
	return new de.botsnscouts.board.BoardRoboter(name);
    }

    /** Roboter-Fabrik kann auch klonen.
	Sollte sie aber nicht. Wozu Roboter kopieren?
    */
    public static Roboter getCopy(Roboter r) {
	return new de.botsnscouts.board.BoardRoboter(r);
    }

    protected String name;
    /**
     * gibt die Ausrichtung des Roboters auf dem Spielfeld an
     * N = 0; O = 1; S = 2; W = 3;
     */
    protected int ausrichtung;		   
    /**
     * gibt Position des Roboters auf dem Spielfeld an
     */
    protected Ort pos = new Ort();;

    /**
     * gibt die Nummer der nächsten Flagge an
     */
    protected int naechsteFlagge=2;
    /**
     * gibt die x-Koordinate auf dem Spielfeld an, auf dem der Roboter nach einer
     * Zerstörung wieder in das Spielfeld gesetzt wird.
     */
    protected int archivX=1;			
    /**
     * gibt die y-Koordinate auf dem Spielfeld an, auf dem der Roboter nach einer
     * Zerstörung wieder in das Spielfeld gesetzt wird.
     */
    protected int archivY=1;
    /**
     * gibt an, ob der Roboter virtuell (true) ist oder nicht (false)
     */
    protected boolean virtuell=true;
    /**
     * gibt die noch zur Verfügung stehenden Leben an
     */	
    protected int leben=4;
    /**
     * enthaelt die Programmierung
     */
    protected Karte[] zug = new Karte[ANZREG];
    /**
     * enthält die Karten der gesperrten Register 
     * Invariant: gesperrteRegister[i] -> zug[i] ist gesperrt.
     */
    protected boolean[] gesperrteRegister = new boolean[ANZREG];	

    /** Zugeteilte Karten */
    protected Karte[] karten = new Karte[ANZKARTEN];

    /**
     * gibt an, ob der Roboter aktiviert (true) oder deaktiviert (false) ist
     */
    protected boolean aktiviert=true;
    /**
     * Die Anzahl der Schadenpnkte
     */
    protected int schaden=0;

    /**
     * Deaktiviert angemeldet fuer naechste Runde?
     */
    protected boolean nextTurnPowerDown;
    

    /**
     * Visualisation id of the bot
     * @see de.botsnscouts.util.BotVis
     **/
    protected int botVis=0;

    /**
     *  Konstruktor
     *  @param Name des Spielers bzw. Roboters.
     *  Not public _on_purpose_. Use Roboter.getNewInstance() instead.
     */

    protected Roboter(String robName) {
	name = robName;
	ausrichtung=0;
	pos.set(1,1);
	naechsteFlagge=2;
	archivX = 1;
	archivY = 1;
	virtuell = true;
	leben = 3;
	aktiviert = true;
	schaden = 0;
	nextTurnPowerDown = false;
    }	 
	
    protected Roboter(Roboter r ) {
	super();
	name = r.name;
	ausrichtung = r.ausrichtung;
	pos.set(r.pos);
	schaden = r.schaden;
	naechsteFlagge = r.naechsteFlagge;
	archivX = r.archivX;
	archivY = r.archivY;
	virtuell = r.virtuell;
	leben = r.leben;
	aktiviert = r.aktiviert;
	for(int i=0;i < zug.length;i++) 
	    zug[i] = r.zug[i];
	for(int i=0;i < gesperrteRegister.length; i++) {
	    gesperrteRegister[i]=r.gesperrteRegister[i];
	}
	for (int i=0; i < karten.length; i++){
	    karten[i] = r.karten[i];
	}
    }

    /** setzt in diesen Roboter die fuer eine Simulation nötigen Daten
     *  diese werden aus dem übergebenen Robbi kopiert. 
     *  (ausrichtung, position, schaden, leben, Zielflagge, Archivpon, aktiv)
     */
    public void copyRob( Roboter r ) {
	ausrichtung = r.ausrichtung;
	pos.set(r.pos);
	schaden = r.schaden;
	naechsteFlagge = r.naechsteFlagge;
	archivX = r.archivX;
	archivY = r.archivY;
	virtuell = r.virtuell;
	botVis = r.botVis;
	leben = r.leben;
	aktiviert = r.aktiviert;
	for(int i=0;i < zug.length;i++) 
	    zug[i] = r.zug[i];
	for(int i=0;i < gesperrteRegister.length; i++) {
	    gesperrteRegister[i]=r.gesperrteRegister[i];
	}
	for (int i=0; i < karten.length; i++){
	    karten[i] = r.karten[i];
	}
    }
	

    /**
     * get the visualiation id of the bot
     * @see de.botsnscouts.util.BotVis
     */
    public int getBotVis() {
	return botVis;
    }

    /**
     * set the visualiation id of the bot
     * @see de.botsnscouts.util.BotVis
     */
    public void setBotVis(int vis) {
	botVis =vis;
    }
    

    /** liefert Zahl der zu erhaltenden Karten */
    public int anzKarten() {
	return ANZKARTEN - schaden;
    }

    public boolean isNaechsteRundeDeaktiviert(){
	return nextTurnPowerDown;
    }

    public void setNaechsteRundeDeaktiviert(boolean b){
	nextTurnPowerDown=b;
    }

    public String getName(){
	return name;
    }

    public int getAusrichtung() {
	return ausrichtung;
    }

    public int getX(){
	return pos.x;
    }

    public int getY() {
	return pos.y;
    }

    public Ort getPos() {
	return pos;
    }

    public int getArchivX(){
	return archivX;
    }

    public int getArchivY(){
	return archivY;
    }

    public int getSchaden(){
	return schaden;
    }

    public int getLeben() {
	return leben;
    }


    public int getNaechsteFlagge() {
	return naechsteFlagge;
    }

    public boolean istVirtuell() {
	return virtuell;
    }

    public boolean istAktiviert(){
	return aktiviert;
    }


    /** Prüft, ob der Roboter gerade in eine Grube gefallen ist. */
    public boolean istInGrube(){
	return (schaden==10) && pos.equals(grube);
    }

    /** returns the card locked at register i. null it isn't locked.
     */
    public Karte getGesperrteRegister(int i){
	if (gesperrteRegister[i])
	    return zug[i];
	else
	    return null;
    }

    /** returns an array which hold a card at position i if register i is
	locked, null otherwise
	This method is to be eliminated!
    */
    public Karte[] getGesperrteRegister() {
	Karte[] regs = new Karte[ANZREG];
	for (int i=0; i < ANZREG; i++){
	    if (gesperrteRegister[i])
		regs[i]=zug[i];
	    else
		regs[i]=null;
	}
	return regs;
    }

    public Karte getZug(int i){
	return zug[i];
    }

    public Karte[] getZug(){
	return zug;
    }


    public Karte[] getKarten() {
	return karten;
    }

    /** Liefert, ob zwei Roboter den gleichen Namen haben. 
     * (Dann sollten sie eigentlich gleich sein.)
     */
    public boolean sameName(Roboter r) {
	return name.equals(r.name);
    }

    /** Prüft, ob sich die Roboter an derselben Position aufhalten.
     *  Sind beide in dieselbe Grube gefallen, sind sie trotzdem nicht am
     *  selben Ort.
     */
    public boolean samePos(Roboter r) {
	return (r.pos.equals(pos) && !r.istInGrube());
    }

    /** Liefert die Anzahl der gesperrten Register
     * @author Miriam
     */
    public int gesperrteRegs() {
	int c=0;
	for (int i=0; i<ANZREG; i++)
	    if (gesperrteRegister[i])
		c++;
	return c;
    }
	
    /** entsperrt alle Register */
    public void entsperreAlleRegs() {
	for (int i=0; i<ANZREG; i++)
	    gesperrteRegister[i] = false;
    }

    /** Aktuelle Position wird archiviert. */
    public void touchArchiv(){
	setArchiv(pos.x, pos.y);
    }

    public void setArchiv(int x, int y) {
	archivX = x;
	archivY = y;
    }

    public void setArchiv(Ort o) {
	archivX = o.x;
	archivY = o.y;
    }
    
    /** schickt den Roboter in eine Grube 
     *  Wenn ein Roboter in eine Grube faellt, verliert er ein Leben,
     *  hat teporär Schaden 10 und die Position (0,0)
     */
    public void falleInGrube(){
	leben--;
	schaden=10;
	pos=grube;
    }

    /** bewegt den Roboter nach (x,Y) */
    public void moveTo(int x, int y){
	setPos(x,y);
    }

    /** bewegt den Roboter nach Ort */
    public void moveTo(Ort o){
	setPos(o);
    }

    public void setPos(int x, int y){
	this.pos.x = x;
	this.pos.y = y;
    }

    public void setPos(Ort o) {
	this.pos.set(o);
    }

    public void setVirtuell() {
	setVirtuell(true);
    }

    public void setVirtuell(boolean b) {
	virtuell = b;
    }


    public void setAktiviert() {
	setAktiviert(true);
    }

    public void setAktiviert(boolean b) {
	aktiviert = b;
    }

    /** Erhoeht den Schaden um 1 */
    public void incSchaden() {
	this.schaden++;
    }


    public void decrSchaden(int i) {
	schaden -= i;
    }

    /** erhoeht den Schaden um eins */
    public void setSchaden(int schaden) {
	this.schaden = schaden;
    }

    public void decrLeben() {
	leben--;
    }

    /** Setze Leben - warum tut das jemand??? */
    public void setLeben(int i) {
	leben = i;
    }

    public void setInvalidPos() {
	this.pos.x = 0;
	this.pos.y = 0;
    }

    /** Dreht den Roboter in die angegebene Richtung */
    public void dreheNach(int richtung){
	setAusrichtung(richtung);
    }

    public void setAusrichtung(int neu) {
	this.ausrichtung = neu;
    }

    public void incNaechsteFlagge() {
	naechsteFlagge++;
    }

    public void setNaechsteFlagge(int i){
	naechsteFlagge = i;
    }

    /** Setzt den Roboter zurueck auf seine Archivposition
     * @author Miriam  
     */
    public void zumArchiv() {
	pos.set(archivX, archivY);
    }

    /** Register n wird gesperrt und mit Karte k belegt. 
     *  deprecated Eigentlich hat man die entsprechende Karte ja bereits im letzten Zug drin!!!
     */
    public void sperreRegister(int i, Karte k){
	zug[i] = k;
	gesperrteRegister[i] = true;
    }


    /** Register n wird gesperrt, 
     */

    public void sperreReg(int n){
	gesperrteRegister[n] = true;
    }


    public void entsperreReg(int n){
	gesperrteRegister[n] = false;
    }
    
    /* to be eliminated: */

    public void sperreRegister(Karte[] karten) {
	for (int i = 0; i < ANZREG; i++){
	    if (karten[i]==null)
		gesperrteRegister[i] = false;
	    else {
		gesperrteRegister[i] = true;
		zug[i] = karten[i];
	    }
	}
    }


    public void setZug(int i, Karte karte) {
	zug[i] = karte;
    }


    /** Neue Karten werden vom Server zugeteilt.
     *  
     *  Der Roboter bekommt nur so viele Karten, wie er bekommen soll.
     *  Wird mit null aufgefuellt.
     */
    public void setKarten(Karte[] karten) {
	int i;
	for (i=0; i < karten.length; i++) {
	    this.karten[i]=karten[i];
	}
	for (; i < ANZKARTEN; i++){
	    this.karten[i] = null;
	}
    }

    /** Programm the register.
     *  This means: set "zug" according to the cards I have in "Karten"
     * and the locked registers.
     * @param Array which tells how to programm: Includes the index in "karten"
     * of the card to be put in the register. The array is one shorter for
     * each lockes register.
     * Pre: register includes only values from 1 to deales cards+1.

     */
    public void program (int[] register){
	int j=0;
	for (int i=0; i < ANZREG; i++){
	    if (!gesperrteRegister[i]){
		zug[i] = karten[register[j++]-1];
	    }
	}

    }

    public String toString()
        {
            String s="";
            s+="name: "+name+"; ausrichtung: "+ausrichtung+"; (x,y): "+pos+"\n";
            s+="naechsteflagge: "+naechsteFlagge+"; archiv (x,y): ("+archivX+", "+archivY+"); leben: "+leben+"; schaden: "+schaden+"\n";
            s+="virtuell:  "+virtuell+"; aktiviert: "+aktiviert;
            s+="zug: [p|a]: ";
            for(int i=0;i<zug.length;i++)
                if (zug[i]!=null)
                    s+="["+zug[i].getprio()+"|"+zug[i].getaktion()+"] ";
                else
                    s+="#";		
				
            s+="\ngesperrteRegister: ";
            for(int i=0;i<5;i++)
                if (gesperrteRegister[i])
                    s+="["+zug[i].getprio()+"|"+zug[i].getaktion()+"] ";
                else
                    s+="#";
						
            s+="\n";
            return s;
        }
    
    public void zeige_Roboter()
        {
            Global.debug(this,this.toString());
        }
}

