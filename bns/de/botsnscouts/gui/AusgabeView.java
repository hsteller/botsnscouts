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

package de.botsnscouts.gui;

import de.botsnscouts.util.*;


import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

import org.apache.log4j.Category;

public class AusgabeView extends JPanel implements AusgabeViewInterface {
    static Category CAT = Category.getInstance(AusgabeView.class);

    private final static long SHOW_MESSAGE_DELAY=300;

    // --- objects
    private JScrollPane gameBoardScrollPane;
    private JViewport gameBoardView;
    private SACanvas  gameBoardCanvas;
    private Ausgabe   ausgabe;
    private Hashtable robotStatus = new Hashtable(8);
    private Hashtable robotCardStatus = new Hashtable(8);
    private StatusLog statusLog;
    private JMenuBar menus = new JMenuBar();

    private JComponent northPanel = new PaintPanel( OptionPane.getBackgroundPaint(this) );

    // settings for Zoom-Menu
    protected static final int MIN_ZOOM = 40;
    protected static final int DEFAULT_ZOOM = 100; // selects the RadioButton to
                                                   // be selected as default, does not
                                                   // influence the default zoom; that
                                                   // will be always 100
    protected static final int MAX_ZOOM = 150;
    protected static final int ZOOM_STEP = 10;

    // sound-menu
    private boolean soundOn = false;

    // speed-menu
    protected final int SLOW = 2000;
    protected final int MEDIUM = 200;
    protected final int FAST = 0;

    protected int speed=MEDIUM;

    public AusgabeView() {
        super();
        this.initMenus();
    }

    public AusgabeView(SACanvas sa, Bot[] robots, Ausgabe aus) {
	ausgabe = aus;
	gameBoardCanvas=sa;
//        statusLog = new StatusLog(aus.getView());

	JPanel robotsStatusContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        robotsStatusContainer.setOpaque(false);
//        Box robotsStatusContainer = new Box(BoxLayout.X_AXIS) {
//            public void paint(Graphics g) {
//                g.setColor(Color.black);
//                g.fillRect(0,0,getWidth(), getHeight());
//                super.paint(g);
//            }
//        };
	JPanel robotsCardContainer = new JPanel(new GridLayout(8,1));
        int flagCount = sa.sf.getFlags().length;

        setLayout(new BorderLayout());

	// create status panel
	for (int i=0; i< robots.length; i++) {
            final String robotName = robots[i].getName();
	    RobotInfo r = new RobotInfo(robots[i], flagCount );
            r.addRobotInfoListener( new RobotInfoListener() {
                        public void robotClicked(RobotInfoEvent rie) {
                            CAT.debug("tracking rob: " + robotName );
                            ausgabe.trackRob(robotName);
                        }
                        public void flagClicked(RobotInfoEvent rie) {
                            RobotInfo ri = (RobotInfo)rie.getSource();
                            ausgabe.scrollFlag(ri.getRobot().getNextFlag());
                        }
                        public void diskClicked(RobotInfoEvent rie) {
                            Bot robot = ((RobotInfo)rie.getSource()).getRobot();
                            ausgabe.trackPos(robot.getArchiveX(), robot.getArchiveY(), true);
                        }
             });
	    robotsStatusContainer.add(r);
	    robotStatus.put(robots[i].getName(),r);

            // add entry to track menu
          //  JMenuItem trackItem = new JMenuItem( r.getName() );
           // trackItem.addActionListener( new HumanPlayer.RoboTrackListener(robots[i]) );
    	   // optTrack.add(trackItem);

	    RobotCard rc= new RobotCard(robots[i]);
	    robotsCardContainer.add(rc);
            robotsCardContainer.setOpaque( false );
	    robotCardStatus.put(robots[i].getName(),rc);
	}

        this.northPanel.setLayout(new BorderLayout());
        northPanel.setOpaque( false );
        northPanel.add(robotsStatusContainer, BorderLayout.WEST);
	//this.add(northPanel, BorderLayout.NORTH);
        // before splitpane: add(robotsStatusContainer,BorderLayout.NORTH);
//	add(robotsCardContainer,BorderLayout.EAST);


	// create status log
//	add(statusLog,BorderLayout.SOUTH);

	// create scroll panel
	gameBoardScrollPane = new JScrollPane();
	gameBoardScrollPane.getHorizontalScrollBar().setUnitIncrement(64);
	gameBoardScrollPane.getVerticalScrollBar().setUnitIncrement(64);
	gameBoardScrollPane.setViewportView(gameBoardCanvas);
        gameBoardScrollPane.setBorder( BorderFactory.createEmptyBorder(5,5,5,5) );
	gameBoardCanvas.setScrollPane(gameBoardScrollPane);
	add(gameBoardScrollPane,BorderLayout.CENTER);
	gameBoardView = gameBoardScrollPane.getViewport();
        this.initMenus();

    }

    protected void addWiseAndScout(JPanel p){
        CAT.debug("adding WiseAndScout to Northpanel/EAST");
        northPanel.add(p, BorderLayout.EAST);
    }

    protected JComponent getNorthPanel(){
      return northPanel;
    }
    /*
    public void shutup() {
	System.exit(0);
    }
*/


    /**
     * show the sinlge line action message that came from the server
     */
    public void showActionMessage(String s){
        ausgabe.getView().logFloatPane.addMessage( s );
//	statusLog.addMessage(s);
        synchronized(this){
          try {
            wait(SHOW_MESSAGE_DELAY);
          }
         catch (InterruptedException ie){
          CAT.debug("interrupted!");
         }
        }
    }


    /**
     * Schreibt in die Statuszeile einen Text
     */
    public void showRobStatus(Bot r){
    }


    /**
     * Shows the new Positions of the Robots
     */
    public void showUpdatedRobots(Bot[] r){
	gameBoardCanvas.ersetzeRobos(r);
	for (int i = 0; i < r.length; i++) {
	    ((RobotStatus) robotStatus.get(r[i].getName())).updateRobot(r[i]);
	    ((RobotCard) robotCardStatus.get(r[i].getName())).updateRobot(r[i]);
	    // RobCards aktualisieren TODO
	}
    }

    public void showPixelPos(int x, int y ) {
        CAT.debug(" scroll request: " + x + " " + y );
        Dimension d = gameBoardView.getExtentSize();
        Rectangle r = new Rectangle( x-(d.width/2), y- (d.width/2), d.width, d.height );
        CAT.debug(" corresponding rect: " + r );
        gameBoardView.scrollRectToVisible( r );
        gameBoardCanvas.scrollRectToVisible( r );
    }

    public void showPos(int robix, int robiy ) {
        showPos(robix, robiy, true);
    }

    public void showPos(int robix, int robiy, boolean highlight) {
        Point p = gameBoardCanvas.ort2Point( robix, robiy, new Point());
//	int x = robix*gameBoardCanvas.getScaledFeld;
//	int y = gameBoardCanvas.getHeight()-(robiy*64);
        int x = p.x;
        int y = p.y;

	Dimension sz = gameBoardView.getExtentSize();
	int w2 = sz.width/2;
	int h2 = sz.height/2;


	// make sure we dont want to scoll 'out' to
	// the left and top
	int x1 = Math.max( x - w2 , 0);
	int y1 = Math.max( y - h2 , 0);

	// soll ich \uFFFDberhaupt scrollen?
	// in X-Richtung
	if ((x < ( (gameBoardView.getViewPosition().x)+10 ) ) ||
	    x > ( (gameBoardView.getViewPosition().x+sz.width)-10 )) {
	    x1 = Math.min( x1, (gameBoardCanvas.getWidth() - sz.width) );
	}
	else x1 = gameBoardView.getViewPosition().x;

	// in Y-Richtung
	if ((y < ( gameBoardView.getViewPosition().y +10) ) ||
	    y > ( (gameBoardView.getViewPosition().y+sz.height)-10 )) {
	    y1 = Math.min( y1, (gameBoardCanvas.getHeight() - sz.height) );
	}
	else y1 = gameBoardView.getViewPosition().y;

	gameBoardView.setViewPosition(new Point(x1, y1));
        if( highlight ) this.gameBoardCanvas.highlight(robix, robiy);
    }


    /**
     * board view is to paint robolaser activity
     */
    public void showRobLaser(Bot von, Bot nach){
	gameBoardCanvas.doRobLaser(von, nach);
    }

    /**
     * board view is to paint bord laser activity
     */
    public void showBoardLaser(Location laserPos, int facing, int stregth, Location r1Pos){
	gameBoardCanvas.doBordLaser(laserPos, facing, stregth, r1Pos,gameBoardView);
    }

    /**
     *  shows the winner state of the game in the mids of the game
     */
    public void showWinnerState (String[] gameState) {
	int i=0;
	while (i<gameState.length&&!gameState[i].equals("null")) {
	    d("getting ranking of "+gameState[i]+" zu holen");
	    ((RobotStatus) robotStatus.get(gameState[i])).setWinnerNumber((i+1));

	    i++;
	}
    }


    /**
     *  shows the winner list at game over
     */
    public void showWinnerlist (String[] winners) {
	gameBoardCanvas.setVisible(false);
	add(new Abspann(winners),BorderLayout.CENTER);
	validate();
    }

    protected void showScout(int chosen, Bot[] robs) {
	gameBoardCanvas.vorschau(chosen,robs);
    }


    public SACanvas getSpielfeld() {
      return gameBoardCanvas;
    }

    /** parameter will be ignored*/
     protected void quit(boolean keepWatching) {
        CAT.debug("AusgabeView starts procedure to quit the client..");
	JLabel[] msg = new JLabel[2];
	msg[0] = new JLabel(Message.say("AusgabeView", "reallyQuit1"));
	msg[1] = new JLabel(Message.say("AusgabeView","reallyQuit2"));
	if (JOptionPane.showConfirmDialog(this, msg,
	Message.say("AusgabeView","reallyQuitTitle"),
	JOptionPane.OK_CANCEL_OPTION,
	JOptionPane.QUESTION_MESSAGE)
	                        == JOptionPane.OK_OPTION){
	 // null means CANCEL
        ausgabe.quit(keepWatching);
	// Since this does not work properly by now, we do:
	System.exit(0);
     }
    }

    private void d(String s){
	Global.debug(this, s);
    }


    /** Ugly; the menubar will be displayed in View*/
    protected JMenuBar getMenuBar() {
      return menus;
    }


    private void initMenus() {
      JMenu trackMenu = new JMenu(Message.say("AusgabeView", "trackMenu"));
      if (gameBoardCanvas!=null)
        trackMenu.add(new ShowFlagMenu(gameBoardCanvas.getFlags()));
      trackMenu.add(new ShowRobMenu());

      JMenuItem stats = new JMenuItem(Message.say("AusgabeView", "stats"));
      stats.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
          ausgabe.showStats();
        }
      });

      JMenu optionsMenu = new JMenu (Message.say("AusgabeFrame","mOptions"));
      optionsMenu.add(new SpeedMenu());
      optionsMenu.add(new SoundMenu());
      optionsMenu.add(stats);



      menus.add(new FileMenu());
      menus.add(new ZoomMenu());
      menus.add(trackMenu);
      menus.add(optionsMenu);
      menus.add(new HelpMenu());
    }

    private class FileMenu extends JMenu {
      FileMenu() {
          super(Message.say("AusgabeView", "mFile"));
          JMenuItem mQuit = new JMenuItem((Message.say("AusgabeView","mFinish")));
          mQuit.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e){
                  quit(false);
                }
	    });

          /*
          JMenuItem mQuitWatch = new JMenuItem((Message.say("AusgabeView","mFinishButWatch")));
          mQuit.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e){
                  quit(true);
                }
	    });

          this.add(mQuitWatch);
          */
          this.add(mQuit);
      }
    }


    private class ZoomMenu extends JMenu implements ActionListener {
	ZoomMenu() {
	    super("Zoom");
	    ButtonGroup group = new ButtonGroup();
	    JRadioButtonMenuItem item = null;
	    for(int d = MIN_ZOOM; d <= MAX_ZOOM; d += ZOOM_STEP ) {
		item = new JRadioButtonMenuItem( "" + d + "%" );
		item.setActionCommand("" + d);
		item.addActionListener( this );
		super.add( item );
		group.add( item );
                if (d==DEFAULT_ZOOM)
                  group.setSelected( item.getModel(), true );
	    }
	}
        public void actionPerformed(ActionEvent e) {
	    int iScale;
	    try {
		String s = e.getActionCommand();
	        iScale = Integer.parseInt( s );
	    } catch( NumberFormatException ne ) {
	        iScale = 10;
		Global.debug(this, "bad zommmenu action command. using default 100%");
	    }
	    final double sc = iScale / 100.0;
	    SwingUtilities.invokeLater( new Runnable() {
		    public void run() {
			 gameBoardCanvas.setScale( sc );
		    }
		});
	}
  }

  private class SoundMenu extends JMenu {
     SoundMenu () {
        super ((Message.say("AusgabeView","mSound")));
        soundOn = SoundMan.isSoundEnabled();
      //  String tmp = Conf.getProperty("sound.active");
      //  if (tmp!=null)
      //     soundOn = tmp.equals("true");
	JCheckBoxMenuItem soundBox = new JCheckBoxMenuItem(Message.say("AusgabeView","mSoundOn"), soundOn);
	soundBox.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e){
		    soundOn = !soundOn;
		    SoundMan.setSoundActive(soundOn);
                    String status = Message.say("AusgabeView","mSoundChange")+" ";
                    if (soundOn)
                      status += Message.say("AusgabeView","mSoundOn");
                    else
                      status += Message.say("AusgabeView","mSoundOff");
                    showActionMessage(status);
                }
	    });
	add(soundBox);
     }
  }

  private class SpeedMenu extends JMenu implements ActionListener {
    JRadioButtonMenuItem lSpeed;
    JRadioButtonMenuItem mSpeed;
    JRadioButtonMenuItem hSpeed;
      SpeedMenu () {
        super(Message.say("AusgabeFrame","mSpeed"));
	ButtonGroup speedGroup = new ButtonGroup();
	lSpeed = new JRadioButtonMenuItem(Message.say("AusgabeFrame","mSlow"),false);
	mSpeed = new JRadioButtonMenuItem(Message.say("AusgabeFrame","mMiddle"),true);
        hSpeed = new JRadioButtonMenuItem(Message.say("AusgabeFrame","mFast"),false);
        lSpeed.addActionListener(this);
	mSpeed.addActionListener(this);
        hSpeed.addActionListener(this);
        speedGroup.add(lSpeed);
        speedGroup.add(mSpeed);
        speedGroup.add(hSpeed);
        this.add(lSpeed);
        this.add(mSpeed);
	this.add(hSpeed);
      }

      public void actionPerformed(ActionEvent e) {
	    if (e.getSource() == lSpeed) {
		speed=SLOW;
		showActionMessage(Message.say("AusgabeFrame","gAufLang"));
	    }
	    else if (e.getSource() == mSpeed) {
		speed=MEDIUM;
                showActionMessage(Message.say("AusgabeFrame","gAufMitt"));
            }
	    else {
		speed=FAST;
	        showActionMessage(Message.say("AusgabeFrame","gAufUn"));
	    }
	}


   }
   private class HelpMenu extends JMenu {
      HelpMenu () {
        super (Message.say("AusgabeFrame","mHelpMenuName"));
        JMenuItem about = new JMenuItem(Message.say("AusgabeFrame","mAbout"));
	about.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    new AboutFenster();
		}
	    });
        add(about);
      }

    }




   private class ShowFlagMenu extends JMenu {
      ShowFlagMenu(Location [] flagPos) {
        super(Message.say("AusgabeView", "flag"));
        init (flagPos);
      }

      void init(Location [] flags) {
        if (flags!=null){
          for (int i=0;i<flags.length;i++){
            JMenuItem flag = new JMenuItem(" #"+(i+1));
            flag.addActionListener(new ShowFlagListener(flags[i].x, flags[i].y));
            this.add(flag);
            if (CAT.isDebugEnabled()){
              CAT.debug("ScrollFlag - added flag: #"+(i+1)+" x:"+flags[i].x+" y:"+flags[i].y);
            }
          }
        }
        else
          CAT.debug("flag array for ScrollFlagMenu was null!");
      }

   }

    private class ShowFlagListener implements ActionListener {
        int xpos, ypos;
        public ShowFlagListener(int x, int y){
          this.xpos = x;
          this.ypos = y;
        }

        public void actionPerformed(ActionEvent e){
          if (CAT.isDebugEnabled())
            CAT.debug("Showing Pos at x:"+xpos+" y:"+ypos);
          showPos(xpos, ypos);
        }
    }

   private class ShowRobMenu extends JMenu {
      ShowRobMenu() {
        super(Message.say("AusgabeView", "rob"));
        init ();
      }

      void init() {
        if (robotStatus!=null){
          int l = robotStatus.size();
         Enumeration e = robotStatus.elements();
         while (e.hasMoreElements()){
            RobotStatus rs = (RobotStatus) e.nextElement();
            Bot robot = rs.getRobot();
            CAT.debug( "Bot is: " + robot );
            CAT.debug( "imgs is: " + OldRobotStatusImpl.robotImages );
            JMenuItem rob = new JMenuItem(robot.getName(),new ImageIcon(OldRobotStatusImpl.robotImages[robot.getBotVis()]));
            rob.addActionListener(new ShowRobListener(rs));
            this.add(rob);
          }
        }
        else
          CAT.debug("robotstatus was null! Failed to create ShowRobMenu items");
      }

   }

    private class ShowRobListener implements ActionListener {
        RobotStatus rob;
        public ShowRobListener(RobotStatus r){
          rob = r;
        }

        public void actionPerformed(ActionEvent e){
          Bot robot = rob.getRobot();
          if (CAT.isDebugEnabled())
            CAT.debug("Showing robot \""+robot.getName()+"\" at x:"+robot.getX()+" y:"+robot.getY());
          int x = robot.getX();
          int y = robot.getY();
          if (x<1 && y<1){ // robot destroyed, not on board

              String s = Message.say("AusgabeView", "robotNotOnBoard", robot.getName());
              JOptionPane.showMessageDialog(null,s, "Ooops!",JOptionPane.INFORMATION_MESSAGE,
                                             new ImageIcon(OldRobotStatusImpl.robotImages[robot.getBotVis()]));
              CAT.debug("Showing popup instead of robot, because robot is not on board..");
          }
          else {
            showPos(x, y);
          }
        }
    }

    public JComponent getBoardView() {
        return gameBoardView;
    }

}


