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

   // private static Category CAT = Category.getInstance(Bot.class);
    
    protected final static Location inPit = new Location(0,0);

    /** Factory method for new robots. */
    public static Bot getNewInstance(String name) {
        return new de.botsnscouts.board.BoardBot(name);
    }

    /** Factory can clone, too. This should not be
     * done, however. What's the point?
    */
    public static Bot getCopy(Bot r) {
	return new de.botsnscouts.board.BoardBot(r);
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
     *  @param Name of the player/robot.
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

    /**
     *  Copy a bot. Needed for simulation.
     * @param The bot whose status is to be copied.
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
	return pos.getX();
    }

    public int getY() {
	return pos.getY();
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


    public Card[] getCards() {
	return cards;
    }


    /**
     * Checks whether two bots are at the same location.
     * If both are in a pitch, they are never at the same position.
     */
    public boolean samePos(Bot r) {
	return (r.pos.equals(pos) && !r.isInPit());
    }

    /**
     * Get number of lockedRegisters.
     */

    public int countLockedRegisters() {
	int c=0;
	for (int i=0; i<NUM_REG; i++)
	    if (lockedRegisters[i])
		c++;
	return c;
    }

    /** entsperrt alle Register */
    public void unlockAllRegisters() {
	for (int i=0; i<NUM_REG; i++)
	    lockedRegisters[i] = false;
    }

    /** Remember the curent poition as archive position */
    public void touchArchive(){
	setArchive(pos.getX(), pos.getY());
    }

    public void setArchive(int x, int y) {
	archiveX = x;
	archiveY = y;
    }

    public void setArchive(Location o) {
	archiveX = o.getX();
	archiveY = o.getY();
    }

    /**
     * The bot is falling into a pitch.
     * I.e. it looses one life, gets full damage temporatily and
     * moves to the virtual possition (0,0).
     */
    public void fallIntoPitch(){
	lives--;
	damage=10;
	pos=inPit;
    }

    /**
     * Move bot to specified position.
     */
    public void moveTo(int x, int y){
	setPos(x,y);
    }

    /** ove bot to specified Location */
    public void moveTo(Location o){
	setPos(o);
    }

    public void setPos(int x, int y){
        this.pos.set(x, y);
    }

    public void setPos(Location o) {
	this.pos.set(o);
    }

    public void setVirtual() {
	setVirtual(true);
    }

    public void setVirtual(boolean b) {
	virtual = b;
    }

    public void setActivated(boolean b) {
	activated = b;
    }

    /** Increment damage by one */
    public void incDamage() {
	this.damage++;
    }


    public void decrDamage(int i) {
	damage -= i;
    }

    /** Set damage. */
    public void setDamage(int schaden) {
	this.damage = schaden;
    }

    public void decrLife() {
	lives--;
    }

    /** Setze Leben - warum tut das jemand??? */
    public void setLives(int i) {
	lives = i;
    }

    public void setInvalidPos() {
	this.pos.set(0,0);
    }


    
    public void turnClockwise(){
       this.facing =((facing + 1) % 4);               
    }
    
    public void turnCounterClockwise(){
        int tmpFacing = this.facing -1;
        if (tmpFacing == -1)
            tmpFacing = 3;
        this.facing = tmpFacing;
     
    }
    
    public void setFacing(int neu) {
        
        this.facing = neu;
      
    }

    public void incNextFlag() {
	nextFlag++;
    }

    public void setNextFlag(int i){
	nextFlag = i;
    }

    /** Move the bot to its last archive position.
     * @author Miriam
     */
    public void toArchive() {
	pos.set(archiveX, archiveY);
    }

    /** Register n wird gesperrt und mit Card k belegt.
     *  deprecated Eigentlich hat man die entsprechende Card ja bereits im letzten Zug drin!!!
     */
    public void lockRegister(int i, Card k){
	move[i] = k;
	lockedRegisters[i] = true;
    }


    /** Register n wird gesperrt,
     */

    public void lockRegister(int n){
	lockedRegisters[n] = true;
    }


    public void unlockRegister(int n){
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


    public void setMove(int i, Card karte) {
	move[i] = karte;
    }


    /**
     * Get new cards by the server.
     *
     * The bot gets only as many cards as it should,
     * the array is filled with nulls at the end.
     */
    public void setCards(Card[] karten) {
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
                    s+="["+move[i].getprio()+"|"+move[i].getAction()+"] ";
                else
                    s+="#";

            s+="\ngesperrteRegister: ";
            for(int i=0;i<5;i++)
                if (lockedRegisters[i])
                    s+="["+move[i].getprio()+"|"+move[i].getAction()+"] ";
                else
                    s+="#";

            s+="\n";
            return s;
        }

    public void debug()
        {
            Global.debug(this,this.toString());
        }
}

