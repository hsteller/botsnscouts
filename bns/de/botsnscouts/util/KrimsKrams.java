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

import java.util.Random;

/**
 * Generating nice random names.
 * @author Miriam
 */
public class KrimsKrams {

    /**
     * Array of vowels. Different frequencys to make the names nicer...
     */
    static char[] vowels = {'a', 'a', 'a', 'e', 'e', 'i', 'i', 'i', 'o', 'u'};
    static char[] consonants = {'b', 'b', 'b', 'c', 'd', 'd', 'd', 'f', 'f', 'g', 'g', 'h', 'h', 'j', 'k', 'k', 'l', 'l', 'l', 'm', 'm', 'n', 'n', 'p', 'p', 'r', 'r', 'r', 's', 's', 's', 's', 't', 't', 't', 'v', 'w', 'w', 'x', 'y', 'z'};

    private static Random random = new Random();

    /**  Pattern vor the random name: true means vowel, false consonant.
     */
    private static boolean[][] vowelPattern = {
        {false, true, false, false, true, false}, //'male' name
        {false, true, false, false, true, false}, //double likeliness for this one
        {false, true, false, false, true, true}, //'female' name
        {false, true, false, false, true}, //e.g. Babba
        {false, true, false, true}, //e.g. Baba
        {false, true, false, false}, //e.g.Babb
        {true, false, false, true, false}, //e.g.Abbab
        {true, false, false, true, false, true}    //e.g.     Abbaba
    };

    /**
     * Generates a nice name, usually pronouncable.
     *
     * @return a random name, consisting of
     * 'A'-'Z','a'-'z'.
     *
     */
    public static String randomName() {

        // Choose random pattern
        int patternIndex = java.lang.Math.abs(random.nextInt()) % vowelPattern.length;

        StringBuffer name = new StringBuffer(vowelPattern[patternIndex].length);

        for (int i = 0; i < vowelPattern[patternIndex].length; i++) {
            char c;
            int z = java.lang.Math.abs(random.nextInt());
            if (vowelPattern[patternIndex][i])
                c = vowels[z % vowels.length];
            else
                c = consonants[z % consonants.length];
            if (name.length() == 0) //Capital first letter
                c = Character.toUpperCase(c);
            name = name.append(c);
        }

        return name.toString();
    }

    public static void main(String argv[]) {
        for (int i = 0; i < 100; i++) {
            System.out.print(randomName() + " ");
        }
        System.out.println();
    }

}






