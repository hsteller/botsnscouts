package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.border.*;

/**
 * Zeigt das Spielfeld und alle Statusmeldungen an
 * @author Lukasz Pekacki
 */

public class AusgabeView extends JPanel implements AusgabeViewInterface {
    // --- Objekte
    JScrollPane spielFeldScrollFenster;
    JViewport spielFeldView;
    SACanvas  spielFeld;
    Ausgabe   ausgabe;

    /** @args SpielerMensch spielerref ist Referenz auf umgebenden 
     *  MenschlichenSpieler, falls Ausgabe zu einem Spieler gehoert,
     *  null sonst.
     */
    public AusgabeView() {
    }

    public AusgabeView(SACanvas sa) {
	spielFeld=sa;

	// Layout erzeugen
	setLayout(new BorderLayout());

	// create scroll window
	spielFeldScrollFenster = new JScrollPane();
	spielFeldScrollFenster.getHorizontalScrollBar().setUnitIncrement(64);
	spielFeldScrollFenster.getVerticalScrollBar().setUnitIncrement(64);
	spielFeldScrollFenster.setViewportView(spielFeld);
	spielFeld.setScrollPane(spielFeldScrollFenster);
	//	spielFeldScrollFenster.validate();
	add(spielFeldScrollFenster,BorderLayout.CENTER);
	//	validate();
	spielFeldView = spielFeldScrollFenster.getViewport();

    }
    /*

    public Dimension getPreferredSize() {
	return new Dimension(800,600);
    }

    public Dimension getMinimumSize() {
	return new Dimension(800,600);
    }

    */

    public void shutup() {
	System.exit(0);
    }
   


    /**
     * Schreibt in die Statuszeile einen Text
     */
    public void showActionMessage(String s){
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
     *  shows the winner list at game over
     */
    public void showWinnerlist (String[] winners) {
    }


}



