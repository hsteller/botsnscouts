package de.botsnscouts.start;

import java.awt.*;
import de.botsnscouts.board.*;
import de.botsnscouts.gui.*;
import de.botsnscouts.util.*;

class TileInfo{
    private Image image;
    private String name;

    public TileInfo(String nam, Image img){
	name=nam;
	image=img;
    }

    public Image getImage(){
	return image;
    }

    public String toString(){
	return name;
    }

}
