/*******************************************************************************
 * ****************************************************************** Bots 'n'
 * Scouts - Multi-Player networked Java game * * Copyright (C) 2001 scouties. *
 * Contact botsnscouts-devel@sf.net *
 * ******************************************************************
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, in version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program, in a file called COPYING in the top directory of the Bots 'n'
 * Scouts distribution; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *  
 ******************************************************************************/

package de.botsnscouts.start;

import org.apache.log4j.Category;

import de.botsnscouts.util.BNSThread;
import de.botsnscouts.util.Global;
import de.botsnscouts.util.Message;

public class WaiterThread extends BNSThread {
    static Category CAT = Category.getInstance(WaiterThread.class);

    Thread[] ks = new Thread[100];

    int ksanz = 0;

    Start parent;

    boolean meldung = false;

    boolean beendemich = false;

    boolean waitServer = false;

    public WaiterThread(Start par) {
        this();
        parent = par;
    }

    public WaiterThread() {
        super("WaiterThread");
    }

    public void run() {
        if (ks == null || ksanz == 0) {
            CAT.debug("habe keine threads! beende mich");
            return;
        }

        CAT.debug("starte mich und warte auf " + ksanz + " threads");
        for (int i = 0; i < ksanz; i++) {
            try {
                ks[i].join();
            }
            catch (InterruptedException ex) {
                CAT.error("Interrupted while waiting for Threads!");
            }
        }
        if (waitServer) {
            Global.debug(this, "gonna wait for Server");
            while (!beendemich) {
                try {
                    sleep(3000);
                }
                catch (InterruptedException ex) {
                    CAT.error("Interrupted while waiting for Server!");
                }
            }
        }
        Global.debug(this, "in 5 sec. beende alles!");
        try {
            sleep(5000);
        }
        catch (InterruptedException ex) {
            CAT.error("Interrupted while waiting for Threads!");
        }
        if (meldung) {
            javax.swing.JOptionPane.showMessageDialog(null, Message.say("Start", "mBeendeAlles"),
                            Message.say("Start", "mMeldung"), javax.swing.JOptionPane.INFORMATION_MESSAGE);
        }

        // enno: wozu das alles, wenn wir sowieso system.exit(0) machen?
        // parent.fassade.killStartServer();
        // parent.fassade=null;
        // parent.dispose();

        // hendrik: ..and I don't even want to exist, "parent" will do the cleanup..
        // System.exit(0);
    }

    public synchronized void addThread(BNSThread th) {
        int count = ks.length;
        if (ksanz >= count) {
            Thread[] ksNew = new Thread[count * 2];
            for (int i = 0; i < count; i++) {
                ksNew[i] = ks[i];
            }
            ks = ksNew;
        }
        ks[ksanz++] = th;
    }

    public void setMeldung(boolean m) {
        meldung = m;
    }

    public synchronized void reset() {
        ksanz = 0;
        ks = new Thread[100];
        waitServer = false;
    }

    public void setServer() {
        waitServer = true;
    }

    public void quitYourself() {
        beendemich = true;
    }

    public void doShutdown() {
        stopAllWaitingThreads();
        reset();
    }

    public synchronized void stopAllWaitingThreads() {
        int count = ks == null ? 0 : ks.length;
        for (int i = 0; i < count; i++) {
            if (ks[i] != null) {
                try {
                    ks[i].interrupt();
                }
                catch (Exception e) {
                    CAT.warn(e);
                }
            }
        }
    }

}
