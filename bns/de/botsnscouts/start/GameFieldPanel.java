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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Properties;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Category;

import de.botsnscouts.gui.HumanPlayer;
import de.botsnscouts.util.BNSThread;
import de.botsnscouts.util.Conf;
import de.botsnscouts.util.Global;
import de.botsnscouts.util.InvalidInputException;
import de.botsnscouts.util.Message;
import de.botsnscouts.util.Task;
import de.botsnscouts.widgets.ColoredComponent;
import de.botsnscouts.widgets.ColoredPanel;
import de.botsnscouts.widgets.TJButton;
import de.botsnscouts.widgets.TJCheckBox;
import de.botsnscouts.widgets.TJLabel;
import de.botsnscouts.widgets.TJNumberField;
import de.botsnscouts.widgets.TJPanel;
import de.botsnscouts.widgets.TJTextField;

/**
 * You see this panel when you want to host a game.
 * You choose the create the board and choose other game options
 * and then start the server and others can register.
 */
public class GameFieldPanel extends JPanel {
    private Paint paint;
    private Start parent;

    private FieldGrid boardGrid;
    JPanel pnl;
    
    
   
    private JComponent okPanel;
    private JButton okBut;
    private JButton backBut;

    private JComponent editPanel;
    private JComboBox spielfelder;
    private JButton save;
    private JButton edit;
    private JTextField nam;
    private JComboBox colors;
    private JCheckBox participate;
    private GameFieldLoader loader = new GameFieldLoader();
    private JFileChooser chooser;
    private JCheckBox allowWisenheimer;
    private JCheckBox allowScout;
    private TJNumberField timeoutInput;

    private GameOptions gameOptions;
    
   public static final String PROPNAME_LAST_SPF = "nameOfLastUsedBoardSPF";
   public static final String PROPNAME_LAST_COLOR_INDEX = "indexOfLastRobColor";
  

    /**
     * Announce the game at a meta server?
     */
    private AnnounceGame announceGame = new AnnounceGame();

    private final static Category CAT = Category.getInstance(GameFieldPanel.class);

    public GameFieldPanel(Start par) {
        parent = par;
        parent.setTitle(Message.say("Start", "mSpielStarten"));
        paint = parent.paint;

        editPanel = getEditPanel();
        okPanel = getOkPanel();
        boardGrid = new FieldGrid(par);
        BorderLayout lay = new BorderLayout();

        setLayout(lay);
        requestFocus();

        JScrollPane leftPane = new JScrollPane();

        pnl = new TJPanel();
        pnl.setLayout(new FlowLayout());
        pnl.setBorder(new EmptyBorder(50, 50, 50, 50));
        pnl.add(boardGrid);

        leftPane.setOpaque(false);
        leftPane.getViewport().setOpaque(false);
        leftPane.getViewport().setView(pnl);
        leftPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        add(BorderLayout.SOUTH, okPanel);
        add(BorderLayout.CENTER, leftPane);
        JScrollPane rightPane= new JScrollPane();
        rightPane.getViewport().setView(editPanel);
        rightPane.setOpaque(false);
       
        rightPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        rightPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
       
        add(BorderLayout.EAST, rightPane/*editPanel*/);
        boardGrid.rasterChanged();

        gameOptions = parent.facade.getGameOptions();
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Dimension d = getSize();
        g2d.setPaint(paint);
        g2d.fillRect(0, 0, d.width, d.height);
        paintChildren(g);
    }

    private JComponent getOkPanel() {
        JComponent panel = new ColoredComponent();

        GridLayout lay = new GridLayout(1, 2);
        lay.setHgap(50);
        lay.setVgap(50);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setLayout(lay);
        panel.setOpaque(false);

        okBut = new TJButton(Message.say("Start", "mSpielStarten"));
        backBut = new TJButton(Message.say("Start", "mZurueckButton"));

        okBut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                okClicked();
            }
        });
        backBut.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.showMainMenu();
            }
        });

        panel.add(backBut);
        panel.add(okBut);
        return panel;
    }

    private JComponent getEditPanel() {
        JComponent panel = new ColoredPanel();

        JComponent inner = new JPanel();
        GridBagLayout lay = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(0, 0, 20, 0);
        gc.gridx = 0;
        gc.gridy = GridBagConstraints.RELATIVE;
        panel.setBorder(new EmptyBorder(20, 10, 10, 10));
        panel.setOpaque(false);

        inner.setLayout(lay);
        inner.setOpaque(false);

        Font font = new Font("Sans", Font.BOLD, 12);

        JLabel spielfeld = new TJLabel(Message.say("Start", "mSpielfeld"));
        String[] spielfeldAr = loader.getSpielfelder();
        String defSpf = null;
        String lastSpfName = Conf.getProperty(PROPNAME_LAST_SPF);
        if (lastSpfName !=  null) {   
            if (lastSpfName.toLowerCase().endsWith(".spf")){
                lastSpfName = lastSpfName.substring(lastSpfName.length()-4);
            }
            for (int i = 0; i < spielfeldAr.length; i++) {
                if (spielfeldAr[i].equals(lastSpfName)) {
                    defSpf = spielfeldAr[i];
                    break;
                }
            }
        }
       
        spielfelder = new JComboBox(spielfeldAr);
        spielfeld.setVisible(false);
        spielfelder.setVisible(true);
        spielfelder.setOpaque(false);

        save = new TJButton(Message.say("Start", "bSave"));
        save.setVisible(true);
        save.setEnabled(true);
        save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Properties spfProp = parent.facade.getBoardAsProperties();
                makeChooser();
                chooser.rescanCurrentDirectory();
                int returnVal = chooser.showSaveDialog(parent);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    String filename = file.getName();
                    if (file.getName().equals("")) {
                        return;
                    }
                    if (!filename.toLowerCase().endsWith(".spf")) {
                        file = new File(file.getParent(), filename + ".spf");
                    }
                    //falls datei existiert
                    if (file.exists()) {
                        Object[] options = {Message.say("Start", "mOK"), Message.say("Start", "mAbbr")};
                        String msg = Message.say("BoardEditor", "mDatEx", filename);
                        String warn = Message.say("BoardEditor", "mWarnung");
                        int r = JOptionPane.showOptionDialog(null, msg, warn, JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                        if (r != 0) {
                            return;
                        }
                    }
                    loader.saveSpielfeld(spfProp, file);
                    spielfelder.setModel(new DefaultComboBoxModel(loader.getSpielfelder()));
                }
            }
        });

        edit = new TJButton(Message.say("Start", "mBearbeiten"));
        edit.setVisible(true);
        edit.setEnabled(true);
        edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.showFieldEditor(boardGrid);
                edit.getModel().setRollover(false);
            }
        });

        nam = new TJTextField(Conf.getDefaultRobName());
        colors = new RoboBox(true);
        int lastIndex = Conf.getIntProperty(PROPNAME_LAST_COLOR_INDEX,0);
        colors.setSelectedIndex(lastIndex);
        participate = new TJCheckBox(Message.say("Start", "mTeilnehmenBox"), true);

        allowScout = new TJCheckBox(Message.say("Start", "mAllowScout"), true);
        allowWisenheimer = new TJCheckBox(Message.say("Start", "mAllowWisenheimer"), true);
        
        
        timeoutInput = new TJNumberField(0, Integer.MAX_VALUE, 10);
        int defaultTO = GameOptions.DTO;
        timeoutInput.setValue(defaultTO);

        final JTextField metaServer = new TJTextField(announceGame.getServerString());
        metaServer.setEnabled(announceGame.willBeAnnounced());
        final JCheckBox announce = new TJCheckBox(Message.say("Start", "mAnnounceMetaServer"),
                announceGame.willBeAnnounced());
        announce.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                metaServer.setEnabled(announce.isSelected());
                metaServer.setEditable(announce.isSelected());
                announceGame.setAnnounce(announce.isSelected());
            }
        });
        metaServer.addFocusListener(
                new FocusAdapter() {
                    public void focusLost(FocusEvent event) {
                        try {
                            announceGame.parse(metaServer.getText());
                        } catch (InvalidInputException ex) {
                            CAT.info(ex.getMessage());
                            Global.beeep();
                            metaServer.setText(announceGame.getServerString());
                        }
                    }
                }
        );


        spielfelder.setFont(font);
        save.setFont(font);
        edit.setFont(font);
        spielfelder.setEnabled(true);

        participate.addChangeListener(new ChangeListener() {
            //changeListener Methode
            //Invoked when the target of the listener has changed its state.
            public void stateChanged(ChangeEvent e) {
                if (!participate.isSelected()) {
                    nam.setEnabled(false);
                    colors.setEnabled(false);
                } else {
                    nam.setEnabled(true);
                    colors.setEnabled(true);
                }
            }
        });


        inner.add(spielfeld, gc);
        inner.add(spielfelder, gc);
        inner.add(save, gc);
        inner.add(edit, gc);
        inner.add(participate, gc);
        inner.add(new TJLabel(Message.say("Start", "mName")), gc);
        inner.add(nam, gc);
        inner.add(new TJLabel(Message.say("Start", "mFarbe")), gc);
        inner.add(colors, gc);


        inner.add(allowWisenheimer, gc);
        inner.add(allowScout, gc);
        inner.add(new TJLabel(Message.say("Start", "mHandInTimeOut")), gc);
        inner.add(timeoutInput, gc);
        inner.add(announce, gc);   
        inner.add(metaServer, gc);

        gc.fill = GridBagConstraints.HORIZONTAL;

        panel.add(inner);
        //Add new game options below this one.

        //Always load th efirst board
        Properties spfProp;
        if (defSpf == null) {
            spfProp = loader.getProperties(spielfeldAr[0]);
        } else {
            spfProp = loader.getProperties(defSpf);
            spielfelder.setSelectedItem(defSpf);
        }
        parent.facade.loadBoardFromProperties(spfProp);
        spielfelder.addItemListener(new ItemListener() {
            //Invoked when an item has been selected or deselected.
            public void itemStateChanged(ItemEvent e) {
                String spfConf = (String) spielfelder.getSelectedItem();
                Properties prop = loader.getProperties(spfConf);
                parent.facade.loadBoardFromProperties(prop);
                boardGrid.rasterChanged();
            }
        });
        
       
        return panel;
    }

    private void makeChooser() {
        // Initialize only if it doesn't exist yet
        if (chooser != null) {
            return;
        }

        chooser = new JFileChooser("tiles");
        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                String name = file.getName();
                return file.isDirectory() || name.toLowerCase().endsWith(".spf");
            }

            public String getDescription() {
                return "spf";
            }
        };
        chooser.setFileFilter(filter);
    }
    
    private int readTimeOut(){
        int timeout = timeoutInput.getValue();
        if (timeout < HumanPlayer.BUFFER_SECONDS_BEFORE_TIMEOUT) {
            timeout = HumanPlayer.BUFFER_SECONDS_BEFORE_TIMEOUT+1;            
            timeoutInput.setValue(timeout);
            JOptionPane.showMessageDialog(this.parent, Message.say("StartSpieler","mIllegalTimeout", timeout));                            						
        }
        return timeout;
        
        
    }

    private void okClicked() {
        String spfName = (String) spielfelder.getSelectedItem();
        if (CAT.isDebugEnabled()) {
            CAT.debug("SPF selected: "+spfName);
        }
        Conf.setProperty(PROPNAME_LAST_SPF, spfName);
        Conf.setProperty(PROPNAME_LAST_COLOR_INDEX, colors.getSelectedIndex()+"");
        Conf.saveProperties();
        gameOptions.setAllowWisenheimer(allowWisenheimer.isSelected());
        gameOptions.setAllowScout(allowScout.isSelected());
        gameOptions.setInvitor(nam.getText());        
        gameOptions.setHandInTimeout(readTimeOut()*1000);
        try {
            parent.facade.updateGameOptions();
            /* Handig over a postServerStartTask is still a bit weird, but
               it is much more sane and faster than before...
             */
            parent.showNewStartPanel(new Task() {
                public void doIt() {
                    if (participate.getSelectedObjects() != null) {                       
                        BNSThread smth = Facade.participateInAGameNoSplash(nam.getText(), colors.getSelectedIndex());
                        parent.addKS(smth);
                        Global.debug(this, "menschlichen spieler gestartet");
                    } else {//starte einen AusgabeFrame
                        parent.addKS(Facade.watchAGameNoSplash());
                    }
                    // Announce game, if we shall do this.
                    try {
                        announceGame.announceGame(gameOptions);
                    } catch (UnableToAnnounceGameException e) {
                       CAT.error(e.getMessage(), e); //TODO: give info
                    } catch (YouAreNotReachable youAreNotReachable) {
                        CAT.error(youAreNotReachable.getMessage(), youAreNotReachable); //TODO: give info
                    }
                }
            });
        } catch (OneFlagException ex) {
            JOptionPane.showMessageDialog(this, Message.say("Start", "mZweiFlaggen"), Message.say("Start", "mError"), JOptionPane.ERROR_MESSAGE);
        } catch (NonContiguousMapException exc) {
            JOptionPane.showMessageDialog(this, Message.say("Start", "mNichtZus"), Message.say("Start", "mError"), JOptionPane.ERROR_MESSAGE);
        }
    }//okclicked

    void unrollOverButs() {
        okBut.getModel().setRollover(false);
        backBut.getModel().setRollover(false);
        save.getModel().setRollover(false);
        edit.getModel().setRollover(false);
    }

}