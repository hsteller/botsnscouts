/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2001-2004 scouties.                                    *
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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Category;

import de.botsnscouts.gui.OkComponent;
import de.botsnscouts.util.BNSThread;
import de.botsnscouts.util.KrimsKrams;
import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.ColoredComponent;
import de.botsnscouts.widgets.TJButton;
import de.botsnscouts.widgets.TJCheckBox;
import de.botsnscouts.widgets.TJLabel;
import de.botsnscouts.widgets.TJPanel;
import de.botsnscouts.widgets.TJTextField;

/**
 * The panel that includes the Really-start-game-button. You get this panel after you chose to host a game and have chosen the game options and thus
 * started a server. Now you see who registers for your game.
 */
@SuppressWarnings("serial")
public class StartPanel extends JPanel {

    private static final Category CAT = Category.getInstance(StartPanel.class);

    private Paint paint;

    private Start parent;

    private JLabel angem;

    private PlayersPanel playersComponent;

    // private ServerObserver listener;

    private JSlider intel;

    private JTextField name;

    private JComboBox<String> color; // actually a RoboBox

    public StartPanel(Start par) {
        parent = par;
        paint = parent.paint;
        Font font = new Font("Sans", Font.BOLD, 24);

        BorderLayout lay = new BorderLayout();

        setLayout(lay);
        setBorder(new EmptyBorder(50, 50, 50, 50));
        setOpaque(false);

        angem = new TJLabel();
        playersComponent = new PlayersPanel(parent);
        // recreateServerObeserver();

        angem.setFont(font);
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        // p.add( angem, BorderLayout.NORTH );
        p.add(playersComponent, BorderLayout.CENTER);
        add(BorderLayout.WEST, p);
        add(BorderLayout.SOUTH, getOkComponent());
        JPanel panel = new TJPanel();

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.anchor = GridBagConstraints.NORTH;
        gc.fill = GridBagConstraints.BOTH;
        gc.gridx = 0;
        gc.gridy = GridBagConstraints.RELATIVE;
        gc.insets = new Insets(30, 30, 30, 30);

        panel.add(getAutoBotComponent(), gc);
        panel.add(getLocalComponent(), gc);
        add(BorderLayout.EAST, panel);
    }

    /*
     * protected void recreateServerObeserver() { if (listener != null) { listener.closeSock(); } listener = new ServerObserver(playersComponent);
     * listener.start(); }
     */
    /*
     * ServerObserver getServerObserver() { return listener; }
     */
    private JComponent getLocalComponent() {
        JComponent panel = new ColoredComponent();

        panel.setOpaque(false);

        Font font = new Font("Sans", Font.BOLD, 24);
        panel.setBorder(new EmptyBorder(50, 50, 50, 50));

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        JLabel label = new TJLabel(Message.say("Start", "mLokaleMS"));
        label.setFont(font);
        gc.gridwidth = 3;
        gc.insets = new Insets(0, 0, 20, 0);
        gc.ipadx = 5;
        gc.ipady = 5;
        panel.add(label, gc);

        // Name-Label
        label = new TJLabel(Message.say("Start", "mName"));
        gc.gridx = 0;
        gc.gridy = 1;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.EAST;
        panel.add(label, gc);

        // Name-Textfield
        name = new TJTextField(KrimsKrams.randomName());
        name.setOpaque(false);
        gc.gridwidth = 2;
        gc.gridx = 1;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.BOTH;
        panel.add(name, gc);

        // Farbe-Label
        label = new TJLabel(Message.say("Start", "mFarbe"));
        gc.gridx = 0;
        gc.gridwidth = 1;
        gc.gridy = 2;
        gc.anchor = GridBagConstraints.EAST;
        panel.add(label, gc);

        gc.gridx = 1;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        color = new RoboBox(true);

        gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.CENTER;
        panel.add(color, gc);

        // Go Button

        JButton ok = new TJButton(Message.say("Start", "mGoButton"));
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    public void run() {
                        CAT.debug("Button pressed. Going to register " + name.getText());
                        BNSThread player;
                        try {
                            player = Facade.participateInAGameNoSplash(name.getText(), color.getSelectedIndex());
                        }
                        catch (JoiningGameFailedException je) {
                            Exception cause = je.getPossibleReason();
                            String msg1 = Message.say("Start", "registerAtServerError");
                            String msg2 = msg1;
                            CAT.error(je.getMessage(), je);
                            if (cause != null) {
                                msg2 = cause.getMessage();
                                CAT.error(msg2, cause);
                            }
                            JOptionPane.showMessageDialog(parent, msg2, msg1, JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        parent.addKS(player);
                        // Generate a new name for the (potential) next local player
                        name.setText(KrimsKrams.randomName());
                    }
                }.start();
            }
        });

        gc.gridx = 1;
        gc.gridwidth = 1;
        gc.gridy++;
        gc.anchor = GridBagConstraints.EAST;
        gc.fill = GridBagConstraints.CENTER;
        gc.insets = new Insets(0, 0, 0, 0);
        panel.add(ok, gc);
        panel.setBorder(new CompoundBorder(new EtchedBorder(8), new EmptyBorder(10, 10, 10, 10)));

        return panel;
    }

    private JComponent getAutoBotComponent() {
        JComponent panel = new ColoredComponent();
        panel.setOpaque(false);
        Font font = new Font("Sans", Font.BOLD, 24);

        panel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        Insets noInsets = new Insets(0, 0, 0, 0);
        Insets insets = new Insets(0, 0, 20, 0);

        // headline for the whole box
        JLabel label = new TJLabel(Message.say("Start", "mStartKS"), SwingConstants.CENTER);
        gc.weightx = 1.0;
        gc.gridx = 0;
        gc.gridwidth = 4;
        gc.gridy = 0;
        gc.insets = insets;
        gc.ipadx = 5;
        gc.ipady = 5;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.HORIZONTAL;
        label.setFont(font);
        panel.add(label, gc);

        JLabel nameLabel = new TJLabel(Message.say("Start", "mKSName"), SwingConstants.LEFT);
        gc.weightx = 0.0;
        gc.gridy++; // 3
        gc.gridx = 0;
        gc.gridwidth = 1;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new Insets(0, 0, 10, 5);
        panel.add(nameLabel, gc);

        final JTextField botNameField = new TJTextField(KrimsKrams.randomName());
        botNameField.setColumns(20);
        gc.gridx = 1;
        gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.WEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(botNameField, gc);

        // headline above the intelligence slider
        JLabel ks = new TJLabel(Message.say("Start", "mIntel"), SwingConstants.CENTER);
        gc.gridy++;
        gc.gridx = 1;
        gc.gridwidth = 2;
        gc.weightx = 1.0;
        gc.insets = noInsets;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(ks, gc);

        // left label on the slider (intelligent)
        JLabel lb = new TJLabel(Message.say("Start", "mSchlau"), SwingConstants.LEFT);
        gc.gridy++;
        gc.gridx = 0;
        gc.weightx = 0.0;
        gc.insets = insets;
        gc.gridwidth = 1;
        gc.gridheight = 1;
        gc.fill = GridBagConstraints.NONE;
        gc.anchor = GridBagConstraints.WEST;
        panel.add(lb, gc);

        // Slider
        intel = new JSlider();
        intel.setOpaque(false);
        intel.getModel().setMinimum(0);
        intel.getModel().setMaximum(130);
        gc.gridx = 1;
        gc.gridwidth = 2;
        gc.gridheight = 1;
        gc.weightx = 2.0;
        gc.anchor = GridBagConstraints.CENTER;
        gc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(intel, gc);

        // right label on the slider (dumb)
        gc.weightx = 0.0;
        gc.gridx = 3;
        gc.anchor = GridBagConstraints.EAST;
        gc.fill = GridBagConstraints.NONE;
        lb = new TJLabel(Message.say("Start", "mDumm"), SwingConstants.RIGHT);
        panel.add(lb, gc);

        // labeled "likes belts"-checkbox
        final JCheckBox beltAware = new TJCheckBox(Message.say("Start", "beltAware"), false);
        beltAware.setOpaque(false);
        beltAware.setToolTipText(Message.say("Start", "beltTooltip"));
        gc.gridy++;
        gc.gridx = 0;
        gc.gridwidth = 2;
        gc.weightx = 0.0;
        gc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(beltAware, gc);

        final JButton startAB = new TJButton(Message.say("Start", "mKSStarten"));
        startAB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Thread() {
                    public void run() {
                        CAT.debug("intelligence is now" + intel.getValue());
                        String localName = botNameField.getText();
                        if (localName == null || localName.length() == 0) {
                            localName = KrimsKrams.randomName();
                        }
                        parent.addKS(Facade.startAutoBot(intel.getValue(), beltAware.getModel().isSelected(), localName));
                        CAT.debug("Bot started, setting new name in textfield; new name will be: " + localName);
                        botNameField.setText(KrimsKrams.randomName()); // DEADLOCK here without new Thread..wtf?
                        CAT.debug("new name set");
                    }
                }.start();

            }
        });

        gc.gridx = 2;
        gc.gridwidth = 2;
        gc.gridheight = 2;
        gc.weightx = 0.0;
        gc.anchor = GridBagConstraints.EAST;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = noInsets;
        panel.add(startAB, gc);
        panel.setBorder(new CompoundBorder(new EtchedBorder(8), new EmptyBorder(10, 10, 10, 10)));
        return panel;
    }

    private JComponent getOkComponent() {

        OkComponent okComponent = new OkComponent(Message.say("Start", "mLos"), Message.say("Start", "mAbbrechen"));

        okComponent.addOkListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CAT.debug("Start-button pressed.");
                if (playersComponent.names.size() != 8) {
                    CAT.debug("Going to kick the server to get really going...");
                    parent.getFacade().gameStarts();
                    parent.setVisible(false);
                    // parent.hide();
                    // parent.beenden(); // will be done in playerspanel
                }
            }
        });
        okComponent.addBackListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.reset();

                parent.showGameFieldPanel();
                // parent.startPanel = null;

                // System.exit(0);
            }
        });

        return okComponent;
    }

    public PlayersPanel getPlayersPanel() {
        return playersComponent;
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Dimension d = getSize();
        g2d.setPaint(paint);
        g2d.fillRect(0, 0, d.width, d.height);
        paintChildren(g);
    }

}
