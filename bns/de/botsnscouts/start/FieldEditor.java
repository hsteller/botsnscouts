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
import javax.swing.*;
import javax.swing.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import de.botsnscouts.util.*;
import de.botsnscouts.board.*;
import de.botsnscouts.gui.ColoredPanel;
import de.botsnscouts.widgets.TransparentButton;

public class FieldEditor extends JPanel implements  ActionListener, TileClickListener,ListSelectionListener{

    public static final int MODE_FLAGGE_SETZEN=0;
    public static final int MODE_FLAGGE_ENTFERNEN=1;
    public static final int MODE_FLAGGE_VERSCHIEBEN=2;

    public static final int MODE_TILE_SETZEN=3;
    public static final int MODE_TILE_ENTFERNEN=4;
    public static final int MODE_TILE_DREHEN=5;

    public static final int CURSOR_DEFAULT = 0,
	CURSOR_FLAGGE_SETZBAR = 1,
	CURSOR_FLAGGE_NICHT_SETZBAR = 2,
	CURSOR_FLAGGE_VERSCHIEBEN = 3,
	CURSOR_FLAGGE_NICHT_OK = 4,
	CURSOR_FLAGGE_LOESCHEN = 5;


    FieldGrid spf;
    Start parent;
    Paint paint;

    JToggleButton modeFlaggeSetzen;
    JToggleButton modeFlaggeEntfernen;
    JToggleButton modeFlaggeVerschieben;

    JToggleButton modeTileSetzen;
    JToggleButton modeTileEntfernen;
    JToggleButton modeTileDrehen;

    ButtonGroup tilesGroup;

    JButton ok;
    JButton zurueck;

    JPanel flaggenButtons;
    JPanel fuerSpf;
    JScrollPane fuerfuerSpf;

    JScrollPane fuerTileListe;
    JList tileListe;
    JPanel okZur;

    int currentMode;
    int currentTile;

    boolean kannFlaggeSetzen;
    boolean flaggeGewaehlt;
    boolean istFlagge;
    String istFlaggeGut;
    int   flaggeX,flaggeY;

    Image[] images;
    TileInfo[] tileInfos;
    Cursor[] cursors;

    public FieldEditor(Start par,FieldGrid spf){
	parent=par;
	paint=parent.paint;
	this.spf=spf;
	currentMode=3;
	flaggeGewaehlt=false;
	initCursors();

	images=ImageMan.getImages(ImageMan.STARTKNOEPFE);

	tileInfos=parent.fassade.getTileInfos();

	BorderLayout lay=new BorderLayout();

	setLayout(lay);
	setOpaque(false);

	modeFlaggeSetzen=new JToggleButton(new ImageIcon(images[0]));
	modeFlaggeEntfernen=new JToggleButton(new ImageIcon(images[2]));
	modeFlaggeVerschieben=new JToggleButton(new ImageIcon(images[1]));
	modeFlaggeSetzen.setOpaque(false);
	modeFlaggeEntfernen.setOpaque(false);
	modeFlaggeVerschieben.setOpaque(false);
	modeFlaggeSetzen.setToolTipText(Message.say("Start","mTTFlaggeSetzen"));
	modeFlaggeEntfernen.setToolTipText(Message.say("Start","mTTFlaggeEntfernen"));
	modeFlaggeVerschieben.setToolTipText(Message.say("Start","mTTFlaggeVerschieben"));
	modeFlaggeSetzen.setActionCommand("flSetzen");
	modeFlaggeEntfernen.setActionCommand("flEntfernen");
	modeFlaggeVerschieben.setActionCommand("flVerschieben");
	modeFlaggeSetzen.addActionListener(this);
	modeFlaggeEntfernen.addActionListener(this);
	modeFlaggeVerschieben.addActionListener(this);

	modeTileSetzen=new JToggleButton(new ImageIcon(images[5]));
	modeTileEntfernen=new JToggleButton(new ImageIcon(images[4]));
	modeTileDrehen=new JToggleButton(new ImageIcon(images[3]));
	modeTileSetzen.setOpaque(false);
	modeTileEntfernen.setOpaque(false);
	modeTileDrehen.setOpaque(false);
	modeTileSetzen.setToolTipText(Message.say("Start","mTTKachelSetzen"));
	modeTileEntfernen.setToolTipText(Message.say("Start","mTTKachelEntfernen"));
	modeTileDrehen.setToolTipText(Message.say("Start","mTTKachelDrehen"));
	modeTileSetzen.setActionCommand("kachSetzen");
	modeTileEntfernen.setActionCommand("kachEntfernen");
	modeTileDrehen.setActionCommand("kahcDrehen");
	modeTileSetzen.addActionListener(this);
	modeTileEntfernen.addActionListener(this);
	modeTileDrehen.addActionListener(this);

	tilesGroup=new ButtonGroup();
	tilesGroup.add(modeTileSetzen);
	tilesGroup.add(modeTileEntfernen);
	tilesGroup.add(modeTileDrehen);
	tilesGroup.add(modeFlaggeSetzen);
	tilesGroup.add(modeFlaggeEntfernen);
	tilesGroup.add(modeFlaggeVerschieben);
	tilesGroup.setSelected(modeTileSetzen.getModel(),true);

	flaggenButtons=new ColoredPanel();
	flaggenButtons.setLayout(new FlowLayout());
	flaggenButtons.setOpaque(false);
	flaggenButtons.add(modeFlaggeSetzen);
	flaggenButtons.add(modeFlaggeEntfernen);
	flaggenButtons.add(modeFlaggeVerschieben);
	flaggenButtons.add(Box.createRigidArea(new Dimension(100,20)));
	flaggenButtons.add(modeTileSetzen);
	flaggenButtons.add(modeTileEntfernen);
	flaggenButtons.add(modeTileDrehen);
	add(BorderLayout.NORTH,flaggenButtons);

	tileListe=new JList(tileInfos);

	tileListe.setCellRenderer(new ThumbsCellRenderer());
	tileListe.setOpaque(false);
	fuerTileListe=new JScrollPane();
	fuerTileListe.getViewport().setView(tileListe);

	add(BorderLayout.EAST,fuerTileListe);
	tileListe.addListSelectionListener(this);

	ok=new TransparentButton(Message.say("Start","mOK"));
	zurueck=new TransparentButton(Message.say("Start","mAbbr"));
	ok.setActionCommand("ok");
	ok.addActionListener(this);
	zurueck.addActionListener(this);
	zurueck.setActionCommand("abbrechen");

	okZur=new ColoredPanel();
	okZur.setLayout(new FlowLayout());
	okZur.setOpaque(false);
	okZur.add(ok);
	okZur.add(zurueck);
	add(BorderLayout.SOUTH,okZur);
	
	fuerSpf=new JPanel();
	fuerSpf.setOpaque(false);
	fuerSpf.add(spf);

	fuerfuerSpf=new JScrollPane();
	fuerfuerSpf.setOpaque(false);
	fuerfuerSpf.getViewport().setView(fuerSpf);
	fuerfuerSpf.getViewport().scrollRectToVisible(new Rectangle(0,540,10,530));
	add(BorderLayout.CENTER,fuerfuerSpf);

	spf.addTileClickListener(this);

    }

    public void actionPerformed(ActionEvent e){ 
	if(e.getActionCommand().equals("ok")){
	    try{
		parent.fassade.checkSpielfeld();
		parent.showGameFieldPanel();
		spf.removeTileClickListener();
		parent.gameFieldPanel.pnl.add(spf);
	    }catch(OneFlagException ex){
		JOptionPane.showMessageDialog(this,Message.say("Start","mZweiFlaggen"),Message.say("Start","mError"),JOptionPane.ERROR_MESSAGE);

	    }catch(NonContiguousMapException ex){
		JOptionPane.showMessageDialog(this,Message.say("Start","mNichtZus"),Message.say("Start","mError"),JOptionPane.ERROR_MESSAGE);

	    }

	}else if(e.getActionCommand().equals("abbrechen")){
	    if(parent.gameFieldPanel==null){
		parent.gameFieldPanel=new GameFieldPanel(parent);
	    }
	    parent.fassade.restorTileRaster();
	    spf.removeTileClickListener();
	    parent.gameFieldPanel.pnl.add(spf);
	    parent.showGameFieldPanel();
	    spf.rasterChanged();

	    parent.gameFieldPanel.unrollOverButs();
	    parent.show();
	}else if(e.getActionCommand().equals("flSetzen")){
	    currentMode=MODE_FLAGGE_SETZEN;
	}else if(e.getActionCommand().equals("flEntfernen")){
	    currentMode=MODE_FLAGGE_ENTFERNEN;
	}else if(e.getActionCommand().equals("flVerschieben")){
	    currentMode=MODE_FLAGGE_VERSCHIEBEN;
	    flaggeGewaehlt=false;
	}else if(e.getActionCommand().equals("kachSetzen")){
	    currentMode=MODE_TILE_SETZEN;
	}else if(e.getActionCommand().equals("kachEntfernen")){
	    currentMode=MODE_TILE_ENTFERNEN;
	}else if(e.getActionCommand().equals("kahcDrehen")){
	    currentMode=MODE_TILE_DREHEN;
	}
    }

    public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	Dimension d = getSize();
	g2d.setPaint( paint );
	g2d.fillRect(0,0, d.width, d.height);
	paintChildren(g);
    }

    public void tileClick(int rx,int ry,int fx,int fy){
	switch(currentMode){
	case  MODE_FLAGGE_SETZEN:{
	    try{
		if(kannFlaggeSetzen){
		      if(!istFlaggeGut.equals("")){
			  int ret=JOptionPane.showConfirmDialog(this,istFlaggeGut+Message.say("Start","mWirklich"),Message.say("Start","mKachelSetzenTitel"),JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,new ImageIcon(images[0]));
			if(ret!=JOptionPane.YES_OPTION){
			    return;
			}
		    }
		    parent.fassade.addFlagge(rx*12+fx,ry*12+fy);
		    spf.rasterChanged();
		}else{
		    //System.err.println("You cannot place a flag here!");
		}
	    }catch (FlagException ex){
		System.err.println("You cannot place a flag here!");
	    }

	    break;
	}
	case  MODE_FLAGGE_ENTFERNEN:{
	    parent.fassade.delFlagge(rx*12+fx,ry*12+fy);
	    spf.rasterChanged();
	    break;
	}
	case  MODE_FLAGGE_VERSCHIEBEN:{
	    if((!flaggeGewaehlt)&&istFlagge){
		flaggeX=rx*12+fx;
		flaggeY=ry*12+fy;
		flaggeGewaehlt=true;
	    }else if(flaggeGewaehlt&&kannFlaggeSetzen){
		if(!istFlaggeGut.equals("")){
		    int ret=JOptionPane.showConfirmDialog(this,istFlaggeGut+Message.say("Start","mWirklich"),Message.say("Start","mKachelSetzenTitel"),JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,new ImageIcon(images[0]));
		    if(ret!=JOptionPane.YES_OPTION){
			return;
		    }
		}
		try{
		    parent.fassade.moveFlagge(flaggeX,flaggeY,rx*12+fx,ry*12+fy);
		}catch (FlagException ex){
		    System.err.println("You cannot place a flag here!");
		}
		flaggeGewaehlt=false;
		spf.rasterChanged();
	    }
	    break;
	}
	case  MODE_TILE_SETZEN:{
	    if(parent.fassade.sindFlaggen(rx,ry)){
		int ret=JOptionPane.showConfirmDialog(this,Message.say("Start","mKachelSetzen"),Message.say("Start","mKachelSetzenTitel"),JOptionPane.YES_NO_OPTION,JOptionPane.DEFAULT_OPTION,new ImageIcon(images[4]));
		//fragen,ob kachel mit den flaggen gelöscht werden soll
		if(ret!=JOptionPane.YES_OPTION){
		    return;
		}
	    }
	    try{
		parent.fassade.delTile(rx,ry);
		parent.fassade.setTile(rx,ry,0,tileInfos[currentTile].toString());
	    }catch (FlagPresentException ex){
		System.err.println("You cannot place here a field!");
	    }
	    spf.rasterChanged();
	    break;
	}
	case  MODE_TILE_ENTFERNEN:{
	    if(parent.fassade.sindFlaggen(rx,ry)){
		//fragen,ob kachel mit den flaggen gelöscht werden soll
		int ret=JOptionPane.showConfirmDialog(this,Message.say("Start","mKachelLoeschen"),Message.say("Start","mKachelSetzenTitel"),JOptionPane.YES_NO_OPTION,JOptionPane.DEFAULT_OPTION,new ImageIcon(images[4]));
		if(ret==JOptionPane.YES_OPTION){
		    parent.fassade.delTile(rx,ry);
		    spf.rasterChanged();
		}
	    }else{
		parent.fassade.delTile(rx,ry);
		spf.rasterChanged();
	    }

	    break;
	}
	case  MODE_TILE_DREHEN:{
	    parent.fassade.rotTile(rx,ry);
	    spf.rasterChanged();
	    
	    break;
	}
	}
    }

    public void tileMouseMove(int rx,int ry,int fx,int fy){
	switch(currentMode){
	case  MODE_FLAGGE_SETZEN:{
	    kannFlaggeSetzen=parent.fassade.checkFlaggePos(rx*12+fx,ry*12+fy); 
	    if (kannFlaggeSetzen){
		istFlaggeGut=parent.fassade.getFlaggeKomment(rx*12+fx,ry*12+fy);
		if (istFlaggeGut.equals("")){
		    setCursor(cursors[CURSOR_FLAGGE_SETZBAR]);
		}else{
		    setCursor(cursors[CURSOR_FLAGGE_NICHT_OK]);
		}
	    }else{
		setCursor(cursors[CURSOR_FLAGGE_NICHT_SETZBAR]);
	    }
	    break;
	}
	case  MODE_FLAGGE_ENTFERNEN:{
	    setCursor(cursors[CURSOR_FLAGGE_LOESCHEN]);
	    break;
	}
	case  MODE_FLAGGE_VERSCHIEBEN:{
	    if(!flaggeGewaehlt){
		istFlagge=parent.fassade.istFlagge(rx*12+fx,ry*12+fy);
		if (istFlagge){
		    setCursor(cursors[CURSOR_FLAGGE_VERSCHIEBEN]);
		}else{
		    setCursor(cursors[CURSOR_DEFAULT]);
		}
	    }else{
		kannFlaggeSetzen=parent.fassade.checkFlaggeMovePos(rx*12+fx,ry*12+fy);
		if (kannFlaggeSetzen){
		    istFlaggeGut=parent.fassade.getFlaggeKomment(rx*12+fx,ry*12+fy);
		    if (istFlaggeGut.equals("")){
			setCursor(cursors[CURSOR_FLAGGE_SETZBAR]);
		    }else{
			setCursor(cursors[CURSOR_FLAGGE_NICHT_OK]);
		    }
		}else{
		    setCursor(cursors[CURSOR_FLAGGE_NICHT_SETZBAR]);
		}
	    }
	    break;
	}
	case  MODE_TILE_SETZEN:{
	    break;
	}
	case  MODE_TILE_ENTFERNEN:{

	    break;
	}
	case  MODE_TILE_DREHEN:{

	    break;
	}
	}
    }

    public void tileMouseLeave(){
	//Global.debug(this,"Mouse left");
	setCursor(cursors[CURSOR_DEFAULT]);
    }

    public void valueChanged(ListSelectionEvent e) {
	//Global.debug(this,"selected tile "+tileListe.getSelectedIndex());
	currentTile=tileListe.getSelectedIndex();
	tilesGroup.setSelected(modeTileSetzen.getModel(),true);
	currentMode=MODE_TILE_SETZEN;
	setCursor(cursors[CURSOR_DEFAULT]);
	flaggenButtons.repaint();
	//repaint();
    }

    private void initCursors(){
	Image[] cursorImages=CursorMan.getImages(CursorMan.CURSOR);
	cursors=new Cursor[cursorImages.length+1];
	Toolkit tk = Toolkit.getDefaultToolkit();
	for(int i=1;i<cursors.length-1;i++){
	    cursors[i]=tk.createCustomCursor(cursorImages[i-1],new Point(5,23),"cursor"+i);

	}
	int last=cursors.length-1;
	cursors[last]=tk.createCustomCursor(cursorImages[last-1],new Point(12,12),"cursor"+last);
    }

}



