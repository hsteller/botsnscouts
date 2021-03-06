package de.botsnscouts.start;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import nanoxml.XMLElement;
import nanoxml.XMLParseException;

import org.apache.log4j.Category;

import de.botsnscouts.util.Conf;
import de.botsnscouts.util.KrimsKrams;
import de.botsnscouts.util.Location;

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

    static final String DHOST;

    static final int DPORT;// =8077;

    static final int DPLAYERS;// =8;

    static final int DTO;// =200;

    static final boolean PUSHERS_PUSH_MULTIPLE_BOTS;

    private final static Category CAT = Category.getInstance(GameOptions.class);

    static {
        String stmp;
        // int tmp;
        stmp = Conf.getProperty("server.IP");
        try {
            stmp = (stmp == null ? InetAddress.getLocalHost().getHostAddress() : stmp);
        }
        catch (UnknownHostException ex) {
            CAT.info("Could not determine IP address for this computer, using localhost/127.0.0.1");
            stmp = "127.0.0.1";
        }
        DHOST = stmp;
        DPORT = Conf.getIntProperty("server.port", 8077);
        DPLAYERS = Conf.getIntProperty("players", 8);
        DTO = Conf.getIntProperty("timeout", 200);
        PUSHERS_PUSH_MULTIPLE_BOTS = Conf.getBooleanProperty("canPushersPushMutltipleBots", false);

    }

    /** Max number of players for this game */
    private int maxPlayers = DPLAYERS;

    /** TCP/IP port for registration */
    private int registrationPort = DPORT;

    /** Tiwmeout in ms for handing in cards. */
    private int handInTimeout = 1000 * DTO;

    /**
     * The board for the game. Represented as a string in the fucking old board format.
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
     * Allow the players to get help from Scout in this game.
     */
    private boolean allowScout = true;

    /**
     * A comment describing the game that will be announced with the game.
     */
    private String comment;

    /**
     * A name for the game
     */
    private String name = KrimsKrams.randomName();

    /** Game hosted by */
    private String invitor;

    /** The server hosting the game */
    private String host;

    /**
     * Set the relevant information later.
     */
    GameOptions() {
    }

    /**
     * 
     * @param noPlayers
     *            Max number of players for this game
     * @param port
     *            TCP/IP port for registration
     * @param timeout
     *            Timeout in ms for handing in cards.
     * @param board
     *            The board for the game.
     * @param flags
     *            Location of the flags
     * @param x
     *            "First coordinate of the board's dimension"
     * @param y
     *            "Second coordinate of the board's dimension"
     */
    public GameOptions(String name, String invitor, int noPlayers, int port, int timeout, String board,
                    Location[] flags, int x, int y) {
        this.name = name;
        this.invitor = invitor;
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
     * @param noPlayers
     *            Max number of players for this game
     * @param port
     *            TCP/IP port for registration
     * @param timeout
     *            Timeout in ms for handing in cards.
     * @param board
     *            The board for the game.
     * @param flags
     *            Location of the flags
     * @param x
     *            "First coordinate of the board's dimension"
     * @param y
     *            "Second coordinate of the board's dimension"
     */
    public GameOptions(String name, String invitor, int noPlayers, int port, int timeout, String board,
                    Location[] flags, int x, int y, boolean allowScout, boolean allowWisenheimer) {
        this(name, invitor, noPlayers, port, timeout, board, flags, x, y);
        this.allowWisenheimer = allowWisenheimer;
        this.allowScout = allowScout;
    }

    /**
     * 
     * @param noPlayers
     *            Max number of players for this game
     * @param port
     *            TCP/IP port for registration
     * @param timeout
     *            Timeout in ms for handing in cards.
     * @param board
     *            The board for the game.
     * @param flags
     *            Location of the flags
     * @param x
     *            "First coordinate of the board's dimension"
     * @param y
     *            "Second coordinate of the board's dimension"
     * @param comment
     *            Description of the game
     * @param name
     *            A name for the game
     * @param invitor
     *            the name of the human host who started the game
     */
    public GameOptions(String name, String invitor, int noPlayers, int port, int timeout, String board,
                    Location[] flags, int x, int y, boolean allowScout, boolean allowWisenheimer, String comment) {
        this(name, invitor, noPlayers, port, timeout, board, flags, x, y, allowScout, allowWisenheimer);
        this.comment = comment;
        this.name = name;
        this.invitor = invitor;
    }

    /**
     * 
     * @return Max number of players for this game
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
     * @return Timeout in ms for handing in cards.
     */
    public int getHandInTimeout() {
        return handInTimeout;
    }

    /**
     * 
     * @return The board for the game.
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

    /**
     * @param handInTimeout
     *            Timeout in ms for handing in cards.
     */
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

    //
    public XMLElement toXML() {
        XMLElement xml = new XMLElement();
        xml.setName("game");
        if (name != null) {
            xml.setAttribute("name", name);
        }
        if (invitor != null) {
            xml.setAttribute("invitor", invitor);
        }
        xml.setIntAttribute("port", registrationPort);
        xml.setAttribute("allowWisenheimer", allowWisenheimer ? "true" : "false");
        xml.setAttribute("allowScout", allowScout ? "true" : "false");
        if (comment != null) {
            xml.setAttribute("comment", comment);
        }
        XMLElement boardElement = new XMLElement();
        boardElement.setName("board");
        boardElement.setAttribute("field", board);
        boardElement.setIntAttribute("x", x);
        boardElement.setIntAttribute("y", y);
        for (int i = 0; i < flags.length; i++) {
            XMLElement flag = new XMLElement();
            flag.setName("flag");
            flag.setIntAttribute("no", i);
            flag.setIntAttribute("x", flags[i].getX());
            flag.setIntAttribute("y", flags[i].getY());
            boardElement.addChild(flag);
        }
        xml.addChild(boardElement);
        return xml;
    }

    /**
     * Parses an xml-games-construct into a hashtable.
     * 
     * @return A hashtable mapping game names to objects of type GameOptions.
     */
    @SuppressWarnings("unchecked")
    static HashMap<String, GameOptions> parseXMLGames(XMLElement xml) {
        HashMap<String, GameOptions> map = new HashMap<String, GameOptions>();

        assertXMLTagName(xml, "games");
        for (Iterator<XMLElement> it = xml.getChildren().iterator(); it.hasNext();) {
            XMLElement game = it.next();
            assertXMLTagName(game, "game");
            Vector<XMLElement> boardElems = game.getChildren();
            if (boardElems.size() != 1) {
                throw new XMLParseException("game", "Must have exactly one child board");
            }
            XMLElement board = boardElems.elementAt(0);
            Vector<XMLElement> flagElements = board.getChildren();
            Location[] flags = new Location[flagElements.size()];
            for (Iterator<XMLElement> flagIt = flagElements.iterator(); flagIt.hasNext();) {
                XMLElement flag = flagIt.next();
                assertXMLTagName(flag, "flag");
                flags[flag.getIntAttribute("no")] = new Location(flag.getIntAttribute("x"), flag.getIntAttribute("y"));
            }

            GameOptions gameOptions = new GameOptions(game.getStringAttribute("name"),
                            game.getStringAttribute("invitor"), DPLAYERS, game.getIntAttribute("port"), DTO,
                            board.getStringAttribute("field"), flags, board.getIntAttribute("x"),
                            board.getIntAttribute("y"), Boolean.valueOf(game.getStringAttribute("allowWisenheimer"))
                                            .booleanValue(), Boolean.valueOf(
                                            game.getStringAttribute("allowWisenheimer")).booleanValue(),
                            game.getStringAttribute("comment"));
            gameOptions.setHost(game.getStringAttribute("host"));
            if (!map.containsKey(gameOptions.name)) {
                map.put(gameOptions.name, gameOptions);
            }
            else {
                CAT.info("Ignoring game named " + gameOptions.name + " introduced by " + gameOptions.invitor
                                + "because we already know a game by that name.");
            }
        }
        return map;
    }

    private void setHost(String host) {
        this.host = host;
    }

    private static void assertXMLTagName(XMLElement xml, String name) {
        if (!xml.getName().equals(name))
            throw new XMLParseException(xml.getName(), "Expected tag: " + name);
    }

    public void setInvitor(String name) {
        invitor = name;
    }

    public String getHost() {
        return host;
    }

    public String getInvitor() {
        return invitor;
    }

    public boolean arePushersAbleToPushMultipleBots() {
        return PUSHERS_PUSH_MULTIPLE_BOTS;
    }

}
