package de.botsnscouts.board;

public class Wand{

    public static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance(Wand.class);  
    public boolean da;
    public final int[] wandEl = new int[2];      // 0: links oder oben
    //  1:rechts oder unten
    public final int[] spez = new int[2];      //   Laserstärke / Pusherphasen

    public Wand()
    {
	wandEl[0]=Spielfeld.WKEINS;
	wandEl[1]=Spielfeld.WKEINS;
	da=false;
    }
}
