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
 
package de.botsnscouts.server;

/** The admin-functions on the entity that maintains the
    list of active ServerRobotThreads / ServerOutputThreads 
*/
public interface ThreadMaintainer{
    /** Should give a reference, as any non-alive threads
	will be removed by an Iterator.remove() */
    java.util.Vector /* of ServerAusgabeThread */ getActiveOutputs();
    void deleteOutput(ServerAusgabeThread victim, String reason);
    void addOutput(ServerAusgabeThread n);
    int allocateColor(int myPreferredColor, String name);
    void addRobotThread(ServerRoboterThread n);
    MOKListener getMOKListener();
    OKListener getOKListener();
    ServerRobotThreadMaintainer getRobThreadMaintainer();
    ServerOutputThreadMaintainer getOutputThreadMaintainer();
    InfoRequestAnswerer getInfoRequestAnswerer();
    de.botsnscouts.start.StartServer getStartServer();
    int getSignUpTimeout();
    int getMaxPlayers();
}
