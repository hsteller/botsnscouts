/*
 * Created on 10.06.2005
 *
 */
package de.botsnscouts.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.border.EtchedBorder;

import de.botsnscouts.util.Card;
import de.botsnscouts.util.ImageMan;

/**
 * @author Hendrik Steller
 * @version $Id$
 */
public class RegisterToolTip extends JToolTip {

    private Dimension prefSize;
    private JLabel text;
    private ScalableRegisterRow myrow;
    
    public RegisterToolTip (ScalableRegisterRow row) {
       super(); 
    myrow = row;
       text = new JLabel();
      text.setAlignmentY(JLabel.TOP_ALIGNMENT);
      text.setAlignmentX(JLabel.LEFT_ALIGNMENT); 
      row.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

      
       prefSize = calcPrefSize(row);
     
       this.removeAll();
       
       
       this.setLayout(new GridBagLayout());
       GridBagConstraints gc = new GridBagConstraints();
       gc.gridx = 0;
       gc.gridy = 0;
       this.add(text, gc);
       gc.gridy=1;
       gc.anchor = GridBagConstraints.CENTER;
       gc.fill = GridBagConstraints.BOTH;       
       this.add(row,gc);
       
       //setLayout(new GridLayout(2,1));
       //this.add(text,0);                   
     //  this.add(row,1)
       ;
       this.revalidate();
    
    }
    
    private Dimension calcPrefSize (ScalableRegisterRow row) {
     /*   ScalableRegView rv = row.getRegView(1);    
        double scale = row.getScale();
        int w = rv.getImageWidth();
        int h = rv.getImageHeight();
        int prefW,prefH;
        if (!row.isVertical()){                 
             prefW = (int) (w*scale*5)+40;
             prefH = (int) (h*scale)+30;
        }
        else {
            prefW = (int) (w*scale)+30;
            prefH = (int) (h*scale*5)+50;
        }
        
        */
        Dimension rs = row.getPreferredSize();
        Dimension ts = text.getPreferredSize();
        int w = Math.max(rs.width, ts.width)+10;
        int h = rs.height+ts.height;
        return new Dimension(w,h);
    }
    
    public void setTheText(String t){
        text.setText(t);
        prefSize = calcPrefSize(myrow);
        super.setPreferredSize(prefSize);
    }
    
    /**
     * @see javax.swing.JToolTip#setTipText(java.lang.String)
     *
     */
   /*
    public void setTipText(String tipText) {
       // text.setText(tipText!=null?tipText:"");
        super.setTipText(tipText); // to fire property change event
        repaint();
       
    }
   */
    /**
     * @see javax.swing.JToolTip#getTipText()
     */
    /*
   public String getTipText() {
        if (text!=null) {
            return text.getText();
        }
        else {
            return super.getTipText();
        }
    }
    */
    /* (non-Javadoc)
     * @see javax.swing.JComponent#setToolTipText(java.lang.String)
     */
    
    public void setToolTipText(String text) {
      
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#getToolTipText()
     */
   /* public String getToolTipText() {
        return null;
    }
  */  
    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        
        return prefSize;
    }
    
    
    public static void main(String[] args) {
        JFrame fr = new JFrame("ToolTipTest");
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
    	
    	
    	
    	final ScalableRegisterRow row = new ScalableRegisterRow(0.3);
    	HumanCard [] cards = new HumanCard[] {
    	                new HumanCard(1,Card.ACTION_MOVE1),
    	                new HumanCard(2,Card.ACTION_MOVE2),
    	                new HumanCard(3,Card.ACTION_MOVE3),
    	                new HumanCard(4,Card.ACTION_UTURN),
    	                new HumanCard(5,Card.ACTION_ROTATE_L)
    	};
    	row.setCards(cards);
    	final RegisterToolTip tp = new RegisterToolTip(row);
    	JButton jl = new JButton("hello World") {
    	    public JToolTip createToolTip() {
                return tp;
            }
    	};
    	
    	foo.add(jl, BorderLayout.CENTER);
    	/*
    	JButton inc = new JButton("+");
    	inc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                row.setScale(row.getScale()+0.1);
            }
        });
    	JButton dec = new JButton("-");
    	dec.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                row.setScale(row.getScale()-0.1);
            }
        });
    	final JButton show = new JButton("show next (1)");
    	show.addActionListener(new ActionListener() {
            int phase = 0;
    	    public void actionPerformed(ActionEvent e) {                    
                row.setCardVisibility(phase+1,true);
                phase=(phase+1)%5;
                show.setText("show next("+(phase+1)+")");
            }
        });
    	
    	JButton hide = new JButton("hide cards");
    	hide.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                row.hideAll();
            }
        });
    	
    	foo.add(hide, BorderLayout.WEST);
    	foo.add(show,BorderLayout.EAST);
    	foo.add(inc, BorderLayout.NORTH);
    	foo.add(dec,BorderLayout.SOUTH);
    	*/
    	fr.getContentPane().add(foo);
    	fr.setSize(450,700);
    	fr.show();
}
    
    
    
    
}
