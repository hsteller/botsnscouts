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
import java.net.*;

/**
 * @author Daniel Holtz, modified by Dirk
 */

public class Splash{
    Window splash;
    Frame dummy;
    Label textLabel;

    public void setText(String s){
	textLabel.setText(s);
	splash.add(textLabel,BorderLayout.SOUTH);
	splash.repaint();
    }

//    public static final int WIDTH = 744;
//    public static final int HEIGHT = 184;
    public static final int WIDTH = 468;
    public static final int HEIGHT = 115;

    public void showSplash(){
	dummy=new Frame();
	splash = new Window(dummy);

	Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
	int x = (screen.width-WIDTH)/2;
	int y = (screen.height-HEIGHT)/2;
	splash.setBounds(x,y,WIDTH,HEIGHT);

	ImageCanvas ic=new ImageCanvas(Toolkit.getDefaultToolkit().getImage(de.botsnscouts.BotsNScouts.class.getResource("images/logosmall.jpg")));
	splash.add(ic,BorderLayout.CENTER);

	textLabel=new Label();
	textLabel.setFont(new Font("Sans-Serif", Font.BOLD, 12));

	splash.pack();
	splash.setVisible(true);
    }

    public void noSplash(){
	splash.setVisible(false);
	splash=null;
    }

    private class ImageCanvas extends Canvas{
	private Image im;
	public ImageCanvas(Image im){ this.im=im; }
	public void paint(Graphics g){
	    g.drawImage(im,0,0,im.getWidth(this),im.getHeight(this),this);
	}
	private Dimension d=new Dimension(Splash.this.WIDTH,Splash.this.HEIGHT);
	public Dimension getPreferredSize(){ return d; }
    }
}
