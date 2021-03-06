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

package de.botsnscouts.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import de.botsnscouts.BotsNScouts;
import de.botsnscouts.autobot.AdvDistanceCalculator;
import de.botsnscouts.autobot.DistanceCalculator;
import de.botsnscouts.board.Board;
import de.botsnscouts.board.FlagException;
import de.botsnscouts.util.Conf;
import de.botsnscouts.util.FormatException;
import de.botsnscouts.util.Global;
import de.botsnscouts.util.ImageMan;
import de.botsnscouts.util.Location;
import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.GreenTheme;

@SuppressWarnings("serial")
public class BoardEditor extends JFrame implements WindowListener, ActionListener {

    public static final Category CAT = Category.getInstance(BoardEditor.class);

    protected EditorBoardView boardView = null;

    protected ButtonBar dp = null;

    protected JScrollPane sp = null;

    protected JScrollPane sp2 = null;

    protected Image[] cbeltCrop, ebeltCrop, diverseCrop;

    protected Image[] images;

    protected JPanel but;

    protected JToggleButton[] buttons;

    protected ButtonGroup felder;

    protected EditableBoard board = null;

    protected int elemX = 0, elemY = 0, indx = 0;

    protected int spfX = 0, spfY = 0;

    protected int phasen = 0;

    private String magicBoardString = null;

    protected int laserSt = 1;

    protected int[] elemTyp = { 122, 123, 131, 132, 102, 121, 120, 130, 133, 103, 122, 123, 131, 132, 100, 120, 121,
            133, 130, 101, 152, 153, 150, 151, 222, 223, 231, 232, 202, 221, 220, 230, 233, 203, 222, 223, 231, 232,
            200, 221, 220, 230, 233, 201, 252, 253, 250, 251, 0, 10, 10, -1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0 };

    protected int[] elemSpez = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 2, 2, 2, 2, 2, 2, 1, 0, 1, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0 };

    public BoardEditor() {
        loadImg();
        initSpF();
        BorderLayout lay = new BorderLayout();
        getContentPane().setLayout(lay);

        setTitle(Message.say("BoardEditor", "mTitel"));

        Dimension dim = BotsNScouts.getScreenSize();

        sp = new JScrollPane();
        sp.getViewport().setView(boardView);
        sp.getViewport().setOpaque(false);
        getContentPane().add(BorderLayout.CENTER, sp);

        felder = new ButtonGroup();
        but = new JPanel();

        buttons = new JToggleButton[66];

        if (dim.width >= 768) {
            but.setLayout(new GridLayout((buttons.length + 2) / 3, 3));// 3
                                                                       // spalten
        }
        else {
            but.setLayout(new GridLayout((buttons.length + 1) / 2, 2));// 2
                                                                       // spalten
        }

        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new JToggleButton(new ImageIcon(images[i]));
            buttons[i].addActionListener(this);
            buttons[i].setActionCommand("" + i);
            but.add(buttons[i]);
            felder.add(buttons[i]);
        }

        sp2 = new JScrollPane();
        sp2.getViewport().setView(but);
        sp2.getViewport().setOpaque(false);
        getContentPane().add(BorderLayout.EAST, sp2);

        dp = new ButtonBar(this);
        getContentPane().add(BorderLayout.NORTH, dp);

        // addMouseListener(this);
        addWindowListener(this);

        setSize(dim.width, dim.height - 70);
        setLocation(0, 25);
        setVisible(true);
    }

    protected String getMagicBoardString() {
        return magicBoardString;
    }

    protected void setMagicBoardString(String magicBoardString) {
        this.magicBoardString = magicBoardString;
    }

    private boolean loadImg() {
        // enno: habe eingebaut, dass BoardEditor und BoardView die gleichen
        // bilder benutzen und nicht jeder sie einzeln laedt.
        ebeltCrop = ImageMan.waitForImages(ImageMan.EBELTS);
        cbeltCrop = ImageMan.waitForImages(ImageMan.CBELTS);
        diverseCrop = ImageMan.waitForImages(ImageMan.DIVERSE);

        int j = 0;
        int i = 0;
        images = new Image[ebeltCrop.length + cbeltCrop.length + diverseCrop.length];

        for (i = 0; i < ebeltCrop.length; i++, j++)
            images[j] = cbeltCrop[i];
        for (i = 0; i < cbeltCrop.length; i++, j++)
            images[j] = ebeltCrop[i];
        for (i = 0; i < diverseCrop.length; i++, j++)
            images[j] = diverseCrop[i];

        setIconImage(cbeltCrop[19]);

        Global.debug(this, "Bilder geladen");

        return true;
    }// ende loadImg

    protected boolean initSpF() {
        String gr1 = "____________\n";
        String gr2 = "_B_B_B_B_B_B_B_B_B_B_B_B_\n";
        try {
            board = new EditableBoard(12, 12, new String(gr1 + gr2 + gr1 + gr2 + gr1 + gr2 + gr1 + gr2 + gr1 + gr2
                            + gr1 + gr2 + gr1 + gr2 + gr1 + gr2 + gr1 + gr2 + gr1 + gr2 + gr1 + gr2 + gr1 + gr2 + gr1),
                            null);
        }
        catch (FormatException e) {
            System.err.println("Oups!" + e);
            return false;
        }
        catch (FlagException ex) {
            System.err.println("Oups!" + ex);
            return false;
        }
        boardView = new EditorBoardView(board, this);
        return true;
    }

    void dumpPngImage(File file) throws IOException {
        boardView.dumpPngImage(file);
    }

    public void setFlag(int x, int y) {
        if (dp.advancedFeaturesEnabled()) {
            CAT.debug("setting flag to " + x + ", " + y);
            board.setFlags(new Location[] { new Location(x, y) });
            DistanceCalculator calc = AdvDistanceCalculator.getInstance(board);
            calc.preCalculate();
            boardView.setCalc(calc);
            repaint();
        }
    }

    /**
     * Load a new file into the editor.
     * 
     * @param file
     */
    void loadTileFile(File file) {
        String save = getMagicBoardString();
        try {
            magicBoardString = Board.readMagicString(file);
            // Leo's Code
            board = new EditableBoard(12, 12, magicBoardString, null);
            CAT.debug("Board erzeugt");

            sp.remove(boardView);
            CAT.debug("sac removed");
            boardView = new EditorBoardView(board, this);
            CAT.debug("sac neu erzeugt");
            sp.getViewport().setView(boardView);

            CAT.debug("sac added");
        }
        catch (FormatException ex) {
            System.err.println(Message.say("BoardEditor", "eDatNotEx") + ex);
            magicBoardString = save;
        }
        catch (FlagException ex) {
            System.err.println(Message.say("BoardEditor", "eDatNotEx") + ex);
            magicBoardString = save;
        }
        catch (IOException ex) {
            System.err.println(Message.say("BoardEditor", "eDateiErr") + ex);
            magicBoardString = save;
        }
    }

    public void actionPerformed(ActionEvent e) {
        String a = e.getActionCommand();
        try {
            indx = Integer.parseInt(a);
            CAT.debug("button #" + indx + " clicked");
        }
        catch (NumberFormatException d) {
            CAT.error("Boo: " + a, d);
        }
    }

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
        this.dispose();
        // System.exit(0);
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public static void main(String[] argv) throws Throwable {
        try {
            PropertyConfigurator.configure(BotsNScouts.class.getResource("conf/log4j.conf"));

            Locale myLocale = null;
            String loc = Conf.getProperty("language.isSet");
            if (loc != null) {
                myLocale = new Locale(Conf.getProperty("language.lang"), Conf.getProperty("language.country"));
            }
            else {
                Locale[] list = Message.getLocales();
                String[] locals = new String[list.length];
                for (int i = 0; i < locals.length; i++) {
                    locals[i] = list[i].getDisplayLanguage();
                }
                int sel = JOptionPane.showOptionDialog(null, "Please select your Language", "Language selection",
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, locals, locals[0]);
                if (sel == JOptionPane.CLOSED_OPTION) {
                    myLocale = new Locale("en", "US");
                }
                else {
                    myLocale = list[sel];
                    Conf.setProperty("language.isSet", "yes");
                    Conf.setProperty("language.lang", myLocale.getLanguage());
                    Conf.setProperty("language.country", myLocale.getCountry());
                    Conf.saveProperties();
                }
            }
            Message.setLanguage(myLocale);

            MetalLookAndFeel.setCurrentTheme(new GreenTheme());
            new BoardEditor();
        }
        catch (Throwable t) {
            CAT.fatal("Exception:", t);
            throw t;
        }
    }

}// end class BoardEditor