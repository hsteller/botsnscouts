package de.botsnscouts.util;

import javax.swing.*;
import java.awt.*;
import javax.swing.plaf.*;
import javax.swing.plaf.metal.*;

class GreenTheme extends DefaultMetalTheme {
	public String getName() {
		return "botsnscouts"; 
	}

	private final ColorUIResource prim1 = new ColorUIResource( new Color(64,255,64) );
	private final ColorUIResource prim2 = new ColorUIResource( new Color(64,192,64) );
	private final ColorUIResource prim3 = new ColorUIResource( new Color(64,128,64) );

	private final ColorUIResource sec1 = new ColorUIResource( new Color(0,128,0) ); 
	private final ColorUIResource sec2 = new ColorUIResource( new Color(0,96,0) ); 
	private final ColorUIResource sec3 = new ColorUIResource( new Color(0,64,0) ); 

	
/*
	private final ColorUIResource prim1 = new ColorUIResource(51, 76, 51);
	private final ColorUIResource prim2 = new ColorUIResource(76, 102, 76);
	private final ColorUIResource prim3 = new ColorUIResource(102, 128, 102);
*/
	protected ColorUIResource getPrimary1() { return prim1; }
	protected ColorUIResource getPrimary2() { return prim2; }
	protected ColorUIResource getPrimary3() { return prim3; }
	protected ColorUIResource getSecondary1() { return sec1; }
	protected ColorUIResource getSecondary2() { return sec2; }
	protected ColorUIResource getSecondary3() { return sec3; }
	
    private final ColorUIResource black = new ColorUIResource( Color.lightGray ); 
	private final ColorUIResource white = new ColorUIResource( Color.black );
	protected ColorUIResource getBlack() { return black; }
	protected ColorUIResource getWhite() { return white; }
}

