package de.botsnscouts.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.JWindow;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.ToolTipManager;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.ColoredPanel;
import de.botsnscouts.widgets.GreenTheme;
import de.botsnscouts.widgets.OptionPane;
import de.botsnscouts.widgets.PaintPanel;
import de.botsnscouts.widgets.TJLabel;
import de.botsnscouts.widgets.TJPanel;


public class StatusRobot extends JButton {
    int lifesLeft;
    Icon bigBot, smallBot;
    Bot robot;

    boolean turnPicAccordingToFacing = true;
    
    // The following three objects are needed for the "advanced tooltip" this class has:
    // it should not only show a text ("click robot to..") but also show the robots registers
    // (including the current cards)     
    private PaintPanel registerPanel= new PaintPanel(OptionPane.getBackgroundPaint(this), true);    
    //private ColoredPanel registerPanel= new ColoredPanel(GreenTheme.getBnsBackgroundColor());
    private ScalableRegisterRow registers;
    private MouseListener customTooltipTrigger;
    
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
    
  
    protected void notifyOfRobRemoval(){
        
        removeCustomToolTipMouseAdapter();
        setRoboRegisters(null);
    }
	

    public void setRoboRegisters(ScalableRegisterRow robsRegisters){
        if (robsRegisters == null) {
            this.registers = null;
        }
        else if (this.registers == null) {       
            this.registers = robsRegisters;                
          //  ColoredPanel registerPanel = new ColoredPanel();
          //  registerPanel.setOpaque(false);        
          //  registerPanelBg.add(registerPanel); // hack to get a darker background
            registerPanel.setLayout(new BorderLayout());
            Border inner = BorderFactory.createEmptyBorder(5,5,5,5);
            Border outer = BorderFactory.createEtchedBorder(EtchedBorder.RAISED, Color.GRAY, Color.GRAY);
            Border both = BorderFactory.createCompoundBorder(outer,inner);            
            registerPanel.setBorder(both);
          // Border inner2 =  BorderFactory.createEmptyBorder(10,0,0,0);
             Border outer2 = BorderFactory.createMatteBorder(2,0,0,0,Color.GRAY);
         //   registers.setBorder(BorderFactory.createCompoundBorder(outer2,inner2));  
             registers.setBorder(outer2);
             registers.setInsets(new Insets(10,5,5,5));
             TJLabel pseudoTooltipText = new TJLabel(
                            Message.say("RobotInfo", "botPos", robot.getName())
                            );
           
            registerPanel.add(pseudoTooltipText, BorderLayout.NORTH);                                   
            registerPanel.add(registers, BorderLayout.CENTER);
            customTooltipTrigger = new ToolTipAdapter(/*this,*/ registerPanel,0,20);
       
            this.addMouseListener(customTooltipTrigger);
        }
        
    }
    
    private void removeCustomToolTipMouseAdapter(){
        this.removeMouseListener(customTooltipTrigger);
    }
   
    class ToolTipAdapter extends MouseAdapter {  
        
        private Popup registerPopup;
      //  private JComponent popupOwner;
        private JComponent compToShow;
        private int xoff;
        private int yoff;
        private PopupFactory popFactory = PopupFactory.getSharedInstance();
        
        public ToolTipAdapter(/*JComponent owner, */JComponent toShow, int yoff, int xoff){
        //    popupOwner = owner;
            compToShow = toShow;
            this.xoff = xoff;
            this.yoff = yoff;
        }
        
        public void 	mouseEntered(MouseEvent e){
            int x = xoff;
            int y  = yoff;            
            Point relativePoint = e.getPoint();
            x+=relativePoint.x;
            y+=relativePoint.y;
            
            Component source = e.getComponent();
            if (source != null) {
                Point p = source.getLocationOnScreen();
                x+=p.x;
                y+=p.y;
            }
            registerPopup = popFactory.getPopup(source, compToShow,x,y);            
            registerPopup.show();
         }
        
        public void 	mouseExited(MouseEvent e) {
                registerPopup.hide();
        
        }
        
        
        
        
    }

}
