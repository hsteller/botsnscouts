package de.spline.rr;

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
