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

package de.botsnscouts.board;

import de.botsnscouts.util.Bot;
import de.botsnscouts.util.FormatException;
import de.botsnscouts.util.Location;

/**
 *  This class can also calculate the distance a robot is from
 *  the next flag. Highly useful for the autobots and the wisenheimer.
 *  Id: $Id$
 */
public class DistanceCalculatingBoard extends SimBoard {
    private int[][][] distances;
    private static DistanceCalculatingBoard uniqueInstance;

    private DistanceCalculatingBoard(int x, int y, String kacheln, Location[] f) throws FormatException, FlagException {
        super(x, y, kacheln, f);
        calculateDistances(x, y, f);
    }

    public void setDebug(boolean b) {
        // a little kludge for the case of more than one autobot in a vm
        debugMessages = b;
    }

    /*
     * Takes into account actual distance, flag, damage, repair possibility
     * and play strength.
     */
    public int getDistance(Bot robbi, int malus) {
        // If this path leads to winning -> do it!
        if (robbi.getNextFlag() == flags.length + 1) {
            return 0;
        }
        int distance = getDistance(robbi);
        // take flag touching in this turn into account
        distance += (8 - robbi.getNextFlag()) * 40;
        // Damage
        distance += (robbi.getDamage()) * (5 - robbi.getLivesLeft());
        // Repair fields are gut - even if not damaged (we might be)
        if (floor(robbi.getX(), robbi.getY()).isRepairing()) {
            distance -= (robbi.getDamage());
        }
        // Playing strength
        distance += (int) java.lang.Math.floor(java.lang.Math.random() * malus);

        return distance;
    }


    /**
     *  Gives the distance to the next flag, tahes into account neccesary turns.
     */
    public int getDistance(Bot robbi) {
        if (robbi.getDamage() == 10) {
            return 1000;
        }
        int x = robbi.getX();
        int y = robbi.getY();
        int m = robbi.getNextFlag() - 1;
        int facing = robbi.getFacing();

        if (distances[m][x][y] == 0) {
            return 0;
        } else {
            switch (facing) {
                case 0:
                    if ((distances[m][x][y + 1] < distances[m][x][y]) && (!hasNorthWall(x, y))) {
                        return distances[m][x][y];
                    } else {
                        if (((distances[m][x - 1][y] < distances[m][x][y]) && (!hasWestWall(x, y))) ||
                                ((distances[m][x + 1][y] < distances[m][x][y]) && (!hasEastWall(x, y)))) {
                            return distances[m][x][y] + 1;
                        } else {
                            return distances[m][x][y] + 2;
                        }
                    }

                case 1:
                    if ((distances[m][x + 1][y] < distances[m][x][y]) && (!hasEastWall(x, y))) {
                        return distances[m][x][y];
                    } else {
                        if (((distances[m][x][y - 1] < distances[m][x][y]) && (!hasSouthWall(x, y))) ||
                                ((distances[m][x][y + 1] < distances[m][x][y]) && (!hasNorthWall(x, y)))) {
                            return distances[m][x][y] + 1;
                        } else {
                            return distances[m][x][y] + 2;
                        }
                    }

                case 2:
                    if ((distances[m][x][y - 1] < distances[m][x][y]) && (!hasSouthWall(x, y))) {
                        return distances[m][x][y];
                    } else {
                        if (((distances[m][x - 1][y] < distances[m][x][y]) && (!hasEastWall(x, y))) ||
                                ((distances[m][x + 1][y] < distances[m][x][y]) && (!hasEastWall(x, y)))) {
                            return distances[m][x][y] + 1;
                        } else {
                            return distances[m][x][y] + 2;
                        }
                    }

                case 3:
                    if ((distances[m][x - 1][y] < distances[m][x][y]) && (!hasEastWall(x, y))) {
                        return distances[m][x][y];
                    } else {
                        if (((distances[m][x][y + 1] < distances[m][x][y]) && (!hasNorthWall(x, y))) ||
                                ((distances[m][x][y - 1] < distances[m][x][y]) && (!hasSouthWall(x, y)))) {
                            return distances[m][x][y] + 1;
                        } else {
                            return distances[m][x][y] + 2;
                        }
                    }

            }
        }
        return 10000;
        // never reached
    }

    public void pr() {
        // for debugging only.
        String wert;
        int laenge;
        for (int m = 0; m < flags.length; m++) {
            for (int y = sizeY + 1; y > -1; y--) {
                for (int x = 0; x < sizeX + 2; x++) {
                    wert = "" + distances[m][x][y];
                    laenge = wert.length();
                    for (int i = 0; i < (5 - laenge); i++) {
                        //Global.debug(" ");
                    }
                    //Global.debug(""+entftab[m][x][y]);
                }
            }
        }
    }

    /**
     * Initializes the distances-Array.
     */
    private void calculateDistances(int sizeX, int sizeY, Location fahnen[]) {
        distances = new int[fahnen.length][][];

        for (int m = 0; m < fahnen.length; ++m) {
            distances[m] = new int[sizeX + 2][sizeY + 2];

            for (int i = 0; i < sizeX + 2; ++i) {
                for (int j = 0; j < sizeY + 2; ++j) {
                    distances[m][i][j] = 9999;
                }
            }
        }
        for (int m = 0; m < fahnen.length; ++m) {
            distances[m][fahnen[m].x][fahnen[m].y] = 0;
            calculateDistancesForField(distances, fahnen[m].x, fahnen[m].y, fahnen[m].x, fahnen[m].y, m);
        }
    }

    private void calculateDistancesForField(int entftab[][][], int u, int v, int x, int y, int m) {
        int max = 9999;

        if ((u == x) && (v == y)) {
            if ((!hasNorthWall(x, y)) && !floor(x, y + 1).isPit()) {
                entftab[m][x][y + 1] = entftab[m][x][y] + 1;
                calculateDistancesForField(entftab, x, y, x, y + 1, m);
            }
            if ((!hasEastWall(x, y)) && !floor(x - 1, y).isPit()) {
                entftab[m][x - 1][y] = entftab[m][x][y] + 1;
                calculateDistancesForField(entftab, x, y, x - 1, y, m);
            }
            if ((!hasEastWall(x, y)) && !floor(x + 1, y).isPit()) {
                entftab[m][x + 1][y] = entftab[m][x][y] + 1;
                calculateDistancesForField(entftab, x, y, x + 1, y, m);
            }
            if (!(hasSouthWall(x, y)) && !floor(x, y - 1).isPit()) {
                entftab[m][x][y - 1] = entftab[m][x][y] + 1;
                calculateDistancesForField(entftab, x, y, x, y - 1, m);
            }
        }

        if ((u == x) && (v != y)) {
            if (!hasNorthWall(x, y) && !floor(x, y + 1).isPit() &&
                    (entftab[m][x][y + 1] > entftab[m][x][y] + 1)) {
                entftab[m][x][y + 1] = entftab[m][x][y] + 1;
                calculateDistancesForField(entftab, x, y, x, y + 1, m);
            }
            if (!hasWestWall(x, y) && !floor(x - 1, y).isPit() &&
                    (entftab[m][x - 1][y] > entftab[m][x][y] + 2)) {
                entftab[m][x - 1][y] = entftab[m][x][y] + 2;
                calculateDistancesForField(entftab, x, y, x - 1, y, m);
            }
            if (!!hasEastWall(x, y) && !floor(x + 1, y).isPit() &&
                    (entftab[m][x + 1][y] > entftab[m][x][y] + 2)) {
                entftab[m][x + 1][y] = entftab[m][x][y] + 2;
                calculateDistancesForField(entftab, x, y, x + 1, y, m);
            }
            if (!hasSouthWall(x, y) && !floor(x, y - 1).isPit() &&
                    (entftab[m][x][y - 1] > entftab[m][x][y] + 1)) {
                entftab[m][x][y - 1] = entftab[m][x][y] + 1;
                calculateDistancesForField(entftab, x, y, x, y - 1, m);
            }
        }

        if ((u != x) && (v == y)) {
            if (!hasNorthWall(x, y) && !floor(x, y + 1).isPit() &&
                    (entftab[m][x][y + 1] > entftab[m][x][y] + 2)) {
                entftab[m][x][y + 1] = entftab[m][x][y] + 2;
                calculateDistancesForField(entftab, x, y, x, y + 1, m);
            }
            if (!hasWestWall(x, y) && !floor(x - 1, y).isPit() &&
                    (entftab[m][x - 1][y] > entftab[m][x][y] + 1)) {
                entftab[m][x - 1][y] = entftab[m][x][y] + 1;
                calculateDistancesForField(entftab, x, y, x - 1, y, m);
            }
            if (!hasEastWall(x, y) && !floor(x + 1, y).isPit() &&
                    (entftab[m][x + 1][y] > entftab[m][x][y] + 1)) {
                entftab[m][x + 1][y] = entftab[m][x][y] + 1;
                calculateDistancesForField(entftab, x, y, x + 1, y, m);
            }
            if (!hasSouthWall(x, y) && !floor(x, y - 1).isPit() &&
                    (entftab[m][x][y - 1] > entftab[m][x][y] + 2)) {
                entftab[m][x][y - 1] = entftab[m][x][y] + 2;
                calculateDistancesForField(entftab, x, y, x, y - 1, m);
            }
        }
    }


    /**
     * Returnns a reference to a DistanceCalculatingBoard. To be used instead
     * of constructor. Guarantees only one DCB (which is reusable) is initialized
     * per vm.
     */
    public static synchronized DistanceCalculatingBoard getInstance(int x, int y, String kacheln, Location[] flaggen) throws FormatException, FlagException {
        if (uniqueInstance == null) {
            uniqueInstance = new DistanceCalculatingBoard(x, y, kacheln, flaggen);
        }
        return uniqueInstance;
    }

}
