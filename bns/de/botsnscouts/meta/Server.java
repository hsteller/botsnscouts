package de.botsnscouts.meta;

import java.io.IOException;

/*
  *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2001-2004 scouties.                                    *
 * Contact botsnscouts-devel@sf.net                                *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 2 of the License.

 */

/**
 * @author Miriam
 */
public class Server {

    public static int PORT=8725;

    private static void usage() {
        System.err.println("Usage: java de.botsnscouts.meta.Server [-p PORT]");
    }

    public static void main(String[] argv) {

        int port = PORT;

        try {
            for (int i=0; i<argv.length; i++) {
                if (argv[i].equals("-p")) {
                    port = Integer.parseInt(argv[i+1]);
                    i++;
                }
            }
        } catch (Exception ex) {
            //Problem in parsing parameters.
            usage();
            System.exit(-1);
        }

        try {
            Listener listener = new Listener(port);
            listener.start();
        } catch (IOException ex) {
            //TODO
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}
