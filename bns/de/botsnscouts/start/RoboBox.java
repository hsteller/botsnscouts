package de.botsnscouts.start;


import java.awt.*;
import javax.swing.*;

class RoboBox extends JComboBox {
    boolean withEgal;

    public RoboBox(boolean egal) {
	String[] farben;
	
	withEgal = true;
	setOpaque( false );
	
	if( withEgal ) {
	    farben = new String[] {Message.say("Start","mFarbeEgal"),Message.say("Start","mFarbeGruen"),Message.say("Start","mFarbeGelb"),Message.say("Start","mFarbeRot"),Message.say("Start","mFarbeBlau"),Message.say("Start","mFarbeMagenta"),Message.say("Start","mFarbeOrange"),Message.say("Start","mFarbeGrau"),Message.say("Start","mFarbeDunkelMagenta") };
	}
	else {
	    farben = new String[] {Message.say("Start","mFarbeGruen"),Message.say("Start","mFarbeGelb"),Message.say("Start","mFarbeRot"),Message.say("Start","mFarbeBlau"),Message.say("Start","mFarbeMagenta"),Message.say("Start","mFarbeOrange"),Message.say("Start","mFarbeGrau"),Message.say("Start","mFarbeDunkelMagenta") };
	}

	setModel( new DefaultComboBoxModel( farben ) );

	setRenderer( new MyCellRenderer( true ) );
	setLightWeightPopupEnabled(false);
	setSelectedIndex(0);
    }
}
	
