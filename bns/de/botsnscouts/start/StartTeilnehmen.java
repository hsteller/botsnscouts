package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import de.botsnscouts.util.*;

public class StartTeilnehmen extends JPanel implements  ActionListener, MouseListener{

    JLabel server;    
    JLabel port;    
    JLabel name;    
    JLabel farbe;    

    JTextField serv;
    JTextField prt;
    JTextField nam;
    JComboBox farb;

    TransparentButton go;
    TransparentButton zurueck;

    Start parent;

    Paint paint;

    Font font;

    public StartTeilnehmen(Start par){
	parent=par;
	parent.setTitle(Message.say("Start","mTeilnehmen"));
	paint=parent.paint;
	
	String[] farben={Message.say("Start","mFarbeEgal"),Message.say("Start","mFarbeGruen"),Message.say("Start","mFarbeGelb"),Message.say("Start","mFarbeRot"),Message.say("Start","mFarbeBlau"),Message.say("Start","mFarbeMagenta"),Message.say("Start","mFarbeOrange"),Message.say("Start","mFarbeGrau"),Message.say("Start","mFarbeDunkelMagenta")};
	font=new Font("Sans", Font.BOLD, 24);

	GridLayout lay;
	lay=new GridLayout(5,2);
	lay.setHgap(170);
	lay.setVgap(30);

	setLayout(lay);
	setBorder(new EmptyBorder(50,50,50,50));
	
	server=new JLabel(Message.say("Start","mServer"));
	port=new JLabel(Message.say("Start","mPort"));
	name=new JLabel(Message.say("Start","mName"));
	farbe=new JLabel(Message.say("Start","mFarbe"));
        serv=new JTextField(Message.say("Start","mServerInh"),JTextField.CENTER);
        prt=new JTextField(Message.say("Start","mPortInh"),JTextField.CENTER);
        nam=new JTextField(KrimsKrams.randomName(),JTextField.CENTER);
	farb=new RoboBox( true ); //new JComboBox(farben);
        go=new TransparentButton(Message.say("Start","mGoButton"));
        zurueck=new TransparentButton(Message.say("Start","mZurueckButton"));

	server.setFont(font);
	port.setFont(font);
	name.setFont(font);
	farbe.setFont(font);
        serv.setFont(font);
        prt.setFont(font);
	nam.setFont(font);
	farb.setFont(font);

	serv.setOpaque(false);
        prt.setOpaque(false);
	nam.setOpaque(false);
	
	/*
	farb.setLightWeightPopupEnabled(false);
	farb.setRenderer( new MyCellRenderer());
	farb.setOpaque(false);
	*/

	server.setForeground(Color.lightGray);
	port.setForeground(Color.lightGray);
	name.setForeground(Color.lightGray);
	farbe.setForeground(Color.lightGray);

        go.addActionListener(this);
        zurueck.addActionListener(this);

        go.setActionCommand("go");
        zurueck.setActionCommand("zurueck");

	add(server);
        add(serv);
	add(port);
        add(prt);
        add(name);
        add(nam);
        add(farbe);
        add(farb);
        add(go);
        add(zurueck);


// 	URL url = getClass().getResource(Message.say("Start","mBG"));
// 	ImageIcon icon = new ImageIcon( url );
// 	BufferedImage bgimg = new BufferedImage( icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB );
// 	Graphics g = bgimg.getGraphics();
// 	icon.paintIcon(this, g, 0,0);
// 	g.dispose();
// 	Rectangle2D anchor = new Rectangle2D.Float(0f,0f, icon.getIconWidth(), icon.getIconHeight());
// 	paint = new TexturePaint( bgimg, anchor );

    }

    public void actionPerformed(ActionEvent e){
	if(e.getActionCommand().equals("go")){
//	    System.out.println(farb.getSelectedIndex());
	    int portnr;
	    try{
		portnr=Integer.parseInt(prt.getText());
	    }catch (Exception x){
		System.err.println("Must be a number!");
		return;
	    }

	    Thread smth=parent.fassade.amSpielTeilnehmen(serv.getText(),portnr,nam.getText(),farb.getSelectedIndex());
	    Global.debug(this,"SpielerMensch gestartet");
	    parent.addKS(smth);
//	    Thread spMensch=parent.fassade.amSpielTeilnehmen(serv.getText(),portnr,nam.getText(),farb.getSelectedIndex());
//	    parent.addKS(spMensch);
// 	    if(spMensch!=null){
// 		parent.hide();
// 		try{
// 		    spMensch.join();
// 		}catch(InterruptedException ex){
// 		    System.err.println("Interrupted while waiting for SpielerMensch");
// 		}
// 		parent.show();
//	    }
	    parent.hide();
	    parent.dispose();
	    parent.beenden();
	}else if(e.getActionCommand().equals("zurueck")){
	    parent.setContentPane(parent.startAnfang);
	    parent.setTitle(Message.say("Start","mStartTitel"));

	    parent.startAnfang.starten.getModel().setRollover(false);
	    parent.startAnfang.teilnehmen.getModel().setRollover(false);
	    parent.startAnfang.zuschauen.getModel().setRollover(false);
	    parent.startAnfang.beenden.getModel().setRollover(false);
	    parent.show();
	}

    }

    public void mouseEntered(MouseEvent e){
	
    }

    public void mouseExited(MouseEvent e){

    }


    public void mouseClicked(MouseEvent e){

    }

    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}

    public void paint(Graphics g) {
	Graphics2D g2d = (Graphics2D) g;
	Dimension d = getSize();
	g2d.setPaint( paint );
	g2d.fillRect(0,0, d.width, d.height);
	paintChildren(g);
    }

}//class StartTeilZusch end
