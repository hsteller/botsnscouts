/*
 * Created on 13.06.2005
 *
 */
package de.botsnscouts.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

import org.apache.log4j.Category;

import de.botsnscouts.util.Bot;
import de.botsnscouts.util.BotVis;
import de.botsnscouts.util.CursorMan;
import de.botsnscouts.util.Directions;
import de.botsnscouts.util.ImageMan;
import de.botsnscouts.util.KrimsKrams;
import de.botsnscouts.widgets.TJPanel;

/**
 * To be shown during  the five evaluation phases of a round.
 * Intended to display who played which cards, maybe highlight currently evaluated card. 
 * 
 * @author Hendrik Steller
 * @version $Id$
 */
public class PhaseEvaluationPanel extends TJPanel {
    
    private static Category CAT = Category.getInstance(PhaseEvaluationPanel.class);
    
    private Bot [] bots;
    private ScalableRegisterRow [] registerRows;
    
    public PhaseEvaluationPanel(){
        
    }
    
    public PhaseEvaluationPanel(Bot [] robots, ScalableRegisterRow [] viewRows ){
        setContents(robots, viewRows);
    }

    public void setContents(Bot [] robots, ScalableRegisterRow [] viewRows ){
        this.bots = robots;
        this.registerRows = viewRows;
        reinitLayout();
    }
    
   
    
    protected void reinitLayout()  {
        this.removeAll(); 
        GridBagLayout gr = new GridBagLayout();
        GridBagConstraints outer = new GridBagConstraints();
        outer.insets.bottom=5;
        outer.fill= GridBagConstraints.BOTH;
        outer.anchor = GridBagConstraints.NORTH;
        this.setLayout(gr);
        setOpaque(false);
        
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets.bottom = 0;
       Font font = new Font("Sans", Font.BOLD, 10);
        
        int count = bots!=null?bots.length:0;
        for (int row=0;row<count;row++){                       
            TJPanel rowPanel = new TJPanel();                        
            Bot currentBot = bots[row];
            
          
            int visID = currentBot.getBotVis();
            Color botColor = BotVis.getBotColorByBotVis(visID);
            
            Image img = BotVis.get48x48BotImageByBotVis(visID, Directions.NORTH);
            	
            ImageIcon botIcon = new ImageIcon(img);
      
          
            //BotLabel picLabel = new BotLabel(currentBot);
            JLabel picLabel = new JLabel(botIcon,JLabel.CENTER);
            
            // I don't get any error, but I'm not sure that a JLabel is required
            // to _not_  choke on negative values here..
            try {
                picLabel.setIconTextGap(-5);
            }
            catch (Exception e){
                CAT.warn("your JDK didn't like a negative pixel value..");
                CAT.warn(e.getMessage(), e);
                picLabel.setIconTextGap(0);
            }
            picLabel.setVerticalAlignment(JLabel.TOP);
            picLabel.setVerticalTextPosition(JLabel.BOTTOM);
            picLabel.setHorizontalTextPosition(JLabel.CENTER);
            picLabel.setForeground(botColor);
            picLabel.setFont(font);
            picLabel.setText(currentBot.getName());
            
            
            gc.gridy = row;
            gc.gridx = 0;
            gc.fill = GridBagConstraints.NONE;
            gc.anchor = GridBagConstraints.WEST;
           
            rowPanel.add(picLabel, gc);
           
            
            gc.gridx = 1;
          
            gc.anchor=GridBagConstraints.EAST;
            gc.fill = GridBagConstraints.BOTH;
            
         //   Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED, botColor.brighter(),botColor.darker());            
         //   TitledBorder titleBorder =  BorderFactory.createTitledBorder(border,currentBot.getName(),
         //                   					TitledBorder.LEFT, TitledBorder.TOP,nameFont, botColor);
            
           
          //  rowPanel.setBorder(border);
            rowPanel.add(registerRows[row], gc);           
          
            outer.gridy = row;
            this.add(rowPanel, outer);
        }
      //  this.setPreferredSize(new Dimension(260,550));
        this.revalidate();
    }
    
    public void hideAll(boolean showCardBacksideInsteadOfEmpty){
        int size = registerRows!=null?registerRows.length:0;
        for (int i=0;i<size;i++){       
            registerRows[i].alwayshowCardBackInsteadOfEmpty(showCardBacksideInsteadOfEmpty); 
            registerRows[i].hideAll();
        }
    }
    
    
    public static void main(String[] args) {
        ImageMan.finishLoading();
        CursorMan.finishLoading();
        Bot [] bs = new Bot[8];
        ScalableRegisterRow [] rows = new ScalableRegisterRow[bs.length];
        for (int i=0;i<bs.length;i++){
            bs[i]=Bot.getNewInstance(KrimsKrams.randomName());
            bs[i].setBotVis(i);
            rows[i] = new ScalableRegisterRow(0.5, false, 5);
        }
        
        
        JFrame fr = new JFrame("EvalPhasePanel Test");
        fr.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we){
                System.exit(0);
            }
        });
        PhaseEvaluationPanel pan = new PhaseEvaluationPanel(bs,rows);
        fr.getContentPane().add(pan);
        fr.pack();
        fr.show();                
    }
}

class BotLabel extends JComponent{
    Image botImage;
    Color color;
    String name;
   
    
    public BotLabel(Bot bot){
        int visID = bot.getBotVis();
        color = BotVis.getBotColorByBotVis(visID);
        
        botImage = BotVis.get48x48BotImageByBotVis(visID, Directions.NORTH);
        name = bot.getName();
        setOpaque(false);
    }
    
    public void paintComponent(Graphics g){
        //g.clearRect(0,0,48,48);
        g.drawImage(botImage,0,-7,48,48,this);
        g.setColor(color);
           
        g.drawString(name, 0,45);
    }
    
    public Dimension getPreferredSize() {
      return  new Dimension (48,48);
    }
    
}
