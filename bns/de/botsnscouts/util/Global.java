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

import org.apache.log4j.Category;
/**
* Hilfsmmethoden f&uuml;r den Debug-Modus
* @author Daniel Holtz
*/
public class Global {
        static Category CAT = Category.getInstance( Global.class );

	public static boolean verbose = true;
/**
* schaltet den Verbose-Modus an und aus
* setVerbose(true); schaltet ihn an
* ratet selbst, wie er wieder ausgeht
*/
	public static void setVerbose(boolean v){verbose = v;}


	public static void debug(Object o, String s){
		if (verbose) {
                Category.getInstance( o.getClass() ).debug( s );
		//Class Klasse = o.getClass();
		//String KlassenName = Klasse.getName();
		//System.out.println(KlassenName+": "+s);
	}
	} // ende debug

	public static void debug(String s){
		if (!verbose)
			return;
                CAT.debug( s );
		//System.out.println("Unbekannt: "+s);
	} // ende debug

} // ende Klasse
