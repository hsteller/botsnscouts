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


package de.botsnscouts.comm;

import de.botsnscouts.util.Directions;

public interface OtherConstants {

   // public static final String REASON_QUIT           = "GONE";
    public static final String REASON_LOST_LIVES     = "LL";
    public static final String REASON_TIMEOUT        = "TO";
    public static final String REASON_RULE_VIOLATION = "RV";
    public static final String REASON_SERVER_SHUTDOWN = "server shutdown";
    
    public static final String REQUEST_PUSHERS_PUSH_MULTIPLE = "ISPPMB";

  // for sending a sequence number in a message use MESSAGE_NUMBER=1234;
  // should be put into the last field if the message's String array
  // or appended in front of a notifychange in the form MESSAGE_NUMBER=1234,NTC(..)
  //                                     appended part: ^^^^^^^^^^^^^^^^^^^^
  public static final String MESSAGE_NUMBER = "message_number";

public static final int BOT_TURN_CLOCKWISE = Directions.BOT_TURN_CLOCKWISE;
public static final int BOT_TURN_COUNTER_CLOCKWISE=Directions.BOT_TURN_COUNTER_CLOCKWISE;

}

