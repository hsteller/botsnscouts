package de.botsnscouts.start;

public interface TileClickListener {
    //rx,ry: rasterkoordinaten 1 bis 12 
    //fx,fy: feldkoordinaten 0 bis z.Zt. 3
    public void tileClick(int rx,int ry,int fx,int fy);
    public void tileMouseMove(int rx,int ry,int fx,int fy);
    public void tileMouseLeave();
}
