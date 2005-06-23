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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JLabel;

import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.TJLabel;
import de.botsnscouts.widgets.TJPanel;

/**
 * 
 * Gets shown in the right half of the GUI (instead of the cards, registers etc) if
 * the game is over.
 * Contains a big vertical message, every letter in a TJLabel; also, another TJLabel
 * for additional inforamtion (example: big message="robot destroyed", small="reason:rule violation").
 * But mostly intended to show something like "Goal reached" if the bot has reached the
 * final flag
 * 
 * @version $Id$
 */
public class ZielfahneErreicht extends TJPanel{

    private static final Color backColor = new Color(4,64,4);
    private static final Color foreColor2 = new Color(140,255,140);

  
    public ZielfahneErreicht() {
        //this("",false);
    }

    
    public void setMessage(String bigVerticalMessage, boolean isDead, String removalReason){
    	boolean haveReason = removalReason != null && removalReason.trim().length()>1;
        int numOfChars = bigVerticalMessage != null?bigVerticalMessage.length():0;
        setLayout(new GridBagLayout());
        
        GridBagConstraints gcs = new GridBagConstraints();        
        gcs.anchor = GridBagConstraints.CENTER;
        gcs.gridx = 0;
        gcs.gridy = 0;
        gcs.insets = new Insets(10,10,10,10);
        gcs.gridheight = 1;
        gcs.gridwidth = 1;
        gcs.fill = GridBagConstraints.HORIZONTAL;
        add(new TJLabel(""), gcs); // space on the top
        Font big = new Font("Sans", Font.BOLD, 24);
        Color col = isDead?Color.RED:Color.YELLOW;
        gcs.insets.bottom = 5;        
        gcs.insets.top = 0;
        gcs.fill = GridBagConstraints.NONE;    
        int align = JLabel.CENTER;
        for (int i=0;i<numOfChars;i++){            
            TJLabel l = new TJLabel(""+bigVerticalMessage.charAt(i),align,col);        
            l.setFont(big);
            gcs.anchor=GridBagConstraints.CENTER;
            gcs.gridy++;            
            add(l, gcs);            
        }
        if (haveReason) {
            gcs.gridy++;
            gcs.gridx=0;
            gcs.insets.top = 20;
            Font small = new Font("Sans",Font.PLAIN, 10);
            TJLabel l = new TJLabel(removalReason,JLabel.CENTER);
            l.setFont(small);   
            add(l, gcs);
        }
        

        
    }
    
   
    public Dimension getPreferredSize() {
	return new Dimension(180,550);
    }

    public static void main (String args[]) {
		Message.setLanguage("deutsch");
		Frame f = new Frame("Test");
		f.setSize(350,640);
		ZielfahneErreicht zf = new ZielfahneErreicht();
		zf.setMessage("Robot destroyed", true, "Rule violation - probably a program error");
		f.add(zf);
		f.setVisible(true);
		f.addWindowListener(new WindowAdapter(){
		    public void windowClosing(WindowEvent e){
		        System.exit(0);
		    }
		});
    }
}
