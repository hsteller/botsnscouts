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

package de.botsnscouts;

import de.botsnscouts.gui.Splash;
import de.botsnscouts.start.Start;
import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import java.awt.*;

public class BotsNScouts {
    private static Category CAT = Category.getInstance(BotsNScouts.class);

    public static void main(String[] args) throws Throwable {
        try {
            Splash splash = new Splash();
            splash.showSplash();

            PropertyConfigurator.configure(BotsNScouts.class.getResource("conf/log4j.conf"));
            CAT.debug("Starting app");
            CAT.info("User.dir: " + System.getProperty("user.dir"));
            CAT.info("Java version: " + System.getProperty("java.vendor") + " " +
                    System.getProperty("java.version"));
            Start.main(args, splash);
        } catch (Throwable t) {
            CAT.fatal("Exception:", t);
            throw t;
        }
    }

    public static Dimension getScreenSize() {
        String s = System.getProperty("geometry");
        if (s != null) {
            try {
                s = s.toLowerCase();
                int x = s.indexOf('x');
                int x2 = x + 1;
                if (x > 0 && x2 < s.length()) {
                    String widthS = s.substring(0, x).trim();
                    String heightS = s.substring(x2).trim();
                    int width = Integer.parseInt(widthS);
                    int height = Integer.parseInt(heightS);
                    return new Dimension(width, height);
                }
            } catch (Exception e) {
                CAT.error("in getScreenSize()", e);
            }
        }

        Toolkit tk = Toolkit.getDefaultToolkit();
        return tk.getScreenSize();
    }

}