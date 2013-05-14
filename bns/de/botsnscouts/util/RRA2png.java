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

package de.botsnscouts.util;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.PropertyConfigurator;

import de.botsnscouts.BotsNScouts;
import de.botsnscouts.board.FlagException;
import de.botsnscouts.gui.BoardView;

/**
 * Create a png image from a rra board file.
 */
public class RRA2png {

    private static final int SIZE = 192;

    /**
     * Dump image with default size.
     * 
     * @param src
     *            File containing board.
     * @param dst
     *            File (png) that will be generated.
     */
    public void dump(File src, File dst) throws IOException, FormatException, FlagException {
        dump(src, dst, SIZE);
    }

    /**
     * Dump board to image with given size.
     * 
     * @param src
     *            src File containing board.
     * @param dst
     *            File (png) that will be generated.
     * @param size
     *            Size of width and height of the generated picture.
     */
    public void dump(File src, File dst, int size) throws IOException, FormatException, FlagException {
        BoardView boardView = new BoardView(src);
        boardView.dumpPngImage(dst, size);
    }

    private static void usage() {
        System.err.println("Usage: java de.botsnscouts.util.RRA2png boardFile pngFile");
    }

    public static void main(String[] argv) {

        PropertyConfigurator.configure(BotsNScouts.class.getResource("conf/log4j.conf"));

        if (argv.length < 2) {
            usage();
        }
        try {
            (new RRA2png()).dump(new File(argv[0]), new File(argv[1]));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (FormatException e) {
            e.printStackTrace();
        }
        catch (FlagException e) {
            e.printStackTrace();
        }
    }

}
