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
     * @param src File containing board.
     * @param dst File (png) that will be generated.
     */
    public void dump(File src, File dst) throws IOException, FormatException,
            FlagException
    {
        dump(src, dst, SIZE);
    }

    /**
     * Dump board to image with given size.
     * @param src src File containing board.
     * @param dst  File (png) that will be generated.
     * @param size  Size of width and height of the generated picture.
     */
    public void dump(File src, File dst, int size) throws IOException, FormatException,
            FlagException
    {
        BoardView boardView = new BoardView(src);
        boardView.dumpPngImage(dst, size);
    }

    private static void usage() {
        System.err.println("Usage: java de.botsnscouts.util.RRA2png boardFile pngFile");
    }


    public static void main(String[] argv){

        PropertyConfigurator.configure(BotsNScouts.class.getResource("conf/log4j.conf"));

        if (argv.length < 2){
            usage();
        }
        try {
           (new RRA2png()).dump(new File(argv[0]), new File(argv[1]));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        } catch (FlagException e) {
            e.printStackTrace();
        }
    }


}
