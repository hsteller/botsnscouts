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
            int x = editor.spfX + 1;
            int y = 12 - editor.spfY;
	    if( editor.board.getFloor(x, y).isBelt() ){
		new PhaseDialog(editor,Message.say("BoardEditor","mCrusher"),true);
		editor.board.setCrusher( x, y, editor.phasen );
	    }
	}
	else if(editor.indx==59){//leer
            int x = editor.spfX + 1;
            int y = 12 - editor.spfY;

	    editor.board.clearFloor(  x  , y );

            editor.board.removeVWall( x  , y-1 );
            editor.board.removeVWall( x-1, y-1 );
            editor.board.removeHWall( x-1, y   );
            editor.board.removeHWall( x-1, y-1 );
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
	    editor.board.setFloor(editor.spfX+1,12-editor.spfY, editor.elemTyp[editor.indx], editor.elemSpez[editor.indx] );
	}
	CAT.debug("hab' ich");
	repaint();
    }

}//ende BoardPanel
