package de.botsnscouts.autobot;

/**
 * Permu-Klasse
 * @author Dirk, Lukasz
 * $Author$
 */

import de.botsnscouts.board.SpielfeldKS;
import de.botsnscouts.util.*;
import de.botsnscouts.board.BoardRoboter;

class Permu {

    SpielfeldKS sf;
    Karte[] bestZug;
    int bestScore;
    int malus;

    private static final String[] zuege = { "RL", "M1", "M2", "M3", "BU" };
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
        
	permut(r,so);
	return bestZug;
    }

    private void permut(Roboter r, Karte[] ka) {
	if (r.getSchaden() == 10) return;
	int anzahl = 0;
	for (int i = 0; i < 5; i++)
	    if (r.getZug(i) != null) anzahl++;
	if (anzahl == 5) {
            int malus=0;
            
            if (sf.bo(r.getX(),r.getY()).typ>=100){ // Fliessband
                Roboter[] tmp=new Roboter[1];
                for (int i=0;i<zuege.length;i++){
                    tmp[0] = new Roboter(r);
                    tmp[0].setZug(0,new Karte(110,zuege[i]));
                    sf.doPhase(1,tmp);
                    if(tmp[0].getSchaden()==10) // wenn wir naechste Runde sterben ...
                        malus+=zuegemali[i];    
                }
                if (malus==zuegemalisumme)
                    return; // keine Chance ...
            }
            
	    int entf = sf.getBewertung(r,malus)+malus;
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
	    Roboter rtemp = new Roboter(r);
	    ka[i] = null; // ausspielen
	    int j = 0;
	    while (r.getZug(j) != null) j++;
	    r.setZug(j, katemp);
	    while ((j < 5) && (r.getZug(j) != null)){
		Roboter[] ra = new Roboter[1];
		ra[0] = r;
		sf.doPhase(j+1,ra);
		j++;
	    }
	    permut(r,ka);
	    ka[i] = katemp;
	    r = rtemp;
            
                // Karten mit derselben Aktion ueberspringen; die haben
                // an dieser Stelle dieselben Auswrikungen

            while ((i<8)&&((ka[i+1]==null)||(ka[i+1].getaktion().equals(katemp.getaktion()))))
                i++;
            
	}
    }
}
