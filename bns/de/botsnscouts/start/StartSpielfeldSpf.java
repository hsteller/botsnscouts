package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import de.botsnscouts.util.*;
import de.botsnscouts.board.*;

public class StartSpielfeldSpf extends JPanel{
    Start parent;
    StartTileComp[][] kachP;
    Facade fassade;

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

	kachP=new StartTileComp[spfDim.x][spfDim.y];
	//initialisiere Panels für jede Kachel
	for (int j=spfDim.y-1;j>=0;j--){
	    for (int i=0;i<spfDim.x;i++){
		kachP[i][j]=new StartTileComp(par.fassade,i,j);
		add(kachP[i][j]);
	    }
	}
    }

    public void rasterChanged(){
	for (int i=0;i<kachP.length;i++){
	    for (int j=0;j<kachP[0].length;j++){
		kachP[i][j].rasterChanged();
	    }
	}
    }

    public void addTileClickListener(TileClickListener tileClickL){
	for (int i=0;i<kachP.length;i++){
	    for (int j=0;j<kachP[0].length;j++){
		kachP[i][j].addTileClickListener(tileClickL);
	    }
	}

    }

    public void removeTileClickListener(){
	for (int i=0;i<kachP.length;i++){
	    for (int j=0;j<kachP[0].length;j++){
		kachP[i][j].removeTileClickListener();
	    }
	}

    }

}
