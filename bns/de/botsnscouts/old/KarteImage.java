package de.spline.rr;

import java.awt.*;
/**
 * Diese Klasse enthält die Graphiken der Karten
 * @author Lukasz Pekacki
 */
class KarteImage {
    public Image RUECK;
    public Image M1; 
    public Image M2; 
    public Image M3; 
    public Image BU; 
    public Image RL; 
    public Image RR; 
    public Image UT; 
    public Image RLEER;

    public KarteImage(){
	// Back-Up-Karte erzeugen
	Graphics g = RUECK.getGraphics();
	// g.setSize(60,100);
}
}
