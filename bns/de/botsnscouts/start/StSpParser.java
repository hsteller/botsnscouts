package de.spline.rr;

import java.io.*;

public class StSpParser{

 public StSpParser(){}

 public static final String GRUBENZWR="____________";
 public static final String GRUBENFLD="_G_G_G_G_G_G_G_G_G_G_G_G_";
 static BufferedReader[][] fromFilez=new BufferedReader[4][3];
 private static StringBuffer out=null;
 private static int xm=-1,ym=-1;
 static String[][] kachF = new String[4][3];
 static int[][] kachind = new int[4][3];


 public static void addFile(int x, int y, /*String pathname*/InputStream istream,int dreh) throws Exception{
    if((x>4)||(y>3)||(/*pathname*/istream==null)){
    System.err.println(Message.say("StartSpieler","eFalscheArg"));
    throw new Exception(Message.say("StartSpieler","eFalscheArg"));
   }

   try{
     fromFilez[x-1][y-1]=new BufferedReader(new InputStreamReader(/*new FileInputStream(pathname)*/istream));
     kachF[x-1][y-1]=checkit(fromFilez[x-1][y-1],dreh);
     if (xm<(x-1)) xm=x-1;
     if (ym<(y-1)) ym=y-1;
    //  System.err.println("Spielfeldgröße: "+xm+" x "+ym+" Kacheln");
     //      fromFilez[x-1][y-1]=new BufferedReader(new InputStreamReader(new FileInputStream(pathname)));
   }catch(Exception e){
     System.err.println(Message.say("StartSpieler","eNoSuchFile"));
     throw new Exception(Message.say("StartSpieler","eNoSuchFile"));
   }


 }

 public static String checkit(BufferedReader b,int dr)throws FormatException{
  StringBuffer str=new StringBuffer();
  String tmp=null;
  Spielfeld spf=null;
  try{
   while((tmp=b.readLine())!=null)
    str.append(tmp+"\n");
   }catch(Exception e){
       //    System.out.println("Fehler in Checkit!"+e);
   }

  Ort[] t=new Ort[2]; t[1]=new Ort(1,1);  t[1]=new Ort(1,10);
  try{
   spf = new Spielfeld(12,12,str.toString(),null);
  switch(dr){
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
      //   System.out.println("Flaggenfehler in Checkit "+e);
  }
  return str.toString();

 }

 public static boolean checkit(String str,int xf,int yf){

  Ort[] t=new Ort[2]; t[1]=new Ort(1,1);  t[1]=new Ort(1,10);
  try{
   new Spielfeld(xf,yf,str,null);
  } catch(FormatException e){
      //   System.out.println("ein Fehler in Checkit "+e);
   return false;
  } catch(FlaggenException e){
      //   System.out.println("flaggen Fehler in Checkit "+e);
   return false;
  }
  return true;
 }

 public static String getField(){

  out=new StringBuffer();
  String rechts=new String();
  boolean links =false;
  StringBuffer oben = new StringBuffer();
  StringBuffer unten =new StringBuffer();
  for (int i=0;i<4;i++)
      for (int j=0;j<3;kachind[i][j++]=0);
  for(int j=ym;j>=0;j--){
   for (int k=0;k<25;k++){
    for (int i=0;i<=xm;i++){
     if (k==0){
      if (kachF[i][j]==null)
       unten.append(GRUBENZWR);
      else unten.append(liesZeile(kachF[i][j],i,j));
     }
     else if (k==24){
      if (kachF[i][j]==null)
       oben.append(GRUBENZWR);
      else oben.append(liesZeile(kachF[i][j],i,j));
     }
     else{
      if (k%2==0){
       if (kachF[i][j]==null)
        out.append(GRUBENZWR);
       else
        out.append(liesZeile(kachF[i][j],i,j));
      }
      else{
       if (kachF[i][j]==null)
        rechts=GRUBENFLD;
       else rechts=new String(liesZeile(kachF[i][j],i,j));
       if (links) mergez(out,rechts);
       else{
        out.append(rechts);
        links=true;
       }
      }
     }
    } //for i
    links=false;
    if (k==0){
     out.append(merger(oben,unten));
     oben = new StringBuffer();
     unten =new StringBuffer();
    } //endif
    out.append("\n");
   } //for k
  } //for j
  out.append(oben);
  out.append(".\n");
  if(!checkit(out.toString(),(xm+1)*12,(ym+1)*12))
      System.out.println("Ooops!!!!");
  /*  if(checkit(out.toString(),(xm+1)*12,(ym+1)*12))
         System.out.println("Feld ist in Ordnung, dim: "+(xm+1)*12+" x "+(ym+1)*12);
	 else System.out.println("Ooops!!!!");*/
  return out.toString();
 }

 public static String liesZeile(String fil,int i, int j){
  StringBuffer str=new StringBuffer();
  try{
   char x=fil.charAt(kachind[i][j]++);
   while(x=='\10'||x=='\13'||x=='\32'||x=='\t'||x=='\n'||x==' ')
    x=fil.charAt(kachind[i][j]++);
   while (x!='\10'&&x!='\13'&&x!='\32'&&x!='\t'&&x!='\n'&&x!=' '){
    str.append(x);
    x=fil.charAt(kachind[i][j]++);
   }
  }catch(Exception ex){
   System.out.println(ex);
  }
  return (str.toString()).trim();
 }

 public static void mergez(StringBuffer l, String r){
  char lc,fc;
  lc=l.charAt(l.length()-1);
  if (lc=='#') fc=lc;
  else fc=r.charAt(0);
  int x=l.length()-1;
  l.setLength(l.length()-1);
  l.append(r);
  l.setCharAt(x,fc);
//  System.out.println("ende: "+lc);
//  System.out.println("begin: "+fc);
 }

 public static String merger(StringBuffer o, StringBuffer u){
//  return u.toString();

  if (o.length()==0)
   return u.toString();
  StringBuffer zwr=new StringBuffer();
  char oc,uc;
  int oi=0, ui=0;
  oc = o.charAt(oi++);
  uc = u.charAt(ui++);
  while (oi<o.length()&&ui<u.length()){
   while (oc != '_' && oc != '#'&&oi<o.length()){
    zwr.append(oc);
    oc=o.charAt(oi++);
   }
   if (oc=='_') zwr.append(uc);
   else zwr.append(oc);
   uc = u.charAt(ui++);
   if (oi<o.length()) oc = o.charAt(oi++);
   while (uc != '_' && uc != '#'&&ui<u.length()){
    zwr.append(uc);
    uc=u.charAt(ui++);
   }
   if (ui==u.length()&&uc!='_'&&uc!='#') zwr.append(uc);
  }
  if (oi<o.length()){
   while (oc != '_' && oc != '#'&&oi<o.length()){
    zwr.append(oc);
    oc=o.charAt(oi++);
   }
  }
  if ((oc == '_'||oc=='#')&&(uc =='_'||uc =='#'))
   if (oc=='_') zwr.append(uc);
   else zwr.append(oc);
  if (ui<u.length()){
   uc = u.charAt(ui++);
   while (uc != '_' && uc != '#'&&ui<u.length()){
    zwr.append(uc);
    uc=u.charAt(ui++);
   }
   zwr.append(uc);
  }

  return (zwr.toString()).trim();
 }

 public static Ort getFieldSize(){
 //  return new Ort(24,12);
  return (new Ort((xm+1)*12,(ym+1)*12));
 }


 public static void reset(){
  fromFilez=new BufferedReader[4][3];
  for (int i=0;i<4;i++)
   for (int j=0;j<3;fromFilez[i][j++]=null);
  for (int i=0;i<4;i++)
   for (int j=0;j<3;kachF[i][j++]=null);
  out=null;
  xm=ym=-1;
 }

}



