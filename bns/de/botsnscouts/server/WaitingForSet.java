package de.botsnscouts.server;

import java.util.*;

/** Wait for some roboters or views for some reason.
 *  Constructing a WaitingForSet blocks the current Thread until all
 *  elements of the set tell they are ready (by calling remove())
 */
 class WaitingForSet {

     private Vector waitingFor;

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
	 if (waitingFor.isEmpty()){
	     return waitingFor.iterator();
	 }
	 try{
	     wait(timeout);
	 }catch (InterruptedException e){
		 d("InterruptedException, shouldn't happen: "+e.getMessage());
	 }
	 return waitingFor.iterator();
     }


     /** Removes one object we waited for from the set. 
      * @throws RuntimeException if we didn't wait for that element. 
      */
     synchronized void removeAndNotify(Waitable w){
	 if (waitingFor.remove(w))
	     fireRemovalEvent(w);
	 if (waitingFor.isEmpty()){
	     notify();
	 }
     }

     boolean isEmpty(){ return waitingFor.isEmpty(); }
     Iterator iterator(){ return waitingFor.iterator(); }
     synchronized void remove(Waitable w){ waitingFor.remove(w); }
     int size() { return waitingFor.size(); }
     synchronized Waitable getElement(){
	 if (waitingFor.size()!=1)
	     throw new RuntimeException("Precondition: only one element left violated.");
	 return (Waitable)waitingFor.get(0);
     }

     // Event-throwing
     Vector listeners=new Vector();
     public void addRemovalListener(RemovalListener l){
	 synchronized (listeners){
	     listeners.add(l);
	 }
     }
     public void removeRemovalListener(RemovalListener l){
	 synchronized (listeners){
	     listeners.remove(l);
	 }
     }
     private void fireRemovalEvent(Waitable removed){
	 synchronized (listeners){
	     for (Iterator it=listeners.iterator();it.hasNext();)
		 ((RemovalListener)it.next()).waitableRemoved(removed);
	 }
     }

     private void d(String s){
	 de.botsnscouts.util.Global.debug(this,s);
     }
 }
