package de.botsnscouts.start;

import de.botsnscouts.util.Location;
import de.botsnscouts.util.Conf;

/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2001-2004 scouties.                               *
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

    static final String DIP;//="127.0.0.1";
    static final int DPORT;//=8077;
    static final int DPLAYERS;//=8;
    static final int DTO;//=200;

    static {
        String stmp;
        int tmp;
        stmp = Conf.getProperty("server.IP");
        DIP = (stmp == null ? "127.0.0.1" : stmp);
        tmp = Conf.getIntProperty("server.port");
        DPORT = (tmp == -1 ? 8077 : tmp);
        tmp = Conf.getIntProperty("players");
        DPLAYERS = (tmp == -1 ? 8 : tmp);
        tmp = Conf.getIntProperty("timeout");
        DTO = (tmp == -1 ? 200 : tmp);
    }

    /** Max number of players for this game */
    private int maxPlayers = DPLAYERS;

    /** TCP/IP port for registration */
    private int registrationPort = DPORT;

    /** Tiwmeout in ms for handing in cards. */
    private int handInTimeout = 1000*DTO;

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
     * Allow the players to get help from wisenhimer in this game.
     */
    private boolean allowWisenheimer = true;

    /**
     *  Allow the players to get help from Scout in this game.
     */
    private boolean allowScout = true;

    /**
     * A comment describing the game that will be announced with the game.
     */
    private String comment;



    /**
     * Set the relevant information later.
     */
    GameOptions() {
    }

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
    public GameOptions(int noPlayers,
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
     * @param noPlayers  Max number of players for this game
     * @param port TCP/IP port for registration
     * @param timeout Timeout in ms for handing in cards.
     * @param board   The board for the game.
     * @param flags   Location of the flags
     * @param x        "First coordinate of the board's dimension"
     * @param y        "Second coordinate of the board's dimension"
     */
    public GameOptions(int noPlayers,
                       int port,
                       int timeout,
                       String board,
                       Location[] flags,
                       int x,
                       int y,
                       boolean allowScout,
                       boolean allowWisenheimer) {
        this(noPlayers, port, timeout, board, flags, x, y);
        this.allowWisenheimer = allowWisenheimer;
        this.allowScout = allowScout;
    }

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
    public GameOptions(int noPlayers,
                       int port,
                       int timeout,
                       String board,
                       Location[] flags,
                       int x,
                       int y,
                       boolean allowScout,
                       boolean allowWisenheimer,
                       String comment) {
        this(noPlayers, port, timeout, board, flags, x, y, allowScout, allowWisenheimer);
        this.comment = comment;
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

    public boolean isAllowScout() {
        return allowScout;
    }

    public boolean isAllowWisenheimer() {
        return allowWisenheimer;
    }

    public String getComment() {
        return comment;
    }

    void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    void setRegistrationPort(int registrationPort) {
        this.registrationPort = registrationPort;
    }

    void setHandInTimeout(int handInTimeout) {
        this.handInTimeout = handInTimeout;
    }

    void setBoard(String board) {
        this.board = board;
    }

    void setFlags(Location[] flags) {
        this.flags = flags;
    }

    void setX(int x) {
        this.x = x;
    }

    void setY(int y) {
        this.y = y;
    }

    void setAllowWisenheimer(boolean allowWisenheimer) {
        this.allowWisenheimer = allowWisenheimer;
    }

    void setAllowScout(boolean allowScout) {
        this.allowScout = allowScout;
    }

    void setComment(String comment) {
        this.comment = comment;
    }

    public String toString() {
        return "numberPlayers=" + maxPlayers + ", registrationPort" + registrationPort + "...";
    }

}
