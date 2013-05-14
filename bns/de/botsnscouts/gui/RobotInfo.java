/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2005 scouties.                                *
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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import de.botsnscouts.util.Bot;
import de.botsnscouts.util.CropperField2;
import de.botsnscouts.util.CursorMan;
import de.botsnscouts.util.ImageMan;
import de.botsnscouts.util.Message;

@SuppressWarnings("serial")
public class RobotInfo extends JComponent implements RobotStatus, ActionListener {

    static org.apache.log4j.Category CAT = org.apache.log4j.Category.getInstance(RobotInfo.class);

    static final Image[] roboImages = ImageMan.getImages(ImageMan.ROBOS);

    static final Image[] cursors = CursorMan.getImages(CursorMan.STATUSROBOTS);

    private static Image[] stuff = null;

    static final Color COLOR_LED_OFF = new Color(0, 0, 50);

    static final Color COLOR_LED_ON = Color.green;

    static final Color COLOR_LED_RED = Color.red;

    static final int MINI_IMAGE_COUNT = 4;

    private DamageBar damageBar1;

    private FlagBar flagBar2 = new FlagBar();

    private StatusRobot statusRobot1; // must not be initialized here because it
                                      // needs the bot images that might not be
                                      // loaded yet

    private JButton diskButton1 = new JButton();

    private int ranking = 0;

    private Bot robot;

    private Color ledColor = COLOR_LED_OFF;

    public Color getLedColor() {
        return ledColor;
    }

    public void setLedColor(Color ledColor) {
        this.ledColor = ledColor;
    }

    static synchronized Image getImage(int nr) {
        if (stuff == null) {
            stuff = new Image[MINI_IMAGE_COUNT];
            ImageMan.loadImages();
            Image image = ImageMan.getImage(ImageMan.STATUS_STUFF);
            CropperField2 cropper = new CropperField2(MINI_IMAGE_COUNT, 1, 16, image);
            CAT.debug("start cropping");
            cropper.multiCrop(image, MINI_IMAGE_COUNT, MINI_IMAGE_COUNT, stuff, 0);
            CAT.debug("waiting for images");
            ImageMan.finishLoading();
            CAT.debug("waiting over");
        }
        return stuff[nr];
    }

    static Image getFlag() {
        return getImage(0);
    }

    static Image getGrayFlag() {
        return getImage(1);
    }

    static Image getDisk() {
        return getImage(2);
    }

    static Image getOffSwitch() {
        return getImage(3);
    }

    RobotInfo(Bot r, int flagCount, ScalableRegisterRow regsOfR_forStatusTooltip) {
        this(r, flagCount, r.getBotVis(), regsOfR_forStatusTooltip);
    }

    RobotInfo(Bot r, int flagCount, int viz, ScalableRegisterRow regsOfR_forStatusTooltip) {
        this.robot = r;
        Icon big = new ImageIcon(roboImages[viz * 4 + robot.getFacing()]);
        Icon small = new ImageIcon(cursors[viz]);
        damageBar1 = new DamageBar();
        flagBar2 = new FlagBar(flagCount);
        statusRobot1 = new StatusRobot(big, small, r);
        if (regsOfR_forStatusTooltip != null) {
            // statusRobot1.setToolTip(new
            // RegisterToolTip(regsOfR_forStatusTooltip), true);
            statusRobot1.setRoboRegisters(regsOfR_forStatusTooltip);
        }
        diskButton1 = new JButton();
        diskButton1.setIcon(new ImageIcon(getDisk()));
        flagBar2.addActionListener(this);
        statusRobot1.addActionListener(this);
        diskButton1.addActionListener(this);
        try {
            initLayout();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RobotInfo() {
        try {
            statusRobot1 = new StatusRobot();
            initLayout();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initLayout() throws Exception {
        this.setBackground(Color.black);
        this.setSize(getPreferredSize());
        this.setLayout(null);
        this.setOpaque(true);
        damageBar1.setBounds(new Rectangle(9, 7, 5, 54));
        flagBar2.setBounds(new Rectangle(15, 7, 70, 16));

        statusRobot1.setBounds(new Rectangle(18, 20, 68, 44));
        diskButton1.setIcon(new ImageIcon(RobotInfo.getDisk()));
        diskButton1.setBounds(new Rectangle(0, 23, 16, 16));
        diskButton1.setBorder(null);
        diskButton1.setBorderPainted(false);
        this.add(damageBar1, null);
        this.add(statusRobot1, null);
        statusRobot1.setLayout(null);
        statusRobot1.add(diskButton1, null);
        this.add(flagBar2, null);

        diskButton1.setToolTipText(Message.say("RobotInfo", "archive", robot.getName()));
        flagBar2.setToolTipText(Message.say("RobotInfo", "nextFlag", robot.getName()));
        // statusRobot got an advanced "tooltip" => no "normal" tooltip anymore
        // statusRobot1.setToolTipText(Message.say("RobotInfo", "botPos",
        // robot.getName()));
        damageBar1.setToolTipText(Message.say("RobotInfo", "damage", robot.getName(), "" + robot.getDamage()));

        // flagBar2, statusRobot1,damageBar1 should not be focusable either;
        // the constructors of the above classes should take care of that and
        // set the focusable
        // property to false
        diskButton1.setFocusable(false);
    }

    static Color lineGreen = new Color(0, 80, 0);

    static Color background = Color.black.brighter().brighter();

    public void paintText(Graphics2D g) {
        g.setFont(font);
        g.setColor(Color.black);
        g.drawString(robot.getName(), 15, 31);
        g.setColor(textColor);
        g.drawString(robot.getName(), 14, 30);
    }

    void paintGrid(Graphics2D g) {
        g.setColor(background);

        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(lineGreen);
        for (int x = 5; x < getWidth(); x += 5)
            g.drawLine(x, 0, x, getHeight());
        for (int y = 5; y < getHeight(); y += 5)
            g.drawLine(0, y, getWidth(), y);
    }

    void paintRank(Graphics2D g) {
        String r = "" + ranking;
        g.setFont(rankfont);
        int width = g.getFontMetrics().stringWidth(r);
        g.setColor(shadeGray);
        g.drawString(r, getWidth() - width, getHeight() - 11);
        g.setColor(Color.yellow);
        g.drawString(r, getWidth() - width - 2, getHeight() - 13);
    }

    void paintShade(Graphics2D g, Color shadeColor) {
        g.setPaint(shadeColor);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    void paintFrame(Graphics2D g) {
        frame.paintIcon(this, g, -2, -2);
        g.setColor(ledColor);
        g.fillRect(1, 1, 5, 4);
    }

    public void paint(Graphics _g) {
        Graphics2D g = (Graphics2D) _g;
        paintGrid(g);

        super.paint(g);
        if (ranking > 0) {
            paintRank(g);
        }

        if (isDead())
            paintShade(g, darkShade);

        paintText(g);

        if (isDead())
            paintShade(g, darkShade);
        else
            if (ranking == 0 && robot != null && !robot.isActivated()) {
                paintShade(g, shadeGray);
            }
        paintFrame(g);
    }

    static Color someGray = new Color(100, 100, 100, 128);

    static Color shadeGray = new Color(200, 200, 200, 128);

    static Color darkShade = new Color(50, 50, 50, 215);

    static Font font = new Font("Serif", Font.ITALIC + Font.BOLD, 12);

    static Font rankfont = new Font("Serif", Font.ITALIC + Font.BOLD, 64);

    static Color textColor = new Color(200, 200, 200);

    static ImageIcon frame = new ImageIcon(ImageMan.getImage(ImageMan.STATUS_FRAME));

    private transient Vector<RobotInfoListener> robotInfoListeners;

    private boolean dead;

    public Dimension getMinimumSize() {
        return new Dimension(90, 70);
    }

    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    public Dimension getMaximumSize() {
        return getMinimumSize();
    }

    public Bot getRobot() {
        return robot;
    }

    public void setLedOn(boolean on) {
        if (on) {
            setLedColor(COLOR_LED_ON);
        }
        else {
            setLedColor(COLOR_LED_OFF);
        }
    }

    public void updateRobot(Bot r) {

        robot = r;
        damageBar1.setDamageValue(r.getDamage());
        damageBar1.setToolTipText(Message.say("RobotInfo", "damage", r.getName(), r.getDamage() + ""));
        flagBar2.setReachedFlag(r.getNextFlag() - 1);
        statusRobot1.setLifesLeft(r.getLivesLeft() - 1);
        statusRobot1.updateRobot(robot/*
                                       * , new
                                       * ImageIcon(roboImages[viz*4+robot.getFacing
                                       * ()])
                                       */);
        setDead(r.getLivesLeft() <= 0);
        repaint();
    }

    public void setWinnerNumber(int ranking) {
        this.ranking = ranking;
        repaint();
    }

    public static void main(String[] args) {
        JFrame myFrame = new JFrame("damage");
        myFrame.setSize(500, 100);
        myFrame.getContentPane().setLayout(new BorderLayout());
        myFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Box b = Box.createHorizontalBox();
        for (int i = 0; i < 6; i++) {
            Bot robot = Bot.getNewInstance("TestRob " + i);
            robot.setActivated(i % 2 == 0);
            final RobotInfo db = new RobotInfo(robot, 7, i, null);
            db.setWinnerNumber(i);
            db.setBorder(BorderFactory.createLineBorder(Color.black));
            db.setSize(db.getPreferredSize());
            switch (i) {
                case 0:
                    db.setLedColor(COLOR_LED_ON);
                    break;

                case 1:
                    db.setLedColor(COLOR_LED_RED);
                    break;

            }
            JPanel p = new JPanel();
            p.add(db);
            b.add(p);
            db.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent me) {
                    db.repaint();
                }
            });
        }
        myFrame.getContentPane().add(b, BorderLayout.CENTER);
        myFrame.setVisible(true);
    }

    public synchronized void removeRobotInfoListener(RobotInfoListener l) {
        if (robotInfoListeners != null && robotInfoListeners.contains(l)) {
            @SuppressWarnings("unchecked")
            Vector<RobotInfoListener> v = (Vector<RobotInfoListener>) robotInfoListeners.clone();
            v.removeElement(l);
            robotInfoListeners = v;
        }
    }

    public synchronized void addRobotInfoListener(RobotInfoListener l) {

        Vector<RobotInfoListener> v = null;
        if (robotInfoListeners == null) {
            v = new Vector<RobotInfoListener>(2);
        }
        else {
            @SuppressWarnings("unchecked")
            Vector<RobotInfoListener> tmp = (Vector<RobotInfoListener>) robotInfoListeners.clone();
            v = tmp;
        }
        if (!v.contains(l)) {
            v.addElement(l);
            robotInfoListeners = v;
        }
    }

    protected void fireFlagClicked(RobotInfoEvent e) {
        if (robotInfoListeners != null) {
            Vector<RobotInfoListener> listeners = robotInfoListeners;
            int count = listeners.size();
            for (int i = 0; i < count; i++) {
                listeners.elementAt(i).flagClicked(e);
            }
        }
    }

    protected void fireRobotClicked(RobotInfoEvent e) {
        if (robotInfoListeners != null) {
            Vector<RobotInfoListener> listeners = robotInfoListeners;
            int count = listeners.size();
            for (int i = 0; i < count; i++) {
                listeners.elementAt(i).robotClicked(e);
            }
        }
    }

    protected void fireDiskClicked(RobotInfoEvent e) {
        if (robotInfoListeners != null) {
            Vector<RobotInfoListener> listeners = robotInfoListeners;
            int count = listeners.size();
            for (int i = 0; i < count; i++) {
                listeners.elementAt(i).diskClicked(e);
            }
        }
    }

    public void actionPerformed(ActionEvent parm1) {
        /** todo: Implement this java.awt.event.MouseListener method */
        Object src = parm1.getSource();
        if (src == diskButton1)
            fireDiskClicked(new RobotInfoEvent(this));
        else
            if (src == flagBar2)
                fireFlagClicked(new RobotInfoEvent(this));
            else
                if (src == statusRobot1)
                    fireRobotClicked(new RobotInfoEvent(this));
                else {
                    CAT.error("unkown source");
                }
    }

    public void setDead(boolean newDead) {
        dead = newDead;
        if (dead) {
            CAT.debug("NOTIFY OF DEATH: " + robot.getName());
            statusRobot1.notifyOfRobRemoval();

        }

    }

    public boolean isDead() {
        return dead;
    }

}
