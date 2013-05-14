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

package de.botsnscouts.start;

import java.io.File;
import java.util.Locale;

import de.botsnscouts.util.Conf;
import de.botsnscouts.util.H;
import de.botsnscouts.util.Message;

/** Helper to allow starting a server on the command line */
public class CLIStarter {

    /** Define the number of columns (only used for centered headlines) */
    public static final int DISPLAY_COLS = 60;

    /**
     * The path to the spf-file that contains the gameboard (step4StartServer will have to use it)
     */
    @SuppressWarnings("unused")
    private static String spfFilePath;

    /** The number of players in this game */
    private static int numberOfPlayers = 8;

    /** The number of autobots (<=numberOfPplayers) in this game */
    private static int numberOfAutobots;

    /** Contains the intelligence/smartness (0<=i<=100) for each autobot */
    private static int[] autobotIntelligence;

    private static Locale myLocale;

    public static void main(String[] args) {
        if (args.length == 0) {
            handleWizardMode();
        }
        else
            if (args.length == 1) {
                handleConfigFileMode(args[0]);
            }
            else {
                printUsage();
            }

    }

    /** todo: implement (or don't..) */
    private static void printUsage() {
    }

    @SuppressWarnings("unused")
    private static void handleConfigFileMode(String filepath) {

    }

    private static void handleWizardMode() {
        step0Preparation();
        step0_5SetLocaleIfNeeded();
        step1SelectBoard();
        step2NumberOfPlayers();
        step3configureAutoBots();
        step4StartServer();
    }

    private static void exit() {
        System.out.println("Bye-Bye, see you later..");
        System.exit(0);
    }

    /**
     * Prints a little help text to create spf-Files. May cause a System.exit(0)
     */
    private static void step0Preparation() {
        H.cls();
        plnC("GAME CREATION STEP 0 - PREPARATION TOP 10 ");
        pln("");
        pln("Note: You need a working Botsnscouts board description (a \".spf\" file)");
        pln("      to start a game.");
        pln("      To create one: 1) start the Botsnscouts GUI");
        pln("                     2) select \"Start Game\"");
        pln("                     3) in the game creation screen, click \"edit\"");
        pln("                     4) build your board, place flags.. ");
        pln("                     5) if you are done, click \"ok\" to get back");
        pln("                     6) use \"save\" to save your board as \".spf\" file");
        pln("                     7) quit Botsnscouts");
        pln("                     8) copy your spf file to your server machine");
        pln("                     9) .. and remember the path to your file");
        pln("                    10) continue here ;-)");

        char c = H.readChar("Continue (y/n): ");
        if (c == 'y' || c == 'Y')
            return /* true */;
        else
            exit();

    }

    /**
     * Here the value of <code>spfFilePath</code> will be set. It is checked that the file at <code>spfFilePath</code> exists and is readable.
     */
    private static void step1SelectBoard() {
        boolean ok = false;
        while (!ok) {
            H.cls();
            plnC("GAME CREATION STEP 1 - Select the board for the game ");
            pln("");
            String filePath = H.readString("Enter the path to your spf-file: ");
            if (filePath != null) {
                filePath = filePath.trim();
                if (filePath.length() > 0) {
                    File spfFile = new File(filePath);
                    if (spfFile.exists()) {
                        spfFilePath = filePath;
                        if (spfFile.canRead()) {
                            ok = true;
                        }
                        else {
                            plnC("Sorry, can not read \"" + filePath + "\" !");
                            H.warte(2000);
                        }
                    }
                    else {
                        plnC("Sorry, the file \"" + filePath + "\" does not exist!");
                        H.warte(2000);
                    }
                }
            }
        }
    }

    /** We ask for the (maximum) number of players */
    private static void step2NumberOfPlayers() {
        H.cls();
        plnC("GAME CREATION STEP 2 - Select (total) number of players ");
        pln("");
        int greaterThan = 0;
        int smallerThan = 9;
        numberOfPlayers = H.readInt("Please enter the number of players (including autobots must be <=8): ",
                        greaterThan, smallerThan);

    }

    /**
     * We ask for the number of autobots and how sophisticated each of them will play.
     */
    private static void step3configureAutoBots() {
        H.cls();
        plnC("GAME CREATION STEP 3 - Select number of autobots ");
        pln("");
        pln("You selected " + numberOfPlayers + " players.");
        pln("Here you can select how many of your " + numberOfPlayers + " robots");
        pln("should be played by the computer!");
        pln("\n");
        int greaterThan = -1;
        int smallerThan = numberOfPlayers + 1;
        numberOfAutobots = H.readInt("Number of autobots (0<=number<=" + numberOfPlayers + "): ", greaterThan,
                        smallerThan);

        autobotIntelligence = new int[numberOfAutobots];
        if (numberOfAutobots > 0) {
            pln("\n");
            pln("Now you have to select the intelligence of the autobots;");
            pln("Select a value between 0 (dumb) and 100 (smart)");
            greaterThan = 0;
            smallerThan = 101;
            for (int i = 0; i < numberOfAutobots; i++) {
                autobotIntelligence[i] = H.readInt("Select intelligence of bot #" + (i + 1) + ": ", greaterThan,
                                smallerThan);
            }
        }
    }

    private static void step0_5SetLocaleIfNeeded() {
        String loc = Conf.getProperty("language.isSet");
        if (loc != null) {
            myLocale = new Locale(Conf.getProperty("language.lang"), Conf.getProperty("language.country"));
        }
        else {
            H.cls();
            plnC("GAME CREATION STEP 0.5 - Select language for Server Messages ");
            pln("");

            Locale[] list = Message.getLocales();
            String[] locals = new String[list.length];
            for (int i = 0; i < locals.length; i++) {
                locals[i] = list[i].getDisplayLanguage();
                System.out.println(i + " - " + locals[i]);
            }
            int sel = H.readInt("Use language number: ", -1, list.length);
            myLocale = list[sel];
            Conf.setProperty("language.isSet", "yes");
            Conf.setProperty("language.lang", myLocale.getLanguage());
            Conf.setProperty("language.country", myLocale.getCountry());
            Conf.saveProperties();
        }

        Message.setLanguage(myLocale);

    }

    private static void step4StartServer() {
        // TODO: Implement me. Will have to use spfFilePath

    }

    private static void plnC(String line) {
        System.out.println(H.center(line, DISPLAY_COLS));
    }

    private static void pln(String line) {
        System.out.println(line);
    }

}
