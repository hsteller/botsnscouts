package de.botsnscouts.meta;

import nanoxml.XMLElement;
import nanoxml.XMLParseException;

import java.io.*;
import java.util.Vector;
import java.util.Iterator;
import java.net.Socket;
import java.net.UnknownHostException;

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
 * @author miriam
 */
public class MetaServer {

    private static MetaServer theInstance = new MetaServer();

    /** Maintaining statistics about no of successfully announced games */
    private int announcedGames = 0;
    /** Maintaining statistics about requests for announcing that were not handled due to errors*/
    private int announceFailures = 0;
        /** Maintaining statistics about no of queries.*/
    private int queries = 0;
    /** Maintaining statistics about asking for server statistics */
    private int statRequests = 0;

    /**
     * Currently announced games.
     * Each entry is a XMLElement.
     */
    private Vector games = new Vector();

    private MetaServer() {

    }

    private void debug(String s) {
       System.out.println(s);
    }

    static MetaServer getInstance() {
       return theInstance;
    }

    /** Request to announce a game.
     *  Will only be fullfilled after checking that the named server is really a BNS server and
     *  that it is accessible.
     * @param request XMLElement describing the game that shall be announced.
     * @return  answer to be send back to the client, either ok or failure.
     * @throws IOException
     */
    XMLElement announce( XMLElement request ) throws IOException {
        debug("announce  game received");
        XMLElement answer;
        /* Check, if accessible. */
        try {
            String host = request.getStringAttribute("host");
            int port = Integer.parseInt(request.getStringAttribute("port"));
            if (checkBNSServer(host, port)) {
                /* If ok, announnce. */
                games.add( request );
                answer = new XMLElement();
                answer.setName("announced");
            } else {
                announceFailures++;
                answer = new XMLElement();
                answer.setName("fail");
                answer.setAttribute("reason", "noBNSServerAccessible");
            }
        } catch (XMLParseException ex) {
            answer = createInvalidXML(ex.getMessage());
        } catch (NumberFormatException ex) {
            answer = createInvalidXML("Invalid port number");
        }
        return answer;
    }

    /**
     * Check wether there is really a BNS server running at the given address.
     * And wether this address is accessible via the internet.
     *
     * Implementation note: Code from KommClient is not reused on purpose. because
     * it sucks. Additionally, the coupling to BNS classes shall be low because
     * maybe this code will serve as example for implementing the meta server
     * in another programming language.
     *
     * @param host hostname that is said to host a BNS server
     * @param port port number of this server
     * @return wether a BNS server is accessible at the given port of the given host.
     */
    private boolean checkBNSServer(String host, int port) {
        try {
            debug("Host "+host+", port "+port);
            Socket socket = new Socket(host,port);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true);
            out.println("RA2(metaservercheck)");
            //TODO: Add timeout for not beeing blocked by malicious clients.
            String answer = in.readLine();
            /* Sign off again for not having to listen to all the game's traffic. */
            out.println("RLE(metaservercheck)");
            socket.close();
            return answer.equalsIgnoreCase("OK");
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    XMLElement createInvalidXML(String s) {
        XMLElement answer = new XMLElement();
        answer.setName("invalid");
        answer.setContent(s);
        return answer;
    }

    /** Handle a request that queries running games. */
    XMLElement query( XMLElement request ) throws IOException {
        debug("query received.");
        queries++;
        //TODO: (?) Synchronization
        XMLElement list = new XMLElement();
        list.setName("games");
        for (Iterator it=games.iterator(); it.hasNext(); ) {
            XMLElement game = (XMLElement )it.next();
            list.addChild(game);
        }
        return list;
    }

    /** Handle request about server statistics*/
    XMLElement stat( XMLElement request ) throws IOException {
        debug("stat received");
        statRequests++;
        XMLElement statistics = new XMLElement();
        statistics.setName("statistics");
        statistics.addChild( createStatXML("announcements", announcedGames) );
        statistics.addChild( createStatXML("announce-failures", announceFailures));
        statistics.addChild( createStatXML("queries", queries) );
        statistics.addChild( createStatXML("stats", statRequests) );
        return statistics;
    }

    private XMLElement createStatXML(String tagname, int value) {
        XMLElement xml = new XMLElement();
        xml.setName(tagname);
        xml.setIntAttribute("no", value);
        return xml;
    }

    /** No longer announce the game orginally announced by this announce-request*/
    public void revoke(XMLElement request) {
        //TODO: implement more efficiently
        games.remove(request);
    }
}
