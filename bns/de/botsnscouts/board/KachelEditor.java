package de.botsnscouts.board;

import java.awt.*; 
import java.awt.image.*; 
import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.metal.*;
import de.botsnscouts.util.*;
import de.botsnscouts.gui.*;
import de.botsnscouts.board.*;

public class KachelEditor extends JFrame implements WindowListener, ActionListener,MouseListener{
    // enno:
    // 1. ImageMan wird verwendet (damit die Bilder nicht einmal von
    //     KachelEditor und einmal vom LeftPanel/SACanvas geladen werden
    // 2. KachelEditor reagiert nur noch auf die linke Maustaste (die anderen 
    //    sind irgendwann mal fürs Scrollen zuständig - s. SACanvas)
    //    das Scrollen funkt. im Editor und der Vorschau bereits, im Spiel noch nicht ..
    //    warum, weiss ich noch nicht

protected LeftPanel sac = null;
protected DPanel dp=null;
protected JScrollPane sp=null;
protected JScrollPane sp2=null;
protected Image[] cbeltCrop,ebeltCrop,diverseCrop;
protected Image[] images;
protected JPanel but;
protected JToggleButton[] buttons;
protected ButtonGroup felder;
protected SpielfeldSim spf=null;

protected int elemX=0,elemY=0,indx=0;
protected int spfX=0,spfY=0;
protected int phasen=0;
protected String spfString=null;
protected int laserSt=1;
    protected int[] elemTyp={122,123,131,132,102,121,120,130,133,103,122,123,131,132,100,120,121,133,130,101,152,153,150,151,222,223,231,232,202,221,220,230,233,203,222,223,231,232,200,221,220,230,233,201,252,253,250,251,0,10,10,-1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    protected int[] elemSpez={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,2,2,2,2,2,2,1,0,1,0,0,0,0,0,0,0,0,0,0};

private int x=800,y=600,x1=500,y1=100;

    public KachelEditor(){
	
	loadImg();
	initSpF();
	BorderLayout lay=new BorderLayout();
	getContentPane().setLayout(lay);
       
	setTitle(Message.say("KachelEditor","mTitel"));

	Toolkit tk=Toolkit.getDefaultToolkit();
	Dimension dim=tk.getScreenSize();

	sp=new JScrollPane();
	sp.getViewport().setView(sac);
 	getContentPane().add(BorderLayout.CENTER,sp);

	felder=new ButtonGroup();
	but=new JPanel();

	buttons=new JToggleButton[66];

	if(dim.width>=768){
	    but.setLayout(new GridLayout((buttons.length+2)/3,3));//3 spalten
	}else{
	    but.setLayout(new GridLayout((buttons.length+1)/2,2));//2 spalten
	}

	for(int i=0;i<buttons.length;i++){
	    buttons[i]=new JToggleButton(new ImageIcon(images[i]));
	    buttons[i].addActionListener(this);
	    buttons[i].setActionCommand(""+i);
	    but.add(buttons[i]);
	    felder.add(buttons[i]);
	}

	sp2=new JScrollPane();
 	sp2.getViewport().setView(but);
 	getContentPane().add(BorderLayout.EAST,sp2);

	dp=new DPanel(this);
	getContentPane().add(BorderLayout.NORTH,dp);

	addMouseListener(this);
	addWindowListener(this);       

	setSize(dim.width,dim.height-70);
	setLocation(0,25);
	setVisible(true);

	show();

    }//ende konstruktor

    private boolean loadImg(){
	// enno: habe eingebaut, dass KachelEditor und SACanvas die gleichen
	// bilder benutzen und nicht jeder sie einzeln laedt.
	ebeltCrop   = ImageMan.waitForImages( ImageMan.EBELTS );
	cbeltCrop   = ImageMan.waitForImages( ImageMan.CBELTS );
	diverseCrop = ImageMan.waitForImages( ImageMan.DIVERSE );

	int j=0;
	int i=0;
	images=new Image[ebeltCrop.length+cbeltCrop.length+diverseCrop.length];

	for(i=0;i<ebeltCrop.length;i++,j++)
	    images[j]=cbeltCrop[i];
	for(i=0;i<cbeltCrop.length;i++,j++)
	    images[j]=ebeltCrop[i];
	for(i=0;i<diverseCrop.length;i++,j++)
	    images[j]=diverseCrop[i];

	setIconImage(cbeltCrop[19]);

	Global.debug(this,"Bilder geladen");
	
	return true;
    }//ende loadImg

    protected boolean initSpF(){
	String gr1="____________\n";
	String gr2="_B_B_B_B_B_B_B_B_B_B_B_B_\n";
	try{
	    spf=new SpielfeldSim(12,12,new String(gr1+gr2+gr1+gr2+gr1+gr2+gr1+gr2+gr1+gr2+gr1+gr2+gr1+gr2+gr1+gr2+gr1+gr2+gr1+gr2+gr1+gr2+gr1+gr2+gr1),null);
	}
	catch(FormatException e){
	    System.err.println("Oups!"+e);
	    return false;
	}
	catch(FlaggenException ex){
	    System.err.println("Oups!"+ex);
	    return false;
	}
	sac=new LeftPanel(spf,this);
	return true;
    }

    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseClicked(MouseEvent e){}

    public void actionPerformed(ActionEvent e){
	String a=e.getActionCommand();
	try{
	    indx=Integer.parseInt(a);
	    //Global.debug(this,"button #"+indx+" clicked");
	}catch(NumberFormatException d){
	    //hier muß nichts stehen!!!
	}
    }

    public void windowDeactivated(WindowEvent e) {}
    public void windowOpened(WindowEvent e)      {}
    public void windowClosing(WindowEvent e) //{}
    {
	this.dispose();
	System.exit(0);
    }
    public void windowClosed(WindowEvent e)      {}
    public void windowIconified(WindowEvent e)   {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e)   {}
    
    public static void main(String[] argv){
	String lang="english";
	if (argv.length>=1){
	    if (argv[0].equals("english")||argv[0].equals("deutsch")){
		lang=argv[0];
	    }
	}
	Message.setLanguage(lang);
	Global.verbose=false;
	MetalLookAndFeel.setCurrentTheme( new GreenTheme() );
	new KachelEditor();
    }

}//end class KachelEditor



class RightPanel extends Canvas implements MouseListener{

    private Font fnt=new Font("SansSerif",Font.PLAIN,10);
    protected KachelEditor par=null;
    protected Image[] allImg=new Image[72];

    public RightPanel(KachelEditor p){
	par=p;
	int k=0;
	for(int i=0;i<24;i++)
	    allImg[k++]=par.cbeltCrop[i];
	for(int i=0;i<24;i++)
	    allImg[k++]=par.ebeltCrop[i];
	for(int i=0;i<24;i++)
	    allImg[k++]=par.diverseCrop[i];
	addMouseListener(this);
    }

    public void paint(Graphics g){
	int k=0;
	for(int j=0;j<17;j++)
	    for(int i=0;i<4;i++)
		if(k<66)
		    g.drawImage(allImg[k++],i*64,j*64,this);
	g.setColor(Color.black);
	for(int i=0;i<=4;i++) 
	    g.drawLine(i*64,0,i*64,17*64);
	for(int i=0;i<=17;i++) 
	    g.drawLine(0,i*64,4*64,i*64);

	g.setColor(Color.green);
	g.drawRect(par.elemX*64,par.elemY*64,64,64);
      
	g.setFont(fnt);
	g.setColor(Color.black);
	g.drawString("Element",3*64+15,14*64+25);
	g.drawString("löschen",3*64+15,14*64+45);
		

    }
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseClicked(MouseEvent e){
	par.elemX=e.getX()/64;
	par.elemY=e.getY()/64;
	par.indx=par.elemY*4+par.elemX;
	repaint();
    }

}//ende RightPanel


class LeftPanel extends SACanvas implements MouseListener{

    protected KachelEditor par=null;

    public LeftPanel(SpielfeldSim ss,KachelEditor p){
	super(ss);
	par=p;
	addMouseListener(this);
    }

    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseClicked(MouseEvent e){
	// Enno: nur linke maustaste bearbeiten  
	int mods = e.getModifiers();
	if( (mods & MouseEvent.BUTTON1_MASK) == 0 ) 
	    return;
	
	par.spfX=e.getX()/64;
	par.spfY=e.getY()/64;
	int vWInd=(e.getX()+32)/64;
	int hWInd=(e.getY()+32)/64;
	//System.out.println(par.spfX+","+par.spfY);
	if(par.indx==54){//pusher nach west
	    new PhaseDialog(par,Message.say("KachelEditor","mPusher"),true);
	    par.spf.vWand[par.spfX+1][11-par.spfY].da=true;
	    par.spf.vWand[par.spfX+1][11-par.spfY].wandEl[0]=2;
	    par.spf.vWand[par.spfX+1][11-par.spfY].spez[0]=par.phasen;
	}
	else if(par.indx==55){//pusher nach sued
	    new PhaseDialog(par,Message.say("KachelEditor","mPusher"),true);
	    par.spf.hWand[par.spfX][12-par.spfY].wandEl[1]=2;
	    par.spf.hWand[par.spfX][12-par.spfY].spez[1]=par.phasen;
	    par.spf.hWand[par.spfX][12-par.spfY].da=true;
	}
	else if(par.indx==56){//pusher nach nord
	    new PhaseDialog(par,Message.say("KachelEditor","mPusher"),true);
	    par.spf.hWand[par.spfX][11-par.spfY].da=true;
	    par.spf.hWand[par.spfX][11-par.spfY].wandEl[0]=2;
	    par.spf.hWand[par.spfX][11-par.spfY].spez[0]=par.phasen;
	}
	else if(par.indx==57){//pusher nach ost
	    new PhaseDialog(par,Message.say("KachelEditor","mPusher"),true);
	    par.spf.vWand[par.spfX][11-par.spfY].da=true;
	    par.spf.vWand[par.spfX][11-par.spfY].wandEl[1]=2;
	    par.spf.vWand[par.spfX][11-par.spfY].spez[1]=par.phasen;
	}
	else if(par.indx==58){//crusher
	    int tp=par.spf.boden[par.spfX+1][12-par.spfY].typ;
	    if((tp>=100)&&(tp<=253)){
		new PhaseDialog(par,Message.say("KachelEditor","mCrusher"),true);
		par.spf.boden[par.spfX+1][12-par.spfY].spez=par.phasen;
	    }
	}
	else if(par.indx==59){//leer
	    par.spf.boden[par.spfX+1][12-par.spfY].spez=0;
	    par.spf.boden[par.spfX+1][12-par.spfY].typ=0;
	    par.spf.vWand[par.spfX+1][11-par.spfY].da=false;
	    par.spf.vWand[par.spfX+1][11-par.spfY].wandEl[0]=0;
	    par.spf.vWand[par.spfX+1][11-par.spfY].spez[0]=0;
	    par.spf.vWand[par.spfX][11-par.spfY].da=false;
	    par.spf.vWand[par.spfX][11-par.spfY].wandEl[1]=0;
	    par.spf.vWand[par.spfX][11-par.spfY].spez[1]=0;
	    par.spf.hWand[par.spfX][12-par.spfY].wandEl[1]=0;
	    par.spf.hWand[par.spfX][12-par.spfY].spez[1]=0;
	    par.spf.hWand[par.spfX][12-par.spfY].da=false;
	    par.spf.hWand[par.spfX][11-par.spfY].da=false;
	    par.spf.hWand[par.spfX][11-par.spfY].wandEl[0]=0;
	    par.spf.hWand[par.spfX][11-par.spfY].spez[0]=0;
	}
	else if(par.indx==60){//vert wand
	    par.spf.vWand[vWInd][11-par.spfY].da=true;
	}
	else if(par.indx==61){//horiz. wand
	    par.spf.hWand[par.spfX][12-hWInd].da=true;
	}
	else if(par.indx==62){//laser nach west
	    new StaerkeDialog(par,Message.say("KachelEditor","mLaser"),true);
	    par.spf.vWand[par.spfX+1][11-par.spfY].da=true;
	    par.spf.vWand[par.spfX+1][11-par.spfY].wandEl[0]=1;
	    par.spf.vWand[par.spfX+1][11-par.spfY].spez[0]=par.laserSt;
	}
	else if(par.indx==63){//laser nach sued
	    new StaerkeDialog(par,Message.say("KachelEditor","mLaser"),true);
	    par.spf.hWand[par.spfX][12-par.spfY].wandEl[1]=1;
	    par.spf.hWand[par.spfX][12-par.spfY].spez[1]=par.laserSt;
	    par.spf.hWand[par.spfX][12-par.spfY].da=true;
	}
	else if(par.indx==64){//laser nach ost
	    new StaerkeDialog(par,Message.say("KachelEditor","mLaser"),true);
	    par.spf.vWand[par.spfX][11-par.spfY].da=true;
	    par.spf.vWand[par.spfX][11-par.spfY].wandEl[1]=1;
	    par.spf.vWand[par.spfX][11-par.spfY].spez[1]=par.laserSt;
	}
	else if(par.indx==65){//laser nach nord
	    new StaerkeDialog(par,Message.say("KachelEditor","mLaser"),true);
	    par.spf.hWand[par.spfX][11-par.spfY].da=true;
	    par.spf.hWand[par.spfX][11-par.spfY].wandEl[0]=1;
	    par.spf.hWand[par.spfX][11-par.spfY].spez[0]=par.laserSt;
	}
	else{ //fliessband etc.
	    par.spf.boden[par.spfX+1][12-par.spfY].typ =par.elemTyp[par.indx];
	    par.spf.boden[par.spfX+1][12-par.spfY].spez=par.elemSpez[par.indx];
	}
	repaint();
    }

}//ende LeftPanel

class DPanel extends JPanel implements MouseListener,ActionListener{
    private JButton feldSpeich=null;
    private JButton feldLaden=null;
    private JButton feldClear=null;
    private JButton exit=null;
    protected KachelEditor par=null;
    JFileChooser chooser;

    void makeChooser() {
	    chooser = new JFileChooser("kacheln");  
	    FileFilter filter = new FileFilter() {
		    public boolean accept(File file) {
			String name = file.getName();
			return file.isDirectory() || name.endsWith(".rra");
		    }
		    
		    public String getDescription() {
			return "rra";
		    }
		}; 

	    chooser.setFileFilter(filter); 
    }


    public DPanel(KachelEditor p){
	par=p;
	setLayout( new BorderLayout() );
	JToolBar tb = new JToolBar();
	//	setLayout(new GridLayout(1,3));

	feldLaden = new JButton(Message.say("KachelEditor","bLaden"));
	feldLaden.addActionListener(this);
	feldLaden.setActionCommand("Laden");
	tb.add(feldLaden);
	
	feldSpeich = new JButton(Message.say("KachelEditor","bSpeichern"));
	feldSpeich.addActionListener(this);
	feldSpeich.setActionCommand("Speichern");
	tb.add(feldSpeich);

	feldClear = new JButton(Message.say("KachelEditor","bClear"));
	feldClear.addActionListener(this);
	feldClear.setActionCommand("Clear");
	tb.add(feldClear);


	exit= new JButton(Message.say("KachelEditor","bBeenden"));
	exit.addActionListener(this);
	exit.setActionCommand("Beenden");
	tb.add(exit);
	add( tb, BorderLayout.NORTH );
	JMenuBar jb = new JMenuBar();
	JMenu jm = new JMenu("File");
	JMenuItem mi;
	mi = new JMenuItem(Message.say("KachelEditor","bLaden"));
	mi.addActionListener(this);
	mi.setActionCommand("Laden");
	jm.add( mi );
	mi = new JMenuItem(Message.say("KachelEditor","bSpeichern"));
	mi.addActionListener(this);
	mi.setActionCommand("Speichern");
	jm.add( mi );
	jm.addSeparator();
	mi = new JMenuItem(Message.say("KachelEditor","bBeenden"));
	mi.addActionListener(this);
	mi.setActionCommand("Beenden");
	jm.add( mi );
	jb.add( jm );
	par.setJMenuBar( jb );
	makeChooser();

    }
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseClicked(MouseEvent e){}

    public void actionPerformed(ActionEvent e){
	if(e.getActionCommand().compareTo("Speichern") == 0){
	    Global.debug(this,"Speichern geclicked, gete String");
	    String tmp=par.spf.getComputedString();
	    Global.debug(this, "String bekommen:\n"+tmp);
	    par.spfString=tmp;
	    Global.debug(this, "Starte FileDialog");

	    //	    new NameDialog(par,Message.say("KachelEditor","mKachelSave"),true);
	    chooser.rescanCurrentDirectory();
	    int returnVal = chooser.showSaveDialog(par); 
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		File file = chooser.getSelectedFile();
		String filename = file.getName();
		if(file.getName().equals("")) return;
		if (!filename.endsWith(".rra")){
		    file=new File(file.getParent(),filename+".rra");
		    Global.debug(this,"File "+file);
		}
		if(file.exists()) {
		    Object[] options = { Message.say("Start","mOK"),
					 Message.say("Start","mAbbr") };

		    String msg  = Message.say("KachelEditor", "mDatEx", filename );
		    String warn = Message.say("KachelEditor", "mWarnung");
		    int r = JOptionPane.showOptionDialog(null, msg, warn, 
						 JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
						 null, options, options[0]);
		    if( r != 0 ) {
			return;
		    }
		}

		Global.debug(this,"Speichere "+ file + " " + file.getName()+" ("+"kacheln/"+file.getName()+")"); 
		//File to=new File("kacheln/"+name.getText()+".rra");
		Global.debug(this,"File opened"); 
		try{
		    FileOutputStream fop=new FileOutputStream(file);
		    Global.debug(this,"FileOutputStream opened"); 
		    PrintWriter pw=new PrintWriter(fop);
		    Global.debug(this,"PrintWriter opened"); 
		    pw.println(par.spfString);
		    Global.debug(this,"String in File written"); 
		    pw.close();
		    Global.debug(this,"File closed"); 
		}catch(IOException i){
		    System.err.println(Message.say("KachelEditor","mDateiErr") + file + i);
		}
	    }
	}
	else if(e.getActionCommand().compareTo("Beenden") == 0){
	    par.dispose();
	    System.exit(0);
	}
	else if(e.getActionCommand().compareTo("Clear") == 0){
	    Global.debug(this,"Clearing field");
	    par.initSpF();
	    par.sp.getViewport().setView(par.sac);	    
	}
	else if(e.getActionCommand().compareTo("Laden") == 0){
	    chooser.rescanCurrentDirectory();
	    int returnVal = chooser.showOpenDialog(par); 
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		File file = chooser.getSelectedFile();
		String name = file.getName();
		
		if( name.equals("") || !file.exists() || !file.canRead() || file.isDirectory() ) {
		    String fehler = Message.say("Start", "mError");
		    String msg    = Message.say("KachelEditor", "eDateiErr");
		    
		    JOptionPane.showMessageDialog(null, msg, fehler, JOptionPane.ERROR_MESSAGE); 
		    return;
		}

		String save=par.spfString;
		try{
		    FileInputStream istream=new FileInputStream( file );
		    //FileInputStream istream=new FileInputStream("kacheln" + File.separator + name);
		    BufferedReader kachReader =new BufferedReader(new InputStreamReader(istream));
		    StringBuffer str=new StringBuffer();
		    String tmp=null;
		    //und lese Spielfeld aus
		    while((tmp=kachReader.readLine())!=null)
			str.append(tmp+"\n");
		    //Global.debug(this,str.toString());
		    par.spfString=str.toString();
		    //Leo's Code
		    par.spf=new SpielfeldSim(12,12,par.spfString,null);
		    Global.debug(this, "Spielfeld erzeugt");

		    par.sp.remove(par.sac);
		    Global.debug(this, "sac removed");
		    par.sac=new LeftPanel(par.spf,par);
		    Global.debug(this, "sac neu erzeugt");
		    par.sp.getViewport().setView(par.sac);

		    Global.debug(this, "sac added");
		}catch(FormatException ex){
		    System.err.println(Message.say("KachelEditor","eDatNotEx")+ex);
		    par.spfString=save;
		}catch(FlaggenException ex){
		    System.err.println(Message.say("KachelEditor","eDatNotEx")+ex);
		    par.spfString=save;
		}catch(IOException ex){
		    System.err.println(Message.say("KachelEditor","eDateiErr")+ex);
		    par.spfString=save;
		}/*catch(Exception ex){
		   System.err.println(Message.say("KachelEditor","eDatNotEx")+ex);
		   }*/
	    } 

	    Global.debug(this,"Laden geclicked, starte LadenDialog");
	    //new LadenDialog(par,Message.say("KachelEditor","mKachelSave"),true);
	    
	}
    }

}//ende class DPanel

class PhaseDialog extends JDialog implements ActionListener{
    
    private Button ok=null;
    private Checkbox[] chkb=new Checkbox[5];

    private KachelEditor par=null;
    private int x=200,y=300;

    public PhaseDialog(KachelEditor pa,String tit,boolean mod){
	super(pa,tit,mod);
	par=pa;
	setSize(x,y);
	getContentPane().setLayout(new GridLayout(6,1,2,2));

	for(int i=0;i<5;i++){
	    chkb[i]=new Checkbox(Message.say("KachelEditor","mPhase")+" "+(i+1));
	    getContentPane().add(chkb[i]);
	}
	ok=new Button(Message.say("KachelEditor","bOk"));
	ok.addActionListener(this);
	ok.setActionCommand("OK");
	getContentPane().add(ok);
	show();
    }

 public void actionPerformed(ActionEvent e)  {
     if(e.getActionCommand().equals("OK")){
	 int mult=1;
	 par.phasen=0;
	 for(int i=0;i<5;i++){
	     if(chkb[i].getState())
		 par.phasen+=mult;
	     mult*=2;
	 }
	 if(par.phasen>0)
	     this.dispose();
     }
 }

}

class StaerkeDialog extends JDialog implements ActionListener{
    
    private Button ok=null;
    private Checkbox[] chkb=new Checkbox[3];
    private CheckboxGroup cgrp=null;

    private KachelEditor par=null;
    private int x=200,y=200;

    public StaerkeDialog(KachelEditor pa,String tit,boolean mod){
	super(pa,tit,mod);
	par=pa;
	setSize(x,y);
	getContentPane().setLayout(new GridLayout(4,1,2,2));

	cgrp=new CheckboxGroup();

	for(int i=0;i<3;i++){
	    chkb[i]=new Checkbox(Message.say("KachelEditor","mStaerke")+" "+(i+1),cgrp,false);
	    getContentPane().add(chkb[i]);
	}
	cgrp.setSelectedCheckbox(chkb[0]);
	ok=new Button(Message.say("KachelEditor","bOk"));
	ok.addActionListener(this);
	ok.setActionCommand("OK");
	getContentPane().add(ok);
	show();
    }

 public void actionPerformed(ActionEvent e)  {
     if(e.getActionCommand().equals("OK")){
	 int sel=1;
	 if(chkb[1]==cgrp.getSelectedCheckbox()) sel=2;
	 if(chkb[2]==cgrp.getSelectedCheckbox()) sel=3;
	 par.laserSt=sel;
	 this.dispose();
     }
 }

}

class NameDialog extends JDialog implements ActionListener,WindowListener{
    
    private Button ok=null;
    private TextField name=null;

    private KachelEditor par=null;
    private int x=200,y=100;

    public NameDialog(KachelEditor pa,String tit,boolean mod){
	super(pa,tit,mod);
	par=pa;
	setSize(x,y);
	getContentPane().setLayout(new GridLayout(2,1,2,2));

	name=new TextField(100);
	getContentPane().add(name);

	ok=new Button(Message.say("KachelEditor","bOk"));
	ok.addActionListener(this);
	ok.setActionCommand("OK");
	getContentPane().add(ok);

	addWindowListener(this);

	show();
    }

 public void actionPerformed(ActionEvent e)  {
     if(e.getActionCommand().equals("OK")){
	 if(name.getText().equals("")) return;
	 Global.debug(this,"Speichere "+name.getText()+" ("+"kacheln/"+name.getText()+".rra"+")"); 
	 File to=new File("kacheln/"+name.getText()+".rra");
	 Global.debug(this,"File opened"); 
	 try{
	     FileOutputStream fop=new FileOutputStream(to);
	     Global.debug(this,"FileOutputStream opened"); 
	     PrintWriter pw=new PrintWriter(fop);
	     Global.debug(this,"PrintWriter opened"); 
	     pw.println(par.spfString);
	     Global.debug(this,"String in Datei written"); 
	     pw.close();
	     Global.debug(this,"File closed"); 
	 }catch(IOException i){
	     System.err.println(Message.say("KachelEditor","mDateiErr",name.getText())+i);
	 }
	 this.dispose();
     }
 }
 public void windowDeactivated(WindowEvent e) {}
 public void windowOpened(WindowEvent e)      {}
 public void windowClosing(WindowEvent e) //{}
  {
  this.dispose();
  }
 public void windowClosed(WindowEvent e)      {}
 public void windowIconified(WindowEvent e)   {}
 public void windowDeiconified(WindowEvent e) {}
 public void windowActivated(WindowEvent e)   {}

}

class LadenDialog extends JDialog implements ActionListener,WindowListener{
    
    private Button ok=null;
    private TextField name=null;

    private KachelEditor par=null;
    private int x=200,y=100;

    public LadenDialog(KachelEditor pa,String tit,boolean mod){
	super(pa,tit,mod);
	par=pa;
	setSize(x,y);
	getContentPane().setLayout(new GridLayout(2,1,2,2));

	name=new TextField(100);
	getContentPane().add(name);

	ok=new Button(Message.say("KachelEditor","bOk"));
	ok.addActionListener(this);
	ok.setActionCommand("OK");
	getContentPane().add(ok);

	addWindowListener(this);
	
	show();
    }

 public void actionPerformed(ActionEvent e)  {
     if(e.getActionCommand().equals("OK")){
	 if(!name.getText().equals("")){
	     String save=par.spfString;
	     try{
		FileInputStream istream=new FileInputStream("kacheln/"+name.getText()+".rra");
		BufferedReader kachReader =new BufferedReader(new InputStreamReader(istream));
		StringBuffer str=new StringBuffer();
		String tmp=null;
		//und lese Spielfeld aus
		while((tmp=kachReader.readLine())!=null)
		    str.append(tmp+"\n");
		Global.debug(this,str.toString());
		par.spfString=str.toString();
		//Leo's Code
		par.spf=new SpielfeldSim(12,12,par.spfString,null);
		Global.debug(this, "Spielfeld erzeugt");

		par.sp.remove(par.sac);
		Global.debug(this, "sac removed");
		par.sac=new LeftPanel(par.spf,par);
		Global.debug(this, "sac neu erzeugt");
		par.sp.getViewport().setView(par.sac);
		par.sac.repaint();

		Global.debug(this, "sac added");
	     }catch(FormatException ex){
		 System.err.println(Message.say("KachelEditor","eDatNotEx")+ex);
		 par.spfString=save;
	     }catch(FlaggenException ex){
		 System.err.println(Message.say("KachelEditor","eDatNotEx")+ex);
		 par.spfString=save;
	     }catch(IOException ex){
		 System.err.println(Message.say("KachelEditor","eDateiErr")+ex);
		 par.spfString=save;
	     }/*catch(Exception ex){
		System.err.println(Message.say("KachelEditor","eDatNotEx")+ex);
		}*/
	     this.dispose();
	 }
     }
 }
    public void windowDeactivated(WindowEvent e) {}
    public void windowOpened(WindowEvent e)      {}
    public void windowClosing(WindowEvent e) //{}
    {
	this.dispose();
    }
    public void windowClosed(WindowEvent e)      {}
    public void windowIconified(WindowEvent e)   {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowActivated(WindowEvent e)   {}
    
}
