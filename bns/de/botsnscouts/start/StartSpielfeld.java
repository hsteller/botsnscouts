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

public class StartSpielfeld extends JPanel{
    Paint paint;
    Start parent;
    GameFieldButs buttons;

    GameFieldPanel spf;
    JPanel pnl;
    JScrollPane scrl;

    JPanel okPanel;
    JButton okBut;
    JButton backBut;

    public StartSpielfeld(Start par){
	parent=par;
	parent.setTitle(Message.say("Start","mSpielStarten"));
	paint=parent.paint;

	buttons=new GameFieldButs(par);
	okPanel=getOkPanel();
	spf=new GameFieldPanel(par);
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
 	add(BorderLayout.EAST,buttons);
   	spf.rasterChanged();
    }
    public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	Dimension d = getSize();
	g2d.setPaint( paint );
	g2d.fillRect(0,0, d.width, d.height);
	paintChildren(g);
    }

    JPanel getOkPanel(){
	JPanel panel=new JPanel();

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

    private void okClicked(){
	try{
	    if(parent.startPanel==null){
		parent.startPanel=new StartPanel(parent);
	    }
	    parent.fassade.startGame();//starte Spiel
	    parent.addServer();
	    if(parent.startSpielfeld.buttons.mitspielen.getSelectedObjects()!=null){//starte einen SpielerMensch
		Thread smth=parent.fassade.amSpielTeilnehmenNoSplash(parent.startSpielfeld.buttons.nam.getText(),parent.startSpielfeld.buttons.farben.getSelectedIndex()); 
		parent.addKS(smth);
		Global.debug(this,"menschlichen spieler gestartet");
	    }else{//starte einen AusgabeFrame
		parent.addKS( parent.fassade.einemSpielZuschauenNoSplash());
	    }
	    parent.current=parent.startPanel;
	    parent.setContentPane(parent.current);
	    parent.show();
	}catch(OneFlagException ex){
	    JOptionPane.showMessageDialog(this,Message.say("Start","mZweiFlaggen"),Message.say("Start","mError"),JOptionPane.ERROR_MESSAGE);

	}catch(NichtZusSpfException exc){
	    JOptionPane.showMessageDialog(this,Message.say("Start","mNichtZus"),Message.say("Start","mError"),JOptionPane.ERROR_MESSAGE);

	}
	
    }//okclicked

    void unrollOverButs(){
	okBut.getModel().setRollover(false);
	backBut.getModel().setRollover(false);
	buttons.unrollOverButs();
    }

}//class StartSpielfeld end

class GameFieldPanel extends JPanel{
    Start parent;
    StartTileComp[][] kachP;
    Facade fassade;

    GameFieldPanel(Start par){
	parent=par;
	fassade=par.fassade;
	Ort spfDim=parent.fassade.getSpielfeldDim();
	GridLayout lay=new GridLayout(spfDim.y,spfDim.x);
	lay.setHgap(0);
	lay.setVgap(0);
	setLayout(lay);
	setOpaque(false);

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


class GameFieldButs extends JPanel implements  ActionListener, ChangeListener, ItemListener{

    Start parent;

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


    GameFieldButs(Start par){
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

	String[] far={Message.say("Start","mFarbeEgal"),Message.say("Start","mFarbeGruen"),Message.say("Start","mFarbeGelb"),Message.say("Start","mFarbeRot"),Message.say("Start","mFarbeBlau"),Message.say("Start","mFarbeMagenta"),Message.say("Start","mFarbeOrange"),Message.say("Start","mFarbeGrau"),Message.say("Start","mFarbeDunkelMagenta")};
	Font font=new Font("Sans", Font.BOLD, 12);

	spielfeld=new JLabel(Message.say("Start","mSpielfeld"));
	String[] spielfeldAr=loadSpielfelder();
	String defSpf=null;
	for (int i=0;i<spielfeldAr.length;i++){
	    if (spielfeldAr[i].equals("default")){
		defSpf=spielfeldAr[i];
	    }
	}
	spielfelder=new JComboBox(spielfeldAr);
	save=new TransparentButton(Message.say("Start","bSave"));

	spielfeld.setVisible(false);
	spielfelder.setVisible(true);
	save.setVisible(true);
	save.setActionCommand("save");
	save.addActionListener(this);

	edit=new TransparentButton(Message.say("Start","mBearbeiten"));
	name=new JLabel(Message.say("Start","mName"));
	nam=new JTextField(KrimsKrams.randomName());
	farbe=new JLabel(Message.say("Start","mFarbe"));
	farben=new RoboBox(true); //new JComboBox(far);
	mitspielen=new JCheckBox(Message.say("Start","mTeilnehmenBox"),true);

	spielfeld.setFont(font);
	spielfelder.setFont(font);
	save.setFont(font);
	edit.setFont(font);
	name.setFont(font);
	nam.setFont(font);
	farbe.setFont(font);

	mitspielen.setFont(font);

	save.setEnabled(true);
	spielfelder.setEnabled(true);

	spielfelder.setOpaque(false);

	mitspielen.setOpaque(false);
	mitspielen.addChangeListener(this);

	edit.setActionCommand("edit");
	edit.addActionListener(this);

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

	add(inner);
    	//lade das erste gefundene Spielfeld Configuration
	Properties spfProp=null;
	if (defSpf==null){
	    spfProp=loader.getProperties(spielfeldAr[0]);
	}else{
	    spfProp=loader.getProperties(defSpf);
	    spielfelder.setSelectedItem(defSpf);
	}
	parent.fassade.loadSpfProp(spfProp);
	spielfelder.addItemListener(this);
    }

    public void actionPerformed(ActionEvent e){
	if(e.getActionCommand().equals("edit")){//end else if "ok"
	    if(parent.startSpielfeldEditor==null){
		parent.startSpielfeldEditor=new StartSpielfeldEditor(parent,parent.startSpielfeld.spf);
	    }else{
		parent.startSpielfeldEditor.spf.addTileClickListener(parent.startSpielfeldEditor);
		parent.startSpielfeldEditor.fuerSpf.add(parent.startSpielfeldEditor.spf);
	    }
	    parent.fassade.saveTileRaster();
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
		loader.saveSpielfeld(spfProp,file);
		spielfelder.setModel(new DefaultComboBoxModel(loadSpielfelder()));
	    }
	}
    }
    
    void unrollOverButs(){
	save.getModel().setRollover(false);
	edit.getModel().setRollover(false);
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
	Properties spfProp=loader.getProperties(spfConf);
	parent.fassade.loadSpfProp(spfProp);
	parent.startSpielfeld.spf.rasterChanged();
    }

    private String[] loadSpielfelder(){
	return loader.getSpielfelder();
    }
}

class GameFieldLoader{

    public String[] getSpielfelder(){
 	File kd=new File("kacheln");
	String[] all = kd.list(new SpfFilter());
	for (int i=0;i<all.length;i++){
	    all[i]=all[i].substring(0,all[i].length()-4);
	}
	Arrays.sort(all);
	return all;
    }

    public Properties getProperties(String name){
	//InputStream istream=null;
	//istream=getClass().getResourceAsStream("kacheln/"+name+".spf");
	Properties spfProp=new Properties();
	try{
	    FileInputStream istream=new FileInputStream("kacheln/"+name+".spf");
	    spfProp.load(istream);
	}catch(FileNotFoundException e){
	    //	    System.err.println(e);
	    return null;
	}catch(IOException e){
	    System.err.println(e);
	}
	return spfProp;
    }

    public void saveSpielfeld(Properties spfProp, File file){
	try{
	    //	 URL url=getClass().getResource("kacheln");
	    //File to=new File(name);
	 OutputStream ostream=new FileOutputStream(file);
	 spfProp.store(ostream,null);
	}catch(IOException e){
	    System.err.println(e);
	}

    }

}

class SpfFilter implements FilenameFilter{
    public SpfFilter(){}
    public boolean accept(File dir, String name){
	try{
	    // endsWith(".spf") ???
	    return name.toLowerCase().endsWith(".spf");
	} catch(Throwable t){return false;}
    }
}
