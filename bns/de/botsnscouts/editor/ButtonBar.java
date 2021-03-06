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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Category;

import de.botsnscouts.util.Message;

@SuppressWarnings("serial")
class ButtonBar extends JPanel implements ActionListener {

    static final Category CAT = Category.getInstance(ButtonBar.class);

    private final static String DEFAULT_SAVE_DIR = "tiles";

    private JButton feldSpeich = null;

    private JButton feldLaden = null;

    private JButton feldClear = null;

    private JButton exit = null;

    private JButton export = null;

    private JCheckBoxMenuItem advanced = null;

    protected BoardEditor editor = null;

    JFileChooser chooser;

    FileFilter rraFilter = new FileFilter() {
        public boolean accept(File file) {
            String name = file.getName();
            return file.isDirectory() || name.toLowerCase().endsWith(".rra");
        }

        public String getDescription() {
            return "rra";
        }
    };

    FileFilter pngFilter = new FileFilter() {
        public boolean accept(File file) {
            String name = file.getName();
            return file.isDirectory() || name.toLowerCase().endsWith(".png");
        }

        public String getDescription() {
            return "png";
        }
    };

    void makeChooser() {
        chooser = new JFileChooser(DEFAULT_SAVE_DIR);
        chooser.setFileFilter(rraFilter);
    }

    public ButtonBar(BoardEditor p) {
        editor = p;
        setLayout(new BorderLayout());
        JToolBar tb = new JToolBar();
        // setLayout(new GridLayout(1,3));

        feldLaden = new JButton(Message.say("BoardEditor", "bLaden"));
        feldLaden.addActionListener(this);
        feldLaden.setActionCommand("Laden");
        tb.add(feldLaden);

        feldSpeich = new JButton(Message.say("BoardEditor", "bSpeichern"));
        feldSpeich.addActionListener(this);
        feldSpeich.setActionCommand("Speichern");
        tb.add(feldSpeich);

        feldClear = new JButton(Message.say("BoardEditor", "bClear"));
        feldClear.addActionListener(this);
        feldClear.setActionCommand("Clear");
        tb.add(feldClear);

        exit = new JButton(Message.say("BoardEditor", "bBeenden"));
        exit.addActionListener(this);
        exit.setActionCommand("Beenden");
        tb.add(exit);

        add(tb, BorderLayout.NORTH);

        JMenuBar jb = new JMenuBar();
        JMenu jm = new JMenu("File");
        JMenuItem mi;
        mi = new JMenuItem(Message.say("BoardEditor", "bLaden"));
        mi.addActionListener(this);
        mi.setActionCommand("Laden");
        jm.add(mi);
        mi = new JMenuItem(Message.say("BoardEditor", "bSpeichern"));
        mi.addActionListener(this);
        mi.setActionCommand("Speichern");
        jm.add(mi);
        mi = new JMenuItem(Message.say("BoardEditor", "bExport"));
        jb.add(jm);

        advanced = new JCheckBoxMenuItem(Message.say("BoardEditor", "bAdv"));
        JMenu jm2 = new JMenu(Message.say("BoardEditor", "mOptions"));
        jm2.add(advanced);
        jb.add(jm2);

        ActionListener exportListener = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                doExport();
            }
        };

        mi.addActionListener(exportListener);
        export = new JButton(Message.say("BoardEditor", "bExport"));
        export.addActionListener(exportListener);
        export.setActionCommand("Export");
        tb.add(export);

        mi.setActionCommand("Export");
        jm.add(mi);
        jm.addSeparator();
        mi = new JMenuItem(Message.say("BoardEditor", "bBeenden"));
        mi.addActionListener(this);
        mi.setActionCommand("Beenden");
        jm.add(mi);
        editor.setJMenuBar(jb);
        makeChooser();

    }

    public boolean advancedFeaturesEnabled() {
        return advanced.getModel().isSelected();
    }

    public void doExport() {
        chooser.setFileFilter(pngFilter);
        chooser.rescanCurrentDirectory();
        int returnVal = chooser.showSaveDialog(editor);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            String filename = file.getName();
            if (file.getName().equals(""))
                return;
            if (!filename.endsWith(".png")) {
                file = new File(file.getParent(), filename + ".png");
                CAT.debug("File " + file);
            }
            if (file.exists()) {
                Object[] options = { Message.say("Start", "mOK"), Message.say("Start", "mAbbr") };

                String msg = Message.say("BoardEditor", "mDatEx", filename);
                String warn = Message.say("BoardEditor", "mWarnung");
                int r = JOptionPane.showOptionDialog(null, msg, warn, JOptionPane.DEFAULT_OPTION,
                                JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                if (r != 0) {
                    return;
                }
            }

            CAT.debug("Speichere " + file + " " + file.getName() + " (" + file.getName() + ")");
            CAT.debug("File opened");
            try {
                editor.dumpPngImage(file);
            }
            catch (IOException i) {
                System.err.println(Message.say("BoardEditor", "mDateiErr") + file + i);
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().compareTo("Speichern") == 0) {
            CAT.debug("Speichern geclicked, gete String");
            String tmp = editor.board.getComputedString();
            CAT.debug("String bekommen:\n" + tmp);
            editor.setMagicBoardString(tmp);
            CAT.debug("Starte FileDialog");

            // new
            // NameDialog(editor,Message.say("BoardEditor","mKachelSave"),true);
            chooser.setFileFilter(rraFilter);
            chooser.rescanCurrentDirectory();
            int returnVal = chooser.showSaveDialog(editor);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                String filename = file.getName();
                if (file.getName().equals(""))
                    return;
                if (!filename.endsWith(".rra")) {
                    file = new File(file.getParent(), filename + ".rra");
                    CAT.debug("File " + file);
                }
                if (file.exists()) {
                    Object[] options = { Message.say("Start", "mOK"), Message.say("Start", "mAbbr") };

                    String msg = Message.say("BoardEditor", "mDatEx", filename);
                    String warn = Message.say("BoardEditor", "mWarnung");
                    int r = JOptionPane.showOptionDialog(null, msg, warn, JOptionPane.DEFAULT_OPTION,
                                    JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                    if (r != 0) {
                        return;
                    }
                }

                CAT.debug("Speichere " + file + " " + file.getName() + " (" + DEFAULT_SAVE_DIR + "/" + file.getName()
                                + ")");
                CAT.debug("File opened");
                try {
                    FileOutputStream fop = new FileOutputStream(file);
                    CAT.debug("FileOutputStream opened");
                    PrintWriter pw = new PrintWriter(fop);
                    CAT.debug("PrintWriter opened");
                    pw.println(editor.getMagicBoardString());
                    CAT.debug("String in File written");
                    pw.close();
                    CAT.debug("File closed");
                }
                catch (IOException i) {
                    System.err.println(Message.say("BoardEditor", "mDateiErr") + file + i);
                }
            }
        }
        else
            if (e.getActionCommand().compareTo("Beenden") == 0) {
                editor.dispose();

                // System.exit(0);
            }
            else
                if (e.getActionCommand().compareTo("Clear") == 0) {
                    CAT.debug("Clearing field");
                    editor.initSpF();
                    editor.sp.getViewport().setView(editor.boardView);
                }
                else
                    if (e.getActionCommand().compareTo("Laden") == 0) {
                        chooser.rescanCurrentDirectory();
                        int returnVal = chooser.showOpenDialog(editor);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            File file = chooser.getSelectedFile();
                            String name = file.getName();

                            if (name.equals("") || !file.exists() || !file.canRead() || file.isDirectory()) {
                                String fehler = Message.say("Start", "mError");
                                String msg = Message.say("BoardEditor", "eDateiErr");

                                JOptionPane.showMessageDialog(null, msg, fehler, JOptionPane.ERROR_MESSAGE);
                                return;
                            }

                            editor.loadTileFile(file);
                        }
                        // new
                        // LadenDialog(editor,Message.say("BoardEditor","mKachelSave"),true);
                    }
                    else
                        if (e.getActionCommand().compareTo("Flag") == 0) {

                        }
                        else {
                            CAT.error("unhandled ActionCommand " + e.getActionCommand());
                        }
    }

}
