package de.botsnscouts.board;

/**
 * Diese Klasse repraesentiert das Spielfeld. Sie soll von
 * Ausgabe, Spielern und Server verwendet werden (ggf.
 * extended).
 * @author: Dirk Materlik
 */

import de.botsnscouts.util.*;

public class Spielfeld implements de.botsnscouts.util.Directions, FloorConstants
{

  public static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance(Spielfeld.class);

  /** Preview-Image is possibly saved along with the tile */
  protected java.awt.Image img;

  /** Die Spielfeldgroesse */
  protected int sizeX,sizeY;

  /** Die Bodentypen
   *  2-dimensional   1. x-Koordinate
   *                  2. y-Koordinate
   */
  private Boden[][] boden;

  // Die Waende
  //
  /** 2-dimensionales Wall-Array vertikal */
  private Wall[][] vWall;    // vertikale Waende
  /** 2-dimensionales Wall-Array horizontal */
  protected Wall[][] hWall;    // horizontale Waende

  /** Die Flaggen */
  protected Ort[] flaggen;      // 1:x-Koordinate, 2:y-Koordinate, int:Flagge Nr. n-1
  protected String flaggenProbleme;  // falls Flaggen suboptimal plaziert sind

  /** Sicherungskopie des Spielfeldstrings */
  protected String SpielfeldString;

    public int getSizeX(){
	return sizeX;
    }
    public int getSizeY(){
	return sizeY;
    }

  /***** Konstruktoren *****/

  /** Initialisiert ein Spielfeld aus zweien. Zerstört unter Umständen Teile der hereingegebenen
      Spielfelder!
   */
//    // UNGETESTET!!!
//    public Spielfeld(Spielfeld s1, Spielfeld s2, boolean nebeneinander) throws FormatException{
//      CAT.debug("new Spielfeld called");
//	if (nebeneinander){
//	    sizeX=s1.sizeX+s2.sizeX;
//	    sizeY=s1.sizeY;
//	    if (sizeY!=s2.sizeY)
//		throw new FormatException("Die beiden Spielfelder sind nicht gleich hoch!");
//
//	    initArys();
//
//	    // Erstes reinkopieren
//	    for (int x=1;x<=s1.sizeX;x++)
//		for (int y=1;y<=sizeY;y++)
//		    boden[x][y]=s1.boden[x][y];
//	    for (int x=0;x<=s1.sizeX;x++)
//		for (int y=0;y<sizeY;y++)
//		    vWall[x][y]=s1.vWall[x][y];
//	    for (int x=0;x<s1.sizeX;x++)
//		for (int y=0;y<=sizeY;y++)
//		    hWall[x][y]=s1.hWall[x][y];
//
//	    // Zweites reinkopieren
//	    for (int x=1;x<=s2.sizeX;x++)
//		for (int y=1;y<=sizeY;y++)
//		    boden[x+s1.sizeX][y]=s2.boden[x][y];
//	    for (int x=0;x<=s2.sizeX;x++)
//		for (int y=0;y<sizeY;y++)
//		    vWall[x+s1.sizeX][y]=s2.vWall[x][y];
//	    for (int x=0;x<s2.sizeX;x++)
//		for (int y=0;y<=sizeY;y++)
//		    hWall[x+s1.sizeX][y]=s2.hWall[x][y];
//
//	    // Konflikte resolven
//	    for (int y=0;y<sizeY;y++){
//                Wall w = vWall[s1.sizeX][y];
// //		w.setExisting( s1.vWall[s1.sizeX][y].isExisting()||s2.vWall[0][y].isExisting() );
// //              w.copyElementNW( s1.vWall[s1.sizeX][y] );
//                // wall on border between boards:
//                if( s1.getVWall(s1.sizeX,y).isExisting() ||s2.getVWall(0,y).isExisting() ) {
//                    vWall[s1.sizeX][y] = w.getWithElementNW( s1.getVWall(s1.sizeX, y) );
// //                  w.copyElementNW( s1.vWall[s1.sizeX][y] );
//                }
//	    }
//
//	}else{ //untereinander
//	    sizeX=s1.sizeX;
//	    sizeY=s1.sizeY+s2.sizeY;
//	    if (sizeX!=s2.sizeX)
//		throw new FormatException("Die beiden Spielfelder sind nicht gleich breit!");
//
//	    initArys();
//
//	    // Erstes reinkopieren
//	    for (int x=1;x<=s1.sizeX;x++)
//		for (int y=1;y<=sizeY;y++)
//		    boden[x][s2.sizeY+y]=s1.boden[x][y];
//	    for (int x=0;x<=s1.sizeX;x++)
//		for (int y=0;y<sizeY;y++)
//		    vWall[x][s2.sizeY+y]=s1.vWall[x][y];
//	    for (int x=0;x<s1.sizeX;x++)
//		for (int y=0;y<=sizeY;y++)
//		    hWall[x][s2.sizeY+y]=s1.hWall[x][y];
//
//	    // Zweites reinkopieren
//	    for (int x=1;x<=s2.sizeX;x++)
//		for (int y=1;y<=sizeY;y++)
//		    boden[x][y]=s2.boden[x][y];
//	    for (int x=0;x<=s2.sizeX;x++)
//		for (int y=0;y<sizeY;y++)
//		    vWall[x][y]=s2.vWall[x][y];
//	    for (int x=0;x<s2.sizeX;x++)
//		for (int y=0;y<=sizeY;y++)
//		    hWall[x][y]=s2.hWall[x][y];
//
//	    // Konflikte resolven
//	    for (int x=0;x<sizeX;x++){
//                if ( s1.vWall[x][0].isExisting()||s2.vWall[x][s2.sizeY].isExisting() ) {
// //                 vWall[x][s2.sizeY] =
//                }
// //		hWall[x][s2.sizeY].setExisting( s1.vWall[x][0].isExisting()||s2.vWall[x][s2.sizeY].isExisting() );
// //		hWall[x][s2.sizeY].copyElementNW( s1.vWall[x][0] );
//	    }
//	}
//
//	SpielfeldString = getComputedString();
//    }

  /** Initialisiert ein neues Spielfeld.
    @param x Groesse in X-Richtung.
    @param y Groesse in Y-Richtung.
    @param kacheln Das Feld im f&uuml;r Netzkommunikation spezifizierten Format.
    @param f Die Flaggen als Orte.
    */
  public Spielfeld(int x, int y, String kacheln, Ort[] f) throws FormatException, FlaggenException
    {
      CAT.debug("new Spielfeld called");
      sizeX=x;
      sizeY=y;
      SpielfeldString=kacheln;

      initArys();

      int strpos=0;            // Current pos in the String

      Boden nbo;
      for (int zeile=sizeY;zeile > 0;zeile--){
	//parse ZwischenReihe (Nordwaende)
	for (int spalte=0;spalte < sizeX;spalte++){
          strpos = parseAndCreateWall(strpos, kacheln, hWall, spalte, zeile );
	}

	strpos=ParseUtils.assertws(kacheln,strpos);
        // one wall, then repeatedly floor & wall
        strpos = parseAndCreateWall(strpos, kacheln, vWall, 0, zeile-1 );
	for (int spalte=1;spalte <= sizeX;spalte++){
          strpos = parseAndCreateFloor( strpos, kacheln, boden, spalte, zeile );
          strpos = parseAndCreateWall(strpos, kacheln, vWall, spalte, zeile-1 );
	}
	strpos=ParseUtils.assertws(kacheln,strpos);
      }    // for zeile
      // parse last row of walls
      for (int spalte=0;spalte < sizeX;spalte++){
        strpos = parseAndCreateWall(strpos, kacheln, hWall, spalte, 0 );
      }

      checkFlaggen(f);
      flaggen=f;
    } //Konstruktor

    private void initArys(){
	// initialize arrays
	boden = new Boden[sizeX+2][sizeY+2];
	vWall = new Wall[sizeX+1][sizeY];
	hWall = new Wall[sizeX][sizeY+1];
	for (int i=0;i<=sizeX+1;i++){
	    boden[i][0]=new Boden();
	    boden[i][0].typ=BDGRUBE;
	    boden[i][sizeY+1]=new Boden();
	    boden[i][sizeY+1].typ=BDGRUBE;
	}
	for (int j=1;j<=sizeY;j++){
	    boden[0][j]=new Boden();
	    boden[0][j].typ=BDGRUBE;
	    boden[sizeX+1][j]=new Boden();
	    boden[sizeX+1][j].typ=BDGRUBE;
	}
    }

    protected void checkFlaggen(Ort[] f) throws FlaggenException
    {
      // prueft ob Flaggen regelkonform plaziert sind (sonst Exception)
      // und ob sie "gut" sind - sonst kann man die Probleme mit
      // getFlaggenProbleme() abfragen

      if (f==null){
	flaggenProbleme=Message.say("Spielfeld","mFlagProbNoFlagSet");
	return;
      }

      for (int i=0;i<f.length;i++){
	  if ((f[i].x>sizeX)||(f[i].y>sizeY))
	      throw new FlaggenException(Message.say("Spielfeld","eFlagNotInField",(i+1)));
	  if (bo(f[i].x,f[i].y).typ==BDGRUBE)
	      throw new FlaggenException(Message.say("Spielfeld","eFlagOnHole",(i+1)));
      }

      flaggenProbleme="";
      for (int i=0;i<f.length;i++){
	int anzwand=0;
	if (nw(f[i].x,f[i].y).isExisting())
	  anzwand++;
	if (ew(f[i].x,f[i].y).isExisting())
	  anzwand++;
	if (sw(f[i].x,f[i].y).isExisting())
	  anzwand++;
	if (ww(f[i].x,f[i].y).isExisting())
	  anzwand++;

	if (anzwand>2){
	    //Ludmila:String so geändert, daß keine Nummer angezeigt wird
	    // flaggenProbleme+=Message.say("Spielfeld","mFlagProbManyWalls",(i+1),anzwand); //original
	    flaggenProbleme+=Message.say("Spielfeld","mFlagProbManyWalls",anzwand);//geändert
	}

	if (bo(f[i].x,f[i].y).typ >= 100){ //Fliessband
	    //flaggenProbleme+=Message.say("Spielfeld","mFlagProbConvBelt",(i+1));//original
	    flaggenProbleme+=Message.say("Spielfeld","mFlagProbConvBelt");//gfeändert
	}
      }
    }

    public String getFlaggenProbleme()
    {
      return flaggenProbleme;
    }

/** Deep Magic, rotates LEFT */
  public String get90GradGedreht()
    {
      CAT.debug("get90GradGedreht() called");
      StringBuffer s=new StringBuffer();
      for (int x=sizeX;x>0;x--){
	for (int y=sizeY;y>0;y--){
	  // "obere" ZwischenReihe
          ow(x,y).writeReversed(s);
	}
	s.append('\n');
        nw(x,sizeX).write(s);
	for (int y=sizeY;y>0;y--){
	  //Boden
	  writeBoden(bo(x,y),s,true);
          sw(x,y).write(s);
	}
	s.append("\n");
      } //for x
      // unterste ZwischenReihe
      for (int y=sizeY;y>0;y--)
        ww(1,y).writeReversed(s);
      s.append("\n.\n");
      return new String(s);
    }

/* Well, and I hoped I'd never have to do this one -right- :-) */
    public String getComputedString()
        {
	  CAT.debug("getComputedString called");
            StringBuffer s=new StringBuffer();
            for (int y=sizeY;y>0;y--){
                for (int x=1;x<=sizeX;x++){
                        // obere ZwischenReihe
                    nw(x,y).write(s);
                }
                s.append("\n");
                ww(1,y).write(s);
                for (int x=1;x<=sizeX;x++){
                        //Boden
                    writeBoden(bo(x,y),s,false);
                    ow(x,y).write(s);
                }
                s.append("\n");
            } //for y
                // unterste ZwischenReihe
            for (int x=1;x<=sizeX;x++) {
                sw(x,1).write(s);
            }
            s.append("\n.\n");
	    return new String(s);
        }

  private void writeBoden(Boden b,StringBuffer s,boolean drehen)
    {
        b.write( s, drehen );
//      final String[] RUECK={ "N", "E", "S", "W" };
//
//      switch (b.typ){
//      case BDGRUBE:
//	s.append('G');
//	break;
//      case BDNORMAL:
//	s.append('B');
//	break;
//      case BDREPA:
//	s.append("R(");
//	s.append(b.spez);
//	s.append(')');
//	break;
//      case BDDREHEL:
//	s.append("D(");
//	s.append((b.spez==DUHRZ)?'R':'L');
//	s.append(')');
//	break;
//      default:
//	  int typ=b.typ;
//	  if (drehen){
//	      typ=(b.typ/10)*10;
//	      typ+=(((b.typ%10)+3)%4);
//	  }
//	s.append("F(");
//	switch (typ%10){
//	case FNORD:
//	  s.append("N,");
//	  break;
//	case FOST:
//	  s.append("E,");
//	  break;
//	case FSUED:
//	  s.append("S,");
//	  break;
//	case FWEST:
//	  s.append("W,");
//	  break;
//	} //switch richtung
//	s.append(typ/100);
//	s.append(",(");
//	if (((typ/10)%10)==2) {// gegen den Uhrzeigersinn
//	  s.append('(');
//	  s.append(RUECK[((typ%10)+3)%4]);
//	  s.append(",D(L))");
//	}
//	else if (((typ/10)%10)==3){ // im Uhrzeigersinn
//	  s.append('(');
//	  s.append(RUECK[((typ%10)+1)%4]);
//	  s.append(",D(R))");
//	}
//	else if (((typ/10)%10)==5){ // beides
//	    if (drehen){
//		s.append('(');
//		s.append(RUECK[((typ%10)+3)%4]);
//		s.append(",D(R))");
//		s.append('(');
//		s.append(RUECK[((typ%10)+1)%4]);
//		s.append(",D(L))");
//	    } else {
//		s.append("(");
//		s.append(RUECK[((typ%10)+1)%4]);
//		s.append(",D(R))");
//		s.append('(');
//		s.append(RUECK[((typ%10)+3)%4]);
//		s.append(",D(L))");
//	    }
//	}
//	s.append(")("); //crushers
//	if (b.spez>0)
//	  for (int i=1;i<6;i++){
//	    if (isCrusherActive(b.spez,i)){
//	      s.append(i);
//	      s.append(',');
//	    }
//	  }
//	s.append("))");
//	break;
//      } //switch typ
    }



    private int parseAndCreateWall(int strpos, String kacheln, Wall[][] walls, int a, int b)
    throws FormatException
    {
          int newpos = Wall.skipWallDef( strpos, kacheln );
          String wallString = kacheln.substring( strpos, newpos );
	  walls[a][b] = Wall.getWall( wallString );
          return newpos;
    }

    private int parseAndCreateFloor(int strpos, String kacheln, Boden[][] floor, int a, int b)
    throws FormatException
    {
	  Boden newFloor = new Boden();
	  int newpos = Boden.parseBoden(strpos, kacheln, newFloor);
	  floor[a][b] = newFloor;
          return newpos;
    }



  /***** public Instanzenmethoden *****/

  /** @return Alle Flaggen die existieren in einem Array. */
  public Ort[] getFlaggen()
    {
      return flaggen;
    }

  /** @return Den zugeordneten Netzwerkkommunikationsstring f&uuml;r das Spielfeld. */
  public String getSpielfeldString()
    {
      return SpielfeldString;
    }

  /***** protected Instanzenmethoden *****/

  /* Debug */
  protected void d(String s)
    {
      Global.debug(this, s);
    }
  protected final static void p(String s)
    {
      System.out.println(s);
    }
  protected final static void pn(String s)
    {
      System.out.print(s);
    }
    public boolean hasNorthWall( int x,int y) {
        return nw(x,y).isExisting();
    }

    public boolean hasSouthWall( int x,int y) {
        return sw(x,y).isExisting();
    }

    public boolean hasWestWall( int x,int y) {
        return ww(x,y).isExisting();
    }

    public boolean hasEastWall( int x,int y) {
        return ew(x,y).isExisting();
    }
  /* Bodentyp */
  public Boden bo(int x,int y) {
    return(boden[x][y]);
  }

  /* Nordwand */
  public Wall nw(int x,int y) {
    return(hWall[x-1][y]);
  }

  /* Ostwand */
  public Wall ow(int x,int y) {
    return(vWall[x][y-1]);
  }
  public Wall ew(int x,int y) {
    return(ow(x,y));
  }

  /* Suedwand */
  public Wall sw(int x,int y) {
    return(hWall[x-1][y-1]);
  }

  /* Westwand */
  public Wall ww(int x,int y) {
    return(vWall[x-1][y-1]);
  }


    public Wall getVWall(int a, int b) {
        return vWall[a][b];
    }

    public Wall getHWall(int a, int b) {
        return hWall[a][b];
    }

    public void setVWall(int a, int b, Wall wand) {
        vWall[a][b] = wand;
    }

    public void setHWall(int a, int b, Wall wand) {
        hWall[a][b] = wand;
    }

    public Boden getBoden(int a, int b) {
        return boden[a][b];
    }

    public void setBoden(int a, int b, Boden aBoden) {
        boden[a][b] = aBoden;
    }
  public void print()
    /* Rein zu Debuggingzwecken; gibt die Zahlen der internen Repräsentation aus */
    {
      p("Boden:");
      p("X="+boden.length+"; Y="+boden[0].length);
      p("");
      pn("\t");
      for (int x=0;x<=sizeX+1;x++)
	pn(x+"\t");
      p("");
      for (int y=sizeY+1;y>=0;y--){
	pn(y+"\t");
	for (int x=0;x<=sizeX+1;x++){
	  if(boden[x][y]==null)
	    pn("null\t");
	  else
	    pn(boden[x][y].typ+"|"+boden[x][y].spez+"\t");
	}
	p("");
      }

      p("");
      p("vertikale Wände:");
      p("X="+vWall.length+"; Y="+vWall[0].length);
      p("");
      pn("\t");
      for (int x=0;x<=sizeX;x++)
	pn(x+"\t");
      p("");
      for (int y=sizeY-1;y>=0;y--){
	pn(y+"\t");
	for (int x=0;x<=sizeX;x++){
          Wall wall = vWall[x][y];
	  if(wall==null)
	    pn("null\t");
          else if (wall.isExisting()) {
            StringBuffer sb = new StringBuffer();
            sb.append( wall.getNWDeviceType() ).append('|').append( wall.getNWDeviceInfo() );
            sb.append( '#' ).append( wall.getSEDeviceType() ).append('|');
            sb.append( wall.getSEDeviceInfo() ).append('\t');
            pn( sb.toString() );
          }
          else
            pn(".\t");
	}
	p("");
      }

      p("");
      p("horizontale Wände:");
      p("X="+hWall.length+"; Y="+hWall[0].length);
      p("");
      pn("\t");
      for (int x=0;x<sizeX;x++)
	pn(x+"\t");
      p("");
      for (int y=sizeY;y>=0;y--){
	pn(y+"\t");
	for (int x=0;x<sizeX;x++){
          Wall wall = hWall[x][y];
	  if(wall==null)
	    pn("null\t");
          else if (wall.isExisting()) {
            StringBuffer sb = new StringBuffer();
            sb.append( wall.getNWDeviceType() ).append('|').append( wall.getNWDeviceInfo() );
            sb.append( '#' ).append( wall.getSEDeviceType() ).append('|');
            sb.append( wall.getSEDeviceInfo() ).append('\t');
            pn( sb.toString() );
          }
          else
            pn(".\t");
	}
	p("");
      }
    }

}









