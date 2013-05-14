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

/*
 * Created on 23.05.2005
 */
package de.botsnscouts.util;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author Hendrik Steller
 * @version $Id$
 * 
 */
public class ShutdownableSupport {

    private Collection<ShutdownListener> listeners;

    private Shutdownable ownerInNeedOfSupport;

    public ShutdownableSupport(Shutdownable supportee) {
        listeners = new LinkedList<ShutdownListener>();
        ownerInNeedOfSupport = supportee;
    }

    public void addShutdownListener(ShutdownListener listener) {
        listeners.add(listener);
    }

    public boolean removeShutdownListener(ShutdownListener listener) {
        return listeners.remove(listener);
    }

    /**
     * The Shutdownable using this helper object should call this method at the end of its shutdown()-method.
     *  It will notify all listeners.
     */
    public void shutdown() {
        for (ShutdownListener sl : listeners) {
            sl.shutdown(ownerInNeedOfSupport);
        }
    }

}
