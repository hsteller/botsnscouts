package de.spline.rr;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import java.io.*;

public class StartKachelComp extends JComponent implements  MouseListener, MouseMotionListener{

    Image kachImage;
    int drehung;
    StSpFassade fassade;
    int myX,myY;
    Paint paint;
    int W=150, H=150;
    int[] flagNum=new int[0];
    Ort[] flagPos=new Ort[0]; 

    private boolean flaggenChanged, kachelChanged;

    KachelClickListener kachelClickListener;

    public StartKachelComp(StSpFassade fas, int x, int y){
	setOpaque(false);
	fassade=fas;
	W=H=fassade.getThumbGR();
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
	Kachel tmpKachel=fassade.getKachelAt(myX, myY);
	if (tmpKachel!=null){
	    Image newKachelImage=tmpKachel.getImage();
	    int newDrehung=tmpKachel.getDrehung();
	    kachelChanged=(kachImage!=newKachelImage||drehung!=newDrehung);
	    kachImage=newKachelImage;
	    drehung=newDrehung;
	}else{
	    kachelChanged=(kachImage!=null);
	    kachImage=null;
	}
	//hole die Flaggen
	Ort[] flag = fassade.getFlaggen();
	int counter=0;
	flaggenChanged=false;
	for (int i=0;i<flag.length;i++){
	    if (flag[i]!=null&&(flag[i].x-1)/12==myX&&(flag[i].y-1)/12==myY){
		flaggenChanged=flagNum.length<=counter;
		flaggenChanged=(flaggenChanged||flagNum[counter]!=i+1||flagPos[counter].x!=(flag[i].x-1)%12||flagPos[counter].y!=(flag[i].y-1)%12);
		counter++;
	    }
	}
	flaggenChanged=(flaggenChanged||flagNum.length!=counter);
	if (flaggenChanged){
	    flagNum=new int[counter];
	    flagPos=new Ort[counter];
	    if (counter!=0){
		int j=0;
		for (int i=0;i<flag.length;i++){
		    if (flag[i]!=null&&(flag[i].x-1)/12==myX&&(flag[i].y-1)/12==myY){
			flagNum[j]=i+1;
			flagPos[j]=new Ort((flag[i].x-1)%12,(flag[i].y-1)%12);
			j++;
		    }
		}
	    }
	}
	if (flaggenChanged||kachelChanged){
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

	if (kachImage==null){//falls keine Kachel an dieser Stelle
	    g2d.setComposite( alphaTrans );
	    g2d.fillRect(0,0, W, H);
	    g2d.setComposite( alphaOpaque );
	}else {//sonst male Kachelimage
	    AffineTransform oldTransform=g2d.getTransform();
	    g2d.drawImage(kachImage,AffineTransform.getRotateInstance(Math.toRadians(-90*drehung),kachImage.getWidth(null)/2,kachImage.getHeight(null)/2),null);
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

    public void addKachelClickListener(KachelClickListener kachelClickL){
	kachelClickListener=kachelClickL;
    }

    public void removeKachelClickListener(){
	kachelClickListener=null;
    }

    //MouseListener Methoden
    //Invoked when the mouse has been clicked on a component.
    public void mouseClicked(MouseEvent e){
	if (kachelClickListener!=null){
	    //awt->flagge
	    //x->x*12/W +1
	    int xx=(e.getX()==W?12:(int)((double)e.getX()*12.0/(double)W)+1);
	    //y->(H-y)*12/H +1
	    int yy = (e.getY()==0?12:(int)((double)(H-e.getY())*12.0/(double)H)+1);
	    kachelClickListener.kachelClick(myX,myY,xx,yy);
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
	if (kachelClickListener!=null){
	    kachelClickListener.kachelMouseLeave();
	}
    }

    //MouseMotionListener Methoden
    public void mouseMoved(MouseEvent e){
	if (kachelClickListener!=null){
	    //awt->flagge
	    //x->x*12/W +1
	    int xx=(e.getX()==W?12:(int)((double)e.getX()*12.0/(double)W)+1);
	    //y->(H-y)*12/H +1
	    int yy = (e.getY()==0?12:(int)((double)(H-e.getY())*12.0/(double)H)+1);
	    kachelClickListener.kachelMouseMove(myX,myY,xx,yy);
	}
    }
    public void mouseDragged(MouseEvent e){    }

}
