package de.botsnscouts.server;

import de.botsnscouts.util.Location;

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
*/

/**
 * Options of a game, e.g. max number of players, allow scout, board ...
 */
public class GameOptions {
    /** Max number of players for this game */
    private int maxPlayers;
    /** TCP/IP port for registration */
    private int registrationPort;
    /** Tiwmeout in ms for handing in cards. */
    private int handInTimeout;
    /** The board for the game.
     * Represented as a string in the fucking old board format.
     */
    private String board;
    /**
     * Location of the flags
     */
    private Location[] flags;
    /**
     * "First coordinate of the board's dimension"
     */
    private int x;
    /**
     * "Second coordinate of the board's dimension"
     */
    private int y;
    /**
     *
     * @param noPlayers  Max number of players for this game
     * @param port TCP/IP port for registration
     * @param timeout Timeout in ms for handing in cards.
     * @param board   The board for the game.
     * @param flags   Location of the flags
     * @param x        "First coordinate of the board's dimension"
     * @param y        "Second coordinate of the board's dimension"
     */
    public GameOptions( int noPlayers,
                        int port,
                        int timeout,
                        String board,
                        Location[] flags,
                        int x,
                        int y) {
        this.maxPlayers = noPlayers;
        this.registrationPort = port;
        this.handInTimeout = timeout;
        this.board = board;
        this.flags = flags;
        this.x = x;
        this.y = y;

    }

    /**
     *
     * @return  Max number of players for this game
     */
    public int getMaxPlayers() {
        return maxPlayers;
    }

    /**
     *
     * @return TCP/IP port for registration
     */
    public int getRegistrationPort() {
        return registrationPort;
    }

    /**
     *
     * @return  Timeout in ms for handing in cards.
     */
    public int getHandInTimeout() {
        return handInTimeout;
    }

    /**
     *
     * @return  The board for the game.
     */
    public String getBoard() {
        return board;
    }

    public Location[] getFlags() {
        return flags;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String toString() {
        return "numberPlayers="+maxPlayers+", registrationPort"+registrationPort+"...";
    }

}
