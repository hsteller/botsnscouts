/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2005 scouties.                                *
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

/*
 * Created on 18.05.2005
 *
 */
package de.botsnscouts.util;

/**
 * @author hendrik
 * @version $Id$
 * 
 *          This class can check (@link #isInRange(int)) whether an integer is in a certain integer interval. It can be set to an "infinite mode" so that @link #isInRange(int) always returns true.
 */

public class RangeFilter {
    private int type;

    private int min;

    private int max;

    public static final int INFINITE = 0;

    public static final int BOUND = 1;

    public RangeFilter() {
        type = INFINITE;
        min = 0;
        max = 0;
    }

    public RangeFilter(int mini, int maxi) {
        type = BOUND;
        min = mini;
        max = maxi;
    }

    public int getMinimum() {
        return min;
    }

    public int getMaximum() {
        return max;
    }

    public void setMinimum(int n) {
        min = n;
    }

    public void setMaximum(int n) {
        max = n;
    }

    public boolean hasCorrectBounds() {
        return min <= max;
    }

    public void setTypInfinite() {
        type = INFINITE;
    }

    public boolean isTypInfinite() {
        return type == INFINITE;
    }

    public void setTypBound(int mini, int maxi) {
        type = BOUND;
        min = mini;
        max = maxi;
    }

    public boolean isInRange(int i) {
        if (isTypInfinite())
            return true;
        else
            return (i >= min && i <= max);

    }

}
