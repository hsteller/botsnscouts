package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;
import java.net.*;
import java.util.*;
import java.io.*;

public class StartSpielfeldBut extends JPanel implements  ActionListener, ChangeListener, ItemListener{

    Start parent;

    JLabel spielfeld;
    JComboBox spielfelder;
    JButton save; 
    JButton bearbeiten;
    JLabel name;
    JTextField nam;
    JLabel farbe;
    JComboBox farben;
    JCheckBox mitspielen;

    StartSpielfeldLoadHelfer helfer=new StartSpielfeldLoadHelfer();
    JFileChooser chooser;
    void makeChooser() {
	    chooser = new JFileChooser("kacheln");  
	    FileFilter filter = new FileFilter() {
		    public boolean accept(File file) {
			String name = file.getName();
			return file.isDirectory() || name.endsWith(".spf");
		    }
		    
		    public String getDescription() {
			return "spf";
		    }
		}; 

	    chooser.setFileFilter(filter); 
    }


    public StartSpielfeldBut(Start par){
	parent=par;
	JPanel inner = new JPanel(); 
	GridBagLayout lay=new GridBagLayout();
	GridBagConstraints gc = new GridBagConstraints();
	gc.fill = GridBagConstraints.HORIZONTAL;
	gc.insets = new Insets(0,0,20,0);
	gc.gridx = 0;
	gc.gridy = GridBagConstraints.RELATIVE;
	setBorder(new EmptyBorder(20,10,10,10));
	setOpaque( false );
	
	inner.setLayout(lay);
	inner.setOpaque(false);

	//erzeuge Chooser
	makeChooser();


//     spielfeld
//     spielfelder
//     laden
//     bearbeiten
//     name
//     nam
//     farbe
//     farben
//     mitspielen
	String[] far={Message.say("Start","mFarbeEgal"),Message.say("Start","mFarbeGruen"),Message.say("Start","mFarbeGelb"),Message.say("Start","mFarbeRot"),Message.say("Start","mFarbeBlau"),Message.say("Start","mFarbeMagenta"),Message.say("Start","mFarbeOrange"),Message.say("Start","mFarbeGrau"),Message.say("Start","mFarbeDunkelMagenta")};
	Font font=new Font("Sans", Font.BOLD, 12);

	spielfeld=new JLabel(Message.say("Start","mSpielfeld"));
	spielfelder=new JComboBox(loadSpielfelder());
	spielfelder.addItemListener(this);
	save=new TransparentButton(Message.say("Start","bSave"));

	spielfeld.setVisible(false);
	spielfelder.setVisible(true);
	save.setVisible(true);
	save.setActionCommand("save");
	save.addActionListener(this);

	bearbeiten=new TransparentButton(Message.say("Start","mBearbeiten"));
	name=new JLabel(Message.say("Start","mName"));
	nam=new JTextField(KrimsKrams.randomName());
	farbe=new JLabel(Message.say("Start","mFarbe"));
	farben=new RoboBox(true); //new JComboBox(far);
	mitspielen=new JCheckBox(Message.say("Start","mTeilnehmenBox"),true);

	spielfeld.setFont(font);
	spielfelder.setFont(font);
	save.setFont(font);
	bearbeiten.setFont(font);
	name.setFont(font);
	nam.setFont(font);
	farbe.setFont(font);
	//farben.setFont(font);
	mitspielen.setFont(font);

	save.setEnabled(true);
	spielfelder.setEnabled(true);

	//spielfeld.setForeground(Color.lightGray);
	spielfelder.setOpaque(false);
	//name.setForeground(Color.lightGray);
	//farbe.setForeground(Color.lightGray);
	////farben.setOpaque(false);
	//mitspielen.setForeground(Color.lightGray);
	mitspielen.setOpaque(false);
	mitspielen.addChangeListener(this);

	bearbeiten.setActionCommand("bearbeiten");
	bearbeiten.addActionListener(this);
	//farben.setRenderer(new MyCellRenderer());
	nam.setEditable(true);
	nam.setEnabled(true);
	nam.setOpaque(false);

	inner.add(spielfeld,gc);
	inner.add(spielfelder,gc);
	inner.add(save,gc);
	inner.add(bearbeiten,gc);
	inner.add(name,gc);
	inner.add(nam,gc);
	inner.add(farbe,gc);
	gc.fill = GridBagConstraints.NONE;
	inner.add(farben,gc);
	gc.fill = GridBagConstraints.HORIZONTAL;
	inner.add(mitspielen,gc);
	add(inner);
    }

    public void actionPerformed(ActionEvent e){
	if(e.getActionCommand().equals("bearbeiten")){//end else if "ok"
	    if(parent.startSpielfeldEditor==null){
		parent.startSpielfeldEditor=new StartSpielfeldEditor(parent,parent.startSpielfeld.spf);
	    }else{
		parent.startSpielfeldEditor.spf.addKachelClickListener(parent.startSpielfeldEditor);
		parent.startSpielfeldEditor.fuerSpf.add(parent.startSpielfeldEditor.spf);
	    }
	    parent.fassade.saveKachelRaster();
	    parent.current=parent.startSpielfeldEditor;
	    parent.setContentPane(parent.current);
	    parent.show();
	    
	}else if (e.getActionCommand().equals("save")){
	    Properties spfProp=parent.fassade.getSpfProp();
	    
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
		    //Global.debug(this,"File "+file);
		}else{
		    focus=filename.substring(0,filename.length()-4);
		}
		//falls datei existiert
		if(file.exists()) {
		    Object[] options = { Message.say("Start","mOK"),Message.say("Start","mAbbr") };
		    String msg  = Message.say("KachelEditor", "mDatEx", filename );
		    String warn = Message.say("KachelEditor", "mWarnung");
		    int r = JOptionPane.showOptionDialog(null, msg, warn, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
		    if( r != 0 ) {
			return;
		    }
		}
		helfer.saveSpielfeld(spfProp,file);
		spielfelder.setModel(new DefaultComboBoxModel(loadSpielfelder()));
	    }
	}
    }
    
    
    
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
    }

    //Invoked when an item has been selected or deselected.
    public void itemStateChanged(ItemEvent e) {
	String spfConf=(String)spielfelder.getSelectedItem();
	//Global.debug(this,spfConf);
	Properties spfProp=helfer.getProperties(spfConf);
	parent.fassade.loadSpfProp(spfProp);
	parent.startSpielfeld.spf.rasterChanged();
    }

    private String[] loadSpielfelder(){
	return helfer.getSpielfelder();
    }

}

