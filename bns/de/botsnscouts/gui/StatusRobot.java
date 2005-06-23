package de.botsnscouts.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolTip;

import de.botsnscouts.util.Bot;


public class StatusRobot extends JButton {
    int lifesLeft;
    Icon bigBot, smallBot;
    Bot robot;

    boolean turnPicAccordingToFacing = true;
    
    public StatusRobot( Icon bigBot, Icon smallBot ) {
        this( bigBot, smallBot, null );
    }

    public StatusRobot( Icon bigBot, Icon smallBot, Bot r ) {
        lifesLeft = 2;
       
        if (turnPicAccordingToFacing)
            this.bigBot = new ImageIcon(RobotInfo.roboImages[r.getBotVis()*4+r.getFacing()]);
        else
            this.bigBot = bigBot;
        this.smallBot = smallBot;
        this.robot = r;
        setBorderPainted( false );
        setOpaque( false );
        setRolloverEnabled( false );
        setFocusable(false);
    }

    public StatusRobot() {
        this( new ImageIcon( RobotInfo.roboImages[0] ),
            new ImageIcon( RobotInfo.cursors[0] ) );
    }


    public void setLifesLeft(int lifes) {
        lifesLeft = lifes;
    }

    public int getLifesLeft() {
        return lifesLeft;
    }

    public void paintComponent(Graphics _g) {
        Graphics2D g = (Graphics2D) _g;
        Composite comp = g.getComposite();
        int w = bigBot.getIconWidth() - 22;

        if( robot != null && !robot.isActivated() ) {
            g.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
            ImageIcon icon = new ImageIcon( RobotInfo.getOffSwitch() );
            icon.paintIcon(this, g, w-8, 15);
        }
        else
            g.setComposite( AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.86f));
        
        
        bigBot.paintIcon(this, g, -10, -10);
        

        if( lifesLeft == 2 ) smallBot.paintIcon(this, g, w, 6);
        if( lifesLeft >= 1 ) smallBot.paintIcon(this, g, w, smallBot.getIconHeight()-3);
        if( robot != null && !robot.isActivated() ) {
            g.setComposite( comp );
        }
    }

    public Dimension getMinimumSize() {
        int w = smallBot.getIconWidth() + bigBot.getIconWidth() - 20;
        int h = bigBot.getIconHeight() - 20;
        return new Dimension( w, h );
    }

    public Dimension getPreferredSize() {
        return getMinimumSize();
    }
    // zum Display
    public static void main(String[] args) {
        JFrame frame = new JFrame("damage");
        frame.setSize(400, 400);
        frame.getContentPane().setLayout( new BorderLayout() );
        Box b = new Box(BoxLayout.Y_AXIS);
        for(int i=0; i<6; i++) {
            Icon big = new ImageIcon( RobotInfo.roboImages[i*4] );
            Icon small = new ImageIcon( RobotInfo.cursors[i] );
            final StatusRobot db = new StatusRobot( big, small );
            db.setBorder( BorderFactory.createLineBorder(Color.black) );
            db.setSize( db.getPreferredSize() );
            JPanel p = new JPanel();
            p.add( db );
            b.add( p );
            db.addMouseListener( new MouseAdapter() {
                public void mouseClicked( MouseEvent me ) {
                    int l = db.getLifesLeft();
                    db.setLifesLeft( (l+1) % 3 );
                    db.repaint();
                }
            });
        }
        frame.getContentPane().add( b, BorderLayout.CENTER );
        frame.setVisible( true );
    }
    
    protected void updateRobot (Bot newValues/*, ImageIcon bigIcon*/){
        robot = newValues;
        
    
        if (turnPicAccordingToFacing)
            bigBot= new ImageIcon(RobotInfo.roboImages[robot.getBotVis()*4+robot.getFacing()]);
        repaint();
    }
    
  
    
	public void setToolTipText(String text) {
	    if(registerToolTip!=null) {
	        //registerToolTip.setTipText(text);
	        registerToolTip.setTheText(text);
	       
	    }
	    
	  super.setToolTipText(""); // DON'T ASK!!
	  // 1. without calling this method (or passing null) there is no Tooltip, due to 
	  //     Java's internal working (ToolTipManager registration
	  // 2. WITH CALLING: 
	  //     the text will be shown..but only if the JLabel registerToolTip.text
	  //     is set, resulting in the text being shown TWIC
	 // 
	  // I mean, WHAT THE F____ ?!?!??!?
	  //
	  // don't get me started on the influence an uncommented getToolTipText() might have..
	   
	        
	} 
	
/*
	public String getToolTipText() {
	 
	   if(registerToolTip!=null) {
	        return registerToolTip.getTipText();
	    }
	    else  {
	        return super.getToolTipText();
	    }
	}
*/

    
    private RegisterToolTip registerToolTip = null;
    public void setToolTip(RegisterToolTip regTip, boolean useOldText){
   /*  if (useOldText && regTip != null) {
             String oldTip = getToolTipText();
             regTip.setTipText(oldTip);
             
     }*/
         registerToolTip = regTip;
         //super.setToolTipText(regTip.getTipText());
    }
   
    public JToolTip createToolTip(){       
        //return new RegisterToolTip(new ScalableRegisterRow(0.3));
      if (registerToolTip == null) {
            return null;//super.createToolTip();
            
        }
        else {
            return registerToolTip;
        }
   }
    

}