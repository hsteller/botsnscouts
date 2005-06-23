/*
 * Created by dirk.
 * Date: Mar 10, 2002, 6:58:27 PM
 * Id: $Id$
 */

package de.botsnscouts.autobot;

import de.botsnscouts.board.Board;
import de.botsnscouts.board.Floor;
import de.botsnscouts.util.Bot;

public abstract class DistanceCalculator {

    Board board;

    public DistanceCalculator(Board board, boolean callPreCalculate) {
        this.board = board;
        if (callPreCalculate) {
            preCalculate();
        }
    }

    public DistanceCalculator(Board board){
        this(board, true);
    }
    
    /*
     * Takes into account actual distance, flag, damage, repair possibility
     * and play strength. The lower the return value is, the better.
     */
    public int getGoodness(Bot robbi) {
        return getGoodness(robbi, 0);
    }

    /** Also takes a malus into account
     */
    public int getGoodness(Bot robbi, int malus) {
        if (robbi.getDamage() == 10) {
            return 1000;
        }

        // If this path leads to winning -> do it!
        if (robbi.getNextFlag() == board.getFlags().length + 1) {
            return 0;
        }
        int distance = getDistance(robbi) / getScalingFactor();

        // take flag touching into account - simply, the farther we get
        // the better the value will be
        distance += (8 - robbi.getNextFlag()) * 40;

        // Damage
        distance += (robbi.getDamage()) * (5 - robbi.getLivesLeft());

        // Repair fields are good - even if not damaged (we might be)
        Floor floor = floor(robbi.getX(), robbi.getY());
        if (floor.isRepairing())
            distance -= (floor.getInfo());

        // Playing strength
        distance += (int) Math.floor(Math.random() * malus);

        return distance;
    }

    /** Gives the "raw" distance, without fancier mechanisms */
    public int getDistance(Bot robbi) {
        return getDistance(robbi.getX(), robbi.getY(),
                robbi.getNextFlag(), robbi.getFacing());
    }

    /**
     *  Gives the distance to the next flag, tahes into account whatever
     *  the subclass thinks necessary.
     */
    protected abstract int getDistance(int x, int y, int flag, int facing);

    /**
     * Does any necessary pre-calculations.
     */
    public abstract void preCalculate();

    // if subclasses have larger "distances"
    int getScalingFactor(){ return 1; }

    /// Utility functions for subclasses. Delegate to board.

    boolean hasNorthWall(int x, int y) {
        return board.hasNorthWall(x, y);
    }
    boolean hasEastWall(int x, int y) {
        return board.hasEastWall(x, y);
    }
    boolean hasSouthWall(int x, int y) {
        return board.hasSouthWall(x, y);
    }
    boolean hasWestWall(int x, int y) {
        return board.hasWestWall(x, y);
    }
    Floor floor(int x, int y) {
        return board.floor(x, y);
    }

    /* final so it may be inlined */
    final int rotateright(int facing) {    
        return (facing+1)%4;
    }
    final int rotateleft(int facing) {
        if (facing==0) {
            return 3;
        }    
        else {
            return --facing;
        }
        
    }

}
