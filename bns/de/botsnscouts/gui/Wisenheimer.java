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

    private SearchRecursively wirbel;
    private Bot simRob = Bot.getNewInstance("dummy");
    // damit wir nicht immer für jede Simulation einen neuen Robby brauchen
    private int[] predict=new int[5];
    private int lastPredict;

    public Wisenheimer (SimBoard board) {
	wirbel = new SearchRecursively(board, 0);
    }

    //berechnet den zug neu und gibt index der Card zurück
    protected int getPrediction(ArrayList registers, ArrayList cards, Bot robi){
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

	Card[] simCards=new HumanCard[9];
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
		simRob.lockRegister(l,  Deck.get(((HumanCard)registers.get(l)).getprio()));
	    }
	}
	// gesperrte Register in simRob.zug schreiben
	for (int i = 0; i < simRob.getLockedRegisters().length; i++){
	    simRob.setMove(i, simRob.getLockedRegister(i));
	}
	Card[] vonPermut = wirbel.findBestMove(simCards, simRob);
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
