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
 
package de.botsnscouts.start;
import de.botsnscouts.gui.*;
import de.botsnscouts.util.*;
import de.botsnscouts.autobot.*;
import org.apache.log4j.Category;

// launches human player, output ...

public class Launcher{

    static final Category CAT = Category.getInstance( Launcher.class );
    // launches output
    public Thread einemSpielZuschauen(String ip, int port, boolean noSplash){
	Thread ret;
	try{
	    ret=new BNSThread(new Ausgabe(ip, port,null,noSplash));
	    ret.start();
	}catch(Exception exp){
	    return null;
	}
	return ret;
    }

    // launches human player
    public Thread amSpielTeilnehmen(String ip, int port, String name, int farbe, boolean noSplash){
	Thread ret;
	try {
	    if( CAT.isDebugEnabled() ) CAT.debug("Trying to start human player...");
	    ret=(Thread) new HumanPlayer(ip,port,name,farbe,noSplash);
	    ret.start();
	} catch (Exception u){
	    return null;
	}
	return ret;
    }

    // launch autobots
    public Thread  kuenstlicheSpielerStarten(String ip, int port, boolean local, int iq, KommSpPr com){
	if (local){
	    Thread ks;
	    ks = new SpielerKuenstlich(ip,port,iq);
	    if(ks!=null){
		ks.start();
		ks.setPriority(java.lang.Thread.MIN_PRIORITY);
	    }
	    return ks;
	}else{
	    if(com.newKS(ip,port,1,iq))
		return null;
	    else
		return null;
	}
    }

    public boolean startGame(KommSpPr com, TileRaster tileRaster, String ip, int port, int pnum, int timeOut, int lisPort) throws OneFlagException, NichtZusSpfException{
	Ort dim = tileRaster.getSpielfeldSize();
	int[][] flags = tileRaster.getRFlaggen();
	String field=tileRaster.getSpielfeld();
	int retFromNewGame = com.newGame(ip, port, pnum, 0, timeOut, field, flags[0], flags[1], dim.x, dim.y, lisPort);
	return (retFromNewGame!=1);
    }


    public boolean spielGehtLos(KommSpPr com, String ip, int port){
	return com.game(ip,port);
    }

}
