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
 
package de.botsnscouts.editor;

import java.awt.*;
import java.awt.event.*;

import de.botsnscouts.board.SimBoard;
import de.botsnscouts.board.Wall;
import de.botsnscouts.gui.BoardView;
import de.botsnscouts.util.Message;
import de.botsnscouts.util.Bot;
import de.botsnscouts.util.Directions;
import de.botsnscouts.autobot.DistanceCalculator;
import org.apache.log4j.Category;


class EditorBoardView extends BoardView{
    private DistanceCalculator calc = null;
    static final Category CAT=Category.getInstance(EditorBoardView.class);

    public EditorBoardView(SimBoard ss,BoardEditor p){
	super(ss);
	addMouseListener(new EditMouseListener(p));
        bot=Bot.getNewInstance("editorDummy");
        bot.setNextFlag(1);
    }

    public void setCalc(DistanceCalculator calc) {
        this.calc = calc;
    }

    public void paintComponent(Graphics g) {
        paintUnbuffered( g );
    }

    Font myFont=new Font("SansSerif", Font.PLAIN, 16);
    Bot bot;
    protected void paintFeldBoden(Graphics g, int xpos, int ypos, int pixelx, int pixely) {
        super.paintFeldBoden(g, xpos, ypos, pixelx, pixely);
        if (calc != null){
            int x = pixelx+FELDSIZE/2;
            int y = pixely+FELDSIZE/2;
            g.setFont(myFont);
            g.setColor(Color.red);
            bot.moveTo(xpos, ypos);
            bot.setFacing(Directions.NORTH);
            g.drawString(""+calc.getDistance(bot), x, y-20);
            bot.setFacing(Directions.SOUTH);
            g.drawString(""+calc.getDistance(bot), x, y+20);
            bot.setFacing(Directions.EAST);
            g.drawString(""+calc.getDistance(bot), x+5, y);
            bot.setFacing(Directions.WEST);
            g.drawString(""+calc.getDistance(bot), x-30, y);
        }
    }
}
