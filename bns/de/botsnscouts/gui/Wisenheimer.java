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

import java.util.ArrayList;

import de.botsnscouts.autobot.SearchRecursively;
import de.botsnscouts.board.SimBoard;
import de.botsnscouts.server.Deck;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Card;
import de.botsnscouts.util.Global;

/** Wisenheimer's logic
 * 
 * 
 */
public class Wisenheimer{

    public static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance(Wisenheimer.class);

    private SearchRecursively wirbel;
    private Bot simRob = Bot.getNewInstance("dummy");
    // damit wir nicht immer f�r jede Simulation einen neuen Robby brauchen
    private int[] predict=new int[5];
    private int lastPredict;

    public Wisenheimer (SimBoard board) {
	wirbel = new SearchRecursively(board, 0);
    }

   
    /**
     *  Calculates the move and returns the index of the recommended card.
     *  @param cards ArrayList of HumanCards we can use for this move
     *   @param registers ArrayList of HumanCards that are already programmed
     * @param robi current values of the bot we want to do the oredicition for 
     */

    protected int getPrediction(ArrayList registers, ArrayList cards, Bot robi){
        CAT.debug("Wisenheimer.getPrediction() called.");
		
		Card[] vonPermut = getPredictionCards(registers, cards, robi);
		if (vonPermut.length == 0){
		    return -1;
		}
		CAT.debug("getPrediction returned:\n"+Global.arrayToString(vonPermut));
		CAT.debug("SIMROB3\n"+simRob);
		for (int i=0;i<5;i++){
		    predict[i]=vonPermut[i].getprio();
		}
		//Global.debug(this,"karten bekommen anz: "+vonPermut.length+" "+vonPermut);
		//for (int i=0; i < vonPermut.length; i++) {
		//Global.debug(this,"vonPermut["+i+"] ist "+vonPermut[i].getprio());
		//}
	
		// in vonPermut steht die vorgeschlagene Registerprogrammierung, so wie sie ggf. z.T.schon
		// in den Registern steht
		int nextprio =-1; // will contain the card (identified by its unique priority) we recommend 
		for (int registerNum=0; registerNum<5;registerNum++){
		    // looking for the register we have to program: 
		    HumanCard card = (HumanCard) registers.get(registerNum);
		    if ( card == null || (card.getprio() != vonPermut[registerNum].getprio()) ){               
		        // found an empty or suboptimal programmed register
		        nextprio = vonPermut[registerNum].getprio(); // setting our recommendation 
                lastPredict=registerNum; // updating the marker for the last predicted register  
                break;
	        }
		}
		if (lastPredict==5){
		    return -1;
		}
		
		return getIndex(nextprio,cards);
    }

    
    protected Card [] getPredictionCards(ArrayList registers, ArrayList cards, Bot robi){
        	//      teste ob alle register belegt sind
		int t=0;
		for (t=0;t<5;t++){
		    if (registers.get(t)==null)
			break;
		}
		if (t==5)
		    return new Card[0];

		simRob.copyRob(robi);
	
		Card[] simCards=new HumanCard[9];
		// put all free cards at the begin of the simCard array 
		int j=0;
		for (int i = 0; i < cards.size(); i++) {
		    if((cards.get(i) != null)&&((HumanCard)cards.get(i)).getState() == HumanCard.FREE) {
				simCards[j] = new HumanCard((HumanCard)cards.get(i));
				j++;
		    }
		}
		CAT.debug("simCards:\n"+Global.arrayToString(simCards));
		// gelegte Karten in das enstprechende gesperrte Register des Robis packen
		for (int l = 0; l<registers.size();l++) {
		    if ((registers.get(l) != null)&&(!((HumanCard)registers.get(l)).free())) {
		        simRob.lockRegister(l,  Deck.get(((HumanCard)registers.get(l)).getprio()));
		    }
		}
		CAT.debug("SIMROB1:\n "+simRob);
		// gesperrte Register in simRob.zug schreiben
		for (int i = 0; i < simRob.getLockedRegisters().length; i++){
		    Card locked = simRob.getLockedRegister(i);
		    
		    if (locked == null){
		        simRob.setMove(i,null);
		    }
		    else {
		        HumanCard hc = new HumanCard(locked);
		        hc.setState(HumanCard.LOCKED);
		        simRob.setMove(i, hc);
		    }
		}
		CAT.debug("SIMROB2:\n "+simRob);
		return wirbel.findBestMove(simCards, simRob);
    }
    

    /** 
     * Will find the index of the card with priority <code>nextprio</code> 
     * in the HumanCard list <code>robicards</code>.
     * If the card is not found it will return 0 (==the first card; don't know
     * whether that is such a good idea but, hey, I didn't write the code and
     * it seems to work this way..).
     * 
     * @param nextprio The unique priority of the card to look for
     * @param robiCards The possible cards (of <code>HumanCards</code>)
     * @return the list position of the card we are looking for in the list
     */
    public static int getIndex(int nextprio,ArrayList robiCards){
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

    //gibt die n�chste karte von dem berechneten zug
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
