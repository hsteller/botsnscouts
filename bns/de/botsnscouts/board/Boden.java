package de.botsnscouts.board;

public class Boden{
    public int typ;           // generelle Bodenart
    public int spez;         //  spezielle Eigenschaften:
    //   Crusherphasen (Feld ist Crusher wenn spez>0 und Feld Fliessband)
    //   Drehrichtung von Drehelementen
    //   Staerke des Reparaturfeldes
    Boden()
      {
	spez=0;
	typ=Spielfeld.BDNORMAL;
      }
}
