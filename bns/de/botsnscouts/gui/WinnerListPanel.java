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


/*
 * Created on 08.06.2005
 *
 */
package de.botsnscouts.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import de.botsnscouts.util.BotVis;
import de.botsnscouts.util.Directions;
import de.botsnscouts.util.KrimsKrams;
import de.botsnscouts.widgets.TJLabel;

/**
 * @author Hendrik Steller
 * @version $Id$
 */
public class WinnerListPanel extends JPanel {
    
    private JLabel [] [] desc;
   
    private static final int COL_COUNT=3;

    private String [] robnames;
    public WinnerListPanel (String [] names){
      super();
    // setOpaque(true);
      
      this.robnames = filterNullValues(names);
      desc = new JLabel[names.length][COL_COUNT];
      init();
    }

    /** Throwing away null values if there are an
     * @param names The robot's names as given by the AusgabeView
     * @return the values given in the parameter array (but without possible NULL-entries)
     */
    private String [] filterNullValues(String [] names){
        int le = names!=null?names.length:0;
        int notNullCount=0;
        for (int i=0;i<le;i++) {
            if (names[i]!=null){
                notNullCount++;
            }
        }
        if (notNullCount == le) {
            return names;
        }
        String [] filtered = new String[notNullCount];
        int j=0;
        for (int i=0;i<le;i++) {
            if (names[i]!=null){
                filtered[j++]=names[i];
            }
        }
        return filtered;
  	}
    

   private void init() {
   
      GridBagLayout grid = new GridBagLayout();
      this.setLayout(grid);
      int rowCount = robnames!=null?robnames.length:0;
      for (int row=0;row<rowCount;row++){ // lines for robots
          for (int col=0;col<COL_COUNT;col++){ //colum values for robot l
            if (col==NAME_COL) { // colum with names
               desc [row] [col] = new TJLabel("", SwingConstants.LEFT);
               this.add (desc[row][col], new GridBagConstraints(col, row+1, 1,1,0.0,0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.HORIZONTAL,
                                     new Insets(2,10,10,0),0,0));
            }
            else {
              desc [row] [col] = new TJLabel("", SwingConstants.LEFT);
              this.add (desc[row][col], new GridBagConstraints(col, row+1, 1,1,0.0,0.0,
                                    GridBagConstraints.CENTER,
                                    GridBagConstraints.HORIZONTAL,
                                    new Insets(2,10,10,0),0,0));
            }
          }
      }
      setAll();
      
      
      Border raisedbevel = BorderFactory.createRaisedBevelBorder();
      
      Border loweredbevel = BorderFactory.createLoweredBevelBorder();
      Border redline = BorderFactory.createLineBorder(Color.red);
      TitledBorder tb = BorderFactory.createTitledBorder(redline, "RANKING",
                      TitledBorder.CENTER,TitledBorder.TOP,new Font("Sans",Font.BOLD,30),Color.YELLOW.darker());
      Border compound = BorderFactory.createCompoundBorder(raisedbevel, loweredbevel);
      compound = BorderFactory.createCompoundBorder(tb, compound);
      
      this.setBorder(compound);
   }

   private static final int NAME_COL = 1; 
   private static final int IMAGE_COL = 2;
   private void setRobRow(int row, String name){
      
      
      Font f = new Font("Sans",Font.BOLD,getFontSize(row));
      desc[row][0].setFont(f);
      desc[row][0].setForeground(FORE_COLOR);
      desc[row][0].setText(row+1+".");
      int vis = BotVis.getBotVisByName(name);
      Color c = BotVis.getBotColorByBotVis(vis);
      
      if (row==0) {
          desc [row][IMAGE_COL].setIcon(new ImageIcon(BotVis.getBotImageByBotVis(vis, Directions.NORTH)));
      }
      else {
          desc [row][IMAGE_COL].setIcon(new ImageIcon(BotVis.getBotIconByBotVis(vis)));
      }
      desc[row][NAME_COL].setFont(f);
      desc [row] [NAME_COL].setForeground(c);
      desc [row] [NAME_COL].setText(name);
     

   }
   private static final Color FORE_COLOR = new Color(110,240,110);
   private static final int BASE_FONT_SIZE = 27;
   private int getFontSize (int row){
       if (row > 2) {
           return BASE_FONT_SIZE-3*3;
       }
       else {
           return BASE_FONT_SIZE-(3*row); 
       }
   }
   
   private void setAll(){
       int rowCount = robnames!=null?robnames.length:0;
       for (int i=0; i<rowCount;i++){
           setRobRow(i, robnames[i]);
       }
    }
   
   public static void main (String args[]){
       String [] robs = new String  [8];
       Hashtable ht= new Hashtable(robs.length);
       for (int i=0;i<robs.length;i++){
           robs[i]=KrimsKrams.randomName();
           ht.put(robs[i], new Integer(i));
       }
       
      
       BotVis.initBotVis(ht);
       WinnerListPanel wl = new WinnerListPanel(robs);
       wl.setBackground(Color.green.darker().darker().darker());
       JFrame fr = new JFrame("WinnerList test");              
       fr.getContentPane().setLayout(new BorderLayout());
       fr.getContentPane().add(wl, BorderLayout.CENTER);
       fr.getContentPane().add(new JLabel("foo"), BorderLayout.NORTH);
       fr.getContentPane().add(new JLabel("foo"), BorderLayout.SOUTH);
       fr.getContentPane().add(new JLabel("foo"), BorderLayout.EAST);
       fr.getContentPane().add(new JLabel("foo"), BorderLayout.WEST);
       fr.pack();
       fr.show();
       fr.addWindowListener(new WindowAdapter(){
           public void windowClosing (WindowEvent we) {
               System.exit(0);
           }
       });
   }

   
   

}

