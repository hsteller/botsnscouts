package de.botsnscouts.util;

import java.util.Vector;
import java.util.Arrays;


/**@author Hendrik
   Container class for Stats objects.
 */
public class StatsList {
    // perhaps additional a Hashtable ?!
    private Stats[] robots;
  


    /** Creates a new list with length 0*/
    public StatsList () {
	robots = new Stats[0];
    }
    /** Creates a new StatsList containing the Stats-objects 
	in stats.
	@param stats A vector containing Stats-objects.
    */
    public StatsList(Vector stats) {
	this.robots = new Stats [stats.size()];
	for (int i=0;i<stats.size();i++) 
	    robots [i] = (Stats) stats.elementAt(i);
    }

    /** Creates a new StatsList.
	For every name in 'namen' a Stats-object will be created.
	@param namen An Array of robotnames.
    */
    public StatsList (String [] namen) {
	robots = new Stats[namen.length];
	for (int i=0;i<namen.length;i++) {
	    robots [i]=new Stats(namen[i]);
	}
    }

    /** Gets the Statsobject for the robot named <code>name</code>.
	@param name The robot's name
	@return A reference to the robot's Stats-object
	null, if no robot calles <code>name</code> was found.
    */
    public Stats getStats (String name) {
	for (int i=0;i<robots.length;i++)
	    if (robots[i].getName().equals(name))
		return robots[i];
	return null;
    }

    /** This method exchanges the Stats-object for the robot neu.name (if the object exists).
	@param neu The new Stats-objects, describing the stats for the robot neu.name.
    */
    public void update (Stats neu) {
	for (int i=0;i<robots.length;i++)
	    if (robots[i].getName().equals(neu.getName())){
		robots [i]=neu;
		break;
	    }
	
    }

    /** Sets the StatsList to <code>sl</code>
	@param sl The new contents of the StatsList
    */
    public void update (StatsList sl) {
	this.robots=sl.getStats();
    }
    /** Gets all Stats that are saved in the StatsList
	@return The Stats of the StatsList*/
    public Stats [] getStats() {
	return this.robots;
    }
    
    /**
       @return A (newline seperated) String describing the lists content (unsorted) 
    */
    public String toString () {
	StringBuffer back= new StringBuffer();
	for (int i=0;i<robots.length;i++)
	    back.append(robots[i].toString()+"\n");
	return back.toString();
    }
    
    /** 
	Creates a special formatted/sorted String for sending the robot's stats
	to the clients.
	@return A sorted String (decreasing order) of the list's contents (Stats objects),
	using the less-method of the Stats class.
    */
    public String toSendString () {
	sort();
	StringBuffer back= new StringBuffer();
	back.append("(");
	for (int i=0;i<robots.length;i++){
	    back.append(robots[i].toSendString());
	    if (i<robots.length-1)
		back.append(",");
	}
	back.append(")");
	return back.toString();
    }
    
    /**
       Sorts the list in decreasing order
    */
    private void sort(){
	Arrays.sort(robots);
    }
 
}
    

