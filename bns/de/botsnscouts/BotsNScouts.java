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

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.plaf.metal.MetalLookAndFeel;

import org.apache.log4j.Category;
import org.apache.log4j.PropertyConfigurator;

import de.botsnscouts.gui.Splash;
import de.botsnscouts.start.Start;
import de.botsnscouts.widgets.GreenTheme;

/**
 * The starting Ramp for BotsNScouts
 * @version $Id$
 */
public class BotsNScouts {
	
	/**
	 * This class has been deprecated and replaced by the Logger subclass. It
	 * will be kept around to preserve backward compatibility until mid 2003.
	 * Logger is a subclass of Category, i.e. it extends Category. In other
	 * words, a logger is a category. Thus, all operations that can be performed
	 * on a category can be performed on a logger. Internally, whenever log4j is
	 * asked to produce a Category object, it will instead produce a Logger
	 * object. Log4j 1.2 will never produce Category objects but only Logger
	 * instances. In order to preserve backward compatibility, methods that
	 * previously accepted category objects still continue to accept category
	 * objects.
	 * (http://logging.apache.org/log4j/docs/api/org/apache/log4j/Category.html)
	 */
	private static Category CAT = Category.getInstance(BotsNScouts.class);

	/**
	 * The method to start BotsnScouts (using the main menu/start application).
	 * Advanced use: for an immediate gamestart without configuring a game you have
	 * to use the following three args:
	 * args[0]: name of an .spf game description file (with or without ".spf" ending) 
	 * args[1]: [yes|no] ("yes" will start a human player, "no" will start a View)
	 * args[2]: the number of A.I. players to start
	 * 
	 * Example: args = {"big_test.spf",  "yes", "3"} will start a game with the
	 * preconfigured board "big_test", a human player and three additional A.I. robots.
	 * 
	 * 
	 * @param args usually none;   
	 * @throws Throwable
	 */
	public static void main(String[] args) throws Throwable {
		try {
			/*
			 * Initializes the LookAndFeel, this has to be done
			 * before ANY UI element is created 
			 */
			MetalLookAndFeel.setCurrentTheme(new GreenTheme());
			
			/*
			 * Splash Screen cration and displaying 
			 */
			Splash splash = new Splash();
			splash.showSplash(true);
			
			/*
			 * Allows the configuration of log4j from an external file
			 */
			PropertyConfigurator.configure(BotsNScouts.class
					.getResource("conf/log4j.conf"));
			//some loggings
			CAT.debug("Starting app");
			CAT.info("User.dir: " + System.getProperty("user.dir"));
			CAT.info("Java version: " + System.getProperty("java.vendor") + " "
					+ System.getProperty("java.version"));
			/*
			 * Shows the ingame window and hides the splash
			 */
			Start.main(args, splash);
		} catch (Throwable t) {
			CAT.fatal("Exception:", t);
			throw t;
		}
	}
	/**
	 * should be used instead of Toolkit.getDefaultToolkit().getScreenSize();
	 * @return the size of the screen 
	 */
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