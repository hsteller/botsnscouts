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
 
package de.botsnscouts.util;

/** Hilfsklasse, die eine Position auf dem Spielfeld durch ihre x - und y - Koordinate darstellt
*@author Hendrik<BR>*/
public class Ort {
  /** x-Koordinate*/
  public int x;
  /** y-Koordinate*/
  public int y;
  public Ort () {}
  public Ort (int a,int b) {
    x=a;
    y=b;
  }

    public Ort(Ort o){
	new Ort(o.x,o.y);
    }

    public int getX() {
	return x;
    }


    public int getY(){
	return y;
    }

    public void set(int x, int y) {
	this.x = x; this.y = y;
    }

    public void set(Ort o) {
	this.x = o.x; this.y = o.y;
    }

    public boolean equals(Ort o){
	return (this.x==o.x)&&(this.y==o.y);
    }


  public String toString() {
    String back="("+x+", "+y+")";
    return back;
  }
}
