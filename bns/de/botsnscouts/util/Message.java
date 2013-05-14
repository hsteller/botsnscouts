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

package de.botsnscouts.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.log4j.Category;

/**
 * Internationalization All texts (except for debug-messages) are not hard coded but represented by labels that are defined in con/MessageBundle_<locale>.
 */
public class Message {

    private static final Category CAT = Category.getInstance(Message.class);

    private static String language = "empty";

    private static String country = "empty";

    private static ResourceBundle messages;

    private static MessageFormat formatter;

    private static LocaleFilter localeFilter;

    /**
     * Set the language for all text messages..
     */
    public static void setLanguage(String lang) {
        if (lang.equals("deutsch")) {
            language = "de";
            country = "DE";
        }
        else {// if (lang.equals("english")){
            language = "en";
            country = "US";
        }
        setLanguage(new Locale(language, country));
    }

    /**
     * Set the language for all text messages.
     */
    public static void setLanguage(Locale loc) {
        Locale myLocale = loc;
        messages = ResourceBundle.getBundle("de/botsnscouts/conf/MessagesBundle", myLocale);
        formatter = new MessageFormat("");
        formatter.setLocale(myLocale);
    }

    public static Locale[] getLocales() {
        Properties locProp = new Properties();
        Locale[] list = null;
        try {
            InputStream istream = de.botsnscouts.BotsNScouts.class.getResourceAsStream("conf/locales");
            locProp.load(istream);
        }
        catch (IOException e) {
            list = new Locale[1];
            list[0] = new Locale("en", "US");
            return list;
        }
        int n = Integer.parseInt(locProp.getProperty("numOfLocales"));
        list = new Locale[n];
        for (int i = 0; i < n; i++) {
            list[i] = new Locale(locProp.getProperty("loc" + i + ".language"), locProp.getProperty("loc" + i
                            + ".country"));
        }
        return list;
    }

    public static LocaleFilter getLocaleFilter() {
        if (localeFilter == null) {
            localeFilter = new LocaleFilter();
        }
        return localeFilter;
    }

    private static String getString(String id) {
        try {
            return messages.getString(id);
        }
        catch (MissingResourceException e) {
            CAT.warn("Could not find resource for " + id);
            return id; // so the program doesn't crash and we at least see *something*
        }
    }

    /**
     * Transform message-IDs to Strings.
     * 
     * @param callerSection
     *            The sectionString (see the MessageBundle-Files)
     * @param id
     *            The ID used to reference this string in the code. IDs starting with "m" are messages, with "x" Exceptions, with "b" button labels and with "e" errors.
     * @param args
     *            Up to four parameters for the message.
     */
    public static String say(String callerSection, String id, String[] args) {
        switch (args.length) {
            case 0:
                return say(callerSection, id);
            case 1:
                return say(callerSection, id, args[0]);
            case 2:
                return say(callerSection, id, args[0], args[1]);
            case 3:
                return say(callerSection, id, args[0], args[1], args[2]);
            default:
                return say(callerSection, id, args[0], args[1], args[2], args[3]);
        }
    }

    public static String say(String callerSection, String id, Object[] params) {
        formatter.applyPattern(getString(callerSection + "." + id));
        return formatter.format(params);
    }

    public static String say(String callerSection, String id) {
        return getString(callerSection + "." + id);
    }

    public static String say(String callerSection, String id, String P1) {
        // Parameter String
        formatter.applyPattern(getString(callerSection + "." + id));
        Object[] params = { P1 };
        return formatter.format(params);
    }

    public static String say(String callerSection, String id, String P1, String P2) { // Parameter String String
        formatter.applyPattern(getString(callerSection + "." + id));
        Object[] params = { P1, P2 };
        return formatter.format(params);
    }

    public static String say(String callerSection, String id, String P1, String P2, String P3) { // Parameter String String String
        formatter.applyPattern(getString(callerSection + "." + id));
        Object[] params = { P1, P2, P3 };
        return formatter.format(params);
    }

    public static String say(String callerSection, String id, String P1, String P2, String P3, String P4) { // Parameter S S S S
        formatter.applyPattern(getString(callerSection + "." + id));
        Object[] params = { P1, P2, P3, P4 };
        return formatter.format(params);
    }

    public static String say(String callerSection, String id, int P1) { // Parameter Int
        return say(callerSection, id, "" + P1);
    }

    public static String say(String callerSection, String id, String P1, int P2) { // Parameter String Int
        return say(callerSection, id, P1, "" + P2);
    }

    public static String say(String callerSection, String id, int P1, String P2) { // Parameter Int String
        return say(callerSection, id, "" + P1, P2);
    }

    public static String say(String callerSection, String id, int P1, int P2) { // Parameter Int Int
        return say(callerSection, id, "" + P1, "" + P2);
    }

    public static String say(String callerSection, String id, int P1, String P2, String P3) { // Parameter Int String String
        return say(callerSection, id, "" + P1, P2, P3);
    }

    public static String say(String callerSection, String id, String P1, int P2, String P3) { // Parameter String Int String
        return say(callerSection, id, P1, "" + P2, P3);
    }

    public static String say(String callerSection, String id, String P1, String P2, int P3) { // Parameter String String Int
        return say(callerSection, id, P1, P2, "" + P3);
    }

    public static String say(String callerSection, String id, String P1, int P2, int P3) { // Parameter String Int Int
        return say(callerSection, id, P1, "" + P2, "" + P3);
    }

    public static String say(String callerSection, String id, int P1, String P2, int P3) { // Parameter Int String Int
        return say(callerSection, id, "" + P1, P2, "" + P3);
    }

    public static String say(String callerSection, String id, int P1, int P2, String P3) { // Parameter Int Int String
        return say(callerSection, id, "" + P1, "" + P2, P3);
    }

    public static String say(String callerSection, String id, int P1, int P2, int P3) { // Parameter Int Int Int
        return say(callerSection, id, "" + P1, "" + P2, "" + P3);
    }

    public static String say(String callerSection, String id, String P1, String P2, String P3, int P4) { // Parameter S S S I
        return say(callerSection, id, P1, P2, P3, "" + P4);
    }

    public static String say(String callerSection, String id, String P1, String P2, int P3, String P4) { // Parameter S S I S
        return say(callerSection, id, P1, P2, "" + P3, P4);
    }

    public static String say(String callerSection, String id, String P1, String P2, int P3, int P4) { // Parameter S S I I
        return say(callerSection, id, P1, P2, "" + P3, "" + P4);
    }

    public static String say(String callerSection, String id, String P1, int P2, String P3, String P4) { // Parameter S I S S
        return say(callerSection, id, P1, "" + P2, P3, P4);
    }

    public static String say(String callerSection, String id, String P1, int P2, String P3, int P4) { // Parameter S I S I
        return say(callerSection, id, P1, "" + P2, P3, "" + P4);
    }

    public static String say(String callerSection, String id, String P1, int P2, int P3, String P4) { // Parameter S I I S
        return say(callerSection, id, P1, "" + P2, "" + P3, P4);
    }

    public static String say(String callerSection, String id, String P1, int P2, int P3, int P4) { // Parameter S I I I
        return say(callerSection, id, P1, "" + P2, "" + P3, "" + P4);
    }

    public static String say(String callerSection, String id, int P1, String P2, String P3, String P4) { // Parameter I S S S
        return say(callerSection, id, "" + P1, P2, P3, P4);
    }

    public static String say(String callerSection, String id, int P1, String P2, String P3, int P4) { // Parameter I S S I
        return say(callerSection, id, "" + P1, P2, P3, "" + P4);
    }

    public static String say(String callerSection, String id, int P1, String P2, int P3, String P4) { // Parameter I S I S
        return say(callerSection, id, "" + P1, P2, "" + P3, P4);
    }

    public static String say(String callerSection, String id, int P1, String P2, int P3, int P4) { // Parameter I S I I
        return say(callerSection,id,"" + P1, P2, "" + P3, "" + P4);
    }

    public static String say(String callerSection, String id, int P1, int P2, String P3, String P4) { // Parameter I I S S
        return say(callerSection, id, "" + P1, "" + P2, P3, P4);
    }

    public static String say(String callerSection, String id, int P1, int P2, String P3, int P4) { // Parameter I I S I
        return say(callerSection, id, "" + P1, "" + P2, P3, "" + P4);
    }

    public static String say(String callerSection, String id, int P1, int P2, int P3, String P4) { // Parameter I I I S
        return say(callerSection, id, "" + P1, "" + P2, "" + P3, P4);
    }

    public static String say(String callerSection, String id, int P1, int P2, int P3, int P4) { // Parameter I I I I
        return say(callerSection, id, "" + P1, "" + P2, "" + P3, "" + P4);
    }



    static class LocaleFilter implements FilenameFilter {
        public LocaleFilter() {
        }
    
        public boolean accept(File dir, String name) {
            try {
                boolean ok = name.endsWith(".properties") && name.startsWith("MessagesBundle_");
                ok = ok && name.length() == 31 && name.charAt(17) == '_';
                return ok;
            }
            catch (Throwable t) {
                CAT.debug("Error in LocaleFilter: " + t);
                return false;
            }
        }
    }
}
