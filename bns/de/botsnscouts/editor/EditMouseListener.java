/*
 * Created by dirk.
 * Date: Mar 9, 2002, 10:22:33 PM
 * Id: $Id$
 */

package de.botsnscouts.editor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.apache.log4j.Category;

import de.botsnscouts.util.Message;

class EditMouseListener extends MouseAdapter{
    private BoardEditor editor=null;
    static final Category CAT=Category.getInstance(EditMouseListener.class);

    public EditMouseListener(BoardEditor editor) {
        this.editor = editor;
    }

    public void mouseClicked(MouseEvent e){
	editor.spfX=e.getX()/64;
	editor.spfY=e.getY()/64;

	int mods = e.getModifiers();
        if ((mods & MouseEvent.BUTTON3_MASK) != 0)
            handleRightClick(e);
 	else if ((mods & MouseEvent.BUTTON1_MASK) != 0)
 	    handleLeftClick(e);
    }

    private void handleRightClick(MouseEvent e) {
        editor.setFlag(editor.spfX+1, 12-editor.spfY);
    }

    private void handleLeftClick(MouseEvent e){
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
        editor.boardView.initFloorHashMap();

	}
	CAT.debug("hab' ich");

    editor.repaint();
    }

}
