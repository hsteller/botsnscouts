/*
 * Created by dirk.
 * Date: Mar 10, 2002, 6:58:27 PM
 * Id: $Id$
 */

package de.botsnscouts.autobot;

import de.botsnscouts.util.Bot;
import de.botsnscouts.board.Board;
import de.botsnscouts.board.Floor;

public abstract class DistanceCalculator {

    Board board;

    public DistanceCalculator(Board board) {
        this.board = board;
    }

    public int getDistance(Bot robbi) {
        return getDistance(robbi, 0);
    }

    /*
     * Takes into account actual distance, flag, damage, repair possibility
     * and play strength.
     */
    public int getDistance(Bot robbi, int malus) {
        if (robbi.getDamage() == 10) {
            return 1000;
        }

        // If this path leads to winning -> do it!
        if (robbi.getNextFlag() == board.getFlags().length + 1) {
            return 0;
        }
        int distance = getDistance(robbi.getX(), robbi.getY(),
                robbi.getNextFlag(), robbi.getFacing());

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

    /**
     *  Gives the distance to the next flag, tahes into account whatever
     *  the subclass thinks necessary.
     */
    protected abstract int getDistance(int x, int y, int flag, int facing);

    /**
     * Does any necessary pre-calculations.
     */
    public abstract void calculateDistances();

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

}
