package de.botsnscouts.start;

public interface KachelClickListener {
    //rx,ry: rasterkoordinaten 1 bis 12 
    //fx,fy: feldkoordinaten 0 bis z.Zt. 3
    public void kachelClick(int rx,int ry,int fx,int fy);
    public void kachelMouseMove(int rx,int ry,int fx,int fy);
    public void kachelMouseLeave();
}
