package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Zeigt das Spielfeld und alle Statusmeldungen an
 * @author Lukasz Pekacki
 */

public class AusgabeView extends JPanel implements AusgabeViewInterface {
    // --- objects
    private JScrollPane spielFeldScrollFenster;
    private JViewport spielFeldView;
    private SACanvas  spielFeld;
    private Ausgabe   ausgabe;
    private Hashtable robotStatus = new Hashtable(8);
    private StatusLog statusLog = new StatusLog();


    /** @args SpielerMensch spielerref ist Referenz auf umgebenden 
     *  MenschlichenSpieler, falls Ausgabe zu einem Spieler gehoert,
     *  null sonst.
     */
    public AusgabeView() {
    }

    public AusgabeView(SACanvas sa, Roboter[] robots, Ausgabe aus) {
	ausgabe = aus;
	spielFeld=sa;
	JPanel robotsStatusContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));

	setLayout(new BorderLayout());

	// create status panel
	for (int i=0; i< robots.length; i++) {
	    RobotStatus r = new RobotStatus(robots[i], new MouseAdapter(){
		    public void mouseClicked(MouseEvent me) {
			ausgabe.trackRob(((JLabel)me.getSource()).getName());
		    }
		});
	    robotsStatusContainer.add(r);
	    robotStatus.put(robots[i].getName(),r);
	}
	add(robotsStatusContainer,BorderLayout.NORTH);
	

	// create status log
	add(statusLog,BorderLayout.SOUTH);

	// create scroll panel
	spielFeldScrollFenster = new JScrollPane();
	spielFeldScrollFenster.getHorizontalScrollBar().setUnitIncrement(64);
	spielFeldScrollFenster.getVerticalScrollBar().setUnitIncrement(64);
	spielFeldScrollFenster.setViewportView(spielFeld);
	spielFeld.setScrollPane(spielFeldScrollFenster);
	add(spielFeldScrollFenster,BorderLayout.CENTER);
	spielFeldView = spielFeldScrollFenster.getViewport();

    }

    public void shutup() {
	System.exit(0);
    }
   


    /**
     * show the sinlge line action message that came from the server
     */
    public void showActionMessage(String s){
	statusLog.addMessage(s);
    }


    /**
     * Schreibt in die Statuszeile einen Text
     */
    public void showRobStatus(Roboter r){
    }


    /**
     * Shows the new Positions of the Robots
     */
    public void showUpdatedRobots(Roboter[] r){
	spielFeld.ersetzeRobos(r);
	for (int i = 0; i < r.length; i++) {
	    ((RobotStatus) robotStatus.get(r[i].getName())).updateRobot(r[i]);
	}
    }


    public void showPos(int robix, int robiy) {

	int x = robix*64;
	int y = spielFeld.getHeight()-(robiy*64);
		
	Dimension sz = spielFeldView.getExtentSize();
	int w2 = sz.width/2;
	int h2 = sz.height/2;
	

	// make sure we dont want to scoll 'out' to
	// the left and top
	int x1 = Math.max( x - w2 , 0);
	int y1 = Math.max( y - h2 , 0);

	// soll ich überhaupt scrollen?
	// in X-Richtung
	if ((x < spielFeldView.getViewPosition().x) || 
	    x > (spielFeldView.getViewPosition().x+sz.width)) {
	    x1 = Math.min( x1, (spielFeld.getWidth() - sz.width) );
	}
	else x1 = spielFeldView.getViewPosition().x;

	// in Y-Richtung
	if ((y < spielFeldView.getViewPosition().y) || 
	    y > (spielFeldView.getViewPosition().y+sz.height)) {
	    y1 = Math.min( y1, (spielFeld.getHeight() - sz.height) );
	}
	else y1 = spielFeldView.getViewPosition().y;
	
	spielFeldView.setViewPosition(new Point(x1, y1));
    }


    /**
     * board view is to paint robolaser activity
     */
    public void showRobLaser(Roboter von, Roboter nach){
	spielFeld.doRobLaser(von, nach);
    }

    /**
     * board view is to paint bord laser activity
     */
    public void showBoardLaser(Ort laserPos, int facing, int stregth, Ort r1Pos){
	spielFeld.doBordLaser(laserPos, facing, stregth, r1Pos,spielFeldView);
    }

    /**
     *  shows the winner state of the game in the mids of the game
     */
    public void showWinnerState (String[] gameState) {
	int i=0;
	while (i<gameState.length&&!gameState[i].equals("null")) {
	    d("getting ranking of "+gameState[i]+" zu holen");
	    ((RobotStatus) robotStatus.get(gameState[i])).setWinnerNumber((i+1));
	    
	    i++;
	}
    }


    /**
     *  shows the winner list at game over
     */
    public void showWinnerlist (String[] winners) {
	spielFeld.setVisible(false);
	add(new Abspann(winners),BorderLayout.CENTER);
	validate();
    }

    protected void showScout(int chosen, Roboter[] robs) {
	spielFeld.vorschau(chosen,robs);
    }



    private void d(String s){
	Global.debug(this, s);
    }


}



