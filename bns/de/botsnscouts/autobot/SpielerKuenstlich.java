package de.botsnscouts.autobot;

import de.botsnscouts.comm.*;
import de.botsnscouts.board.*;
import de.botsnscouts.util.*;

/** SpielerKuenstlich implementiert den kuenstlichen Spieler
 */
public class SpielerKuenstlich extends Thread {

        /**
         * Konstruktor erhaelt IP-Adresse und Portnummer
         */
    public SpielerKuenstlich (String ip, int port)
        {
            this(ip,port,0);
        };

        /** Konstruktor mit Spielstaerke - je hoeher desto schlechter, 0 ist am besten */
    public SpielerKuenstlich(String i, int p, int m)
        {
            ip=i;
            port=p;
            malus=m;
        }

    String ip;
    int port;
    int malus;

    String realname;
    Permu wirbel;  
    String spielfeldstring;
    Ort [] fahnen;
    Ort robbiPos;
		
    Roboter	meinRobbi = Roboter.getNewInstance("OrgRobbi");
    KommClientSpieler meinKomm = new KommClientSpieler();
    ClientAntwort antwort = new ClientAntwort();
	
    SpielfeldKS meinSpielfeld;

        /**
         * run-Methode erzeugt zufaelligen Namen fuer den kuenstlichen Spieler, meldet ihn an
         * und wartet dann auf Nachrichten vom Server, die entsprechend beantwortet werden
         */
    public void run() 
        {  
            boolean spielLaeuft;
    
            try{
                realname=KrimsKrams.randomName();
                meinKomm.anmelden(ip, port,realname);
            }catch(KommException e){
                d("Anmelden gescheitert"+e);
            }

            try{
                antwort=meinKomm.warte();
                if (antwort.typ==antwort.SPIELSTART){
                    meinKomm.spielstart();
                }
            }catch(KommException e) {
                d("keinen Spielstart bekommen"+e);
		return;
            }
            spielLaeuft=true;
	
            while (spielLaeuft) {
                d("Warte auf Servermitteilung...");
                try {
                    antwort=meinKomm.warte();
                }
                catch (KommException kE) {
                    d("KommException... Exite..."+kE.getMessage());
                    return;
                }

                d("Vom Server erhalten: "+antwort.getTyp());
		
                switch(antwort.typ) {
                    case(antwort.ZERSTOERUNG):
                        if (meinSpielfeld==null) {
                            spielfeldkreieren();
                            wirbel = new Permu(meinSpielfeld,malus);
			    meinSpielfeld.setDebug(false); //Wollen wir garantiert hier nicht hören :)
                        }
                        d("Robbi fuellen");
                        robbifuellen();

			hotfix();

                        d("antwortaufZerstoerung");
                        antwortaufZerstoerung();
                        break;

                    case(antwort.REPARATUR):
                        d("answer auf Reparatur");
                        robbifuellen();
			
			hotfix();

                        antwortaufReparatur(antwort.zahl);
                        break;

                    case(antwort.MACHEZUG):
                        boolean powerdown=false;
                        for (int i = 0; i < antwort.karten.length; i++)
                            d("Karte "+i+" ist "+antwort.karten[i].getprio()+"|"+antwort.karten[i].getaktion());
                        robbifuellen();
                        Roboter simRob= Roboter.getCopy(meinRobbi);  // Kopieren fuer spaetere Powerdown-Simulationen
		    
                        Karte[] vollKA = new Karte[9];

                        for ( int i = 0; i < antwort.karten.length; i++) vollKA[i] = antwort.karten[i];
                        Roboter pRob = Roboter.getCopy(meinRobbi);
                            // gesperrte Register in pRob.zug schreiben
                        for (int i = 0; i < pRob.getGesperrteRegister().length; i++)
                            pRob.setZug(i, pRob.getGesperrteRegister(i));
                        pRob.zeige_Roboter();
		    
                        Karte[] vonPermut = wirbel.permutiere(vollKA, pRob);

                            // Absende-Karten vorbereiten
                        int kartenZahl;
                        if (antwort.karten.length > 5) kartenZahl = 5;
                        else kartenZahl = antwort.karten.length;

                        int[] anServer = new int[kartenZahl];
                        int k = 0; int l = 0;
                        for (int i = 0; i < antwort.karten.length; i++) {
                            int j = 0;
                            while ((j < antwort.karten.length) && (k < 5)) {
                                if(antwort.karten[j].getprio() == vonPermut[k].getprio()) {
                                    anServer[l++] = (j+1);
                                }
                                j++;
                            }
                            k++;
                            if (j == antwort.karten.length) {
                                i--;
                            }
                        }
	  
                        Roboter[] simRobs=new Roboter[1];         // Powerdown !?
                        simRobs[0]=simRob;                        // 
                        int schadenAlt=simRob.getSchaden();            //
                        for (int i=1;i<6;i++){                        //
                            simRobs[0].setZug(i-1, vonPermut[i-1]);     //
                            meinSpielfeld.doPhase(i,simRobs);       // geplante Belegung simulieren
                            simRobs[0].setZug(i-1, null);               //
                        }                                         //
                        if ((simRobs[0].getSchaden()>5) || (java.lang.Math.random() < (((double)simRobs[0].getSchaden()-1) * 0.1 )))
                        {
                            simRobs[0].setAktiviert(false);
                            simRobs[0].setSchaden(0);                     //
                            for (int i=1;i<6;i++){                    // die Phase mit powerdown simulieren 
                                meinSpielfeld.doPhase(i,simRobs);       //
                            }                                         //
                            if (simRobs[0].getSchaden()<=schadenAlt)       //
                                powerdown=true;                         //
                        }
	  	  
                            // Karten senden
                        meinKomm.registerProg(realname,anServer,powerdown);
                        break;

                    case(antwort.REAKTIVIERUNG):                    // Anfrage vom Server ob Roboter wieder eingesetzt werden möchte

			hotfix();

                        meinKomm.respReaktivierung (realname,false); // Anwort: Roboter wiedereinsetzen
                        break;

                    case(antwort.ENTFERNUNG):
                        d("Wurde aus Spiel entfernt! Grund:"+antwort.str+"\nSende OK an Server.");
                        spielLaeuft=false;

			hotfix();

			meinKomm.bestaetigung();
                        break;
			
                    default:
                        d("Warnung: Ungueltige Serverantwort."+antwort.getTyp());
                        break;
                }     //Ende switch
            }         //Ende while
	    d("Habe das Ende meiner run()-Methode erreicht!");
        }             //Ende run



       
        /**
         * fuellt einen Roboter meinRobbi mit dem aktuellen RobStatus
         */
    public Roboter robbifuellen()
        {
            try {
                meinRobbi = meinKomm.getRobStatus(realname);
            }
            catch(KommException e){
                Global.debug(this,"Keinen Robi bekommen!"+e);
            }
            return meinRobbi;
        }
    
        /**
         * erzeugt mit der Spielfelddimension, den Fahnenpositionen und dem 
         * Spielfeldstring das Spielfeld des kuenstlichen Spielers, ruft 
         * ausserdem die Entfernungsberechnung in SpielfeldKS auf
         */ 
    public void spielfeldkreieren()
        {
            d("Spielfeld erzeugen");
            int dimx, dimy;
            Ort dimension;

            try{
                dimension=meinKomm.getSpielfeldDim();
                dimx=dimension.x;
                dimy=dimension.y;

                fahnen=meinKomm.getFahnenPos();
				
                spielfeldstring=meinKomm.getSpielfeld();
                    //d(spielfeldstring);

                try{
                    meinSpielfeld = SpielfeldKS.getInstance(dimx,dimy,spielfeldstring,fahnen);

                }
                catch (FlaggenException fe){
                    Global.debug(this,"Flagge auf Grube"+fe);
                }
		
                catch (FormatException e){
                    Global.debug(this,"Kein Spielfeld bekommen!"+e);
                }
            }

            catch(KommException e){
                Global.debug(this,"Kein Spielfeld bekommen!"+e);
            }			
        }

        /**
         * bestimmt und uebermittelt das zu reparierende Register als Antwort auf Reparaturangebot
         * des Servers 
         */
    public void antwortaufReparatur(int reparatur)
        {
            d(" reparatur="+reparatur);
            int [] entsperrteRegister = new int[reparatur];
            int a=0;       // Anzahl der erfolgten Reparaturen auf 0 setzen.
            for (int i=0 ; i<5 ; i++)
            {
                if (meinRobbi.getGesperrteRegister(i)!=null)
                {
                    if (a<reparatur) {
                        entsperrteRegister[a]=i+1;  // falls noch so oft repariert wie Reparaturpunkte verfuegbar, dann reparieren und
                        d(meinRobbi.getName()+": Repariere Register"+i);
                        a++;                                       // Anzahl der erfolgten Reparaturen hochsetzen
                    }
                }
            }
	    meinKomm.respReparatur (realname, entsperrteRegister);
        }

        // Antwort auf Zerstoert
        // modifiziert von Gero - 1999-07-20
        /**
         * bestimmt und uebermittelt die beste Wiedereinsetzrichtung als Antwort auf Zerstörung und
         * bei Spielbeginn
         */
    public void antwortaufZerstoerung() {
        int richtung=0;
        Roboter testRobbi = Roboter.getCopy(meinRobbi);

        testRobbi.zumArchiv();

        int entfernungBeste=9999;  // beste bisher gefundene Entfernung
        int entfernungNeu;         // neue gefundene Entfernung

        d("Roboter zerstoert, Suche neue Ausrichtung");
        for (int i=0;i<4;i++){
            testRobbi.setAusrichtung(i);
            entfernungNeu=meinSpielfeld.getEntfernung(testRobbi);
            if (entfernungNeu < entfernungBeste) 
            {
                entfernungBeste = entfernungNeu;
                richtung=i;
                d("Neue beste Entfernung "+entfernungBeste+", Richtung "+richtung);
            }
        }
        meinKomm.respZerstoert (realname, richtung);
    }

        /**
         * Main-Methode, die den kuenstlichen Spieler von der Shell aus als Thread startet
         */
    public static void main(String[] args)
        {
            int sPort = 0;
            SpielerKuenstlich spK;
                // Kommandozeilenparameter auswerten
            if ((args.length > 0) &&(args[0] != "") && (args[1]) !="") {
                try {sPort = Integer.parseInt(args[1]); } 
                catch (Exception e) {
		    System.err.println("Usage: java de.spline.rr.SpielerKuenstlich <host> <port>");
                }
                spK = new SpielerKuenstlich(args[0],sPort);
            }
            else spK = new SpielerKuenstlich("localhost",8077);
            spK.start();
        }

    private void hotfix(){
	try{
	    sleep(2500);
	}catch(InterruptedException e){}
    }

    private void d(String s)
        {
            Global.debug(this,s);
        }
}








