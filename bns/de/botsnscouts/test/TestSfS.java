package de.botsnscouts.board;

import java.io.*;

import de.botsnscouts.util.*;

/** Testklasse fuer SpielfeldSim
  */

public class TestSfS
{
    private static boolean verbose=true;
    
    public static void main(String[] args) throws IOException,FormatException,FlaggenException {
	BufferedReader options=null;
        if (args.length==0){
            System.err.println("usage: java TestSfS <option-file-name>");
            System.exit(5);
        }
	try { 
            options= new BufferedReader(new FileReader(args[0])); 
        }
	catch (Exception e){
	    p("Dateiladefehler:"+e.getMessage());
	    System.exit(1);
	}
	
            //** Die offiziellen Statusinformationen
        SpielfeldSim sf=null;
	String spielfeldstring="";
        int phase=-1;
        Ort[] flaggen = null;
        Roboter[] robbis = null;

            //** Vars fuer die interne Nutzung hier
	String s;
        int fn=-1;
        int rn=-1;
        int sx=-1,sy=-1; // Spielfeldgroesse
        
            //	try{ 
            // ziemlich bloede Idee, weil man die Line number der Exception nicht mehr sieht...
        s=options.readLine();
        while(s!=null){
            if ((s.length()>1) && (s.charAt(0)!='#')) {
                String id="" + (s.charAt(0)) + (s.charAt(1));
                p("-"+id+"-");
                if (id.equals("Rn")) {
                    rn=java.lang.Character.digit(s.charAt(3),10);
                    p("Anzahl Roboter "+rn);
                    robbis=new Roboter[rn];
                }
                else if (id.equals("R ")){
                    p("Roboter-Definition");
                    Roboter r=Roboter.getNewInstance(s.substring(6,12));
                    p("Name: |"+s.substring(6,12)+"|");
                    p("Index: "+parseInt(s,2));
                    
                    switch(s.charAt(13)){
                        case 'N':
                            r.setAusrichtung(0);
                            break;
                        case 'E':
                        case 'O':
                            r.setAusrichtung(1);
                            break;
                        case 'S':
                            r.setAusrichtung(2);
                            break;
                        case 'W':
                            r.setAusrichtung(3);
                            break;
                        default:
                            System.err.println("Fand keine legale Ausrichtung");
                            System.exit(5);
                    }//switch ausrichtung
                    p("Ausrichtung: "+r.getAusrichtung());

                    r.setPos(parseInt(s,19), parseInt(s,23));
                    p("Position: x="+r.getX()+"; y="+r.getY());
                    r.setNaechsteFlagge(parseInt(s,27));
                    r.setArchiv(parseInt(s,31), parseInt(s,35));
                    p("Naechste Flagge: "+r.getNaechsteFlagge()+"; archivX="+r.getArchivX()+"; archivY="+r.getArchivY());
                    if (parseInt(s,39)==1)
                        r.setVirtuell(true);
                    else
                        r.setVirtuell(false);
                    r.setLeben(parseInt(s,43));
                    p("Leben: "+r.getLeben()+"; Virtuell: "+(r.istVirtuell()?"ja":"nein"));
                    for (int i=0;i<5;i++){
                        int prio=parseInt(s,47+8*i);
                        String aktion=s.substring(51+8*i,53+8*i);
                        Karte k=new Karte(prio,aktion);
                        r.setZug(i, k);
                    } //for Karte
                    for (int i=0;i<5;i++){
                        int prio=parseInt(s,87+8*i);
                        String aktion=s.substring(91+8*i,93+8*i);
                        Karte k=new Karte(prio,aktion);
                        if (aktion.equals("NN"))
                            r.sperreRegister(i, null);
                        else
                            r.sperreRegister(i, k);
                    }
                    if (parseInt(s,127)==1)
                        r.setAktiviert(true);
                    else
                        r.setAktiviert(false);
                    r.setSchaden(parseInt(s,131));
                    robbis[parseInt(s,2)]=r;
                }
                else if (id.equals("S ")){
                    p("Spielfeld-Definition");
                    sx=parseInt(s,2);
                    sy=parseInt(s,6);
		    p("x="+sx+"; y="+sy);
                    BufferedReader in=null;
                    try{
                        in= new BufferedReader(new FileReader(s.substring(10)));
                    }
                    catch (FileNotFoundException e){
                        System.err.println("Kein solches file gefunden!");
                        System.err.println(e.getMessage());
                        System.exit(5);
                    }
                    String s2="";
                    String t;
                    try{
                        t=in.readLine();
                        while(t!=null){
                            s2+=t;
                            s2+="\n";
                            t=in.readLine();
                            spielfeldstring=s2;
                        }
                    }
                    catch(IOException e){
                        System.err.println("Fehler beim Einlesen des Spielfelds");
                        System.err.println(e.getMessage());
                        System.exit(5);
                    }
		    p(spielfeldstring);
                }
                else if (id.equals("P ")){
                    phase=java.lang.Character.digit(s.charAt(2),10);
                    p("Phase Nr. "+phase);                        
                }
                else if (id.equals("Fn")){
                    fn=java.lang.Character.digit(s.charAt(3),10);
                    p("Anzahl Fahnen: "+fn);
                    flaggen=new Ort[fn];
                }
                else if (id.equals("F ")){
                    p("Fahnen-Definition");
                    int idx=parseInt(s,2);
                    flaggen[idx]=new Ort();
                    flaggen[idx].x=parseInt(s,6);
                    flaggen[idx].y=parseInt(s,10);
                    p("Index="+idx+"; x="+flaggen[idx].x+"; y="+flaggen[idx].y);
                }
                else if (id.equals("VE")){
                    switch (parseInt(s,3)){
                        case 0:
                            verbose=false;
                            break;
                        case 1:
                            verbose=true;
                            break;
                    }
                }
                else if (id.equals("CS")){
                    sf=new SpielfeldSim(sx,sy,spielfeldstring,flaggen);
                }    
                else if (id.equals("PS")){
                    System.out.println("Drucke Spielfeld:");
                    sf.print();
                    System.out.println();
                }
                else if (id.equals("PR")){
                    System.out.println("Drucke Roboter:");
                    for (int i=0;i<robbis.length;i++){
                        System.out.println("Robbi "+i+":");
                        if (robbis[i]==null)
                            System.out.println("null");
                        else
                            robbis[i].zeige_Roboter();
                    }
                    System.out.println();
                }
                else if (id.equals("PF")){
                    System.out.println("Drucke Flaggen:");
                    for (int i=0;i<flaggen.length;i++){
                        System.out.print("Flagge "+i+": ");
                        if (flaggen[i]==null)
                            System.out.println("null");
                        else
                            System.out.println("x="+flaggen[i].x+"; y="+flaggen[i].y);
                    }
                    System.out.println();
                }
                else if (id.equals("DP")){
                    System.out.println("Doing Phase");
                    sf.doPhase(phase,robbis);
                }
                else if (id.equals("Q "))
                    return;
		else if (id.equals("RO")){
		  String s2 = sf.get90GradGedreht();
		  System.out.println(s2);
		  sf = new SpielfeldSim(sx,sy,s2,flaggen);
		  spielfeldstring=s2;
		}
		else if (id.equals("SF")){
		  PrintWriter foo=new PrintWriter(new FileOutputStream(s.substring(3)));
		  foo.print(spielfeldstring);
		  foo.close();
		}
                else System.err.println("Statement nicht erkannt: \""+id+"\"");
            }
            s=options.readLine();
        }
    }

    private static int parseInt(String s,int base)
        {
            return (100*java.lang.Character.digit(s.charAt(base),10)+10*java.lang.Character.digit(s.charAt(base+1),10)+java.lang.Character.digit(s.charAt(base+2),10));
        }
    private static void p(String s)
        {
            if (verbose)
                System.out.println(s);
        }
}

    
