package de.botsnscouts.editor;

import java.awt.*;
import java.awt.event.*;

import de.botsnscouts.board.SpielfeldSim;
import de.botsnscouts.board.Wall;
import de.botsnscouts.gui.SACanvas;
import de.botsnscouts.util.Message;
import org.apache.log4j.Category;


class BoardPanel extends SACanvas implements MouseListener{
    static final Category CAT = Category.getInstance( BoardPanel.class );

    protected BoardEditor editor=null;

    public BoardPanel(SpielfeldSim ss,BoardEditor p){
	super(ss);
	editor=p;
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
	editor.spfX=e.getX()/64;
	editor.spfY=e.getY()/64;
	int vWInd=(e.getX()+32)/64;
	int hWInd=(e.getY()+32)/64;
	CAT.debug("(x,y):typ=="+editor.spfX+","+editor.spfY+":"+editor.indx);

	if(editor.indx==54){//pusher nach west
	    new PhaseDialog(editor,Message.say("BoardEditor","mPusher"),true);
            editor.board.setWestPusher( editor.spfX+1, 11-editor.spfY, editor.phasen );
	}
	else if(editor.indx==55){//pusher nach sued
	    new PhaseDialog(editor,Message.say("BoardEditor","mPusher"),true);
            editor.board.setSouthPusher( editor.spfX, 12-editor.spfY, editor.phasen );
	}
	else if(editor.indx==56){//pusher nach nord
	    new PhaseDialog(editor,Message.say("BoardEditor","mPusher"),true);
	    editor.board.setNorthPusher(editor.spfX, 11-editor.spfY, editor.phasen);
	}
	else if(editor.indx==57){//pusher nach ost
	    new PhaseDialog(editor,Message.say("BoardEditor","mPusher"),true);
	    editor.board.setEastPusher(editor.spfX, 11-editor.spfY, editor.phasen );
	}
	else if(editor.indx==58){//crusher
	    int tp=editor.board.getBoden(editor.spfX+1,12-editor.spfY).typ;
	    if((tp>=100)&&(tp<=253)){
		new PhaseDialog(editor,Message.say("BoardEditor","mCrusher"),true);
		editor.board.getBoden(editor.spfX+1,12-editor.spfY).spez=editor.phasen;
	    }
	}
	else if(editor.indx==59){//leer
	    editor.board.getBoden(editor.spfX+1,12-editor.spfY).spez=0;
	    editor.board.getBoden(editor.spfX+1,12-editor.spfY).typ=0;

            editor.board.removeVWall(editor.spfX+1, 11-editor.spfY);
            editor.board.removeVWall(editor.spfX, 11-editor.spfY);
            editor.board.removeHWall(editor.spfX, 12-editor.spfY);
            editor.board.removeHWall(editor.spfX, 11-editor.spfY);
	}
	else if(editor.indx==60){//vert wand
	    editor.board.addVWall(vWInd, 11-editor.spfY);
	}
	else if(editor.indx==61){//horiz. wand
	    editor.board.addHWall(editor.spfX, 12-hWInd);
	}
	else if(editor.indx==62){//laser nach west
	    new StrengthDialog(editor,Message.say("BoardEditor","mLaser"),true);
            editor.board.setWestLaser( editor.spfX+1, 11-editor.spfY, editor.laserSt );
	}
	else if(editor.indx==63){//laser nach sued
	    new StrengthDialog(editor,Message.say("BoardEditor","mLaser"),true);
            editor.board.setSouthLaser( editor.spfX, 12-editor.spfY, editor.laserSt );
	}
	else if(editor.indx==64){//laser nach ost
	    new StrengthDialog(editor,Message.say("BoardEditor","mLaser"),true);
	    editor.board.setEastLaser(editor.spfX, 11-editor.spfY, editor.laserSt );
	}
	else if(editor.indx==65){//laser nach nord
	    new StrengthDialog(editor,Message.say("BoardEditor","mLaser"),true);
	    editor.board.setNorthLaser(editor.spfX, 11-editor.spfY, editor.laserSt);
	}
	else{ //fliessband etc.
	    editor.board.getBoden(editor.spfX+1,12-editor.spfY).typ =editor.elemTyp[editor.indx];
	    editor.board.getBoden(editor.spfX+1,12-editor.spfY).spez=editor.elemSpez[editor.indx];
	}
	CAT.debug("hab' ich");
	repaint();
    }

}//ende BoardPanel
