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

import java.io.BufferedReader;
import java.io.PrintWriter;

import org.apache.log4j.Category;

import de.botsnscouts.util.Encoder;
import de.botsnscouts.util.StatsList;

// STAND : fertig
/**
 * Communication from the server to the views.
 * 
 * @author Hendrik<BR>
 */
public class KommServerAusgabe extends KommServer {

    private static final Category CAT = Category.getInstance(KommServerAusgabe.class);

    public KommServerAusgabe(BufferedReader in, PrintWriter out, String name) {
        super(in, out, name);
    }

    /**
     * Zur Benachrichtigung der Ausgabekan�le von �nderungen. Teilt (Ausgabe)
     * mit, bei welchen Robotern �nderungen eingetreten sind. Erh�lt die Namen
     * als Argument.
     * 
     * @exception KommException
     *                wird geworfen, falls beim Senden ein Fehler (z.B.
     *                IOException) auftrat
     */
    public void aenderung(int msgId, String[] roboternamen) throws KommException {
        try {
            String raus;
            if (msgId < 0)
                raus = "NTC(";
            else
                raus = OtherConstants.MESSAGE_NUMBER + "=" + msgId + ",NTC(";
            for (int i = 0; i < roboternamen.length; i++)
                raus = raus + Encoder.commEncode(roboternamen[i]) + ",";
            raus += ")";
            out.println(raus);
            CAT.debug("Notify change send: " + raus);
        }
        catch (Exception not_existant) {
            throw new KommException("Bei \"Aenderung\" trat eine Exception auf(Message: " + not_existant.getMessage()
                            + ")");
        }
    }

    /**
     * Zum &uuml;bermitteln von Nachrichten/Ereignissen f&uuml; die Ausgabe
     * 
     * @param id
     *            Die Art der Nachricht bzw ihre id f&uuml;r die Message-Klasse
     */
    public void message(String id) {
        out.println("MSG(" + id + ")");
        if (CAT.isDebugEnabled())
            CAT.debug("Message send: " + id);
    }

    /**
     * Zum &uuml;bermitteln von Nachrichten/Ereignissen f&uuml; die Ausgabe
     * 
     * @param id
     *            Die Art der Nachricht bzw ihre id f&uuml;r die Message-Klasse
     * @param name1
     *            Ein zus&auml;tzliches Argument, f&uuml;r die Ausgabe von
     *            Nachrichten
     */
    public void message(String id, String name1) {
        out.println("MSG(" + id + "," + Encoder.commEncode(name1) + ")");
        if (CAT.isDebugEnabled())
            CAT.debug("Message send: " + id + " " + Encoder.commEncode(name1));
    }

    public void message(String id, String name1, String name2) {
        out.println("MSG(" + id + "," + Encoder.commEncode(name1) + "," + Encoder.commEncode(name2) + ")");
        if (CAT.isDebugEnabled())
            CAT.debug("Message send: " + id + " " + Encoder.commEncode(name1) + " " + Encoder.commEncode(name2));
    }

    public void message(String id, String name1, String name2, String name3) {
        out.println("MSG(" + id + "," + Encoder.commEncode(name1) + "," + Encoder.commEncode(name2) + ","
                        + Encoder.commEncode(name3) + ")");
        if (CAT.isDebugEnabled())
            CAT.debug("Message send: " + id + " " + Encoder.commEncode(name1) + " " + Encoder.commEncode(name2) + " "
                            + Encoder.commEncode(name3));
    }

    public void message(String id, String name1, String name2, String name3, String name4) {
        out.println("MSG(" + id + "," + Encoder.commEncode(name1) + "," + Encoder.commEncode(name2) + ","
                        + Encoder.commEncode(name3) + "," + Encoder.commEncode(name4) + ")");
        if (CAT.isDebugEnabled())
            CAT.debug("Message send: " + id + "," + Encoder.commEncode(name1) + "," + Encoder.commEncode(name2) + ","
                            + Encoder.commEncode(name3) + "," + Encoder.commEncode(name4) + ")");
    }

    public void message(String id, String[] namen) {
        String send = "MSG(" + id;
        if (namen != null) {
            send += ",";
            for (int i = 0; i < namen.length; i++) {
                CAT.debug("appending: " + namen[i]);
                if (namen[i] != null)
                    send += Encoder.commEncode(namen[i]) + ",";
                else
                    send += "null,";
            }
        }
        send += ")";

        out.println(send);
        if (CAT.isDebugEnabled())
            CAT.debug("Message send: " + send);
    }

    /**
     * Sends the stats to the output-channel
     * 
     * @param sl
     *            the current StatsList of the server
     */
    public void sendStats(StatsList sl) {
        // sl.toSendString patched; will encode the robot names now
        out.println("GST" + sl.toSendString());
    }

}
