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

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;

import de.botsnscouts.util.BotVis;
import de.botsnscouts.util.StatsList;
import de.botsnscouts.util.Stats;
import de.botsnscouts.util.Message;
import de.botsnscouts.util.TJLabel;



public class StatisticPanel extends JPanel implements ActionListener {

    private StatsList list;
    private JLabel [] [] desc;
    private static JLabel [] headings;

    private static final int COL_COUNT=8;
    private static final String MESSAGE_SECTION="StatisticPanel";
    private static final TJLabel [] COL_HEADINGS = {
      //new JLabel(""),// robot icon
      new TJLabel(Message.say(MESSAGE_SECTION, "name"), SwingConstants.LEFT),// robot name
      new TJLabel(Message.say(MESSAGE_SECTION, "hits"), SwingConstants.CENTER),
      new TJLabel(Message.say(MESSAGE_SECTION, "kills"), SwingConstants.CENTER),
      new TJLabel(Message.say(MESSAGE_SECTION, "damage_by_board"), SwingConstants.CENTER),
      new TJLabel(Message.say(MESSAGE_SECTION, "damage_by_robots"), SwingConstants.CENTER),
      new TJLabel(Message.say(MESSAGE_SECTION, "asked_wise"), SwingConstants.CENTER),
      new TJLabel(Message.say(MESSAGE_SECTION, "slowestProgrammer", SwingConstants.CENTER))

    };

    public StatisticPanel (StatsList stats){
      super();
      setOpaque(false);
      list = stats;
      init();
      list.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
      setAll();
      update();
    }

    public void update(){
      repaint();
    }

   private void init() {
      int l = list.size();

      desc = new JLabel [l] [COL_COUNT];

     /* GridLayout grid = new GridLayout(l+1, COL_COUNT);
      grid.setHgap(5);
      grid.setVgap(5);
      */
      GridBagLayout grid = new GridBagLayout();
      this.setLayout(grid);

      // add colum titles
       // "Bot" over the first two columns (robot name and icon)
      COL_HEADINGS[0].setForeground(Color.white);
      COL_HEADINGS[0].setFont(new Font("Times", Font.BOLD, 14));
      this.add (COL_HEADINGS[0], new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0,
                                    GridBagConstraints.CENTER,
                                    GridBagConstraints.HORIZONTAL,
                                    new Insets(0,10,10,0),0,0));

      for (int i=1;i<COL_COUNT-1;i++){
        COL_HEADINGS[i].setForeground(Color.white);
        COL_HEADINGS[i].setFont(new Font("Times", Font.BOLD, 14));
        this.add (COL_HEADINGS[i], new GridBagConstraints(i+1, 0, 1,1,0.0,0.0,
                                    GridBagConstraints.CENTER,
                                    GridBagConstraints.NONE,
                                    new Insets(0,10,10,0),0,0));
      }
      for (int i=0;i<l;i++){ // lines for robots
          for (int j=0;j<COL_COUNT;j++){ //colum values for robot l
            if (j==1) { // colum with names should be aligned left
               desc [i] [j] = new JLabel("", SwingConstants.LEFT);
               this.add (desc[i][j], new GridBagConstraints(j, i+1, 1,1,0.0,0.0,
                                     GridBagConstraints.WEST,
                                     GridBagConstraints.NONE,
                                     new Insets(0,0,0,0),0,0));
            }
            else {
              desc [i] [j] = new JLabel("", SwingConstants.CENTER);
              this.add (desc[i][j], new GridBagConstraints(j, i+1, 1,1,0.0,0.0,
                                    GridBagConstraints.CENTER,
                                    GridBagConstraints.NONE,
                                    new Insets(0,0,0,0),0,0));
            }
          }
      }
      setAll();
   }

   private void setStatsRow(int row, Stats s){
      String name = s.getName(); // will need the robot's name several times
      Color c = BotVis.getBotColorByName(name);

      desc [row][0].setIcon(new ImageIcon(BotVis.getBotIconByName(name)));

      desc [row] [1].setForeground(c);
      desc [row] [1].setText(s.getName());
      desc [row] [2].setForeground(c);
      desc [row] [2].setText(s.getHits()+"");
      desc [row] [3].setForeground(c);
      desc [row] [3].setText(s.getKills()+"");


      int foo =  s.getDamageByBoard();
      desc [row] [4].setForeground(c);
      desc [row] [4].setText(foo+"");

      foo = s.getDamageByRobots();
      desc [row] [5].setForeground(c);
      desc [row] [5].setText(foo+"");

      foo = s.getAskedWisenheimer();
      desc [row] [6].setForeground(c);
      desc [row] [6].setText(foo+"");

      foo = s.getWasSlowest();
      desc [row] [7].setForeground(c);
      desc [row] [7].setText(foo+"");
   }

   private void setAll(){
      Stats [] stats = list.getStatsSorted();
      int l = stats.length;
      for (int i=0; i<l;i++){
          setStatsRow(i, stats[i]);
      }
   }
}


