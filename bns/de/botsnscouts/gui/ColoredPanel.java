package de.botsnscouts.gui;

import java.awt.*;
import javax.swing.JPanel;

public class ColoredPanel extends JPanel{

  public static final Color defaultColor = Color.black;
  public static final int alpha = 133;

  Color color;

  public ColoredPanel(){
    this(defaultColor);
  }

  public ColoredPanel(Color c){
    color = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
  }


  public void paint(Graphics g){
    g.setColor(color);
    g.fillRect(0,0,getWidth(),getHeight());
    super.paint(g);
  }

}
