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

    public static void assertTrue(String s, int pos, char c) throws FormatException {
        //d("assert: Erwarte "+c+" an Pos "+pos+" ;da ist "+s.charAt(pos));
        if(s.charAt(pos) != c) {
            throw new FormatException(Message.say("Board", "xExpectedChar", c, pos));
        }
        // "c" erwartet an Position "pos"
    }

    public static int assertws(String s, int pos) throws FormatException {
        if(!(java.lang.Character.isWhitespace(s.charAt(pos++)))) {
            throw new FormatException(Message.say("Board", "xNoWhitespaceAtPos", (pos - 1)));
        }
        while(java.lang.Character.isWhitespace(s.charAt(pos))) {
            pos++;
        }
        return pos;
    }
}

