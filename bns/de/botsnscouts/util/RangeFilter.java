/*
 * Created on 18.05.2005
 *
 */
package de.botsnscouts.util;

/**
 *  @author hendrik
 *  @version $Id$
 *
 * This class can check (@link #isInRange(int)) whether an integer is in a certain integer interval.
 * It can be set to an "infinite mode" so that @link #isInRange(int) always returns true. 
 */

public  class RangeFilter {
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
