package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;

public class StartSpielfeldSpf extends JPanel{
    Start parent;
    StartKachelComp[][] kachP;
    StSpFassade fassade;

    public StartSpielfeldSpf(Start par){
	parent=par;
	fassade=par.fassade;
	Ort spfDim=parent.fassade.getSpielfeldDim();
	GridLayout lay=new GridLayout(spfDim.y,spfDim.x);
	lay.setHgap(0);
	lay.setVgap(0);
	setLayout(lay);
	setOpaque(false);
	//setBorder(new EmptyBorder(50,50,50,50));

	kachP=new StartKachelComp[spfDim.x][spfDim.y];
	//initialisiere Panels für jede Kachel
	for (int j=spfDim.y-1;j>=0;j--){
	    for (int i=0;i<spfDim.x;i++){
		kachP[i][j]=new StartKachelComp(par.fassade,i,j);
		add(kachP[i][j]);
	    }
	}
	//lade default Spielfeld
	try{
	     fassade.setKachel(0,0,Message.say("Start","mKachel00"));
	}catch(FlaggenVorhandenException e){
	    System.err.println(e);
	}
	//lade default Flaggen
	for (int i=0;i<Integer.parseInt(Message.say("Start","mFlaggen"));i++){
	    try{
		fassade.addFlagge(Integer.parseInt(Message.say("Start","mFlagge"+(i+1)+"x")),Integer.parseInt(Message.say("Start","mFlagge"+(i+1)+"y")));
	    }catch(FlaggenException e){
		System.err.println(e);
	    }
	}
	//sage der Kachel 0,0 sie soll sich aktualisieren
	kachP[0][0].rasterChanged();

    }

    public void rasterChanged(){
	for (int i=0;i<kachP.length;i++){
	    for (int j=0;j<kachP[0].length;j++){
		kachP[i][j].rasterChanged();
	    }
	}
    }

    public void addKachelClickListener(KachelClickListener kachelClickL){
	for (int i=0;i<kachP.length;i++){
	    for (int j=0;j<kachP[0].length;j++){
		kachP[i][j].addKachelClickListener(kachelClickL);
	    }
	}

    }

    public void removeKachelClickListener(){
	for (int i=0;i<kachP.length;i++){
	    for (int j=0;j<kachP[0].length;j++){
		kachP[i][j].removeKachelClickListener();
	    }
	}

    }

}
