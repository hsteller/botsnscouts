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

package de.botsnscouts.autobot;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Category;

import de.botsnscouts.board.Board;
import de.botsnscouts.util.Directions;
import de.botsnscouts.util.Location;

/**
 * This class can calculate the distance a robot is from
 * the next flag. Does not take conveyor belts into account.
 * Id: $Id$
 */
public class SimpleDistanceCalculator extends DistanceCalculator {
    static final Category CAT = Category.getInstance(SimpleDistanceCalculator.class);

    private int[][][] distances;

    private SimpleDistanceCalculator(Board board) {
        super(board);
    }

    /**
     * Gives the distance to the next flag, tahes into account neccesary turns.
     */
    public int getDistance(int x, int y, int flag, int facing) {
        int m = flag - 1;

        if (distances[m][x][y] == 0) {
            return 0;
        }
        switch (facing) {
            case Directions.NORTH:
                if ((distances[m][x][y + 1] < distances[m][x][y]) && (!hasNorthWall(x, y))) {
                    return distances[m][x][y];
                }
                if (((distances[m][x - 1][y] < distances[m][x][y]) && (!hasWestWall(x, y)))
                                || ((distances[m][x + 1][y] < distances[m][x][y]) && (!hasEastWall(x, y)))) {
                    return distances[m][x][y] + 1;
                }
                return distances[m][x][y] + 2;

            case Directions.EAST:
                if ((distances[m][x + 1][y] < distances[m][x][y]) && (!hasEastWall(x, y))) {
                    return distances[m][x][y];
                }
                if (((distances[m][x][y - 1] < distances[m][x][y]) && (!hasSouthWall(x, y)))
                                || ((distances[m][x][y + 1] < distances[m][x][y]) && (!hasNorthWall(x, y)))) {
                    return distances[m][x][y] + 1;
                }
                return distances[m][x][y] + 2;

            case Directions.SOUTH:
                if ((distances[m][x][y - 1] < distances[m][x][y]) && (!hasSouthWall(x, y))) {
                    return distances[m][x][y];
                }
                if (((distances[m][x - 1][y] < distances[m][x][y]) && (!hasEastWall(x, y)))
                                || ((distances[m][x + 1][y] < distances[m][x][y]) && (!hasEastWall(x, y)))) {
                    return distances[m][x][y] + 1;
                }
                return distances[m][x][y] + 2;

            case Directions.WEST:
                if ((distances[m][x - 1][y] < distances[m][x][y]) && (!hasEastWall(x, y))) {
                    return distances[m][x][y];
                }
                if (((distances[m][x][y + 1] < distances[m][x][y]) && (!hasNorthWall(x, y)))
                                || ((distances[m][x][y - 1] < distances[m][x][y]) && (!hasSouthWall(x, y)))) {
                    return distances[m][x][y] + 1;
                }
                return distances[m][x][y] + 2;

        }
        CAT.error("reached unreachable!");
        return 10000;
        // never reached
    }

    /**
     * Initializes the distances-Array.
     */
    public void preCalculate() {
        Location[] flags = board.getFlags();
        distances = new int[flags.length][][];

        for (int m = 0; m < flags.length; ++m) {
            distances[m] = new int[board.getSizeX() + 2][board.getSizeY() + 2];

            for (int i = 0; i < board.getSizeX() + 2; ++i) {
                for (int j = 0; j < board.getSizeY() + 2; ++j) {
                    distances[m][i][j] = 9999;
                }
            }
        }
        for (int m = 0; m < flags.length; ++m) {
            distances[m][flags[m].x][flags[m].y] = 0;
            calculateDistancesForField(distances[m], flags[m].x, flags[m].y, flags[m].x, flags[m].y);
        }
    }

    private void calculateDistancesForField(int enftab[][], int u, int v, int x, int y) {
        if ((u == x) && (v == y)) {
            if ((!hasNorthWall(x, y)) && !floor(x, y + 1).isPit()) {
                enftab[x][y + 1] = enftab[x][y] + 1;
                calculateDistancesForField(enftab, x, y, x, y + 1);
            }
            if ((!hasWestWall(x, y)) && !floor(x - 1, y).isPit()) {
                enftab[x - 1][y] = enftab[x][y] + 1;
                calculateDistancesForField(enftab, x, y, x - 1, y);
            }
            if ((!hasEastWall(x, y)) && !floor(x + 1, y).isPit()) {
                enftab[x + 1][y] = enftab[x][y] + 1;
                calculateDistancesForField(enftab, x, y, x + 1, y);
            }
            if (!(hasSouthWall(x, y)) && !floor(x, y - 1).isPit()) {
                enftab[x][y - 1] = enftab[x][y] + 1;
                calculateDistancesForField(enftab, x, y, x, y - 1);
            }
        }

        if ((u == x) && (v != y)) {
            if (!hasNorthWall(x, y) && !floor(x, y + 1).isPit() && (enftab[x][y + 1] > enftab[x][y] + 1)) {
                enftab[x][y + 1] = enftab[x][y] + 1;
                calculateDistancesForField(enftab, x, y, x, y + 1);
            }
            if (!hasWestWall(x, y) && !floor(x - 1, y).isPit() && (enftab[x - 1][y] > enftab[x][y] + 2)) {
                enftab[x - 1][y] = enftab[x][y] + 2;
                calculateDistancesForField(enftab, x, y, x - 1, y);
            }
            if (!hasEastWall(x, y) && !floor(x + 1, y).isPit() && (enftab[x + 1][y] > enftab[x][y] + 2)) {
                enftab[x + 1][y] = enftab[x][y] + 2;
                calculateDistancesForField(enftab, x, y, x + 1, y);
            }
            if (!hasSouthWall(x, y) && !floor(x, y - 1).isPit() && (enftab[x][y - 1] > enftab[x][y] + 1)) {
                enftab[x][y - 1] = enftab[x][y] + 1;
                calculateDistancesForField(enftab, x, y, x, y - 1);
            }
        }

        if ((u != x) && (v == y)) {
            if (!hasNorthWall(x, y) && !floor(x, y + 1).isPit() && (enftab[x][y + 1] > enftab[x][y] + 2)) {
                enftab[x][y + 1] = enftab[x][y] + 2;
                calculateDistancesForField(enftab, x, y, x, y + 1);
            }
            if (!hasWestWall(x, y) && !floor(x - 1, y).isPit() && (enftab[x - 1][y] > enftab[x][y] + 1)) {
                enftab[x - 1][y] = enftab[x][y] + 1;
                calculateDistancesForField(enftab, x, y, x - 1, y);
            }
            if (!hasEastWall(x, y) && !floor(x + 1, y).isPit() && (enftab[x + 1][y] > enftab[x][y] + 1)) {
                enftab[x + 1][y] = enftab[x][y] + 1;
                calculateDistancesForField(enftab, x, y, x + 1, y);
            }
            if (!hasSouthWall(x, y) && !floor(x, y - 1).isPit() && (enftab[x][y - 1] > enftab[x][y] + 2)) {
                enftab[x][y - 1] = enftab[x][y] + 2;
                calculateDistancesForField(enftab, x, y, x, y - 1);
            }
        }
    }

    private static Map<Board, SimpleDistanceCalculator> uniqueInstances = new HashMap<Board, SimpleDistanceCalculator>();

    /**
     * Returns a reference to a SimpleDistanceCalculator. To be used instead
     * of constructor. Guarantees only one DCB (which is reusable) is initialized
     * per vm.
     */
    public static synchronized SimpleDistanceCalculator getInstance(Board board) {
        if (uniqueInstances.get(board) == null)
            uniqueInstances.put(board, new SimpleDistanceCalculator(board));
        return uniqueInstances.get(board);
    }

}
