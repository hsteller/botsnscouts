/*
 * Created on 09.06.2005
 *
 */
package de.botsnscouts.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Category;

import de.botsnscouts.util.ImageMan;

/**
 * @author Hendrik Steller
 * @version $Id$
 */
public class ScalableRegView extends JComponent {
    Category CAT = Category.getInstance(ScalableRegView.class);
    
    private static final Color EMPTY_BG_COLOR = RegisterView.BG_COLOR;
    private static final Color PRIORITY_FONT_COLOR = RegisterView.prioColor;
    private static final Font  PRIORITY_FONT = RegisterView.prioFont;
    private static final Color HIGHLIGHT_COLOR = Color.RED.brighter();
    
    private HumanCard myCard;
    private boolean isCardHidden = true;
    private double scale = 1;
    private int bgWidth, bgHeight;
    private boolean isHighlited = false;
    
    
    private Image emptyBgImg;
    private Image cardBackSideImg;
    private Image lockImg;
    
    private boolean showBackSideInsteadOfEmpty = false;
    
    public ScalableRegView(double scale){
        this.scale =  scale;
        if (emptyBgImg == null) {
            emptyBgImg = ImageMan.CardRLEER.getImage(); 
            bgWidth = emptyBgImg.getWidth(this);
            bgHeight = emptyBgImg.getHeight(this);
        }
        if (cardBackSideImg== null) {
            cardBackSideImg = ImageMan.CardRUECK.getImage();           
        }
        if (lockImg == null){
            lockImg = ImageMan.getImage(ImageMan.PNG_REGLOCK);
        }
        isCardHidden = true;
     
    }

    public int getImageWidth(){
        return bgWidth;
    }
    
    public int getImageHeight(){
        return bgHeight;
    }
    
    /**
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        int w = ((int) (scale*bgWidth))+1;
        int h = ((int) (scale*bgHeight))+1;
        return new Dimension(w,h);
    }
    
//   public Dimension getMinimumSize(){
 //       return getPreferredSize();
 //   }
   
    
   public boolean isCardBackInsteadOfEmptyShown(){
       return showBackSideInsteadOfEmpty;
   }
    
    public void alwayshowCardBackInsteadOfEmpty(boolean showBackside){
        showBackSideInsteadOfEmpty = showBackside;
    }
    
    public void setCard(HumanCard hc){    
        CAT.debug(hc);
        myCard = hc;
        repaint();
    }
    
    public void setScale(double scale){
        if (this.scale == scale){
            return;
        }
        this.scale = scale;
        super.setPreferredSize(getPreferredSize());
        super.setMinimumSize(getPreferredSize());
        repaint();
    }
    public double getScale(){
        return scale;
    }
    
    private static final AlphaComposite AC_SRC_OVER_05 = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
    public void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D)g;               
        g2.scale(scale,scale);                                      
        if (myCard == null  ){
            g.setColor( EMPTY_BG_COLOR );
            g.fillRect(0,0,bgWidth,bgHeight);            
            if (!showBackSideInsteadOfEmpty) {
                g2.drawImage(emptyBgImg,0,0,bgWidth,bgHeight,this);
            }
            else {
                g2.drawImage( cardBackSideImg,0,0,bgWidth,bgHeight,this); 
            }
            return;
        }

        if (/*isCardHidden ||*/ !isCardHidden || myCard.locked()) {
           
            Image img = myCard.getImage();//ImageMan.getCardImage(myCard.getAction());//
            g2.drawImage(img,0,0,bgWidth,bgHeight,this);
            g.setFont(PRIORITY_FONT);
        	g.setColor(PRIORITY_FONT_COLOR);
        
        	int prio = myCard.getprio();
        	if (prio>0) { 
        	    g.drawString(""+10*prio,26,17);
        	}
            if (myCard.locked()){
                g2.drawImage( lockImg, 4,3,this);
            }
            if (isHighlited){
                g2.setColor(HIGHLIGHT_COLOR);
                
                g2.setComposite(AC_SRC_OVER_05);
                g2.fillRect(0,0,62,102);
            }
            
        }
        else {
            g2.drawImage( cardBackSideImg,0,0,bgWidth,bgHeight,this);          
        }                                                 
        
    }
    public void setHidden(boolean showCardsBackside) {
        setHidden(showCardsBackside, true);
    }
   
    public void setHidden(boolean showCardsBackside, boolean callRepaint) {
        isCardHidden = showCardsBackside;
        if (callRepaint) {
            repaint();
        }
    }
    
    public void setEmpty(){
        myCard = null;
        isCardHidden = true;
        repaint();
    }
    
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("card: ").append(myCard).append(";hidden=").append(isCardHidden)
        	.append(";scaleFactor="+scale);
        return sb.toString();
    }
    
    public boolean isHighLighted(){
        return isHighlited;
    }
    
    public void setHighLighted(boolean isHighLighted ){
        this.isHighlited = isHighLighted;
        repaint();
    }
    
    public static void main(String[] args) {
        JFrame fr = new JFrame("RegViewTest");
        fr.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        });
        ImageMan.finishLoading();
        final HumanCard card = new HumanCard( 1, "M1" );
        final ScalableRegView rv = new ScalableRegView(1);
        rv.setCard(card);
        System.out.println(rv.getWidth()+","+rv.getHeight());
        JPanel foo =new JPanel();
        foo.setBackground(Color.BLUE);
        foo.setLayout(new BorderLayout());
        foo.add(rv, BorderLayout.CENTER);
        JButton hide = new JButton("hide");
        rv.setScale(1.0);
        hide.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rv.setHidden(true);
            }
        });
        JButton hide2 = new JButton("empty");
        hide2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (rv.myCard != null){
                    rv.setEmpty();
                }
                else {
                    rv.myCard = card;
                    rv.repaint();
                }
            }
        });
        JButton hide3 = new JButton("show");
        hide3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                rv.setHidden(false);
            }
        });
        JButton hide4 = new JButton("lock");
        hide4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (rv.myCard != null) {
                    rv.myCard.setState(HumanCard.LOCKED);
                    rv.repaint();
                }
            }
        });
        foo.add(hide,BorderLayout.NORTH);
        foo.add(hide2,BorderLayout.EAST);
        foo.add(hide3,BorderLayout.SOUTH);
        foo.add(hide4,BorderLayout.WEST);
        System.out.println(rv.getWidth()+","+rv.getHeight());
        fr.getContentPane().add(foo);
        System.out.println(rv.getWidth()+","+rv.getHeight());
        fr.setSize(250,600);
        System.out.println(rv.getWidth()+","+rv.getHeight());
        fr.show();
        System.out.println(rv.getWidth()+","+rv.getHeight());               
    }
}
