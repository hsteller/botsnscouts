package de.botsnscouts.start;

import java.awt.*;
import java.net.*;

public class HalloCanvas extends Canvas{
    boolean ende = false;
	private int x,y,xx,yy;
	private double rotater;
	private Font font1,font2;
	private Color color1,color2;
	private Image rr_pic;

	private String[] text1 = {"StartingEnvironment","Server","GrafiX & UserInterface","AdvancedBoard","Artificial Intelligence",
								"Communications","Supervisor"};
	private String[] text2 = {"Ludmila Scharf, Leo Scharf","Alexandar Rakovski, Mohammad al Saad, Holger Dreher","Daniel Holtz, Lukasz Pekacki",
								"Dirk Materlik, Gero Eggers",
								"Helen Bohse, Torsten Wlock","Hendrik Steller, Ndoula Ourima","Miriam Busch"};

	public HalloCanvas(int width,int height){
		x = width;
		y = height;
        setBackground(new Color(4,64,4));
        color1 = new Color(84,158,73);
        color2 = new Color(255,160,0);
        font1 = new Font("SansSerif",Font.BOLD,16);
        font2 = new Font("SansSerif",Font.BOLD+Font.ITALIC,96);
	URL url = getClass().getResource("images/RallyMed.jpg");
        rr_pic = Toolkit.getDefaultToolkit().getImage(url);
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(rr_pic,0);
        try{
        mt.waitForID(0);}
        catch(InterruptedException ie){}
    }
								
								
								
								
	public void paint(Graphics g){
		
		while (!ende){		
		Image dbi = createImage(x,y);
		Graphics dbg = dbi.getGraphics();
		xx = (int) (Math.cos(rotater)*20);
		yy = (int) (Math.sin(rotater)*20);
		dbg.drawImage(rr_pic,100,50,this);
		for (int i=0;i< text1.length;i++){

			//if((move%5)==0)
			//dbg.setColor(new Color((float)Math.random(),(float)Math.random(),(float)Math.random()));
			dbg.setColor(color1);
			dbg.setFont(font1);
			dbg.drawString(text1[i],xx+i*20+50,yy+i*50+150);
			dbg.drawString(text2[i],xx+i*20+55,yy+i*50+165);	
		}
		if(rotater>Math.PI*2)
		rotater=0;
		else
		rotater+=0.14;
		g.drawImage(dbi,0,0,this);
		//try{Thread.sleep(50);}
		//catch(InterruptedException ie){}
		ende = true;
		}
	}				
	public void update(Graphics g){
		paint(g);
	}			
}
