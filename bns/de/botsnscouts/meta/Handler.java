package de.botsnscouts.meta;

import nanoxml.XMLElement;
import nanoxml.XMLParseException;

import java.net.Socket;
import java.io.*;

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
 * Handles an incoming request.
 * @author miriam
 */
public class Handler extends Thread {

    private Reader reader;
    private Writer writer;

    Handler(Socket s) throws IOException {
        reader = new InputStreamReader(s.getInputStream());
        writer = new OutputStreamWriter(s.getOutputStream());
    }

    private void delegate(XMLElement request) throws IOException {

        XMLElement answer;

        try {
            if (request.getName().equals("game")) {
                answer = MetaServer.getInstance().announce(request);
                answer.write(writer);
                if (answer.getName().equals("announced")) {
                    try {
                        XMLElement revoke = new XMLElement();
                        revoke.parseFromReader(reader);
                        /* Do not care what is actually sent: We will revoke the announced game
                           in either situation.
                        */
                    } catch (IOException ex) {
                        //TODO
                        ex.printStackTrace();
                    } catch (XMLParseException ex) {
                        //TODO
                        ex.printStackTrace();
                    } finally {
                        MetaServer.getInstance().revoke(request);
                    }
                }
            } else {
                if (request.getName().equals("query"))

                    answer = MetaServer.getInstance().query(request);
                else if (request.getName().equals("stat"))
                    answer = MetaServer.getInstance().stat(request);
                else {
                    // Unknown Request
                    answer = MetaServer.getInstance().createInvalidXML("Unknown Request");
                }
                System.out.println("Sending answer " + answer);
                answer.write(writer);
            }
        } catch (XMLParseException ex) {
            // Incorrect behaviour.
            answer = new XMLElement();
            answer.setName("invalid");
            answer.setContent("Non-XML request.");
            answer.write(writer);
        }
    }


    public void run() {
        try {
            XMLElement request = new XMLElement();
            request.parseFromReader(reader);
            delegate(request);

            writer.flush();
            reader.close();
            writer.close();
        } catch (IOException ex) {
            //TODO
            ex.printStackTrace();
        }
    }
}
