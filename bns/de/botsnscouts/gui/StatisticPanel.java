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

public class StatisticPanel extends JPanel implements ActionListener {

    private StatsList list;
    private JLabel [] [] desc;

    private static final int COL_COUNT=6;

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
      this.setLayout(new GridLayout(l, COL_COUNT));
      for (int i=0;i<l;i++){
          for (int j=0;j<COL_COUNT;j++){
            desc [i] [j] = new JLabel();
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


