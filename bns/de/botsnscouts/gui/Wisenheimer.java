package de.botsnscouts.gui;

import  java.util.*;
import  de.botsnscouts.util.*;
import  de.botsnscouts.autobot.*;
import  de.botsnscouts.board.*;
import  de.botsnscouts.server.*;

/**
 * Wisenheimer
 * 
 */
public class Wisenheimer{

    private SpielfeldKS aiBoard;
    private Permu wirbel;
    private Roboter simRob = Roboter.getNewInstance("dummy");
    // damit wir nicht immer für jede Simulation einen neuen Robby brauchen

    public Wisenheimer (SpielfeldKS aiB) {
	aiBoard=aiB;
	wirbel = new Permu(aiBoard,0);
    }

    protected int getPrediction(ArrayList registers, ArrayList cards, Roboter robi){
	Global.debug(this,"habe bekommen: register: "+registers+" cards: "+cards+" und einen Robi: "+robi);
	simRob.copyRob(robi);

	Karte[] simCards=new HumanCard[9];
	// cards in simCards einlesen, gesperrte Karten werden nicht berücksichtigt
	int j=0;
	for (int i = 0; i < cards.size(); i++) {
	    if((cards.get(i) != null)&&((HumanCard)cards.get(i)).getState() == HumanCard.FREE) {
		simCards[j] = new HumanCard((HumanCard)cards.get(i));
		j++;
	    }
	}
	// gelegte Karten in das enstprechende gesperrte Register des Robis packen
	for (int l = 0; l<registers.size();l++) {
	    if ((registers.get(l) != null)&&(!((HumanCard)registers.get(l)).free())) {
		simRob.sperreRegister(l,  KartenStapel.get(((HumanCard)registers.get(l)).getprio(),((HumanCard)registers.get(l)).getaktion()));
	    }
	}
	// gesperrte Register in simRob.zug schreiben
	for (int i = 0; i < simRob.getGesperrteRegister().length; i++){
	    simRob.setZug(i, simRob.getGesperrteRegister(i));
	}
	Karte[] vonPermut = wirbel.permutiere(simCards, simRob);
	Global.debug(this,"karten bekommen anz: "+vonPermut.length+" "+vonPermut);
	for (int i=0; i < vonPermut.length; i++) {
	    Global.debug(this,"vonPermut["+i+"] ist "+vonPermut[i].getprio());
	}

	// in vonPermut steht die vorgeschlagene Registerprogrammierung, so wie sie ggf. z.T.schon
	// in den Registern steht
	int nextprio =-1;
	for (int su=0; su<5;su++){
	    if ((registers.get(su) == null) || ((HumanCard)registers.get(su)).getprio() != (vonPermut[su].getprio())){
                nextprio = vonPermut[su].getprio();
                break;
            }
	}
	int ind=0;
	for (int i=0; i<cards.size();i++){
	    if (cards.get(i) != null) {
		if (((HumanCard)cards.get(i)).getprio() == nextprio) {
		    ind=i;
		    break;
		}
	    }
	}
	return ind;
    }
    
}
