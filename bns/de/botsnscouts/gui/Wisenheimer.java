package de.botsnscouts.gui;

import  java.util.*;
import  de.botsnscouts.util.*;
import  de.botsnscouts.autobot.*;
import  de.botsnscouts.board.*;
import  de.botsnscouts.server.*;

/** Wisenheimer's logic
 * 
 * 
 */
public class Wisenheimer{

    public static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance(Wisenheimer.class);

    private SpielfeldKS aiBoard;
    private Permu wirbel;
    private Roboter simRob = Roboter.getNewInstance("dummy");
    // damit wir nicht immer für jede Simulation einen neuen Robby brauchen
    private int[] predict=new int[5];
    private int lastPredict;

    public Wisenheimer (SpielfeldKS aiB) {
	aiBoard=aiB;
	wirbel = new Permu(aiBoard,0);
    }

    //berechnet den zug neu und gibt index der Karte zurück
    protected int getPrediction(ArrayList registers, ArrayList cards, Roboter robi){
        CAT.debug("Wisenheimer.getPrediction() called.");
	//teste ob alle register belegt sind
	int t=0;
	for (t=0;t<5;t++){
	    if (registers.get(t)==null)
		break;
	}
	if (t==5)
	    return -1;

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
	for (int i=0;i<5;i++){
	    predict[i]=vonPermut[i].getprio();
	}
	//Global.debug(this,"karten bekommen anz: "+vonPermut.length+" "+vonPermut);
	//for (int i=0; i < vonPermut.length; i++) {
	//Global.debug(this,"vonPermut["+i+"] ist "+vonPermut[i].getprio());
	//}

	// in vonPermut steht die vorgeschlagene Registerprogrammierung, so wie sie ggf. z.T.schon
	// in den Registern steht
	int nextprio =-1;
	for (int su=0; su<5;su++){
	    if ((registers.get(su) == null) || ((HumanCard)registers.get(su)).getprio() != (vonPermut[su].getprio())){
                nextprio = vonPermut[su].getprio();
		lastPredict=su;
                break;
            }
	}
	if (lastPredict==5){
	    return -1;
	}
	return getIndex(nextprio,cards);
    }

    private int getIndex(int nextprio,ArrayList robiCards){
        CAT.debug("Wisenheimer.getIndex() called");
	int ind=0;
	for (int i=0; i<robiCards.size();i++){
	    if (robiCards.get(i) != null) {
		if (((HumanCard)robiCards.get(i)).getprio() == nextprio) {
		    ind=i;
		    break;
		}
	    }
	}
	return ind;
    }

    //gibt die nächste karte von dem berechneten zug
    public int getNextPrediction(ArrayList registers,ArrayList robiCards){
        CAT.debug("Wisenheimer.getNextPrediction() called.");
	//teste ob alle register belegt sind
	int t=0;
	for (t=0;t<5;t++){
	    if (registers.get(t)==null)
		break;
	}
	if (t==5)
	    return -1;
	while(lastPredict<5&&registers.get(lastPredict)!=null){
	    lastPredict++;
	}
	if (lastPredict==5){
	    return -1;
	}
	return getIndex(predict[lastPredict],robiCards);
    }
    
}
