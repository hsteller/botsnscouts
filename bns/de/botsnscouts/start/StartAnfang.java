package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.border.*;
import java.net.*;
import de.botsnscouts.util.*;
public class StartAnfang extends JPanel implements  ActionListener, MouseListener{
    Paint paint;

    TransparentButton starten;
    TransparentButton teilnehmen;
    TransparentButton zuschauen;
    TransparentButton beenden;
    JLabel logo;

    Start parent;

    public StartAnfang(Start par){
	parent=par;
	parent.setTitle(Message.say("Start","mStartTitel"));
	paint=parent.paint;

	GridLayout lay=new GridLayout(4,1);
	lay.setHgap(170);
	lay.setVgap(20);
	JPanel buttons = new JPanel( lay );
	buttons.setOpaque( false );
	setLayout( new GridBagLayout() );

	logo = new Logo();

	starten=new TransparentButton(Message.say("Start","mSpielStarten"));
	teilnehmen=new TransparentButton(Message.say("Start","mTeilnehmen"));
	zuschauen=new TransparentButton(Message.say("Start","mZuschauen"));
	beenden=new TransparentButton(Message.say("Start","mBeenden"));

	starten.addActionListener(this);
	teilnehmen.addActionListener(this);
	zuschauen.addActionListener(this);
	beenden.addActionListener(this);

	starten.setActionCommand("starten");
	teilnehmen.setActionCommand("teilnehmen");
	zuschauen.setActionCommand("zuschauen");
	beenden.setActionCommand("beenden");
	
	buttons.add(starten);
	buttons.add(teilnehmen);
	buttons.add(zuschauen);
	buttons.add(beenden);

	GridBagConstraints con = new GridBagConstraints();
	buttons.setBorder( new EmptyBorder(50, 20, 50, 20));
	con.gridx = 0;
	con.gridheight = 2;
	con.weightx = 0.1;
	con.weighty = 0.1;
	add( logo, con );
	con.gridheight = GridBagConstraints.REMAINDER;
	con.fill = GridBagConstraints.VERTICAL;
	con.insets = new Insets(50, 20, 50, 20);
	con.anchor = GridBagConstraints.CENTER;
	con.weighty = 0.2;
	con.weightx = 0.0;
	add( buttons, con );

// 	URL url = de.botsnscouts.BotsNScouts.class.getResource(Message.say("Start","mBG"));
// 	ImageIcon icon = new ImageIcon( url );
// 	BufferedImage bgimg = new BufferedImage( icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB );
// 	Graphics g = bgimg.getGraphics();
// 	icon.paintIcon(this, g, 0,0);
// 	g.dispose();
// 	Rectangle2D anchor = new Rectangle2D.Float(0f,0f, icon.getIconWidth(), icon.getIconHeight());
// 	paint = new TexturePaint( bgimg, anchor );

    }

    public void actionPerformed(ActionEvent e){
	if(e.getActionCommand().equals("beenden")){
	    parent.myclose();
	}else if(e.getActionCommand().equals("starten")){
	    if(parent.startSpielfeld==null){
		parent.startSpielfeld=new StartSpielfeld(parent);
	    }
	    parent.current=parent.startSpielfeld;
	    parent.setContentPane(parent.current);
	    parent.show();
	}else if(e.getActionCommand().equals("teilnehmen")){
	    if(parent.startTeilnehmen==null){
		parent.startTeilnehmen=new StartTeilnehmen(parent);
	    }
	    parent.current=parent.startTeilnehmen;
	    parent.setContentPane(parent.current);
	    parent.show();
	}else if(e.getActionCommand().equals("zuschauen")){
	    if(parent.startZuschauen==null){
		parent.startZuschauen=new StartZuschauen(parent);
	    }
	    parent.current=parent.startZuschauen;
	    parent.setContentPane(parent.current);
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


}//class StartAnfang end
