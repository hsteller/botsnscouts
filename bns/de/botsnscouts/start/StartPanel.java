package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import java.io.*;
import de.botsnscouts.util.*;
//import de.botsnscouts.board.*;

public class StartPanel extends JPanel implements  ActionListener, MouseListener{
    Paint paint;
    Start parent;
    Thread thread;

    JLabel angem;
    StartStartAnmeldung anmeldung;
    StartStartOkZur unten;
    StartStartKS ks;
    StartStartMS ms;
    StSpListener listen;

    public StartPanel(Start par){
	parent=par;
	paint=parent.paint;
	Font font=new Font("Sans", Font.BOLD, 24);

	BorderLayout lay=new BorderLayout();

	setLayout(lay);
	setBorder(new EmptyBorder(50,50,50,50));
	setOpaque( false );

	angem=new JLabel(Message.say("Start","mAngem"));
	anmeldung=new StartStartAnmeldung(parent);
	unten=new StartStartOkZur(parent);
        ks=new StartStartKS(parent);
        ms=new StartStartMS(parent);
	listen=new StSpListener(anmeldung);
	listen.start();

	angem.setFont(font);
	//angem.setForeground(Color.lightGray);
	JPanel p = new JPanel(new BorderLayout());
	p.setOpaque( false );
	p.add( angem, BorderLayout.NORTH );
	p.add( anmeldung, BorderLayout.CENTER );
	//add(BorderLayout.NORTH,angem);
	add(BorderLayout.WEST,p);
	add(BorderLayout.SOUTH,unten);
	JPanel panel=new TJPanel();

	panel.setLayout( new GridBagLayout() );
	GridBagConstraints gc = new GridBagConstraints();
	gc.anchor = GridBagConstraints.NORTH;
	gc.fill = GridBagConstraints.BOTH;
	gc.gridx = 0;
	gc.gridy = GridBagConstraints.RELATIVE;
	gc.insets = new Insets(30, 30, 30, 30);
	
	panel.add(ks, gc);
	panel.add(ms, gc);
	add(BorderLayout.EAST,panel);

// 	ImageIcon icon = ImageMan.getIcon(Message.say("Start","mBG"));
// 	BufferedImage bgimg = new BufferedImage( icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB );
// 	Graphics g = bgimg.getGraphics();
// 	icon.paintIcon(this, g, 0,0);
// 	g.dispose();
// 	Rectangle2D anchor = new Rectangle2D.Float(0f,0f, icon.getIconWidth(), icon.getIconHeight());
// 	paint = new TexturePaint( bgimg, anchor );
    }

    public void actionPerformed(ActionEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseClicked(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}

    public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	Dimension d = getSize();
	g2d.setPaint( paint );
	g2d.fillRect(0,0, d.width, d.height);
	paintChildren(g);
    }

    public void setThreadToWait(Thread th){
	thread=th;
    }

}//class StartStart end

class StSpListener extends Thread{
    
    public String name=null;
    public int farbe=0;
    public StartStartAnmeldung par=null;
    
    private ServerSocket srv=null;
    private  Socket clt =null;
    private  InputStream inp =null;
    private  BufferedReader br=null;
    private  OutputStream outp=null;
    private  PrintWriter pw=null;
    
    int PORTNR=8889;
    private boolean torun=true;
    private String fromclt=null;
    
    public StSpListener(StartStartAnmeldung r) {
	boolean gotit=false;
	for (int i=PORTNR;(i<PORTNR+10)&&(!gotit);i++)
	    try{
		srv = new ServerSocket(i);
		par=r;
		gotit=true;
		PORTNR=i;
	    }catch(Exception e){
		System.err.println("StSpListener: Kann ServerSocket nicht öffnen:"+e+"\nprobiert: "+i);
	    }
	
    }
    
    public void run(){
	while(torun){
	    if (listen()){
		//	par.neurob(name,farbe);
		ok();
	    } 
	    else
		error();
	}
	Global.debug(this,"Habe ende meiner run() methode erreicht");
    }
    
    
    public boolean listen(){
	try{
	    clt = srv.accept();
	    clt.setSoTimeout(30000);
	    //System.out.println("Ein Klient!");
	} catch (Exception e){System.err.println("StSpListener: Kann nicht ACCEPT!");}
	
	try{
	    pw = new PrintWriter(new OutputStreamWriter(clt.getOutputStream()), true);
	} catch (Exception e){
	    System.err.println("StSpListener: Kann nicht getOutputStream!");}
	
	try{
	    br= new BufferedReader(new InputStreamReader(clt.getInputStream()));
	} catch (Exception e)
	    {System.err.println("StSpListener: Kann nicht getInputStream!");}
	
	try{
	    pw.println("StartSpielerListener ist bereit.");
	    //if(br.ready())
	    fromclt=br.readLine();
	} catch (Exception e){System.err.println("StSpListener: Kann nicht println/readLine!");}
	//System.out.println(fromclt.substring(0,4));
	Global.debug(this,"empfange "+fromclt);	
	try{
	    if(fromclt.substring(0,3).equals("NSA")){//neuerSpielerAngemeldet
		name=br.readLine(); //SpielerName
		Global.debug(this,"Neuer Spieler! "+name);//!!!!!
		fromclt=br.readLine();
		farbe=Integer.parseInt(fromclt); //Farbe als Zahl zw. 1 u. 7
		if((farbe>=0)&&(farbe<=7)){
		    par.neurob(name,farbe);
		    return true; //alles war OK
		}
		else return false;
	    }  else if(fromclt.substring(0,3).equals("SGL")){ //SpielGehtLos
		par.spGL();
		return true;
	    }  else if(fromclt.substring(0,3).equals("SZE")){ //SpielZuEnde
		par.spZE();
		torun=false;
		return true;
	    } 

	} catch (Exception e){
	    System.err.println("StSpListener: Klientenfehler!"+e);
	}
	return false; //fehler ist aufgetreten
    }
    
    
    public void ok(){
	pw.println("OK.");
	try{
	    //   srv.close();
	    clt.close();
	} catch (Exception e){
	    System.err.println("StSpListener: Kann die Sockets nicht schliessen!");
	}
    }
    
    protected void closeSock(){
	try{
	    srv.close();
	    torun=false;
	    clt.close();
	} catch (Exception e){
	    try{
		clt.close();
	    }catch (Exception ex){
		System.err.println("StSpListener: Kann die Sockets nicht schliessen!"+ex);
	    }
	    System.err.println("StSpListener: Kann die Sockets nicht schliessen!"+e);
	}
    }

    
    public void error(){
	if (pw!=null){
	    pw.println("error.");
	}
	try{
	    //   srv.close(); 
	    clt.close();
	} catch (Exception e){
	    System.err.println("StSpListener: Kann die Sockets nicht schliessen!");
	}
    }
}
