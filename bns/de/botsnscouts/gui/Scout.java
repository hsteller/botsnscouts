package de.botsnscouts.gui;

import  de.botsnscouts.*;
import  de.botsnscouts.util.*;
import  de.botsnscouts.comm.*;
import  de.botsnscouts.autobot.*;
import  de.botsnscouts.board.*;
import de.botsnscouts.server.KartenStapel; 

/**
 * scout logic
 * @author Lukasz Pekacki
 */
public class Scout {

    private boolean active = false;

    public Scout() {
    }


    protected boolean active() {
	return active;
    }

    /**
     * Setzt den Scout für das Feld, das den gelegten Karten entspricht
     **/
    Roboter[] doPhaseRob = new Roboter[1];




    /**
     * Setzt den Scout für das Feld, das den gelegten Karten entspricht
     **/
    private void removeScout() {
	/*
	// -------- entferne Scout 
	Roboter[] doPhaseRob = new Roboter[1];

	doPhaseRob[0] = Roboter.getCopy(f.statusLine.sC[myRobIndex].r);
	doPhaseRob[0].zeige_Roboter();
	int moeglichePhasen = 0;
	f.spielFeld.vorschau(moeglichePhasen,doPhaseRob);
	*/
    }
    


    
}
