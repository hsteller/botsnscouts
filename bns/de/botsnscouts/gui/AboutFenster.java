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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import de.botsnscouts.BotsNScouts;
import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.TJButton;

@SuppressWarnings("serial")
public class AboutFenster extends JFrame {

    public AboutFenster() {

        Dimension d = BotsNScouts.getScreenSize();
        setLocation((d.width / 2) - 200, ((d.height / 2) - 200));
        setSize(400, 500);
        setTitle(Message.say("AboutFenster", "mtitel"));

        String s = Message.say("AboutFenster", "mtext");
        JPanel inhalt = new JPanel();
        inhalt.setBorder(new EmptyBorder(10, 10, 10, 10));

        JTextArea text = new JTextArea(s) {
            public Dimension getPreferredSize() {
                return new Dimension(370, 400);
            }
        };
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        inhalt.add(text);

        JButton ok = new TJButton(Message.say("AboutFenster", "mok"));
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        inhalt.add(ok);

        getContentPane().add(inhalt);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setVisible(true);
    }

    public static void main(String args[]) {
        Message.setLanguage("deutsch");
        AboutFenster f = new AboutFenster();
        // f.setSize(200,640);
        f.setVisible(true);
    }
}
