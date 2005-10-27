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

public interface MessageID {

    public static final String AUSWERTUNG = "mAusw";

    public static final String BOT_CRUSHED = "mBotCrushed";
    public static final String BOT_IN_PIT = "mGrubenopfer";
    public static final String BOT_LASER = "mRobLaser";
    public static final String WISE_USED = "mKlugSchKlick";
    public static final String BORD_LASER_SHOT = "mBoardLaser";
    public static final String FLAG_REACHED = "mNextFlag";
    public static final String CHAT = "mChat";
    public static final String PROG_DONE = "mProgReceived";
    public static final String LAST_PROG = "mLastProg";
    public static final String INITIAL_FACINGS = "mInitFacing";
    public static final String BOT_MOVE = AUSWERTUNG + "Move";
    
    public static final String BOT_TURN = AUSWERTUNG+"Turn";
    public static final String BOT_UTURN = AUSWERTUNG+"UTurn";

    public static final String SOMEONE_QUIT = "mAbmeldung";
    public static final String BOT_REMOVED = "mHinr";

    public static final String SIGNAL_ACTION_START = "mSignalStart";
    public static final String SIGNAL_ACTION_STOP = "mSignalStop";
    public static final String PHASE_STARTED = AUSWERTUNG+"phaseStart";
    public static final String PHASE_ENDED = AUSWERTUNG+"phaseStop";
    public static final String PLAYING_CARD = AUSWERTUNG+"playingCard";
    public static final String REGISTER_LOCKED = AUSWERTUNG+"registerLocked";
    public static final String REGISTER_UNLOCKED = AUSWERTUNG+"registerUNLocked";
    public static final String NTC = "NOTIFY_CHANGE";

    
}

