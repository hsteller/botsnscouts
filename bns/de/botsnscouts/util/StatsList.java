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

import java.util.Vector;
import java.util.Iterator;
import java.util.Arrays;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.apache.log4j.Category;

/**@author Hendrik
   Container class for Stats objects.
 */
public class StatsList implements ActionListener{
    static Category CAT = Category.getInstance(StatsList.class);

    // perhaps additional a Hashtable ?!
    private Stats[] robots;

    private Vector listeners = new Vector();


    /** Creates a new list with length 0*/
    public StatsList () {
	robots = new Stats[0];
    }
    /** Creates a new StatsList containing copies of the Stats-objects
	in stats.
	@param stats A vector containing Stats-objects.
    */
    public StatsList(Vector stats) {
	this.robots = new Stats [stats.size()];
	for (int i=0;i<stats.size();i++){
	    robots [i] = (Stats) stats.elementAt(i);
            robots [i].addActionListener(this);
        }
    }

    /** Creates a new StatsList.
	For every name in 'namen' a default Stats-object will be created.
	@param namen An Array of robotnames.
    */
    public StatsList (String [] namen) {
	robots = new Stats[namen.length];
	for (int i=0;i<namen.length;i++) {
	    robots [i]=new Stats(namen[i]);
            robots [i].addActionListener(this);
	}
    }

    /** Gets the Statsobject for the robot named <code>name</code>.
	@param name The robot's name
	@return A reference to the robot's Stats-object
	null, if no robot calles <code>name</code> was found.
    */
    public Stats getStats (String name) {
    // we have max. eight robots, so the simple for-loop will be efficient enough
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
                robots[i].removeActionListener(this);
		robots [i]=neu;
                robots [i].addActionListener(this);
		break;
	    }
        fireActionEvent();
    }

    /** Sets the StatsList to <code>sl</code>
	@param sl The new contents of the StatsList
    */
    public void update (StatsList sl) {
	this.changeStatsArray(sl);
        fireActionEvent();
    }

    /** Gets all Stats that are saved in the StatsList
	@return The Stats of the StatsList*/
    public Stats [] getStats() {
	return this.robots;
    }


    public int size(){
      if (robots==null)
        return 0;
      else
        return robots.length;
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

    public Stats[] getStatsSorted() {
      sort();
      return robots;
    }

    /**
       Sorts the list in descending order
    */
    public void sort(){
	Arrays.sort(robots);
    }

    public void addActionListener(ActionListener al) {

      if (al!=null && !listeners.contains(al)){
        listeners.add(al);
          CAT.debug("a listener was registered that wants to be informed about statistics");
      }
    }

    public void removeActionListener(ActionListener al){
      if (listeners.removeElement(al))
        CAT.debug("a listener was removed");
      else {
        CAT.debug("tried to remove a non existent listener..");
      }
    }

    private void fireActionEvent() {
      CAT.debug("some stats have changed; firing actionEvent..");
      ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED,"");
      Iterator it = listeners.iterator();
      while (it.hasNext()){
        ActionListener l = (ActionListener) it.next();
        l.actionPerformed(e);
      }
    }

    public void actionPerformed(ActionEvent e){
      CAT.debug("underlying stats have changed..");
      fireActionEvent();
    }

    private void changeStatsArray(StatsList sl) {
      changeStatsArray(sl.getStats());
    }

    private void changeStatsArray(Stats [] neu){
      CAT.debug("replacing entire internal stats array");
      for (int i=0;i<robots.length;i++){
        robots[i].removeActionListener(this);
      }
      robots = neu;
      for (int i=0;i<robots.length;i++){
        robots[i].addActionListener(this);
      }
    }

}


