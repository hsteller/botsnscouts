package de.botsnscouts.board;

  /** Laser-Definition
   */
public class LaserDef {
    int strength;  // Staerke des Lasers
    int facing;    // generische Konstanten, s.u.
    int x;         // erstes bestrahltes Feld, x-Koordinate
    int y;         // erstes bestrahltes Feld, y-Koordinate
    int length;    // Anz. der Felder bis zur ersten Wand (Maximallaenge des Laserstrahls)
        
    public LaserDef(int xx,int yy,int f,int s,int l)
    {
	x=xx;
	y=yy;
	facing=f;
	strength=s;
	length=l;
    }
}


