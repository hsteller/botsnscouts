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

import de.botsnscouts.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.border.*;
import org.apache.log4j.Category;
/**
 * shows the status messages and action messages on the scrren
 * @author Lukasz Pekacki
 */

public class StatusLog  extends JPanel implements ActionListener{
    public StatusLog(View out) {
        parent = out;

        lF = new LogFrame();
        JButton logButton = OptionPane.getTransparentButton(Message.say("StatusLog","afLog"), 14);
        logButton.addActionListener(this);
        add(logButton);

	setLayout(new FlowLayout(FlowLayout.LEFT));
	add(singleMessage);
    }

    static Category CAT = Category.getInstance(StatusLog.class);

    // Objects
    private JTextArea textArea = new JTextArea();
    private JLabel singleMessage = new JLabel();

    private View parent;
    private LogFrame lF;
    protected TextArea ta = new TextArea(Message.say("StatusLog","afLog"),5,40);

    public void addMessage(String s) {
	singleMessage.setText(s);
        ta.insert(s+"\n", 0);
    }


    private void d(String s){
	Global.debug(this, s);
    }



    public void actionPerformed(ActionEvent e){
      lF.makeVisible();
    }


 protected Point ausgabeFrameLoc() {
	return getLocationOnScreen();
 }
 protected class LogFrame extends Frame implements WindowListener,MouseListener {
	boolean first = true;
	public LogFrame() {
	    this.setTitle(Message.say("AusgabeFrame","ereigLog"));
	    this.add(ta);
	    this.addWindowListener(this);
	    this.addMouseListener(this);
	    ta.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent e){ LogFrame.this.setVisible(false);}
		    public void mousePressed(MouseEvent e){ LogFrame.this.setVisible(false);}
		});
	    this.setSize(500,150);
	    this.setBackground(Color.lightGray);
	    this.setResizable(false);
	}
	public void windowDeactivated(WindowEvent e) {this.setVisible(false);}
	public void windowOpened(WindowEvent e)      {}
	public void windowClosing(WindowEvent e) {this.setVisible(false);}
	public void windowIconified(WindowEvent e)   { this.setVisible(false);}
	public void windowDeiconified(WindowEvent e) {}
	public void windowActivated(WindowEvent e)   {}
	public void windowClosed(WindowEvent e)   { this.setVisible(false);}

	public void mouseClicked(MouseEvent e){this.setVisible(false);}
	public void mouseEntered(MouseEvent e){}
	public void mouseExited(MouseEvent e){this.setVisible(false);}
	public void mouseReleased(MouseEvent e){}
	public void mousePressed(MouseEvent e){this.setVisible(false);}

	public void makeVisible(){
	    if (first){
		int startx = ausgabeFrameLoc().x;
		int starty = ausgabeFrameLoc().y;
		Dimension dim = parent.getSize();
                if (dim==null){
                  CAT.warn("Oh-Oh! Dim is null=>showing log window in upper left corner!");
                  this.setLocation(0,0);
                }
                else {
                  this.setLocation((startx+ dim.width-500),
                             (starty+dim.height-150));
                }
		first = false;
	    }
            this.setVisible(true);
	    this.show();
	}
	public void finalize() throws Throwable{
	    super.finalize();
	    this.dispose();
	}

    }

}



