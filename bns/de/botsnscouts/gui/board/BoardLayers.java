/*
 * Created on 19.10.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package de.botsnscouts.gui.board;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JLayeredPane;

import org.apache.log4j.Category;

import com.keypoint.PngEncoder;

import de.botsnscouts.board.SimBoard;
import de.botsnscouts.util.ImageMan;
import de.botsnscouts.util.Location;

/**
 * @author hendrik
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class BoardLayers extends JLayeredPane implements DrawingConstants, Scalable{

    private static Category CAT = Category.getInstance(BoardLayers.class);
    
    private BotCanvas botCanvas;
    private FloorCanvas floorCanvas;
    private ElementCanvas elementCanvas;
    
    private TopCanvas topCanvas;
    private AnimationCanvas animationCanvas;
    
    /** some board elements..*/
    private Image[] cbeltCrop,ebeltCrop,miscCrop,robosCrop,scoutCrop;
    
    private SimBoard gameboard;
    
    /** scale factor for zooming*/
    private double dScale = 1.0;
  

   private  double scaledFeldSize; // FELDSIZE * scale
   private int scaledFieldWithInPixels;
   
   private int scaledFieldHeightInPixels;
  
   
    
    private Scalable [] layers;
    private int layerCount=0;



    
   
    
    
    public BoardLayers (SimBoard sf_neu, Color[] robColors) {
        init(sf_neu, robColors);
       // mouseInit();
    }
    
    public BoardLayers(SimBoard gameboard){
        this.gameboard = gameboard;
        init (gameboard, ROBOCOLOR);
    }
    
    private void init(SimBoard sf_neu, Color[] robColors) {
        // XXX HS activeBordLasers = false; -> elementCanvas(?)
        // XXX HS gotColors = false; -> botCanvas
        gameboard = sf_neu;

        setDoubleBuffered(true);
        setScale(dScale); // does call setSize()

        ImageMan.finishLoading();

        ebeltCrop = ImageMan.getImages(ImageMan.EBELTS);
        cbeltCrop = ImageMan.getImages(ImageMan.CBELTS);
        miscCrop = ImageMan.getImages(ImageMan.DIVERSE);
        robosCrop = ImageMan.getImages(ImageMan.ROBOS);
        scoutCrop = ImageMan.getImages(ImageMan.SCOUT);

        floorCanvas = new FloorCanvas(gameboard, cbeltCrop, ebeltCrop, miscCrop);
        elementCanvas = new ElementCanvas(gameboard, miscCrop);
        botCanvas = new BotCanvas(gameboard, robosCrop); 
        animationCanvas = new AnimationCanvas(gameboard, botCanvas, robosCrop);
        topCanvas = new TopCanvas(gameboard, scoutCrop);
     
        layers = new Scalable[]{
                        floorCanvas, elementCanvas,botCanvas, animationCanvas, topCanvas
        				};        
        layerCount = layers.length;
    
        
    }
    
    
    public synchronized void setScale(double scale) {
        // adapt this Component to the scaling factor
        dScale = scale;
        Dimension dim = calcBoardDimensionInPixel(dScale, gameboard);
        // XXX HS rescaled = true;
       this.scaledFieldWithInPixels = (int) dim.getWidth();
       this.scaledFieldHeightInPixels = (int) dim.getHeight();
       
       
       
       for (int i=0; i<layerCount;i++){
           Scalable s = layers[i];
           s.setScale(dScale);
           s.setSize(dim);
       }
       
       Graphics2D g2 = (Graphics2D)getGraphics();
       g2.scale(dScale, dScale);
       setSize(dim);                      
       
    }
    
    
    protected static Dimension calcBoardDimensionInPixel(double dscale, SimBoard board){
        double scaledFeldSize = (dscale * FIELDSIZE_IN_PIXELS);
        int scaledWidthPixels = (int) (board.getSizeX() * scaledFeldSize);
        int scaledHeightPixels = (int) (board.getSizeY() * scaledFeldSize);
        return new Dimension(scaledWidthPixels, scaledHeightPixels);
    }
    
    protected Point calcKachelPos(int mx, int my) {
        int sfh = gameboard.getSizeY();
        int sfw = gameboard.getSizeX();

        Point p = new Point();
        p.x = 1 + (int) (mx / scaledFeldSize);
        p.y = sfh - (int) (my / scaledFeldSize);

        // assure that 1 <= p.x <= sfw
        // and 1 <= p.y <= sfy

        p.x = Math.min(Math.max(1, p.x), sfw);
        p.y = Math.min(Math.max(1, p.y), sfh);
        return p;
    }
 
    protected void paintUnbuffered(Graphics dbg) {
        floorCanvas.paintSpielfeldBoden(dbg);
        elementCanvas.paintUnbuffered(dbg);
      //  paintScout(dbg);->TopCanvas/BoardCanvas
    }
    
    
    // Little helper for getting thumbnails of the board
    private static BoardLayers sac = null;
    public static Image createThumb(SimBoard sim, int size) {
        if (sac == null) {
            sac = new BoardLayers(sim);
        } else {
            sac.floorCanvas.replaceGameboard(sim);
            sac.elementCanvas.replaceGameboard(sim);
            
          
        }
        return sac.getThumb(size);
    }
 
    public Image getThumb(int size) {
        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bi.createGraphics();
        g2.setClip(0, 0, size, size);
        g2.scale(((double) size) / scaledFieldWithInPixels, ((double) size) / scaledFieldHeightInPixels);
        paintUnbuffered(g2);
        g2.dispose();

        return bi;
    }
    
    public void update(Graphics g) {
        paint(g);
    }
    
    
    /**
     * Dump this BoardView as a png image file.
     * @param file The file name to dump the image to.
     * @param size The width and hight of the generated image (square).
     *             Use size=0 for keeping the orginal size.
     * @throws IOException is thrown if the file cannot be created.
     */
    public void dumpPngImage(File file, int size) throws IOException {
        FileOutputStream fop = new FileOutputStream(file);
        Image image;
        if (size > 0) {
            image = getThumb(size);
        } else {
            image = getBoardImage();
        }
        fop.write((new PngEncoder(image)).pngEncode());
        fop.flush();
        fop.close();
    }

    /**
     * Dump this BoardView as a png image file.
     * @param file The file name to dump the image to.
     * @throws IOException is thrown if the file cannot be created.
     */
    public void dumpPngImage(File file) throws IOException {
        dumpPngImage(file, 0);
    }
    
    private BufferedImage getBoardImage() {
        //preBoard = new BufferedImage(x,y, BufferedImage.TYPE_BYTE_INDEXED);
        BufferedImage bi = new BufferedImage(scaledFieldWithInPixels, scaledFieldHeightInPixels, BufferedImage.TYPE_INT_RGB);
        Graphics2D g_off = (Graphics2D) bi.getGraphics();
        g_off.setClip(0, 0, scaledFieldWithInPixels, scaledFieldHeightInPixels);
        g_off.scale(dScale, dScale);
        paintUnbuffered(g_off);        
        
        g_off.dispose();
        return bi;
    }
    public Dimension getMinimumSize() {
        return new Dimension(scaledFieldWithInPixels, scaledFieldHeightInPixels);
    }

    public Dimension getPreferredSize() {
        return new Dimension(scaledFieldWithInPixels, scaledFieldHeightInPixels);
    }
    
    protected static  BufferedImage getBlankImage(double dScale, SimBoard gameboard){
        Dimension dim = BoardLayers.calcBoardDimensionInPixel(dScale,gameboard);
        int width = (int) dim.getWidth();
        int height = (int) dim.getHeight();
        BufferedImage bi = new BufferedImage(width, height,  BufferedImage.TYPE_INT_RGB);
        Graphics2D g_off = (Graphics2D) bi.getGraphics();
        g_off.setClip(0, 0, width,height);
        g_off.scale(dScale, dScale);
        // XXX g_off.dispose() ???
        return bi;
    }
    public Location point2Ort(Point p, Location ort) {
        ort.x = (int) (p.x / scaledFeldSize) + 1;
        ort.y = (int) ((getHeight() - p.y) / scaledFeldSize) + 1;
        return ort;
    }

    /** returns left upper point of square*/
    public Point ort2Point(int ortx, int orty, Point p) {
        p.x = (int) ((ortx - 1) * scaledFeldSize);
        p.y = (int) ((gameboard.getSizeY() - orty) * scaledFeldSize);
        return p;
    }

    public Point ort2Point(Location ort, Point p) {
        return ort2Point(ort.x, ort.y, p);
    }
    
 /*
    private ClickListener myClickListener;
 
    public void addClickListener(ClickListener listener) {
        myClickListener = listener;
    }
    
    
    
    private void mouseInit() {
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                Point feld = calcKachelPos(me.getX(), me.getY());
                if (myClickListener != null) {
                    myClickListener.feldClicked(feld.x, feld.y, me.getModifiers());
                }
*/
                /*
                int mods = me.getModifiers();
                 if( (mods & MouseEvent.BUTTON3_MASK) == 0 )
                     return;

                Dimension sz = myScrollPane.getViewport().getExtentSize();
                int w2 = sz.width/2;
                int h2 = sz.height/2;

                //make sure we dont want to scoll 'out' to
                // the left and top
                int x1 = Math.max( me.getX() - w2 , 0);
                int y1 = Math.max( me.getY() - h2 , 0);

                // ... and right and bottom
                x1 = Math.min( x1, x - sz.width );
                y1 = Math.min( y1, y - sz.height );

                myScrollPane.getViewport().setViewPosition(new Point(x1, y1));
                */
/*
            }
        });
    }
  */
    
}
