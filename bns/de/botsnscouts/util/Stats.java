package de.botsnscouts.util; 

public class Stats {
    private String name;
    private int hits;
    private int kills;
    private int damageByBoard;
    private int damageByRobots;
    

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
    }
    public void setHits(int i) {
	this.hits=i;
    }
    public void setKills(int i) {
	this.kills=i;
    }
    public void setDamageByBoard(int i) {
	this.damageByBoard=i;
    }
    public void setDamageByRobots(int i) {
	this.damageByRobots=i;
    }

    /** Increases this Stats number of hits by(?) one*/
    public void incHits() {
	++hits;
    }
    /** Increases this Stats number of kills by(?) one*/
    public void incKills() {
	++kills;
    }
   /** Increases this Stats number of damage got by Boardlasers by(?) one*/
    public void incDamageByBoard() {
	++damageByBoard;
    }
    /** Increases this Stats number of damage got by(?) robotlasers by(?) one*/
    public void incDamageByRobots() {
	++damageByRobots;
    }


    public String toSendString () {
	return (this.name+","+this.hits+","+this.kills+","+this.damageByBoard+","+this.damageByRobots);
    }

    public String toString () {
	return ("Robot: "+name+"\tHits: "+hits+"\tKills: "+kills+"\tdamByBoard:"+damageByBoard+"\tdamByRobot:"+this.damageByRobots);
    }
    public boolean less (Stats s) {
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

}
