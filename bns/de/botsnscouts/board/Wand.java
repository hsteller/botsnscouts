package de.botsnscouts.board;

public class Wand{
    public boolean da;
    public int[] wandEl;      // 0: links oder oben
    //  1:rechts oder unten
    public int[] spez;      //   Laserstärke / Pusherphasen

    public Wand()
    {
	wandEl=new int[2];
	wandEl[0]=Spielfeld.WKEINS;
	wandEl[1]=Spielfeld.WKEINS;
	spez=new int[2];
	da=false;
    }
}
