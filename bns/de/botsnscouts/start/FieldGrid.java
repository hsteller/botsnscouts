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
 
package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import javax.swing.filechooser.FileFilter;
import java.util.*;
import java.io.*;
import de.botsnscouts.util.*;

class FieldGrid extends JPanel{
    Start parent;
    TileComponent[][] tileP;
    Facade fassade;

    FieldGrid(Start par){
	parent=par;
	fassade=par.fassade;
	Ort spfDim=parent.fassade.getSpielfeldDim();
	GridLayout lay=new GridLayout(spfDim.y,spfDim.x);
	lay.setHgap(0);
	lay.setVgap(0);
	setLayout(lay);
	setOpaque(false);

	tileP=new TileComponent[spfDim.x][spfDim.y];
	//initialisiere Panels für jede Kachel
	for (int j=spfDim.y-1;j>=0;j--){
	    for (int i=0;i<spfDim.x;i++){
		tileP[i][j]=new TileComponent(par.fassade,i,j);
		add(tileP[i][j]);
	    }
	}
    }

    public void rasterChanged(){
	for (int i=0;i<tileP.length;i++){
	    for (int j=0;j<tileP[0].length;j++){
		tileP[i][j].rasterChanged();
	    }
	}
    }

    public void addTileClickListener(TileClickListener tileClickL){
	for (int i=0;i<tileP.length;i++){
	    for (int j=0;j<tileP[0].length;j++){
		tileP[i][j].addTileClickListener(tileClickL);
	    }
	}
    }

    public void removeTileClickListener(){
	for (int i=0;i<tileP.length;i++){
	    for (int j=0;j<tileP[0].length;j++){
		tileP[i][j].removeTileClickListener();
	    }
	}
    }
}
