/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2001 scouties.                                    *
 * Contact botsnscouts-devel@sf.net                                *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 2 of the License.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program, in a file called COPYING in the top
 directory of the Bots 'n' Scouts distribution; if not, write to 
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
 Boston, MA  02111-1307  USA
 
 *******************************************************************/
 
package de.botsnscouts.start;

import de.botsnscouts.util.*;
import de.botsnscouts.board.*;

public class TileRaster{
    private int KX=3, KY=3, FL=6 ;
    private Tile tiles[][]=new Tile[KX][KY];
    private Location[] flaggen = new Location[FL];
    private int flaggenN=0;
    private int[][] tileind = new int[KX][KY];
    private TileFactory tileFactory;

    public TileRaster(TileFactory tileF){
	tileFactory=tileF;
    }

    //setzt übergebene Tile an Stelle (x,y)
    public void setTile(int x, int y, int rot, String name) throws FlagPresentException{
	if (sindFlaggen(x,y))
	    throw new FlagPresentException();
	tiles[x][y]=tileFactory.getTile(name,rot);
    }

    //dreht die Tile um 90° nach links
    public void rotTile(int x, int y){
	if (tiles[x][y]==null) return;
	//hole rotierte Tile
	tiles[x][y]=tileFactory.getTile(tiles[x][y].getName(), (tiles[x][y].getRotation()+1)%4);
	//rotiere Flaggen, falls vorhanden
	int kx,ky,fx,fy;
	for (int j=0;j<flaggenN;j++){
	    kx=(flaggen[j].x-1)/12;//tile x
	    ky=(flaggen[j].y-1)/12;//tile y
	    if (kx==x&&ky==y){//die Flagge ist auf der zu drehenden Tile
		fx=(flaggen[j].x-1)%12;//Flaggenposition
		fy=(flaggen[j].y-1)%12;//im Tile (-1)
		//y->11-y + kachelX*12 +1 (x)
		int nx=11-fy+kx*12+1;
		//x->11-x + kachelY*12 +1 (y)
		int ny=fx+ky*12+1;
		flaggen[j].x=nx;
		flaggen[j].y=ny;
	    }
	}
    }
    
    //löscht die Tile at (x,y)
    public void delTile(int x, int y){
	//entferne Flaggen falls vorhanden
	for (int i=flaggenN-1;i>=0;i--){
	    if ((flaggen[i].x-1)/12==x&&(flaggen[i].y-1)/12==y){
		delFlagge(i);
	    }
	}
	//enferne Tile
	tiles[x][y]=null;
    }

    //prüft ob auf der Tile at (x,y) Flaggen stehen
    public boolean sindFlaggen(int x, int y){
	for (int i=0;i<flaggenN;i++){
	    if ((flaggen[i].x-1)/12==x&&(flaggen[i].y-1)/12==y){
		return true;
	    }
	}
	return false;
    }

    // gibt ein String zurück falls die FlaggenPosition ungünstig ist
    // "" sonst
    public String getFlaggeKomment(int x,int y){
	int kx=(x-1)/12;
	int ky=(y-1)/12;
	Location[] flag= new Location[1];
	flag[0]=new Location((x-1)%12+1,(y-1)%12+1);
	String komment="";
	try{
	    Board tmpSpf=new Board(12,12,tiles[kx][ky].getComputedString(),flag);
	    komment=tmpSpf.getFlaggenProbleme();
	}catch(FlagException e){
	    System.err.println(e);
	}catch (FormatException e){
	    System.err.println(e);
	}
	//Global.debug(this,komment);
	return komment;
    }

    // prüft ob eine Flagge hinzugefügt werden kann
    public boolean checkFlaggePos(int x,int y){
	//falls schon alle Flaggen da sind
	if (flaggenN==FL){
	    return false;
	}
	return checkFlaggeMovePos(x,y);
    }

    // prüft ob eine Flagge hinzugefügt werden kann
    public boolean checkFlaggeMovePos(int x,int y){
	int kx=(x-1)/12;
	int ky=(y-1)/12;
	//fals keine Tile drunter
	if (tiles[kx][ky]==null){
	    return false;
	}

	//prüfe ob an der Stelle schon eine Flagge Steht
	if (istFlagge(x,y)){
	    return false;
	}
	//prüfe Tileelement
	Location[] flag=new Location[1];
	flag[0]=new Location((x-1)%12+1,(y-1)%12+1);

	boolean testFl=tiles[kx][ky].areFlagPositionsValid(flag);
	return testFl;
    }

    // fügt eine Flagge hinzu 
    public void addFlagge(int x,int y) throws FlagException{
	if (!checkFlaggePos(x,y))
	    throw new FlagException();
	flaggen[flaggenN++]=new Location(x,y);
    }

    // löscht eine Flagge
    public void delFlagge(int nr){
	if (nr>=flaggenN) return;
	for (int i=nr+1;i<flaggenN;i++){
	    flaggen[i-1]=flaggen[i];
	}
	flaggen[--flaggenN]=null;
    }

    // löscht eine Flagge an der Position
    public void delFlagge(int ax,int ay){
	for (int i=0;i<flaggenN;i++){
	    if (flaggen[i].x==ax&&flaggen[i].y==ay){
		delFlagge(i);
		return;
	    }
	}
    }

    // prüft ob eine Flagge an der Position vorhanden ist
    public boolean istFlagge(int ax,int ay){
	for (int i=0;i<flaggenN;i++){
	    if (flaggen[i].x==ax&&flaggen[i].y==ay){
		return true;
	    }
	}
	return false;
    }

    // versetzt eine Flagge 
    public void moveFlagge(int nr, int x,int y) throws FlagException{
	if (!checkFlaggeMovePos(x,y))
	    throw new FlagException();
	flaggen[nr]=new Location(x,y);
    }

    // versetzt eine Flagge an der Position
    public void moveFlagge(int ax,int ay, int x,int y) throws FlagException{
	for (int i=0;i<flaggenN;i++){
	    if (flaggen[i].x==ax&&flaggen[i].y==ay){
		moveFlagge(i,x,y);
		return;
	    }
	}
    }

    //gibt die Flaggen zurück
    public Location[] getFlaggen(){
	return flaggen;
    }

    //gibt maximale Anzahl der Flaggen zurück
    public int getMaxFlag(){
	return FL;
    }

    //gibt Tiles als 2-dim Array von Board zurück
    public Tile[][] getTiles(){
	return tiles;
    }

    //gibt eine Tile an der gegebenen Position
    public Tile getTileAt(int x, int y){
	return tiles[x][y];
    }

    //setzt Spielfeldgröße
    public void setSpielfeldDim(int x, int y){
	KX=x;
	KY=y;
    }

    //gibt Spielfeldgröße zurück
    public Location getSpielfeldDim(){
	return new Location(KX,KY);
    }

    //gibt den "Clone" zurück
    public TileRaster getClone(){
	TileRaster tmpRaster = new TileRaster(tileFactory);
	tmpRaster.flaggenN=flaggenN;
	for (int i=0;i<flaggenN;i++){
	    tmpRaster.flaggen[i]=flaggen[i];
	}
	for (int i=0;i<KX;i++){
	    for (int j=0;j<KY;j++){
		tmpRaster.tiles[i][j]=tiles[i][j];
		tmpRaster.tileind[i][j]=tileind[i][j];
	    }
	}

	return tmpRaster;
    }

    //* nicht für Fassade *//

    //gibt Flaggen mit evtl versetzten Koordinaten zurück
    public Location[] getRFlaggen(){
	Location[] bounds=findBounds();
	Location[] flags=new Location[flaggenN];
	for (int i=0;i<flaggenN;i++){
	    flags[i] = new Location(flaggen[i].x-bounds[0].x*12,
	                            flaggen[i].y-bounds[0].y*12);
	}
	return flags;
    }

    /**
     * Hand out string representation of a board.
     */
    public String getBoard() throws OneFlagException, NonContiguousMapException{
	checkSpielfeld();//teste
	Location[] bounds= findBounds();
	String GRUBENZWR="____________";
	String GRUBENFLD="_G_G_G_G_G_G_G_G_G_G_G_G_";
	StringBuffer out=new StringBuffer();
	String rechts=new String();
	boolean links =false;
	StringBuffer oben = new StringBuffer();
	StringBuffer unten =new StringBuffer();
	for (int i=0;i<KX;i++)
	    for (int j=0;j<KY;tileind[i][j++]=0);
	for(int j=bounds[1].y;j>=bounds[0].y;j--){
	    for (int k=0;k<25;k++){
		for (int i=bounds[0].x;i<=bounds[1].x;i++){
		    if (k==0){
			if (tiles[i][j]==null)
			    unten.append(GRUBENZWR);
			else unten.append(liesZeile(tiles[i][j].getComputedString(),i,j));
		    }
		    else if (k==24){
			if (tiles[i][j]==null)
			    oben.append(GRUBENZWR);
			else oben.append(liesZeile(tiles[i][j].getComputedString(),i,j));
		    }
		    else{
			if (k%2==0){
			    if (tiles[i][j]==null)
				out.append(GRUBENZWR);
			    else
				out.append(liesZeile(tiles[i][j].getComputedString(),i,j));
			}
			else{
			    if (tiles[i][j]==null)
				rechts=GRUBENFLD;
			    else rechts=new String(liesZeile(tiles[i][j].getComputedString(),i,j));
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

    //liest eine Zeile aus Tilestring
    public String liesZeile(String fil,int i, int j){
	StringBuffer str=new StringBuffer();
	try{
	    char x=fil.charAt(tileind[i][j]++);
	    while(x=='\10'||x=='\13'||x=='\32'||x=='\t'||x=='\n'||x==' ')
		x=fil.charAt(tileind[i][j]++);
	    while (x!='\10'&&x!='\13'&&x!='\32'&&x!='\t'&&x!='\n'&&x!=' '){
		str.append(x);
		x=fil.charAt(tileind[i][j]++);
	    }
	}catch(Exception ex){
	    System.err.println(ex);
	}
	return (str.toString()).trim();
    }
    
    //findet Grenzen des Spielfeldes
    Location[] findBounds(){
	Location[] bounds=new Location[2];
	bounds[0]=new Location(0,0);
	bounds[1]=new Location(0,0);
	x0: for (int i=0;i<KX;i++){
	    for (int j=0;j<KY;j++){
		if (tiles[i][j]!=null){
		    bounds[0].x=i;
		    break x0;
		}
	    }
	}
	y0: for (int j=0;j<KY;j++){
	    for (int i=0;i<KX;i++){
		if (tiles[i][j]!=null){
		    bounds[0].y=j;
		    break y0;
		}
	    }
	}
	x1: for (int i=KX-1;i>=0;i--){
	    for (int j=KY-1;j>=0;j--){
		if (tiles[i][j]!=null){
		    bounds[1].x=i;
		    break x1;
		}
	    }
	}
	y1: for (int j=KY-1;j>=0;j--){
	    for (int i=KX-1;i>=0;i--){
		if (tiles[i][j]!=null){
		    bounds[1].y=j;
		    break y1;
		}
	    }
	}	    
	return bounds;
    }

    //gibt Größe des Spielfeldes zurück
    public Location getSpielfeldSize(){
	Location[] bounds=findBounds();
	return new Location((bounds[1].x-bounds[0].x+1)*12,(bounds[1].y-bounds[0].y+1)*12);
    }
    
    //prüft ob Board gültig ist (plausibilitätstests)
    public boolean checkSpielfeld() throws OneFlagException, NonContiguousMapException{
	//zu wenig Flaggen
	if (flaggenN<2)
	    throw new OneFlagException();
        //das Board ist nicht zusammenhängend
	Location ftile=findFirstTile();
	boolean[][] mark=new boolean[KX][KY];
	for (int i=0;i<KX;i++)
	    for (int j=0;j<KY;mark[i][j]=false,j++);
	mark[ftile.x][ftile.y]=true;
	markNachbarn(ftile,mark);
	for (int i=0;i<KX;i++){
	    for (int j=0;j<KY;j++){
		if (tiles[i][j]!=null&&!mark[i][j]){
		    throw new NonContiguousMapException();
		}
	    }
	}
	return true;
    }

    //findet die erste belegte Tile
    Location findFirstTile(){
	for (int i=0;i<KX;i++){
	    for (int j=0;j<KY;j++){
		if (tiles[i][j]!=null)
		    return new Location(i,j);
	    }
	}
	return null;
    }

    //markiert alle (mit der ersten) zusammenhängende Tiles
    void markNachbarn(Location tile, boolean[][] mark){
	//shau nach links
	if (tile.x-1>=0&&tiles[tile.x-1][tile.y]!=null&&!mark[tile.x-1][tile.y]){
	    mark[tile.x-1][tile.y]=true;
	    markNachbarn(new Location(tile.x-1,tile.y),mark);
	}
	//rechts
	if (tile.x+1<KX&&tiles[tile.x+1][tile.y]!=null&&!mark[tile.x+1][tile.y]){
	    mark[tile.x+1][tile.y]=true;
	    markNachbarn(new Location(tile.x+1,tile.y),mark);
	}
	//unten
	if (tile.y-1>=0&&tiles[tile.x][tile.y-1]!=null&&!mark[tile.x][tile.y-1]){
	    mark[tile.x][tile.y-1]=true;
	    markNachbarn(new Location(tile.x,tile.y-1),mark);
	}
	//oben
	if (tile.y+1<KY&&tiles[tile.x][tile.y+1]!=null&&!mark[tile.x][tile.y+1]){
	    mark[tile.x][tile.y+1]=true;
	    markNachbarn(new Location(tile.x,tile.y+1),mark);
	}
    }

}

