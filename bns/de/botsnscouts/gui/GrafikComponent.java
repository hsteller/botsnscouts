package de.botsnscouts.gui;


 /**
  * Unterklasse, die es ermöglicht, bequem Bilder einzufügen
  * @author Lukasz Pekacki
  * @version 0.7
  * @see Ausgabe 
  */

import java.awt.*;


public class GrafikComponent extends Canvas {
    
	protected Image img;
 
	public GrafikComponent(String fname)
	{
		img = getToolkit().getImage(fname);
		MediaTracker mt = new MediaTracker(this);
 
		mt.addImage(img, 0);
		try {
			//Warten, bis das Image vollständig geladen ist,
			//damit getWidth() und getHeight() funktionieren
			mt.waitForAll();
		} catch (InterruptedException e) {
			//nothing
		}

	    
	}

	public GrafikComponent(Image i) {
		
		img = i;
		MediaTracker mt = new MediaTracker(this);
 
		mt.addImage(img, 0);
		try {
			//Warten, bis das Image vollständig geladen ist,
			//damit getWidth() und getHeight() funktionieren
			mt.waitForAll();
		} catch (InterruptedException e) {
			//nothing
		}
	}
 
	public void paint(Graphics g) {
	
		g.drawImage(img,1,1,this);
	}
 
	public Dimension getPreferredSize()
	{
		return new Dimension(
							 img.getWidth(this),
							 img.getHeight(this)
							 );
	}
 
 	public Dimension getMinimumSize()
	{
		return new Dimension(
							 img.getWidth(this),
							 img.getHeight(this)
							 );
	}
		public Dimension getMaximumSize()
	{
		return new Dimension(
							 img.getWidth(this),
							 img.getHeight(this)
							 );
	}
}
