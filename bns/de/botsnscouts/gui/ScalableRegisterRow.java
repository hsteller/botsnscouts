/*
 * Created on 10.06.2005
 *
 */
package de.botsnscouts.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Category;



import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Card;
import de.botsnscouts.util.Global;
import de.botsnscouts.util.ImageMan;

/**
 * @author Hendrik Steller
 * @version $Id$
 */
public class ScalableRegisterRow extends JPanel {

    Category CAT = Category.getInstance(ScalableRegisterRow.class);
    
    private double scale = 1;
    private boolean isVertical = false;
    
    private ScalableRegView [] phases = new ScalableRegView[Bot.NUM_REG];
    
    public ScalableRegisterRow(){
        this(1,false, 0);
    }
    
    public ScalableRegisterRow(double scale){
        this (scale, false, 0);
    }
    
    public ScalableRegisterRow(double scale, boolean isVertical, int gap){
        super();
        this.isVertical = isVertical;
        this.scale = scale;
        int phaseCount = phases.length;
        this.setOpaque(true);
       
        if (this.isVertical){
            GridLayout lay = new  GridLayout(phaseCount,1);
            lay.setHgap(gap);
            this.setLayout(lay);           
        }
        else {
            GridLayout lay = new  GridLayout(1,phaseCount);
            lay.setHgap(gap);
            this.setLayout(lay);            
        }
        for (int i=0;i<phaseCount;i++){
            ScalableRegView rv = new ScalableRegView(scale); 
            phases[i]  = rv;
            rv.setScale(scale);
            rv.setVisible(true);           
            add(rv,i);
        }           
       
    }
    
    public boolean isVertical(){
        return this.isVertical;
    }
    
    public void updateCardsByRobot(Bot r){
        if (CAT.isDebugEnabled()) {
            CAT.debug("UPDATE FOR BOT:\n"+r);            		
        }
        Card [] foo = r.getMove();
      
        if (foo == null){
            emptyAll();
            return;
        }        
        HumanCard [] all = new HumanCard[foo.length];
        for (int i=0;i<foo.length;i++){
            Card card = foo[i];
            if (card != null) { 
                all[i] = new HumanCard(card);
                if (r.getLockedRegister(i)!=null) {
                    all[i].setState(HumanCard.LOCKED);
                }
            }
            else {
                all[i]=null;
            }
            
        }
        setCards(all);
    }
    
    
    
    public void alwayshowCardBackInsteadOfEmpty(boolean showBackside){
        for (int i=0;i<phases.length;i++){
            phases[i].alwayshowCardBackInsteadOfEmpty(showBackside);
        }
    }
    
    /**
     * To toggle which side of a card is shown: front or backside
     *  
     * @param phase phase the register number/number of the evaluation phase (1-5)
     * @param showFront
     */
    public void setCardVisibility (int phase, boolean showFront){
        phases[phase-1].setHidden(!showFront);
    }
    
    public void setCardVisibilityUntilPhase (int lastAffectedPhase, boolean showFront){
        
        for (int i=0;i<lastAffectedPhase;i++){
            phases[i].setHidden(!showFront);
        }
    }
    
    
    /**
    * Remove a card from a certain register/phase
    *  
    * @param phase phase the register number/ number of the evaluation phase (1-5)
   */
   public void setEmpty(int phase){
       phases[phase-1].setEmpty();
   }
   
   public void emptyAll(){
       for (int i=0;i<phases.length;i++){
           phases[i].setEmpty();
       }
   }
   
   /** Shows the back side of every card; shouldn't affect locked registers*/
   public void hideAll(){
       for (int i=0;i<phases.length;i++){
           phases[i].setHidden(true);
       }
   }
   
   /**
    * Set the card for a  certain register/phase 
    *  
    * @param phase phase the register number/number of the evaluation phase (1-5)
   */
   public void setCard(int phase, HumanCard card){
       phases[phase-1].setCard(card);
   }
   
   /* (non-Javadoc)
 * @see javax.swing.JComponent#getPreferredSize()
 *//*
public Dimension getPreferredSize() {
   Dimension d = phases[0].getPreferredSize();
   int w = 5*d.width;
   int h = d.height;
    return new Dimension (w,h);
}
*/

      
   /**
    * Get the card for a  certain register/phase 
    *  
    * @param phase phase the register number/number of the evaluation phase (1-5)
    * @return the ScalableRegView object for the requested phase/register
   */
   protected  ScalableRegView getRegView(int phase){
       return phases[phase-1];
   }

   public void setCards(HumanCard[] cards){
       if (CAT.isDebugEnabled()){
           CAT.debug("setCards:");
           //CAT.debug(Global.arrayToString(cards));
       }
       int length = cards!=null?cards.length:0;
       int i=0;
       for (;i<length;i++){                   
           phases[i].setCard(cards[i]);
       }
       for (;i<phases.length;i++){
           phases[i].setEmpty();           
       }
      repaint();
   }
   
   public double getScale(){
       return scale;
   }
   
   public void setScale(double scale) {
       this.scale = scale;
       for (int i=0;i<phases.length;i++){
           phases[i].setScale(scale);
       }
       super.validate();
   }
   
   public String toString(){
       return Global.arrayToString(phases); 
   }

   
  
    
    public static void main(String[] args) {
	        JFrame fr = new JFrame("RegRowTest");
	        fr.addWindowListener(new WindowAdapter(){
	            public void windowClosing(WindowEvent e){
	                System.exit(0);
	            }
	        });
	        ImageMan.finishLoading();
        	JPanel foo = new JPanel();
        	foo.setBackground(Color.RED);
        	foo.setSize(250,700);
        	foo.setLayout(new BorderLayout());
        
        	final ScalableRegisterRow row = new ScalableRegisterRow(0.5);
        	HumanCard [] cards = new HumanCard[] {
        	                new HumanCard(1,Card.ACTION_MOVE1),
        	                new HumanCard(2,Card.ACTION_MOVE2),
        	                new HumanCard(3,Card.ACTION_MOVE3),
        	                new HumanCard(4,Card.ACTION_UTURN),
        	                new HumanCard(5,Card.ACTION_ROTATE_L)
        	};
        	row.setCards(cards);
        	final ScalableRegisterRow row2 = new ScalableRegisterRow(0.8);
        	row2.setCards(cards);
        	JPanel bar = new JPanel();
        	bar.setLayout(new GridLayout(2,1));
        	bar.add(row, 0);
        	bar.add(row2,1);
        	
        	foo.add(bar, BorderLayout.CENTER);
        	JButton inc = new JButton("+");
        	inc.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    double s = row.getScale()+0.1;
                    System.out.print("setting scale to "+s);
                    row.setScale(s);
                    row2.setScale(s);
                    System.out.println(": prefSize="+row.getPreferredSize());
                }
            });
        	JButton dec = new JButton("-");
        	dec.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    double s = row.getScale()-0.1;
                    System.out.print("setting scale to "+s);
                    row.setScale(s);
                    row2.setScale(s);
                    System.out.println(": prefSize="+row.getPreferredSize());
                }
            });
        	final JButton show = new JButton("show next (1)");
        	show.addActionListener(new ActionListener() {
                int phase = 0;
        	    public void actionPerformed(ActionEvent e) {                    
                    row.setCardVisibility(phase+1,true);
                    row2.setCardVisibility(phase+1, true);
                    phase=(phase+1)%5;
                    show.setText("show next("+(phase+1)+")");
                }
            });
        	
        	JButton hide = new JButton("hide cards");
        	hide.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    row.hideAll();
                    row2.hideAll();
                }
            });
        	
        	foo.add(hide, BorderLayout.WEST);
        	foo.add(show,BorderLayout.EAST);
        	foo.add(inc, BorderLayout.NORTH);
        	foo.add(dec,BorderLayout.SOUTH);
        	fr.getContentPane().add(foo);
        	fr.setSize(450,700);
        	fr.show();
    }
}
