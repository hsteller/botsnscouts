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
 
package de.botsnscouts.util;

/**
 * Represents one robot.
 * @author Miriam (zumindest nach Refactoring...)
 */

public class Bot {

    final static int NUM_CARDS = 9;
    final static int NUM_REG = 5;

    protected final static Location inPit = new Location(0,0);

    /** Factory method for new robots. */
    public static Bot getNewInstance(String name) {
	return new de.botsnscouts.board.BoardRoboter(name);
    }

    /** Factory can clone, too. This should not be
     * done, however. What's the point?
    */
    public static Bot getCopy(Bot r) {
	return new de.botsnscouts.board.BoardRoboter(r);
    }

    public void fillCopy(Bot r) {
	r.name = name;
	r.facing = facing;
	r.pos.set(pos);
	r.damage = damage;
	r.nextFlag = nextFlag;
	r.archiveX = archiveX;
	r.archiveY = archiveY;
	r.virtual = virtual;
	r.lives = lives;
	r.activated = activated;
	for(int i=0;i < move.length;i++)
	    r.move[i] = move[i];
	for(int i=0;i < lockedRegisters.length; i++) {
	    r.lockedRegisters[i]=lockedRegisters[i];
	}
	for (int i=0; i < cards.length; i++){
	    r.cards[i] = cards[i];
	}
    }

    protected String name;
    /**
     * Which direction the bot is facing.
     * N = 0; O = 1; S = 2; W = 3;
     */
    protected int facing;
    /**
     * the location of this bot on the board
     */
    protected Location pos = new Location();

    /**
     * Which is the next flag this bot has to touch
     */
    protected int nextFlag=2;
    /**
     * X-Coordinate of archive position, where bot will reappear after death.
     */
    protected int archiveX=1;
    /**
    * Y-Coordinate of archive position, where bot will reappear after death.
    */
    protected int archiveY=1;
    /**
     * Whether this bot is virtual or not
     */
    protected boolean virtual=true;
    /**
     * Number of lives left.
     */
    protected int lives=4;
    /**
     * current program
     */
    protected Card[] move = new Card[NUM_REG];
    /**
     * contains the cards of the locked registers.
     * Invariant lockedRegisters[i] -> move[i]!=null
     */
    protected final boolean[] lockedRegisters = new boolean[NUM_REG];

    /** Cards given to this bot */
    protected final Card[] cards = new Card[NUM_CARDS];

    /**
     * Whether the bot is activated or not.
     */
    protected boolean activated=true;
    /**
     * Damage
     */
    protected int damage=0;

    /**
     * Did this bot want to power down next turn?
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
     *  Not public _on_purpose_. Use Bot.getNewInstance() instead.
     */
    protected Bot(String robName) {
	name = robName;
	facing=0;
	pos.set(1,1);
	nextFlag=2;
	archiveX = 1;
	archiveY = 1;
	virtual = true;
	lives = 3;
	activated = true;
	damage = 0;
	nextTurnPowerDown = false;
    }

    protected Bot(Bot r ) {
	super();
	name = r.name;
	facing = r.facing;
	pos.set(r.pos);
	damage = r.damage;
	nextFlag = r.nextFlag;
	archiveX = r.archiveX;
	archiveY = r.archiveY;
	virtual = r.virtual;
	lives = r.lives;
	activated = r.activated;
	for(int i=0;i < move.length;i++)
	    move[i] = r.move[i];
	for(int i=0;i < lockedRegisters.length; i++) {
	    lockedRegisters[i]=r.lockedRegisters[i];
	}
	for (int i=0; i < cards.length; i++){
	    cards[i] = r.cards[i];
	}
    }

    /** setzt in diesen Bot die fuer eine Simulation nötigen Daten
     *  diese werden aus dem übergebenen Robbi kopiert.
     *  (ausrichtung, position, schaden, leben, Zielflagge, Archivpon, aktiv)
     */
    public void copyRob( Bot r ) {
	facing = r.facing;
	pos.set(r.pos);
	damage = r.damage;
	nextFlag = r.nextFlag;
	archiveX = r.archiveX;
	archiveY = r.archiveY;
	virtual = r.virtual;
	botVis = r.botVis;
	lives = r.lives;
	activated = r.activated;
	for(int i=0;i < move.length;i++)
	    move[i] = r.move[i];
	for(int i=0;i < lockedRegisters.length; i++) {
	    lockedRegisters[i]=r.lockedRegisters[i];
	}
	for (int i=0; i < cards.length; i++){
	    cards[i] = r.cards[i];
	}
    }


    /**
     * get the visualiation id of the bot
     * @see de.botsnscouts.util.BotVis
     */
    public int getBotVis() {
      try {
	return BotVis.getBotVisByName( name );
      } catch( IllegalStateException ies ) {
        Global.debug(this, "couldn't get my color from botvis");
        return botVis;
      }
    }

    /**
     * set the visualiation id of the bot
     * @see de.botsnscouts.util.BotVis
     */
    public void setBotVis(int vis) {
	botVis =vis;
    }

    /** Returns the number of cards the server should
     *  give to this robot.
     */
    public int cardsToGive() {
	return NUM_CARDS - damage;
    }

    public boolean isPoweredDownNextTurn(){
	return nextTurnPowerDown;
    }

    public void setNextTurnPoweredDown(boolean b){
	nextTurnPowerDown=b;
    }

    public String getName(){
	return name;
    }

    public int getFacing() {
	return facing;
    }

    public int getX(){
	return pos.x;
    }

    public int getY() {
	return pos.y;
    }

    public Location getPos() {
	return pos;
    }

    public int getArchiveX(){
	return archiveX;
    }

    public int getArchiveY(){
	return archiveY;
    }

    public int getDamage(){
	return damage;
    }

    public int getLivesLeft() {
	return lives;
    }

    public int getNextFlag() {
	return nextFlag;
    }

    public boolean isVirtual() {
	return virtual;
    }

    public boolean isActivated(){
	return activated;
    }

    /** Checks whether this bot just fell into a pit.
     *
     */
    public boolean isInPit(){
	return (damage==10) && pos.equals(inPit);
    }

    /** returns the card locked at register i. null it isn't locked.
     */
    public Card getLockedRegister(int i){
	if (lockedRegisters[i])
	    return move[i];
	else
	    return null;
    }

    /** returns an array which hold a card at position i if register i is
	locked, null otherwise
	This method is to be eliminated!
    */
    public Card[] getLockedRegisters() {
	Card[] regs = new Card[NUM_REG];
	for (int i=0; i < NUM_REG; i++){
	    if (lockedRegisters[i])
		regs[i]=move[i];
	    else
		regs[i]=null;
	}
	return regs;
    }

    public Card getMove(int i){
	return move[i];
    }

    public Card[] getMove(){
	return move;
    }


    public Card[] getKarten() {
	return cards;
    }

    /** Liefert, ob zwei Bot den gleichen Namen haben.
     * (Dann sollten sie eigentlich gleich sein.)
     */
    public boolean sameName(Bot r) {
	return name.equals(r.name);
    }

    /** Prüft, ob sich die Bot an derselben Position aufhalten.
     *  Sind beide in dieselbe Grube gefallen, sind sie trotzdem nicht am
     *  selben Location.
     */
    public boolean samePos(Bot r) {
	return (r.pos.equals(pos) && !r.isInPit());
    }

    /** Liefert die Anzahl der gesperrten Register
     * @author Miriam
     */
    public int gesperrteRegs() {
	int c=0;
	for (int i=0; i<NUM_REG; i++)
	    if (lockedRegisters[i])
		c++;
	return c;
    }

    /** entsperrt alle Register */
    public void entsperreAlleRegs() {
	for (int i=0; i<NUM_REG; i++)
	    lockedRegisters[i] = false;
    }

    /** Aktuelle Position wird archiviert. */
    public void touchArchiv(){
	setArchiv(pos.x, pos.y);
    }

    public void setArchiv(int x, int y) {
	archiveX = x;
	archiveY = y;
    }

    public void setArchiv(Location o) {
	archiveX = o.x;
	archiveY = o.y;
    }

    /** schickt den Bot in eine Grube
     *  Wenn ein Bot in eine Grube faellt, verliert er ein Leben,
     *  hat teporär Schaden 10 und die Position (0,0)
     */
    public void falleInGrube(){
	lives--;
	damage=10;
	pos=inPit;
    }

    /** bewegt den Bot nach (x,Y) */
    public void moveTo(int x, int y){
	setPos(x,y);
    }

    /** bewegt den Bot nach Location */
    public void moveTo(Location o){
	setPos(o);
    }

    public void setPos(int x, int y){
	this.pos.x = x;
	this.pos.y = y;
    }

    public void setPos(Location o) {
	this.pos.set(o);
    }

    public void setVirtuell() {
	setVirtuell(true);
    }

    public void setVirtuell(boolean b) {
	virtual = b;
    }


    public void setAktiviert() {
	setAktiviert(true);
    }

    public void setAktiviert(boolean b) {
	activated = b;
    }

    /** Erhoeht den Schaden um 1 */
    public void incSchaden() {
	this.damage++;
    }


    public void decrSchaden(int i) {
	damage -= i;
    }

    /** erhoeht den Schaden um eins */
    public void setSchaden(int schaden) {
	this.damage = schaden;
    }

    public void decrLeben() {
	lives--;
    }

    /** Setze Leben - warum tut das jemand??? */
    public void setLeben(int i) {
	lives = i;
    }

    public void setInvalidPos() {
	this.pos.x = 0;
	this.pos.y = 0;
    }

    /** Dreht den Bot in die angegebene Richtung */
    public void dreheNach(int richtung){
	setAusrichtung(richtung);
    }

    public void setAusrichtung(int neu) {
	this.facing = neu;
    }

    public void incNaechsteFlagge() {
	nextFlag++;
    }

    public void setNaechsteFlagge(int i){
	nextFlag = i;
    }

    /** Setzt den Bot zurueck auf seine Archivposition
     * @author Miriam
     */
    public void zumArchiv() {
	pos.set(archiveX, archiveY);
    }

    /** Register n wird gesperrt und mit Card k belegt.
     *  deprecated Eigentlich hat man die entsprechende Card ja bereits im letzten Zug drin!!!
     */
    public void sperreRegister(int i, Card k){
	move[i] = k;
	lockedRegisters[i] = true;
    }


    /** Register n wird gesperrt,
     */

    public void sperreReg(int n){
	lockedRegisters[n] = true;
    }


    public void entsperreReg(int n){
	lockedRegisters[n] = false;
    }

    /* to be eliminated: */

    public void sperreRegister(Card[] karten) {
	for (int i = 0; i < NUM_REG; i++){
	    if (karten[i]==null)
		lockedRegisters[i] = false;
	    else {
		lockedRegisters[i] = true;
		move[i] = karten[i];
	    }
	}
    }


    public void setZug(int i, Card karte) {
	move[i] = karte;
    }


    /** Neue Karten werden vom Server zugeteilt.
     *
     *  Der Bot bekommt nur so viele Karten, wie er bekommen soll.
     *  Wird mit null aufgefuellt.
     */
    public void setKarten(Card[] karten) {
	int i;
	for (i=0; i < karten.length; i++) {
	    this.cards[i]=karten[i];
	}
	for (; i < NUM_CARDS; i++){
	    this.cards[i] = null;
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
	for (int i=0; i < NUM_REG; i++){
	    if (!lockedRegisters[i]){
		move[i] = cards[register[j++]-1];
	    }
	}

    }

    public String toString()
        {
            String s="";
            s+="name: "+name+"; ausrichtung: "+facing+"; (x,y): "+pos+"\n";
            s+="naechsteflagge: "+nextFlag+"; archiv (x,y): ("+archiveX+", "+archiveY+"); leben: "+lives+"; schaden: "+damage+"\n";
            s+="virtuell:  "+virtual+"; aktiviert: "+activated;
            s+="zug: [p|a]: ";
            for(int i=0;i<move.length;i++)
                if (move[i]!=null)
                    s+="["+move[i].getprio()+"|"+move[i].getaktion()+"] ";
                else
                    s+="#";

            s+="\ngesperrteRegister: ";
            for(int i=0;i<5;i++)
                if (lockedRegisters[i])
                    s+="["+move[i].getprio()+"|"+move[i].getaktion()+"] ";
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

