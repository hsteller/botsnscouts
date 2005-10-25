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

package de.botsnscouts.start;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Category;

import de.botsnscouts.BotsNScouts;
import de.botsnscouts.gui.Splash;
import de.botsnscouts.server.Server;
import de.botsnscouts.util.BNSThread;
import de.botsnscouts.util.Conf;
import de.botsnscouts.util.KrimsKrams;
import de.botsnscouts.util.Message;
import de.botsnscouts.util.Registry;
import de.botsnscouts.util.SoundMan;
import de.botsnscouts.util.Task;
import de.botsnscouts.widgets.OptionPane;

/**
 * 
 * 
 * @version $Id$
 */
public class Start extends JFrame implements WindowListener {
	/**
	 * Duh, logging
	 */
	private static final Category CAT = Category.getInstance(Start.class);

	/**
	 * This seems to be the Container for the texture the background image for
	 * nearly all Windows It is located at \de\botsnscouts\images\garage2.jpg A
	 * more customizable solution should be found
	 */
	// argh! Now I realize why this is PROTECTED! Nobody ever extends it!
	// This is an synthetic extension of the visibility for package members
	// protected is removed now. We should make this private and add get/set
	Paint paint;

	/**
	 * this Cardlayout shold be outsourced in an extra layout class, where we
	 * can store all the layout methods
	 */
	private CardLayout layout = new CardLayout();

	// these two seem never to be used seriously;
	// ServerObserver has a call to showLastShown() that uses them; used to keep track which Card
	// was shown the before the current one.
	// Reason: press a button in one of the menus
	//             -> button kicks off some background action (like starting the server),
	//                  showing the "doing stuff"-card in the meantime 
	//             -> background action fails for some reason
	//             -> we want to go back to the Card/Menu where the button was pressed 
	//             
 
	private String lastShown = MAIN_MENU;
	private String currentlyShowing = MAIN_MENU;

	/*
	 * All the funny menus
	 */
	/** Start screen, the first Menu to be seen*/
	private MainMenu mainMenu;
	private static String MAIN_MENU = "main";

	/** build your board */
	private FieldEditor fieldEditor;
	private static String FIELD_EDITOR = "fieldedit";

	/** participating panel */
	private JoinGamePanel partPanel;
	private static String PARTICIPATE = "participate";

	/** watching panel */
	private WatchPanel watchPanel;
	private static String WATCH = "watch";

	// Second level in depth
	/** Will be displayed after pressing "Spiel starten/Host Game"*/
	private DoingStuffPanel busyPanel;
	private static String BUSY = "busy";

	/**
	 * Choose your robot and board
	 */ 
	protected GameFieldPanel gameFieldPanel;
	private static String GAME_FIELD = "gamefield";

	// Third level in depth
	/** 
	 * Screen with all information, Autorobot chooser and start button to
	 * finally launch the game
	 * */
	private StartPanel startPanel;
	private static String START = "start";
	// Menus End
	
	/**
	 * This is set in the main method and then ass raped for the window listener
	 * This can be static at least and using the java.awt.event.WindowAdapter
	 * 
	 */
	private Splash splash;

	/**
	 * the singleton Instance
	 */
	private static Start globalStart;

	/**
	 * Gets activated every time a panel is changed
	 * 
	 */
	private Thread animatorThread;
	
	/**
	 * This is used for some wicked synchronization
	 * Comments here? 
	 * TODO(Comment all the wth stuff)
	 */
	private WaiterThread wth;
	
	/**
	 * The facade for this game instance, it seems to be more
	 * sensefull(lol, germandishing) to put the facade in the 
	 * registry singleton, so we can call 
	 * Registry.getInstance().getFacade().useMe(1MioKisses)
	 * instead of all these parent.facade.xyz() calls
	 * It breaks the information hiding concept!
	 */
	Facade facade;
	
	/**
	 * This is the private Constructor, you can get an instance by
	 * calling the @see getLauncherAppSingleton
	 * @param splash The Splash which will be hidden
	 */
	private Start(Splash splash) {
		// setting the title with the localized string
		super(Message.say("Start", "mStartTitel"));

		// again the splash, see above
		this.splash = splash;
		
		getContentPane().setLayout(layout);

		wth = new WaiterThread(this);
		Dimension ssize = BotsNScouts.getScreenSize();
		if (ssize.height < 600) {
			facade = new Facade(150);
		} else {
			facade = new Facade();
		}
		setSize(ssize);
		setLocation(0, 0);

		paint = OptionPane.getBackgroundPaint(this);

		mainMenu = new MainMenu(this);
		getContentPane().add(mainMenu, MAIN_MENU);

		showMainMenu();

		busyPanel = new DoingStuffPanel(paint);
		getContentPane().add(busyPanel, BUSY);

		addWindowListener(this);
		show();
		this.toFront();
	}

	/**
	 * Switches through the card layout by the given paramter <code>to</code>.
	 * <br>
	 * For example:<br>
	 * the CardLayout is initialized with the cards name Hendrik, Miriam and
	 * Igzorn <br>
	 * a call switchCard("Hendrik") would show the items which were added to the
	 * card "Hendrik" <br>
	 * If no such card exists nothing happens
	 * 
	 * @param to
	 *            The card u are looking for
	 */
	private void switchCard(String to) {
		layout.show(getContentPane(), to);
		// updating the last shown card with the one shown before the switch above
		lastShown = currentlyShowing;
		// updating the card that is shown now
		currentlyShowing = to; 
	}

	/**
	 * Will switch back to the card that was shown before the one that is currently displayed;
	 * think of javascript's "history(-1)" call and a "history" with the maximum length of 1. 
	 * 
	 */
//	 Don't delete me (yet). 
//	 * While there isn't a single _active_ reference in the whole project, there still is one in 
//	 * comments in ServerObserver. 
//	 * Also, I like the unused functionality more than 

	protected void showLastShown() {
		switchCard(lastShown);
	}

	
	/**
	 * Kill this; uses the System.exit(0) command after dispose
	 */
	public void myclose() {
		dispose();
		System.exit(0);
	}

	/** Does (atm) nothing else than calling super.dispose()*/
	public void dispose() {
		super.dispose();
	}

	
	/**
	 * Displays the Main menu by switching to the layoutcard for the very first screen
	 */
	public void showMainMenu() {
		switchCard(MAIN_MENU);
		mainMenu.unrollOverButs();
		setTitle(Message.say("Start", "mStartTitel"));
		this.toFront();
		// setVisible(true);
	}
	
	/**
	 * Displays the layoutcard where u can choose your robot and start the game
	 *
	 */
	protected void showGameFieldPanel() {
	// All of the following Methods showXXXPanel follow the same schema:
		// Show the Busy Panel
		// make a thread
		// in which we DO show the panel 
		showBusy(Message.say("Start", "mLoadGameFieldPanel"));
		new Thread(new Runnable() {
			public void run() {
				if (gameFieldPanel == null) {
					gameFieldPanel = new GameFieldPanel(Start.this);
					getContentPane().add(gameFieldPanel, GAME_FIELD);
				}

				CAT.debug("setting gameFieldPanel");
				switchCard(GAME_FIELD);
				stopBusy();
			}
		}).start();
	}

	/**
	 * Displays the layoutcard where u can participate in a online game 
	 */
	protected void showParticipatePanel() {
		showBusy(Message.say("Start", "mLoadParticipatePanel"));
		new Thread(new Runnable() {
			public void run() {
				if (partPanel == null) {
					partPanel = new JoinGamePanel(Start.this);
					getContentPane().add(partPanel, PARTICIPATE);
				}
				switchCard(PARTICIPATE);
				stopBusy();
			}
		}).start();
	}

	/**
	 * Displays the layoutcard where u can watch a online game 
	 */
	protected void showWatchPanel() {
		showBusy(Message.say("Start", "mLoadWatchPanel"));
		new Thread(new Runnable() {
			public void run() {
				if (watchPanel == null) {
					watchPanel = new WatchPanel(Start.this);
					getContentPane().add(watchPanel, WATCH);
				}
				switchCard(WATCH);
				stopBusy();
			}
		}).start();
	}

	/**
   * Displays the layoutcard where u can build your board 
   * @param spf why do we need this?
   */
	protected void showFieldEditor(final FieldGrid spf) {
		showBusy(Message.say("Start", "mLoadFieldEditor"));
		new Thread(new Runnable() {
			public void run() {
				if (fieldEditor == null) {
					fieldEditor = new FieldEditor(Start.this, spf);
					getContentPane().add(fieldEditor, FIELD_EDITOR);
				} else {
					fieldEditor.spf.addTileClickListener(fieldEditor);
					fieldEditor.fuerSpf.add(fieldEditor.spf);
				}
				facade.saveTileRaster();
				switchCard(FIELD_EDITOR);
				stopBusy();
			}
		}).start();
	}

	/**
	 * Shows the panel where u can add the autorobots, see the other players
	 * and start the game
	 * @param postServerStartTask we will call doIt! :-)
	 */
	protected void showNewStartPanel(final Task postServerStartTask) {
		showBusy(Message.say("Start", "mStartingServer"));
		new Thread(new Runnable() {
			public void run() {
				try {
					// TODO(HS_TODO)
					if (startPanel == null) {
						startPanel = new StartPanel(Start.this);
						getContentPane().add(startPanel, START);
					}
					ServerObserver foo = new ServerObserver(startPanel
							.getPlayersPanel());
					Server server = facade.startGame(foo);// starting game
					addServer();
					postServerStartTask.doIt();

					switchCard(START);
					stopBusy();
				} catch (OneFlagException ex) {
					JOptionPane.showMessageDialog(Start.this, Message.say(
							"Start", "mZweiFlaggen"), Message.say("Start",
							"mError"), JOptionPane.ERROR_MESSAGE);

				} catch (NonContiguousMapException exc) {
					JOptionPane.showMessageDialog(Start.this, Message.say(
							"Start", "mNichtZus"), Message.say("Start",
							"mError"), JOptionPane.ERROR_MESSAGE);

				}
			}
		}).start();

	}

	/**
	 * Shows the busy panel, ya the busy panel, is more a doingStuff panel
	 * @see de.botsnscouts.start.DoingStuffPanel
	 * @param txt the String we'll display
	 */
	protected void showBusy(String txt) {
	/*
	 * This was an external declaration but is ONLY
	 * used in this method. We will put things together
	 * which belong to each other
	 */
		/**
		 * uses busyPanel.inc()
		 */
		Runnable busyAnimator = new Runnable() {
			public void run() {
				while (!Thread.currentThread().isInterrupted()) {
					busyPanel.inc();
					try {
						Thread.sleep(50);
					} catch (InterruptedException ex) {
						break;
					}
				}
				CAT.debug("Animatorthread exiting.");
			}
		};

		CAT.debug("setting busypanel. txt: " + txt);
		busyPanel.setText(txt);
		/** we are busy now*/
		switchCard(BUSY);
		/** using the declared Runnable from above*/
		animatorThread = new Thread(busyAnimator);
		animatorThread.start();
	}
	
	/**
	 * Stops the bysypanellayoutcard displaying
	 */
	protected void stopBusy() {
		animatorThread.interrupt();
	}

	
	
	protected WaiterThread getWaiterThread() {
		return wth;
	}

	public synchronized void addKS(BNSThread k) {
		wth.addThread(k);
	}
	public synchronized void addServer() {
		wth.setServer();
	}


	private synchronized void resetWaiter() {
		wth.quitYourself();
		wth.reset();
		wth = new WaiterThread(this);
	}
	
    public void reset() {
		facade.killServer();
		getContentPane().remove(startPanel);
		startPanel = null;
		// startPanel.recreateServerObeserver();
		// getWaiterThread().stopAllWaitingThreads();
		resetWaiter();
	}
    
	private void beenden() {
		CAT.debug("beenden() wurde aufgerufen");
		try {
			if (!wth.isAlive()) {
				wth.start();
			}
		} catch (IllegalThreadStateException e) {
			CAT.error(Message.say("Start", "eSpielEnde"));
		}
		CAT.debug("disposing start frame");
		dispose();
		CAT.debug(this);
	}

	
	/**
	 * To obtain the global "Start" object; will exist after
	 * Start.main(String[]) was called.
	 * 
	 * @param splash a Splash that must be hidden before the main menu is displayed
	 * @return The global "Start" object
	 */
	private  static Start getLauncherAppSingleton(Splash splash) {		
		  if (globalStart==null) { 		     
		      globalStart=new Start(splash); 
		  }
		  return globalStart;	    
	}

	public  static Start getLauncherAppSingleton() {		
	    return getLauncherAppSingleton(null);
	}
	
	/**
	 * Initializes the game registry, the sounds and language via the
	 * <code>argv<\code> parameter array OR by interrogating
	 * the user
	 * Hides the Splash screen
	 * @param argv the parameters for the developer quickstart
	 * @param splash the Splash wich is displayed at the loading screen
	 */
	public static void main(String[] argv, Splash splash) {
		/**
		 * Initializing the Gameregistry, the Sounds and Language
		 */
		initBasics();

		if (argv.length != 3) { // "normal" case
			globalStart = getLauncherAppSingleton(splash);
		} else { // developer quickstart
			try {
				String spielfeld = argv[0];
				if (spielfeld.toLowerCase().endsWith(".spf")) {
					spielfeld = spielfeld.substring(0, spielfeld.length() - 4);
				}
				CAT.debug("Board " + spielfeld);
				GameFieldLoader loader = new GameFieldLoader();
				Facade fassade = new Facade();
				Properties prop = loader.getProperties(spielfeld);
				CAT.debug("Properties " + prop);
				fassade.loadBoardFromProperties(prop);
				CAT.debug("Spielfed loaded");
				fassade.startGame();
				CAT.debug("Server gestartet");
				if (argv[1].equals("yes")) {
					Facade.participateInAGame(KrimsKrams.randomName(), 0);
					CAT.debug("Human player started");
				} else {
					Facade.watchAGame();
					CAT.debug("View started");
				}
				int anzKS = 0;
				try {
					anzKS = Integer.parseInt(argv[2]);
					for (int i = 0; i < anzKS; i++) {
						Facade.startAutoBot(40, true);
						CAT.debug("A.I. Bot started");
					}
				} catch (NumberFormatException e) {
				}
				try {
					Thread.sleep((anzKS + 1) * 3000);
				} catch (InterruptedException e) {
					CAT.warn(e.getMessage());
				}
				fassade.gameStarts();
				CAT.debug("Spiel geht los");
				return;
			} catch (Exception e) {
				CAT.error(e.getMessage(), e);
				System.exit(1);
			}
		}

	}

	/**
	 * Initializing the Gameregistry, the Sounds and Language
	 */
	protected static void initBasics() {
		// enabling registry
		Registry.getSingletonInstance().setEnabled(true);

		// load Sounds
		CAT.debug("starting soundman..");
		SoundMan.loadSounds();
		CAT.debug("..done");

		// language conf
		Locale myLocale = null;
		String loc = Conf.getProperty("language.isSet");
		if (loc != null) {
			myLocale = new Locale(Conf.getProperty("language.lang"), Conf
					.getProperty("language.country"));
		} else {
			Locale[] list = Message.getLocales();
			String[] locals = new String[list.length];
			for (int i = 0; i < locals.length; i++) {
				locals[i] = list[i].getDisplayLanguage();
			}
			int sel = JOptionPane.showOptionDialog(null,
					"Please select your Language", "Language selection",
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, locals, locals[0]);
			if (sel == JOptionPane.CLOSED_OPTION) {
				myLocale = new Locale("en", "US");
			} else {
				myLocale = list[sel];
				Conf.setProperty("language.isSet", "yes");
				Conf.setProperty("language.lang", myLocale.getLanguage());
				Conf.setProperty("language.country", myLocale.getCountry());
				Conf.saveProperties();
			}
		}

		Message.setLanguage(myLocale);
		// ende language conf
	}

	/*
	 * Begin of Methods for:
	 * 
	 * @see java.awt.event.WindowListener
	 */
	public void windowOpened(WindowEvent e) {
		/* Finally hiding the splash */
	    if (splash != null ) {
	        splash.noSplash();
	    }
		
		CAT.debug("window opened");
		CAT.debug("triggering tilefactory");

		// DO WE REALLY NEED THIS?
		// I tested it and it seems to be useless,
		// if it is neccesary a triggered solution should be preferred
		// Needed so our window is actually drawn before triggering
		// tile-loading
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ie) {
				}
				facade.prepareTiles();
			}
		}).start();
	}

	/**
	 * Disposes the Window and terminates the currently running VM
	 */
	public void windowClosing(WindowEvent e) {
		myclose();
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}
	/*
	 * end java.awt.event.WindowListener
	 */

}// class Start end
