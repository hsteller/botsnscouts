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

public class Permu {

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

    public Karte[] permutiere(Karte[] ka, Roboter r){
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
                //        System.err.println("Returne gültigen Zug");
            return bestZug;
        }
	bestScore = 1000;
	j=0;
	while (r.getZug(j) != null) {  // kann nicht ueber r.zug.length gehen, s. 3 Z weiter oben
	    Roboter[] ra = new Roboter[1];
	    ra[0] = r;
                //System.err.println("skipping register #"+j);
	    sf.doPhase(j+1,ra);
	    j++;
	}

            // sortiere ka

        Karte[] so=new Karte[9];
        int len=0;
        while ((len<9)&&(ka[len]!=null))
            len++;
        for (int i=0;i<len;i++){
            int highest=0;
            int highind=0;
            for (j=0;j<9;j++){
                if (ka[j]==null)
                    continue;
                if (ka[j].getprio()>highest){
                    highest=ka[j].getprio();
                    highind=j;
                }
            }
            so[i]=ka[highind];
            ka[highind]=null;
        }
        
	permut((BoardRoboter)r,so);
	return bestZug;
    }

    private void permut(BoardRoboter r, Karte[] ka) {
	if (r.getSchaden() == 10) return;
	int anzahl = 0;
	for (int i = 0; i < 5; i++)
	    if (r.getZug(i) != null) anzahl++;
	if (anzahl == 5) {
            int diemalus=0;
            
            if (sf.bo(r.getX(),r.getY()).typ>=100){ // Fliessband
                for (int i=0;i<zuege.length;i++){
                    BoardRoboter tmp = (BoardRoboter)Roboter.getCopy(r);
                    tmp.setZug(0,zuege[i]);
                    sf.doPhase(1,tmp);
                    if(tmp.getSchaden()==10) // wenn wir naechste Runde sterben ...
                        diemalus+=zuegemali[i];    
                }
                if (diemalus==zuegemalisumme)
                    return; // keine Chance ...
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
	    BoardRoboter rBackup = (BoardRoboter)Roboter.getCopy(r);
	    ka[i] = null; // ausspielen
	    int j = 0;
	    while (r.getZug(j) != null) j++;
	    r.setZug(j, katemp);
	    while ((j < 5) && (r.getZug(j) != null)){
		sf.doPhase(j+1,r);
		j++;
	    }
	    permut(r,ka);
	    ka[i] = katemp;
	    r = rBackup;
            
                // Skip cards with identical action

            while ((i<8)&&((ka[i+1]==null)||(ka[i+1].getaktion().equals(katemp.getaktion()))))
                i++;
            
	}
    }
}
