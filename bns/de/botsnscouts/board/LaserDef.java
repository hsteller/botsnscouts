package de.botsnscouts.board;

  /** Laser-Definition
   */
public class LaserDef {
    public int strength;  // Staerke des Lasers
    public int facing;    // generische Konstanten, s.u.
    public int x;         // erstes bestrahltes Feld, x-Koordinate
    public int y;         // erstes bestrahltes Feld, y-Koordinate
    public int length;    // Anz. der Felder bis zur ersten Wand (Maximallaenge des Laserstrahls)
        
    public LaserDef(int xx,int yy,int f,int s,int l)
    {
	x=xx;
	y=yy;
	facing=f;
	strength=s;
	length=l;
    }
}


