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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JComponent;

import org.apache.log4j.Category;

import com.keypoint.PngEncoder;

import de.botsnscouts.autobot.AdvDistanceCalculator;
import de.botsnscouts.autobot.DistanceCalculator;
import de.botsnscouts.board.Board;
import de.botsnscouts.board.FlagException;
import de.botsnscouts.board.Floor;
import de.botsnscouts.board.FloorConstants;
import de.botsnscouts.board.LaserDef;
import de.botsnscouts.board.SimBoard;
import de.botsnscouts.board.Wall;
import de.botsnscouts.comm.OtherConstants;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.BotVis;
import de.botsnscouts.util.Directions;
import de.botsnscouts.util.FormatException;
import de.botsnscouts.util.ImageMan;
import de.botsnscouts.util.Location;
import de.botsnscouts.util.SoundMan;

/**
 * Board-Ausgabe-Canvas ist das Objekt, das der Ausgabe und dem menschlichen
 * Spieler das Board grafisch darstellt und verwaltet
 * 
 * @version $Id$
 * 
 *          changes: enno v1.23 1. beim painten wird nur noch der teil ins
 *          sichtbare kopiert, der auch in gepaintet werden muss (hat aber nicht
 *          viel gebracht) 2. Felder, die nicht wirklich neu gezeichnet werden
 *          m\uFFFDssen, werden nicht mehr betrachtet (bringt was!) 3. Code
 *          insgesamt lesbarer strukturiert (noch nicht ganz beendet) 4.
 *          makePixelArray in eigene Methode ausgelagert 5. lokale Variablen
 *          eingef\uFFFDhrt ...
 * 
 *          6. Verwaltung und Speicherung der Bilder ausgelagert in Klasse
 *          ImageMan (kann so dann auch vom KachelEditor verwendet werden)
 *          bilder werden dann nur einmal pro JVM geladen, und zwar bei
 *          Programm- start (StartSpieler) im Hintergrund. Der erste
 *          Spielfeldaufbau ist damit viel schneller, ebenso die Board-Vorschau
 */
@SuppressWarnings("serial")
public class BoardView extends JComponent {

    private final static Category CAT = Category.getInstance(BoardView.class);

    private final Object rescaleLock = new Object();

    private final Object offScreenImgNotNullLock = new Object();

    /**
     * Switch for the method that animations use to clear the area where they
     * animate the bots: if true, we will not only have an offScreenImage for
     * doublebuffering but _also_ another BufferedImage ("staticBackground") of
     * the board that will contain the background without any dynamic components
     * (bots, scout,highlight) drawn on it. Without increasing the JVM heapsize
     * on startup, using bigger boards/more autobots while useStaticBg=true will
     * create an OutOfMemoryError.
     * 
     * 
     * If false, this second BufferedImage will be null and another way to paint
     * the background will be used.
     */
    private final boolean useStaticBg = false;

    // inner classes
    public static interface ClickListener {
        void feldClicked(int x, int y, int modifiers);
    }

    /** Constant for direction/facing north */
    protected static final int NORTH = Directions.NORTH;

    /** Constant for direction/facing east */
    protected static final int EAST = Directions.EAST;

    /** Constant for direction/facing south */
    protected static final int SOUTH = Directions.SOUTH;

    /** Constant for direction/facing west */
    protected static final int WEST = Directions.WEST;

    /** The size (length and width) of one little floor field in pixels */
    protected static final int FELDSIZE = 64;

    /** Number of single steps a laser animation is drawn. */
    private static final int FULL_LENGTH_INT = 30;

    /** Number of single steps a laser animation is drawn. */
    private static final double FULL_LENGTH_DOUBLE = 30.0;

    /** Color for boardlasers */
    private static final Color fstLaserColor = Color.red.brighter();

    /** The color used for the background of active lasers. */
    private final static Color sndLaserColor = new Color(255, 255, 155);

    /** For looking up the color of a robot; contains name->color mapping. */
    private java.util.HashMap<String, Color> nameToColorHash;

    private boolean gotColors;

    /** some board elements.. */
    private Image[] cbeltCrop, ebeltCrop, diverseCrop, robosCrop, scoutCrop;

    /** maps Location(x,y) to the Image that should be painted as floor */
    private HashMap<Location, Image> floorElementHash = new HashMap<Location, Image>();

    /**
     * May contain names(!) of robots that should not be painted by request of
     * the user (to support the new "hide robot(s)" menu option). Does not
     * affect animated bots; those bots (and their laser shots) will be painted
     * anyway.
     */
    private HashSet<String> theseBotsShouldNotBePainted = new HashSet<String>(8);

    /** The current width of the whole board in pixels. */
    private int widthInPixel;

    /** The current height of the whole board in pixels. */
    private int heightInPixel;

    /** Stores data of the robots. */
    // private Bot[] robos;

    /**
     * The Scout. This robot is used for calculations, like making a suggestion
     * for the next move.
     * 
     */
    private Bot previewRob;

    /** Last position of our famous scout ;-) */
    private Location lastScoutPos = new Location();

    // Let's define some colors, so that everybody uses the same..
    public static final Color YELLOW = BotVis.YELLOW;

    public static final Color RED = BotVis.RED;

    public static final Color BLUE = BotVis.BLUE;

    public static final Color ROSA = BotVis.ROSA;

    public static final Color ORANGE = BotVis.ORANGE;

    public static final Color GRAY = BotVis.GRAY;

    public static final Color VIOLET = BotVis.VIOLET;

    public static final Color GREEN = BotVis.GREEN;

    /** Colors of the robots. */
    public static final Color[] ROBOCOLOR = { GREEN, YELLOW, RED, BLUE, ROSA, ORANGE, GRAY, VIOLET };

    // some often used composite values
    public static final AlphaComposite AC_SRC = AlphaComposite.getInstance(AlphaComposite.SRC);

    public static final AlphaComposite AC_SRC_OVER = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);

    public static final AlphaComposite AC_SRC_OVER_05 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

    public static final AlphaComposite AC_SRC_OVER_07 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f);

    /**
     * gameboard object; stores the information about the board we are playing
     * on; (where are the pits, where are lasers, and so on..)
     */
    private SimBoard sf;

    /** scale factor for zooming */
    private double dScale = 1.0;

    /**
     * To switch the method in which scaling is done.<br>
     * <br>
     * If set to <code>false</code>, we will create the background image of the
     * board only once and use Java's <code>Graphics.scale()</code> method to
     * scale the board if the user changes the zoom level. This can be somewhat
     * CPU heavy as the scaling has to be done everytime <code>{@link: paintComponent(Graphics)}</code> is called.<br>
     * <br>
     * If set to <code>true</code>, we will create a new, properly scaled
     * background image everytime the user changes the zoom level. This is the
     * faster method as the background isn't scaled everytime <code>{@link: paintComponent(Graphics)}</code> is called, but on big
     * boards (size 3x3 tiles) it might result in <code>OutOfMemoryError</code>s
     * if the user zooms in (like more than 110%). The OutOfMemoryError seems
     * not to crash the game, only the zooming in will fail.
     * 
     * CAUTION: setting this to false wil break the board location <-> pixel
     * calculations and so the locating of flags etc (will only work for
     * dScale=1)
     * 
     */
    private boolean usePrescaledBoardImage = true;

    /**
     * This will only be set if {@link: USE_PRESCALED_BGIMAGE} is set to <code>true</code>; in this case it will have the same value as {@link:
     * dScale}.<br>
     * If {@link: USE_PRESCALED_BGIMAGE} is set to <code>false</code> it must
     * always be 1.
     */
    private double dScale2ForBackground = 1.0;

    private int scaledFeldSize = FELDSIZE; // FELDSIZE * scale

    /** position to highlight */
    private Location highlightPos = new Location(0, 0);

    /** This is where we keep our internal robots for animations */
    private HashMap<String, Bot> internalBotHash = new java.util.HashMap<String, Bot>(8);

    private static final Location PIT = new Location(0, 0);

    private AnimationConfig currentAnimationConfig;

    // This stuff can be used to display the distance calculation on the board:
    public static final boolean DEBUG_DISTANCE_CALC = false;

    private Font myDebugFont = new Font("SansSerif", Font.PLAIN, 10);

    private Bot debugbot;

    private DistanceCalculator calc;

    public BoardView(SimBoard sf_neu) {
        init(sf_neu);

    }

    /**
     * Get a simple board view loaded from one tile file. This is not the way to
     * do this in general since boards may consist of serveral tiles and each
     * tile may be rotated.
     * 
     * @param boardFile
     *            The board file to load.
     */
    public BoardView(File boardFile) throws IOException, FormatException, FlagException {
        this(new SimBoard(12, 12, Board.readMagicString(boardFile)));
    }

    public double getScale() {
        return dScale;
    }

    private double biggestWorkingScaleFactor = 1;

    private boolean encounteredOutOfMemory = false;

    /**
     * Adapt this Component to the scaling factor; must not be called before
     * images are loaded
     * */
    public void setScale(double scale) {
        synchronized (rescaleLock) {
            synchronized (offScreenImgNotNullLock) {
                if (encounteredOutOfMemory && scale <= biggestWorkingScaleFactor) {
                    // zooming back into "safe" region, we can use the faster
                    // scaling again..
                    usePrescaledBoardImage = true;
                    encounteredOutOfMemory = false;
                    CAT.debug("switching back to faster scale method");
                    offScreenImage = null;
                    System.gc();
                }

                boolean tmpMemError = false;

                double newFeldSize = scale * FELDSIZE;
                dScale = scale;
                widthInPixel = (int) (sf.getSizeX() * newFeldSize);
                heightInPixel = (int) (sf.getSizeY() * newFeldSize);
                CAT.debug("dim : " + widthInPixel + " " + heightInPixel);
                if (usePrescaledBoardImage) {
                    dScale2ForBackground = scale;
                    scaledFeldSize = (int) newFeldSize;
                }
                super.setSize(widthInPixel + 1, heightInPixel + 1);
                try {

                    deleteScout();
                    if (usePrescaledBoardImage || offScreenImage == null) {
                        // the preComputed-BoardImage is no longer valid
                        offScreenImage = null;
                        System.gc();
                        offScreenImage = createBoardImage();
                    }
                    if (useStaticBg) {
                        staticBackground = createBoardImage();
                    }

                    repaint();

                }
                catch (OutOfMemoryError err) {

                    CAT.warn("encountered out of memory error because of zooming; switching the scaling method..");
                    offScreenImage = null;
                    System.gc();
                    tmpMemError = true;
                    encounteredOutOfMemory = true;
                    usePrescaledBoardImage = false;
                    scaledFeldSize = FELDSIZE;
                    dScale2ForBackground = 1;
                    offScreenImage = createBoardImage();
                }
                if (usePrescaledBoardImage && !tmpMemError && dScale > biggestWorkingScaleFactor) {
                    CAT.debug("no memory error, setting lastWorkingScale to: " + dScale);
                    biggestWorkingScaleFactor = dScale;
                }
            }
        }
    }

    private void init(SimBoard sf_neu) {

        gotColors = false;
        sf = sf_neu;

        // setDoubleBuffered(true);
        ImageMan.finishLoading();

        ebeltCrop = ImageMan.getImages(ImageMan.EBELTS);
        cbeltCrop = ImageMan.getImages(ImageMan.CBELTS);
        diverseCrop = ImageMan.getImages(ImageMan.DIVERSE);
        robosCrop = ImageMan.getImages(ImageMan.ROBOS);
        scoutCrop = ImageMan.getImages(ImageMan.SCOUT);

        initFloorHashMap();
        setScale(dScale); // does setSize(); must not be called before images
                          // are loaded

    }

    public Dimension getMinimumSize() {
        return new Dimension(widthInPixel, heightInPixel);
    }

    public Dimension getPreferredSize() {
        return new Dimension(widthInPixel, heightInPixel);
    }

    /** Create "name->color" - Hashtable */
    private void setRobColors(Bot[] robs) {
        gotColors = true;
        nameToColorHash = new java.util.HashMap<String, Color>(robs.length);
        for (int i = 0; i < robs.length; i++)
            if (robs[i] == null) {
                break;
            }
            else {
                nameToColorHash.put(robs[i].getName(), ROBOCOLOR[robs[i].getBotVis()]);
            }
    }

    /**
     * Lookup the Bot's color (by name)
     * 
     * @param name
     *            The Bot's name
     * @return The Bot's color. If the name is unknown, Color.white will be
     *         returned,
     */
    private Color getRobColor(String name) {
        Color foo = nameToColorHash.get(name);
        if (foo == null) {
            CAT.error("getRobColor: Color for " + name + "'s Laser not found");
            return Color.white;
        }
        else
            return foo;
    }

    /**
     * Show/hide a robot.
     * 
     * @param bot
     *            the robot to be shown or hidden
     * @param isVisible
     *            true: bot will be painted; false: bot will not be painted
     */
    protected void setRobotVisbility(Bot bot, boolean isVisible) {
        if (isVisible) {
            theseBotsShouldNotBePainted.remove(bot.getName());
        }
        else {
            theseBotsShouldNotBePainted.add(bot.getName());
        }
        repaint();
    }

    /** Call this to ensure that no robot is hidden. */
    protected void setAllRobotsVisible() {
        theseBotsShouldNotBePainted.clear();
        repaint();
    }

    /** Call this to ensure that all robots are hidden. */
    protected void setAllRobotsInvisible() {
        for (String botName : nameToColorHash.keySet()) {
            theseBotsShouldNotBePainted.add(botName);
        }
        repaint();
    }

    private void replaceInternalRobots(Bot[] robos_neu) {
        int count = robos_neu != null ? robos_neu.length : 0;
        synchronized (internalBotHash) {
            for (int i = 0; i < count; i++) { // initalizing my internal hash
                internalBotHash.put(robos_neu[i].getName(), robos_neu[i]);
            }
        }
    }

    protected void ersetzeRobos(Bot[] robos_neu) {
        waitForPhaseDisplay();

        if (!gotColors) { // this is the first time I get the robots
            setRobColors(robos_neu);
            replaceInternalRobots(robos_neu);
            repaint();
            return;
        }
        // we dont want to overwrite the robots positions/facings, because they
        // have been updated in animateRobMove()/animateRobTurn() before;
        // animateRobMove()/animateRobTurn() gets informed earlier, so
        // overwriting the positions/facings
        // would reset a robot back to a position/facing he has already left
        else {
            if (AnimationConfig.areMovementAnimationsEnabled()) {
                int count = robos_neu != null ? robos_neu.length : 0;
                for (int i = 0; i < count; i++) {
                    Bot serverBot = robos_neu[i];
                    String botName = serverBot.getName();
                    Bot ourBot = internalBotHash.get(botName);
                    Location ourPos = ourBot.getPos();
                    if (!(serverBot.getPos().equals(PIT) || serverBot.getDamage() >= 10 || ourPos.equals(PIT))) {
                        // ^^^^^^^^^^^^^
                        // otherwise we would not show
                        // the destroyed robot ever again
                        // as we would ignore him if he
                        // is placed on the board again
                        if (CAT.isDebugEnabled()) {
                            CAT.debug("ignoring server values of robot " + serverBot.getName()
                                            + " as my calculated values will be more accurate");
                        }
                        // use the internal kept values of our robot if it is not destroyed
                        serverBot.setPos(ourPos);
                        serverBot.setFacing(ourBot.getFacing());
                        // TODO robot virtuality might need the same treatment as position and facing?
                    }
                    else {
                        if (CAT.isDebugEnabled())
                            CAT.debug("using server values for position/facing of robot " + serverBot.getName());

                    }

                    synchronized (internalBotHash) {
                        internalBotHash.put(botName, serverBot);
                    }
                    repaint();
                } // end for
            } // end if (animations enabled)
            else { // no animation, simply replace all robots
                replaceInternalRobots(robos_neu);
                repaint();
                waitSomeTime(currentAnimationConfig.getDelayBetweenActions(), this);
            }
        }

    }

    /**
     * This method was added so that we can get the initial facings of the
     * robots. ersetzeRobos(Bot[]) doesn't work if animations are enabled (if
     * animations are enabled we have to use an internal version of the bots and
     * ignore the server values for position and facing because we have the bots
     * moved before we get the notification that something has changed (and
     * might get other notifications in between that still contain the old
     * values=>bots would be animated and then placed back to their position
     * before the animation)).
     * 
     * DON'T USE THIS METHOD TO TURN ROBOTS, use animateRobTurn etc. instead
     * 
     * @param updated
     *            the facing of these Bots will be updated
     */
    protected void updateFacings(Bot[] updated) {
        if (internalBotHash.isEmpty()) { // just in case, probably unnecessary
            CAT.warn("updateFacings called but we haven't got the robots yet!");
            replaceInternalRobots(updated);
            repaint();
            return;
        }

        int count = updated != null ? updated.length : 0;
        for (int i = 0; i < count; i++) {
            Bot tmp = updated[i];
            Bot internal = internalBotHash.get(tmp.getName());
            internal.setFacing(tmp.getFacing());
        }
        repaint();
    }

    private void paintBotsOnPositionButNotMe(Location position, Bot me, Graphics2D g2d, int xoffset, int yoffset) {

        int acht = (int) (8 * dScale2ForBackground);
        Composite old = g2d.getComposite();
        synchronized (internalBotHash) {
            int roboCounter = 0;
            for (Bot bot : internalBotHash.values()) {
                if (!bot.getName().equals(me.getName()) && bot.getPos().equals(position)) {
                    if (bot.isVirtual()) {
                        g2d.setComposite(AC_SRC_OVER_05);
                    }
                    else {
                        g2d.setComposite(AC_SRC_OVER);
                    }
                    int botVis = bot.getBotVis();
                    g2d.setColor(ROBOCOLOR[botVis]);
                    g2d.drawString(bot.getName(), xoffset, yoffset + acht + roboCounter * acht);
                    g2d.drawImage(BotVis.getBotImageByBotVis(botVis, bot.getFacing()), xoffset, yoffset,
                                    scaledFeldSize, scaledFeldSize, this);

                }

                roboCounter++;
            }
        }
        g2d.setComposite(old);
    }

    private void moveRobNorth(Bot internal, Graphics2D g2) {
        CAT.debug("moving bot one square to the north");
        AlphaComposite ac = AC_SRC_OVER;
        if (internal.isVirtual())
            ac = AC_SRC_OVER_05;
        Image imgRob = robosCrop[internal.getFacing() + internal.getBotVis() * 4];
        Location botStartPos = internal.getPos();
        Location botEndPos = new Location(botStartPos.getX(), botStartPos.getY() + 1);

        synchronized (rescaleLock) {
            int feldSize = scaledFeldSize;// FELDSIZE;//(int)(FELDSIZE*dScale);
            int xposScaled = (internal.getX() - 1) * feldSize;
            int yposScaled = (sf.getSizeY() - internal.getY() - 1) * feldSize;
            int clipLength = 2 * feldSize;

            BufferedImage offScreenClipImage;
            synchronized (offScreenImgNotNullLock) {
                offScreenClipImage = offScreenImage.getSubimage(xposScaled, yposScaled, feldSize, clipLength);
            }
            Graphics2D myg = (Graphics2D) offScreenClipImage.getGraphics();

            // Raster blankBg;
            BufferedImage blankBgImage;
            if (useStaticBg) {
                blankBgImage = staticBackground.getSubimage(xposScaled, yposScaled, feldSize, clipLength);
            }
            else {
                // blankBg = offscreeClipImage.getData(new
                // Rectangle(0,0,feldSize,clipLength));
                blankBgImage = new ScaledBufferedImage(feldSize, clipLength, offScreenClipImage.getType());
                blankBgImage.getGraphics().drawImage(offScreenClipImage, 0, 0, feldSize, clipLength, this);
            }

            paintBotsOnPositionButNotMe(botStartPos, internal, myg, 0, feldSize);
            paintBotsOnPositionButNotMe(botEndPos, internal, myg, 0, 0);

            myg.setComposite(ac);

            int animationOffsetMoveRob = currentAnimationConfig.getAnimationOffsetMoveRob();
            int animationDelayMoveRob = currentAnimationConfig.getAnimationDelayMoveRob();
            Rectangle oldClipBounds = g2.getClipBounds();
            g2.setClip(xposScaled, yposScaled, feldSize, clipLength);
            Composite oldComp = g2.getComposite();
            g2.setComposite(ac);

            // hs_scale
            // g2.scale(dScale, dScale);

            for (int yoffset = 0; yoffset >= -feldSize; yoffset -= animationOffsetMoveRob) {
                if (useStaticBg) {
                    myg.drawImage(blankBgImage, 0, 0, feldSize, clipLength, this); // paint
                                                                                   // the
                                                                                   // image
                }
                else {
                    myg.drawImage(blankBgImage, 0, 0, feldSize, clipLength, this);
                    g2.drawImage(blankBgImage, 0, 0, feldSize, clipLength, this);
                    // offScreenClipImage.setData(blankBg);
                }
                paintBotsOnPositionButNotMe(botStartPos, internal, myg, 0, feldSize);
                paintBotsOnPositionButNotMe(botEndPos, internal, myg, 0, 0);
                myg.drawImage(imgRob, 0, feldSize + yoffset, feldSize, feldSize, this); // paint
                                                                                        // the
                                                                                        // image

                g2.drawImage(offScreenClipImage, xposScaled, yposScaled, feldSize, clipLength, this);
                repaint(xposScaled, yposScaled, feldSize, clipLength);
                waitSomeTime(animationDelayMoveRob, this);

            }

            if (useStaticBg) {
                myg.drawImage(blankBgImage, 0, 0, feldSize, clipLength, this); // paint
                                                                               // the
                                                                               // image
            }
            else {
                Graphics2D tmpg = (Graphics2D) offScreenImage.getGraphics();
                Composite tmpComp = tmpg.getComposite();
                tmpg.setComposite(AC_SRC_OVER);
                tmpg.drawImage(blankBgImage, xposScaled, yposScaled, feldSize, clipLength, this);
                tmpg.setComposite(tmpComp);
                // myg.drawImage(blankBgImage, 0,0, feldSize, clipLength, this);
                // // paint the image
                // offscreeClipImage.setData(blankBg);
            }

            g2.setClip(oldClipBounds);
            g2.setComposite(oldComp);
        }
    }

    private void moveRobSouth(Bot internal, Graphics2D g2) {
        CAT.debug("moving bot one square to the south");
        AlphaComposite ac = AC_SRC_OVER;
        if (internal.isVirtual())
            ac = AC_SRC_OVER_05;
        Image imgRob = robosCrop[internal.getFacing() + internal.getBotVis() * 4];
        Location botStartPos = internal.getPos();
        Location botEndPos = new Location(botStartPos.getX(), botStartPos.getY() - 1);

        synchronized (rescaleLock) {
            int feldSize = scaledFeldSize;// (int)(FELDSIZE*dScale);
            int xpos64 = (internal.getX() - 1) * feldSize;
            int ypos64 = (sf.getSizeY() - internal.getY()) * feldSize;
            int clipLength = 2 * feldSize;

            BufferedImage offScreenClipImage;
            synchronized (offScreenImgNotNullLock) {
                offScreenClipImage = offScreenImage.getSubimage(xpos64, ypos64, feldSize, clipLength);
            }

            Graphics2D myg = (Graphics2D) offScreenClipImage.getGraphics();
            // myg.scale(dScale, dScale);
            BufferedImage blankBgImage;
            // Raster blankBg;
            if (useStaticBg) {
                blankBgImage = staticBackground.getSubimage(xpos64, ypos64, feldSize, clipLength);
            }
            else {
                // blankBg = offScreenClipImage.getData(new
                // Rectangle(0,0,feldSize,clipLength));
                blankBgImage = new ScaledBufferedImage(feldSize, clipLength, offScreenClipImage.getType());
                blankBgImage.getGraphics().drawImage(offScreenClipImage, 0, 0, feldSize, clipLength, this);
            }
            paintBotsOnPositionButNotMe(botStartPos, internal, myg, 0, 0);
            paintBotsOnPositionButNotMe(botEndPos, internal, myg, 0, feldSize);

            int animationOffsetMoveRob = currentAnimationConfig.getAnimationOffsetMoveRob();
            int animationDelayMoveRob = currentAnimationConfig.getAnimationDelayMoveRob();
            myg.setComposite(ac);
            Rectangle oldClipBounds = g2.getClipBounds();
            g2.setClip(xpos64, ypos64, feldSize, clipLength);
            Composite oldComp = g2.getComposite();
            g2.setComposite(oldComp);

            // hs_scale
            // g2.scale(dScale, dScale);
            for (int yoffset = 0; yoffset <= feldSize; yoffset += animationOffsetMoveRob) {
                if (useStaticBg) {
                    myg.drawImage(blankBgImage, 0, 0, feldSize, clipLength, this);
                }
                else {
                    // offScreenClipImage.setData(blankBg);
                    myg.drawImage(blankBgImage, 0, 0, feldSize, clipLength, this);
                }
                paintBotsOnPositionButNotMe(botStartPos, internal, myg, 0, 0);
                paintBotsOnPositionButNotMe(botEndPos, internal, myg, 0, feldSize);
                myg.drawImage(imgRob, 0, yoffset, feldSize, feldSize, this); // paint
                                                                             // the
                                                                             // image
                g2.drawImage(offScreenClipImage, xpos64, ypos64, feldSize, clipLength, this);
                waitSomeTime(animationDelayMoveRob, this);
                repaint(xpos64, ypos64, feldSize, clipLength);
            }
            if (useStaticBg) {
                myg.drawImage(blankBgImage, 0, 0, feldSize, clipLength, this);
            }
            else {
                // offScreenClipImage.setData(blankBg);
                // myg.drawImage(blankBgImage,0,0,feldSize,clipLength,this);
                Graphics2D tmpg = (Graphics2D) offScreenImage.getGraphics();
                Composite tmpComp = tmpg.getComposite();
                tmpg.setComposite(AC_SRC_OVER);
                tmpg.drawImage(blankBgImage, xpos64, ypos64, feldSize, clipLength, this);
                tmpg.setComposite(tmpComp);
            }
            g2.setClip(oldClipBounds);
            g2.setComposite(oldComp);

        }
    }

    private void moveRobEast(Bot internal, Graphics2D g2) {
        CAT.debug("moving bot one square to the east");
        AlphaComposite ac = AC_SRC_OVER;
        if (internal.isVirtual())
            ac = AC_SRC_OVER_05;
        Image imgRob = robosCrop[internal.getFacing() + internal.getBotVis() * 4];
        Location botStartPos = internal.getPos();
        Location botEndPos = new Location(botStartPos.getX() + 1, botStartPos.getY());
        synchronized (rescaleLock) {

            // g2.scale(dScale, dScale);
            int feldSize = scaledFeldSize;// (int)(FELDSIZE*dScale);
            int xpos64 = (internal.getX() - 1) * feldSize;
            int ypos64 = (sf.getSizeY() - internal.getY()) * feldSize;
            int clipLength = 2 * feldSize;

            BufferedImage offScreenClipImage;
            synchronized (offScreenImgNotNullLock) {
                offScreenClipImage = offScreenImage.getSubimage(xpos64, ypos64, clipLength, feldSize);
            }
            Graphics2D myg = (Graphics2D) offScreenClipImage.getGraphics();
            // myg.scale(dScale, dScale);
            // Raster blankBg;
            BufferedImage blankBgImage;
            if (useStaticBg) {
                blankBgImage = staticBackground.getSubimage(xpos64, ypos64, clipLength, feldSize);
            }
            else {
                // blankBg = offScreenClipImage.getData(new
                // Rectangle(0,0,clipLength, feldSize));
                blankBgImage = new ScaledBufferedImage(clipLength, feldSize, offScreenClipImage.getType());
                blankBgImage.setData(offScreenClipImage.getData(new Rectangle(0, 0, clipLength, feldSize)));
                blankBgImage.getGraphics().drawImage(offScreenClipImage, 0, 0, clipLength, feldSize, this);
            }
            paintBotsOnPositionButNotMe(botStartPos, internal, myg, 0, 0);
            paintBotsOnPositionButNotMe(botEndPos, internal, myg, feldSize, 0);

            myg.setComposite(ac);
            int animationOffsetMoveRob = currentAnimationConfig.getAnimationOffsetMoveRob();
            int animationDelayMoveRob = currentAnimationConfig.getAnimationDelayMoveRob();
            Rectangle oldClipBounds = g2.getClipBounds();
            g2.setClip(xpos64, ypos64, clipLength, feldSize);
            Composite oldComp = g2.getComposite();
            // hs_scale
            // g2.scale(dScale, dScale);

            for (int xoffset = 0; xoffset <= feldSize; xoffset += animationOffsetMoveRob) {
                if (useStaticBg) {
                    myg.drawImage(blankBgImage, 0, 0, clipLength, feldSize, this); // paint
                                                                                   // the
                                                                                   // image
                }
                else {
                    // offScreenClipImage.setData(blankBg);
                    myg.drawImage(blankBgImage, 0, 0, clipLength, feldSize, this);
                }
                paintBotsOnPositionButNotMe(botStartPos, internal, myg, 0, 0);
                paintBotsOnPositionButNotMe(botEndPos, internal, myg, feldSize, 0);

                myg.drawImage(imgRob, xoffset, 0, feldSize, feldSize, this); // paint
                                                                             // the
                                                                             // image
                g2.drawImage(offScreenClipImage, xpos64, ypos64, clipLength, feldSize, this);
                repaint(xpos64, ypos64, clipLength, feldSize);
                waitSomeTime(animationDelayMoveRob, this);

            }
            if (useStaticBg) {
                myg.drawImage(blankBgImage, 0, 0, clipLength, feldSize, this); // paint
                                                                               // the
                                                                               // image
            }
            else {
                Graphics2D tmpg = (Graphics2D) offScreenImage.getGraphics();
                Composite tmpComp = tmpg.getComposite();
                tmpg.setComposite(AC_SRC_OVER);
                tmpg.drawImage(blankBgImage, xpos64, ypos64, clipLength, feldSize, this);
                tmpg.setComposite(tmpComp);
                // myg.drawImage(blankBgImage, 0,0, clipLength, feldSize, this);
                // // paint the image
                // offScreenClipImage.setData(blankBg);
            }
            g2.setClip(oldClipBounds);
            g2.setComposite(oldComp);
        }

    }

    private void moveRobWest(Bot internal, Graphics2D g2) {
        CAT.debug("moving bot one square to the west");
        AlphaComposite ac = AC_SRC_OVER;
        if (internal.isVirtual())
            ac = AC_SRC_OVER_05;
        Image imgRob = robosCrop[internal.getFacing() + internal.getBotVis() * 4];
        Location botStartPos = internal.getPos();
        Location botEndPos = new Location(botStartPos.getX() - 1, botStartPos.getY());
        synchronized (rescaleLock) {

            int feldSize = scaledFeldSize;// (int)( FELDSIZE*dScale);
            int clipLength = 2 * feldSize;
            int xpos64 = (internal.getX() - 2) * feldSize;
            int ypos64 = (sf.getSizeY() - internal.getY()) * feldSize;

            BufferedImage offScreenClipImage;
            synchronized (offScreenImgNotNullLock) {
                offScreenClipImage = offScreenImage.getSubimage(xpos64, ypos64, clipLength, feldSize);
            }
            Graphics2D offScreenClipGraphics = (Graphics2D) offScreenClipImage.getGraphics();
            // offScreenClipGraphics.scale(dScale, dScale);
            BufferedImage blankBgImage;
            // Raster blankBg;
            if (useStaticBg) {
                blankBgImage = staticBackground.getSubimage(xpos64, ypos64, clipLength, feldSize);
            }
            else {
                // blankBg = offScreenClipImage.getData(new
                // Rectangle(0,0,clipLength,feldSize));
                blankBgImage = new ScaledBufferedImage(clipLength, feldSize, offScreenClipImage.getType());
                blankBgImage.getGraphics().drawImage(offScreenClipImage, 0, 0, clipLength, feldSize, this);
            }

            paintBotsOnPositionButNotMe(botStartPos, internal, offScreenClipGraphics, feldSize, 0);
            paintBotsOnPositionButNotMe(botEndPos, internal, offScreenClipGraphics, 0, 0);

            int animationOffsetMoveRob = currentAnimationConfig.getAnimationOffsetMoveRob();
            int animationDelayMoveRob = currentAnimationConfig.getAnimationDelayMoveRob();
            offScreenClipGraphics.setComposite(ac);
            Rectangle oldClipBounds = g2.getClipBounds();
            g2.setClip(xpos64, ypos64, clipLength, feldSize);
            Composite oldComp = g2.getComposite();
            g2.setComposite(ac);
            // hs_scale
            // g2.scale(dScale, dScale);
            for (int xoffset = 0; xoffset >= -feldSize; xoffset -= animationOffsetMoveRob) {
                if (useStaticBg) {
                    offScreenClipGraphics.drawImage(blankBgImage, 0, 0, clipLength, feldSize, this);
                }
                else {
                    // offScreenClipImage.setData(blankBg);
                    offScreenClipGraphics.drawImage(blankBgImage, 0, 0, clipLength, feldSize, this);
                }
                paintBotsOnPositionButNotMe(botStartPos, internal, offScreenClipGraphics, feldSize, 0);
                paintBotsOnPositionButNotMe(botEndPos, internal, offScreenClipGraphics, 0, 0);
                offScreenClipGraphics.drawImage(imgRob, feldSize + xoffset, 0, feldSize, feldSize, this); // paint
                                                                                                          // the
                                                                                                          // image
                g2.drawImage(offScreenClipImage, xpos64, ypos64, clipLength, feldSize, this);
                waitSomeTime(animationDelayMoveRob, this);
                repaint(xpos64, ypos64, clipLength, feldSize);
            }

            if (useStaticBg) {
                offScreenClipGraphics.drawImage(blankBgImage, 0, 0, clipLength, feldSize, this);
            }
            else {
                // offScreenClipImage.setData(blankBg);
                // offScreenClipGraphics.drawImage(blankBgImage,0,0,clipLength,feldSize,this);
                Graphics2D tmpg = (Graphics2D) offScreenImage.getGraphics();
                Composite tmpComp = tmpg.getComposite();
                tmpg.setComposite(AC_SRC_OVER);
                tmpg.drawImage(blankBgImage, xpos64, ypos64, clipLength, feldSize, this);
                tmpg.setComposite(tmpComp);
            }
            g2.setClip(oldClipBounds);
            g2.setComposite(oldComp);
        }
    }

    protected/* synchronized */void animateRobUTurn(Bot rob) {
        waitForPhaseDisplay();
        if (!AnimationConfig.areMovementAnimationsEnabled()) {
            return;
        }

        Bot internal = internalBotHash.get(rob.getName());// getBotByName(rob.getName());
        turnRobot(internal, 180, 2 * currentAnimationConfig.getAnimationStepsTurnRob(), true);
        internal.turnClockwise();
        internal.turnClockwise();
        waitSomeTime(currentAnimationConfig.getDelayBetweenActions(), this);
    }

    /**
     * @param direction
     *            either BOT_TURN_CLOCKWISE or BOT_TURN_COUNTER_CLOCKWISE in
     *            MessageID
     */
    protected/* synchronized */void animateRobTurn(Bot rob, int direction) {
        waitForPhaseDisplay();
        if (!AnimationConfig.areMovementAnimationsEnabled()) {
            return;
        }
        try {
            Bot internal = internalBotHash.get(rob.getName());// getBotByName(rob.getName());
            currentlyAnimated = internal;
            int animationStepsTurnRob = currentAnimationConfig.getAnimationStepsTurnRob();

            if (direction == OtherConstants.BOT_TURN_CLOCKWISE) {
                turnRobot(internal, 90, animationStepsTurnRob, true);
                internal.turnClockwise();
            }
            else {
                turnRobot(internal, 90, animationStepsTurnRob, false);
                internal.turnCounterClockwise();
            }
        }
        // better safe than sorry:
        catch (RasterFormatException fixmeCanForExampleHappenIfWeWantToAnimateABotThatHasBeenKilledRightBefore) {
            CAT.error(fixmeCanForExampleHappenIfWeWantToAnimateABotThatHasBeenKilledRightBefore);
        }
        finally {
            currentlyAnimated = null;
        }
        waitSomeTime(currentAnimationConfig.getDelayBetweenActions(), this);

    }

    private void turnRobot(Bot internal, int angle, int animationSteps, boolean clockWise) {
        try {
            AlphaComposite ac = AC_SRC_OVER;
            if (internal.isVirtual())
                ac = AC_SRC_OVER_05;
            Image cropRobImage = robosCrop[internal.getFacing() + internal.getBotVis() * 4];
            double rotateTheta;
            if (clockWise)
                rotateTheta = Math.toRadians(angle / animationSteps);
            else
                rotateTheta = Math.toRadians(360 - angle / animationSteps);
            // CAT.debug("turning bot; theta="+rotateTheta);
            synchronized (rescaleLock) {
                Graphics2D mainGraphics = getScaledGraphics();
                Composite oldComposite = mainGraphics.getComposite();
                int feldSize = scaledFeldSize;
                int xposScaled = (internal.getX() - 1) * feldSize;
                int yposScaled = (sf.getSizeY() - internal.getY()) * feldSize;
                int clipLength = feldSize;
                int halfSize = feldSize / 2;
                // TODO rasterFormatException
                BufferedImage offScreenClipImage;
                synchronized (offScreenImgNotNullLock) {
                    offScreenClipImage = offScreenImage.getSubimage(xposScaled, yposScaled, feldSize, clipLength);
                    // new BufferedImage(feldSize, feldSize,
                    // BufferedImage.TYPE_INT_RGB);
                }
                Graphics2D offScreenClip = (Graphics2D) offScreenClipImage.getGraphics();
                offScreenClip.setComposite(ac);
                mainGraphics.setComposite(ac);
                BufferedImage blankImage;

                // Raster blank;
                if (useStaticBg) {
                    // blank = staticBackground.getData(new
                    // Rectangle(xposScaled,yposScaled,feldSize,clipLength));
                    blankImage = staticBackground.getSubimage(xposScaled, yposScaled, feldSize, clipLength);
                    // offScreenClipImage.setData(blank);
                    offScreenClip.drawImage(blankImage, 0, 0, feldSize, clipLength, this);
                    mainGraphics.drawImage(blankImage, xposScaled, yposScaled, feldSize, clipLength, this);
                }
                else {
                    // there shouldn't be any active content on the offScreen
                    // image if useStaticBg==false
                    // blank = offScreenClipImage.getData(new
                    // Rectangle(0,0,feldSize,clipLength));
                    blankImage = new ScaledBufferedImage(feldSize, clipLength, BufferedImage.TYPE_INT_ARGB);
                    blankImage.getGraphics().drawImage(offScreenClipImage, 0, 0, feldSize, clipLength, this);

                }

                // paintBotsOnPositionButNotMe(internal.getPos(),
                // internal,offScreenClip,0,0);
                Raster blankWithBots = offScreenClipImage.getData(new Rectangle(0, 0, feldSize, clipLength));
                // offScreenClipImage.setData(blank);

                // painting the animated bot
                // offScreenClip.drawImage(cropRobImage, 0, 0, feldSize,
                // feldSize, this);

                int animationStepsTurnRob = animationSteps;// currentAnimationConfig.getAnimationStepsTurnRob();
                int animationDelayTurnRob = currentAnimationConfig.getAnimationDelayTurnRob();
                for (int step = 0; step < animationStepsTurnRob; step++) {

                    // offScreenClip.drawImage(blankImage,0,0,feldSize,clipLength,this);
                    // paintBotsOnPositionButNotMe(internal.getPos(),
                    // internal,offScreenClip,0,0);
                    offScreenClipImage.setData(blankWithBots); // erasing the
                                                               // offscreen
                                                               // image with the
                                                               // boardbackground
                    offScreenClip.rotate(rotateTheta, halfSize, halfSize); // rotating
                                                                           // the
                                                                           // robot
                                                                           // pic
                                                                           // further
                    offScreenClip.drawImage(cropRobImage, 0, 0, feldSize, feldSize, this);
                    // paint the offscreen image on the screen:
                    mainGraphics.drawImage(offScreenClipImage, xposScaled, yposScaled, feldSize, clipLength, this);
                    waitSomeTime(animationDelayTurnRob, this);
                }
                if (useStaticBg) {
                    // offScreenClipImage.setData(blank);
                    offScreenClip.drawImage(blankImage, 0, 0, feldSize, clipLength, this);
                }
                else {
                    // doesn't work: offScreenClipImage.setData(blank);
                    // also doesn't work: offScreenClip.XYZ()
                    Graphics2D tmpg = (Graphics2D) offScreenImage.getGraphics();
                    Composite tmpComp = tmpg.getComposite();
                    tmpg.setComposite(AC_SRC_OVER);
                    tmpg.drawImage(blankImage, xposScaled, yposScaled, feldSize, clipLength, this);
                    tmpg.setComposite(tmpComp);
                    mainGraphics.setComposite(oldComposite);
                }
            }
        }
        catch (RasterFormatException e) {
            CAT.error(e.getMessage(), e);
        }
    }

    protected void animateBotCrushed(Bot b) {
        SoundMan.playSound(SoundMan.CRUSHED);
        shrinkRobot(b);
    }

    protected void animatePitFall(Bot b) {
        SoundMan.playSound(SoundMan.PIT);
        Location pos = b.getPos();

        if (pos.x < 1 || pos.y < 1 || pos.x > sf.getSizeX() || pos.y > sf.getSizeY()) {
            // don't shrink if bot fell from the board
            CAT.debug("ignoring pitfall animation for " + pos);
        }
        else {
            if (sf.getFloor(pos.x, pos.y).isPit()) { // don't shrink if bot fell
                                                     // from the board
                shrinkRobot(b);
            }
        }

        synchronized (this) {
            waitSomeTime(currentAnimationConfig.getLaserDelayAfterEndOfAnimation(), this);
        }
    }

    private void shrinkRobot(Bot internal) {
        currentlyAnimated = internal;
        double scalePerStep = 0.9;
        int numberOfShrinks = 15;
        AlphaComposite ac = AC_SRC_OVER;
        if (internal.isVirtual())
            ac = AC_SRC_OVER_05;
        Image cropRobImage = robosCrop[internal.getFacing() + internal.getBotVis() * 4];

        synchronized (rescaleLock) {
            Graphics2D mainGraphics = getScaledGraphics();
            Composite oldComposite = mainGraphics.getComposite();
            int feldSize = scaledFeldSize;
            int xposScaled = (internal.getX() - 1) * feldSize;
            int yposScaled = (sf.getSizeY() - internal.getY()) * feldSize;
            int clipLength = feldSize;

            BufferedImage backgroundImage;
            synchronized (offScreenImgNotNullLock) {
                backgroundImage = offScreenImage.getSubimage(xposScaled, yposScaled, feldSize, clipLength);
                // new BufferedImage(feldSize, feldSize,
                // BufferedImage.TYPE_INT_RGB);
            }
            Graphics2D background = (Graphics2D) backgroundImage.getGraphics();
            background.setComposite(ac);
            // saving a copy of the background:
            Raster blank = backgroundImage.getData(new Rectangle(0, 0, feldSize, clipLength));
            mainGraphics.drawImage(backgroundImage, xposScaled, yposScaled, feldSize, feldSize, this);

            // painting the animated bot
            // background.drawImage(cropRobImage, 0, 0, feldSize, feldSize,
            // this);
            mainGraphics.setComposite(ac);
            double doffset = 0;
            for (int step = 0; step < numberOfShrinks; step++) {
                backgroundImage.setData(blank); // erasing the offscreen image
                                                // with the boardbackground
                background.scale(scalePerStep, scalePerStep);
                doffset += feldSize * (1 - scalePerStep);
                int offset = (int) doffset;
                background.drawImage(cropRobImage, 0 + offset, 0 + offset, feldSize, feldSize, this);
                // paint the offscreen image on the screen:

                mainGraphics.drawImage(backgroundImage, xposScaled, yposScaled, feldSize, clipLength, this);
                waitSomeTime(100, this); // TODO make delay configurable
            }
            backgroundImage.setData(blank);
            internal.setPos(PIT);
            mainGraphics.setComposite(oldComposite);
        }
        currentlyAnimated = null;
    }

    protected/* synchronized */void animateRobMove(Bot rob, int direction) {
        waitForPhaseDisplay();

        // important: according to the code in SpielfeldSim we do not get
        // the updated robot position;
        // the updated position will be the endposition of the total move,
        // as ersetzeRobos() will be called when the robot has reached its
        // final position
        // THIS METHOD will be called for each single step of a move
        // (i.e. three times for a "Move 3 forward")
        // So we have to update our internal position of the robot in
        // between to show an animation that makes sense
        try {
            String name = rob.getName();

            Bot internal = internalBotHash.get(name);

            int oldX = internal.getX();
            int oldY = internal.getY();
            currentlyAnimated = internal;
            // paint the move animation and update the position in my internal
            // robot array
            switch (direction) {
                case NORTH: {
                    if (oldY < sf.getSizeY()) {
                        moveRobNorth(internal, getScaledGraphics());
                        internal.setPos(oldX, oldY + 1);
                    }
                    return;
                }
                case EAST: {
                    if (oldX < sf.getSizeX()) {
                        moveRobEast(internal, getScaledGraphics());
                        internal.setPos(oldX + 1, oldY);
                    }
                    return;
                }
                case WEST: {
                    if (oldX > 1) {
                        moveRobWest(internal, getScaledGraphics());
                        internal.setPos(oldX - 1, oldY);
                    }
                    return;
                }
                case SOUTH: {
                    if (oldY > 1) {
                        moveRobSouth(internal, getScaledGraphics());
                        internal.setPos(oldX, oldY - 1);
                    }
                    return;
                }
                default: {
                    // this must not happen,
                    // otherwise the whole gui might be useless as it keeps
                    // probably
                    // a wrong position for one robot
                    CAT.fatal("Got illgeal direction for animating robot");
                }
            }
        } // better safe than sorry:
        catch (RasterFormatException fixmeCanForExampleHappenIfWeWantToAnimateABotThatHasBeenKilledRightBefore) {
            CAT.error(fixmeCanForExampleHappenIfWeWantToAnimateABotThatHasBeenKilledRightBefore);
        }
        currentlyAnimated = null;
        waitSomeTime(currentAnimationConfig.getDelayBetweenActions(), this);
    }

    /**
     * Draws animated robot lasers.
     * 
     * @param sourceRob
     *            position of firing robot
     * @param targetRob
     *            position of the robot hit
     */

    public void doRobLaser(Bot sourceRob, Bot targetRob) {
        // allDone = false;
        if (CAT.isDebugEnabled())
            CAT.debug("doRobLaser: " + sourceRob.getName() + " -> " + targetRob.getName());
        Location source = sourceRob.getPos();
        Location target = targetRob.getPos();
        int laserFacing = sourceRob.getFacing();
        int laenge = calculateLaserLength(source, target, laserFacing);
        laenge *= scaledFeldSize;

        String name = sourceRob.getName();

        Color robColor = getRobColor(name);

        SoundMan.playSound(BotVis.getBotLaserSoundByName(name));
        synchronized (this) {
            waitSomeTime(currentAnimationConfig.getLaserDelayBetweenStartOfSoundAndAnimation(), this);

            Graphics2D g2 = getScaledGraphics();
            // g2.scale(dScale, dScale);
            int step = Math.max(1, (int) (laenge / FULL_LENGTH_DOUBLE)); // step
                                                                         // must
                                                                         // not
                                                                         // be 0
                                                                         // otherwise
                                                                         // the
                                                                         // while
                                                                         // loop
                                                                         // below
                                                                         // won't
                                                                         // exit
            int tmp_laenge = step;

            // for (int i = 1; i <= FULL_LENGTH_INT; i++) {
            // int tmp_laenge = (int) ((((double) i) / FULL_LENGTH_DOUBLE) *
            // laenge);

            int delayPerStep = currentAnimationConfig.getLaserDelayPerAnimationStep();
            while (tmp_laenge <= laenge) {

                paintActiveRobLaser(g2, source, laserFacing, tmp_laenge, robColor);
                tmp_laenge += step;
                // synchronized(this){
                waitSomeTime(delayPerStep, this);

                // }
            }
            repaint();
            waitSomeTime(currentAnimationConfig.getLaserDelayAfterEndOfAnimation(), this);
        }

        // drawRobLaser=false;
        if (SoundMan.isSoundActive()) {
            // SoundMan.playSound(SoundMan.BUMM);
            synchronized (this) {
                waitSomeTime(200, this);
            }
        }
        waitSomeTime(currentAnimationConfig.getDelayBetweenActions(), this);

    }

    /**
     * Berechnet die (Java-)Pixel-Koordinaten der linken oberen Ecke eines
     * Bord-Feldes. Gibt die x- und y-Pixelwerte der linken oberen Ecke des
     * Feldes mit der Position (x,y) auf dem Spielplan zurueck.
     * 
     * @param x
     *            Die X-Koordinate des Feldes
     * @param y
     *            Die Y-Koordinate des Feldes
     * @return Die Position der linken oberen Ecke des Feldes als
     *         Java-Pixelwerte zum Zeichnen.
     */
    private Location mapC2PixelNorthWest(int x, int y) {
        Location pixel = new Location();
        pixel.x = (x - 1) * scaledFeldSize;
        pixel.y = (sf.getSizeY() - y) * scaledFeldSize;
        return pixel;
    }

    /**
     * Berechnet die (Java-)Pixelwerte fuer den Mittelpunkt des Feldes. Genauer:
     * Den Punkt (31,31) auf dem 64x64 grossen Feld mit Koordinaten zwischen 0
     * und 63.
     */
    private Location mapC2PixelCenter(int x, int y) {
        Location pixel = mapC2PixelNorthWest(x, y);
        pixel.x += scaledFeldSize / 2;
        pixel.y += scaledFeldSize / 2;
        return pixel;
    }

    private void paintActiveRobLaser(Graphics g, Location source, int laserFacing, int actualLength, Color c) {
        // Laser sollen immer von Source nach Target gezeichnet werden

        int breite = (int) (4 * dScale2ForBackground); // Die Breite des Lasers,
                                                       // sollte gerade sein
        int lSourceX = 0;
        int lSourceY = 0; // Anfangspunkt des Lasers in Pixeln,
        Location tmp = mapC2PixelCenter(source.x, source.y); /*
                                                              * Mitte (Punkt
                                                              * (31,31) auf Feld
                                                              * mit Punkten von
                                                              * 0 bis 63, also
                                                              * einem 64x64
                                                              * grossen Feld
                                                              */

        Graphics2D g2d = (Graphics2D) g;
        AlphaComposite ac = AC_SRC_OVER_05;
        g2d.setComposite(ac);
        g2d.setColor(c);
        switch (laserFacing) {
            case NORTH: {
                lSourceX = tmp.x - (breite / 2);
                lSourceY = tmp.y - actualLength;
                g2d.fillRect(lSourceX, lSourceY, breite, actualLength);
                break;
            }
            case EAST: {
                lSourceX = tmp.x;
                lSourceY = tmp.y - (breite / 2);
                g2d.fillRect(lSourceX, lSourceY, actualLength, breite);
                break;
            }
            case SOUTH: {
                lSourceX = tmp.x - (breite / 2);
                lSourceY = tmp.y;
                g2d.fillRect(lSourceX, lSourceY, breite, actualLength);
                break;
            }
            case WEST: {
                lSourceX = tmp.x - actualLength;
                lSourceY = tmp.y - (breite / 2 - 1);
                g2d.fillRect(lSourceX, lSourceY, actualLength, breite);
                break;
            }
            default: {
                CAT.error("BoardView.paintActiveRobLaser: ");
                CAT.error("Ungueltige Laserrichtung: " + laserFacing);
            }
        }// end switch facing
        g2d.setComposite(AC_SRC);
    }

    /**
     * Berechnet die Laenge eines Lasers (in Feldern) zwischen zwei Botn. Bsp:
     * Schiesst ein Bot an Position (2,2) auf einen Bot an Position (5,2), so
     * wird 3 zurueckgegeben (=> multipliziert man den Rueckgabewert mit 64, so
     * erhaelt man die zu zeichnende Laserlaenge in Pixeln).
     * 
     * @param source
     *            Das Startfeld des Lasers
     * @param target
     *            Das Feld des Ziels
     * @param facing
     *            Die Richtung, in die der Laser schiesst (0=NORTH, 1=EAST,
     *            2=SOUTH, 3=WEST)
     * @return Die Anzahl der Felder, ueber die der Laser geht (inklusive
     *         Startfeld).
     */
    private int calculateLaserLength(Location source, Location target, int facing) {

        int laenge = 0;

        switch (facing) {
            case NORTH: {
                laenge = target.y - source.y;
                break;
            }
            case EAST: {
                laenge = target.x - source.x;
                break;
            }
            case SOUTH: {
                laenge = source.y - target.y;
                break;
            }
            case WEST: {
                laenge = source.x - target.x;
                break;
            }
            default: {
                CAT.error("BoardView.calculateLaserLength(): ungueltige Laserrichtung: " + facing);
            }
        }
        // System.err.println("calculate Length: ("+source.x+","+source.y+")-"+facing+"->("+target.x+","+target.y+") ist "+laenge+" lang");
        return laenge;
    }

    private void paintActiveBordLaser(Graphics g, Location source, int facing, int strength, int actualLength) {

        Graphics2D g2d = (Graphics2D) g;
        AlphaComposite ac = AC_SRC_OVER;// , 0.5f
        g2d.setComposite(ac);

        int breite = (int) (dScale2ForBackground * 4); // Die Breite des Lasers,
                                                       // sollte gerade sein
        int eleven = (int) (dScale2ForBackground * 11);
        int lSourceX = 0;
        int lSourceY = 0; // Anfangspunkt des Lasers in Pixeln,
        Location tmp = mapC2PixelCenter(source.x, source.y);
        // synchronized (lock) {
        switch (facing) {
            case NORTH: {
                lSourceX = tmp.x - (breite / 2 - 1);
                lSourceY = tmp.y - actualLength + (scaledFeldSize / breite);
                if (strength != 2) {
                    g2d.setColor(fstLaserColor);
                    g2d.fillRect(lSourceX, lSourceY, breite, actualLength);
                    g2d.setColor(sndLaserColor);
                    g2d.drawRect(lSourceX, lSourceY, breite, actualLength);
                }
                if (strength > 1) {
                    int offset = (strength - 1) * eleven;
                    g2d.setColor(fstLaserColor);
                    g2d.fillRect(lSourceX - offset, lSourceY, breite, actualLength);
                    g2d.fillRect(lSourceX + offset, lSourceY, breite, actualLength);
                    g2d.setColor(sndLaserColor);
                    g2d.drawRect(lSourceX - offset, lSourceY, breite, actualLength);
                    g2d.drawRect(lSourceX + offset, lSourceY, breite, actualLength);
                }
                break;
            }
            case SOUTH: {
                lSourceX = tmp.x - (breite / 2 - 1);
                lSourceY = tmp.y - (scaledFeldSize / 4) + 1;
                if (strength != 2) {
                    g2d.setColor(fstLaserColor);
                    g2d.fillRect(lSourceX, lSourceY, breite, actualLength);
                    g2d.setColor(sndLaserColor);
                    g2d.drawRect(lSourceX, lSourceY, breite, actualLength);
                }
                if (strength > 1) {
                    int offset = (strength - 1) * eleven;
                    g2d.setColor(fstLaserColor);
                    g2d.fillRect(lSourceX - offset, lSourceY, breite, actualLength);
                    g2d.fillRect(lSourceX + offset, lSourceY, breite, actualLength);
                    g2d.setColor(sndLaserColor);
                    g2d.drawRect(lSourceX - offset, lSourceY, breite, actualLength);
                    g2d.drawRect(lSourceX + offset, lSourceY, breite, actualLength);
                }
                break;
            }
            case EAST: {
                lSourceX = tmp.x - (scaledFeldSize / breite) + breite;
                lSourceY = tmp.y - (breite / 2 - 1);

                if (strength != 2) {
                    g2d.setColor(fstLaserColor);
                    g2d.fillRect(lSourceX, lSourceY, actualLength, breite);
                    g2d.setColor(sndLaserColor);
                    g2d.drawRect(lSourceX, lSourceY, actualLength, breite);
                }
                if (strength > 1) {
                    int offset = (strength - 1) * eleven;
                    g2d.setColor(fstLaserColor);
                    g2d.fillRect(lSourceX, lSourceY - offset, actualLength, breite);
                    g2d.fillRect(lSourceX, lSourceY + offset, actualLength, breite);
                    g2d.setColor(sndLaserColor);
                    g2d.drawRect(lSourceX, lSourceY - offset, actualLength, breite);
                    g2d.drawRect(lSourceX, lSourceY + offset, actualLength, breite);
                }
                break;
            }
            case WEST: {
                lSourceX = tmp.x - actualLength + (scaledFeldSize / 4) + ((int) (3 * dScale2ForBackground));
                lSourceY = tmp.y - (breite / 2 - 1);
                if (strength != 2) {
                    g2d.setColor(fstLaserColor);
                    g2d.fillRect(lSourceX, lSourceY, actualLength - 2, breite);
                    g2d.setColor(sndLaserColor);
                    g2d.drawRect(lSourceX, lSourceY, actualLength - 2, breite);
                }
                if (strength > 1) {
                    int offset = (strength - 1) * eleven;
                    g2d.setColor(fstLaserColor);
                    g2d.fillRect(lSourceX, lSourceY - offset, actualLength - 2, breite);
                    g2d.fillRect(lSourceX, lSourceY + offset, actualLength - 2, breite);
                    g2d.setColor(sndLaserColor);
                    g2d.drawRect(lSourceX, lSourceY - offset, actualLength - 2, breite);
                    g2d.drawRect(lSourceX, lSourceY + offset, actualLength - 2, breite);
                }
                break;
            }
            default: {
                CAT.error("BoardView.paintActiveRobLaser: ");
                CAT.error("illegal value for laser facing: " + facing);
            }
        }// end switch facing
         // allDone = true;
         // lock.notifyAll();
         // }
         // g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
    }

    /**
     * @param laserPos
     *            Die Koordinaten des schiessenden BordLasers
     * @param laserDir
     *            Die Ausrichtung des Lasers
     * @param targetRob
     *            Die Koordinaten des getroffenen Bots
     * @param surrounding
     *            Das ScrollPane in dem der Canvas dargestellt wird
     */
    protected void doBordLaser(Location laserPos, int laserDir, int strength, Location targetRob) {
        // init laser values
        int laenge = calculateLaserLength(laserPos, targetRob, laserDir);
        laenge = laenge * scaledFeldSize + (scaledFeldSize / 4) + ((int) (dScale2ForBackground * 3));

        // paint lasers step by step
        Graphics2D g2 = getScaledGraphics();
        for (int i = 1; i <= FULL_LENGTH_INT; i++) {
            int tmp_laenge = (int) ((i / FULL_LENGTH_DOUBLE) * laenge);

            paintActiveBordLaser(g2, laserPos, laserDir, strength, tmp_laenge);

        }

        repaint(); // lasers again
        synchronized (this) {
            waitSomeTime(currentAnimationConfig.getLaserDelayAfterEndOfAnimation(), this);
        }

    }

    private boolean turner(int x, int y, int r) {
        Floor floor = sf.floor(x, y);
        return floor.isBelt() && (floor.getBeltDirection() == r);
    }

    protected void preview(int phase, Bot simRob) {
        if (phase == 0) {
            // scoutOn = true; // flag for repaint: yes, paint scout!
            previewRob = null;
            deleteScout();
            // repaint();
            return;
        }

        Bot[] robs = new Bot[1];
        robs[0] = simRob;
        for (int i = 1; i < phase + 1; i++) {
            // sf.doPhase(phase, simRob);
            sf.doPhase(phase, robs);
        }
        // previewRob = vorschauRobArray[0];
        previewRob = simRob;
        showScout(simRob.getPos());

        // repaint();

    }

    protected void preview(int phasen, Bot[] previewRoboters) {
        if (phasen == 0) {
            previewRob = null;
            deleteScout();
            // repaint();
            return;
        }

        for (int i = 1; i < phasen + 1; i++) {
            sf.doPhase(i, previewRoboters);
        }
        previewRob = previewRoboters[0];
        showScout(previewRob.getPos());
        // repaint();

    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    protected void paintFeldBoden(Graphics g, int xpos, int ypos, int actx, int acty) {
        paintFeldBoden(g, xpos, ypos, actx, acty, scaledFeldSize, scaledFeldSize);
    }

    // TODO: Make method private again and find a proper way to update the hash
    // map if nec.
    public void initFloorHashMap() {
        int sizeX = sf.getSizeX();
        int sizeY = sf.getSizeY();
        for (int x = 1; x <= sizeX; x++) {
            for (int y = 1; y <= sizeY; y++) {
                floorElementHash.put(new Location(x, y), getFloorImage(x, y));
            }
        }
    }

    private Image getFloorImage(int xpos, int ypos) {
        Floor floor = sf.floor(xpos, ypos);
        switch (floor.getType()) {

            case (FloorConstants.FL_PIT):
                return diverseCrop[3];
            case (FloorConstants.FL_NORMAL):
                return diverseCrop[24 + ((xpos * ypos * 19) % 17) % 4];
            case (FloorConstants.FL_ROTGEAR):
                if (floor.getInfo() == 0)
                    return diverseCrop[2];
                else
                    return diverseCrop[1];
            case (FloorConstants.FL_REPAIR):
                if (floor.getInfo() == 1)
                    return diverseCrop[4];
                else
                    return diverseCrop[5];
                // ------------------- normale Fliessbaender
                // -------------------------

            case (FloorConstants.FN1):
                return cbeltCrop[14];
            case (FloorConstants.FE1):
                return cbeltCrop[19];
            case (FloorConstants.FW1):
                return cbeltCrop[9];
            case (FloorConstants.FS1):
                return cbeltCrop[4];

            case (FloorConstants.NFW1):
                if (turner(xpos, ypos - 1, NORTH))
                    return cbeltCrop[15];
                else
                    return cbeltCrop[6];
            case (FloorConstants.NFE1):
                if (turner(xpos, ypos - 1, NORTH))
                    return cbeltCrop[18];
                else
                    return cbeltCrop[7];
            case (FloorConstants.SFW1):
                if (turner(xpos, ypos + 1, SOUTH))
                    return cbeltCrop[13];
                else
                    return cbeltCrop[3];
            case (FloorConstants.SFE1):
                if (turner(xpos, ypos + 1, SOUTH))
                    return cbeltCrop[10];
                else
                    return cbeltCrop[0];
            case (FloorConstants.EFN1):
                if (turner(xpos - 1, ypos, EAST))
                    return cbeltCrop[16];
                else
                    return cbeltCrop[5];
            case (FloorConstants.EFS1):
                if (turner(xpos - 1, ypos, EAST))
                    return cbeltCrop[12];
                else
                    return cbeltCrop[2];
            case (FloorConstants.WFN1):
                if (turner(xpos + 1, ypos, WEST))
                    return cbeltCrop[17];
                else
                    return cbeltCrop[8];
            case (FloorConstants.WFS1):
                if (turner(xpos + 1, ypos, WEST))
                    return cbeltCrop[11];
                else
                    return cbeltCrop[1];

            case (FloorConstants.NFEW1):
                return cbeltCrop[22];
            case (FloorConstants.SFWE1):
                return cbeltCrop[20];
            case (FloorConstants.EFNS1):
                return cbeltCrop[23];
            case (FloorConstants.WFNS1):
                return cbeltCrop[21];

                // ------------------------ Expressfliessbaender
                // ---------------------

            case (FloorConstants.FN2):
                return ebeltCrop[14];
            case (FloorConstants.FE2):
                return ebeltCrop[19];
            case (FloorConstants.FW2):
                return ebeltCrop[9];
            case (FloorConstants.FS2):
                return ebeltCrop[4];

            case (FloorConstants.NFW2):
                if (turner(xpos, ypos - 1, NORTH))
                    return ebeltCrop[16];
                else
                    return ebeltCrop[6];
            case (FloorConstants.NFE2):
                if (turner(xpos, ypos - 1, NORTH))
                    return ebeltCrop[17];
                else
                    return ebeltCrop[7];
            case (FloorConstants.SFW2):
                if (turner(xpos, ypos + 1, SOUTH))
                    return ebeltCrop[13];
                else
                    return ebeltCrop[3];
            case (FloorConstants.SFE2):
                if (turner(xpos, ypos + 1, SOUTH))
                    return ebeltCrop[10];
                else
                    return ebeltCrop[0];
            case (FloorConstants.EFN2):
                if (turner(xpos - 1, ypos, EAST))
                    return ebeltCrop[15];
                else
                    return ebeltCrop[5];
            case (FloorConstants.EFS2):
                if (turner(xpos - 1, ypos, EAST))
                    return ebeltCrop[12];
                else
                    return ebeltCrop[2];
            case (FloorConstants.WFN2):
                if (turner(xpos + 1, ypos, WEST))
                    return ebeltCrop[18];
                else
                    return ebeltCrop[8];
            case (FloorConstants.WFS2):
                if (turner(xpos + 1, ypos, WEST))
                    return ebeltCrop[11];
                else
                    return ebeltCrop[1];

            case (FloorConstants.NFWE2):
                return ebeltCrop[22];
            case (FloorConstants.SFWO2):
                return ebeltCrop[20];
            case (FloorConstants.EFNS2):
                return ebeltCrop[23];
            case (FloorConstants.WFNS2):
                return ebeltCrop[21];

            default:
                return null;
        }

    }

    private void paintFeldBoden(Graphics g, int xpos, int ypos, int actx, int acty, int width, int height) {

        // CAT.debug("xpos="+xpos+" ypos="+ypos+"actx="+actx+"acty="+acty);
        Location l = new Location(xpos, ypos);
        g.drawImage(floorElementHash.get(l), actx, acty, width, height, this);
        if (DEBUG_DISTANCE_CALC) {
            paintFeldBodenAutoBotDebug(g, xpos, ypos, actx, acty);
        }
    }

    // for painting crushers
    private static final int[] crushlb_x = { 20, 30, 30, 30, 40 };

    private static final int[] crushlb_y = { 35, 25, 35, 45, 35 };

    private void paintCrusher(Graphics2D g, Floor floor, int actx, int acty) {

        g.setComposite(AC_SRC_OVER);
        g.drawImage(diverseCrop[10], actx, acty, scaledFeldSize, scaledFeldSize, this);
        g.setColor(Color.white);
        for (int phasecount = 1; phasecount <= 5; phasecount++) {
            if (floor.isCrusherActive(phasecount)) {
                int strx = actx + crushlb_x[phasecount - 1];
                int stry = acty + crushlb_y[phasecount - 1];
                g.drawString("" + phasecount, strx, stry);
            }
        } // for
    }

    /**
     * To support debugging of the autobot distance calculation: toggle the flag
     * for which the distances should be shown
     */
    @SuppressWarnings("unused")
    // because debugbot is usually null
    protected void setDebugBotNextFlag(int flag) {
        if (!DEBUG_DISTANCE_CALC) {
            return;
        }
        if (debugbot == null) {
            debugbot = Bot.getNewInstance("debugDummy");
        }
        debugbot.setNextFlag(flag);
        synchronized (offScreenImgNotNullLock) {
            offScreenImage = null;
            System.gc();
            offScreenImage = createBoardImage();
        }
        repaint();
    }

    /**
     * Will paint the floor and add the values of the autobot distance
     * calculation on it for debugging purposes
     * */
    private void paintFeldBodenAutoBotDebug(Graphics g, int xpos, int ypos, int pixelx, int pixely) {
        if (calc == null && sf != null && sf.getFlags() != null) {

            calc = AdvDistanceCalculator.getInstance(sf);
            if (debugbot == null) {
                debugbot = Bot.getNewInstance("debugDummy");
            }
            debugbot.setNextFlag(1);
        }
        if (calc != null) {
            int x = pixelx + FELDSIZE / 2;
            int y = pixely + FELDSIZE / 2;
            g.setFont(myDebugFont);
            g.setColor(Color.red);
            debugbot.moveTo(xpos, ypos);
            debugbot.setDamage(0);
            debugbot.setFacing(Directions.NORTH);
            g.drawString("" + calc.getDistance(debugbot), x, y - 20);
            debugbot.setFacing(Directions.SOUTH);
            g.drawString("" + calc.getDistance(debugbot), x, y + 20);
            debugbot.setFacing(Directions.EAST);
            g.drawString("" + calc.getDistance(debugbot), x + 5, y);
            debugbot.setFacing(Directions.WEST);
            g.drawString("" + calc.getDistance(debugbot), x - 30, y);
        }
    }

    /** paints the (back-)ground of the board */
    private void paintSpielfeldBoden(Graphics g2) {

        Graphics2D g = (Graphics2D) g2;
        g.setComposite(AC_SRC_OVER);
        // Grenzen des zu zeichnenden Bereichs berechnen:

        int foo64 = scaledFeldSize;
        Rectangle clip = g.getClipBounds();
        int x0 = clip.x / foo64 + 1;
        int y0 = clip.y / foo64 + 1;
        int x1 = (clip.x + clip.width - 1) / foo64 + 1;
        int y1 = (clip.y + clip.height - 1) / foo64 + 1;
        x1 = Math.min(x1, sf.getSizeX());
        y1 = Math.min(y1, sf.getSizeY());

        for (int hori = x0; hori <= x1; hori++) {
            for (int vert = y0; vert <= y1; vert++) {
                int actx = (hori - 1) * foo64;
                int acty = (vert - 1) * foo64;
                int xpos = hori;
                int ypos = sf.getSizeY() + 1 - vert;
                Floor floor = sf.floor(xpos, ypos);

                paintFeldBoden(g, xpos, ypos, actx, acty);
                if ((floor.isBelt()) && (floor.getInfo() > 0))
                    paintCrusher(g, floor, actx, acty);
            }
        }
    }

    /** Paints the boardlaser-elements */
    private void paintLaserStrahlen(Graphics g) {
        Graphics2D dbg = (Graphics2D) g;
        AlphaComposite ac = null;
        // if (activeBordLasers)
        // ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f);
        // else
        ac = AC_SRC_OVER_05;
        dbg.setComposite(ac);

        int eleven = (int) (11 * dScale2ForBackground);
        int vier = (int) (4 * dScale2ForBackground);
        int dreissig = (int) (30 * dScale2ForBackground);

        dbg.setColor(fstLaserColor);
        for (LaserDef actuallaser : sf.getLasers()) {
            int lx = actuallaser.x - 1;
            int ly = sf.getSizeY() - actuallaser.y;
            int ll = actuallaser.length;
            int strength = actuallaser.strength;
            switch (actuallaser.facing) {
                case NORTH:
                    if (strength != 2) {
                        dbg.fillRect(lx * scaledFeldSize + dreissig, (ly - ll + 1) * scaledFeldSize, vier, ll
                                        * scaledFeldSize);
                    }
                    if (strength > 1) {
                        int offset = eleven * (strength - 1); // two lasers:
                                                              // paint them
                                                              // close(r)
                                                              // together, three
                                                              // lasers: move
                                                              // them to the
                                                              // outer edge
                        dbg.fillRect(lx * scaledFeldSize + dreissig - offset, (ly - ll + 1) * scaledFeldSize, vier, ll
                                        * scaledFeldSize);
                        dbg.fillRect(lx * scaledFeldSize + dreissig + offset, (ly - ll + 1) * scaledFeldSize, vier, ll
                                        * scaledFeldSize);
                    }
                    break;
                case SOUTH:
                    if (strength != 2) {
                        dbg.fillRect(lx * scaledFeldSize + dreissig, ly * scaledFeldSize, vier, ll * scaledFeldSize);
                    }
                    if (strength > 1) {
                        int offset = eleven * (strength - 1); // two lasers:
                                                              // paint them
                                                              // close(r)
                                                              // together, three
                                                              // lasers: move
                                                              // them to the
                                                              // outer edge
                        dbg.fillRect(lx * scaledFeldSize + dreissig - offset, ly * scaledFeldSize, vier, ll
                                        * scaledFeldSize);
                        dbg.fillRect(lx * scaledFeldSize + dreissig + offset, ly * scaledFeldSize, vier, ll
                                        * scaledFeldSize);
                    }
                    break;
                case EAST:
                    if (strength != 2) {
                        dbg.fillRect(lx * scaledFeldSize, ly * scaledFeldSize + dreissig, ll * scaledFeldSize, vier);
                    }
                    if (strength > 1) {
                        int offset = eleven * (strength - 1); // two lasers:
                                                              // paint them
                                                              // close(r)
                                                              // together, three
                                                              // lasers: move
                                                              // them to the
                                                              // outer edge
                        dbg.fillRect(lx * scaledFeldSize, ly * scaledFeldSize + dreissig - offset, ll * scaledFeldSize,
                                        vier);
                        dbg.fillRect(lx * scaledFeldSize, ly * scaledFeldSize + dreissig + offset, ll * scaledFeldSize,
                                        vier);
                    }
                    break;

                case WEST:
                    if (strength != 2) {
                        dbg.fillRect((lx - ll + 1) * scaledFeldSize, ly * scaledFeldSize + dreissig, ll
                                        * scaledFeldSize, vier);
                    }
                    if (strength > 1) {
                        int offset = eleven * (strength - 1); // two lasers:
                                                              // paint them
                                                              // close(r)
                                                              // together, three
                                                              // lasers: move
                                                              // them to the
                                                              // outer edge
                        dbg.fillRect((lx - ll + 1) * scaledFeldSize, ly * scaledFeldSize + dreissig - offset, ll
                                        * scaledFeldSize, vier);
                        dbg.fillRect((lx - ll + 1) * scaledFeldSize, ly * scaledFeldSize + dreissig + offset, ll
                                        * scaledFeldSize, vier);
                    }
                    break;
            }
        }
        dbg.setComposite(AC_SRC);
    }

    /**
     * Paints the wall(s) of a square field at position (xpos, ypos) on board
     * and (pixel-)position (actx, acty)
     */
    private void paintWall(Graphics g, int xpos, int ypos, int actx, int acty) {

        int vier = (int) (dScale2ForBackground * 4);
        int fuenf = (int) (dScale2ForBackground * 5);
        int sieben = (int) (dScale2ForBackground * 7);
        int sechs = (int) (dScale2ForBackground * 6);
        int neun20 = (int) (dScale2ForBackground * 29);
        int zwei40 = (int) (dScale2ForBackground * 42);
        int vier20 = (int) (dScale2ForBackground * 24);
        int sieben30 = (int) (dScale2ForBackground * 37);
        int zehn = (int) (dScale2ForBackground * 10);
        int elf = (int) (dScale2ForBackground * 11);
        // paint wall in the north, if any
        Wall northWall = sf.nw(xpos, ypos);
        if (northWall.isExisting()) {
            // is there a boardlaser to paint at this wall?
            if (northWall.getSouthDeviceType() == Wall.TYPE_LASER) {
                int strength = northWall.getSouthDeviceInfo();
                if (strength != 2) { // paint one in the middle
                    g.drawImage(diverseCrop[15], actx, acty + fuenf, scaledFeldSize, scaledFeldSize, this);
                }
                if (strength > 1) {
                    int offset = elf * (strength - 1); // two lasers: paint them
                                                       // close(r) together,
                                                       // three lasers: move
                                                       // them to the outer edge
                    g.drawImage(diverseCrop[15], actx - offset, acty + fuenf, scaledFeldSize, scaledFeldSize, this);
                    g.drawImage(diverseCrop[15], actx + offset, acty + fuenf, scaledFeldSize, scaledFeldSize, this);
                }
            }
            // is there a pusher?
            if (northWall.getSouthDeviceType() == Wall.TYPE_PUSHER) {
                g.drawImage(diverseCrop[7], actx - 1, acty + fuenf, scaledFeldSize, scaledFeldSize, this);
                // ------------draw text (phases when active) on pusher
                // --------------------
                for (int phasecount = 1; phasecount <= 5; phasecount++) {
                    if (northWall.isSouthPusherActive(phasecount)) {
                        int strx = actx + zehn * phasecount;
                        g.setColor((phasecount % 2) == 0 ? Color.black : Color.yellow);
                        g.drawString("" + phasecount, strx - 1, acty + neun20);
                    }
                }

            }
            g.drawImage(diverseCrop[13], actx, acty - sechs, scaledFeldSize, scaledFeldSize, this);
        }

        // paint wall in the south, if any
        Wall southWall = sf.sw(xpos, ypos);
        if (southWall.isExisting()) {
            if (southWall.getNorthDeviceType() == Wall.TYPE_LASER) {
                int strength = southWall.getNorthDeviceInfo();
                if (strength != 2) { // paint one in the middle
                    g.drawImage(diverseCrop[17], actx, acty - fuenf, scaledFeldSize, scaledFeldSize, this);
                }
                if (strength > 1) {
                    int offset = elf * (strength - 1); // two lasers: paint them
                                                       // close(r) together,
                                                       // three lasers: move
                                                       // them to the outer edge
                    g.drawImage(diverseCrop[17], actx - offset, acty - fuenf, scaledFeldSize, scaledFeldSize, this);
                    g.drawImage(diverseCrop[17], actx + offset, acty - fuenf, scaledFeldSize, scaledFeldSize, this);
                }
            }
            if (southWall.getNorthDeviceType() == Wall.TYPE_PUSHER) {
                g.drawImage(diverseCrop[8], actx, acty - fuenf, scaledFeldSize, scaledFeldSize, this);
                // -----------text on pusher--------------------
                for (int phasecount = 1; phasecount <= 5; phasecount++) {
                    if (southWall.isNorthPusherActive(phasecount)) {
                        int strx = actx + zehn * phasecount;
                        g.setColor((phasecount % 2) == 0 ? Color.black : Color.yellow);
                        g.drawString("" + phasecount, strx - 1, acty + zwei40);
                    }
                } // for
            }
            g.drawImage(diverseCrop[13], actx, acty + scaledFeldSize - sechs, scaledFeldSize, scaledFeldSize, this);
        }

        // paint wall in the east, if any
        Wall eastWall = sf.ew(xpos, ypos);
        if (eastWall.isExisting()) {
            if (eastWall.getWestDeviceType() == Wall.TYPE_LASER) {
                int strength = eastWall.getWestDeviceInfo();
                if (strength != 2) { // paint one in the middle
                    g.drawImage(diverseCrop[14], actx - sechs + 1, acty, scaledFeldSize, scaledFeldSize, this);
                }
                if (strength > 1) {
                    int offset = elf * (strength - 1); // two lasers: paint them
                                                       // close(r) together,
                                                       // three lasers: move
                                                       // them to the outer edge
                    g.drawImage(diverseCrop[14], actx - sechs + 1, acty - offset, scaledFeldSize, scaledFeldSize, this);
                    g.drawImage(diverseCrop[14], actx - sechs + 1, acty + offset, scaledFeldSize, scaledFeldSize, this);
                }
            }
            if (eastWall.getWestDeviceType() == Wall.TYPE_PUSHER) {
                g.drawImage(diverseCrop[6], actx - sechs, acty, scaledFeldSize, scaledFeldSize, this);
                // ------------text on pusher --------------------
                for (int phasecount = 1; phasecount <= 5; phasecount++) {
                    if (eastWall.isWestPusherActive(phasecount)) {
                        int stry = acty + zehn * phasecount;
                        g.setColor((phasecount % 2) == 0 ? Color.black : Color.yellow);
                        g.drawString("" + phasecount, actx + sieben30, stry + vier);
                    }
                } // for

            }
            g.drawImage(diverseCrop[12], actx + scaledFeldSize - sieben, acty, scaledFeldSize, scaledFeldSize, this);
        }

        // paint wall in the west, if any
        Wall westWall = sf.ww(xpos, ypos);
        if (westWall.isExisting()) {
            if (westWall.getEastDeviceType() == Wall.TYPE_LASER) {
                int strength = westWall.getEastDeviceInfo();
                if (strength != 2) { // paint one in the middle
                    g.drawImage(diverseCrop[16], actx + fuenf, acty, scaledFeldSize, scaledFeldSize, this);
                }
                if (strength > 1) {
                    int offset = elf * (strength - 1); // two lasers: paint them
                                                       // close(r) together,
                                                       // three lasers: move
                                                       // them to the outer edge
                    g.drawImage(diverseCrop[16], actx + fuenf, acty - offset, scaledFeldSize, scaledFeldSize, this);
                    g.drawImage(diverseCrop[16], actx + fuenf, acty + offset, scaledFeldSize, scaledFeldSize, this);
                }
            }
            if (westWall.getEastDeviceType() == Wall.TYPE_PUSHER) {
                g.drawImage(diverseCrop[9], actx + vier, acty, scaledFeldSize, scaledFeldSize, this);
                // ------------Beschriftung --------------------
                for (int phasecount = 1; phasecount <= 5; phasecount++) {
                    if (westWall.isEastPusherActive(phasecount)) {
                        int stry = acty + zehn * phasecount;
                        g.setColor((phasecount % 2) == 0 ? Color.black : Color.yellow);
                        g.drawString("" + phasecount, actx + vier20, stry + vier);
                    }
                } // for

            }
            g.drawImage(diverseCrop[12], actx - sieben, acty, scaledFeldSize, scaledFeldSize, this);
        }
    }

    private void paintWaende(Graphics g2) {

        Graphics2D g = (Graphics2D) g2;
        g.setComposite(AC_SRC_OVER);

        // Grenzen des zu zeichnenden Bereichs berechnen:
        Rectangle clip = g.getClipBounds();
        int x64 = scaledFeldSize;
        int x0 = clip.x / x64 + 1;
        int y0 = clip.y / x64 + 1;
        int x1 = (clip.x + clip.width - 1) / x64 + 1;
        int y1 = (clip.y + clip.height - 1) / x64 + 1;
        x1 = Math.min(x1, sf.getSizeX());
        y1 = Math.min(y1, sf.getSizeY());

        // Zeichnen
        for (int hori = x0; hori <= x1; hori++) {
            for (int vert = y0; vert <= y1; vert++) {
                int actx = hori * x64 - x64;
                int acty = vert * x64 - x64;
                int xpos = hori;
                int ypos = sf.getSizeY() - vert + 1;
                paintWall(g2, xpos, ypos, actx, acty);
            }
        }

    }

    private void paintFlaggen(Graphics g2) {

        Graphics2D g = (Graphics2D) g2;
        g.setComposite(AC_SRC_OVER);

        if (sf.getFlags() != null) {
            Location[] flaggen = sf.getFlags();
            for (int flaggencount = 0; flaggencount < flaggen.length; flaggencount++) {
                int xflagge = flaggen[flaggencount].x - 1;
                int yflagge = sf.getSizeY() - flaggen[flaggencount].y;
                g.drawImage(diverseCrop[18 + flaggencount], xflagge * scaledFeldSize, yflagge * scaledFeldSize,
                                scaledFeldSize, scaledFeldSize, this);
            }
        }
    }

    /** Berechnet zu einem Location das Rechteck, das die Kachel umschliesst */
    private Rectangle ort2Rect(Location ort) {
        return ort2Rect(ort.x, ort.y);
    }

    private Rectangle ort2Rect(int x, int y) {
        Rectangle dest = new Rectangle();
        int tmpFieldSize = scaledFeldSize;
        if (!usePrescaledBoardImage) {
            tmpFieldSize = (int) (dScale * FELDSIZE);
        }
        dest.x = (x - 1) * tmpFieldSize;
        dest.y = (sf.getSizeY() - y) * tmpFieldSize;
        dest.width = tmpFieldSize;
        dest.height = tmpFieldSize;
        return dest;
    }

    public Point ort2Point(Location ort, Point p) {
        return ort2Point(ort.x, ort.y, p);
    }

    public Location point2Ort(Point p, Location ort) {
        int tmpFieldSize = scaledFeldSize;
        if (!usePrescaledBoardImage) {
            tmpFieldSize = (int) (dScale * FELDSIZE);
        }
        ort.x = p.x / tmpFieldSize + 1;
        ort.y = (getHeight() - p.y) / tmpFieldSize + 1;
        return ort;
    }

    /** returns left upper point of square */
    public Point ort2Point(int ortx, int orty, Point p) {
        if (p == null) {
            p = new Point();
        }
        int tmpFieldSize = scaledFeldSize;
        if (!usePrescaledBoardImage) {

            tmpFieldSize = (int) (dScale * FELDSIZE);
        }
        p.x = (ortx - 1) * tmpFieldSize;
        p.y = (sf.getSizeY() - orty) * tmpFieldSize;
        return p;
    }

    /** returns left upper point of square */
    public Point ort2Point(int ortx, int orty) {
        return ort2Point(ortx, orty, null);
    }

    // private
    // for internal use. see repaintOrt()

    /**
     * Triggert ein Neuzeichnen des Feldes mit den \uFFFDbergebenen Koordinaten.
     * N\uFFFDtzlich um einzelne Felder neuzeichnen zu lassen
     */

    private void repaintOrt(Location ort) {
        Rectangle rcForOrt2Rect = ort2Rect(ort);
        repaint(1, rcForOrt2Rect.x, rcForOrt2Rect.y, rcForOrt2Rect.width, rcForOrt2Rect.height);
    }

    private void repaintOrt(int x, int y) {
        Rectangle rcForOrt2Rect = ort2Rect(x, y);
        repaint(1, rcForOrt2Rect.x, rcForOrt2Rect.y, rcForOrt2Rect.width, rcForOrt2Rect.height);
    }

    private void unhighlight() {
        highlightPos.x = 0;
        highlightPos.y = 0;
        repaint();
    }

    private final javax.swing.Timer t = new javax.swing.Timer(5000, new ActionListener() {
        public void actionPerformed(ActionEvent ae) {
            unhighlight();
        }
    });

    void highlight(int x, int y) {
        // remove old highlight:
        repaintOrt(highlightPos);

        if (CAT.isDebugEnabled())
            CAT.debug("highlighting 1 " + x + " " + y);
        highlightPos.x = x;
        highlightPos.y = y;

        // this.paintHighlight((Graphics2D)this.getGraphics());
        if (!t.isRunning())
            t.start();
        else
            t.restart();

        repaintOrt(x, y);

    }

    protected void setAnimationSettings(AnimationConfig current) {
        currentAnimationConfig = current;
    }

    private void showScout(Location ort) {
        deleteScout();
        repaintOrt(ort);
        lastScoutPos.set(ort);
    }

    private void deleteScout() {
        repaintOrt(lastScoutPos);
    }

    protected void paintScout(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        if (previewRob == null)
            return;

        int xpos = previewRob.getX() - 1;
        int ypos = sf.getSizeY() - previewRob.getY();
        int xpos64 = xpos * scaledFeldSize;
        int ypos64 = ypos * scaledFeldSize;
        // Scout
        AlphaComposite ac = AC_SRC_OVER_07;
        g2d.setComposite(ac);

        g2d.drawImage(scoutCrop[previewRob.getFacing()], xpos64, ypos64, scaledFeldSize, scaledFeldSize, this);
        repaint(xpos64, ypos64, scaledFeldSize, scaledFeldSize);
        g2d.setComposite(AC_SRC);
    }

    /*
     * private void paintFeldWithElements(Graphics2D g2d, int xpos, int ypos,
     * int actx, int acty) { Floor floor = sf.floor(xpos, ypos);
     * paintFeldBoden(g2d, xpos, ypos, actx, acty); if ((floor.isBelt()) &&
     * (floor.getInfo() > 0)) // restore possible Crusher paintCrusher(g2d,
     * floor, actx, acty); // TODO: only repaint the stuff on the field we want
     * to paint g2d.setComposite(AC_SRC_OVER); paintWall(g2d, xpos, ypos, actx,
     * acty); paintFlaggen(g2d); }
     */

    private void paintRobot(Graphics2D g2d, Bot robot, int robocount) {

        int xpos = robot.getX() - 1;
        int ypos = sf.getSizeY() - robot.getY();
        int xpos64 = xpos * scaledFeldSize;
        int ypos64 = ypos * scaledFeldSize;
        paintRobot(g2d, robot, robocount, xpos64, ypos64);
    }

    private void paintRobot(Graphics2D g2d, Bot robot, int robocount, int xpos64, int ypos64) {
        if (theseBotsShouldNotBePainted.contains(robot.getName())) {
            return;
        }
        int acht = (int) (dScale2ForBackground * 8);

        int botVis = robot.getBotVis();
        Image imgRob = robosCrop[robot.getFacing() + botVis * 4];
        boolean virtuell = robot.isVirtual();
        Composite oldComp = g2d.getComposite();
        if (imgRob != null) {
            if (virtuell) {
                g2d.setComposite(AC_SRC_OVER_05);
            }
            else {
                g2d.setComposite(AC_SRC_OVER);
            }
            g2d.drawImage(imgRob, xpos64, ypos64, scaledFeldSize, scaledFeldSize, this);
            // if (virtuell) {
            // g2d.setComposite(AC_SRC);
            // XXXHS }
            String beschriftung = "" + robot.getName();
            g2d.setColor(ROBOCOLOR[botVis]);
            // TODO pick AND SET a font for the names, otherwise we use whatever the last component has set..
            g2d.drawString(beschriftung, xpos64, ypos64 + acht + robocount * acht);
            g2d.setComposite(oldComp);
        }
    }

    private void paintRobos(Graphics g, Bot dontPaintMe) {
        Graphics2D g2d = (Graphics2D) g;
        // roboCounter will be used in paintRobot() as an offset for writing the
        // robot's name on the
        // screen so that the names won't be written over each other if some
        // (virtual) robots
        // have the same position on the board
        int roboCounter = 0;
        for (Bot robot : internalBotHash.values()) {
            if (!robot.equals(dontPaintMe) && !theseBotsShouldNotBePainted.contains(robot.getName())) {
                if ((robot.getDamage() < 10) && (robot.getLivesLeft() > 0)) {
                    paintRobot(g2d, robot, roboCounter);
                }
            }
            ++roboCounter;
        }
    }

    private final static Stroke[] hi = new Stroke[] { new BasicStroke(6), new BasicStroke(4), new BasicStroke(2),
            new BasicStroke(1) };

    private final static Color[] hiColOut = new Color[] { Color.red.darker().darker(), Color.red.darker(), Color.red,
            Color.red.brighter() };

    public final Color highCol1 = new Color(255, 0, 0, 255);

    public final Color highCol2 = new Color(255, 255, 0, 128);

    private void paintHighlight(Graphics2D g) {
        Rectangle rect = ort2Rect(highlightPos);
        rect.grow(-3, -3);
        for (int i = 0; i < hi.length; i++) {
            g.setColor(hiColOut[i]);
            g.setStroke(hi[i]);
            g.drawOval(rect.x, rect.y, rect.width, rect.height);
        }

        Paint p = new GradientPaint(rect.x, rect.y, highCol1, rect.x + rect.width, rect.y + rect.height, highCol2);
        g.setPaint(p);
        rect.grow(-1, -1);
        g.fillOval(rect.x, rect.y, rect.width, rect.height);
    }

    // private void createOffscreenImage() {
    // //vielleicht besser das skalieren erst beim reinkopieren
    // dbi = createImage(x,y);
    // g_off = (Graphics2D)dbi.getGraphics();
    // g_off.setFont(new
    // Font(g_off.getFont().getName(),g_off.getFont().getStyle(),8));
    // g_off.setClip(0,0,x,y);
    // g_off.scale( dScale, dScale );
    // }
    // private Graphics2D g_off;

    private BufferedImage offScreenImage;

    private BufferedImage staticBackground;

    private BufferedImage createBoardImage() {
        CAT.debug("createBoardImage called!");
        // preBoard = new BufferedImage(x,y, BufferedImage.TYPE_BYTE_INDEXED);
        BufferedImage bi = new ScaledBufferedImage(widthInPixel, heightInPixel, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g_off = (Graphics2D) bi.getGraphics();
        g_off.setClip(0, 0, widthInPixel, heightInPixel);

        // g_off.scale(dScale, dScale);

        paintUnbuffered(g_off);
        g_off.dispose();
        return bi;
    }

    /**
     * Dump this BoardView as a png image file.
     * 
     * @param file
     *            The file name to dump the image to.
     * @param size
     *            The width and hight of the generated image (square). Use
     *            size=0 for keeping the orginal size.
     * @throws IOException
     *             is thrown if the file cannot be created.
     */
    public void dumpPngImage(File file, int size) throws IOException {
        FileOutputStream fop = new FileOutputStream(file);
        Image image;
        if (size > 0) {
            image = getThumb(size);
        }
        else {
            image = createBoardImage();
        }
        fop.write((new PngEncoder(image)).pngEncode());
        fop.flush();
        fop.close();
    }

    /**
     * Dump this BoardView as a png image file.
     * 
     * @param file
     *            The file name to dump the image to.
     * @throws IOException
     *             is thrown if the file cannot be created.
     */
    public void dumpPngImage(File file) throws IOException {
        dumpPngImage(file, 0);
    }

    /**
     * During animation the of a bot move or turn we save a reference of the bot
     * that is animated. This reference is used to avoid the robot being painted
     * twice: once by the animation, once during paintRobos() int the
     * paintComponent method. During an animation, paintComponent has to ignore
     * this bot.
     */
    private Bot currentlyAnimated = null;

    public void paintComponent(Graphics g) {

        Graphics2D dbg = (Graphics2D) g;
        // hs_scale
        if (!usePrescaledBoardImage) {
            dbg.scale(dScale, dScale);
        }
        // System.out.println("OLD="+oldClip+"\tNEW="+rect);
        // g.setClip(rect);
        if (useStaticBg) { // 100% doublebuffered
            Rectangle oldClip = g.getClipBounds();
            Graphics2D offG;
            synchronized (offScreenImgNotNullLock) {
                offG = (Graphics2D) offScreenImage.getGraphics();
            }
            offG.setClip(oldClip);
            offG.drawImage(staticBackground, 0, 0, widthInPixel, heightInPixel, this);
            // draw the active elements (robos)
            paintHighlight(offG);
            paintScout(offG);

            paintRobos(offG, null);

        }

        synchronized (offScreenImgNotNullLock) {
            // BufferedImage clip = offScreenImage.getSubimage(oldClip.x,
            // oldClip.y,oldClip.width, oldClip.height);
            dbg.drawImage(offScreenImage, 0, 0, this);
        }
        // draw the active elements (robos)
        if (!useStaticBg) { // the active elements must not be painted on the
                            // offscreen image in this case;
                            // reason: we need it as a source for "clean"
                            // background during animations
                            // (animations will still be doublebuffered, but we
                            // will clean the offscreenimage
                            // when the animation is finished)

            paintHighlight(dbg);
            paintScout(dbg);
            paintRobos(dbg, currentlyAnimated);
            dbg.setComposite(AC_SRC);
        }

        // g.setClip(oldClip);
    }

    protected void paintUnbuffered(Graphics dbg) {
        paintSpielfeldBoden(dbg);
        paintLaserStrahlen(dbg);
        paintWaende(dbg);
        paintFlaggen(dbg);
        // paintScout(dbg);
    }

    @SuppressWarnings("unused")
    // for the static background bits
    protected void finalize() throws Throwable {
        super.finalize();
        synchronized (offScreenImgNotNullLock) {
            if (offScreenImage != null) {
                Graphics g = offScreenImage.getGraphics();
                g.dispose();
            }
            if (useStaticBg && staticBackground != null) {
                Graphics g = staticBackground.getGraphics();
                g.dispose();
            }
        }
    }

    /*
     * public void update(Graphics g) { paint(g); }
     */
    protected Location[] getFlags() {
        return sf.getFlags();
    }

    public Image getThumb(int size) {
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setClip(0, 0, size, size);
        g2.scale(((double) size) / widthInPixel, ((double) size) / heightInPixel);
        paintUnbuffered(g2);
        g2.dispose();

        return bi;
    }

    private synchronized void ersetzeSpielfeld(SimBoard sfs) {
        sf = sfs;
        widthInPixel = sf.getSizeX() * scaledFeldSize;
        heightInPixel = sf.getSizeY() * scaledFeldSize;
        setSize(widthInPixel, heightInPixel);
        initFloorHashMap();
    }

    // Little helper for getting thumbnails of the board
    private static BoardView sac = null;

    public static Image createThumb(SimBoard sim, int size) {
        if (sac == null) {
            sac = new BoardView(sim);
        }
        else {
            sac.ersetzeSpielfeld(sim);
        }
        return sac.getThumb(size);
    }

    // private Object phaseDisplayLock = new Object();
    private void waitForPhaseDisplay() {
        /*
         * synchronized (phaseDisplayLock){ while (isPhaseNumberToBePainted){
         * try { phaseDisplayLock.wait(); } catch (InterruptedException ie){
         * CAT.warn(ie.getMessage(), ie); } phaseDisplayLock.notifyAll(); }
         * 
         * }
         */
    }

    private void waitSomeTime(int ms, Object lock) {
        if (ms == 0) {
            CAT.warn("waitSomeTime was asked to wait " + ms
                            + "ms; ignoring this request as it means to wait infinite time..");
            return;
        }
        else
            if (ms < 0) {
                CAT.error("was asked to wait a negative number of milliseconds: " + ms);
                return;
            }
        synchronized (lock) {
            try {
                lock.wait(ms);
            }
            catch (InterruptedException ie) {
                CAT.error(ie);
            }
        }

        /*
         * try { Thread.sleep(ms); } catch (InterruptedException ie){
         * CAT.error("Interrupted while waiting: "+ie.getMessage(), ie); }
         */
    }

    private Graphics2D getScaledGraphics() {
        Graphics2D g2 = (Graphics2D) super.getGraphics();
        if (!usePrescaledBoardImage) {
            g2.scale(dScale, dScale);

        }
        return g2;
    }

    class ScaledBufferedImage extends BufferedImage {

        public ScaledBufferedImage(int width, int height, int img_type) {
            super(width, height, img_type);
        }

        public Graphics getGraphics() {
            Graphics2D g2 = (Graphics2D) super.getGraphics();
            // g2.scale(dScale, dScale);
            return g2;
        }

    }
}
