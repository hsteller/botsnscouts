package de.botsnscouts.board;

/**
 * Diese Klasse repraesentiert das Spielfeld. Sie soll von
 * Ausgabe, Spielern und Server verwendet werden (ggf.
 * extended).
 * @author: Dirk Materlik
 */ 

import de.botsnscouts.util.*;

public class Spielfeld implements de.botsnscouts.util.Directions
{

  public static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance(Spielfeld.class);

  /***** Public Konstanten *****/

  // Wandgerätetypen
  // ================
   
  /** Kein Wandgerät in der Wand */
  public static final int WKEINS = 0;
  /**  Laser in der Wand */
  public static final int WLASER = 1;
  /**  Pusher in der Wand */
  public static final int WPUSHER = 2;
  
  //  Bodentypen
  //  ==========
  
  /** Boden Grube */
  public static final int BDGRUBE   = -1;
  /** Boden normaler Boden */
  public static final int BDNORMAL  =  0;
  /** Boden Reparaturfeld */
  public static final int BDREPA    =  1;
  /** Boden DrehElement */
  public static final int BDDREHEL  = 10;

  /** Drehelementrichtung im Uhrzeigersinn */
  public static final int DUHRZ   = 0;
  /** Drehelementrichtung gegen den Uhrzeigersinn */
  public static final int DGGUHRZ = 1;

  /* Fliessbaenderdesign : Endziffern stehen fuer: */
  /** Fliessband Richtung Norden */
  public static final int FNORD = 0;
  /** Fliessband Richtung Osten */
  public static final int FOST  = 1;
  /** Fliessband Richtung Süden */
  public static final int FSUED = 2;
  /** Fliessband Richtung Westen */
  public static final int FWEST = 3;

  // Fliessbänder - geradeaus
  // ========================
  /** Fliessband Richtung Norden, Geschwindigkeit 1 */
  public static final int FN1 = 100;
  /** Fliessband Richtung Osten,  Geschwindigkeit 1 */
  public static final int FO1 = 101;
  /** Fliessband Richtung Sueden, Geschwindigkeit 1 */
  public static final int FS1 = 102;
  /** Fliessband Richtung Westen, Geschwindigkeit 1 */
  public static final int FW1 = 103;

  /** Fliessband Richtung Norden, Geschwindigkeit 2 */
  public static final int FN2 = 200; //
  /** Fliessband Richtung Osten,  Geschwindigkeit 2 */
  public static final int FO2 = 201; // 
  /** Fliessband Richtung Sueden, Geschwindigkeit 2 */
  public static final int FS2 = 202; // 
  /** Fliessband Richtung Westen, Geschwindigkeit 2 */
  public static final int FW2 = 203; // 

    // Fliessbaender - abbiegen -  normal

  /** Fließband Abbiegen (Richtung) Norden von Westen*/
  public static final int NVW1 = 120;   
  /** Fließband Abbiegen (Richtung) Norden von Osten */
  public static final int NVO1 = 130;   
  /** Fließband Abbiegen (Richtung) Osten von Norden */
  public static final int OVN1 = 121;   
  /** Fließband Abbiegen (Richtung) Osten von Sueden */
  public static final int OVS1 = 131;   
  /** Fließband Abbiegen (Richtung) Süden von Westen */
  public static final int SVW1 = 132;   
  /** Fließband Abbiegen (Richtung) Süden von Osten */
  public static final int SVO1 = 122;    
  /** Fließband Abbiegen (Richtung) Westen von Norden */
  public static final int WVN1 = 133;   
  /** Fließband Abbiegen (Richtung) Westen von Süden */
  public static final int WVS1 = 123;   
  /** Fließband Abbiegen (Richtung)  Norden von  Westen oder Osten */
  public static final int NVWO1 = 150;   
  /** Fließband Abbiegen (Richtung)  Osten von   Norden oder Süden */
  public static final int OVNS1 = 151;   
  /** Fließband Abbiegen (Richtung)  Süden von  Westen oder Osten */
  public static final int SVWO1 = 152;   
  /** Fließband Abbiegen (Richtung)  Westen von  Nord oder Süden */
  public static final int WVNS1 = 153;           

  // Fliessbaender - abbiegen - express
  //
  /** Express-Fließband Abbiegen (Richtung) Norden von Westen (kommend) */
  public static final int NVW2 = 220;   
  /** Express-Fließband Abbiegen (Richtung) Norden von Osten */
  public static final int NVO2 = 230;   
  /** Express-Fließband Abbiegen (Richtung) Osten von Norden */
  public static final int OVN2 = 221;   
  /** Express-Fließband Abbiegen (Richtung) Osten von Sueden */
  public static final int OVS2 = 231;   
  /** Express-Fließband Abbiegen (Richtung) Süden von Westen */
  public static final int SVW2 = 232;   
  /** Express-Fließband Abbiegen (Richtung) Süden von Osten */
  public static final int SVO2 = 222;    
  /** Express-Fließband Abbiegen (Richtung) Westen von Norden */
  public static final int WVN2 = 233;   
  /** Express-Fließband Abbiegen (Richtung) Westen von Süden */
  public static final int WVS2 = 223;   
  /** Express-Fließband Abbiegen (Richtung)  Norden von  Westen oder Osten */
  public static final int NVWO2 = 250;   
  /** Express-Fließband Abbiegen (Richtung)  Osten von   Norden oder Süden */
  public static final int OVNS2 = 251;   
  /** Express-Fließband Abbiegen (Richtung)  Süden von  Westen oder Osten */
  public static final int SVWO2 = 252;   
  /** Express-Fließband Abbiegen (Richtung)  Westen von  Nord oder Süden */
  public static final int WVNS2 = 253;

  /***** protected Instanzenvariablen *****/

  /** Die Spielfeldgroesse */
  protected int sizeX,sizeY;
 
  /** Die Bodentypen 
   *  2-dimensional   1. x-Koordinate 
   *                  2. y-Koordinate
   */
  protected Boden[][] boden;
    
  // Die Waende
  // 
  /** 2-dimensionales Wand-Array vertikal */
  protected Wand[][] vWand;    // vertikale Waende
  /** 2-dimensionales Wand-Array horizontal */
  protected Wand[][] hWand;    // horizontale Waende

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
    // UNGETESTET!!!
    public Spielfeld(Spielfeld s1, Spielfeld s2, boolean nebeneinander) throws FormatException{
      CAT.debug("new Spielfeld called");
	if (nebeneinander){
	    sizeX=s1.sizeX+s2.sizeX;
	    sizeY=s1.sizeY;
	    if (sizeY!=s2.sizeY)
		throw new FormatException("Die beiden Spielfelder sind nicht gleich hoch!");
	    
	    initArys();
	    
	    // Erstes reinkopieren
	    for (int x=1;x<=s1.sizeX;x++)
		for (int y=1;y<=sizeY;y++)
		    boden[x][y]=s1.boden[x][y];		    
	    for (int x=0;x<=s1.sizeX;x++)
		for (int y=0;y<sizeY;y++)
		    vWand[x][y]=s1.vWand[x][y];
	    for (int x=0;x<s1.sizeX;x++)
		for (int y=0;y<=sizeY;y++)
		    hWand[x][y]=s1.hWand[x][y];

	    // Zweites reinkopieren
	    for (int x=1;x<=s2.sizeX;x++)
		for (int y=1;y<=sizeY;y++)
		    boden[x+s1.sizeX][y]=s2.boden[x][y];
	    for (int x=0;x<=s2.sizeX;x++)
		for (int y=0;y<sizeY;y++)
		    vWand[x+s1.sizeX][y]=s2.vWand[x][y];
	    for (int x=0;x<s2.sizeX;x++)
		for (int y=0;y<=sizeY;y++)
		    hWand[x+s1.sizeX][y]=s2.hWand[x][y];

	    // Konflikte resolven
	    for (int y=0;y<sizeY;y++){
		vWand[s1.sizeX][y].da=s1.vWand[s1.sizeX][y].da||s2.vWand[0][y].da;
		vWand[s1.sizeX][y].wandEl[0]=s1.vWand[s1.sizeX][y].wandEl[0];
		vWand[s1.sizeX][y].spez[0]=s1.vWand[s1.sizeX][y].spez[0];
	    }
		    
	}else{ //untereinander
	    sizeX=s1.sizeX;
	    sizeY=s1.sizeY+s2.sizeY;
	    if (sizeX!=s2.sizeX)
		throw new FormatException("Die beiden Spielfelder sind nicht gleich breit!");
	    
	    initArys();
	    
	    // Erstes reinkopieren
	    for (int x=1;x<=s1.sizeX;x++)
		for (int y=1;y<=sizeY;y++)
		    boden[x][s2.sizeY+y]=s1.boden[x][y];		    
	    for (int x=0;x<=s1.sizeX;x++)
		for (int y=0;y<sizeY;y++)
		    vWand[x][s2.sizeY+y]=s1.vWand[x][y];
	    for (int x=0;x<s1.sizeX;x++)
		for (int y=0;y<=sizeY;y++)
		    hWand[x][s2.sizeY+y]=s1.hWand[x][y];

	    // Zweites reinkopieren
	    for (int x=1;x<=s2.sizeX;x++)
		for (int y=1;y<=sizeY;y++)
		    boden[x][y]=s2.boden[x][y];
	    for (int x=0;x<=s2.sizeX;x++)
		for (int y=0;y<sizeY;y++)
		    vWand[x][y]=s2.vWand[x][y];
	    for (int x=0;x<s2.sizeX;x++)
		for (int y=0;y<=sizeY;y++)
		    hWand[x][y]=s2.hWand[x][y];

	    // Konflikte resolven
	    for (int x=0;x<sizeX;x++){
		hWand[x][s2.sizeY].da=s1.vWand[x][0].da||s2.vWand[x][s2.sizeY].da;
		vWand[x][s2.sizeY].wandEl[0]=s1.vWand[x][0].wandEl[0];
		vWand[x][s2.sizeY].spez[0]=s1.vWand[x][0].spez[0];
	    }	    
	}

	SpielfeldString = getComputedString();
    }

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
      Wand neu;
      Boden nbo;
      for (int zeile=sizeY;zeile > 0;zeile--){      // Anfang parsen
	//parse ZwischenReihe (Nordwaende)
	for (int spalte=0;spalte < sizeX;spalte++){
	  neu=new Wand();
	  strpos=parseWand(strpos,kacheln,neu);
	  hWand[spalte][zeile]=neu;
	  //d("spalte="+spalte+"; zeile="+zeile+";neu: "+neu.wandEl[0]+"|"+neu.spez[0]+(neu.da?"#":"_")+neu.wandEl[1]+"|"+neu.spez[1]);
	}//for spalte, ZwischenR-parsen
	strpos=assertws(kacheln,strpos);
	//parse erste Wand
	neu=new Wand();
	strpos=parseWand(strpos,kacheln,neu);
	//parse Boden,Wand
	vWand[0][zeile-1]=neu;
	for (int spalte=1;spalte <= sizeX;spalte++){
	  nbo=new Boden();
	  strpos=parseBoden(strpos,kacheln,nbo);
	  boden[spalte][zeile]=nbo;
	  neu=new Wand();
	  //d("<init>: calling parseWand; pos="+strpos+"; spalte="+spalte+"; zeile="+zeile);
	  strpos=parseWand(strpos,kacheln,neu);
	  vWand[spalte][zeile-1]=neu;
	}
	strpos=assertws(kacheln,strpos);
      }    // for zeile
      // parse unterste ZwischenReihe
      for (int spalte=0;spalte < sizeX;spalte++){
	neu=new Wand();
	strpos=parseWand(strpos,kacheln,neu);
	hWand[spalte][0]=neu;                     // Ende parsen
      }

      checkFlaggen(f);
      flaggen=f;
    } //Konstruktor

    private void initArys(){
	// initialize arrays
	boden = new Boden[sizeX+2][sizeY+2];
	vWand = new Wand[sizeX+1][sizeY];
	hWand = new Wand[sizeX][sizeY+1];
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
	if (nw(f[i].x,f[i].y).da)
	  anzwand++;
	if (ew(f[i].x,f[i].y).da)
	  anzwand++;
	if (sw(f[i].x,f[i].y).da)
	  anzwand++;
	if (ww(f[i].x,f[i].y).da)
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

/** Deep Magic */
  public String get90GradGedreht()
    {
      CAT.debug("get90GradGedreht() called");
      StringBuffer s=new StringBuffer();
      for (int x=sizeX;x>0;x--){
	for (int y=sizeY;y>0;y--){
	  // "obere" ZwischenReihe
	  Wand w = ow(x,y);
	  writeWandH(w,s);
	}
	s.append('\n');
	writeWand(nw(x,sizeX),s);
	for (int y=sizeY;y>0;y--){
	  //Boden
	  writeBoden(bo(x,y),s,true);
	  writeWand(sw(x,y),s);
	}
	s.append("\n");
      } //for x
      // unterste ZwischenReihe
      for (int y=sizeY;y>0;y--)
	writeWandH(ww(1,y),s);
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
                    Wand w = nw(x,y);
                    writeWand(w,s);
                }
                s.append("\n");
                writeWand(ww(1,y),s);
                for (int x=1;x<=sizeX;x++){
                        //Boden
                    writeBoden(bo(x,y),s,false);
                    writeWand(ow(x,y),s);
                }
                s.append("\n");
            } //for y
                // unterste ZwischenReihe
            for (int x=1;x<=sizeX;x++)
                writeWand(sw(x,1),s);
            s.append("\n.\n");
	    return new String(s);
        }

  private void writeWandH(Wand w,StringBuffer s)
    {
	switch (w.wandEl[1]){
	case WLASER:
	    s.append("[L(");
	    s.append(w.spez[1]);
	    s.append(")");
	    break;
	case WPUSHER:
	    s.append("[S(");
	    for (int i=1;i<6;i++){
	      if (isPusherActive(w.spez[1],i)){
		s.append(i);
		s.append(',');
	      }
	    }
	s.append(')');
	break;
      }
      s.append(w.da?'#':'_');
      switch (w.wandEl[0]){
      case WLASER:
	s.append("L(");
	s.append(w.spez[0]);
	s.append(")]");
	break;
      case WPUSHER:
	s.append("S(");
	for (int i=1;i<6;i++){
	  if (isPusherActive(w.spez[0],i)){
	    s.append(i);
	    s.append(',');
	  }
	}
	s.append(")]");
	
	break;
      }
    }

  private void writeWand(Wand w,StringBuffer s)
    {
      switch (w.wandEl[0]){
      case WLASER:
	s.append("[L(");
	s.append(w.spez[0]);
	s.append(')');
	break;
      case WPUSHER:
	s.append("[S(");
	for (int i=1;i<6;i++){
	  if (isPusherActive(w.spez[0],i)){
	    s.append(i);
	    s.append(',');
	  }
	}
	s.append(')');
	break;
      }
      s.append(w.da?'#':'_');
      switch (w.wandEl[1]){
      case WLASER:
	s.append("L(");
	s.append(w.spez[1]);
	s.append(")]");
	break;
      case WPUSHER:
	s.append("S(");
	for (int i=1;i<6;i++){
	  if (isPusherActive(w.spez[1],i)){
	    s.append(i);
	    s.append(',');
	  }
	}
	s.append(")]");
	break;
      }
    }

  private void writeBoden(Boden b,StringBuffer s,boolean drehen)
    {
      final String[] RUECK={ "N", "E", "S", "W" };

      switch (b.typ){
      case BDGRUBE:
	s.append('G');
	break;
      case BDNORMAL:
	s.append('B');
	break;
      case BDREPA:
	s.append("R(");
	s.append(b.spez);
	s.append(')');
	break;
      case BDDREHEL:
	s.append("D(");
	s.append((b.spez==DUHRZ)?'R':'L');
	s.append(')');
	break;
      default:
	  int typ=b.typ;
	  if (drehen){
	      typ=(b.typ/10)*10;
	      typ+=(((b.typ%10)+3)%4);
	  }
	s.append("F(");
	switch (typ%10){
	case FNORD:
	  s.append("N,");
	  break;
	case FOST:
	  s.append("E,");
	  break;
	case FSUED:
	  s.append("S,");
	  break;
	case FWEST:
	  s.append("W,");
	  break;
	} //switch richtung
	s.append(typ/100);
	s.append(",(");
	if (((typ/10)%10)==2) {// gegen den Uhrzeigersinn
	  s.append('(');
	  s.append(RUECK[((typ%10)+3)%4]);
	  s.append(",D(L))");
	}
	else if (((typ/10)%10)==3){ // im Uhrzeigersinn
	  s.append('(');
	  s.append(RUECK[((typ%10)+1)%4]);
	  s.append(",D(R))");
	}
	else if (((typ/10)%10)==5){ // beides 
	    if (drehen){
		s.append('(');
		s.append(RUECK[((typ%10)+3)%4]);
		s.append(",D(R))");
		s.append('(');
		s.append(RUECK[((typ%10)+1)%4]);
		s.append(",D(L))");
	    } else {
		s.append("(");
		s.append(RUECK[((typ%10)+1)%4]);
		s.append(",D(R))");
		s.append('(');
		s.append(RUECK[((typ%10)+3)%4]);
		s.append(",D(L))");
	    }
	}
	s.append(")("); //crushers
	if (b.spez>0)
	  for (int i=1;i<6;i++){
	    if (isCrusherActive(b.spez,i)){
	      s.append(i);
	      s.append(',');
	    }
	  }
	s.append("))");
	break;
      } //switch typ
    }
  
  private int parseBoden(int pos,String s,Boden neu) throws FormatException
    {
      if (is(s,pos,'B')){
	neu.typ=BDNORMAL;
	pos++;
      }
      else if(is(s,pos,'G')){
	neu.typ=BDGRUBE;
	pos++;
      }
      else if(is(s,pos,'D')){
	neu.typ=BDDREHEL;
	pos++;
	assert(s,pos++,'(');
	if(is(s,pos,'L'))
	  neu.spez=DGGUHRZ;
	else if (is(s,pos,'R'))
	  neu.spez=DUHRZ;
	else // Keines der erlaubten Zeichen 'LR' in Position "pos"
	  throw new FormatException(Message.say("Spielfeld","xCharNotAllowed",pos,"LR"));
	pos++;
	assert(s,pos++,')');
      }
      else if(is(s,pos,'R')){
	neu.typ=BDREPA;
	pos++;
	assert(s,pos++,'(');
	neu.spez=java.lang.Character.digit(s.charAt(pos++),10);
	assert(s,pos++,')');
      }
      else if(is(s,pos,'F')){
	pos++;
	assert(s,pos++,'(');
	int typus;
	if (is(s,pos,'N'))
	  typus=FNORD;
	else if (is(s,pos,'E'))
	  typus=FOST;
	else if (is(s,pos,'S'))
	  typus=FSUED;
	else if (is(s,pos,'W'))
	  typus=FWEST;
	else //Keines der erlaubten Zeichen 'NEWS' in Position "pos"
	    throw new FormatException(Message.say("Spielfeld","xCharNotAllowed",pos,"NEWS"));
	pos++;
	assert(s,pos++,',');
	typus+=100*(java.lang.Character.digit(s.charAt(pos++),10));
	assert(s,pos++,',');
	assert(s,pos++,'(');
	if(is(s,pos,'(')){
	  pos++;
	  char fromR=s.charAt(pos++);
	  assert(s,pos++,',');
	  assert(s,pos++,'D');
	  assert(s,pos++,'(');
	  char drehR=s.charAt(pos++);
	  typus=drehungLegal(typus,fromR,drehR,pos);
	  assert(s,pos++,')');
	  assert(s,pos++,')');
	}
	if(is(s,pos,'(')){
	  pos++;
	  char fromR=s.charAt(pos++);
	  assert(s,pos++,',');
	  assert(s,pos++,'D');
	  assert(s,pos++,'(');
	  char drehR=s.charAt(pos++);
	  typus=drehungLegal(typus,fromR,drehR,pos);
	  assert(s,pos++,')');
	  assert(s,pos++,')');
	}
	assert(s,pos++,')');
	assert(s,pos++,'(');
	int crusher=0;
	while(java.lang.Character.isDigit(s.charAt(pos))){
	  crusher+=(int)java.lang.Math.pow(2,java.lang.Character.digit(s.charAt(pos++),10)-1);
	  //d("parseFließbandCrusher: read "+s.charAt(pos-1)+"; crusher="+crusher);
	  assert(s,pos++,',');
	  //if(((typus%100)/10)>1)
	  //  throw new FormatException("Keine Crusher auf Drehfliessbaendern! Problem nahe Zeichen "+pos);
	  //else if (((typus%100)/10)==0) //sonst ist's schon erhöht
	  //  typus+=10;                                                 
	}
	assert(s,pos++,')');
	assert(s,pos++,')');
	neu.typ=typus;
	if(crusher!=0)
	  neu.spez=crusher;
      }
      else// Keines der erlaubten Zeichen 'BGDRF' in Position "pos"
	throw new FormatException(Message.say("Spielfeld","xCharNotAllowed",pos,"BGDRF"));
      return pos;
    }
  private int drehungLegal(int typus,char from,char dreh,int pos) throws FormatException
    {
      int t=typus;
      switch (typus%10){
      case FNORD:
	switch (from){
	case 'W':
	  t+=20;
	  break;
	case 'E':
	  t+=30;
	  break;
	default:// Es werden nur vernuenftige Drehfliessbaender unterstuetzt. Problem bei Zeichen "pos"
	  throw new FormatException(Message.say("Spielfeld","xTurnConvBeltNoSense",pos));
	}
	break;
      case FOST:
	switch(from){
	case 'N':
	  t+=20;
	  break;
	case 'S':
	  t+=30;
	  break;
	default:// Es werden nur vernuenftige Drehfliessbaender unterstuetzt. Problem bei Zeichen "pos"
	  throw new FormatException(Message.say("Spielfeld","xTurnConvBeltNoSense",pos));
	}
	break;                        
      case FSUED:
	switch(from){
	case 'E':
	  t+=20;
	  break;
	case 'W':
	  t+=30;
	  break;
	default:// Es werden nur vernuenftige Drehfliessbaender unterstuetzt. Problem bei Zeichen "pos"
	  throw new FormatException(Message.say("Spielfeld","xTurnConvBeltNoSense",pos));
	}
	break;
      case FWEST:
	switch(from){
	case 'S':
	  t+=20;
	  break;
	case 'N':
	  t+=30;
	  break;
	default:// Es werden nur vernuenftige Drehfliessbaender unterstuetzt. Problem bei Zeichen "pos"
	  throw new FormatException(Message.say("Spielfeld","xTurnConvBeltNoSense",pos));
	}
	break;
      }
      return t;
    }
  private int parseWand(int strpos,String kacheln,Wand neu) throws FormatException
    {
      //d("parseWand: called with strpos="+strpos);
      if (is(kacheln,strpos,'#')){
	neu.da=true;
	strpos++;
      }
      else if(is(kacheln,strpos,'_')){
	neu.da=false;
	strpos++;
	return strpos;
      }
      else if(is(kacheln,strpos,'[')){
	strpos++;
	neu.da=true;
	if(is(kacheln,strpos,'L')){
	  //d("Laser [0] found");
	  strpos=parseL(++strpos,kacheln,neu,0);
	}
	else if(is(kacheln,strpos,'S')){
	  //d("Schieber [0] found");
	  strpos=parseS(++strpos,kacheln,neu,0);
	}
	assert(kacheln,strpos++,'#');     
      }
      else // "Fand keinen der erlaubten Chars '#_[' in Position "strpos"; da ist:"kacheln.charAt(strpos)"
	throw new FormatException(Message.say("Spielfeld","xCharsNotFound","#_[",strpos,""+kacheln.charAt(strpos)));
      if(is(kacheln,strpos,'L')){
	//d("Laser [1] found");
	strpos=parseL(++strpos,kacheln,neu,1);
	assert(kacheln,strpos++,']');
      }
      else if(is(kacheln,strpos,'S')){
	//d("Schieber [1] found");
	strpos=parseS(++strpos,kacheln,neu,1);
	assert(kacheln,strpos++,']');
      }
      return strpos;
    }
  private int parseS(int pos,String s,Wand it,int index) throws FormatException
    {
      assert(s,pos++,'(');
      int tmp=0;
      while (!is(s,pos,')')){
	int digit=java.lang.Character.digit(s.charAt(pos++),10);
	tmp+=(int)java.lang.Math.pow(2,digit-1);
	//d("parseS: read "+s.charAt(pos-1)+"; tmp is now: "+tmp);
	assert(s,pos++,',');
      }
      assert(s,pos++,')');
      it.wandEl[index]=WPUSHER;
      it.spez[index]=tmp;
      return pos;
    }                   
  private int parseL(int pos,String s,Wand it,int index) throws FormatException
    {
      assert(s,pos++,'(');
      int str=java.lang.Character.digit(s.charAt(pos++),10);
      assert(s,pos++,')');
      it.wandEl[index]=WLASER;
      it.spez[index]=str;
      return pos;
    }        
  private static boolean is(String s,int pos,char c)
    {
      return s.charAt(pos)==c;
    }
  private void assert(String s,int pos,char c) throws FormatException
    {   
      //d("assert: Erwarte "+c+" an Pos "+pos+" ;da ist "+s.charAt(pos));
      if (s.charAt(pos)!=c)
	  throw new FormatException(Message.say("Spielfeld","xExpectedChar",c,pos)); // "c" erwartet an Position "pos"
    }
  private static int assertws(String s,int pos) throws FormatException
    {
      if (!(java.lang.Character.isWhitespace(s.charAt(pos++))))
	throw new FormatException(Message.say("Spielfeld","xNoWhitespaceAtPos",(pos-1)));
      while (java.lang.Character.isWhitespace(s.charAt(pos)))
	pos++;
      return pos;
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
      p("X="+vWand.length+"; Y="+vWand[0].length);
      p("");
      pn("\t");
      for (int x=0;x<=sizeX;x++)
	pn(x+"\t");
      p("");
      for (int y=sizeY-1;y>=0;y--){
	pn(y+"\t");
	for (int x=0;x<=sizeX;x++){
	  if(vWand[x][y]==null)
	    pn("null\t");
	  else
	    pn(vWand[x][y].da?vWand[x][y].wandEl[0]+"|"+vWand[x][y].spez[0]+"#"+vWand[x][y].wandEl[1]+"|"+vWand[x][y].spez[1]+"\t":".\t");
	}
	p("");
      }

      p("");
      p("horizontale Wände:");
      p("X="+hWand.length+"; Y="+hWand[0].length);
      p("");
      pn("\t");
      for (int x=0;x<sizeX;x++)
	pn(x+"\t");
      p("");
      for (int y=sizeY;y>=0;y--){
	pn(y+"\t");
	for (int x=0;x<sizeX;x++){
	  if(hWand[x][y]==null)
	    pn("null\t");
	  else
	    pn(hWand[x][y].da?hWand[x][y].wandEl[0]+"|"+hWand[x][y].spez[0]+"#"+hWand[x][y].wandEl[1]+"|"+hWand[x][y].spez[1]+"\t":".\t");
	}
	p("");
      }
    }           


  /* Bodentyp */
  public Boden bo(int x,int y) {
    return(boden[x][y]);
  }
  
  /* Nordwand */
  public Wand nw(int x,int y) {
    return(hWand[x-1][y]);
  }

  /* Ostwand */
  public Wand ow(int x,int y) {
    return(vWand[x][y-1]);
  }
  public Wand ew(int x,int y) {
    return(ow(x,y));
  }

  /* Suedwand */
  public Wand sw(int x,int y) {
    return(hWand[x-1][y-1]);
  }

  /* Westwand */
  public Wand ww(int x,int y) {
    return(vWand[x-1][y-1]);
  }

  /** Gibt true zurueck, wenn ein Crusher mit Spezifikationszahl spez
    in phase aktiv ist. False sonst.
    */
  public boolean isCrusherActive(int spez,int phase)
    {
      return((spez>>(phase-1))%2==1);
    }
  /** Gibt true zurueck, wenn ein Pusher mit Spezifikationszahl spez
    in phase aktiv ist. False sonst.
    */
  public boolean isPusherActive(int spez,int phase)
    {
      return((spez>>(phase-1))%2==1);
    }
}









