package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import de.botsnscouts.util.*;
import de.botsnscouts.gui.*;
import de.botsnscouts.board.*;

public class StSpVorschau extends Frame implements ActionListener, WindowListener{

 private ScrollPane feld=null;
 private Button schl=null;

 public StSpVorschau(String fldstr, int dreh) throws Exception{
  super("Kachel Vorschau");
  setLayout(new BorderLayout(0, 10));
  setLocation(10,10);
  setSize(840,660);

  BufferedReader b=null;
  Spielfeld spf=null;

  try{
   b=new BufferedReader(new InputStreamReader(new FileInputStream(fldstr)));
  }catch(Exception e){
   System.err.println("Fehler in Vorschau1");
   throw new Exception();
  }

  StringBuffer str=new StringBuffer();
  String tmp=null;
  try{
   while((tmp=b.readLine())!=null)
    str.append(tmp+"\n");
   }catch(Exception e){
    System.err.println("Fehler in Vorschau2"+e);
    throw new Exception();
   }

  try{
   spf = new Spielfeld(12,12,str.toString(),null);
  switch(dreh){
  case 3: {str=new StringBuffer(spf.get90GradGedreht());
  spf=new Spielfeld(12,12,str.toString(),null);
  }
  case 2:{str=new StringBuffer(spf.get90GradGedreht());
  spf=new Spielfeld(12,12,str.toString(),null);
  } 
  case 1:{str=new StringBuffer(spf.get90GradGedreht());
  //  spf=new Spielfeld(12,12,str.toString(),null);
  }
  }
  } catch(FlaggenException e){
   System.out.println("Flaggenfehler in Checkit "+e);
  }

  //  Ort[] t=new Ort[1]; t[0]=new Ort(0,0);

  feld=new ScrollPane();
  //  feld.setSize(800,580);
  //  feld.setLocation(20,20);
  try{
   feld.add(new SACanvas(new SpielfeldSim(12,12,str.toString(),null)));
  }catch(Exception e){
   System.err.println("Fehler in Vorschau3"+e);
   throw new Exception();
  }
//  feld.addImpl(new SACanvas(new SpielfeldSim(12,12,tmp.toString(),t)),this,-1);
  feld.getHAdjustable().setUnitIncrement(100);
  feld.getVAdjustable().setUnitIncrement(100);
  add("Center",feld);

  schl=new Button("OK");
  //  schl.setSize(800,30);
  //  schl.setLocation(20,610);
  add("South",schl);
  schl.addActionListener(this);

  this.show();
  this.addWindowListener(this);
  this.repaint();
 }

 public StSpVorschau(String fldstr,int x, int y, Ort[] fl) throws Exception{
  super("Spielfeld Vorschau");
  setLayout(new BorderLayout(0, 10));
  setLocation(10,10);
  setSize(840,660);

  feld=new ScrollPane();
  //  feld.setSize(800,580);
  //  feld.setLocation(20,20);
  try{
   feld.add(new SACanvas(new SpielfeldSim(x,y,fldstr,fl)));
  }catch(FormatException fex){
      System.err.println("Format "+fex);
  }catch(FlaggenException flex){
      System.err.println("Flaggen "+flex);
  }/*catch(Exception e){
   System.err.println("Fehler in Vorschau3"+e);
   throw new Exception();
   }*/
  add("Center",feld);

  schl=new Button("OK");
  //  schl.setSize(800,30);
  //  schl.setLocation(20,610);
  add("South",schl);
  schl.addActionListener(this);

  this.show();
  this.addWindowListener(this);
  this.repaint();
 }

 public void actionPerformed(ActionEvent e){
   if(e.getActionCommand().compareTo("OK") == 0)
    {
     this.dispose();
    }
 }

 public void windowDeactivated(WindowEvent e) {}
 public void windowOpened(WindowEvent e)      {}
 public void windowClosing(WindowEvent e) //{}
  {
   this.dispose();
  }
 public void windowClosed(WindowEvent e)      {}
 public void windowIconified(WindowEvent e)   {}
 public void windowDeiconified(WindowEvent e) {}
 public void windowActivated(WindowEvent e)   {}



}













