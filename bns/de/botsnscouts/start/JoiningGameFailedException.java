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
 * Created on 09.09.2005
 *
 */
package de.botsnscouts.start;

/**
 * @author Hendrik Steller
 * @version $Id$
 */
@SuppressWarnings("serial")
public class JoiningGameFailedException extends Exception {

    private Exception possibleReason;

    public JoiningGameFailedException() {
        super();
    }

    public JoiningGameFailedException(String reason) {
        super(reason);
    }

    public JoiningGameFailedException(Exception reason) {
        super();
        possibleReason = reason;
    }

    public Exception getPossibleReason() {
        return possibleReason;
    }

}
