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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;

import de.botsnscouts.util.Location;

public class TileComponent extends JComponent implements  MouseListener, MouseMotionListener{

    Image tileImage;
    int drehung;
    Facade fassade;
    int myX,myY;
    Paint paint;
    int W=150, H=150;
    int[] flagNum=new int[0];
    Location[] flagPos=new Location[0];

    private boolean flagsChanged, tileChanged;

    TileClickListener tileClickListener;

    public TileComponent(Facade fas, int x, int y){
	setOpaque(false);
	fassade=fas;
	W=H=fas.getThumbnailSize();
	myX=x;
	myY=y;
	paint=new GradientPaint(0,0, Color.lightGray, W, H, Color.darkGray);

	addMouseListener(this);
	addMouseMotionListener(this);
    }

    public Dimension getMinimumSize(){
	return new Dimension(W,H);
    }
    public Dimension getPreferredSize(){
	return new Dimension(W,H);
    }

    //aktualisiert den Image und Flaggen
    public void rasterChanged(){
	//hole den neuen Image
	//Global.debug(this, "rasterChanged "+myX+","+myY);
	Tile tmpTile=fassade.getTileAt(myX, myY);
	if (tmpTile!=null){
	    Image newTileImage=fassade.getImage(tmpTile.getName());
	    int newDrehung=tmpTile.getRotation();
	    tileChanged=(tileImage!=newTileImage||drehung!=newDrehung);
	    tileImage=newTileImage;
	    drehung=newDrehung;
	}else{
	    tileChanged=(tileImage!=null);
	    tileImage=null;
	}
	//hole die Flaggen
	Location[] flag = fassade.getFlagPositions();
	int counter=0;
	flagsChanged=false;
	for (int i=0;i<flag.length;i++){
	    if (flag[i]!=null&&(flag[i].x-1)/12==myX&&(flag[i].y-1)/12==myY){
		flagsChanged=(flagsChanged||flagNum.length<=counter||flagNum[counter]!=i+1||flagPos[counter].x!=(flag[i].x-1)%12||flagPos[counter].y!=(flag[i].y-1)%12);
		counter++;
	    }
	}
	flagsChanged=(flagsChanged||flagNum.length!=counter);
	if (flagsChanged){
	    flagNum=new int[counter];
	    flagPos=new Location[counter];
	    if (counter!=0){
		int j=0;
		for (int i=0;i<flag.length;i++){
		    if (flag[i]!=null&&(flag[i].x-1)/12==myX&&(flag[i].y-1)/12==myY){
			flagNum[j]=i+1;
			flagPos[j]=new Location((flag[i].x-1)%12,(flag[i].y-1)%12);
			j++;
		    }
		}
	    }
	}
	if (flagsChanged||tileChanged){
	    repaint();
	}
    }

    Font flagFont = new Font("Serif", Font.BOLD, 12);
    AlphaComposite alphaTrans = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
    AlphaComposite alphaOpaque = AlphaComposite.getInstance(AlphaComposite.SRC)
; 
    public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	g2d.setPaint( paint );

	if (tileImage==null){//falls keine Kachel an dieser Stelle
	    g2d.setComposite( alphaTrans );
	    g2d.fillRect(0,0, W, H);
	    g2d.setComposite( alphaOpaque );
	}else {//sonst male Kachelimage
	    AffineTransform oldTransform=g2d.getTransform();
	    g2d.drawImage(tileImage,AffineTransform.getRotateInstance(Math.toRadians(-90*drehung),tileImage.getWidth(null)/2,tileImage.getHeight(null)/2),null);
	    g2d.setTransform(oldTransform);
	    
	    //male Flaggen
	    g2d.setColor(Color.white);
	    g2d.setFont(flagFont);
	    for (int i=0;i<flagNum.length;i++){
		//flagge-1 -> awt:
		//x->x*W/12 (+2)
		//y->H-y*H/12 (-2)
		g2d.drawString(""+flagNum[i],(int)((double)flagPos[i].x*(double)W/(double)12+2),H-(int)((double)(flagPos[i].y)*(double)H/(double)12)-2);
	    }
	}
    }

    public void addTileClickListener(TileClickListener tileClickL){
	tileClickListener=tileClickL;
    }

    public void removeTileClickListener(){
	tileClickListener=null;
    }

    //MouseListener Methoden
    //Invoked when the mouse has been clicked on a component.
    public void mouseClicked(MouseEvent e){
	if (tileClickListener!=null){
	    //awt->flagge
	    //x->x*12/W +1
	    int xx=(e.getX()==W?12:(int)((double)e.getX()*12.0/(double)W)+1);
	    //y->(H-y)*12/H +1
	    int yy = (e.getY()==0?12:(int)((double)(H-e.getY())*12.0/(double)H)+1);
	    tileClickListener.tileClick(myX,myY,xx,yy);
	}
    }
    //Invoked when a mouse button has been pressed on a component.
    public void mousePressed(MouseEvent e){}
    //Invoked when a mouse button has been released on a component.
    public void mouseReleased(MouseEvent e){}
    //Invoked when the mouse enters a component.
    public void mouseEntered(MouseEvent e){}
    //Invoked when the mouse exits a component.
    public void mouseExited(MouseEvent e){
	if (tileClickListener!=null){
	    tileClickListener.tileMouseLeave();
	}
    }

    //MouseMotionListener Methoden
    public void mouseMoved(MouseEvent e){
	if (tileClickListener!=null){
	    //awt->flagge
	    //x->x*12/W +1
	    int xx=(e.getX()==W?12:(int)((double)e.getX()*12.0/(double)W)+1);
	    //y->(H-y)*12/H +1
	    int yy = (e.getY()==0?12:(int)((double)(H-e.getY())*12.0/(double)H)+1);
	    tileClickListener.tileMouseMove(myX,myY,xx,yy);
	}
    }
    public void mouseDragged(MouseEvent e){    }

}
