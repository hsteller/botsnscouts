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

package de.botsnscouts.comm;

/**
 * KommFutschExceptions werden von den Komm-Klassen geworfen, falls a) mehrmals
 * hintereinander "null" ueber den BufferedReader kommt b) eine IOException beim
 * Lesen auftritt c) der Client, der das Komm-Objekt benutzt, aus dem Spiel
 * entfernt wurde, ohne dass er mit 'warte' darauf gewartet hat; d.h. falls er
 * z.B einen InfoRequest ausgefuehrt hat und statt der Antwort ein REN erhielt.
 * Der Entfernungsgrund steht in der Exception-Message.
 * 
 * @author Hendrik
 */
@SuppressWarnings("serial")
public class KommFutschException extends KommException {

    public KommFutschException() {
        super();
    }

    /**
     * Der String s wird als Message der Exception ausgegeben.
     */
    public KommFutschException(String s) {
        super(s);
    }

}
