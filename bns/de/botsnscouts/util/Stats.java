package de.botsnscouts.util;

import java.util.Vector;
import java.util.Iterator;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.log4j.Category;

public class Stats implements Comparable{
    static Category CAT = Category.getInstance(Stats.class);

    private String name;
    private int hits;
    private int kills;
    private int damageByBoard;
    private int damageByRobots;

    private Vector listeners = new Vector();


    public Stats (String n) {
	this.name  = n;
	this.hits  = 0;
	this.kills = 0;
	this.damageByBoard=0;
	this.damageByRobots=0;
    }



    public String getName (){
	return name;
    }

    public int getHits(){
	return hits;
    }
    public int getDamageByBoard(){
	return damageByBoard;
    }
    public int getDamageByRobots(){
	return damageByRobots;
    }
    public int getKills(){
	return kills;
    }
    public void setName(String n) {
	this.name=n;
        fireActionEvent();
    }
    public void setHits(int i) {
	this.hits=i;
        fireActionEvent();
    }
    public void setKills(int i) {
	this.kills=i;
        fireActionEvent();
    }
    public void setDamageByBoard(int i) {
	this.damageByBoard=i;
        fireActionEvent();
    }
    public void setDamageByRobots(int i) {
	this.damageByRobots=i;
        fireActionEvent();
    }

    /** Increases this Stats number of hits by(?) one*/
    public void incHits() {
	++hits;
        fireActionEvent();
    }
    /** Increases this Stats number of kills by(?) one*/
    public void incKills() {
	++kills;
        fireActionEvent();
    }
   /** Increases this Stats number of damage got by Boardlasers by(?) one*/
    public void incDamageByBoard() {
	++damageByBoard;
        fireActionEvent();
    }
    /** Increases this Stats number of damage got by(?) robotlasers by(?) one*/
    public void incDamageByRobots() {
	++damageByRobots;
        fireActionEvent();
    }


    public String toSendString () {
	return (this.name+","+this.hits+","+this.kills+","+this.damageByBoard+","+this.damageByRobots);
    }

    public String toString () {
	return ("Robot: "+name+"\tHits: "+hits+"\tKills: "+kills+"\tdamByBoard:"+damageByBoard+"\tdamByRobot:"+this.damageByRobots);
    }

    /** Helper method for implementing the <code>Comparable</code> interface.*/
    private boolean less (Stats s) {
	if (this.hits<s.getHits())
	    return true;
	else if (this.hits==s.getHits()) {
	    if (this.kills<s.getKills())
		return true;
	    else if (this.kills==s.kills){
		if (this.name.toUpperCase().compareTo(s.name.toUpperCase())<0)
		    return true;
		else
		    return false;
	    }
	    else
		return false;
	}
	else
	    return false;
    }

    /** Implementation of the Comparable-interface;
	two stats-Objects can not be equal.
	@return -1 if this is less than o
	         1  otherwise
    */
    public int compareTo(Object o) {
	Stats s = (Stats) o;

	if (this.less(s)){
	    return 1;
	}
	else {
	    return -1;
	}

    }

    public void addActionListener (ActionListener al){
      if (al!=null && !listeners.contains(al)) {
        listeners.add(al);
        CAT.debug("registered a listener that wants to be informed about my robot's stats");
      }
    }

    public void removeActionListener(ActionListener al){
      CAT.debug("removeActionListener was called");
      if (listeners.removeElement(al)){
        CAT.debug("removed Listener");
      }
      else {
        CAT.debug("this listener was not registered");
      }
    }

    private void fireActionEvent() {
      CAT.debug("my robot has changed; firing actionEvent..");
      ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED,"");
      Iterator it = listeners.iterator();
      while (it.hasNext()){
        ActionListener l = (ActionListener) it.next();
        l.actionPerformed(e);
      }
    }


}
