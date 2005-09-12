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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import org.apache.log4j.Category;

import de.botsnscouts.gui.hotkey.HotKey;
import de.botsnscouts.gui.hotkey.HotKeyAction;
import de.botsnscouts.gui.hotkey.HotKeyConf;
import de.botsnscouts.gui.hotkey.HotKeyEditorPanel;
import de.botsnscouts.gui.hotkey.HotKeyMan;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.BotVis;
import de.botsnscouts.util.Conf;
import de.botsnscouts.util.Global;
import de.botsnscouts.util.Location;
import de.botsnscouts.util.Message;
import de.botsnscouts.util.Registry;
import de.botsnscouts.util.SoundMan;
import de.botsnscouts.widgets.BnsOptionPane;
import de.botsnscouts.widgets.OptionPane;
import de.botsnscouts.widgets.PaintPanel;
import de.botsnscouts.widgets.TJLabel;
import de.botsnscouts.widgets.TJPanel;

public class AusgabeView extends PaintPanel  {
    static Category CAT = Category.getInstance(AusgabeView.class);

    private final static long SHOW_MESSAGE_DELAY=300;

    // --- objects
    private PhaseEvaluationPanel phaseInfo=new PhaseEvaluationPanel(); 
    private JScrollPane gameBoardScrollPane;
    private JViewport gameBoardView;
    private BoardView  gameBoardCanvas;
    private Ausgabe   ausgabe;
    private Hashtable robotStatus = new Hashtable(8);
    private Hashtable robotCardStatus = new Hashtable(8);
   

    
    private JMenuBar menus = new JMenuBar();

    private JComponent northPanel = new PaintPanel( OptionPane.getBackgroundPaint(this) );
    private ZoomMenu zoomMenu = new ZoomMenu();
    private ShowOrHideRobMenu showHideMenu =  new ShowOrHideRobMenu(); // must not be null
    private SpeedMenu speedMenu = new SpeedMenu();
    
    // settings for Zoom-Menu
    protected static final int MIN_ZOOM = 40;
    protected static final int DEFAULT_ZOOM = 100; // selects the RadioButton to
                                                   // be selected as default, does not
                                                   // influence the default zoom; that
                                                   // will be always 100
    protected static final int MAX_ZOOM = 150;
    protected static final int ZOOM_STEP = 10;

    private static final int MIN_ZOOM_SCALE = MIN_ZOOM/10;
    private static final int MAX_ZOOM_SCALE = MAX_ZOOM/10;

    /** Scroll to the active robot*/
    private static final int TRACKMODE_BOT = 1;
    /** Scroll to whereever something happens*/
    private static final int TRACKMODE_ACTION = 2;
    /** Don't scroll automatically*/
    private static final int TRACKMODE_NOTHING=3;
    /** Indicates if and where to scroll the viewport if something happens*/
    private int currentTrackMode = TRACKMODE_NOTHING;
    /**  If currentTrackMode is set to TRACKMODE_BOT, we will try to scroll to this bot's position 
     *  everytime something happens to him.*/
    private Bot botToTrack;
 
    // sound-menu
    private boolean soundOn = false;

    // Options for the speed-menu
    private AnimationConfig speedSettingSlow;
    private AnimationConfig speedSettingMedium;
    private AnimationConfig speedSettingFast;
    
    private AnimationConfig currentSpeedConfig; 
    
    /** Editor for SpeedMenu actions*/
    private AnimationsSettingEditor speedSettingEditor;
    
    
    private Location [] flags;
    
    public AusgabeView(BoardView sa, Bot[] robots, Ausgabe aus) {
        super(OptionPane.getBackgroundPaint(null));
		ausgabe = aus;
		gameBoardCanvas = sa;
		
		JPanel robotsStatusContainer = new TJPanel(new FlowLayout(FlowLayout.LEFT));
		robotsStatusContainer.setOpaque(false);
		JPanel robotsCardContainer = new TJPanel(new GridLayout(8, 1));
		flags = sa.sf.getFlags();
		int flagCount = flags.length;

		setLayout(new BorderLayout());

		// create status panel
		ScalableRegisterRow [] phaseEvalRows = new ScalableRegisterRow[robots.length];
		for (int i = 0; i < robots.length; i++) {		
		    final String robotName = robots[i].getName();
		    // not for the status panel, but while I am already looping over the bots..
		    phaseEvalRows[i] = ausgabe.getInfoRegistersForBot(robotName);		    
			RobotInfo r = new RobotInfo(robots[i], flagCount, ausgabe.getTooltipRegistersForBot(robotName));
			r.addRobotInfoListener(new RobotInfoListener() {
				public void robotClicked(RobotInfoEvent rie) {
				    Bot robot = ((RobotInfo) rie.getSource()).getRobot();
					showPos(robot.getX(), robot.getY(),true, false);
					
				}

				public void flagClicked(RobotInfoEvent rie) {
					RobotInfo ri = (RobotInfo) rie.getSource();
					jumpToFlag(ri.getRobot().getNextFlag());
				}

				public void diskClicked(RobotInfoEvent rie) {
					Bot robot = ((RobotInfo) rie.getSource()).getRobot();
					showPos(robot.getArchiveX(), robot.getArchiveY(),
							true, false);
				}
			});
			robotsStatusContainer.add(r);
			robotStatus.put(robots[i].getName(), r);			
			RobotCard rc = new RobotCard(robots[i]);
			robotsCardContainer.add(rc);
			robotsCardContainer.setOpaque(false);
			robotCardStatus.put(robots[i].getName(), rc);
		}

		phaseInfo.setContents(robots, phaseEvalRows);
		
		this.northPanel.setLayout(new BorderLayout());
		northPanel.setOpaque(false);
		northPanel.add(robotsStatusContainer, BorderLayout.WEST);		
		gameBoardScrollPane = new JScrollPane();
		gameBoardScrollPane.getHorizontalScrollBar().setUnitIncrement(64);
		gameBoardScrollPane.getVerticalScrollBar().setUnitIncrement(64);
		gameBoardScrollPane.setViewportView(gameBoardCanvas);
		gameBoardScrollPane.setOpaque(false);
		gameBoardScrollPane.getViewport().setOpaque(false);
		gameBoardScrollPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));		
		this.add(gameBoardScrollPane, BorderLayout.CENTER);
		gameBoardView = gameBoardScrollPane.getViewport();
		this.initSpeedSettings();
		this.initMenus();
		

	}
    
    
    
    public static final String PROPERTY_DEFAULT_SPEED="defaultAnimationSetting";
    
    private void initSpeedSettings() {
        
        speedSettingSlow = new AnimationConfig(AnimationConfig.ANIMATION_SLOW, true);
        speedSettingMedium = new AnimationConfig(AnimationConfig.ANIMATION_MEDIUM, true);
        speedSettingFast = new AnimationConfig(AnimationConfig.ANIMATION_FAST, true);
        AnimationConfig defaultSpeed = speedSettingMedium;
        String lastSetting = Conf.getProperty(PROPERTY_DEFAULT_SPEED);
        if (lastSetting != null){
            if (lastSetting.equals(speedSettingSlow.getConfigName())) {
               defaultSpeed = speedSettingSlow;
            }
            else if (lastSetting.equals(speedSettingFast.getConfigName())){
               defaultSpeed = speedSettingFast;
            }
           // else: we keep medium as the defaultspeed
        }
        speedMenu.setAnimationSpeed(defaultSpeed);
        
        speedSettingEditor = new AnimationsSettingEditor(speedSettingSlow, speedSettingMedium,
                                                                                                   speedSettingFast);
        speedSettingEditor.pack();
    }

    protected void addWiseAndScout(JPanel p){
        CAT.debug("adding WiseAndScout to Northpanel/EAST");
        northPanel.add(p, BorderLayout.EAST);
    }

    protected JComponent getNorthPanel(){
      return northPanel;
    }
    /*
	 * public void shutup() { System.exit(0); }
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

    public void showGameStatusMessage(String s){
    }

    /**
     * Schreibt in die Statuszeile einen Text
     */
    public void showRobStatus(Bot r){
    }

    boolean allLedReset  = true;
    public void notifyBotProgrammingDone(Bot bot) {
        RobotInfo ri = (RobotInfo)robotStatus.get( bot.getName() );
        ri.setLedOn(true);
        SoundMan.playSound(SoundMan.PROGRAMMING_DONE);
        ri.repaint();
        allLedReset = false;
    }


    public void resetProgrammingLEDs() {
        if( allLedReset ) {
            return;
        }

        Collection c = robotStatus.values();
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            RobotInfo info = (RobotInfo) iterator.next();
            info.setLedOn(false);
        }
        northPanel.repaint();
        allLedReset = true;
    }

    /**
     * Shows the new Positions of the Robots
     */
    public void showUpdatedRobots(Bot[] r){
        checkAndDoTracking();
		gameBoardCanvas.ersetzeRobos(r);        	
		updateRobotStatusDisplay(r);		
    }
    
    protected void updateRobotStatusDisplay(Bot [] r){
        int count = r.length;
	    for (int i = 0; i < count; i++) {
	        Bot b = r[i];
	        String name = b.getName();
		    ((RobotStatus) robotStatus.get(name)).updateRobot(b);
		    ((RobotCard) robotCardStatus.get(name)).updateRobot(b);		
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

 /*   public void showPos2(int robix, int robiy, boolean scrollStepwise ) {
        showPos(robix, robiy, true, scrollStepwise);
    }
*/
    public void showPos(int robix, int robiy, boolean highlight, boolean scrollStepWise) {
        showPos(robix, robiy, highlight, scrollStepWise, 250); 
    }
    
    public void showPos(int robix, int robiy, boolean highlight, boolean scrollStepWise, int tolerance) {
       
        Point newCenter = gameBoardCanvas.ort2Point( robix, robiy);
        Point newUpperLeft = new Point();
        Dimension visibleSize = gameBoardView.getExtentSize();
        Dimension totalSize = gameBoardView.getViewSize();
        Point currentUpperLeft = gameBoardView.getViewPosition();
        int maxX = totalSize.width-visibleSize.width;
        int maxY = totalSize.height-visibleSize.height;
        int preferredUpperLeftX = newCenter.x - visibleSize.width/2;
        int preferredUpperLeftY = newCenter.y - visibleSize.height/2;
                                    
        newUpperLeft.x = Math.min(maxX,preferredUpperLeftX); // don't scroll too far to the right
        newUpperLeft.y = Math.min(maxY, preferredUpperLeftY); // don't scroll too far down
        // in case newUpperLeft has now negative coordinates, we rather scroll
        // too far right ( down) than too  far left (up): 
        newUpperLeft.x = Math.max(0, newUpperLeft.x);
        newUpperLeft.y = Math.max(0, newUpperLeft.y);
        
        int tol =tolerance;
        boolean doWeScroll = Math.abs(preferredUpperLeftX-currentUpperLeft.x)>tol
                                          || Math.abs(preferredUpperLeftY-currentUpperLeft.y)>tol
                                          || !gameBoardView.getViewRect().contains(newCenter);
        
        if (doWeScroll) {                        
            if (!scrollStepWise) {
                gameBoardView.setViewPosition(newUpperLeft);
            }
            else {
	            int offset = 30;
	            int delay = 5; // TODO find good value/make configurable	          
	            Point [] scrollPoints = ScrollHelper.calculateDiagonalScrollSteps(currentUpperLeft, 
	                            newUpperLeft, offset);
	           // Point [] scrollPoints = ScrollHelper.scrollRightAngled(newUpperLeft, currentUpperLeft,
	          //                 offset, true);
	            int pcount = scrollPoints.length;
	            for (int i=0;i<pcount;i++){
	                gameBoardView.setViewPosition(scrollPoints[i]);
	                waitSomeTime(delay, this);
	            }
	           
	            waitSomeTime(300, this); // TODO find good value/make configurable
            }
        }
        
        if( highlight ) {
            this.gameBoardCanvas.highlight(robix, robiy);
        }
    }
        


   
    /**
     * board view is to paint robolaser activity
     */
    public void animateRobLaser(Bot von, Bot nach){
        checkAndDoTracking(von.getPos());
        gameBoardCanvas.doRobLaser(von, nach);       
    }

    /**
     * board view is to paint bord laser activity
     */
    protected void animateBoardLaser(Location laserPos, int facing, int stregth, Location r1Pos){
        checkAndDoTracking(laserPos);
       gameBoardCanvas.doBordLaser(laserPos, facing, stregth, r1Pos,gameBoardView);
    }

    /** bord view is to animate robot movement*/
    protected void animateRobMove(Bot rob, int direction){
        checkAndDoTracking(rob.getPos());
        gameBoardCanvas.animateRobMove(rob, direction);
    }
    /** bord view is to animate robot movement*/
    protected void animateRobTurn(Bot rob, int direction){
        checkAndDoTracking(rob.getPos());
        gameBoardCanvas.animateRobTurn(rob, direction);
    }
    /** bord view is to animate robot movement*/
    protected void animateRobUTurn(Bot rob){
        checkAndDoTracking(rob.getPos());
        gameBoardCanvas.animateRobUTurn(rob);
    }
    
    /** bord view is to animate robot pitfall*/
    protected void animatePitFall(Bot rob){
        checkAndDoTracking(rob.getPos());
        gameBoardCanvas.animatePitFall(rob);
    }
    
    /** bord view is to animate robot pitfall*/
    protected void animateBotCrushed(Bot rob){
        checkAndDoTracking(rob.getPos());
        gameBoardCanvas.animateBotCrushed(rob);
    }
    
    protected void setInitialFacings(Bot [] botsWithUpdatedFacing){
       // no tracking needed (yet)
        gameBoardCanvas.updateFacings(botsWithUpdatedFacing);
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
		gameBoardScrollPane.setVisible(false);				
		this.remove(gameBoardScrollPane);
		View view = ausgabe.getView();
		if (view != null){
		    view.logFloatPane.setVisible(false);
		}
        WinnerListPanel wp = new WinnerListPanel(winners);        
        //JPanel bla = new JPanel(); // stupid, but the only (simple) working way I found to 
        // show the WinnerListPanel including its borders
        ///bla.setLayout(new BorderLayout());
       // bla.add(wp, BorderLayout.CENTER);
       // add(bla,BorderLayout.CENTER);
        add(wp, BorderLayout.CENTER);
		validate();
    }

    protected void showScout(int chosen, Bot[] robs) {
	gameBoardCanvas.preview(chosen,robs);
    }


    
    
    public PhaseEvaluationPanel getPhaseEvalPanel(){
        return phaseInfo;
    }
    
    public BoardView getBoardView() {
      return gameBoardCanvas;
    }

    
    protected void quit(boolean keepWatching){
        quit (keepWatching, false);
    }
    
     protected void quit(boolean keepWatching, boolean killJVM) {
        CAT.debug("AusgabeView starts procedure to quit the client..");
		JLabel[] msg = new JLabel[2];
		msg[0] = new JLabel(Message.say("AusgabeView", "reallyQuit1"));
		Registry globalReg = Registry.getSingletonInstance();
		if (globalReg.isMyServerLocal(this.ausgabe) && // have we started the server or are we a remote view? 
		    globalReg.getNumOfLocalViewsForMyGame(this.ausgabe)<2){
		    // ..and are we the only local view left?
		    // => add message that the server will go down if this view quits
		    msg[1] = new JLabel(Message.say("AusgabeView","reallyQuit2"));
		}
		else {
		    msg[1]=new TJLabel("");
		}
		
/*		BnsOptionPane pane = BnsOptionPane.createPaintedOptionPane(true);
		int feedback = pane.bnsShowConfirmDialog(this, 
		                Message.say("AusgabeView","reallyQuitTitle"), msg);
	*/	
		int feedback = JOptionPane.showConfirmDialog(this, msg,
		                	Message.say("AusgabeView","reallyQuitTitle"),
		                	JOptionPane.OK_CANCEL_OPTION,
		                	JOptionPane.QUESTION_MESSAGE);
		
		if (feedback == JOptionPane.OK_OPTION){		
		    if (phaseTimer != null) {
		        try {
		            phaseTimer.cancel();		           
		        }
		        catch (Exception e){
		            CAT.warn(e.getMessage(), e);
		        }
		    }
		    if (killJVM) {
		        // the disabling of the registry is needed here, because the shutdown
		        // of every client will result in the registry trying to "redisplay" the
		        // main menu - and "redisplaying the main menu" means at the moment that a new
		        // JVM is launched => the application could only be closed in the main menu,
		        // but not by closing the window or selecting the "quit game" menu item.
		        // In fact, without disabling the registry, both "restart game" and "quit game"
		        // would have the same effect at the moment
		        Registry.getSingletonInstance().setEnabled(false);
		    }
		    boolean shutdownPossibleHumanPlayer = true;
	        ausgabe.quit(keepWatching, shutdownPossibleHumanPlayer);
	        if (killJVM) {
		        try {
		            synchronized(this){
		                this.wait(3000);
		            }
		        }
		        catch (Exception e){
		            CAT.warn(e.getMessage(), e);
		        }
		        finally {
		            System.exit(0);
		        }
	        }
	     }
		else {  // dont quit
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
      JMenu locateMenu = new JMenu(Message.say("AusgabeView", "locateMenu"));
      if (gameBoardCanvas!=null) {
        locateMenu.add(new JumpToFlagMenu(gameBoardCanvas.getFlags()));
      }
      locateMenu.add(new JumpToRobMenu());
    //  locateMenu.add(new TrackMenu());
      
      
      JMenuItem hotkeyMenu = new JMenuItem(Message.say("AusgabeView", "hotkeyMenu"));
      hotkeyMenu.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e){
          showEditHotKeys();
        }
      });

      JMenu optionsMenu = new JMenu (Message.say("AusgabeFrame","mOptions"));
      optionsMenu.add(speedMenu);
      optionsMenu.add(new SoundMenu());
      optionsMenu.add(hotkeyMenu);



      menus.add(new FileMenu());

      JMenu viewMenu = new ViewMenu();      
      menus.add(viewMenu);
      menus.add(locateMenu);
      menus.add(new TrackMenu());
      menus.add(optionsMenu);
      menus.add(new HelpMenu());
    }




    private class FileMenu extends JMenu {
      FileMenu() {
          super(Message.say("AusgabeView", "mFile"));
          JMenuItem mQuit = new JMenuItem((Message.say("AusgabeView","mFinish")));
          mQuit.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e){		    	
                  quit(false, true);
                }
	    });
          JMenuItem mRestart = new JMenuItem((Message.say("AusgabeView","mRestart")));
          mRestart.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e){		    	
                  quit(false, false);
                }
	    });
          JMenuItem stats = new JMenuItem(Message.say("AusgabeView", "stats"));
          stats.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
              ausgabe.showStats();
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
          this.add(stats);
          this.addSeparator();
          this.add(mRestart);
          this.add(mQuit);
      }
    }

    private int actualScale = DEFAULT_ZOOM;
    private void zoom (int iScale) {
         if (MAX_ZOOM<iScale || MIN_ZOOM>iScale ) {
            CAT.debug("zoom: scale out of range: "+iScale);
            return;
         }
         final double sc = iScale / 100.0;
         final int tmpScale = iScale;
	    SwingUtilities.invokeLater( new Runnable() {
		    public void run() {
		         gameBoardCanvas.setScale( sc );
		         CAT.info("GARBAGE COLLECTION");
		         System.gc();
		         actualScale = tmpScale;
                 zoomMenu.select(actualScale);
            }
		});

    }

    private void zoomIn() {
        CAT.debug("zooming in");
        zoom (actualScale+ZOOM_STEP);
    }

    private void zoomOut(){
        CAT.debug("zooming out");
        zoom (actualScale-ZOOM_STEP);
    }


    
    protected void showAllRobots() {
        if (showHideMenu != null) {
            showHideMenu.showAllRobots();
        }
    }
    
    private class ViewMenu extends JMenu  {
        
       
        
        public ViewMenu() {
            super(Message.say("AusgabeView", "viewMenu"));
            zoomMenu = new ZoomMenu();
            showHideMenu = new ShowOrHideRobMenu();
            add(zoomMenu);
            add(showHideMenu);
           
        }
        
    }
    
    
    private class ShowOrHideRobMenu extends JMenu{        
       private JMenuItem showAll;  
       private JMenuItem showNone; 
      // private JMenuItem showMe;    
       private BotCheckBoxMenuItem [] botBoxes;
       private boolean botsVisible = true;
       
        public ShowOrHideRobMenu(){
            super(Message.say("AusgabeView", "showRobMenu"));
            showAll     = new JMenuItem(Message.say("AusgabeView", "showRobMenuAll"));
            showAll.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ae) {
                    showAllRobots();
                }
            });
            
            showNone  =  new JMenuItem(Message.say("AusgabeView", "showRobMenuNone"));
            showNone.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent ae) {
                    hideAllRobots();
                }
            });
         
            /* not a good idea, this would break the separation of View and Player;
             *  this View has no "Me"; also, with "hide all" one can easily simulate the function
            showMe = new JMenuItem(Message.say("AusgabeView", "showRobMenuMe"));
            showMe.addActionListener(new ActionListener(){
                public void actionPerformed (ActionEvent e){
                    
                }
            });
           */
            
            add(showAll);
            //add(showMe);
            add(showNone);
            addSeparator();
            
            botBoxes = new BotCheckBoxMenuItem[robotStatus.size()];
            Enumeration e = robotStatus.elements();                        
            int i=0;
            while (e.hasMoreElements()){
                RobotStatus rs = (RobotStatus) e.nextElement();
                Bot robot = rs.getRobot();
                BotCheckBoxMenuItem box =createCheckBoxItemForBot(robot); 
                botBoxes[i++] = box;
                add(box);
            }
        }
                      

        private void hideRobot(Bot bot){
            gameBoardCanvas.setRobotVisbility(bot, false);
        }
        
      
        private void showRobot (Bot  bot){
            gameBoardCanvas.setRobotVisbility(bot, true);
        }
        
        public void hideAllRobots(){
            int count = botBoxes.length;
            for (int i=0;i<count;i++){
                botBoxes[i].setSelected(false);
            }            	
            botsVisible = false;
            gameBoardCanvas.setAllRobotsInvisible();
        }
        
        public void showAllRobots(){
            int count = botBoxes.length;
            for (int i=0;i<count;i++){
                botBoxes[i].setSelected(true);
            }
            botsVisible = true;
            gameBoardCanvas.setAllRobotsVisible();
        }
            
        
        
        private BotCheckBoxMenuItem createCheckBoxItemForBot(Bot bot){
            final BotCheckBoxMenuItem box = new BotCheckBoxMenuItem(bot);
            box.addActionListener(new ActionListener() {
	    		public void actionPerformed(ActionEvent e){
	    		    	if (box.isSelected()) {
	    		    	    CAT.debug("IS SELECTED");
	    		    	    showRobot( box.getBot());
	    		    	}
	    		    	else {
	    		    	    CAT.debug("IS NOT SELECTED");
	    		    	    hideRobot(box.getBot());
	    		    	}
	    		} 
	    	 });
            return box;
        }
        
    }
    
    
    private class BotCheckBoxMenuItem extends JCheckBoxMenuItem{
        private Bot myBot;
        public BotCheckBoxMenuItem(Bot bot){            
            super(bot.getName(), true);
            myBot = bot;
            int myVis = myBot.getBotVis();
            this.setIcon(new ImageIcon(BotVis.getRobotImages()[myVis]));            
            this.setForeground(BotVis.getBotColorByBotVis(myVis));                   
        }
        public Bot getBot(){
            return myBot;
        }
    
    }
    

    private class ZoomMenu extends JMenu implements ActionListener {
        private ButtonModel[] zoomItemModels = new ButtonModel[MAX_ZOOM_SCALE - MIN_ZOOM_SCALE + 1];

        ButtonGroup group;

        ZoomMenu() {
            super("Zoom");

            group = new ButtonGroup();
            JRadioButtonMenuItem item = null;
            int count = 0;
            for (int d = MIN_ZOOM; d <= MAX_ZOOM; d += ZOOM_STEP) {
                item = new JRadioButtonMenuItem("" + d + "%");
                item.setActionCommand("" + d);
                item.addActionListener(this);
                super.add(item);
                group.add(item);
                zoomItemModels[count++] = item.getModel();
                if (d == DEFAULT_ZOOM) {
                    item.setSelected(true);
                    group.setSelected(item.getModel(), true);
                }
            }
        }

        public void select(int scale) {
            if (CAT.isDebugEnabled())
                CAT.debug("selecting zoom=" + scale + "%");
            group.setSelected(zoomItemModels[(scale - MIN_ZOOM) / ZOOM_STEP], true);
        }

        public void actionPerformed(ActionEvent e) {
            int iScale;
            try {
                String s = e.getActionCommand();
                iScale = Integer.parseInt(s);
            }
            catch (NumberFormatException ne) {
                iScale = 10;
                Global.debug(this, "bad zommmenu action command. using default 100%");
            }
            zoom(iScale);
        }

    }

    private class TrackMenu extends JMenu{
    
        JRadioButtonMenuItem trackNothingButton = new JRadioButtonMenuItem(Message.say("AusgabeView","trackNothing"));
        JRadioButtonMenuItem trackAction = new JRadioButtonMenuItem(Message.say("AusgabeView","trackAction"));
        JMenu trackBot = new JMenu(Message.say("AusgabeView","trackBot"));
        ButtonGroup trackButtons = new ButtonGroup();
        
        public TrackMenu() {
            super(Message.say("AusgabeView", "trackMenu"));
            createAndAddBotRadioButtons();
            addOtherButtons();
            trackNothingButton.setSelected(true);
            currentTrackMode = TRACKMODE_NOTHING;           
            
            this.add(trackNothingButton);
            this.add(trackAction);
            this.add(trackBot);
        }
        
  
        private void addOtherButtons() {
            trackButtons.add(trackAction);
            trackButtons.add(trackNothingButton);
            trackNothingButton.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    trackNothingButton.setSelected(true);
                    AusgabeView.this.currentTrackMode = TRACKMODE_NOTHING;
                }
            });
            trackAction.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    trackAction.setSelected(true);
                    AusgabeView.this.currentTrackMode = TRACKMODE_ACTION;
                }
            });
        }
        
        private void createAndAddBotRadioButtons() {
            if (robotStatus!=null){              
                      Enumeration e = robotStatus.elements();
                      // int i=0;
                      while (e.hasMoreElements()){
                        RobotStatus rs = (RobotStatus) e.nextElement();
                        Bot robot = rs.getRobot();        
                        
                        ImageIcon image = new ImageIcon(BotVis.getRobotImages()[robot.getBotVis()]);
                        final String name = robot.getName();                            
                        final JRadioButtonMenuItem botItem = new JRadioButtonMenuItem(name,image);
                        trackBot.add(botItem);
                        trackButtons.add(botItem);
                     //   final ButtonModel itemModel = botItem.getModel();
                     //   buttons[i++]= botItem;
                      //  trackItemModels[i] = itemModel;                                                    
                        botItem.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e){
                               AusgabeView myView = AusgabeView.this;
                               RobotStatus stat  = (RobotStatus)  myView.robotStatus.get(name);
                               myView.botToTrack = stat.getRobot();
                               myView.currentTrackMode = TRACKMODE_BOT;
                               botItem.setSelected(true);
                         //      trackButtons.setSelected(itemModel, true);
                            }
                        });
                      }
                    }
                    else {
                      CAT.debug("robotstatus was null! Failed to create TrackMenu items");                      
                    }
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
    	ButtonGroup speedGroup;
    	JMenuItem customizeButton = new JMenuItem(Message.say("AusgabeFrame", "mCustomize"));
        SpeedMenu () {
	        super(Message.say("AusgabeFrame","mSpeed"));
	        speedGroup = new ButtonGroup();
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
	        customizeButton.addActionListener(this);
	        this.add(customizeButton);
        }

        public void actionPerformed(ActionEvent e) {
		    String message = null;	      
		 
	      	if (e.getSource()==customizeButton){    
	      	  if (speedSettingEditor.getState() == JFrame.ICONIFIED) {
	              speedSettingEditor.setState(JFrame.NORMAL);
	          }
	      	    speedSettingEditor.setVisible(true);
	      	    speedSettingEditor.toFront();
	      	    
	      	}
	      	else {
		      	if (e.getSource() == lSpeed) {
					currentSpeedConfig = speedSettingSlow;
					message = "gAufLang";	
			    }
		      	else if (e.getSource() == hSpeed){
		      	  currentSpeedConfig = speedSettingFast;
		    		message = "gAufUn";    	
		    	}
			    else  {
			        currentSpeedConfig = speedSettingMedium;
					message = "gAufMitt";              
		        }
		      	
			    setAnimationSpeed(currentSpeedConfig);
		      	showActionMessage(Message.say("AusgabeFrame",message));
	      	}
	}
        private void setAnimationSpeed(AnimationConfig conf){
           currentSpeedConfig = conf;
           if (currentSpeedConfig.equals(speedSettingSlow)){
               speedGroup.setSelected(lSpeed.getModel(),true);
           }
           else if (currentSpeedConfig.equals(speedSettingFast)){
               speedGroup.setSelected(hSpeed.getModel(),true);
           }
           else {
               speedGroup.setSelected(mSpeed.getModel(),true);
           }
           Conf.setProperty(PROPERTY_DEFAULT_SPEED, conf.getConfigName());
           Conf.saveProperties();
           gameBoardCanvas.setAnimationSettings(conf);
        }



   }
   private class HelpMenu extends JMenu {
      HelpMenu () {
        super (Message.say("AusgabeFrame","mHelpMenuName"));
       // TODO include Jude's manual, add title of MenuItem to message bundles
        /*JMenuItem manual = new JMenuItem(Message.say("AusgabeFrame", "manual"));
        manual.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                
            }
        });
        */
        JMenuItem about = new JMenuItem(Message.say("AusgabeFrame","mAbout"));
        about.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    new AboutFenster();
			}
	    });
        add(about);
        
      }

    }




   private class JumpToFlagMenu extends JMenu {
      JumpToFlagMenu(Location [] flagPos) {
        super(Message.say("AusgabeView", "flag"));
        init (flagPos);
      }

      void init(Location [] newflags) {
        if (newflags!=null){
          for (int i=0;i<newflags.length;i++){
            final int flagNr = i+1;
            JMenuItem flag = new JMenuItem(" #"+flagNr);
            flag.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (CAT.isDebugEnabled()) {
                        CAT.debug("Showing Flag:"+flagNr);
                      }                     
                     jumpToFlag(flagNr);
                }
            });           
            this.add(flag);
            if (CAT.isDebugEnabled()){
              CAT.debug("ScrollFlag - added flag: #"+(i+1)+" x:"+newflags[i].x+" y:"+newflags[i].y);
            }
          }
        }
        else
          CAT.debug("flag array for ScrollFlagMenu was null!");
      }

   }



   private class JumpToRobMenu extends JMenu {
       JumpToRobMenu() {
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
                   CAT.debug( "imgs is: " + BotVis.getRobotImages() );
                   JMenuItem rob = new JMenuItem(robot.getName(),new ImageIcon(BotVis.getRobotImages()[robot.getBotVis()]));
                   rob.addActionListener(new JumpToRobListener(rs));
                   this.add(rob);
               }
           }
           else
               CAT.debug("robotstatus was null! Failed to create ShowRobMenu items");
       }
       
   }

    private class JumpToRobListener implements ActionListener {
        RobotStatus rob;
        public JumpToRobListener(RobotStatus r){
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
                                             new ImageIcon(BotVis.getRobotImages()[robot.getBotVis()]));
              CAT.debug("Showing popup instead of robot, because robot is not on board..");
          }
          else {
            showPos(x, y, true, false);
          }
        }
    }

    public JComponent getBoardViewport() {
        return gameBoardView;
    }


    
    private Timer phaseTimer;
    protected void displayPhaseNumber(int phase, long showThatManyMS ){
      
        if (showThatManyMS<=0){
            return;
        }
        TimerTask displayStopper = new TimerTask(){
            public void run(){
                if (gameBoardCanvas != null) {
                    gameBoardCanvas.setIsPhaseNumToBePainted(false);
                    gameBoardCanvas.repaint();
                }
                
            }
        };
        
        phaseTimer = new Timer();
        phaseTimer.schedule(displayStopper,showThatManyMS);        
        gameBoardCanvas.setPhaseNumber(phase);
        gameBoardCanvas.setIsPhaseNumToBePainted(true);
        gameBoardCanvas.repaint();
            
    }
    
   
    protected void initHotKeysAndAddToHotkeyman(HotKeyMan keyMan) {
        
        HotKeyAction toggleBotVisibility = new HotKeyAction() {           
            public void actionPerformed(ActionEvent e) {                      
                if (showHideMenu.botsVisible) {                                     
                    showHideMenu.hideAllRobots();
                }
                else {
                    showHideMenu.showAllRobots();
                }
               
                    
                }
            };
        keyMan.addHotKey(new HotKey(HotKeyConf.HOTKEY_TOGGLE_BOT_VISIBILITY, 
                        								toggleBotVisibility));
        
        HotKeyAction zoomIn = new HotKeyAction() {
            public void actionPerformed(ActionEvent ae) {
                zoomIn();
            }
        };
        keyMan.addHotKey(new HotKey(HotKeyConf.HOTKEY_ZOOM_IN, zoomIn));

        HotKeyAction zoomOut = new HotKeyAction() {
            public void actionPerformed(ActionEvent ae) {
                zoomOut();
            }
        };
        keyMan.addHotKey(new HotKey(HotKeyConf.HOTKEY_ZOOM_OUT, zoomOut));

        Location[] finalFlags = gameBoardCanvas.getFlags();
        int num = finalFlags.length;

        for (int i = 0; i < num; i++) {           
            final Location pos = finalFlags[i];
            HotKeyAction showFlagAction = new HotKeyAction() {              
                public void actionPerformed(ActionEvent ae) {                 
                    showPos(pos.getX(), pos.getY(), true, false);
                }
            };
            String keynameForFlagX = HotKeyConf.HOTKEY_SHOW_FLAG_X[i];
            keyMan.addHotKey(new HotKey(keynameForFlagX, showFlagAction));
        }
        
        // Important: don't call this method before all keys are created and  added to the Keymanager!
        createHotkeyEditFrame(keyMan);
    }

    private JFrame hotKeyFrame;
    /** Important: don't call this method before all keys are created and added to the Keymanager!
     *  (The EditFrame won't show fields for the keys that are added to the Keyman after this method was called)*/
    private void createHotkeyEditFrame(HotKeyMan keyMan){
        hotKeyFrame = new JFrame(Message.say("AusgabeView", "hotkeyMenu"));
   
        hotKeyFrame.setContentPane(new PaintPanel(OptionPane.getBackgroundPaint(hotKeyFrame), true));
        hotKeyFrame.getContentPane().add(new HotKeyEditorPanel(keyMan));
        final HotKeyMan finalkeyMan = keyMan;
        hotKeyFrame.addWindowListener(new WindowAdapter() {  	   
	       public void windowClosing(WindowEvent e){
	                    CAT.debug("saving properties to save hotkey settings");
	                    finalkeyMan.save(); // important, because changes done to
	                                          // chatmessages will not be saved by
	                                          // ChatMessageEditor or HotKeyEditorPanel
	                    hotKeyFrame.setVisible(false);
	
	                  }
	     });
	    hotKeyFrame.pack();	  
    }

    private void showEditHotKeys() {
      if (hotKeyFrame == null) {
          CAT.error("HotkeyFrame is still NULL! "+
                             "Make sure to call initHotKeysAndAddToHotkeyman before "+
                             "as the Frame will be initinalized there!");
      }
 

      if (hotKeyFrame.getState()==JFrame.ICONIFIED)
        hotKeyFrame.setState(JFrame.NORMAL);

      hotKeyFrame.toFront();
      hotKeyFrame.setVisible(true);      
  

    }

   
    protected AnimationConfig getCurrentSpeedSettings() {
        return currentSpeedConfig;
    }
    
    
    private void checkAndDoTracking(){
        checkAndDoTracking(null);
    }
   
    
    private void checkAndDoTracking(Location actionPosForActionTracking){
        
        
        
       if (currentTrackMode == TRACKMODE_BOT){
           
           if (botToTrack == null) {
                CAT.error("the bot to track was null!");
            }
            else {
                int x = botToTrack.getX();
                int y =  botToTrack.getY();
                if ( botToTrack.isInPit()){ 
                    x = botToTrack.getArchiveX();
                    y = botToTrack.getArchiveY();
                }
                showPos(x,y, false, true, 0); // 0==scroll everytime
            }
        }
       else if (currentTrackMode == TRACKMODE_ACTION && actionPosForActionTracking != null){
           showPos(actionPosForActionTracking.x, actionPosForActionTracking.y, false, true);
           //waitSomeTime(500);
           
       }
       else {
           // TRACKMODE_NOTHING or unknown => do nothing
       }
        
    }
    
    private void waitSomeTime(int ms, Object lock){
        if (ms<1){
            return;
        }
       synchronized(lock) {
           try {
               lock.wait(ms);
           }
           catch (InterruptedException ie){
               CAT.error(ie);
           }
       }
    }
    /**
    * Centers the view on the given flag and highlight it.
    * 
    * @param the number of the flag to center on (possible values: [1-6])
    */
   public void jumpToFlag(int nr) {
       if (nr > 0 && nr <= flags.length) {
           showPos(flags[(nr - 1)].getX(), flags[(nr - 1)].getY(), true, false);
           if (BoardView.DEBUG_DISTANCE_CALC) {
               gameBoardCanvas.setDebugBotNextFlag(nr);
           }
       }
   }
}


