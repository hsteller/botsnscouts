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

import de.botsnscouts.util.StatsList;
import de.botsnscouts.util.Stats;
import de.botsnscouts.util.Message;


public class StatisticPanel extends JPanel implements ActionListener {

    private StatsList list;
    private JLabel [] [] desc;
    private static JLabel [] headings;

    private static final int COL_COUNT=6;
    private static final String MESSAGE_SECTION="StatisticPanel";
    private static final JLabel [] COL_HEADINGS = {
      new JLabel("foo"),// robot icon
      new JLabel(Message.say(MESSAGE_SECTION, "name"), SwingConstants.CENTER),// robot name
      new JLabel(Message.say(MESSAGE_SECTION, "kills"), SwingConstants.CENTER),
      new JLabel(Message.say(MESSAGE_SECTION, "hits"), SwingConstants.CENTER),
      new JLabel(Message.say(MESSAGE_SECTION, "damage_by_board"), SwingConstants.CENTER),
      new JLabel(Message.say(MESSAGE_SECTION, "damage_by_robots"), SwingConstants.CENTER)

    };

    public StatisticPanel (StatsList stats){
      super();
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
      this.setLayout(new GridLayout(l+1, COL_COUNT));

      // add colum titles
      for (int i=0;i<COL_COUNT;i++)
        this.add (COL_HEADINGS[i]);

      for (int i=0;i<l;i++){ // lines for robots
          for (int j=0;j<COL_COUNT;j++){ //colum values for robot l
            desc [i] [j] = new JLabel("", SwingConstants.CENTER);
            this.add (desc[i][j]);
          }
      }
      setAll();
   }

   private void setStatsRow(int row, Stats s){
      //desc [row] [0].setIcon;//image
      desc [row] [0].setText("[img]");
      desc [row] [1].setText(s.getName());
      desc [row] [2].setText(s.getKills()+"");
      desc [row] [3].setText(s.getHits()+"");
      desc [row] [4].setText(s.getDamageByBoard()+"");
      desc [row] [5].setText(s.getDamageByRobots()+"");
   }

   private void setAll(){
      Stats [] stats = list.getStatsSorted();
      int l = stats.length;
      for (int i=0; i<l;i++){
          setStatsRow(i, stats[i]);
      }
   }
}


