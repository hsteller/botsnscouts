package de.botsnscouts.old;

import java.awt.*; 
import java.awt.image.*; 
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.zip.*;
import de.botsnscouts.gui.*;
import de.botsnscouts.util.*;
import de.botsnscouts.board.*;
import de.botsnscouts.start.*;
import de.botsnscouts.autobot.*;
/**
* StartSpieler ermöglicht dem Benutzer einen neuen SpielServer auf dem remote Rechner,
* künstliche Spieler lokal oder remote, menschlichen Spieler oder Ausgabekanal aus einem Programm
* zu starten
* @author  Ludmila und Leo Scharf
* @see     StartServer
*/
public class StartSpieler extends Frame implements WindowListener,
                                      ActionListener, MouseListener, KeyListener{
 protected KommSpPr com;

 private Label caption = null;
 protected Label ipcap = null;
 protected Label portcap = null;
 protected Label statbar = null;
 private Button exit = null;
 private Button neuSp = null;
 private Button spAnm = null;
 private Button kSp = null;
 private Button ausg = null;
 protected TextField ip = null;
 protected TextField port = null;
 protected NeuSp ns = null;
 private SpAnm sa = null;
 private AfAnm as = null;
 private KSp ks = null;
 protected StSpHalloCanv hs =null;
/* protected int fltempX[]= new int[5];
 protected int fltempY[]= new int[5];        */
 protected Object[] meineThreads=new Object[32];
 protected int thrNum=0;


 protected int x1=10,x=800,y=600,y1=10,y2=575,y3=545,yb=50,yk=30, xloc=0, yloc=20, xvd=70, xv=x-xvd-x1,yv=30;

 protected String zuspath="de/spline/rr/";

 public StartSpieler() {
     this("127.0.0.1");
 }

 public StartSpieler(String serverip){
  super("Loading...");
  try{
      Message.setLanguage("deutsch");
  }catch(Exception e){
      System.err.println(e);
  }
  setTitle(Message.say("StartSpieler","mTitel"));
//  for(int i=0;i<5;i++) fltempX[i]=fltempY[i]=0;

  Toolkit tk = Toolkit.getDefaultToolkit();

  StSpParser.reset();

  Global.verbose=true;//!!!!!!!!!!!!

  zuspath=getpath();
  //  System.out.println("Zusatzpath "+zuspath)
;
  setSize(x,y);
  setLocation(xloc,yloc);
  setResizable(true);
  setLayout(null);
  setBackground(Color.lightGray);

  hs=new StSpHalloCanv(x-2*x1,y3-y1-yb-yk,zuspath);
  hs.setSize(x-2*x1,y3-y1-yb-yk);
  hs.setLocation(x1,y1+yb+yk);
  add(hs);
  hs.repaint();
  hs.setVisible(true);

  ns=new NeuSp(this);
  ns.setSize(x-2*x1,y3-y1-yb-yk);
  ns.setLocation(x1,y1+yb+yk);
  add(ns);
  ns.setVisible(false);
  ns.setBackground(Color.lightGray);

  sa=new SpAnm(this,Message.say("StartSpieler","mEinSpSt"),Message.say("StartSpieler","mSpName"),/*Message.say("StartSpieler","mName")*/KrimsKrams.randomName(),Message.say("StartSpieler","mSpErzUanm"));
  sa.setSize(x-2*x1,y3-y1-yb-yk);
  sa.setLocation(x1,y1+yb+yk);
  add(sa);
  sa.setVisible(false);
  sa.setBackground(Color.lightGray);

  //as=new SpAnm(this,"Einen Ausgabekanal starten","Kanalname" , "--Name eingeben--","Ausgabe erzeugen und anmelden");
  as=new AfAnm(this,Message.say("StartSpieler","mEinAKSt"),/*Message.say("StartSpieler","mKanName"),Message.say("StartSpieler","mName"),*/Message.say("StartSpieler","mAKErzUanm"));
  as.setSize(x-2*x1,y3-y1-yb-yk);
  as.setLocation(x1,y1+yb+yk);
  add(as);
  as.setVisible(false);
  as.setBackground(Color.lightGray);

  ks=new KSp(this);
  ks.setSize(x-2*x1,y3-y1-yb-yk);
  ks.setLocation(x1,y1+yb+yk);
  add(ks);
  ks.setVisible(false);
  ks.setBackground(Color.lightGray);

  caption=new Label(Message.say("StartSpieler","mRRMenu"),Label.CENTER);
  add(caption);
  caption.setSize(x-2*x1,yb);
  caption.setFont(new Font("SansSerif", Font.BOLD, 25));
  caption.setLocation(x1,y1);
  caption.addMouseListener(this);

  statbar=new Label(Message.say("StartSpieler","mStBarBereit"),Label.LEFT);
  add(statbar);
  statbar.setSize(x-2*x1,y-y2);
  statbar.setFont(new Font("SansSerif", Font.PLAIN, 15));
  statbar.setLocation(x1,y2);
  statbar.setForeground(new Color(4,64,4));

  ipcap=new Label(Message.say("StartSpieler","mIP"),Label.LEFT);
  add(ipcap);
  ipcap.setSize(180,y2-y3);
  ipcap.setFont(new Font("SansSerif", Font.PLAIN, 11));
  ipcap.setLocation(x1,y3);

  portcap=new Label(Message.say("StartSpieler","mPort"),Label.LEFT);
  add(portcap);
  portcap.setSize(90,y2-y3);
  portcap.setFont(new Font("SansSerif", Font.PLAIN, 11));
  portcap.setLocation(x1+350,y3);

  ip=new TextField(serverip,50);
  add(ip);
  ip.setSize(150,y2-y3);
  ip.setLocation(180+x1,y3);

  port=new TextField("8077",50);
  add(port);
  port.setSize(49,y2-y3);
  port.setLocation(450+x1,y3);
  
  exit=new Button(Message.say("StartSpieler","mExit"));
  exit.addActionListener(this);
  exit.addMouseListener(this);
  add(exit);
  exit.setSize(xvd,yk);
  exit.setLocation(x1+(x-2*x1-xvd),y1+yb);

  neuSp=new Button(Message.say("StartSpieler","mNeuSp"));
  neuSp.addActionListener(this);
  neuSp.addMouseListener(this);
  add(neuSp);
  neuSp.setSize((x-2*x1-xvd)/4,yk);
  neuSp.setLocation(x1,y1+yb);
  neuSp.setActionCommand("neu");

  spAnm=new Button(Message.say("StartSpieler","mTeilnehmen"));
  spAnm.addActionListener(this);
  spAnm.addMouseListener(this);
  add(spAnm);
  spAnm.setSize((x-2*x1-xvd)/4,yk);
  spAnm.setLocation(x1+(x-2*x1-xvd)/4,y1+yb);
  spAnm.setActionCommand("spielen");

  kSp=new Button(Message.say("StartSpieler","mKSAnm"));
  kSp.addActionListener(this);
  kSp.addMouseListener(this);
  add(kSp);
  kSp.setSize((x-2*x1-xvd)/4,yk);
  kSp.setLocation(x1+2*(x-2*x1-xvd)/4,y1+yb);
  kSp.setActionCommand("kspieler");

  ausg=new Button(Message.say("StartSpieler","mAusgSt"));
  ausg.addActionListener(this);
  ausg.addMouseListener(this);
  add(ausg);
  ausg.setSize((x-2*x1-xvd)/4,yk);
  ausg.setLocation(x1+3*(x-2*x1-xvd)/4,y1+yb);
  ausg.setActionCommand("ausg");


  addWindowListener(this);
  addKeyListener(this);
  getScout();
  show();
  repaint();

  com=new KommSpPr();

	  ipcap.setVisible(false);
	  portcap.setVisible(false);
	  ip.setVisible(false);
	  port.setVisible(false);
  

 }

    private void getScout(){
	URL url = getClass().getResource("images/cbelts.gif");
	Toolkit tk=Toolkit.getDefaultToolkit();
	Image cbelt = tk.getImage(url);

	MediaTracker mt = new MediaTracker(this);
	mt.addImage(cbelt,0);
	try{mt.waitForAll();}
	catch(InterruptedException ie){System.err.println(ie.getMessage());}

	ImageFilter crop;

	crop = new CropImageFilter((14%5)*64,(14/5)*64,64,64);
	Image cbeltCrop = createImage(new FilteredImageSource(cbelt.getSource(),crop));

	setIconImage(cbeltCrop);

    }

 public String getpath(){
     /*	String klassenpfad = System.getProperty("java.class.path");
	char sep = (System.getProperty("path.separator")).charAt(0);
	int last=0,next=0;
	next=klassenpfad.indexOf((int)sep,last);
	//System.out.println("CLASSPATH="+klassenpfad+" last "+last+" next "+next);
	while (next!=-1){
	    String nextpf=klassenpfad.substring(last,next);
	    //	System.out.println("nextpf "+nextpf);
	    File kd=new File(nextpf+File.separator+"kacheln");
	    String[] all = kd.list(new ThumbFilter());
	    if (all!=null) return (nextpf+File.separator);
	    last=next+1;
	    next=klassenpfad.indexOf((int)sep,last);
	    }*/
        return "de/spline/rr/";
 } 

 public void paint(Graphics g)
  {
   //g.drawImage(img, 250, 90, null);
  }

 public void actionPerformed(ActionEvent e)
  {
   if(e.getActionCommand().compareTo("Verlassen") == 0)
    {
    int k=0;
    if(thrNum>0){
	for(int i=thrNum-1;i>=0;i--)
	    if (!((Thread)meineThreads[i]).isAlive()){
		System.out.println("EINE IST TOD!"+i);
		meineThreads[i]=null;
		k++;
	    }
	for(int z=0;z<k;z++)
	    for (int i=0;i<thrNum;i++){
		if (meineThreads[i]==null){
		    for(int j=i;j<thrNum-1;j++){
			meineThreads[j]=meineThreads[j+1];
		    }
		    meineThreads[thrNum]=null;
		    i=thrNum;
		    thrNum--;
		}
		
     }	
    }
    if(thrNum==0){ System.out.println("Bye!");System.exit(0);}
    else { 
	System.out.println("Tschuess!");
	this.dispose(); 
	for(int i=0;i<thrNum;i++)
	synchronized(this){
	    try{
		((Thread)meineThreads[i]).join();
	    }catch(InterruptedException exc){
	    }
	}
	System.out.println("Bye!");
	System.exit(0);
    }

    } else
   if(e.getActionCommand().compareTo("neu")== 0)
    {
//     if(!ns.isVisible()){
      sa.setVisible(false);
      ks.setVisible(false);
      as.setVisible(false);
      hs.setVisible(false);
      ns.setVisible(true);
      //      StSpParser.reset();
      ns.reset();
      if (ns.warte){
	  ipcap.setVisible(false);
	  portcap.setVisible(false);
	  ip.setVisible(false);
	  port.setVisible(false);
      }else{
	  ipcap.setVisible(true);
	  portcap.setVisible(true);
	  ip.setVisible(true);
	  port.setVisible(true);
      }
//     }
    } else
   if(e.getActionCommand().compareTo("spielen")== 0)
    {
     //if(ns.isVisible()){
      ns.setVisible(false);
      ks.setVisible(false);
      hs.setVisible(false);
      as.setVisible(false);
      sa.setVisible(true);
      ipcap.setVisible(true);
      portcap.setVisible(true);
      ip.setVisible(true);
      port.setVisible(true);
     //}
    } else
   if(e.getActionCommand().compareTo("kspieler")== 0)
    {
//     if(ns.isVisible()){
      ns.setVisible(false);
      ks.setVisible(true); 
      hs.setVisible(false);
      sa.setVisible(false);
      as.setVisible(false);
      ipcap.setVisible(true);
      portcap.setVisible(true);
      ip.setVisible(true);
      port.setVisible(true);
//     }
    } else
   if(e.getActionCommand().compareTo("ausg")== 0)
    {
//     if(ns.isVisible()){
	hs.setVisible(false);
	ks.setVisible(false);
	ns.setVisible(false);
	sa.setVisible(false);
	as.setVisible(true);   
	ipcap.setVisible(true);
	portcap.setVisible(true);
	ip.setVisible(true);
	port.setVisible(true);
	//     }
	/*if(ns.warte){
	  ns.setVisible(true);
	  }
	  else{
	  ns.setVisible(false);
	  hs.setVisible(true);
	  hs.repaint();
	  }
	  try{
	  meineThreads[thrNum++]=new Ausgabe(ip.getText(), Integer.parseInt(port.getText()));
	  ((Thread)meineThreads[thrNum-1]).start();
	  }catch(Exception exp){
	  statbar.setText(Message.say("StartSpieler","eFalPort"));
	  }
	  System.out.println("JETZT IST thrNum="+thrNum+"!!!!!");*/
    }
  }

 public void mouseEntered(MouseEvent e){
   if(e.getComponent() == neuSp){
       neuSp.setForeground(Color.yellow);
     spAnm.setForeground(Color.black);
     ausg.setForeground(Color.black);
     kSp.setForeground(Color.black);
     exit.setForeground(Color.black);
     statbar.setText(Message.say("StartSpieler","mStBarNeuSp",port.getText(),ip.getText()));
   }
   if(e.getComponent() == spAnm){
     neuSp.setForeground(Color.black);
     spAnm.setForeground(Color.yellow);
     ausg.setForeground(Color.black);
     kSp.setForeground(Color.black);
     exit.setForeground(Color.black);
     statbar.setText(Message.say("StartSpieler","mStBarTeil",ip.getText(),port.getText()));
   }
   if(e.getComponent() == kSp){
     neuSp.setForeground(Color.black);
     spAnm.setForeground(Color.black);
     kSp.setForeground(Color.yellow);
     ausg.setForeground(Color.black);
     exit.setForeground(Color.black);
     statbar.setText(Message.say("StartSpieler","mStBarKS",ip.getText(),port.getText()));
   }
   if(e.getComponent() == ausg){
     neuSp.setForeground(Color.black);
     spAnm.setForeground(Color.black);
     kSp.setForeground(Color.black);
     ausg.setForeground(Color.yellow);
     exit.setForeground(Color.black);
     statbar.setText(Message.say("StartSpieler","mStBarAus",ip.getText(),port.getText()));
   }
   if(e.getComponent() == exit){
     neuSp.setForeground(Color.black);
     spAnm.setForeground(Color.black);
     ausg.setForeground(Color.black);
     kSp.setForeground(Color.black);
     exit.setForeground(Color.yellow);
     statbar.setText(Message.say("StartSpieler","mStBarExit"));
   }
  }

 public void mouseExited(MouseEvent e){
   statbar.setText("");
   if(e.getComponent() == neuSp) neuSp.setForeground(Color.black);
   if(e.getComponent() == spAnm) spAnm.setForeground(Color.black);
   if(e.getComponent() == ausg) ausg.setForeground(Color.black);
   if(e.getComponent() == kSp) kSp.setForeground(Color.black);
   if(e.getComponent() == exit) exit.setForeground(Color.black);
 }

 public void mouseClicked(MouseEvent e){
//  if(e.getComponent() == neuSp) port.setText("WOW!");
     //  if(e.getComponent() == exit) {
     //  this.dispose();
//   System.exit(0);
     //  }
 }

 public void mousePressed(MouseEvent e){}
 public void mouseReleased(MouseEvent e){}


  // Window Events
 public void windowDeactivated(WindowEvent e) {}
 public void windowOpened(WindowEvent e)      {}
 public void windowClosing(WindowEvent e) //{}
  {
  this.dispose();
//  System.exit(0);
/*  try{
   ns.finalize();
   ks.finalize();
   sa.finalize();
   as.finalize();
   this.finalize();
  }catch(Throwable t){System.err.println(t);}*/
  }
 public void windowClosed(WindowEvent e)      {}
 public void windowIconified(WindowEvent e)   {}
 public void windowDeiconified(WindowEvent e) {}
 public void windowActivated(WindowEvent e)   {}

 public void keyPressed(KeyEvent e){
  //statbar.setText("key pressed");
  return;
 }
 public void keyReleased(KeyEvent e){
   ///statbar.setText("key released");
   return;
  }
 public void keyTyped(KeyEvent e){
  //statbar.setText("key typed");
  if (e.getKeyText(e.getKeyCode()).equals("F11"))
  com.sendString("back",ip.getText());
  return;
 }


 public static void main(String[] argv){

     if(argv.length > 0) { //Bugfix zu Lukasz' Code!
     	 StartSpieler sp = new StartSpieler(argv[0]);
     }
     else {
	 StartSpieler sp = new StartSpieler();
     }

     // enno: trigger loading of images in background
     ImageMan.loadImages();
     return;
 }
}




class NeuSp extends Panel implements ActionListener, AdjustmentListener{

    // private Label anmTimCap = null;
    protected Label zugTimCap = null;
    protected Label anzCap =null;
    private Label spfCap = null;
    private Label kacCap =null;
    // protected Button[][] kach=new Button[4][3];
    // protected Button[] flags=new Button[6];
    // private TextField anmTim =  null;
    protected TextField zugTim = null;
    protected TextField anzahl = null;
    protected Scrollbar anzslide=null;
    protected StartSpieler par=null;
    private ScrollPane thumb=null;
    
    protected int fltempX[]= new int[32];
    protected int fltempY[]= new int[32];
    protected int flanzahl = 0;
    
    // protected boolean activ=true;
    
    private int x=0,y=0,x1=600,y1d=30,x1d=130,x2d=50,x3d=150,x4d=50,x5d=150,x6d=30,x7d=100,y1=30;
    private int xbut=0,ybut=0,xbd=0,ybd=0,xbut1=xbut,ybut1=ybd*3;
    
    Image[] img=null;
    private PCanvas probe=null;
    protected ThNCanvas thmbn=null;
    protected FlCanvas flcanv=null;
    protected RobiCanvas rcanv=null;
    protected boolean warte=false;
    String[][] kachdat=new String[3][2];
    String akt=null;
    int imind=-1;
    int[][] drehungen=new int[3][2];
    /*    Color lincol=new Color(4,64,4);
	  Color butcol=new Color(4,64,4);
	  Color foncol=new Color(84,158,73);
    */
    Color lincol=Color.green;
    Color butcol=Color.lightGray;
    Color foncol=Color.black;

 public NeuSp(StartSpieler pa){

  this.par=pa;
  x=par.x-2*par.x1;
  y=par.y3-par.y1-par.yb-par.yk;
  for(int i=0;i<32;i++) fltempX[i]=fltempY[i]=0;

  setSize(x,y);
  setLayout(null);

  zugTimCap=new Label(Message.say("StartSpieler","mNeuSpZugTim"),Label.LEFT);
  add(zugTimCap);
  zugTimCap.setSize(x3d,y1d);
  zugTimCap.setFont(new Font("SansSerif", Font.PLAIN, 11));
  zugTimCap.setLocation(0,y-y1d);

  anzCap=new Label(Message.say("StartSpieler","mNeuSpAnz"),Label.LEFT);
  add(anzCap);
  anzCap.setSize(x5d,y1d);
  anzCap.setFont(new Font("SansSerif", Font.PLAIN, 11));
  anzCap.setLocation(x3d+x4d+100,y-y1d);

  spfCap=new Label(Message.say("StartSpieler","mSpfLab"),Label.CENTER);
  add(spfCap);
  spfCap.setSize(x1-150,y1);
  spfCap.setFont(new Font("SansSerif", Font.BOLD, 14));
  spfCap.setLocation(0,0);

  kacCap=new Label(Message.say("StartSpieler","mKacLab"),Label.CENTER);
  add(kacCap);
  kacCap.setSize(x-x1,y1);
  kacCap.setFont(new Font("SansSerif", Font.BOLD, 14));
  kacCap.setLocation(x1,0);

  anzahl=new TextField("8",50);
  add(anzahl);
  anzahl.setSize(x6d,y1d);
  anzahl.setLocation(x3d+x4d+x5d+100,y-y1d);
  anzahl.addActionListener(this);

        anzslide = new Scrollbar(Scrollbar.VERTICAL, 1, 1, 1, 9);
	anzslide.setSize(15,y1d);
	anzslide.setLocation(x3d+x4d+x5d+x6d+100,y-y1d);
	anzslide.addAdjustmentListener(this);
        add(anzslide);

  /*  anmTim=new TextField("1800",50);
  add(anmTim);
  anmTim.setSize(x2d,y1d);
  anmTim.setLocation(x1d,y-y1d);
  */
  zugTim=new TextField("600",50);
  add(zugTim);
  zugTim.setSize(x4d,y1d);
  zugTim.setLocation(x3d+30,y-y1d);

  thumb=new ScrollPane();
  thumb.setSize(x-x1,y-y1-y1d);
  thumb.setLocation(x1,y1);
  thumb.getVAdjustable().setUnitIncrement(150);
  add(thumb);

  String[] kachfiles=getKacheln();
  img=new Image[kachfiles.length];

  MediaTracker mt = new MediaTracker(this);

  //---------------aus reset------------------//
  /*  for (int i=0; i<4;i++)
   for (int j=0;j<3;j++){
    kach[i][j].setEnabled(false);
    kach[i][j].setLabel((i+1)+","+(j+1));
   }
  kach[0][0].setEnabled(true);
  for (int j=0;j<flags.length;j++){
   flags[j].setLabel(""+(j+1));
  }
  */
  //-----------------------------------------//

  //------------Lade Def. Konf.-----------------//
  //  ladekonf("kacheln"+File.separator+"default.spf");
  //-------------------------------------------//

  //------------Lade Thumbnails-----------------//


	int w=150,h=150;
	int[][] pix=new int[kachfiles.length][w*h];
	try{
	    for (int i=0;i<kachfiles.length;i++){
		InputStream istream=getClass().getResourceAsStream("kacheln"+File.separator+kachfiles[i]);
		//		FileInputStream istream = new FileInputStream("kacheln"+File.separator+kachfiles[i]);
		GZIPInputStream gzi=new GZIPInputStream(istream);
		ObjectInputStream p = new ObjectInputStream(gzi);
		pix[i] = (int[])p.readObject();
		istream.close();
	    }
	}catch(Exception e){System.out.println(e+"\n Fehler beim Lesen.");}

	try{
	    for (int i=0;i<kachfiles.length;i++){
		img[i] = createImage(new MemoryImageSource(w, h, pix[i], 0, w));
		mt.addImage(img[i],i);

	    }
	}catch(Exception e){System.out.println(e+"\n Fehler beim Konvertieren.");}

	try{mt.waitForAll();}
	catch(InterruptedException ie){System.err.println(ie.getMessage());}


  thmbn=new ThNCanvas(this,kachfiles);
  thmbn.setSize(160,160*kachfiles.length);
  thmbn.setLocation(0,0);
  //  thumb.add(thmbn);
  //-----------------------------//

  //--------File Canv------------//
  flcanv=new FlCanvas(this);
  flcanv.setSize(150,300);
  flcanv.setLocation(0,0);
  thumb.add(flcanv);
  //  reset();
  //-----------------------------//
  //--------Robi Canv------------//
  rcanv=new RobiCanvas(this);
  rcanv.setSize(155,350);
  rcanv.setLocation(0,0);
  //  thumb.add(rcanv);
  //-----------------------------//

  //-----------------------------//

  probe=new PCanvas(null,0,0);
  //hs=new StSpHalloCanv(360,360);
  probe.setSize(x1,y-y1d-y1);
  //hs.setSize(360,360);
  probe.setLocation(0,y1);
  add(probe);

  for (int i=0;i<3;i++){
      drehungen[i][0]=0;
      drehungen[i][1]=0;
  }
  
  /*probe.addImage(img,0,1);
  probe.addImage(img,1,0);
  probe.addImage(img,2,0);*/


 }

    public void adjustmentValueChanged(AdjustmentEvent e){
	anzahl.setText(""+(9-anzslide.getValue()));
    }

    public String[] getKacheln(){
	//      URL kurl=getClass().getResource("kacheln");
	//	System.out.println(kurl.getFile());
	File kd=new File(par.zuspath+"kacheln"/*kurl.getFile()*/);
	System.err.println(""+kd);
	String[] all = kd.list(new ThumbFilter());
	/*	int s=0;
	String tmp=new String[all.length/2];
	for (int i=0;i<all.length;i++){
	    if (all[i].endsWith(".rra")&&all[i+1].endsWith(".thmb"))&&(all[i].s)
	    }*/
	return all;
    }

    public void reset (){
	if (!warte){
	thumb.remove(flcanv);
	for (int i=0;i<8;rcanv.robies[i++]=/*"bla"*/null);
  	thumb.remove(rcanv);
	thumb.add(thmbn);
	probe.flaggen=false;
	probe.watch=false;
	fltempX= new int[32];
	fltempY= new int[32];
	flanzahl=0;
	for(int i=0;i<fltempX.length;i++) fltempX[i]=fltempY[i]=0;
	rcanv.robanz=0;
	}
	probe.repaint();
	//	probe.spfok="Spielfeld OK";
    }
       /*  fltempX= new int[6];
  fltempY= new int[6];
  for(int i=0;i<fltempX.length;i++) fltempX[i]=fltempY[i]=0;
  for (int i=0; i<4;i++)
   for (int j=0;j<3;j++){
    kach[i][j].setEnabled(false);
    kach[i][j].setLabel((i+1)+","+(j+1));
   }
  kach[0][0].setEnabled(true);
  for (int j=0;j<flags.length;j++){
   flags[j].setLabel(""+(j+1));
  }

  inputReturned("kacheln/cross.rra",1,1,0);
  kach[0][0].setLabel("cross.rra");
  fltempX[0]=3; fltempY[0]=3;
  fltempX[1]=8; fltempY[1]=9;
  fltempX[2]=11; fltempY[2]=3;
  flags[0].setLabel("("+3+","+3+")");
  flags[1].setLabel("("+8+","+9+")");
  flags[2].setLabel("("+11+","+3+")");
  anzahl.setText("2");
  }*/

 public void feldClick(int x,int y){
     if ((akt!=null)&&(imind>=0)) {
	 kachdat[x][y]=akt;
	 probe.addImage(img[imind],x,y);
	 thmbn.fClick();
     }
     //     System.out.println("kachel "+x+" "+y);  
 }

 public void feldRightClick(int x,int y){
     drehungen[x][y]=(drehungen[x][y]+3)%4;
 }

 public void thumbClick(String datei,int ind){
  akt=datei;
  imind=ind;
  //  System.out.println(akt);
 }

 public void delKach(int x, int y){
     //    System.out.println("Lösche "+kachdat[x][y]+" von "+x+","+y);
     kachdat[x][y]=null;
     drehungen[x][y]=0;
     //     System.out.println("Neuer Inhalt "+kachdat[x][y]);
 }

 public boolean spfok(){
     boolean nachb=false;
     //------Spielfeld prüfen---------//
     int xmin=4,ymin=4;
     int singls=0,cntr=0;
     for (int i=0;i<3;i++)
	 for (int j=0;j<2;j++){
	     if (kachdat[i][j]!=null){
		 nachb=false;
		 if (i>0) nachb=nachb||(kachdat[i-1][j]!=null); 
		 if (i<2) nachb=nachb||(kachdat[i+1][j]!=null); 
		 if (j==0) nachb=nachb||(kachdat[i][1]!=null); 
		 if (j==1) nachb=nachb||(kachdat[i][0]!=null); 
		 if (!nachb) singls++;
		 if (i<xmin) xmin=i;
		 if (j<ymin) ymin=j;
		 cntr++;
	     }
	 }
     if (xmin==4||ymin==4||(singls>0&&cntr>1)) return false;
     if (kachdat[0][0]!=null&&kachdat[0][1]!=null&&kachdat[2][0]!=null&&kachdat[2][1]!=null&&kachdat[1][0]==null&&kachdat[1][1]==null)  return false;
     //---------Spielfeld erstellen----------//
     //--schieben--//
     if (ymin==1) 
	 for (int i=0;i<3;i++){
	     kachdat[i][0]=kachdat[i][1];
	     drehungen[i][0]=drehungen[i][1];
	     kachdat[i][1]=null;
	     drehungen[i][1]=0;
	     probe.im[i][0]=probe.im[i][1];
	     probe.im[i][1]=null;
	 }
     if (xmin>0){
	 for (int i=0;i<3-xmin;i++){
	     kachdat[i][0]=kachdat[i+xmin][0];
	     drehungen[i][0]=drehungen[i+xmin][0];
	     kachdat[i][1]=kachdat[i+xmin][1];
	     drehungen[i][1]=drehungen[i+xmin][1];
	     probe.im[i][0]=probe.im[i+xmin][0];
	     probe.im[i][1]=probe.im[i+xmin][1];
	 }
	 for (int i=3-xmin;i<3;i++){
	     kachdat[i][0]=null;
	     drehungen[i][0]=0;
	     kachdat[i][1]=null;
	     drehungen[i][1]=0;
	     probe.im[i][0]=null;
	     probe.im[i][1]=null;
	 }
     }
     //--StSpParsen--//
     for (int i=0;i<3;i++)
	 for (int j=0;j<2;j++)
	     if (kachdat[i][j]!=null){
		 inputReturned(kachdat[i][j],i+1,j+1, drehungen[i][j]);
		 //		 System.out.println(kachdat[i][j]);
	     }
     //--------------------------------------//
     thumb.remove(thmbn);
     thumb.add(flcanv);
     return true;
 }
    
 public void vorschau(){
       try{
	   Ort dim=StSpParser.getFieldSize();
	   new StSpVorschau(StSpParser.getField(),dim.x, dim.y, getFlags());
       }catch(Exception expt){
	   System.err.println("Feld oder Flaggen nicht ok "+expt);
       }
 }

 public boolean spstart(){
    try{
	String ip=par.ip.getText();
	int prt=Integer.parseInt(par.port.getText());
	int anz=Integer.parseInt(anzahl.getText());
	int aTim=0;//Integer.parseInt(anmTim.getText());
	int zTim=Integer.parseInt(zugTim.getText());
	Ort sizeF=StSpParser.getFieldSize();
	if(!rewriteFlags()){
	    par.statbar.setText(Message.say("StartSpieler","eKeineFl"));
	    throw new Exception();
	}
	String feld=StSpParser.getField();
	int retfrnewgame=par.com.newGame(ip, prt, anz, aTim, zTim, feld, fltempX, fltempY,sizeF.x,sizeF.y,rcanv.lis.PORTNR);
	if(retfrnewgame==0)
	    par.statbar.setText(Message.say("StartSpieler","mSpielGest"));
	else if (retfrnewgame==1){
	    par.statbar.setText(Message.say("StartSpieler","eSpielGesch"));
	    return false;
	}else {
	    par.statbar.setText(Message.say("StartSpieler","mIPGeaendert"));
	    par.ip.setText("127.0.0.1");
	} 
	
    }catch(Exception u){
	System.err.println(Message.say("StartSpieler","eFalscheAngaben"));
	return false;
    }
    thumb.remove(flcanv);
    thumb.add(rcanv);
    warte=true;
    return true;
 }

 public boolean splos(){
     String ip=par.ip.getText();
     int prt=Integer.parseInt(par.port.getText());
     try{     
	 if(par.com.game(ip,prt))
	     par.statbar.setText(Message.say("StartSpieler","mSpielGest"));
	 else par.statbar.setText(Message.say("StartSpieler","eSpielGesch"));
     }catch(Exception u){
	 System.err.println(Message.say("StartSpieler","eFalscheAngaben"));
	 return false;
     }
     this.setVisible(false);
     par.hs.setVisible(true);
     par.hs.repaint();
     warte=false;
     /*     thumb.remove(flcanv);
	    thumb.remove(rcanv);
	    thumb.add(rcanv);
     */
     return true;
 }

 public boolean checkFlag(int tmpx, int tmpy, int iF){
     boolean ok=true;
     String probl="";
     if (iF>32) return false;
     try{
	 if (StSpParser.fromFilez[(tmpx-1)/12][(tmpy-1)/12]==null){
	     par.statbar.setText(Message.say("StartSpieler","eKeineKach"));
	     ok=false;
	 }
	 for (int i=0; i<fltempX.length;i++)
	     if (tmpx== fltempX[i]&&tmpy== fltempY[i]&&i!=iF-1){
		 par.statbar.setText(Message.say("StartSpieler","eStehtSchon"));
		 ok=false;
	     }
	 Ort dim = StSpParser.getFieldSize();
	 Ort[] altfl = getFlags();
	 int len=0;
	 if (altfl==null)
	     len=0;
	 else len=altfl.length;
	 boolean nf=true;
	 if (iF<=len) nf=false;
	 Ort flgs[]=null;
	 if (nf) flgs=new Ort[len+1];
	 else flgs=new Ort[len];
	 for (int i=0;i<len;flgs[i]=altfl[i++]);
	 flgs[iF-1]=new Ort(tmpx, tmpy);
	 Spielfeld spf = new Spielfeld(dim.x, dim.y, StSpParser.getField(), flgs);
	 probl = spf.getFlaggenProbleme();
     }catch(FlaggenException fe){
	 par.statbar.setText(fe.getMessage());
	 ok=false;
     }catch (FormatException frm){
	 System.out.println("Hmmm!!!"+frm);
     }catch (Exception e){
	 System.out.println("Exception! "+e);
	 par.statbar.setText(Message.say("StartSpieler","eZuViel"));
	 ok=false;
	 return false;
     }
     if (ok&&(!probl.equals("")))
	 new Flagerr(par,probl);
     return ok;
 }
    
 public void actionPerformed(ActionEvent e)
  {
      try{
	  int eing=Integer.parseInt(e.getActionCommand());
	  if (eing<=1){
	      anzslide.setValue(8);
	      anzahl.setText("1");
	  }
	  else if (eing<9) anzslide.setValue(9-eing);
	  else{
	      anzslide.setValue(1);
	      anzahl.setText("8");
	  }
      }
      catch(Exception u){
	  par.statbar.setText("Anzahl der Mitspieler soll ein Integer von 1 bis 8 sein!");
      } 
      
   return;
  }

 public boolean rewriteFlags(){
     return rewriteFlags(true);
 }

 public boolean rewriteFlags(boolean kuerzen){
  int cntr=0;
  for(int i=0;i<fltempX.length;i++)
   if((fltempX[i]!=0)&&(fltempY[i]!=0)) cntr++;

  if(cntr<2&&kuerzen) return false;

  int[] tmpX=null;
  int[] tmpY=null;

  if(kuerzen){
   tmpX=new int[cntr];   
   tmpY=new int[cntr];   
  }
  else{
   tmpX=new int[32];   
   tmpY=new int[32];   
  }
  cntr=0;

  for(int i=0;i<fltempX.length;i++)
   if((fltempX[i]!=0)&&(fltempY[i]!=0)){
    tmpX[cntr]=fltempX[i];
    tmpY[cntr++]=fltempY[i];
   }
  fltempX=tmpX;
  fltempY=tmpY;
  return true;
 }

 public Ort[] getFlags(){
  int cntr=0;
  for(int i=0;i<fltempX.length;i++)
   if((fltempX[i]!=0)&&(fltempY[i]!=0)) cntr++;

  if(cntr==0) return null;

  Ort[] tmp=new Ort[cntr];
  cntr=0;

  for(int i=0;i<fltempX.length;i++)
   if((fltempX[i]!=0)&&(fltempY[i]!=0)){
    tmp[cntr++]=new Ort(fltempX[i],fltempY[i]);
   }
  return tmp;
 }

 public boolean inputReturned(String fil, int iKach,int jKach,int dr){
  try{
   InputStream istr=getClass().getResourceAsStream(fil);
   StSpParser.addFile(iKach, jKach, /*fil*/istr,dr);
   /*   if (iKach<4) kach[iKach][jKach-1].setEnabled(true);
   if (jKach<3) kach[iKach-1][jKach].setEnabled(true);
   if (iKach-2>=0) kach[iKach-2][jKach-1].setEnabled(true);
   if (jKach-2>=0) kach[iKach-1][jKach-2].setEnabled(true);*/
  }catch(Exception e){
   return false;
  }

  return true;
 }


}

class ThumbFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
	return name.endsWith(".thmb");
    }
}

class PCanvas extends Canvas implements MouseListener{
    protected Image[][] im=new Image[3][2];
    public int lastX=-1,lastY=-1;
    private boolean kachDel=false;
    public boolean flaggen=false;
    public boolean watch=false;
    public boolean wirdVersch=false;
    public boolean justone=false;
    private int verschPos=-1;
    private int x11=460,x12=590,y11=0,y12=50,x21=x11,x22=x12,y21=100,y22=150,x31=x11,x32=x12,y31=200,y32=250;
    /* private Image trash,trashakt;
    private boolean trdone=false;
    */
    //    private boolean justone=false;
   
    public PCanvas(Image i,int x,int y){
	im[x][y]=i;
	addMouseListener(this);
	/*Toolkit tk=Toolkit.getDefaultToolkit();
        trash=tk.getImage("images"+File.separator+"trash100.gif");
        trashakt=tk.getImage("images"+File.separator+"anieyes.gif");
	*/
    }

    public void addImage(Image i,int x,int y){
	im[x][y]=i;
	repaint();
    }
    
    public void paint(Graphics g){

      for(int i=0;i<3;i++)
	  for(int j=0;j<2;j++)
	      if(im[i][j]!=null) g.drawImage(im[i][j],i*150,(1-j)*150,this);

      g.setColor(((NeuSp)getParent()).lincol);
      for(int i=0;i<=3;i++) g.drawLine(i*150,0,i*150,300);
      for(int j=0;j<=2;j++) g.drawLine(0,j*150,450,j*150);
      delKachBut(g);
      spFOKBut(g);
      if(flaggen) flaggenM(g);
      zurBut(g);
      /*      if (!trdone){
	  g.drawImage(trashakt,460,300,this);
	  	  trdone=true;
	  }*/
    }

    private void delKachBut(Graphics gg){
	gg.setColor(((NeuSp)getParent()).butcol);
	gg.fill3DRect(x11,y11,x12-x11,y12-y11,!kachDel);
	gg.setColor(((NeuSp)getParent()).foncol);
	gg.setFont(new Font("SansSerif",Font.PLAIN,14));
	if(!flaggen) gg.drawString(Message.say("StartSpieler","mKachDel"),x11+20,(y11+y12)/2);
	else gg.drawString(Message.say("StartSpieler","mVorschau"),x11+20,(y11+y12)/2);
    }


    private void spFOKBut(Graphics gg){
	gg.setColor(((NeuSp)getParent()).butcol);
	gg.fill3DRect(x21,y21,x22-x21,y22-y21,/*false*/true);
	gg.setColor(((NeuSp)getParent()).foncol);
	gg.setFont(new Font("SansSerif",Font.BOLD,14));
	if(!flaggen&&!watch) gg.drawString(Message.say("StartSpieler","mSpFOK"),x21+20,(y21+y22)/2);
	else if(flaggen&&!watch) gg.drawString(Message.say("StartSpieler","mSpSt"),x21+20,(y21+y22)/2);
	else gg.drawString(Message.say("StartSpieler","mSpLos"),x21+20,(y21+y22)/2);
    }

    private void zurBut(Graphics gg){
	if(flaggen&&!watch){
	    gg.setColor(((NeuSp)getParent()).butcol);
	    gg.fill3DRect(x31,y31,x32-x31,y32-y31,/*false*/true);
	    gg.setColor(((NeuSp)getParent()).foncol);
	    gg.setFont(new Font("SansSerif",Font.PLAIN,14));
	    gg.drawString(Message.say("StartSpieler","mZurSpF1"),x31+30,y31+20);
	    gg.drawString(Message.say("StartSpieler","mZurSpF2"),x31+30,y31+35);
	}else if(!flaggen&&!watch)
	    {
	    gg.setColor(((NeuSp)getParent()).butcol);
	    gg.fill3DRect(x31,y31,x32-x31,y32-y31,/*false*/true);
	    gg.setColor(((NeuSp)getParent()).foncol);
	    gg.setFont(new Font("SansSerif",Font.PLAIN,14));
	    gg.drawString(Message.say("StartSpieler","mQuickDirty1"),x31+30,y31+20);
	    gg.drawString(Message.say("StartSpieler","mQuickDirty2"),x31+30,y31+35);
	    }
    }

    private void flaggenM(Graphics gg){
	int flanz=((NeuSp)getParent()).flanzahl;
	int[] flX=((NeuSp)getParent()).fltempX;
	int[] flY=((NeuSp)getParent()).fltempY;
	if(flanz>0){
	    gg.setColor(Color.white);
	    gg.setFont(new Font("SansSerif",Font.BOLD,12));
	    for(int i=0;i<flanz;i++){
		if(justone&&(verschPos==i))
		    gg.setColor(Color.blue);
		else
		    gg.setColor(Color.white);
		if((i+1)<10)
		    gg.drawString(""+(i+1),(int)((double)flX[i]*12.5-8.0),300-(int)((double)flY[i]*12.5-10.0));
		else 
		    gg.drawString(""+(i+1),(int)((double)flX[i]*12.5-12.0),300-(int)((double)flY[i]*12.5-10.0));	
		
	    }
	}

    }

    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}

    public void mouseClicked(MouseEvent e){
	((NeuSp)getParent()).par.statbar.setText("");
	lastX=e.getX();
	lastY=e.getY();
	if((lastX>0)&&(lastX<450)&&(lastY<300)&&(lastY>0)){
	    if(!flaggen&&!watch){
		if(!kachDel){
		    if(e.getModifiers()==e.BUTTON1_MASK){
			((NeuSp)getParent()).feldClick(lastX/150,1-lastY/150);
		    }
		    else if (im[lastX/150][1-lastY/150]!=null){
			((NeuSp)getParent()).feldRightClick(lastX/150,1-lastY/150);
			im[lastX/150][1-lastY/150]=gedreht(im[lastX/150][1-lastY/150]);
			repaint();
		    }
		}
		else{//kachel löschen
		    im[lastX/150][1-lastY/150]=null;
		    ((NeuSp)getParent()).delKach(lastX/150,1-lastY/150);
		    kachDel=false;
		    repaint();
		}//ende if !kachDel
	    }//if !flaggen
	    else if (flaggen&&!watch){//flaggen 
if (((NeuSp)getParent()).flcanv.losch||(e.getModifiers()!=e.BUTTON1_MASK)){// flagge löschen
		    int xco=(int)(((double)lastX)/12.5)+1;
		    int yco=(int)(((double)(300-lastY))/12.5)+1;
		    int pos =searchFlag(xco,yco);
		    if(pos >-1){ //flagge steht noch da 
			((NeuSp)getParent()).fltempX[pos]=0;
			((NeuSp)getParent()).fltempY[pos]=0;
			if (pos<((NeuSp)getParent()).flanzahl) 
			    ((NeuSp)getParent()).rewriteFlags(false);
			((NeuSp)getParent()).flanzahl--;
			((NeuSp)getParent()).flcanv.losch=false;
			((NeuSp)getParent()).flcanv.neu=true;
			((NeuSp)getParent()).flcanv.repaint();
			repaint();
		    } 
		    else {//flagge ist nicht da	
			((NeuSp)getParent()).par.statbar.setText(Message.say("StartSpieler","mDaIstKeine"));
		    }
		}// ende flagge löschen
		else if(((NeuSp)getParent()).flcanv.neu){ //flagge setzen
		    int xco=(int)(((double)lastX)/12.5)+1;
		    int yco=(int)(((double)(300-lastY))/12.5)+1;
		    boolean flaggeok=((NeuSp)getParent()).checkFlag(xco,yco,((NeuSp)getParent()).flanzahl+1);
		    if(flaggeok){
			((NeuSp)getParent()).fltempX[((NeuSp)getParent()).flanzahl]=xco;
			((NeuSp)getParent()).fltempY[((NeuSp)getParent()).flanzahl]=yco;
			((NeuSp)getParent()).flanzahl++;
			repaint();
		    }
		} 
		else if (((NeuSp)getParent()).flcanv.versch){// flagge verschieben
		    if(wirdVersch){ //flagge ist schon fast verschoben
			int xco=(int)(((double)lastX)/12.5)+1;
			int yco=(int)(((double)(300-lastY))/12.5)+1;
			boolean flaggeok=((NeuSp)getParent()).checkFlag(xco,yco,verschPos+1);
			if(flaggeok){
			    ((NeuSp)getParent()).fltempX[verschPos]=xco;
	 		    ((NeuSp)getParent()).fltempY[verschPos]=yco;
			    repaint();
			    verschPos=-1;
			    wirdVersch=false;
			    justone=false;
			    ((NeuSp)getParent()).flcanv.neu=true;
			    ((NeuSp)getParent()).flcanv.versch=false;
			    ((NeuSp)getParent()).flcanv.repaint();
			}
		    }
		    else{ //es geht erst los
			int xco=(int)(((double)lastX)/12.5)+1;
			int yco=(int)(((double)(300-lastY))/12.5)+1;
			int pos =searchFlag(xco,yco);
			if(pos >-1){ //flagge steht noch da 
/*			    ((NeuSp)getParent()).fltempX[pos]=0;
			    ((NeuSp)getParent()).fltempY[pos]=0;
*/			    wirdVersch=true;
			    verschPos=pos;
			    justone=true;
			    repaint();
			} 
		    else {//flagge ist nicht da			
			((NeuSp)getParent()).par.statbar.setText(Message.say("StartSpieler","mDaIstKeine"));
		    }
		    }
		}// ende flagge verschieben
	    }// else if !flaggen
	}//ende feldclick

	if((lastX>x11)&&(lastX<x12)&&(lastY>y11)&&(lastY<y12)){ //delKach/vorschau
	    if(!flaggen){
		if(!kachDel) kachDel=true;
		else kachDel=false;
		repaint();
	    }
	    else{
		((NeuSp)getParent()).vorschau();
	    }
	}
	if((lastX>x31)&&(lastX<x32)&&(lastY>y31)&&(lastY<y32)){ //zuruck/quick&dirty
	    if(flaggen&&!watch){
		((NeuSp)getParent()).reset();
		((NeuSp)getParent()).thmbn.repaint();
		repaint();
	    }else if(!flaggen&&!watch)//quick &dirty
		{
		    for (int ix=0;ix<3;ix++)
			for(int iy=0;iy<2;iy++){
			    ((NeuSp)getParent()).delKach(ix,iy);
			    im[ix][iy]=null;
			}
		    //repaint();
		    ((NeuSp)getParent()).kachdat[0][0]="kacheln"+File.separator+"cross.rra";
		    ((NeuSp)getParent()).drehungen[0][0]=0;
		    int i;
		    for (i=0;i<((NeuSp)getParent()).thmbn.kdat.length;i++){
			if (((NeuSp)getParent()).thmbn.kdat[i].equals("cross.rra")) break;
		    }
		    addImage (((NeuSp)getParent()).img[i],0,0);
		    if (((NeuSp)getParent()).spfok()){
			flaggen=true;
			((NeuSp)getParent()).fltempX[0]=2;
			((NeuSp)getParent()).fltempY[0]=3;
			((NeuSp)getParent()).fltempX[1]=10;
			((NeuSp)getParent()).fltempY[1]=12;
			((NeuSp)getParent()).fltempX[2]=4;
			((NeuSp)getParent()).fltempY[2]=5;
			((NeuSp)getParent()).flanzahl=3;
			//			repaint();
			if (((NeuSp)getParent()).spstart()){
			    watch=true;
			    ((NeuSp)getParent()).par.ipcap.setVisible(false);
			    ((NeuSp)getParent()).par.portcap.setVisible(false);
			    ((NeuSp)getParent()).par.ip.setVisible(false);
			    ((NeuSp)getParent()).par.port.setVisible(false);
			    ((NeuSp)getParent()).zugTimCap.setVisible(false);
			    ((NeuSp)getParent()).zugTim.setVisible(false);
			    ((NeuSp)getParent()).anzCap.setVisible(false);
			    ((NeuSp)getParent()).anzahl.setVisible(false);
			    ((NeuSp)getParent()).anzslide.setVisible(false);
			}
			repaint();
		    }
		    
		}
	}
	if((lastX>x21)&&(lastX<x22)&&(lastY>y21)&&(lastY<y22)){//spfok/spiel starten/abwarten //
	    if(!flaggen&&!watch){
		if (((NeuSp)getParent()).spfok()){
		    flaggen=true;
		    repaint();
		}
	    }
	    else if (flaggen && !watch){
		if (((NeuSp)getParent()).spstart()){
		    watch=true;
		    ((NeuSp)getParent()).par.ipcap.setVisible(false);
		    ((NeuSp)getParent()).par.portcap.setVisible(false);
		    ((NeuSp)getParent()).par.ip.setVisible(false);
		    ((NeuSp)getParent()).par.port.setVisible(false);
		    ((NeuSp)getParent()).zugTimCap.setVisible(false);
		    ((NeuSp)getParent()).zugTim.setVisible(false);
		    ((NeuSp)getParent()).anzCap.setVisible(false);
		    ((NeuSp)getParent()).anzahl.setVisible(false);
		    ((NeuSp)getParent()).anzslide.setVisible(false);
		}
		repaint();
	    }
	    else {
		if(((NeuSp)getParent()).rcanv.robanz>0){
		    ((NeuSp)getParent()).splos();
		    ((NeuSp)getParent()).par.statbar.setText("");
		    //?
		    ((NeuSp)getParent()).par.ipcap.setVisible(false);
		    ((NeuSp)getParent()).par.portcap.setVisible(false);
		    ((NeuSp)getParent()).par.ip.setVisible(false);
		    ((NeuSp)getParent()).par.port.setVisible(false);
		    ((NeuSp)getParent()).zugTimCap.setVisible(true);
		    ((NeuSp)getParent()).zugTim.setVisible(true);
		    ((NeuSp)getParent()).anzCap.setVisible(true);
		    ((NeuSp)getParent()).anzahl.setVisible(true);
		    ((NeuSp)getParent()).anzslide.setVisible(true);
		}
		else
		    ((NeuSp)getParent()).par.statbar.setText(Message.say("StartSpieler","eKeineSpieler"));
	    }
	}

    }

    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}

    private int searchFlag(int xc, int yc){
	int fan=((NeuSp)getParent()).flanzahl;
	int[] fX=((NeuSp)getParent()).fltempX;
	int[] fY=((NeuSp)getParent()).fltempY;
	for (int i=0;i<fan;i++)
	    if ((fX[i]==xc) && (fY[i]==yc)) return i;
	return -1;
    }

    public Image gedreht(Image imsrc){
	Image tmp=null;
	int w=150,h=150;
        int[] pixls = new int[w * h];
	//--------------img -> pix------------//
        PixelGrabber pg = new PixelGrabber(imsrc, 0, 0, w, h, pixls, 0, w);
        try {
            pg.grabPixels();
        } catch (Exception e) {
            System.err.println("interrupted waiting for pixels!");
        }

	//--------------drehen----------------//
        int[] npixls = new int[w * h];
	int k=0;
	for(int i=0;i<w;i++)
	    for (int j=0;j<h;j++){
		npixls[k++]=pixls[(w-1-j)*w+i];
	    }

	//--------------pix -> img------------//
	try{
	    tmp = createImage(new MemoryImageSource(w, h, npixls, 0, w));
	}catch(Exception e){System.out.println(e+"\n Fehler beim Konvertieren.");}
	return tmp;
    }

}

class ThNCanvas extends Canvas implements MouseListener{
    private NeuSp par;
    private int clickX=-1,clickY=-1, sel=-1;
    protected String[] kdat =null;
    public ThNCanvas(NeuSp p,String[] d){
	par=p;
	kdat=new String[d.length];
	for (int i=0;i<d.length;i++)
	    kdat[i]=d[i].substring(0,d[i].length()-4)+"rra";
	addMouseListener(this);
    }
    public void paint(Graphics g){
	for (int i=0;i<par.img.length;i++){
	    g.drawImage(par.img[i],3,i*160+2,this);
	    g.setColor(Color.black);
	    g.setFont(new Font("SansSerif",Font.PLAIN, 8));
	    g.drawString(kdat[i].substring(0,kdat[i].length()-4),3,i*160+158);
	}
	if (sel>=0&&sel<par.img.length){
	    g.setColor(par.lincol);
	    g.drawRect(1,sel*160,155,159);
	    g.drawRect(2,sel*160+1,153,157);
	}
    }
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseClicked(MouseEvent e){
	par.par.statbar.setText("");
	clickX=e.getX();
	clickY=e.getY();
	int kclick=clickY/160;
	if (kclick==sel) sel=-1;
	else sel=kclick;
	repaint();
	if (sel!=-1){
	    par.thumbClick("kacheln"+File.separator+kdat[sel],sel);
	    par.par.statbar.setText(Message.say("StartSpieler","mFeldClick"));
	}
	else{
	    par.thumbClick(null,sel);
	    par.par.statbar.setText(Message.say("StartSpieler","mThumbClick"));
	}
    }
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void fClick(){
	sel=-1;
	repaint();
	par.thumbClick(null,sel);
	par.par.statbar.setText(Message.say("StartSpieler","mRightClick"));
    }
}

class FlCanvas extends Canvas implements MouseListener{
    private NeuSp par;
    public boolean neu=true,versch=false,losch=false;
    //    private int x1,y1,ys1;
    public FlCanvas(NeuSp p){
	par=p;
	addMouseListener(this);
	//x1=70;
	//y1=25;
    }
    public void paint(Graphics g){
	nflBut(g);
	mvflBut(g);
	delflBut(g);
    }
    private void nflBut(Graphics g){
	g.setColor(par.butcol);
	g.fill3DRect(10,20,130,50,!neu);
	g.setColor(par.foncol);
	g.setFont(new Font("SansSerif",Font.PLAIN,14));
	g.drawString(Message.say("StartSpieler","mFlagSetz"),20,50);
    }

    private void mvflBut(Graphics g){
	g.setColor(par.butcol);
	g.fill3DRect(10,90,130,50,!versch);
	g.setColor(par.foncol);
	g.setFont(new Font("SansSerif",Font.PLAIN,14));
	g.drawString(Message.say("StartSpieler","mFlagVersch"),20,120);
    }

    private void delflBut(Graphics g){
	g.setColor(par.butcol);
	g.fill3DRect(10,160,130,50,!losch);
	g.setColor(par.foncol);
	g.setFont(new Font("SansSerif",Font.PLAIN,14));
	g.drawString(Message.say("StartSpieler","mFlagDel"),20,190);
    }

    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseClicked(MouseEvent e){
	int clickX,clickY;
	clickX=e.getX();
	clickY=e.getY();
	if (clickX<=140&&clickX>=10){
	    if (clickY>=20&&clickY<=70){
		if (neu) neu=false; 
		else{
		    neu=true;
		    versch=false;
		    losch=false;
		}
	    }
	    else if (clickY>=90&&clickY<=140){
		if (versch) versch=false; 
		else{
		    versch=true;
		    neu=false;
		    losch=false;
		}
	    }
	    else if (clickY>=60&&clickY<=210){
		if (losch) losch=false; 
		else{
		    losch=true;
		    versch=false;
		    neu=false;
		}
	    }
	    repaint();
	}
    }
    public void passiert(){
	versch=losch=false;
	repaint();
    }
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
}

class RobiCanvas extends Canvas{
    private NeuSp par;
    private final Color[] robocolor = SACanvas.robocolor;
    String[] robies=new String[8];
    StSpLis lis=new StSpLis(this);
    protected int robanz=0;
    public RobiCanvas(NeuSp p){
	par=p;
	//	for (int i=0;i<8;robies[i++]="bla");
	lis.start();
    }
    public void paint(Graphics g){
	g.setColor(Color.black);
	g.drawString(Message.say("StartSpieler","mAnmRob"),20,25);
	for (int i=0;i<8;i++)
	    if (robies[i]!=null){
		g.setColor(robocolor[i]);
		g.fillOval(20,i*30+35,20,20);
		g.drawString(robies[i],50,i*30+50);
	    }
	if (!par.warte){
	    g.setColor(Color.black);
	    g.drawString(Message.say("StartSpieler","mSpGl"),20,310);
	    g.drawString(Message.say("StartSpieler","mSpGl2"),20,325);
	}
    }
    public void neurob(String nam, int farb){
	robies[farb]=nam;
	robanz++;
	repaint();
    }
    public void spGL(){
	par.warte=false;
	repaint();
    }
}

class SpAnm extends Panel implements ActionListener{

 private Label caption=null;
 private Label namCap=null;
 private TextField nam=null;
 private Button fertig=null;
 private String fertCap;
 private StartSpieler par=null;
 private CheckboxGroup cbg=null;
 private Checkbox[] cbox=new Checkbox[9];
 private Label farbcap=null;

 public SpAnm(StartSpieler pa,String capt,String nc, String n, String fert){
  fertCap=fert;
  setSize(600,400);
  setLayout(null);
  par=pa;

  caption=new Label(capt,/*"Einen Spieler starten",*/Label.CENTER);
  add(caption);
  caption.setSize(360,30);
  caption.setFont(new Font("SansSerif", Font.BOLD, 16));
  caption.setLocation(0,0);

  namCap=new Label(nc,/*"Spielername",*/ Label.RIGHT);
  namCap.setSize(140,30);
  namCap.setFont(new Font("SansSerif", Font.PLAIN, 12));
  namCap.setLocation(30,60);
  add(namCap);

  nam=new TextField(n,/*"--Name eingeben--",*/50);
  nam.setSize(150,30);
  nam.setLocation(180,60);
  add(nam);

  fertig=new Button(fertCap/*"Spieler erzeugen und anmelden"*/);
  fertig.setSize(300,30);
  fertig.setLocation(30,120);
  fertig.addActionListener(this);
  add(fertig);

  cbg=new CheckboxGroup();

  cbox[0]=new Checkbox(Message.say("StartSpieler","mFarb0"),cbg,true);
  cbox[0].setForeground(Color.black);
  add(cbox[0]);
  cbox[1]=new Checkbox(Message.say("StartSpieler","mFarb1"),cbg,false);
  cbox[1].setForeground(Color.green);
  add(cbox[1]);
  cbox[2]=new Checkbox(Message.say("StartSpieler","mFarb2"),cbg,false);
  cbox[2].setForeground(Color.yellow);
  add(cbox[2]);
  cbox[3]=new Checkbox(Message.say("StartSpieler","mFarb3"),cbg,false);
  cbox[3].setForeground(Color.red);
  add(cbox[3]);
  cbox[4]=new Checkbox(Message.say("StartSpieler","mFarb4"),cbg,false);
  cbox[4].setForeground(Color.blue);
  add(cbox[4]);
  cbox[5]=new Checkbox(Message.say("StartSpieler","mFarb5"),cbg,false);
  cbox[5].setForeground(Color.magenta);
  add(cbox[5]);
  cbox[6]=new Checkbox(Message.say("StartSpieler","mFarb6"),cbg,false);
  cbox[6].setForeground(Color.orange);
  add(cbox[6]);
  cbox[7]=new Checkbox(Message.say("StartSpieler","mFarb7"),cbg,false);
  cbox[7].setForeground(Color.gray);
  add(cbox[7]);
  cbox[8]=new Checkbox(Message.say("StartSpieler","mFarb8"),cbg,false);
  cbox[8].setForeground(Color.magenta.darker());
  add(cbox[8]);

  farbcap=new Label(Message.say("StartSpieler","mFarb"));
  farbcap.setSize(200,50);
  farbcap.setLocation(400,10);
  add(farbcap);
  
  for (int i=0;i<9;i++){
      cbox[i].setLocation(400,i*30+50);
      cbox[i].setSize(150,30);
  }

 }

 public void actionPerformed(ActionEvent e)
  {
   if(e.getActionCommand().compareTo("Spieler erzeugen und anmelden") == 0)
    {
	int sel=0;
	if(cbg.getSelectedCheckbox()==cbox[1])
	    sel=1;
	else if(cbg.getSelectedCheckbox()==cbox[2])
	    sel=2;
	else if(cbg.getSelectedCheckbox()==cbox[3])
	    sel=3;
	else if(cbg.getSelectedCheckbox()==cbox[4])
	    sel=4;
	else if(cbg.getSelectedCheckbox()==cbox[5])
	    sel=5;
	else if(cbg.getSelectedCheckbox()==cbox[6])
	    sel=6;
	else if(cbg.getSelectedCheckbox()==cbox[7])
	    sel=7;
	else if(cbg.getSelectedCheckbox()==cbox[8])
	    sel=8;

     try {
      (new SpielerMensch(par.ip.getText(),Integer.parseInt(par.port.getText()),nam.getText(),sel)).start();
      this.setVisible(false);
      if(par.ns.warte) {
	  par.ns.setVisible(true);
      }
      else{
	  par.hs.setVisible(true);
	  par.hs.repaint();
      }
      par.ipcap.setVisible(false);
      par.portcap.setVisible(false);
      par.ip.setVisible(false);
      par.port.setVisible(false);
    } catch (Exception u){
      System.err.println(Message.say("StartSpieler","eFalPort"));
      par.statbar.setText(Message.say("StartSpieler","eFalPort"));
     }

    }
   return;
  }


}

class AfAnm extends Panel implements ActionListener{

 private Label caption=null;
 private Button fertig=null;
 private String fertCap;
 private StartSpieler par=null;

 private int xs,ys,xrr,ysp;
 public AfAnm(StartSpieler pa,String capt,/*String nc, String n,*/ String fert){
  xs=pa.x-2*pa.x1;
  ys=pa.y3-pa.y1-pa.yb-pa.yk;
  ysp=20;
  xrr=250;

  fertCap=fert;
  setSize(600,400);
  setLayout(null);
  par=pa;

  caption=new Label(capt,/*"Einen Spieler starten",*/Label.CENTER);
  add(caption);
  caption.setSize(xs,30);
//  caption.setSize(360,30);
  caption.setFont(new Font("SansSerif", Font.BOLD, 16));
  caption.setLocation(0,0);

  fertig=new Button(fertCap/*"Spieler erzeugen und anmelden"*/);
  fertig.setSize(300,30);
  fertig.setLocation(xrr,270);
//  fertig.setLocation(30,120);
  fertig.addActionListener(this);
  add(fertig);

 }

 public void actionPerformed(ActionEvent e)
  {
   if(e.getActionCommand().compareTo("Einen Ausgabekanal erzeugen und anmelden") == 0)
    {
	try {
	    /*      (new SpielerMensch(par.ip.getText(),Integer.parseInt(par.port.getText()),nam.getText(),sel)).start();*/
	    par.meineThreads[par.thrNum++]=new Ausgabe(par.ip.getText(), Integer.parseInt(par.port.getText()));
	    ((Thread)par.meineThreads[par.thrNum-1]).start();
	    /*	}catch(Exception exp){
		statbar.setText(Message.say("StartSpieler","eFalPort"));
		}*/
	    this.setVisible(false);
	    if(par.ns.warte) {
		par.ns.setVisible(true);
	    
	    }
	    else{
		par.hs.setVisible(true);
		par.hs.repaint();
	    }
	    par.ipcap.setVisible(false);
	    par.portcap.setVisible(false);
	    par.ip.setVisible(false);
	    par.port.setVisible(false);
	} catch (Exception u){
	    System.err.println(Message.say("StartSpieler","eFalPort"));
	    par.statbar.setText(Message.say("StartSpieler","eFalPort"));
	}
	
    }
   return;
  }


}


class KSp extends Panel implements ActionListener,AdjustmentListener{

 private Label caption=null;
 private Label namCap=null;
 private TextField nam=null;
 private Scrollbar anzslide=null;
 private Button fertig=null;
 private CheckboxGroup grp=null;
 private Checkbox one=null, two=null;
 private StartSpieler par=null;

 private int iq=0;
 private Label dumm=null;
 private Label schlau=null;
 private Scrollbar slide=null;
    // private TextField slval=null;
 private int xs,ys,xrr,ysp;

 public KSp(StartSpieler pa){
  par=pa;
  
  xs=pa.x-2*pa.x1;
  ys=pa.y3-pa.y1-pa.yb-pa.yk;
  ysp=20;
  xrr=250;
  //  System.out.println("Width"+xs);
  //  setSize(360,360);
  setLayout(null);

  caption=new Label(Message.say("StartSpieler","mKSSt"),Label.CENTER);
  add(caption);
  caption.setSize(xs,30);
  caption.setFont(new Font("SansSerif", Font.BOLD, 16));
  caption.setLocation(0,10);

  namCap=new Label(Message.say("StartSpieler","mAnzahl"), Label.LEFT);
  namCap.setSize(150,20);
  namCap.setFont(new Font("SansSerif", Font.PLAIN, 12));
  namCap.setLocation(xrr,60);
  add(namCap);

  nam=new TextField("2",50);
  nam.setSize(100,30);
  nam.setLocation(xrr+150,60);
  add(nam);
  nam.addActionListener(this);

        anzslide = new Scrollbar(Scrollbar.VERTICAL, 7, 1, 1, 9);
	anzslide.setSize(15,30);
	anzslide.setLocation(xrr+250,60);
	anzslide.addAdjustmentListener(this);
        add(anzslide);

  caption=new Label(Message.say("StartSpieler","mSpWerdenGest"),Label.CENTER);
  add(caption);
  caption.setSize(xs,30);
  caption.setFont(new Font("SansSerif", Font.PLAIN, 12));
  caption.setLocation(0,100);

  grp=new CheckboxGroup();
  one=new Checkbox(Message.say("StartSpieler","mLokRech"),grp,true);
  one.setSize(150,30);
  one.setLocation(xrr,140);
  add(one);
  two=new Checkbox(Message.say("StartSpieler","mServer"),grp,false);
  two.setSize(150,30);
  two.setLocation(xrr+150,140);
  add(two);

  fertig=new Button(Message.say("StartSpieler","mSpErzUanm"));
  fertig.setSize(300,30);
  fertig.setLocation(xrr,270);
  fertig.addActionListener(this);
  add(fertig);
  //////////////////////////////////////////7
  dumm=new Label(Message.say("StartSpieler","mDumm"), Label.LEFT);
  dumm.setSize(70,20);
  dumm.setFont(new Font("SansSerif", Font.PLAIN, 12));
  dumm.setLocation(xrr,200);
  add(dumm);

  //  schlau=new Label("intelligent", Label.LEFT);
  schlau=new Label(Message.say("StartSpieler","mSchlau"), Label.RIGHT);
  schlau.setSize(70,20);
  schlau.setFont(new Font("SansSerif", Font.PLAIN, 12));
  schlau.setLocation(xrr+70+200,200);
  add(schlau);

  /*  slval=new TextField("100",50);
  slval.setSize(150,30);
  slval.setLocation(180,190);
  slval.addActionListener(this);
  add(slval);*/

        //Add the slider.  It's horizontal, its initial value is 100,
        //a click increments the value by 100 pixels, and it has the
        //minimum and maximum values specified by the instance variables
        //min and max.
        slide = new Scrollbar(Scrollbar.HORIZONTAL, 150, 1, 0, 151);
	slide.setSize(200,20);
	slide.setLocation(xrr+70,200);
	//	slide.addAdjustmentListener(this);
        add(slide);

 }

    public void adjustmentValueChanged(AdjustmentEvent e){
	nam.setText(""+(9-anzslide.getValue()));
    }

    public void actionPerformed(ActionEvent e){
    if(e.getActionCommand().compareTo("Spieler erzeugen und anmelden") == 0)
	{
	    iq=150-slide.getValue();
	    //    System.out.println(""+slide.getValue()+" "+iq);
	    try{
		String ip=par.ip.getText();
		int prt=Integer.parseInt(par.port.getText());
		int anz=Integer.parseInt(nam.getText());
		if (grp.getSelectedCheckbox()==two)
		    if(par.com.newKS(ip,prt,anz,iq))
			par.statbar.setText(Message.say("StartSpieler","mSpieServSt"));
		    else
			par.statbar.setText(Message.say("StartSpieler","mStartGesch"));
		else {
		    for (int i=0;i<anz;i++){
			par.meineThreads[par.thrNum++]=new SpielerKuenstlich(ip,prt,iq);
			((Thread)par.meineThreads[par.thrNum-1]).start();
			((Thread)par.meineThreads[par.thrNum-1]).setPriority(java.lang.Thread.MIN_PRIORITY);
		    }
		    par.statbar.setText(Message.say("StartSpieler","mSpieLocSt"));
		}
		this.setVisible(false);
		if(par.ns.warte){
		    par.ns.setVisible(true);
		}
		else{
		    par.hs.setVisible(true);
		    par.hs.repaint();
		}
		    par.ipcap.setVisible(false);
		    par.portcap.setVisible(false);
		    par.ip.setVisible(false);
		    par.port.setVisible(false);
	    }catch(Exception u){
		System.err.println(Message.say("StartSpieler","eFalscheAngaben"));
		par.statbar.setText(Message.say("StartSpieler","eFalscheAngaben"));
	    }
	    
	}
    else{
	try{
	    int eing=Integer.parseInt(e.getActionCommand());
	    if (eing<=1){
		anzslide.setValue(8);
		nam.setText("1");
	    }
	    else if (eing<9) anzslide.setValue(eing);
	    else{
		anzslide.setValue(1);
		nam.setText("8");
	    }
	}
	catch(Exception u){
	    par.statbar.setText("Intelligenzfaktor soll ein Integer von 0 bis 100 sein!");
	}
    }
    return;
    }
    

}



///////////
class RraFilter implements FilenameFilter{

    public RraFilter(){}

    public boolean accept(File dir, String name){
	try{
	    if (name.substring(name.length()-4,name.length()).equals(".rra")) return true;
	} catch(Throwable t){return false;}
	return false;
    }

}







 class Flagerr extends Dialog implements WindowListener,
                                              ActionListener{
 private Button select=null;
 private TextArea msg=null;

     // public Flagerr(String prob){
     //  super("Ungünstige Flaggenposition");
  public Flagerr(StartSpieler sp, String prob){
      super(sp);
  setTitle("Ungünstige Flaggenposition");

  setSize(600,130);
  setLocation(200,200);
  setResizable(false);
  setLayout(null);
  
  setModal(true);

  /*  msg=new Label(prob,Label.CENTER);
  msg.setSize(600,30);
  msg.setLocation(0,30);
  msg.setFont(new Font("Serif", Font.PLAIN, 11));
  add(msg);*/

  msg=new TextArea(prob,1,30, TextArea.SCROLLBARS_VERTICAL_ONLY);
  msg.setSize(550,60);
  msg.setLocation(25,30);
  msg.setEditable(false);
  msg.setFont(new Font("SansSerif", Font.PLAIN, 11));
  add(msg);

  select=new Button("OK");
  select.setSize(70,30);
  select.setLocation(265,95);
  select.addActionListener(this);
  add(select);

  addWindowListener(this);
  show();
  repaint();

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
 public void actionPerformed(ActionEvent e)  {
   if(e.getActionCommand().equals("OK"))
       this.dispose();
 }
 }

/*import java.awt.*; 
import java.awt.event.*;
import java.net.*;
import java.io.*;
*/
class StSpLis extends Thread{

 public String name=null;
 public int farbe=0;
 public RobiCanvas par=null;

 private ServerSocket srv=null;
 private  Socket clt =null;
 private  InputStream inp =null;
 private  BufferedReader br=null;
 private  OutputStream outp=null;
 private  PrintWriter pw=null;

 int PORTNR=8889;
 private String fromclt=null;

 public StSpLis(RobiCanvas r) {
     boolean gotit=false;
     for (int i=PORTNR;(i<PORTNR+10)&&(!gotit);i++)
     try{
	 srv = new ServerSocket(i);
	 par=r;
	 gotit=true;
	 PORTNR=i;
     }catch(Exception e){
	 System.out.println("Kann ServerSocket nicht öffnen:"+e+"\n probiert: "+i);
     }

 }

    public void run(){
	while(true)
	    if (listen()){
		//	par.neurob(name,farbe);
		ok();
	    } 
	    else
		error();
	
    }


public boolean listen(){


  try{
   clt = srv.accept();
   clt.setSoTimeout(30000);
   //System.out.println("Ein Klient!");
  } catch (Exception e){System.err.println("Kann nicht ACCEPT!");}

  try{
   pw = new PrintWriter(new OutputStreamWriter(clt.getOutputStream()), true);
  } catch (Exception e){
   System.err.println("Kann nicht getOutputStream!");}

  try{
   br= new BufferedReader(new InputStreamReader(clt.getInputStream()));
  } catch (Exception e)
  {System.err.println("Kann nicht getInputStream!");}

  try{
    pw.println("StartSpielerListener ist bereit.");
    //if(br.ready())
    fromclt=br.readLine();
  } catch (Exception e){System.err.println("Kann nicht println/readLine!");}
  //System.out.println(fromclt.substring(0,4));

  try{
   if(fromclt.substring(0,3).equals("NSA")){//neuerSpielerAngemeldet
    name=br.readLine(); //SpielerName

    fromclt=br.readLine();
    farbe=Integer.parseInt(fromclt); //Farbe als Zahl zw. 1 u. 7
    if((farbe>=0)&&(farbe<=7)){
	par.neurob(name,farbe);
	return true; //alles war OK
    }
    else return false;
   }
   else{
       if(fromclt.substring(0,3).equals("SGL")) //SpielGehtLos
      	  par.spGL();
       return true;
   }
  } catch (Exception e){
    System.err.println("Klientenfehler!"+e);
  }
  return false; //fehler ist aufgetreten
  }


 public void ok(){
  pw.println("OK.");
  try{
//   srv.close();
   clt.close();
  } catch (Exception e){
   System.err.println("Kann die Sockets nicht schliessen!");
  }
 }

 protected void closeSock(){
  try{
//   srv.close();
   clt.close();
  } catch (Exception e){
   System.err.println("Kann die Sockets nicht schliessen!");
  }
 }


 public void error(){
  pw.println("error.");
  try{
      //   srv.close(); 
   clt.close();
  } catch (Exception e){
   System.err.println("Kann die Sockets nicht schliessen!");
  }
 }
}















