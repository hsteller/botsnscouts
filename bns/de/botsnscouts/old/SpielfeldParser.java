package de.botsnscouts.old;
import de.botsnscouts.util.*;
import de.botsnscouts.board.*;
import de.botsnscouts.autobot.*;
import de.botsnscouts.start.*;
import de.botsnscouts.comm.*;

public class SpielfeldParser{
    private int KX=3, KY=3, FL=6 ;
    private SpielfeldSim kacheln[][]=new SpielfeldSim[KX][KY];
    private Ort[] flaggen = new Ort[FL];
    private int flaggenN=0;
    private int[][] kachind = new int[KX][KY];

    //setzt übergebene Kachel an Stelle (x,y)
    public void setKachel(int x, int y, SpielfeldSim kachel){
	kacheln[x][y]=kachel;
    }

    //dreht die Kachel um r*90° nach rechts
    public void rotKachel(int x, int y, int r){
	if (kacheln[x][y]==null) return;
	String drKachel;
	for (int i=0;i<r;i++){
	    //rotiere Kachel
	    drKachel= kacheln[x][y].get90GradGedreht();
	    try{
		kacheln[x][y]= new SpielfeldSim(12,12,drKachel,null);
	    }catch(FlaggenException e){
		System.err.println(e);
	    }catch(FormatException e){
		System.err.println(e);
	    }
	}
	//rotiere Flaggen
	int kx,ky,fx,fy;
	for (int j=0;j<flaggenN;j++){
	    kx=(flaggen[j].x-1)/12;//kachel x
	    ky=(flaggen[j].y-1)/12;//kachel y
	    if (kx==x&&ky==y){
		fx=(flaggen[j].x-1)%12;//Flaggenposition
		fy=(flaggen[j].y-1)%12;//im Kachel (-1)
		switch(r){
		case 1:
		    flaggen[j].x=fy+kx*12+1;
		    flaggen[j].y=11-fx+fy*12+1;
		    break;
		case 2:
		    flaggen[j].x=11-fx+kx*12+1;
		    flaggen[j].y=11-fy+ky*12+1;
		    break;
		case 3:
		    flaggen[j].x=11-fy+kx*12+1;
		    flaggen[j].y=fx+ky*12+1;
		}
	    }
	}
    }

    //löscht die Kachel at (x,y)
    public void delKachel(int x, int y){
	//entferne Flaggen falls vorhanden
	for (int i=0;i<flaggenN;i++){
	    if ((flaggen[i].x-1)/12==x&&(flaggen[i].y-1)/12==y){
		delFlagge(i);
	    }
	}
	//enferne Kachel
	kacheln[x][y]=null;
    }

    //prüft ob auf der Kachel at (x,y) Flaggen stehen
    public boolean sindFlaggen(int x, int y){
	for (int i=0;i<flaggenN;i++){
	    if ((flaggen[i].x-1)/12==x&&(flaggen[i].y-1)/12==y){
		return true;
	    }
	}
	return false;
    }

    // prüft ob eine Flagge hinzugefügt werden kann
    public boolean checkFlaggePos(int x,int y){
	int kx=(x-1)/12;
	int ky=(y-1)/12;
	//fals kein Kachel drunter
	if (kacheln[kx][ky]==null)
	    return false;

	//falls schon alle Flaggen da sind
	if (flaggenN==FL)
	    return false;

	//prüfe Kachelelement
	Ort[] flag=new Ort[1];
	flag[0]=new Ort(x,y);

	String kach=kacheln[kx][ky].getComputedString();

	SpielfeldSim test;
	try{
	    test= new SpielfeldSim(12,12,kach,flag);
	}catch(FlaggenException e){
	    return false;
	}catch(FormatException e){
	    System.err.println(e);
	}

	//falls alle tests bestanden
	return true;
    }

    // fügt eine Flagge hinzu 
    public void addFlagge(int x,int y) throws FlaggenException{
	if (!checkFlaggePos(x,y))
	    throw new FlaggenException();
	flaggen[flaggenN++]=new Ort(x,y);
    }

    // löscht eine Flagge hinzu 
    public void delFlagge(int nr){
	if (nr>=flaggenN) return;
	for (int i=nr+1;i<flaggenN;i++){
	    flaggen[i-1]=flaggen[i];
	}
	flaggen[flaggenN--]=null;
    }

    // versetzt eine Flagge 
    public void moveFlagge(int nr, int x,int y) throws FlaggenException{
	if (!checkFlaggePos(x,y))
	    throw new FlaggenException();
	flaggen[nr]=new Ort(x,y);
    }

    //gibt die Flaggen zurück
    public Ort[] getFlaggen(){
	return flaggen;
    }

    //gibt Kacheln als 2-dim Array von Spielfeld zurück
    public SpielfeldSim[][] getKacheln(){
	return kacheln;
    }

    //setzt Spielfeldgröße
    public void setSpielfeldDim(int x, int y){
	KX=x;
	KY=y;
    }

    //gibt Spielfeldgröße zurück
    public Ort getSpielfeldDim(){
	return new Ort(KX,KY);
    }

    //* nicht für Fassade *//

    //gibt Flaggen mit evtl versetzten Koordinaten zurück
    public int[][] getRFlaggen(){
	Ort[] bounds=findBounds();
	int[][] flags=new int[2][flaggenN];
	for (int i=0;i<flaggenN;i++){
	    flags[0][i]=flaggen[i].x-bounds[0].x*12;
	    flags[1][i]=flaggen[i].y-bounds[0].y*12;
	}
	return flags;
    }

    //gibt das Spielfeld als ein String zurück
    public String getSpielfeld() throws OneFlagException, NichtZusSpfException{
	checkSpielfeld();//teste
	Ort[] bounds= findBounds();
	String GRUBENZWR="____________";
	String GRUBENFLD="_G_G_G_G_G_G_G_G_G_G_G_G_";
	StringBuffer out=new StringBuffer();//hier wird das Spielfeld aufgebaut
	String rechts=new String();
	boolean links =false;
	StringBuffer oben = new StringBuffer();
	StringBuffer unten =new StringBuffer();
	for (int i=0;i<KX;i++)
	    for (int j=0;j<KY;kachind[i][j++]=0);
	for(int j=bounds[1].y;j>=bounds[0].y;j--){
	    for (int k=0;k<25;k++){
		for (int i=bounds[0].x;i<=bounds[1].x;i++){
		    if (k==0){
			if (kacheln[i][j]==null)
			    unten.append(GRUBENZWR);
			else unten.append(liesZeile(kacheln[i][j].getComputedString(),i,j));
		    }
		    else if (k==24){
			if (kacheln[i][j]==null)
			    oben.append(GRUBENZWR);
			else oben.append(liesZeile(kacheln[i][j].getComputedString(),i,j));
		    }
		    else{
			if (k%2==0){
			    if (kacheln[i][j]==null)
				out.append(GRUBENZWR);
			    else
				out.append(liesZeile(kacheln[i][j].getComputedString(),i,j));
			}
			else{
			    if (kacheln[i][j]==null)
				rechts=GRUBENFLD;
			    else rechts=new String(liesZeile(kacheln[i][j].getComputedString(),i,j));
			    if (links) mergez(out,rechts);
			    else{
				out.append(rechts);
				links=true;
			    }
			}
		    }
		} //for i
		links=false;
		if (k==0){
		    out.append(merger(oben,unten));
		    oben = new StringBuffer();
		    unten =new StringBuffer();
		} //endif
		out.append("\n");
	    } //for k
	} //for j
	out.append(oben);
	out.append(".\n");
	/*	if(!checkit(out.toString(),(xm+1)*12,(ym+1)*12))
		System.err.println("Ooops!!!!");*/
	return out.toString();
    }

    public static void mergez(StringBuffer l, String r){
	char lc,fc;
	lc=l.charAt(l.length()-1);
	if (lc=='#') fc=lc;
	else fc=r.charAt(0);
	int x=l.length()-1;
	l.setLength(l.length()-1);
	l.append(r);
	l.setCharAt(x,fc);
    }

    public static String merger(StringBuffer o, StringBuffer u){
	if (o.length()==0)
	    return u.toString();
	StringBuffer zwr=new StringBuffer();
	char oc,uc;
	int oi=0, ui=0;
	oc = o.charAt(oi++);
	uc = u.charAt(ui++);
	while (oi<o.length()&&ui<u.length()){
	    while (oc != '_' && oc != '#'&&oi<o.length()){
		zwr.append(oc);
		oc=o.charAt(oi++);
	    }
	    if (oc=='_') zwr.append(uc);
	    else zwr.append(oc);
	    uc = u.charAt(ui++);
	    if (oi<o.length()) oc = o.charAt(oi++);
	    while (uc != '_' && uc != '#'&&ui<u.length()){
		zwr.append(uc);
		uc=u.charAt(ui++);
	    }
	    if (ui==u.length()&&uc!='_'&&uc!='#') zwr.append(uc);
	}
	if (oi<o.length()){
	    while (oc != '_' && oc != '#'&&oi<o.length()){
		zwr.append(oc);
		oc=o.charAt(oi++);
	    }
	}
	if ((oc == '_'||oc=='#')&&(uc =='_'||uc =='#'))
	    if (oc=='_') zwr.append(uc);
	    else zwr.append(oc);
	if (ui<u.length()){
	    uc = u.charAt(ui++);
	    while (uc != '_' && uc != '#'&&ui<u.length()){
		zwr.append(uc);
		uc=u.charAt(ui++);
	    }
	    zwr.append(uc);
	}
	
	return (zwr.toString()).trim();
    }

    //liest eine Zeile aus Kachelstring
    public String liesZeile(String fil,int i, int j){
	StringBuffer str=new StringBuffer();
	try{
	    char x=fil.charAt(kachind[i][j]++);
	    while(x=='\10'||x=='\13'||x=='\32'||x=='\t'||x=='\n'||x==' ')
		x=fil.charAt(kachind[i][j]++);
	    while (x!='\10'&&x!='\13'&&x!='\32'&&x!='\t'&&x!='\n'&&x!=' '){
		str.append(x);
		x=fil.charAt(kachind[i][j]++);
	    }
	}catch(Exception ex){
	    System.err.println(ex);
	}
	return (str.toString()).trim();
    }
    
    //findet Grenzen des Spielfeldes
    Ort[] findBounds(){
	Ort[] bounds=new Ort[2];
	bounds[0]=new Ort(0,0);
	bounds[1]=new Ort(0,0);
	for (int i=0;i<KX;i++){
	    for (int j=0;j<KY;j++)
		if (kacheln[i][j]!=null)
		    bounds[0].x=i;
	}
	for (int j=0;j<KY;j++){
	    for (int i=0;i<KX;i++)
		if (kacheln[i][j]!=null)
		    bounds[0].y=j;
	}
	for (int i=KX-1;i>=0;i--){
	    for (int j=KY-1;j>=0;j--)
		if (kacheln[i][j]!=null)
		    bounds[1].x=i;
	}
	for (int j=KY-1;j>=0;j--){
	    for (int i=KX-1;i>=0;i--)
		if (kacheln[i][j]!=null)
		    bounds[1].y=j;
	}	    
	return bounds;
    }

    //gibt Größe des Spielfeldes zurück
    public Ort getSpielfeldSize(){
	Ort[] bounds=findBounds();
	return new Ort((bounds[1].x-bounds[0].x+1)*12,(bounds[1].y-bounds[0].y+1)*12);
    }
    
    //prüft ob Spielfeld gültig ist (plausibilitätstests)
    boolean checkSpielfeld() throws OneFlagException, NichtZusSpfException{
	//zu wenig Flaggen
	if (flaggenN<2)
	    throw new OneFlagException();
	//das Spielfeld ist nicht zusammenhängend
	Ort fkachel=findFirstKachel();
	boolean[][] mark=new boolean[KX][KY];
	for (int i=0;i<KX;i++)
	    for (int j=0;j<KY;mark[i][j]=false,j++);
	mark[fkachel.x][fkachel.y]=true;
	markNachbarn(fkachel,mark);
	for (int i=0;i<KX;i++){
	    for (int j=0;j<KY;j++){
		if (kacheln[i][j]!=null&&!mark[i][j]){
		    throw new NichtZusSpfException();
		}
	    }
	}
	return true;
    }

    //findet die erste belegte Kachel
    Ort findFirstKachel(){
	for (int i=0;i<KX;i++){
	    for (int j=0;j<KY;j++){
		if (kacheln[i][j]!=null)
		    return new Ort(i,j);
	    }
	}
	return null;
    }

    //markiert alle (mit der ersten) zusammenhängende Kacheln
    void markNachbarn(Ort kach, boolean[][] mark){
	//shau nach links
	if (kach.x-1>=0&&kacheln[kach.x-1][kach.y]!=null&&!mark[kach.x-1][kach.y]){
	    mark[kach.x-1][kach.y]=true;
	    markNachbarn(new Ort(kach.x-1,kach.y),mark);
	}
	//rechts
	if (kach.x+1<KX&&kacheln[kach.x+1][kach.y]!=null&&!mark[kach.x+1][kach.y]){
	    mark[kach.x+1][kach.y]=true;
	    markNachbarn(new Ort(kach.x+1,kach.y),mark);
	}
	//unten
	if (kach.y-1>=0&&kacheln[kach.x][kach.y-1]!=null&&!mark[kach.x][kach.y-1]){
	    mark[kach.x][kach.y-1]=true;
	    markNachbarn(new Ort(kach.x,kach.y-1),mark);
	}
	//oben
	if (kach.y+1<KY&&kacheln[kach.x][kach.y+1]!=null&&!mark[kach.x][kach.y+1]){
	    mark[kach.x][kach.y+1]=true;
	    markNachbarn(new Ort(kach.x,kach.y+1),mark);
	}
    }

}

