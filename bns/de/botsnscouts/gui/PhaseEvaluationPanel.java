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
 * Created on 13.06.2005
 *
 */
package de.botsnscouts.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import org.apache.log4j.Category;

import de.botsnscouts.util.Bot;
import de.botsnscouts.util.BotVis;
import de.botsnscouts.util.CursorMan;
import de.botsnscouts.util.Directions;
import de.botsnscouts.util.ImageMan;
import de.botsnscouts.util.KrimsKrams;
import de.botsnscouts.widgets.TJPanel;

/**
 * To be shown during the five evaluation phases of a round. Intended to display
 * who played which cards, maybe highlight currently evaluated card.
 * 
 * @author Hendrik Steller
 * @version $Id: PhaseEvaluationPanel.java,v 1.7 2005/11/13 18:38:18 igzorn Exp
 *          $
 */
@SuppressWarnings("serial")
public class PhaseEvaluationPanel extends TJPanel {

    private static Category CAT = Category.getInstance(PhaseEvaluationPanel.class);

    private Bot[] bots;

    private ScalableRegisterRow[] registerRows;

    public PhaseEvaluationPanel() {
    }

    public PhaseEvaluationPanel(Bot[] robots, ScalableRegisterRow[] viewRows) {
        setContents(robots, viewRows);
    }

    public void setContents(Bot[] robots, ScalableRegisterRow[] viewRows) {
        this.bots = robots;
        this.registerRows = viewRows;
        reinitLayout();
    }

    protected void reinitLayout() {
        this.removeAll();
        GridBagLayout gr = new GridBagLayout();
        GridBagConstraints outer = new GridBagConstraints();
        outer.insets.bottom = 5;
        outer.fill = GridBagConstraints.BOTH;
        outer.anchor = GridBagConstraints.NORTH;
        this.setLayout(gr);
        setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets.bottom = 0;
        Font font = new Font("Sans", Font.BOLD, 10);

        int count = bots != null ? bots.length : 0;
        for (int row = 0; row < count; row++) {
            TJPanel rowPanel = new TJPanel();
            Bot currentBot = bots[row];

            int visID = currentBot.getBotVis();
            Color botColor = BotVis.getBotColorByBotVis(visID);

            Image img = BotVis.get48x48BotImageByBotVis(visID, Directions.NORTH);

            ImageIcon botIcon = new ImageIcon(img);

            // BotLabel picLabel = new BotLabel(currentBot);
            JLabel picLabel = new JLabel(botIcon, SwingConstants.CENTER);

            // I don't get any error, but I'm not sure that a JLabel is required
            // to _not_ choke on negative values here..
            try {
                picLabel.setIconTextGap(-5);
            }
            catch (Exception e) {
                CAT.warn("your JDK didn't like a negative pixel value..");
                CAT.warn(e.getMessage(), e);
                picLabel.setIconTextGap(0);
            }
            picLabel.setVerticalAlignment(SwingConstants.TOP);
            picLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
            picLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            picLabel.setForeground(botColor);
            picLabel.setFont(font);
            picLabel.setText(currentBot.getName());

            gc.gridy = row;
            gc.gridx = 0;
            gc.fill = GridBagConstraints.NONE;
            gc.anchor = GridBagConstraints.WEST;

            rowPanel.add(picLabel, gc);

            gc.gridx = 1;

            gc.anchor = GridBagConstraints.EAST;
            gc.fill = GridBagConstraints.BOTH;

            // Border border =
            // BorderFactory.createEtchedBorder(EtchedBorder.LOWERED,
            // botColor.brighter(),botColor.darker());
            // TitledBorder titleBorder =
            // BorderFactory.createTitledBorder(border,currentBot.getName(),
            // TitledBorder.LEFT, TitledBorder.TOP,nameFont, botColor);

            // rowPanel.setBorder(border);
            rowPanel.add(registerRows[row], gc);

            outer.gridy = row;
            this.add(rowPanel, outer);
        }
        // this.setPreferredSize(new Dimension(260,550));
        this.revalidate();
        this.repaint();
    }

    public void hideAll(boolean showCardBacksideInsteadOfEmpty) {
        int size = registerRows != null ? registerRows.length : 0;
        for (int i = 0; i < size; i++) {
            registerRows[i].alwayshowCardBackInsteadOfEmpty(showCardBacksideInsteadOfEmpty);
            registerRows[i].hideAll();
        }
    }

    public static void main(String[] args) {
        ImageMan.finishLoading();
        CursorMan.finishLoading();
        Bot[] bs = new Bot[8];
        ScalableRegisterRow[] rows = new ScalableRegisterRow[bs.length];
        for (int i = 0; i < bs.length; i++) {
            bs[i] = Bot.getNewInstance(KrimsKrams.randomName());
            bs[i].setBotVis(i);
            rows[i] = new ScalableRegisterRow(0.5, false, 5);
        }

        JFrame fr = new JFrame("EvalPhasePanel Test");
        fr.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });
        PhaseEvaluationPanel pan = new PhaseEvaluationPanel(bs, rows);
        fr.getContentPane().add(pan);
        fr.pack();
        fr.setVisible(true);
        // fr.show();
    }
}

@SuppressWarnings("serial")
class BotLabel extends JComponent {
    Image botImage;

    Color color;

    String name;

    public BotLabel(Bot bot) {
        int visID = bot.getBotVis();
        color = BotVis.getBotColorByBotVis(visID);

        botImage = BotVis.get48x48BotImageByBotVis(visID, Directions.NORTH);
        name = bot.getName();
        setOpaque(false);
    }

    public void paintComponent(Graphics g) {
        // g.clearRect(0,0,48,48);
        g.drawImage(botImage, 0, -7, 48, 48, this);
        g.setColor(color);

        g.drawString(name, 0, 45);
    }

    public Dimension getPreferredSize() {
        return new Dimension(48, 48);
    }

}
