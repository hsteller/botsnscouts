package de.botsnscouts.editor;

import java.awt.*;
import java.awt.event.*;

import de.botsnscouts.board.SpielfeldSim;
import de.botsnscouts.gui.SACanvas;
import de.botsnscouts.util.Message;
import org.apache.log4j.Category;


class BoardPanel extends SACanvas implements MouseListener{
    static final Category CAT = Category.getInstance( BoardPanel.class );

    protected BoardEditor par=null;

    public BoardPanel(SpielfeldSim ss,BoardEditor p){
	super(ss);
	par=p;
	addMouseListener(this);
    }

    public void paintComponent(Graphics g) {
        paintUnbuffered( g );
    }

    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseClicked(MouseEvent e){
	// Enno: nur linke maustaste bearbeiten
	int mods = e.getModifiers();
 	if( (mods & MouseEvent.BUTTON1_MASK) == 0 )
 	    return;
	par.spfX=e.getX()/64;
	par.spfY=e.getY()/64;
	int vWInd=(e.getX()+32)/64;
	int hWInd=(e.getY()+32)/64;
	CAT.debug("(x,y):typ=="+par.spfX+","+par.spfY+":"+par.indx);

	if(par.indx==54){//pusher nach west
	    new PhaseDialog(par,Message.say("BoardEditor","mPusher"),true);
	    par.spf.getVWand(par.spfX+1, 11-par.spfY).da=true;
	    par.spf.getVWand(par.spfX+1, 11-par.spfY).wandEl0(2);
	    par.spf.getVWand(par.spfX+1, 11-par.spfY).spez0(par.phasen);
	}
	else if(par.indx==55){//pusher nach sued
	    new PhaseDialog(par,Message.say("BoardEditor","mPusher"),true);
	    par.spf.getHWand(par.spfX, 12-par.spfY).wandEl1(2);
	    par.spf.getHWand(par.spfX, 12-par.spfY).spez1(par.phasen);
	    par.spf.getHWand(par.spfX, 12-par.spfY).da=true;
	}
	else if(par.indx==56){//pusher nach nord
	    new PhaseDialog(par,Message.say("BoardEditor","mPusher"),true);
	    par.spf.getHWand(par.spfX, 11-par.spfY).da=true;
	    par.spf.getHWand(par.spfX, 11-par.spfY).wandEl0(2);
	    par.spf.getHWand(par.spfX, 11-par.spfY).spez0(par.phasen);
	}
	else if(par.indx==57){//pusher nach ost
	    new PhaseDialog(par,Message.say("BoardEditor","mPusher"),true);
	    par.spf.getVWand(par.spfX, 11-par.spfY).da=true;
	    par.spf.getVWand(par.spfX, 11-par.spfY).wandEl1(2);
	    par.spf.getVWand(par.spfX, 11-par.spfY).spez1(par.phasen);
	}
	else if(par.indx==58){//crusher
	    int tp=par.spf.getBoden(par.spfX+1,12-par.spfY).typ;
	    if((tp>=100)&&(tp<=253)){
		new PhaseDialog(par,Message.say("BoardEditor","mCrusher"),true);
		par.spf.getBoden(par.spfX+1,12-par.spfY).spez=par.phasen;
	    }
	}
	else if(par.indx==59){//leer
	    par.spf.getBoden(par.spfX+1,12-par.spfY).spez=0;
	    par.spf.getBoden(par.spfX+1,12-par.spfY).typ=0;
	    par.spf.getVWand(par.spfX+1, 11-par.spfY).da=false;
	    par.spf.getVWand(par.spfX+1, 11-par.spfY).wandEl0(0);
	    par.spf.getVWand(par.spfX+1, 11-par.spfY).spez0(0);
	    par.spf.getVWand(par.spfX, 11-par.spfY).da=false;
	    par.spf.getVWand(par.spfX, 11-par.spfY).wandEl1(0);
	    par.spf.getVWand(par.spfX, 11-par.spfY).spez1(0);
	    par.spf.getHWand(par.spfX, 12-par.spfY).wandEl1(0);
	    par.spf.getHWand(par.spfX, 12-par.spfY).spez1(0);
	    par.spf.getHWand(par.spfX, 12-par.spfY).da=false;
	    par.spf.getHWand(par.spfX, 11-par.spfY).da=false;
	    par.spf.getHWand(par.spfX, 11-par.spfY).wandEl0(0);
	    par.spf.getHWand(par.spfX, 11-par.spfY).spez0(0);
	}
	else if(par.indx==60){//vert wand
	    par.spf.getVWand(vWInd, 11-par.spfY).da=true;
	}
	else if(par.indx==61){//horiz. wand
	    par.spf.getHWand(par.spfX, 12-hWInd).da=true;
	}
	else if(par.indx==62){//laser nach west
	    new StrengthDialog(par,Message.say("BoardEditor","mLaser"),true);
	    par.spf.getVWand(par.spfX+1, 11-par.spfY).da=true;
	    par.spf.getVWand(par.spfX+1, 11-par.spfY).wandEl0(1);
	    par.spf.getVWand(par.spfX+1, 11-par.spfY).spez0(par.laserSt);
	}
	else if(par.indx==63){//laser nach sued
	    new StrengthDialog(par,Message.say("BoardEditor","mLaser"),true);
	    par.spf.getHWand(par.spfX, 12-par.spfY).wandEl1(1);
	    par.spf.getHWand(par.spfX, 12-par.spfY).spez1(par.laserSt);
	    par.spf.getHWand(par.spfX, 12-par.spfY).da=true;
	}
	else if(par.indx==64){//laser nach ost
	    new StrengthDialog(par,Message.say("BoardEditor","mLaser"),true);
	    par.spf.getVWand(par.spfX, 11-par.spfY).da=true;
	    par.spf.getVWand(par.spfX, 11-par.spfY).wandEl1(1);
	    par.spf.getVWand(par.spfX, 11-par.spfY).spez1(par.laserSt);
	}
	else if(par.indx==65){//laser nach nord
	    new StrengthDialog(par,Message.say("BoardEditor","mLaser"),true);
	    par.spf.getHWand(par.spfX, 11-par.spfY).da=true;
	    par.spf.getHWand(par.spfX, 11-par.spfY).wandEl0(1);
	    par.spf.getHWand(par.spfX, 11-par.spfY).spez0(par.laserSt);
	}
	else{ //fliessband etc.
	    par.spf.getBoden(par.spfX+1,12-par.spfY).typ =par.elemTyp[par.indx];
	    par.spf.getBoden(par.spfX+1,12-par.spfY).spez=par.elemSpez[par.indx];
	}
	CAT.debug("hab' ich");
	repaint();
    }

}//ende BoardPanel
