package de.botsnscouts.util;
/**@author Hendrik
   Container class for Stats objects.
 */
public class StatsList {
    private Stats[] robots;
  
    /** Creates a new list with length 0 */ 
    public StatsList () {
	robots = new Stats[0];
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
    protected String toSendString () {
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
	sort(0,robots.length-1);
    }
    
    private void sort(int l, int r) {
	if (l<r){
	    // Waehle Pivotelement zufaellig
	    int pivotindex = (int)(Math.random()*(r-l+1));
	    // Pivotelement nach vorne vertauschen
	    Stats pivotelem = robots[l+pivotindex];
	    robots[l+pivotindex]=robots[l];
	    robots[l] = pivotelem;
	    // Alle Elemente, die kleiner als das Pivotelement sind,
	    // nach links vertauschen,
	    // die, die groesser als das Pivotelement sind, nach rechts.
	    int i=l+1;
	    int j=r;
	    while (i<=j){
		Stats swap = robots[i];
		robots[i] = robots[j];
		robots[j] = swap;
		// two robots can't be equal, because they have different names
		/*	while ((i<=j)&&(robots[i].less(pivotelem))) 
		    i++;
		while ((i<=j)&&(pivotelem.less(robots[j]))) 
		    j--;
		*/
		while ((i<=j)&&(pivotelem.less(robots[i]))) 
		    i++;
		while ((i<=j)&&(robots[j].less(pivotelem))) 
		    j--;
	    }
	    i--;
	    //Pivotelement in die Mitte tauschen
	    robots[l] = robots[i];
	    robots[i] = pivotelem;
	    // Arraybereich links und rechts vom Pivotelement rekursiv
	    // sortieren
	    sort(l,i-1);
	    sort(i+1,r);
	}
    } 	
}
    

