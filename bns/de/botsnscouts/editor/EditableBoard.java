package de.botsnscouts.editor;

import de.botsnscouts.board.*;
import de.botsnscouts.util.Ort;
import de.botsnscouts.util.FormatException;

class EditableBoard extends de.botsnscouts.board.SpielfeldSim {
    EditableBoard(int x, int y, String kacheln, Ort[] flaggen) throws FormatException, FlaggenException {
        super(x, y, kacheln, flaggen, null);
    }

    void setNorthPusher( int x, int y, int phases ) {
        setHWall( x, y, getHWall(x, y).getWithNWPusher( phases ) );
    }
    void setWestPusher( int x, int y, int phases ) {
        setVWall( x, y, getVWall(x, y).getWithNWPusher( phases ) );
    }
    void setSouthPusher( int x, int y, int phases ) {
        setHWall( x, y, getHWall(x, y).getWithSEPusher( phases ) );
    }
    void setEastPusher( int x, int y, int phases ) {
        setVWall( x, y, getVWall(x, y).getWithSEPusher( phases ) );
    }

    void setNorthLaser( int x, int y, int strength ) {
        setHWall( x, y, getHWall(x, y).getWithNWLaser( strength ) );
    }
    void setWestLaser( int x, int y, int strength ) {
        setVWall( x, y, getVWall(x, y).getWithNWLaser( strength ) );
    }
    void setSouthLaser( int x, int y, int strength ) {
        setHWall( x, y, getHWall(x, y).getWithSELaser( strength ) );
    }
    void setEastLaser( int x, int y, int strength ) {
        setVWall( x, y, getVWall(x, y).getWithSELaser( strength ) );
    }

    void removeVWall( int x, int y ) {
        setVWall( x, y, Wall.getNonExistingWall() );
    }
    void removeHWall( int x, int y ) {
        setHWall( x, y, Wall.getNonExistingWall() );
    }
    void addVWall( int x, int y ) {
        setVWall( x, y, Wall.getEmptyWall() );
    }
    void addHWall( int x, int y ) {
        setHWall( x, y, Wall.getEmptyWall() );
    }


    // Floor-Stuff
    void clearFloor(int x, int y) {
        setFloor(x, y, Floor.getEmptyFloor() );
    }

    void setFloor( int x, int y, int type, int info ) {
        setFloor( x, y, Floor.getFloor( type, info ) );
    }

    void setCrusher( int x, int y, int phases ) {
        setFloor( x, y, getFloor(x,y).getWithCrusher( phases ) );
    }
}
