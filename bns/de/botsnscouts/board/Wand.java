package de.botsnscouts.board;

public class Wand{
    boolean da;
    int[] wandEl;      // 0: links oder oben
    //  1:rechts oder unten
    int[] spez;      //   Laserstärke / Pusherphasen

    Wand()
    {
	wandEl=new int[2];
	wandEl[0]=Spielfeld.WKEINS;
	wandEl[1]=Spielfeld.WKEINS;
	spez=new int[2];
	da=false;
    }
}
