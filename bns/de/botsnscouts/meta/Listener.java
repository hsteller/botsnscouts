package de.botsnscouts.meta;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

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
 * Listen for requests.
 * @author miriam
 */
public class Listener extends Thread {

    private ServerSocket serverSocket;

    Listener( int port ) throws IOException {

        serverSocket = new ServerSocket(port);

    }

    public void run() {
        while (true) {
            try {
                Socket sock = serverSocket.accept();
                (new Handler(sock)).start();
            } catch (IOException ex) {
                //TODO:
                ex.printStackTrace();
            }
        }
    }

}
