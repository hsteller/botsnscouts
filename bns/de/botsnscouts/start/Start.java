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

import de.botsnscouts.BotsNScouts;
import de.botsnscouts.gui.Splash;
import de.botsnscouts.util.*;
import de.botsnscouts.widgets.OptionPane;
import de.botsnscouts.widgets.GreenTheme;
import org.apache.log4j.Category;

import javax.swing.*;
import javax.swing.plaf.metal.MetalLookAndFeel;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Locale;
import java.util.Properties;

public class Start extends JFrame implements WindowListener {

    private static final Category CAT = Category.getInstance(Start.class);

    Facade facade;

    protected Paint paint;
    private MainMenu mainMenu;                // Start screen
    private static String MAIN_MENU = "main";
    protected GameFieldPanel gameFieldPanel;          //build your board
    private static String GAME_FIELD = "gamefield";
    private ParticipatePanel partPanel;        //participate
    private static String PARTICIPATE = "participate";
    private WatchPanel watchPanel;          //watch
    private static String WATCH = "watch";
    protected StartPanel startPanel;                  //Screen with all information and start button
    private static String START = "start";
    private FieldEditor fieldEditor;
    private static String FIELD_EDITOR = "fieldedit";
    private DoingStuffPanel busyPanel;
    private static String BUSY = "busy";
    private CardLayout layout = new CardLayout();

    protected WaiterThread wth;

    private Splash splash;

    private Start(Splash splash) {
        super(Message.say("Start", "mStartTitel"));

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
    }

    private void switchCard(String to) {
        layout.show(getContentPane(), to);
    }

    protected void showBusy(String txt) {
        CAT.debug("setting busypanel. txt: " + txt);
        busyPanel.setText(txt);

        switchCard(BUSY);

        animatorThread = new Thread(busyAnimator);
        animatorThread.start();
    }

    private Thread animatorThread;
    private Runnable busyAnimator = new Runnable() {
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

    protected void stopBusy() {
        animatorThread.interrupt();
    }

    public void showMainMenu() {
        switchCard(MAIN_MENU);
        mainMenu.unrollOverButs();
        setTitle(Message.say("Start", "mStartTitel"));
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
        splash.noSplash();
        CAT.debug("window opened");
        CAT.debug("triggering tilefactory");

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

    public void windowClosing(WindowEvent e) {
        myclose();
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void myclose() {
        dispose();
        System.exit(0);
    }

    public void addKS(Thread k) {
        wth.addThread(k);
    }

    public void addServer() {
        wth.setServer();
    }

    public void resetWaiter() {
        wth.quitYourself();
        wth.reset();
        wth = new WaiterThread(this);
    }

    public void beenden() {
        CAT.debug("beenden() wurde aufgerufen");
        try {
            if (!wth.isAlive()) {
                wth.start();
            }
        } catch (IllegalThreadStateException e) {
            System.err.println(Message.say("Start", "eSpielEnde"));
        }
        CAT.debug("disposing start frame");
        dispose();
        CAT.debug(this);
    }

    protected void showGameFieldPanel() {
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

    protected void showParticipatePanel() {
        showBusy(Message.say("Start", "mLoadParticipatePanel"));
        new Thread(new Runnable() {
            public void run() {
                if (partPanel == null) {
                    partPanel = new ParticipatePanel(Start.this);
                    getContentPane().add(partPanel, PARTICIPATE);
                }
                switchCard(PARTICIPATE);
                stopBusy();
            }
        }).start();
    }

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

    protected void showNewStartPanel(final Task postServerStartTask) {
        showBusy(Message.say("Start", "mStartingServer"));
        new Thread(new Runnable() {
            public void run() {
                try {
                    if (startPanel == null) {
                        startPanel = new StartPanel(Start.this);
                        getContentPane().add(startPanel, START);
                    }
                    facade.startGame(startPanel.getListener());//starte Spiel
                    addServer();
                    postServerStartTask.doIt();

                    switchCard(START);
                    stopBusy();
                } catch (OneFlagException ex) {
                    JOptionPane.showMessageDialog(Start.this, Message.say("Start", "mZweiFlaggen"), Message.say("Start", "mError"), JOptionPane.ERROR_MESSAGE);

                } catch (NonContiguousMapException exc) {
                    JOptionPane.showMessageDialog(Start.this, Message.say("Start", "mNichtZus"), Message.say("Start", "mError"), JOptionPane.ERROR_MESSAGE);

                }
            }
        }).start();

    }


    protected static void initBasics() {
        MetalLookAndFeel.setCurrentTheme(new GreenTheme());

        //load Sounds
        CAT.debug("starting soundman..");
        SoundMan.loadSounds();
        CAT.debug("..done");

        //language conf
        Locale myLocale = null;
        String loc = Conf.getProperty("language.isSet");
        if (loc != null) {
            myLocale = new Locale(Conf.getProperty("language.lang"), Conf.getProperty("language.country"));
        } else {
            Locale[] list = Message.getLocales();
            String[] locals = new String[list.length];
            for (int i = 0; i < locals.length; i++) {
                locals[i] = list[i].getDisplayLanguage();
            }
            int sel = JOptionPane.showOptionDialog(null, "Please select your Language", "Language selection", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, locals, locals[0]);
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
        //ende language conf

    }


    public static void main(String[] argv, Splash splash) {
        initBasics();

        if (argv.length >= 4) {
            try {
                String spielfeld = argv[1];
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
                if (argv[2].equals("yes")) {
                    fassade.participateInAGame(KrimsKrams.randomName(), 0);
                    CAT.debug("Menschlichen Spieler gestartet");
                } else {
                    fassade.watchAGame();
                    CAT.debug("Ausgabe gestartet");
                }
                int anzKS = 0;
                try {
                    anzKS = Integer.parseInt(argv[3]);
                    for (int i = 0; i < anzKS; i++) {
                        fassade.startAutoBot(40, true);
                        CAT.debug("Künstlichen Spieler gestartet");
                    }
                } catch (NumberFormatException e) {
                }
                try {
                    Thread.sleep((anzKS + 1) * 3000);
                } catch (InterruptedException e) {
                    System.err.println(e);
                }
                fassade.gameStarts();
                CAT.debug("Spiel geht los");
                return;
            } catch (Exception e) {
                System.err.println(e);
            }
        } else {
            new Start(splash);
        }
    }

}//class Start end