package de.botsnscouts.autobot;

/**
 * Permu-Klasse
 * @author Dirk, Lukasz
 * $Author$
 */

import de.botsnscouts.board.SpielfeldKS;
import de.botsnscouts.util.*;
import de.botsnscouts.board.BoardRoboter;
import de.botsnscouts.server.KartenStapel;

import java.util.Arrays;

public class Permu {
    static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance( Permu.class );
    private SpielfeldKS sf;
    private Karte[] bestZug;
    private int bestScore;
    private int malus;

    private static final Karte[] zuege = { KartenStapel.getRefCard("RL"),
                                           KartenStapel.getRefCard("M1"),
                                           KartenStapel.getRefCard("M2"),
                                           KartenStapel.getRefCard("M3"),
                                           KartenStapel.getRefCard("BU")};
    private static final int[] zuegemali = { 25, 15, 15, 15, 10 };
    private static final int zuegemalisumme = 80;

    public Permu (SpielfeldKS s,int m) {
	sf = s;
        malus=m;
    }

    public Karte[] permutiere(Karte[] ka, final Roboter r){
	int j = 0;
	bestZug = new Karte[5];
	for (int i = 0; i < 5; i++) {
	    if (r.getZug(i) == null) {
		bestZug[i] = ka[j++];
	    }
	    else {
		bestZug[i] = r.getZug(i);
	    }
	}
	if (j == 0){
            return bestZug;
        }
	bestScore = 1000;
	j=0;
	while (r.getZug(j) != null) {
	    Roboter[] ra = new Roboter[1];
	    ra[0] = r;
	    sf.doPhase(j+1,ra);
	    j++;
	}

        int len=0;
        while ((len<9)&&(ka[len]!=null))
            len++;
        Arrays.sort( ka, 0, len, Karte.INVERSE_PRIORITY_COMPARATOR );

	permut((BoardRoboter)r,ka,0);
	return bestZug;
    }

    /** We need one temp per level of recursion, however we don't want to
	create a new one on each call. */
    private BoardRoboter[] tmp = { new BoardRoboter(), new BoardRoboter(),
				   new BoardRoboter(), new BoardRoboter(),
				   new BoardRoboter(), new BoardRoboter() };
    private void permut(final BoardRoboter r, Karte[] ka, int recursionLevel) {
	if (r.getSchaden() == 10) return;
	int anzahl = 0;
	for (int i = 0; i < 5; i++)
	    if (r.getZug(i) != null) anzahl++;
	if (anzahl == 5) {   // end of recursion reached, 5 cards selected
            int diemalus=0;

	    // If we are standing on a conveyor belt, check what cards we need
	    // to not die next phase
            if (sf.bo(r.getX(),r.getY()).typ>=100){ // Belt
                for (int i=0;i<zuege.length;i++){
                    tmp[recursionLevel].initFrom(r);
                    tmp[recursionLevel].setZug(0,zuege[i]);
                    sf.doPhase(1,tmp[recursionLevel]);
                    if(tmp[recursionLevel].getSchaden()==10)
                        diemalus+=zuegemali[i];
                }
                if (diemalus==zuegemalisumme)
                    return; // we die surely, discard this choice
            }

	    int entf = sf.getBewertung(r,malus)+diemalus;
	    if (entf <= bestScore) {
		bestScore = entf;
		for (int i = 0; i < 5; i++)
                    bestZug[i] = r.getZug(i);
	    }
	    return;
	}

	for (int i = 0; i < 9; i++) {
	    if (ka[i] == null)
                continue;
	    Karte katemp = ka[i];
	    ka[i] = null; // play that card
            tmp[recursionLevel].initFrom( r );

	    int j = 0;
	    while (tmp[recursionLevel].getZug(j) != null) j++;
	    tmp[recursionLevel].setZug(j, katemp);
	    while ((j < 5) && (tmp[recursionLevel].getZug(j) != null)){
		sf.doPhase(j+1,tmp[recursionLevel]);
		j++;
	    }
	    permut(tmp[recursionLevel],ka,recursionLevel+1);
	    ka[i] = katemp;
	    // Skip cards with identical action
            while ((i<8)&&((ka[i+1]==null)||(ka[i+1].getaktion().equals(katemp.getaktion()))))
                i++;

	}
    }
}
