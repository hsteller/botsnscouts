package de.botsnscouts.server;

import java.util.*;

/** Wait for some roboters or views for some reason.
 *  Constructing a WaitingForSet blocks the current Thread until all
 *  elements of the set tell they are ready (by calling remove())
 */
 class WaitingForSet {

     private Collection waitingFor;

     /** Constructs the set of elements to wait for.  
      */
     WaitingForSet(Collection c){
	 waitingFor = new Vector(c); 
     }

     /** Blocks until the thing we waited for is done.
      *  @param timeout Timeout in ms.
      *  @return Iterator to the ones who did not make it within the timeout.
      */
     synchronized Iterator waitFor(int timeout){
	 try{
	     wait(timeout);
	 }catch (InterruptedException e){
	     de.botsnscouts.util.Global.debug(this, "InterruptedException, shouldn't happen: "+e.getMessage());
	 }
	 return waitingFor.iterator();
     }


     /** Removes one object we waited for from the set. 
      * @throws RuntimeException if we didn't wait for that element. 
      */
     synchronized void removeAndNotify(Waitable w){
	 waitingFor.remove(w);
	 if (waitingFor.isEmpty()){
	     notify();
	 }
	     
     }

     boolean isEmpty(){ return waitingFor.isEmpty(); }
     Iterator iterator(){ return waitingFor.iterator(); }
     synchronized void remove(Waitable w){ waitingFor.remove(w); }
 }
