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

import java.util.*; 
 
/** 
 * KrimsKrams stellt Hilfsfunktionen zur Verf&uuml;gung.
 * @author Miriam
 */ 
public class KrimsKrams { 
/** 
 * Bastelt einen zufaelligen Namen.
 * Aenderung fuer Version 2: Namen haben nicht immer das gleiche Muster, sondern werden gemaess einem 
 * Vokal/Konsonsantenmuster erzeugt.
 * 
 * @return String zuf&auml;lliger Name. Der Name besteht aus
 * 'A'-'Z','a'-'z', ist aussprechbar und hat eine sinnvolle L&auml;nge.
 * @param Laenge des Namens ist aus Kompatibilitaetsgruenden noch drin, 
 * aber ohne Argument wird der Name schoener, sorry.
 *                
 * Known Bugs: Aufrufe sofort hinteinander koennen identische namen generieren, schuld ist der Java-random-
 * Konstruktor, der als seed die Zeit in Millisec. nimmt. (Oder was?)
 */

    // Unterschiedliche Buchstabenhaeufigkeiten, damit die Namen
    // schoener klingen.
    static char[] vokale = {'a','a','a','e','e','i','i','i','o','u'};
    static char[] konsonanten = {'b','b','b','c','d','d','d','f','f','g','g','h','h','j','k','k','l','l','l','m','m','n','n','p','p','r','r','r','s','s','s','s','t','t','t','v','w','w','x','y','z'};


    public static String randomName(int n) {             
	StringBuffer s = new StringBuffer(randomName());
	if (n<s.length())
	    return (new String(s)).substring(0,n);
	else 
	    for (int i=s.length(); s.length() < n; i++){
		Random random = new Random();
		char c;
		if (i%2 == 0) {
		    // Zufaelligen Vokal nehmen
		    int z = java.lang.Math.abs(random.nextInt())%vokale.length; 
		    c = vokale[z];
		}
		else {
		    // Zufaelligen Vokal nehmen
		    int z = java.lang.Math.abs(random.nextInt())%konsonanten.length; 
		    c = konsonanten[z];
		}
		s.append(c);
	    }
	return new String(s);
    }

/** 
 * Bastelt einen zufaelligen Namen.
 * 
 * @return String zuf&auml;lliger Name. Der Name besteht aus
 * 'A'-'Z','a'-'z' und ist aussprechbar.
 *
 */ 
    public static String randomName() {


	/*  Muster fuer den Namen. Ein Muster dient als Vorlage fuer einen Namen:
	 *  true steht fuer Vokal, false fuer Konsonanten.
	 */
	boolean[][] vokalmuster = {{false,true,false,false,true,false}, //alte Version fuer "maennlich"
				   {false,true,false,false,true,false}, // (doppelt so haeufig wie andere)y
				   {false,true,false,false,true,true},  //alte Version fuer "eher weiblich"
				   {false,true,false,false,true},       //z.B. Babba
				   {false,true,false,true},              //     Baba
				   {false,true,false,false},             //     Babb
				   {true,false,false,true,false},         //     Abbab
				   {true,false,false,true,false,true}    //     Abbaba
	                          };

	Random random = new Random(); 
	
	// Waehle zufaellig ein Muster:
	int musterindex = java.lang.Math.abs(random.nextInt())%vokalmuster.length;


	StringBuffer name = new StringBuffer(vokalmuster[musterindex].length);

	/* ------- ALTE VERSION --------------------
	   
	   for (int i=2;i < n+2; i++) {
	   if (i%3 == 0 || (i==n+1 && lastchar == 0)) {
	   // Zufaelligen Vokal nehmen
	   int z = java.lang.Math.abs(random.nextInt())%vokale.length; 
	   c = vokale[z];
	   }
	   else {
	   // Zufaelligen Vokal nehmen
	   int z = java.lang.Math.abs(random.nextInt())%konsonanten.length; 
	   c = konsonanten[z];
	   }
	   if (name.length()==0) //Erster Buchstabe gross 
	   c = Character.toUpperCase(c);
	   name = name.append(c);
	   }
	*/
	
	for (int i=0; i<vokalmuster[musterindex].length;i++) {
	    char c;
	    int z = java.lang.Math.abs(random.nextInt());    
	    if (vokalmuster[musterindex][i])
		c = vokale[z%vokale.length];
	    else
		c = konsonanten[z%konsonanten.length];
	    if (name.length()==0) //Erster Buchstabe gross 
		c = Character.toUpperCase(c);
	    name = name.append(c); 
	}

	return name.toString();
    }

    public static void main(String argv[]) {
	/*
 	String s1 = randomName(8);
	String s2 = randomName(6);
	String s3 = randomName(4);
	System.out.println("Zufaellige Namen: "+s1+", "+s3+"\n"+s2); */
	for (int i=0;i<100;i++){
	    System.out.print(randomName());
	    for (int j=1;j<100000;j++); //busy waiting....... (um bei Testausgabe den Bug nicht zu sehen.) 
	    System.out.print(" ");
	}
	System.out.println();
    }

}






