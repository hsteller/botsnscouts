package de.botsnscouts.gui;



import java.awt.Point;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import de.botsnscouts.util.Global;

/*
 * Created on 07.09.2005
 *
 */

/**
 * @author Hendrik Steller
 * @version $Id$
 */
public class ScrollHelper {

    
  /** This method is a helper method to calculate a list of points on a straight line
   *   between two points. 
   * 
   * @param startpoint One of the endpoints of the straight line
   * @param endpoint The other endpoint of the straight line
   * @param offset the maximum number of pixels to move in one step (horizontal or vertical)
   * @return a list of Points between startpoint and endpoint; the last entry will alway be a copy of <code>endpoint</code>
   */
    public static Point [] calculateDiagonalScrollSteps(Point startpoint, Point endpoint, int offset) {
        Point currentUpperLeft = new Point(startpoint);
        Point newUpperLeft = new Point(endpoint);
        // I intend to use the functional equation of the straight line between
        // the two points to calculate the intermediate points;
        // there is the problem of Java having (0,0) in the upper left of the screen 
        // instead of bottem left; correcting my calculations will be easier if
        // I move that it goes through (0,0)
        // => I will set "startpoint" to (0,0) and have to move the coordinates
        // of "endpoint" accordingly: 
        int transX = currentUpperLeft.x;
        int transY = currentUpperLeft.y;
        newUpperLeft.x -=transX;
        newUpperLeft.y -=transY;
        currentUpperLeft.x =0;
        currentUpperLeft.y = 0;
        // okay, now start- and endpoint are moved by transX and transY;
        // moving back happenns by adding transX and transY to the coordinates
        // of the Points that will be added to the Point[] to be returned
        int ydistance = newUpperLeft.y-currentUpperLeft.y;
        int xdistance = newUpperLeft.x-currentUpperLeft.x;	            
        boolean isYinc = ydistance>=0; // check if we  increase y (==if we scroll down)
        boolean isXinc = xdistance>=0; // check if we  increase x (==if we scroll to the right)
        
        // depending on the moving directions we will either have to add or
        // subtract "offset" to the coordinates of "startpoint" to get to "endpoint"
        // (or not changing one of them at all if we only scroll horizontal XOR only vertical)
        int horizontalOffset = 0;
        int verticalOffset = 0;       
        if (!isXinc) {
            horizontalOffset=-offset;
        }
        else if (xdistance>0)  {
            horizontalOffset = offset;
        }
        if (!isYinc) {
            verticalOffset=-offset;
        }
        else if (ydistance>0)  {
            verticalOffset = offset;
        }
        
        // checking whether the horizontal or the vertical scrolling distance is greater:
        // (needed, because we have to calculate the number of steps for the maximum
        //  distance if each step is supposed to be <="offset" pixels):
        ydistance = Math.abs(ydistance);
        xdistance = Math.abs(xdistance);	           
        boolean xIsLonger = xdistance>=ydistance;
        float maxDistance = xIsLonger?xdistance:ydistance;
        // rounding up, because otherwise the last step might be greater than offset;
        // rounding up ensures that the last step will be <=offset
        int numOfSteps = (int)  Math.ceil(maxDistance/offset);        
        Point [] scrollPoints = new Point[numOfSteps];
        if (numOfSteps==0){ // hack: hatte eine IndexOutOfBoundsEx mit scrollPoints[-1]
            return new Point [] { new Point(endpoint)};
        }
            
            
            // end of the loop over the steps; is one less than numOfSteps because as coordinates
            // of the last step we can will simply use "endpoint"            
            int end = numOfSteps-1;
            int x = currentUpperLeft.x; // shorter to write ;-)
            int y = currentUpperLeft.y;         
           
            if (xdistance==0) { // an easy special case: we move only vertical
                // => we can't (and don't need to)  use the line equation below  as "m" (see below)
                 // would be infinite
                for (int i=0;i<end;i++){
                    y+=verticalOffset;
                    scrollPoints[i]= new Point(x+transX,y+transY);                      
                }
                scrollPoints[end]=new Point(endpoint);
                return scrollPoints;
            }
            
            
            // we will now create the equation/function for the straight line between newUpperLeft and 
            // currentUpperLeft as we need to find the closest y-value to any x-value between
            // currentUpperLeft.x and newUpperLeft.x    
            // Definition (explanation of later variable names):
            // currentUpperLeft := (a,b)
            //     newUpperLeft := (c,d)
            //
            float bMinusD  = -ydistance; // (already calculated above for finding the max distance)
            float aMinusC  = -xdistance; // (already calculated above for finding the max distance)
            float m = bMinusD/aMinusC; // slope of the line (german: Steigung der Gerade)
            // n (the y-axis intercept is zero  because of the above transformation of 
            // "startpoint" to the point of origin (0,0))           
            
            if (xIsLonger) { // "sane" case: we move along the x-axis and use the line equation  
                                  // to calculate the y-values
                for (int i=0;i<end;i++){
                    x +=horizontalOffset;
                    y = (int) (m*x);
                    if (isXinc != isYinc) { 
                        // this is the fix for Java having the point of origin in the upper left corner
                        y = -y;
                    }                    
                    // creating the new intermediate scrollpoint, undo of the startpoint->(0,0) transformation 
                    scrollPoints[i]= new Point(x+transX,y+transY);                    
                }
            }
            else { // "not-so-sane-case":             
                // we need to calculate the inverse function because we use the ydistance and need
                // to find x values for  our y values:
                // x=(g(x)-n)/m=(y-n)/m
                // => x = y/m  as n==0 for us                  
                for (int i=0;i<end;i++){
                    y+=verticalOffset;	                    	         
                    x = (int) (y/m);
                    if (isXinc!=isYinc) { 
                        // this is the fix for Java having the point of origin in the upper left corner;
                        // here we have to fix the x-coordinate because we are working with the inverse..
                        x = -x;
                    }
                    scrollPoints[i]= new Point(x+transX, y+transY);
                }                                                
            } 
            // now the last step; ensures that we reach our planned destination,
            // even if my above calculations were wrong ;-)
            //newUpperLeft.y = - newUpperLeft.y;
          
            scrollPoints[end] = new Point(endpoint);
            
            return scrollPoints;
            
        }
        
    /**
     * Calculates intermediate points for scrolling from <code>currentUpperLeft</code> to
     * <code>newUpperLeft</code>.
     * The scrolling is done in a right-angle: we either scroll first vertical, then horizontal
     * or first horizontal, then vertical. 
     * 
     * @param newUpperLeft endpoint of scrolling; last point in returned array
     * @param currentUpperLeft startpoint of scrolling
     * @param offset maximum amount to scroll per step
     * @param horizontalFirst toggles whether order of scroll direction is horizontal->vertical or vertical->horizontal 
     */
    public static  Point []  scrollRightAngled(Point newUpperLeft, Point currentUpperLeft, int offset,  boolean horizontalFirst) {
        Collection horizontalScrollPoints=null;
        Collection verticalScrollPoints=null;
        if (newUpperLeft.x < currentUpperLeft.x) {
            horizontalScrollPoints =  scrollWest(currentUpperLeft, newUpperLeft, offset);
         }
         else if (newUpperLeft.x > currentUpperLeft.x) {
             horizontalScrollPoints = scrollEast(currentUpperLeft, newUpperLeft, offset);
         }
         
         if (newUpperLeft.y < currentUpperLeft.y) {
             verticalScrollPoints = scrollNorth(currentUpperLeft, newUpperLeft, offset);
         }
         else if (newUpperLeft.y > currentUpperLeft.y) {
             verticalScrollPoints = scrollSouth(currentUpperLeft, newUpperLeft, offset);
         }            
         Iterator firstDirection;
         Iterator sndDirection;
         if (horizontalFirst) {
             firstDirection = horizontalScrollPoints.iterator();
             sndDirection = verticalScrollPoints.iterator();
         }
         else {
             firstDirection = verticalScrollPoints.iterator();
             sndDirection = horizontalScrollPoints.iterator();
         }
         
         Point [] retour = new Point [horizontalScrollPoints.size()+verticalScrollPoints.size()];
         int counter = 0;
         while (firstDirection.hasNext()){
             retour[counter++]=(Point)firstDirection.next();
         }
         while (sndDirection.hasNext()){
             retour[counter++]=(Point)sndDirection.next();
         }
         return retour;
    }
         
     private static Collection scrollWest (Point cur, Point pref, int diff){
         int stop = pref.x+diff;
         LinkedList pointList = new LinkedList();
         while (stop< cur.x) {
             cur.x-=diff;
             pointList.add(new Point(cur));
         }
         pointList.add(new Point(pref));
         return pointList;
     }
     
     private static Collection scrollEast (Point cur, Point pref, int diff){
         int stop = pref.x-diff;
         LinkedList pointList = new LinkedList();
         while (stop>cur.x) {
             cur.x+=diff;
             pointList.add(new Point(cur));
         }      
         pointList.add(new Point(pref));
         return pointList;
     }
     private static Collection scrollSouth (Point cur, Point pref, int diff){
         int stop = pref.y-diff;
         LinkedList pointList = new LinkedList();
         while (stop>cur.y) {
             cur.y+=diff;
             pointList.add(new Point(cur));
         }
         pointList.add(new Point(pref));
         return pointList;
     }
     private static Collection scrollNorth (Point cur, Point pref, int diff){
         int stop = pref.y+diff;
         LinkedList pointList = new LinkedList();
         while (stop< cur.y) {
             cur.y-=diff;
             pointList.add(new Point(cur));
         }
         pointList.add(new Point(pref));
         return pointList;
     }
    
    
    
    public static void main(String[] args) {
        
        Point start = new Point(100, 100);
        // all eight possible scroll directons:
        Point targetLeft = new Point(7, 100);
        Point targetRight = new Point(234, 100);
        Point targetUp = new Point(100, 13);
        Point targetDown = new Point(100, 234);
        
        Point targetUpperLeftXLonger = new Point(7, 17);
        Point targetUpperRightXLonger = new Point(234, 7);
        Point targetDownLeftXLonger = new Point(7, 165);
        Point targetDownRightXLonger = new Point(234, 200);

        Point targetUpperLeftYLonger = new Point(17, 7);
        Point targetUpperRightYLonger = new Point(165, 7);
        Point targetDownLeftYLonger = new Point(7, 234);
        Point targetDownRightYLonger = new Point(200, 234);
        Point [] testPoints = new Point[] {
                        targetLeft, targetRight, targetUp, targetDown,
                        targetUpperLeftXLonger, targetUpperRightXLonger, targetDownLeftXLonger, targetDownRightXLonger,
                        targetUpperLeftYLonger, targetUpperRightYLonger, targetDownLeftYLonger, targetDownRightYLonger
        };
        
        for (int i=0;i<testPoints.length;i++){
            System.out.println("scrolling: "+start+"-->"+testPoints[i]);
            Point [] points  = calculateDiagonalScrollSteps(start, testPoints[i],30);
            System.out.println(Global.arrayToString(points));
            System.out.println("------------------------------------------------------------------------------------ ");        
        }
        
    }
    
   
}
