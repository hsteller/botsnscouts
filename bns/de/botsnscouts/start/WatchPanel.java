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

package de.botsnscouts.start;

import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.TJButton;
import de.botsnscouts.widgets.TJLabel;
import de.botsnscouts.widgets.TJTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * You see this panel when you want to register for watching a game without
 * participating.
 */
public class WatchPanel extends JPanel implements ActionListener, MouseListener {

    JLabel server;
    //JLabel port;

    JTextField serv;
    //JTextField prt;

    Start parent;

    Paint paint;

    Font font;

    public WatchPanel(Start par) {
        parent = par;
        parent.setTitle(Message.say("Start", "mZuschauen"));
        paint = parent.paint;

        font = new Font("Sans", Font.BOLD, 24);

        GridLayout lay;
        lay = new GridLayout(2, 2);
        lay.setHgap(170);
        lay.setVgap(200);

        setLayout(lay);
        setBorder(new EmptyBorder(200, 100, 200, 100));

        server = new TJLabel(Message.say("Start", "mServer"));
        //port=new JLabel(Message.say("Start","mPort"));
        serv = new TJTextField(Message.say("Start", "mServerInh"));
        //prt=new JTextField(Message.say("Start","mPortInh"));
        TJButton go = new TJButton(Message.say("Start", "mGoButton"));
        TJButton back = new TJButton(Message.say("Start", "mZurueckButton"));

        server.setFont(font);
        //port.setFont(font);
        serv.setFont(font);
        //prt.setFont(font);
        serv.setOpaque(false);
        //prt.setOpaque(false);

        server.setForeground(Color.lightGray);
        //port.setForeground(Color.lightGray);

        go.addActionListener(this);
        back.addActionListener(this);

        go.setActionCommand("go");
        back.setActionCommand("back");

        add(server);
        add(serv);
        //add(port);
        //add(prt);
        add(go);
        add(back);

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("go")) {
//	    System.out.println(farb.getSelectedIndex());
            int portnr = 8077;
            /*try{
            portnr=Integer.parseInt(prt.getText());
            }catch (Exception x){
            System.err.println("Must be a number!");
            return;
            }*/
            Thread ausgabe = parent.facade.watchAGame(serv.getText(), portnr);
            parent.addKS(ausgabe);
            parent.hide();
            parent.dispose();
            parent.beenden();
        } else if (e.getActionCommand().equals("back")) {
            parent.showMainMenu();
        }

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }


    public void mouseClicked(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Dimension d = getSize();
        g2d.setPaint(paint);
        g2d.fillRect(0, 0, d.width, d.height);
        paintChildren(g);
    }

}
