package de.botsnscouts.start;

import java.awt.*;

public class KachelInfo{
    private Image image;
    private String name;

    public KachelInfo(String nam, Image img){
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
