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
import de.botsnscouts.gui.ColoredComponent;
import de.botsnscouts.gui.ColoredPanel;

public class GameFieldPanel extends JPanel{
    Paint paint;
    Start parent;

    FieldGrid spf;
    JPanel pnl;
    JScrollPane scrl;

    JComponent okPanel;
    JButton okBut;
    JButton backBut;

    JComponent editPanel;
    JLabel spielfeld;
    JComboBox spielfelder;
    JButton save;
    JButton edit;
    JLabel name;
    JTextField nam;
    JLabel farbe;
    JComboBox farben;
    JCheckBox mitspielen;
    GameFieldLoader loader=new GameFieldLoader();
    JFileChooser chooser;

    public GameFieldPanel(Start par){
	parent=par;
	parent.setTitle(Message.say("Start","mSpielStarten"));
	paint=parent.paint;

	editPanel=getEditPanel();
	okPanel=getOkPanel();
	spf=new FieldGrid(par);
	BorderLayout lay=new BorderLayout();

	setLayout(lay);
	requestFocus();

	scrl=new JScrollPane();

	pnl =new JPanel();
	pnl.setLayout(new FlowLayout());
	pnl.setOpaque(false);
	pnl.setBorder(new EmptyBorder(50,50,50,50));
	pnl.add(spf);

	scrl.setOpaque(false);
	scrl.getViewport().setView(pnl);

 	add(BorderLayout.SOUTH,okPanel);
 	add(BorderLayout.CENTER,scrl);
 	add(BorderLayout.EAST,editPanel);
   	spf.rasterChanged();
    }
    public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	Dimension d = getSize();
	g2d.setPaint( paint );
	g2d.fillRect(0,0, d.width, d.height);
	paintChildren(g);
    }

    private JComponent getOkPanel(){
	JComponent panel=new ColoredComponent();

	GridLayout lay=new GridLayout(1,2);
	lay.setHgap(50);
	lay.setVgap(50);
	panel.setBorder(new EmptyBorder(20,20,20,20));
	panel.setLayout(lay);
	panel.setOpaque(false);

	okBut=new TransparentButton(Message.say("Start","mSpielStarten"));
	backBut=new TransparentButton(Message.say("Start","mZurueckButton"));

	okBut.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    okClicked();
		}});
	backBut.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    parent.toMainMenu();
		    parent.show();
		}});

	panel.add(okBut);
	panel.add(backBut);
	return panel;
    }

    private JComponent getEditPanel(){
      JComponent panel=new ColoredPanel();

	JComponent inner = new JPanel();
	GridBagLayout lay=new GridBagLayout();
	GridBagConstraints gc = new GridBagConstraints();
	gc.fill = GridBagConstraints.HORIZONTAL;
	gc.insets = new Insets(0,0,20,0);
	gc.gridx = 0;
	gc.gridy = GridBagConstraints.RELATIVE;
	panel.setBorder(new EmptyBorder(20,10,10,10));
	panel.setOpaque( false );

	inner.setLayout(lay);
	inner.setOpaque(false);

	String[] far={Message.say("Start","mFarbeEgal"),Message.say("Start","mFarbeGruen"),Message.say("Start","mFarbeGelb"),Message.say("Start","mFarbeRot"),Message.say("Start","mFarbeBlau"),Message.say("Start","mFarbeMagenta"),Message.say("Start","mFarbeOrange"),Message.say("Start","mFarbeGrau"),Message.say("Start","mFarbeDunkelMagenta")};
	Font font=new Font("Sans", Font.BOLD, 12);

	spielfeld=new JLabel(Message.say("Start","mSpielfeld"));
	String[] spielfeldAr=loader.getSpielfelder();
	String defSpf=null;
	for (int i=0;i<spielfeldAr.length;i++){
	    if (spielfeldAr[i].equals("default")){
		defSpf=spielfeldAr[i];
	    }
	}
	spielfelder=new JComboBox(spielfeldAr);
	spielfeld.setVisible(false);
	spielfelder.setVisible(true);

	save=new TransparentButton(Message.say("Start","bSave"));
	save.setVisible(true);
	save.setEnabled(true);
	save.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    Properties spfProp=parent.fassade.getSpfProp();
		    makeChooser();
		    chooser.rescanCurrentDirectory();
		    int returnVal = chooser.showSaveDialog(parent);
		    if(returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			String filename = file.getName();
			String focus="";
			if(file.getName().equals("")) return;
			if (!filename.endsWith(".spf")){
			    file=new File(file.getParent(),filename+".spf");
			    focus=filename;
			}else{
			    focus=filename.substring(0,filename.length()-4);
			}
			//falls datei existiert
			if(file.exists()) {
			    Object[] options = { Message.say("Start","mOK"),Message.say("Start","mAbbr") };
			    String msg  = Message.say("BoardEditor", "mDatEx", filename );
			    String warn = Message.say("BoardEditor", "mWarnung");
			    int r = JOptionPane.showOptionDialog(null, msg, warn, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
			    if( r != 0 ) {
				return;
			    }
			}
			loader.saveSpielfeld(spfProp,file);
			spielfelder.setModel(new DefaultComboBoxModel(loader.getSpielfelder()));
		    }
		}});

	edit=new TransparentButton(Message.say("Start","mBearbeiten"));
	edit.setVisible(true);
	edit.setEnabled(true);
	edit.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    new Thread(new Runnable(){
			    public void run(){
				parent.showBusy(Message.say("Start","mLoadFieldEditor"));			       

				if(parent.fieldEditor==null){
				    parent.fieldEditor=new FieldEditor(parent,spf);
				}else{
				    parent.fieldEditor.spf.addTileClickListener(parent.fieldEditor);
				    parent.fieldEditor.fuerSpf.add(parent.fieldEditor.spf);
				}
				parent.fassade.saveTileRaster();
				parent.current=parent.fieldEditor;
				parent.setContentPane(parent.current);
				parent.stopBusy();
				parent.show();
			    }}).start();
		    edit.getModel().setRollover(false);
		}});

	name=new JLabel(Message.say("Start","mName"));

	nam=new JTextField(Conf.getDefaultRobName());
	farbe=new JLabel(Message.say("Start","mFarbe"));
	farben=new RoboBox(true);
	mitspielen=new JCheckBox(Message.say("Start","mTeilnehmenBox"),true);

	spielfeld.setFont(font);
	spielfelder.setFont(font);
	save.setFont(font);
	edit.setFont(font);
	name.setFont(font);
	nam.setFont(font);
	farbe.setFont(font);
	mitspielen.setFont(font);

	spielfelder.setEnabled(true);
	spielfelder.setOpaque(false);
	mitspielen.setOpaque(false);

	mitspielen.addChangeListener(new ChangeListener() {
		//changeListener Methode
		//Invoked when the target of the listener has changed its state.
		public void stateChanged(ChangeEvent e) {
		    if (!mitspielen.isSelected()){
			nam.setEnabled(false);
			farben.setEnabled(false);
		    }else{
			nam.setEnabled(true);
			farben.setEnabled(true);
		    }
		}});

	nam.setEditable(true);
	nam.setEnabled(true);
	nam.setOpaque(false);

	inner.add(spielfeld,gc);
	inner.add(spielfelder,gc);
	inner.add(save,gc);
	inner.add(edit,gc);
	inner.add(mitspielen,gc);
	inner.add(name,gc);
	inner.add(nam,gc);
	inner.add(farbe,gc);
	gc.fill = GridBagConstraints.NONE;
	inner.add(farben,gc);
	gc.fill = GridBagConstraints.HORIZONTAL;

	panel.add(inner);
    	//lade das erste gefundene Spielfeld ConfigurationtoLowerCase().
	Properties spfProp=null;
	if (defSpf==null){
	    spfProp=loader.getProperties(spielfeldAr[0]);
	}else{
	    spfProp=loader.getProperties(defSpf);
	    spielfelder.setSelectedItem(defSpf);
	}
	parent.fassade.loadSpfProp(spfProp);
	spielfelder.addItemListener(new ItemListener () {
		//Invoked when an item has been selected or deselected.
		public void itemStateChanged(ItemEvent e) {
		    String spfConf=(String)spielfelder.getSelectedItem();
		    Properties prop=loader.getProperties(spfConf);
		    parent.fassade.loadSpfProp(prop);
		    spf.rasterChanged();
		}});

	return panel;
    }

    private void makeChooser() {
	// Initialize only if it doesn't exist yet
	if (chooser != null)
	    return;

	chooser = new JFileChooser("tiles");
	FileFilter filter = new FileFilter() {
		public boolean accept(File file) {
		    String name = file.getName();
		    return file.isDirectory() || name.toLowerCase().endsWith(".spf");
		}
		public String getDescription() {
		    return "spf";
		}
	    };
	chooser.setFileFilter(filter);
    }

    private void okClicked(){
	new Thread(new Runnable(){
		public void run(){
		    parent.showBusy(Message.say("Start","mStartingServer"));
		    
		    try{
			if(parent.startPanel==null){
			    parent.startPanel=new StartPanel(parent);
			}
			parent.fassade.startGame();//starte Spiel
			parent.addServer();
			if( mitspielen.getSelectedObjects()!=null){
			    Thread smth=parent.fassade.amSpielTeilnehmenNoSplash(nam.getText(),farben.getSelectedIndex());
			    parent.addKS(smth);
			    Global.debug(this,"menschlichen spieler gestartet");
			}else{//starte einen AusgabeFrame
			    parent.addKS( parent.fassade.einemSpielZuschauenNoSplash());
			}
			parent.current=parent.startPanel;
			parent.setContentPane(parent.current);
			parent.show();
			parent.stopBusy();
		    }catch(OneFlagException ex){
			JOptionPane.showMessageDialog(GameFieldPanel.this,Message.say("Start","mZweiFlaggen"),Message.say("Start","mError"),JOptionPane.ERROR_MESSAGE);
			
		    }catch(NichtZusSpfException exc){
			JOptionPane.showMessageDialog(GameFieldPanel.this,Message.say("Start","mNichtZus"),Message.say("Start","mError"),JOptionPane.ERROR_MESSAGE);
			
		    }
		}}).start();
    }//okclicked

    void unrollOverButs(){
	okBut.getModel().setRollover(false);
	backBut.getModel().setRollover(false);
	save.getModel().setRollover(false);
	edit.getModel().setRollover(false);
    }

}//class GameFieldPanel end



