package de.botsnscouts.gui;

import de.botsnscouts.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * Zeigt das Spielfeld und alle Statusmeldungen an
 * @author Lukasz Pekacki
 */

public class AusgabeView extends JPanel implements AusgabeViewInterface {
    // --- objects
    private JScrollPane spielFeldScrollFenster;
    private JViewport spielFeldView;
    private SACanvas  spielFeld;
    private Ausgabe   ausgabe;
    private Hashtable robotStatus = new Hashtable(8);
    private Hashtable robotCardStatus = new Hashtable(8);
    private StatusLog statusLog = new StatusLog();
    private JMenuBar menus = new JMenuBar();

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




    /** @args SpielerMensch spielerref ist Referenz auf umgebenden
     *  MenschlichenSpieler, falls Ausgabe zu einem Spieler gehoert,
     *  null sonst.
     */
    public AusgabeView() {
        super();
        this.initMenus();
    }

    public AusgabeView(SACanvas sa, Roboter[] robots, Ausgabe aus) {
	ausgabe = aus;
	spielFeld=sa;
	JPanel robotsStatusContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
	JPanel robotsCardContainer = new JPanel(new GridLayout(8,1));

	setLayout(new BorderLayout());

	// create status panel
	for (int i=0; i< robots.length; i++) {
	    RobotStatus r = new RobotStatus(robots[i],
					    new MouseAdapter(){
						    public void mouseClicked(MouseEvent me) {
							ausgabe.trackRob(((JLabel)me.getSource()).getName());
						    }
						},
					    new MouseAdapter(){
						    public void mouseClicked(MouseEvent me) {
							ausgabe.scrollFlag(Integer.parseInt(((JLabel)me.getSource()).getText()));
						    }
						}
					    );
	    robotsStatusContainer.add(r);
	    robotStatus.put(robots[i].getName(),r);

            // add entry to track menu
          //  JMenuItem trackItem = new JMenuItem( r.getName() );
           // trackItem.addActionListener( new HumanPlayer.RoboTrackListener(robots[i]) );
    	   // optTrack.add(trackItem);

	    RobotCard rc= new RobotCard(robots[i]);
	    robotsCardContainer.add(rc);
	    robotCardStatus.put(robots[i].getName(),rc);
	}
	add(robotsStatusContainer,BorderLayout.NORTH);
//	add(robotsCardContainer,BorderLayout.EAST);


	// create status log
	add(statusLog,BorderLayout.SOUTH);

	// create scroll panel
	spielFeldScrollFenster = new JScrollPane();
	spielFeldScrollFenster.getHorizontalScrollBar().setUnitIncrement(64);
	spielFeldScrollFenster.getVerticalScrollBar().setUnitIncrement(64);
	spielFeldScrollFenster.setViewportView(spielFeld);
	spielFeld.setScrollPane(spielFeldScrollFenster);
	add(spielFeldScrollFenster,BorderLayout.CENTER);
	spielFeldView = spielFeldScrollFenster.getViewport();
        this.initMenus();

    }

    public void shutup() {
	System.exit(0);
    }



    /**
     * show the sinlge line action message that came from the server
     */
    public void showActionMessage(String s){
	statusLog.addMessage(s);
    }


    /**
     * Schreibt in die Statuszeile einen Text
     */
    public void showRobStatus(Roboter r){
    }


    /**
     * Shows the new Positions of the Robots
     */
    public void showUpdatedRobots(Roboter[] r){
	spielFeld.ersetzeRobos(r);
	for (int i = 0; i < r.length; i++) {
	    ((RobotStatus) robotStatus.get(r[i].getName())).updateRobot(r[i]);
	    ((RobotCard) robotCardStatus.get(r[i].getName())).updateRobot(r[i]);
	    // RobCards aktualisieren TODO
	}
    }


    public void showPos(int robix, int robiy) {

	int x = robix*64;
	int y = spielFeld.getHeight()-(robiy*64);

	Dimension sz = spielFeldView.getExtentSize();
	int w2 = sz.width/2;
	int h2 = sz.height/2;


	// make sure we dont want to scoll 'out' to
	// the left and top
	int x1 = Math.max( x - w2 , 0);
	int y1 = Math.max( y - h2 , 0);

	// soll ich überhaupt scrollen?
	// in X-Richtung
	if ((x < ( (spielFeldView.getViewPosition().x)+10 ) ) ||
	    x > ( (spielFeldView.getViewPosition().x+sz.width)-10 )) {
	    x1 = Math.min( x1, (spielFeld.getWidth() - sz.width) );
	}
	else x1 = spielFeldView.getViewPosition().x;

	// in Y-Richtung
	if ((y < ( spielFeldView.getViewPosition().y +10) ) ||
	    y > ( (spielFeldView.getViewPosition().y+sz.height)-10 )) {
	    y1 = Math.min( y1, (spielFeld.getHeight() - sz.height) );
	}
	else y1 = spielFeldView.getViewPosition().y;

	spielFeldView.setViewPosition(new Point(x1, y1));
    }


    /**
     * board view is to paint robolaser activity
     */
    public void showRobLaser(Roboter von, Roboter nach){
	spielFeld.doRobLaser(von, nach);
    }

    /**
     * board view is to paint bord laser activity
     */
    public void showBoardLaser(Ort laserPos, int facing, int stregth, Ort r1Pos){
	spielFeld.doBordLaser(laserPos, facing, stregth, r1Pos,spielFeldView);
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
	spielFeld.setVisible(false);
	add(new Abspann(winners),BorderLayout.CENTER);
	validate();
    }

    protected void showScout(int chosen, Roboter[] robs) {
	spielFeld.vorschau(chosen,robs);
    }


    public SACanvas getSpielfeld() {
      return spielFeld;
    }

    /** parameter will be ignored*/
     private void quit(boolean keepWatching) {
        ausgabe.quit(keepWatching);
    }

    private void d(String s){
	Global.debug(this, s);
    }


    /** Ugly; the menubar will be displayed in View*/
    protected JMenuBar getMenuBar() {
      return menus;
    }


    private void initMenus() {
      menus.add(new FileMenu());
      menus.add(new ZoomMenu());
      menus.add(new OptionsMenu());
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




    private class OptionsMenu extends JMenu {
       OptionsMenu () {
        super(Message.say("AusgabeFrame","mOptions"));
        //add(new ZoomMenu());
        add(new SpeedMenu());
        add(new SoundMenu());
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
			 spielFeld.setScale( sc );
		    }
		});
	}
  }

  private class SoundMenu extends JMenu {
     SoundMenu () {
        super ((Message.say("AusgabeView","mSound")));
	JCheckBoxMenuItem soundBox = new JCheckBoxMenuItem(Message.say("AusgabeView","mSoundOn"), soundOn);
	soundBox.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e){
		    soundOn = !soundOn;
		    spielFeld.setSoundActive(soundOn);
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
      }

    }






}


