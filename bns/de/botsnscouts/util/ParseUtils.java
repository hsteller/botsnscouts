package de.botsnscouts.util;

/**
 *  Description of the Class
 *
 *@author     enno
 *@created    22. April 2001
 */
public class ParseUtils {
    public static boolean is(String s, int pos, char c) {
        return s.charAt(pos) == c;
    }

    public static void assert(String s, int pos, char c) throws FormatException {
        //d("assert: Erwarte "+c+" an Pos "+pos+" ;da ist "+s.charAt(pos));
        if(s.charAt(pos) != c) {
            throw new FormatException(Message.say("Spielfeld", "xExpectedChar", c, pos));
        }
        // "c" erwartet an Position "pos"
    }

    public static int assertws(String s, int pos) throws FormatException {
        if(!(java.lang.Character.isWhitespace(s.charAt(pos++)))) {
            throw new FormatException(Message.say("Spielfeld", "xNoWhitespaceAtPos", (pos - 1)));
        }
        while(java.lang.Character.isWhitespace(s.charAt(pos))) {
            pos++;
        }
        return pos;
    }
}

