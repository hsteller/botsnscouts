package de.botsnscouts.board;

import java.util.Vector;
import java.util.Enumeration;


import de.botsnscouts.util.*;
import de.botsnscouts.server.Server;

/** Das intelligente Spielfeld
 * @author: Dirk Materlik, Gero Eggers
 */
public class SpielfeldSim extends Spielfeld
{
  protected boolean debugmeldungen=true; // Damit mehrere KIs auf einem
                                         // Rechner sich synchronisieren

  public static final int LNORD=0; // Laser nach Norden
  public static final int LOST=1;  //            Osten
  public static final int LSUED=2; //            Sueden
  public static final int LWEST=3; //            Westen
  
  /* Die Laser - um sie schnell iterieren zu können 
     Hier sollen _nur_ LaserDef's rein!
     */
  protected Vector lasers;

  /* Das Serverobjekt, das von jeder &Auml;nderung benachrichtigt werden soll
     und seinerseits die Ausgabekan&auml;le informiert. */
  protected Server server;

  /* Enthaelt bewegte Roboter, um String[] erzeugen zu koennen */
  protected boolean[] bewegt;

    private BoardRoboter[] br=new BoardRoboter[1]; /* Singleton-Array um einen Robi effizient casten zu können*/

    /** Contains the laser stats*/
    private StatsList stats=null;
    private Stats actualStats=null;

public Vector getLasers(){
	return lasers;
    }

  private void bewegt2false()
    {
      for (int i=0;i<bewegt.length;i++)
	bewegt[i]=false;
    }
  
  /* Benachrichtigt die Ausgaben von Aenderungen */
  private void benachrichtige(BoardRoboter[] robbis){
    if (server==null)
      return;
    
    int anz=0;
    for (int i=0;i<bewegt.length;i++)
      if (bewegt[i])
	anz++;
    
    if (anz==0)
      return;
    
    String[] s=new String[anz];
    int j=0;
    
    for (int i=0;i<bewegt.length;i++)
      if (bewegt[i])
	s[j++]=robbis[i].getName();

    String str="Benachrichtige Ausgaben. Geaenderte Robs: ";
    for (int i=0;i<s.length;i++)
      str=str+s[i]+";";
    
    //d(str);

    server.ausgabenBenachrichtigen(s);

    bewegt2false();
  }

    /** Schickt eine Nachricht an alle Ausgaben, falls wir einen Server haben
     */
    public void ausgabenMsg(String id, String[] args){
	if (server != null)
	    server.ausgabenMsg(id,args);
    }
    private void ausgabenMsgString(String id, String arg){
	if (server == null)
	    return;
	String[] tmp=new String[1];
	tmp[0]=arg;
	ausgabenMsg(id,tmp);
    }
    private void ausgabenMsgString2(String id, String arg1, String arg2){
	if (server==null)
	    return;
	String[] tmp=new String[2];
	tmp[0]=arg1;
	tmp[1]=arg2;
	ausgabenMsg(id,tmp);
    }

   
    
    /** Creates the internal StatsList.
    @param sl The robots that should be managed in the StatsList 
     */
    public void initStats (StatsList sl)  {
	stats= sl;
    }

  /** Initialisiert mit Unterst&uuml;tzung f&uuml;r Ausgabenbenachrichtigung.
    @param kacheln Das Feld im f&uuml;r Netzkommunikation spezifizierten Format.
    @param flaggen Die Flaggen im f&uuml;r Netzkommunikation spezifizierten Format.
    @param al Ein Array von KommServerAusgabe-Objekten, die bei jeder &Auml;nderung benachrichtigt werden sollen.
    @see KommServerAusgabe
    */
  public SpielfeldSim(int sx,int sy,String kacheln, Ort[] flaggen, Server s) throws FormatException, FlaggenException

    {
      super(sx,sy,kacheln,flaggen);
      server=s;

      actualStats= new Stats("foo");

      // Baue die LaserListe auf.
      lasers=new Vector();
      for (int x=1;x<=sizeX;x++)
	for(int y=1;y<=sizeY;y++){
	  Wand w=nw(x,y);
	  //d("x="+x+"; y="+y+";w: "+w.wandEl[0]+"|"+w.spez[0]+(w.da?"#":"_")+w.wandEl[1]+"|"+w.spez[1]);
	  if (w.da&&(w.wandEl[1]==WLASER)){  // Laser nach S
	      //d("LaserSued");
	    int length=findLLength(x,y,LSUED);
	    LaserDef neu=new LaserDef(x,y,LSUED,w.spez[1],length);
	    lasers.addElement(neu);
	  }
	  w=ow(x,y);
	  if (w.da&&(w.wandEl[0]==WLASER)){  // Laser nach W
	      //d("LaserWest");
	    int length=findLLength(x,y,LWEST);
	    LaserDef neu=new LaserDef(x,y,LWEST,w.spez[0],length);
	    lasers.addElement(neu);
	  }
	  w=sw(x,y);
	  if (w.da&&(w.wandEl[0]==WLASER)){ // Laser nach N
	      //d("LaserNord");
	    int length=findLLength(x,y,LNORD);
	    LaserDef neu=new LaserDef(x,y,LNORD,w.spez[0],length);
	    lasers.addElement(neu);
	  }
	  w=ww(x,y);
	  if (w.da&&(w.wandEl[1]==WLASER)){ // Laser nach O
	      //d("LaserOst");
	    int length=findLLength(x,y,LOST);
	    LaserDef neu=new LaserDef(x,y,LOST,w.spez[1],length);
	    lasers.addElement(neu);
	  }
	}
    }

    protected int findLLength(int x,int y,int richtung) // von private nach protected (hendrik)
    {
      // Findet maximale Länge eines Lasers, dessen erstes
      // Feld (x,y) ist und dessen Ausrichtung richtung ist.
      // d("findLLength: x="+x+"; y="+y+"; r="+richtung);
      int l=0;
      if (richtung==LOST)
	while((x+l<=sizeX)&&(!ow(x+l,y).da))
	  l++;
      if (richtung==LWEST)
	while((x-l>0)&&(!ww(x-l,y).da))
	  l++;
      if (richtung==LNORD)
	while((y+l<=sizeY)&&(!nw(x,y+l).da))
	  l++;
      if (richtung==LSUED)
	while((y-l>0)&&(!sw(x,y-l).da))
	  l++;
      return l+1;
    }        

  /** Initialisiert ohne Ausgabekanäle.
    @param kacheln Das Feld im f&uuml;r Netzkommunikation spezifizierten Format.
    @param flaggen Die Flaggen im f&uuml;r Netzkommunikation spezifizierten Format.
    */
  public SpielfeldSim(int x,int y,String kacheln, Ort[] flaggen) throws FormatException, FlaggenException
    {
      this(x,y,kacheln,flaggen,null);
    }

  public void print()
    {
      // Debuggingmethode. Schmeisst Spielfeldrepraesentation und Lasers auf den Schirm
      super.print();
      p("Die LaserDefinitionen:");
      p("");
      for (Enumeration e = lasers.elements() ; e.hasMoreElements() ;) {
	LaserDef l=(LaserDef)e.nextElement();
	p("str="+l.strength+"; facing="+l.facing+"; x="+l.x+"; y="+l.y+"; length="+l.length);
      }
    }


    /** Führt eine Phase mit nur einem Roboter aus */
    public void doPhase(int phase, Roboter r){
	br[0]=(BoardRoboter)r;
	doPhaseReal(phase, br);
    }

  /** F&uuml;hrt eine Phase aus. Falls mit Ausgabekanalkomm-Objekten
    initialisiert, werden diese von jeder &Auml;nderung benachrichtigt.
    Ergebnisr&uuml;ckgabe erfolgt durch &Auml;nderung der &uuml;bergebenen
    Roboter.
    @param phase Die zu simulierende Phase
    @param robbis Die dabei zu beachtenden Roboter
    */
    public void doPhase(int phase, Roboter[] robbis){
	BoardRoboter[] b=new BoardRoboter[robbis.length];
	for (int i=0;i<robbis.length;i++)
	    b[i]=(BoardRoboter)robbis[i];
	doPhaseReal(phase, b);
    }

  private void doPhaseReal(int phase,BoardRoboter[] robbis)
    {
      for (int i=0;i<robbis.length;i++) // angedachte Richtung (aa) auf ungueltig (-1) setzen
	robbis[i].aa=-1;

      bewegt=new boolean[robbis.length];
      bewegt2false();

      ausgabenMsg("mAuswRobBew",null);
      doRobBew(phase,robbis);        // Roboter bewegen sich entspr. ihrer Karte
      // benachrichtige() nach jeder Bewegung

      ausgabenMsg("mAuswExprFl",null);
      doExprFl(phase,robbis);        // Expressfliessbaender 1
      benachrichtige(robbis);
      
      ausgabenMsg("mAuswFl",null);
      doFl(phase,robbis);            // Exprf 2, Fliessbaender 1 
      benachrichtige(robbis);

      ausgabenMsg("mAuswPusher",null);
      doPushers(phase,robbis);       // Schieber
      benachrichtige(robbis);

      ausgabenMsg("mAuswRot",null);
      doDrehEl(phase,robbis);        // Rotating Gears
      benachrichtige(robbis);

      ausgabenMsg("mAuswCrushers",null);
      doCrushers(phase,robbis);      // Stampfer
      benachrichtige(robbis);

      doLasers(phase,robbis);        // Lasers, board & robbi
      benachrichtige(robbis);
      doArchivUpdate(phase,robbis);  // Archivpointupdate
      benachrichtige(robbis);
      doFlaggenUpdate(phase,robbis);  // letzte besuchte Flaggenupdate 
      benachrichtige(robbis);
      if (phase==5){                 // Ende Phase 5
	doRepairs(phase,robbis);     // Reperaturfelder
	benachrichtige(robbis);
	entvirtualisiere(phase,robbis); // Roboter falls moeglich entvirtualisieren
	benachrichtige(robbis);
      }
    }
  private void doRobBew(int phase,BoardRoboter[] robbis)
    {
      d("doRobBew called.");
      boolean[] bewegt=new boolean[robbis.length];
      int todo=robbis.length;
      for (int i=0;i<robbis.length;i++)
	if (!robbis[i].istAktiviert() || (robbis[i].getSchaden()>=10)) {
	  todo--;
	  bewegt[i]=true;
	}
      while (todo>0){
	int highest=0;
	int highrob=-1;
	for (int i=0;i<robbis.length;i++)  // finde höchste Prio
	  if((!bewegt[i])&&(robbis[i].getZug()[phase-1].getprio()>highest)){
	    highest=robbis[i].getZug()[phase-1].getprio();
	    highrob=i;
	  }
	d("doRobBew: next is "+robbis[highrob].getName()+"; prio="+highest);
	moveRob(robbis,highrob,robbis[highrob].getZug()[phase-1].getaktion());
	bewegt[highrob]=true;
	benachrichtige(robbis);
	
	todo--;
      }
    }
  private void moveRob(BoardRoboter[] robbis,int rob,String aktion)
    {
      d("MoveRob: "+robbis[rob].getName()+"; aktion="+aktion);

      if (aktion.equals("M1")){
	moveRobOne(robbis,rob,robbis[rob].getAusrichtung(),true);
	checkGrubenOpfer(robbis,false);
      }
      else if(aktion.equals("M2")){
	moveRobOne(robbis,rob,robbis[rob].getAusrichtung(),true);
	checkGrubenOpfer(robbis,false);
	moveRobOne(robbis,rob,robbis[rob].getAusrichtung(),true);
	checkGrubenOpfer(robbis,false);
      }
      else if(aktion.equals("M3")){
	moveRobOne(robbis,rob,robbis[rob].getAusrichtung(),true);
	checkGrubenOpfer(robbis,false);
	moveRobOne(robbis,rob,robbis[rob].getAusrichtung(),true);
	checkGrubenOpfer(robbis,false);
	moveRobOne(robbis,rob,robbis[rob].getAusrichtung(),true);
	checkGrubenOpfer(robbis,false);
      }
      else if(aktion.equals("BU")){  // Back Up
	moveRobOne(robbis,rob,(robbis[rob].getAusrichtung()+2)%4,true);
	checkGrubenOpfer(robbis,false);
      }
      else if(aktion.equals("RL")){  // Rotate Left 
	dreheRoboter(robbis[rob], DGGUHRZ);
	bewegt[rob]=true;
      }
      else if(aktion.equals("RR")){  // Rotate Right
	dreheRoboter(robbis[rob],DUHRZ);
	bewegt[rob]=true;
      }
      else if(aktion.equals("UT")){  // U-Turn
	robbis[rob].setAusrichtung((robbis[rob].getAusrichtung()+2)%4);
	bewegt[rob]=true;
      }
      else
	throw new RRdoPhaseException("Nicht erlaubte Karte '"+aktion+"' fuer Roboter "+robbis[rob].getName());
    }
  private boolean moveRobOne(BoardRoboter[] robbis,int rob,int direction, boolean schubsen)
    {
      // Bewegt Roboter Nr. rob in direction wenn nix im Weg ist

      // kaputte Robbis ignorieren
      if (robbis[rob].getSchaden() >= 10){
	d("moveRobOne: Ignoriere, da kaputt");
	return false;
      }

      // first, check for Wall
      d("MoveRobOne called. Nr. "+robbis[rob].getName()+"; dir="+direction+"; schubsen: "+(schubsen?"ja":"nein"));
      switch(direction){
      case NORD:
	if (nw(robbis[rob].getX(),robbis[rob].getY()).da)
	  return(false);
	break;
      case OST:
	if (ow(robbis[rob].getX(),robbis[rob].getY()).da)
	  return(false);
	break;
      case SUED:
	if (sw(robbis[rob].getX(),robbis[rob].getY()).da)
	  return(false);
	break;
      case WEST:
	if (ww(robbis[rob].getX(),robbis[rob].getY()).da)
	  return(false);
	break;
      } //switch
      d("No wall. Entering schubsen-choice");

      if (schubsen){
	// second, do a "virtual" move to be able to collision-check
	int xx=robbis[rob].getX();
	int yy=robbis[rob].getY();
	switch(direction){
	case NORD:
	  yy++;
	  break;
	case OST:
	  xx++;
	  break;
	case SUED:
	  yy--;
	  break;
	case WEST:
	  xx--;
	  break;
	} //switch

	d("schubsen. now collision-checking...");
	//third, check for collision with other robbis
	for (int i=0;i<robbis.length;i++)
	  if ((i!=rob)&&(robbis[i].getX()==xx)&&(robbis[i].getY()==yy)&&(!robbis[i].istVirtuell())&&(!robbis[rob].istVirtuell())) 
	    if (!moveRobOne(robbis,i,direction,true)) return(false);	    
	      
	d("Now moving.");
	//fourth, commit the change if we reach this point
	robbis[rob].setPos(xx,yy);
	//in that case, the robot has actually moved a square
	bewegt[rob]=true;
      } //if schubsen
      else {
	d("no schubsen. doing changes");
	switch(direction) {
	case NORD:
	  robbis[rob].yy= robbis[rob].getY() + 1;
	  robbis[rob].xx= robbis[rob].getX();
	  break;
	case OST:
	  robbis[rob].yy= robbis[rob].getY();
	  robbis[rob].xx= robbis[rob].getX() + 1;
	  break;
	case SUED:
	  robbis[rob].yy= robbis[rob].getY() - 1;
	  robbis[rob].xx= robbis[rob].getX();
	  break;
	case WEST:
	  robbis[rob].yy= robbis[rob].getY();
	  robbis[rob].xx= robbis[rob].getX() - 1;
	  break;
	}
      }
      return(true);
    }

  private void checkGrubenOpfer(BoardRoboter[] robbis, boolean xxyy) 
    { 
      // d("cGrube: "+(xxyy?"xxyy":"richtige Koordinaten"));
      // Schaut ob jemand in eine Grube gefallen ist
      // xxyy ist Flag fuer Benutzung der gedachten Position
      for (int rob=0;rob<robbis.length;rob++) {
	  //d("checkGrubenOpfer: rob="+robbis[rob].getName()+"; x="+robbis[rob].x+"; y="+robbis[rob].y+"; xx="+robbis[rob].xx+"; yy="+robbis[rob].yy+"; boden="+bo(robbis[rob].x,robbis[rob].y)+"boden-xxyy="+bo(robbis[rob].xx,robbis[rob].yy).typ+(xxyy?"virtuelle Koord":"reale Koord"));
	if (!xxyy) {
	    if (bo(robbis[rob].getX(),robbis[rob].getY()).typ==BDGRUBE){
		if (!robbis[rob].istInGrube())
		    ausgabenMsgString("mGrubenopfer",robbis[rob].getName());
		vernichteRoboter(robbis[rob]);
	    }
	} else if (bo(robbis[rob].xx,robbis[rob].yy).typ==BDGRUBE){
	    if (!robbis[rob].istInGrube())
		ausgabenMsgString("mGrubenopfer",robbis[rob].getName());
	    vernichteRoboter(robbis[rob]);
	}
      }
    }

  /**
    Roboter auf zerstört setzen (Schaden=10, virtuell=true)
   */
  private void vernichteRoboter(BoardRoboter thorsten)
    {
      d("vernichteRoboter: "+thorsten.getName());
      thorsten.setSchaden(10);
      thorsten.setVirtuell();
      thorsten.setInvalidPos();
      thorsten.xx=0;
      thorsten.yy=0;
    }

  private void dreheRoboter(BoardRoboter robbi, int drehR)
    { 
      d("dreheRoboter called. "+robbi.getName()+" nach "+drehR);
      // drehR = DrehRichtung
      switch (drehR) {
      case DUHRZ:
	  robbi.setAusrichtung((robbi.getAusrichtung() + 1)%4);
	break;
      case DGGUHRZ:
	robbi.setAusrichtung(robbi.getAusrichtung() -1 );
	if (robbi.getAusrichtung()==-1) 
	    robbi.setAusrichtung(3);
	break;
      } // switch
    }
  private void dreheRoboterGedacht(BoardRoboter robbi, int drehR)
    { 
      d("dreheRoboterGedacht called. robbi="+robbi.getName()+"; drehR="+drehR);
      // veraendert die gedachte Ausrichtung
      // drehR = DrehRichtung
      switch (drehR) {
      case DUHRZ:
	robbi.aa=(robbi.getAusrichtung() + 1)%4;
	break;
      case DGGUHRZ:
	robbi.aa = (robbi.getAusrichtung() - 1);
	if (robbi.aa==-1) 
	  robbi.aa=3;
	break;
      } // switch
    }

  private void doExprFl(int phase,BoardRoboter[] robbis)
    {
      d("doExprFl called");
      // ExpressFliessband
      gedachteWerteInitialisieren(robbis);
      for (int i=0;i<robbis.length;i++)
	if (((bo(robbis[i].getX(),robbis[i].getY()).typ)/100)>1)
	  ausfuehrenFliessband(robbis,i,(bo(robbis[i].getX(),robbis[i].getY()).typ)%100);   
      checkGrubenOpfer(robbis,true);
      gedachtesAusfuehren(robbis);
    }

  private void doFl(int phase,BoardRoboter[] robbis)
    {
      d("doFliessband called");
      // Fliessband (normal)
      gedachteWerteInitialisieren(robbis);
      for (int i=0;i<robbis.length;i++)
	if (((bo(robbis[i].getX(),robbis[i].getY()).typ)/100)>0)
	  ausfuehrenFliessband(robbis,i,(bo(robbis[i].getX(),robbis[i].getY()).typ)%100);
      checkGrubenOpfer(robbis,true);
      gedachtesAusfuehren(robbis);
    }

  /**
    Ausführen der Fließbandbewegungen
   */

  private void ausfuehrenFliessband(BoardRoboter[] robbis,int rob,int typ)
    {
      d("ausfuehrenFliessband called. rob="+rob+"; typ="+typ);
      switch (typ%10) {
      case FNORD:
	if (!moveRobOne(robbis,rob,NORD,false)) return; // Abbruch falls vor die Wand gelaufen
	break;
      case FOST:
	if (!moveRobOne(robbis,rob,OST,false)) return;
	break;
      case FSUED:
	if (!moveRobOne(robbis,rob,SUED,false)) return;
	break;
      case FWEST:
	if (!moveRobOne(robbis,rob,WEST,false)) return;
	break;
      } // switch
      int bodn=bo(robbis[rob].xx,robbis[rob].yy).typ%100;
      //d("bodn="+bodn);
      if (((bodn/10)==2)||((bodn/10)==5)) // gegen den Uhrzeigersinn
	if (((bodn%10+1)%4)==typ%10) 
	  dreheRoboterGedacht(robbis[rob],DGGUHRZ);
      if (((bodn/10)==3)||((bodn/10)==5)) // mit dem Uhrzeigersinn
	if (((typ%10+1)%4)==bodn%10)
	  dreheRoboterGedacht(robbis[rob],DUHRZ);
    } // ausfuehrenFliessband

  private void gedachteWerteInitialisieren(BoardRoboter[] robbis)
    {
      for (int i=0;i<robbis.length;i++){
	robbis[i].xx=robbis[i].getX();
	robbis[i].yy=robbis[i].getY();
	robbis[i].aa=robbis[i].getAusrichtung();
      } // for
    } // gedachteWerteSetzen

  /**
	Ausführen der in dieser Phase aktiven Boardpusher
   */
  private void doPushers(int phase,BoardRoboter[] robbis)
    {
      d("doPushers called.");
      gedachteWerteInitialisieren(robbis);
      for (int i=0;i<robbis.length;i++){
	if (robbis[i].getSchaden()>=10)
	  continue;
	Roboter r=robbis[i];
	if((nw(r.getX(),r.getY()).wandEl[1]==WPUSHER)&&(isCrusherActive(nw(r.getX(),r.getY()).spez[1],phase)))
	  moveRobOne(robbis,i,SUED,false);
	if((sw(r.getX(),r.getY()).wandEl[0]==WPUSHER)&&(isCrusherActive(sw(r.getX(),r.getY()).spez[0],phase)))
	  moveRobOne(robbis,i,NORD,false);
	if((ow(r.getX(),r.getY()).wandEl[0]==WPUSHER)&&(isCrusherActive(ow(r.getX(),r.getY()).spez[0],phase)))
	  moveRobOne(robbis,i,WEST,false);
	if((ww(r.getX(),r.getY()).wandEl[1]==WPUSHER)&&(isCrusherActive(ww(r.getX(),r.getY()).spez[1],phase)))
	  moveRobOne(robbis,i,OST,false);
      } //for
      checkGrubenOpfer(robbis, true);
      gedachtesAusfuehren(robbis);
    } // doPushers

  private void gedachtesAusfuehren(BoardRoboter[] robbis)
    {
      // zuvor angedachte Zuege jetzt ausfuehren, falls sie keine Konflikte ergeben.
      boolean[] robmove= new boolean[robbis.length];
      for (int i=0;i<robmove.length;i++)
	robmove[i]=true;
      for (int rob1=0;rob1<robbis.length;rob1++)
	for (int rob2=rob1+1;rob2<robbis.length;rob2++) 
	  if ((!robbis[rob1].istVirtuell())&&(!robbis[rob2].istVirtuell())){
	    if ((robbis[rob1].xx==robbis[rob2].xx)&&(robbis[rob1].yy==robbis[rob2].yy)) { 
                                // gleiches Zielfeld
	      robmove[rob1]=false;
	      robmove[rob2]=false;
	    }
	    if ((robbis[rob1].getX()==robbis[rob2].xx)&&(robbis[rob1].getY()==robbis[rob2].yy)&&
		(robbis[rob2].getX()==robbis[rob1].xx)&&(robbis[rob2].getY()==robbis[rob1].yy)) {
                                // getauschte Position
	      robmove[rob1]=false;
	      robmove[rob2]=false;
	    }
	  }
      for (int i=0;i<robbis.length;i++) 
	if (robmove[i]) {
	    robbis[i].setPos(robbis[i].xx,robbis[i].yy);
	  if (robbis[i].aa > -1) {
	    robbis[i].setAusrichtung(robbis[i].aa);
	    robbis[i].aa = -1; // angenommene Ausrichtung auf ungueltig setzen
	  }
	  bewegt[i]=true;
	}    
    } // gedachesAusfuehren
  
  /**
    Ausführen der Drehelemente (Drehelemente != Drehfließbaender!!)
   */
  private void doDrehEl(int phase,BoardRoboter[] robbis)
    {
      d("doDrehEl called.");
      
      for (int i=0;i<robbis.length;i++)
	if (bo(robbis[i].getX(),robbis[i].getY()).typ==BDDREHEL){
	  dreheRoboter(robbis[i],bo(robbis[i].getX(),robbis[i].getY()).spez);
	  bewegt[i]=true;
	}
    } // doDrehEl

  /**
    Ausführen der in dieser Phase aktiven Crusher
   */
  private void doCrushers(int phase,BoardRoboter[] robbis)
    {
      d("doCrushers called.");
      
      for (int i=0;i<robbis.length;i++) {
	  if ((bo(robbis[i].getX(),robbis[i].getY()).typ>=100)&&((bo(robbis[i].getX(),robbis[i].getY()).spez)>0)&&(isCrusherActive(bo(robbis[i].getX(),robbis[i].getY()).spez,phase)))
              vernichteRoboter(robbis[i]);
      }
    } // doCrushers

  /**
    Ausführen der Board-Laser
   */
  private void doLasers(int phase,BoardRoboter[] robbis)
    {
      d("doLasers called.");
      
      // die Board-Laser
      for (Enumeration e = lasers.elements() ; e.hasMoreElements() ;) {
	LaserDef l=(LaserDef)e.nextElement();
	int x=l.x;
	int y=l.y;
	boolean hit=false;
	
      aussen: for (int i=l.length;i>0;i--){
	  for (int j=0;j<robbis.length;j++)
	      if ((robbis[j].getX()==x)&&(robbis[j].getY()==y)){ // Treffer!
		  if (server!=null){                  // Nachricht an die Ausgaben
		      String[] tmp=new String[5];
		      tmp[0]=robbis[j].getName();
		      tmp[1]=""+l.strength;
		      tmp[2]=""+l.x;
		      tmp[3]=""+l.y;
		      tmp[4]=""+l.facing;
		      d("Boardlasertreffer auf "+robbis[j].getName());
		      actualStats = stats.getStats (robbis[j].getName());
		      actualStats.incDamageByBoard();
		      
		      
		      ausgabenMsg("mBoardLaser",tmp);
		  }

		  if (robbis[j].istVirtuell()){
		      // Virtuelle Robots kriegen Schaden, blocken aber nicht
		      for (int s=l.strength;s>0;s--){  // Schaden zufügen und Register sperren
			  robbis[j].incSchaden();
			  registerSperren(robbis[j]);
		      }
		      bewegt[j]=true;
		  }
		  else if (!hit){
		      // Maximal ein nichtvirtueller Roboter wird getroffen
		      // Danach wird dieser Laser beendet (hit==true)
		      for (int s=l.strength;s>0;s--){
			  robbis[j].incSchaden();
			  registerSperren(robbis[j]);
		      }
		      bewegt[j]=true;
		      hit=true;
		  }
	      } // Treffer
	  
	// Falls Treffer -> naechster Laser.
	  if (hit)
	      break aussen;
	  
	  // Ansonsten: naechstes Feld
	  switch (l.facing){
	  case LNORD:
	      y++;
	      break;
	  case LOST:
	      x++;
	      break;
	  case LSUED:
	      y--;
	      break;
	  case LWEST:
	      x--;
	      break;
	  } //switch
      } // for length
      } //for Enumeration
      
      //die Roboter-Laser
    aussen2: for (int rob=0;rob<robbis.length;rob++){
      if ((robbis[rob].istVirtuell())||(!robbis[rob].istAktiviert()))
	continue aussen2;
      int x=robbis[rob].getX();
      int y=robbis[rob].getY();
      switch (robbis[rob].getAusrichtung()){
      case OST:
	  if (ow(x,y).da)
	      continue aussen2;
	  x++;  // Start auf Folgefeld
	while((x<=sizeX)&&(!ww(x,y).da)){
	  for (int j=0;j<robbis.length;j++)
	    if ((robbis[j].getX()==x)&&(robbis[j].getY()==y)&&(!robbis[j].istVirtuell())){ // Treffer
		robbis[j].incSchaden();
		registerSperren(robbis[j]);
		bewegt[j]=true; // Änderung erfolgt
		
		ausgabenMsgString2("mRobLaser",robbis[rob].getName(),robbis[j].getName());
		actualStats=stats.getStats(robbis[rob].getName());
		actualStats.incHits();
		if (robbis[j].getSchaden()>=10)
		    actualStats.incKills();
		actualStats=stats.getStats(robbis[j].getName());
		actualStats.incDamageByRobots();
		


		continue aussen2;
	    }
	  x++;
	}
	break;
      case WEST:
	  if (ww(x,y).da)
	      continue aussen2;
	x--;
	while((x>0)&&(!ow(x,y).da)){
	  for (int j=0;j<robbis.length;j++)
	    if ((robbis[j].getX()==x)&&(robbis[j].getY()==y)&&(!robbis[j].istVirtuell())){ //Treffer
	      robbis[j].incSchaden();
	      registerSperren(robbis[j]);
	      bewegt[j]=true;

	      ausgabenMsgString2("mRobLaser",robbis[rob].getName(),robbis[j].getName());
	      
	      continue aussen2;
	    }
	  x--;
	}
	break;
      case NORD:
	  if (nw(x,y).da)
	      continue aussen2;
	y++;
	while((y<=sizeY)&&(!sw(x,y).da)){
	  for (int j=0;j<robbis.length;j++)
	    if ((robbis[j].getX()==x)&&(robbis[j].getY()==y)&&(!robbis[j].istVirtuell())){ //Treffer
		robbis[j].incSchaden();
		registerSperren(robbis[j]);
		bewegt[j]=true;

		ausgabenMsgString2("mRobLaser",robbis[rob].getName(),robbis[j].getName());
	      
		continue aussen2;
	    }
	  y++;
	}
	break;
      case SUED:
	  if (sw(x,y).da)
	      continue aussen2;
	y--;
	while((y>0)&&(!nw(x,y).da)){
	  for (int j=0;j<robbis.length;j++)
	    if ((robbis[j].getX()==x)&&(robbis[j].getY()==y)&&(!robbis[j].istVirtuell())){ //Treffer
		robbis[j].incSchaden();
	      registerSperren(robbis[j]);
	      bewegt[j]=true;

	      ausgabenMsgString2("mRobLaser",robbis[rob].getName(),robbis[j].getName());
	      
	      continue aussen2;
	    }
	  y--;
	}
	break;
      } //switch
    } //for rob
    } // doLasers

  private void doArchivUpdate(int phase,BoardRoboter[] robbis)
    {
      d("doArchivUpdate called.");
      
      for (int i=0;i<robbis.length;i++) {
	
	if ((bo(robbis[i].getX(),robbis[i].getY()).typ)==BDREPA) {
	    robbis[i].setArchiv(robbis[i].getPos());
	    bewegt[i]=true;
	    d(robbis[i].getName()+" ist auf einem Reperaturfeld. Archivpos updated");
	}
	for (int j=0;j<flaggen.length;j++)
	  if ((robbis[i].getX()==flaggen[j].x)&&(robbis[i].getY()==flaggen[j].y)){
	      robbis[i].touchArchiv();
	      bewegt[i]=true;
	      d(robbis[i].getName()+" ist auf einer Flagge (R1). Archivpos updated");
	  }
      }      
    } // doArchivUpdate

  private void doFlaggenUpdate(int phase,BoardRoboter[] robbis)
    { 
      d("doFlaggenUpdate called.");
      
      for (int i=0;i<robbis.length;i++) {
	if (robbis[i].getNaechsteFlagge()==flaggen.length+1)
		continue;
	if ((robbis[i].getX()==flaggen[robbis[i].getNaechsteFlagge()-1].x)&&(robbis[i].getY()==flaggen[robbis[i].getNaechsteFlagge()-1].y)) {
	    robbis[i].incNaechsteFlagge();
	  bewegt[i]=true;
	  d(robbis[i].getName()+" hat naechste Flagge erreicht.");
	  ausgabenMsgString2("mNextFlag",robbis[i].getName(),""+(robbis[i].getNaechsteFlagge()-1));
	}
	
      }
    } // doCheckUpdate

  /**
    Roboter am Ende der 5. Phase reparieren, falls er auf einem
    Reparaturfeld/Flaggenfeld steht und beschädigt ist.
   */
  private void doRepairs(int phase,BoardRoboter[] robbis)
    {
      d("doRepairs called.");
      
      for (int i=0;i<robbis.length;i++) {
	if ((bo(robbis[i].getX(),robbis[i].getY()).typ)==BDREPA) {
	  boolean msg=robbis[i].getSchaden()>0;
	  robbis[i].decrSchaden(bo(robbis[i].getX(),robbis[i].getY()).spez);
	  bewegt[i]=true;
	  d(robbis[i].getName()+" repariert wegen Repa-Feld.");
	  if (msg)
	      ausgabenMsgString2("mRepFeld",robbis[i].getName(),""+bo(robbis[i].getX(),robbis[i].getY()).spez);
	}
	
	for (int j=0;j<flaggen.length;j++)
	  if ((robbis[i].getX()==flaggen[j].x)&&(robbis[i].getY()==flaggen[j].y)) {
	    boolean msg=robbis[i].getSchaden()>0;
	    robbis[i].decrSchaden(1);
	    bewegt[i]=true;
	    d(robbis[i].getName()+" repariert wegen Flagge.");
	    if (msg)
		ausgabenMsgString("mRepFlag",robbis[i].getName());
	  }
	
	if (robbis[i].getSchaden() < 0)
	    robbis[i].setSchaden(0);
      }
    } // doRepairs


  /**
    Falls ein Roboter nicht zerstört ist, und auch kein anderer
    Roboter auf ihm steht, so wird der Roboter wieder normal
    (entvirtualisiert).
   */
  private void entvirtualisiere(int phase, BoardRoboter[] robbis)    // extended edition
    {
      boolean cont;
      for (int a=0;a<robbis.length;a++) {      // Schleife 1
	cont=false;
	if (robbis[a].istVirtuell()) {
	  for (int b=0;b<robbis.length;b++)   // Schleife 2
	    if (robbis[a]!=robbis[b])
	      if ((robbis[a].getX()==robbis[b].getX()) && (robbis[a].getY()==robbis[b].getY()))  // wenn zwei verschiedene Roboter auf gleicher Position
		{
		  cont=true;   // continue aktivieren
		  break;       // dann entvirtualisieren fuer robbis[a] abbrechen
		}
	  if (cont) continue;  // naechsten Roboter bearbeiten
	  if (robbis[a].getSchaden()<10) {
		    robbis[a].setVirtuell(false);     // wenn er nicht zerstoert ist: entvirtualisieren durchführen
		    d("Entvirtualisiere Roboter "+robbis[a].getName());
		    bewegt[a]=true;
		  }
	} // if
      } // for Schleife 1
      if (robbis.length==1)          // Sonderfall einzelner Roboter
	if (robbis[0].getSchaden()<10) 
	  {
	      robbis[0].setVirtuell(false);
	  d("Entvirtualisiere einzelnen Roboter "+robbis[0].getName());
	  bewegt[0]=true;
	  }
    } // entvirtualisiere ende
  

  /**
    Weiteres Register gesperren, falls mehr als 4 Schaden
   */
  private void registerSperren(BoardRoboter robbi)
    {
      d("registerSperren called mit "+robbi.getName());

      if (robbi.getSchaden()>=10) {
	vernichteRoboter(robbi);
	return;
      }
      if (robbi.getSchaden()>=5) {
	for (int i=4;i>=0;i--){
	  if (robbi.getGesperrteRegister()[i]==null) {
	    if (robbi.getZug()[i] == null)
		robbi.sperreRegister(i, null);
	    else{
	        robbi.sperreRegister(i, new Karte(robbi.getZug(i).getprio(),robbi.getZug(i).getaktion()));
		d("Sperre Register "+i);
	    }
	    return;
	  }
	} // for
      } // if
    } // registerSperren ende

    protected void d(String s){
	if (debugmeldungen){
		Global.debug(this,s);
	}
    }

} // spielfeldsim ende



