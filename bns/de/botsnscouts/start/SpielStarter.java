package de.botsnscouts.start;
//import de.botsnscouts.gui.*;
import de.botsnscouts.util.*;

public class SpielStarter{

    public boolean startSpiel(KommSpPr com, KachelRaster kachelRaster, String ip, int port, int anzahl, int zugTimeOut, int lisPort) throws OneFlagException, NichtZusSpfException{
	Ort dim = kachelRaster.getSpielfeldSize();
	int[][] flags = kachelRaster.getRFlaggen();
	String feld=kachelRaster.getSpielfeld();
	int retFromNewGame = com.newGame(ip, port, anzahl, 0, zugTimeOut, feld, flags[0], flags[1], dim.x, dim.y, lisPort);
	return (retFromNewGame!=1);
    }


    public boolean spielGehtLos(KommSpPr com, String ip, int port){
	return com.game(ip,port);
    }

}
