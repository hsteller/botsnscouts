package de.botsnscouts.board;

import java.io.*;
import java.lang.*;

import de.botsnscouts.util.*;

/**
 *  Description of the Class
 *
 *@author     enno
 *@created    22. April 2001
 */
public class SpielfeldKS extends SpielfeldSim {
    private int[][][] entftab;
    private static SpielfeldKS uniqueInstance;


    private SpielfeldKS(int x, int y, String kacheln, Ort[] f) throws FormatException, FlaggenException {
        super(x, y, kacheln, f);
        berechneEntfernung(x, y, f);
    }

    public void setDebug(boolean b) {
        debugmeldungen = b;
    }

    /*
     * Beruecksichtigt Entfernung, Flagge, Schaden, Reperatur, Spielstaerke
     */
    public int getBewertung(Roboter robbi, int malus) {
        // wenn dieser Weg zum Spielgewinn fuehrt -> tu das!
        if(robbi.getNaechsteFlagge() == flaggen.length + 1) {
            return 0;
        }
        int entfernung = getEntfernung(robbi);
        // beruecksichtige Flaggenbesuch in einem Spielzug
        entfernung += (8 - robbi.getNaechsteFlagge()) * 40;
        // Schaden beruecksichtigen
        entfernung += (robbi.getSchaden()) * (5 - robbi.getLeben());
        // reparaturfeld ist gut
        if(bo(robbi.getX(), robbi.getY()).isRepairing() ) {
            entfernung -= (robbi.getSchaden());
        }
        // Spielstaerke einberechnen
        entfernung += (int) java.lang.Math.floor(java.lang.Math.random() * malus);

        return entfernung;
    }


    /**
     *  getEntfernung liefert die Entfernung des Roboters zur nächsten Flagge
     *  und berücksichtigt dabei nötige Drehungen.
     *
     *@param  robbi  der zu bewertende Roboter
     *@return        die Entfernung.
     */
    public int getEntfernung(Roboter robbi) {
        if(robbi.getSchaden() == 10) {
            return 1000;
        }
        int x = robbi.getX();
        int y = robbi.getY();
        int m = robbi.getNaechsteFlagge() - 1;
        int richt = robbi.getAusrichtung();

        if(entftab[m][x][y] == 0) {
            return 0;
        } else {
            switch (richt) {
                case 0:
                    if((entftab[m][x][y + 1] < entftab[m][x][y]) && (!hasNorthWall(x, y))) {
                        return entftab[m][x][y];
                    } else {
                        if(((entftab[m][x - 1][y] < entftab[m][x][y]) && (!hasWestWall(x, y))) ||
                                ((entftab[m][x + 1][y] < entftab[m][x][y]) && (!hasEastWall(x, y)))) {
                            return entftab[m][x][y] + 1;
                        } else {
                            return entftab[m][x][y] + 2;
                        }
                    }

                case 1:
                    if((entftab[m][x + 1][y] < entftab[m][x][y]) && (!hasEastWall(x, y))) {
                        return entftab[m][x][y];
                    } else {
                        if(((entftab[m][x][y - 1] < entftab[m][x][y]) && (!hasSouthWall(x, y))) ||
                                ((entftab[m][x][y + 1] < entftab[m][x][y]) && (!hasNorthWall(x, y)))) {
                            return entftab[m][x][y] + 1;
                        } else {
                            return entftab[m][x][y] + 2;
                        }
                    }

                case 2:
                    if((entftab[m][x][y - 1] < entftab[m][x][y]) && (!hasSouthWall(x, y))) {
                        return entftab[m][x][y];
                    } else {
                        if(((entftab[m][x - 1][y] < entftab[m][x][y]) && (!hasEastWall(x, y))) ||
                                ((entftab[m][x + 1][y] < entftab[m][x][y]) && (!hasEastWall(x, y)))) {
                            return entftab[m][x][y] + 1;
                        } else {
                            return entftab[m][x][y] + 2;
                        }
                    }

                case 3:
                    if((entftab[m][x - 1][y] < entftab[m][x][y]) && (!hasEastWall(x, y))) {
                        return entftab[m][x][y];
                    } else {
                        if(((entftab[m][x][y + 1] < entftab[m][x][y]) && (!hasNorthWall(x, y))) ||
                                ((entftab[m][x][y - 1] < entftab[m][x][y]) && (!hasSouthWall(x, y)))) {
                            return entftab[m][x][y] + 1;
                        } else {
                            return entftab[m][x][y] + 2;
                        }
                    }

            }
        }
        return 10000;
        //diese Zeile wird nie erreicht
    }

    public void pr() {
        // nur für Testzwecke
        String wert;
        int laenge;
        for(int m = 0; m < flaggen.length; m++) {
            for(int y = sizeY + 1; y > -1; y--) {
                for(int x = 0; x < sizeX + 2; x++) {
                    wert = "" + entftab[m][x][y];
                    laenge = wert.length();
                    for(int i = 0; i < (5 - laenge); i++) {
                        //Global.debug(" ");
                    }
                    //Global.debug(""+entftab[m][x][y]);
                }
            }
        }
    }

    /**
     *  Die Entfernungsberechnungsfunktion berechnet für jede Flagge den Abstand
     *  von jedem Feld zu dieser Flagge.
     *
     *@param  sizeX   Description of Parameter
     *@param  sizeY   Description of Parameter
     *@param  fahnen  Description of Parameter
     */
    private void berechneEntfernung(int sizeX, int sizeY, Ort fahnen[]) {
        entftab = new int[fahnen.length][][];

        for(int m = 0; m < fahnen.length; ++m) {
            entftab[m] = new int[sizeX + 2][sizeY + 2];

            for(int i = 0; i < sizeX + 2; ++i) {
                for(int j = 0; j < sizeY + 2; ++j) {
                    entftab[m][i][j] = 9999;
                }
            }
        }
        for(int m = 0; m < fahnen.length; ++m) {
            entftab[m][fahnen[m].x][fahnen[m].y] = 0;
            feldEntf(entftab, fahnen[m].x, fahnen[m].y, fahnen[m].x, fahnen[m].y, m);
        }
    }

    private void feldEntf(int entftab[][][], int u, int v, int x, int y, int m) {
        int max = 9999;

        if((u == x) && (v == y)) {
            if((!hasNorthWall(x, y)) && !bo(x, y + 1).isPit() ) {
                entftab[m][x][y + 1] = entftab[m][x][y] + 1;
                feldEntf(entftab, x, y, x, y + 1, m);
            }
            if((!hasEastWall(x, y)) && !bo(x - 1, y).isPit() ) {
                entftab[m][x - 1][y] = entftab[m][x][y] + 1;
                feldEntf(entftab, x, y, x - 1, y, m);
            }
            if((!hasEastWall(x, y)) && !bo(x + 1, y).isPit() ) {
                entftab[m][x + 1][y] = entftab[m][x][y] + 1;
                feldEntf(entftab, x, y, x + 1, y, m);
            }
            if(!(hasSouthWall(x, y)) && !bo(x, y - 1).isPit() ) {
                entftab[m][x][y - 1] = entftab[m][x][y] + 1;
                feldEntf(entftab, x, y, x, y - 1, m);
            }
        }

        if((u == x) && (v != y)) {
            if(!hasNorthWall(x, y) && !bo(x, y + 1).isPit() &&
                    (entftab[m][x][y + 1] > entftab[m][x][y] + 1)) {
                entftab[m][x][y + 1] = entftab[m][x][y] + 1;
                feldEntf(entftab, x, y, x, y + 1, m);
            }
            if(!hasWestWall(x, y) && !bo(x - 1, y).isPit() &&
                    (entftab[m][x - 1][y] > entftab[m][x][y] + 2)) {
                entftab[m][x - 1][y] = entftab[m][x][y] + 2;
                feldEntf(entftab, x, y, x - 1, y, m);
            }
            if(!!hasEastWall(x, y) && !bo(x + 1, y).isPit() &&
                    (entftab[m][x + 1][y] > entftab[m][x][y] + 2)) {
                entftab[m][x + 1][y] = entftab[m][x][y] + 2;
                feldEntf(entftab, x, y, x + 1, y, m);
            }
            if(!hasSouthWall(x, y) && !bo(x, y - 1).isPit() &&
                    (entftab[m][x][y - 1] > entftab[m][x][y] + 1)) {
                entftab[m][x][y - 1] = entftab[m][x][y] + 1;
                feldEntf(entftab, x, y, x, y - 1, m);
            }
        }

        if((u != x) && (v == y)) {
            if(!hasNorthWall(x, y) && !bo(x, y + 1).isPit() &&
                    (entftab[m][x][y + 1] > entftab[m][x][y] + 2)) {
                entftab[m][x][y + 1] = entftab[m][x][y] + 2;
                feldEntf(entftab, x, y, x, y + 1, m);
            }
            if(!hasWestWall(x, y) && !bo(x - 1, y).isPit() &&
                    (entftab[m][x - 1][y] > entftab[m][x][y] + 1)) {
                entftab[m][x - 1][y] = entftab[m][x][y] + 1;
                feldEntf(entftab, x, y, x - 1, y, m);
            }
            if(!hasEastWall(x, y) && !bo(x + 1, y).isPit()  &&
                    (entftab[m][x + 1][y] > entftab[m][x][y] + 1)) {
                entftab[m][x + 1][y] = entftab[m][x][y] + 1;
                feldEntf(entftab, x, y, x + 1, y, m);
            }
            if(!hasSouthWall(x, y) && !bo(x, y - 1).isPit() &&
                    (entftab[m][x][y - 1] > entftab[m][x][y] + 2)) {
                entftab[m][x][y - 1] = entftab[m][x][y] + 2;
                feldEntf(entftab, x, y, x, y - 1, m);
            }
        }
    }


    /**
     *  Liefert eine Referenz auf das SpielfeldKS, Zu verwenden statt
     *  Konstruktor. Garantiert, dass in einer VM nur ein SpielfeldKS
     *  initialisiert wird. Blockiert bis fertig initialisiert.
     *
     *@param  x                     Description of Parameter
     *@param  y                     Description of Parameter
     *@param  kacheln               Description of Parameter
     *@param  flaggen               Description of Parameter
     *@return                       The Instance value
     *@exception  FormatException   Description of Exception
     *@exception  FlaggenException  Description of Exception
     */
    public static synchronized SpielfeldKS getInstance(int x, int y, String kacheln, Ort[] flaggen) throws FormatException, FlaggenException {
        if(uniqueInstance == null) {
            uniqueInstance = new SpielfeldKS(x, y, kacheln, flaggen);
        }
        return uniqueInstance;
    }

}
