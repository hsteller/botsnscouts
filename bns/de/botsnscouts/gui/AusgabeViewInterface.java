package de.botsnscouts.gui;

import de.botsnscouts.util.*;
/**
 * Diese Klasse erledigt die Ausgabe von Spielfeld und Status
 * @author Lukasz Pekacki
 */

public interface AusgabeViewInterface {
    
    /**
     * Schreibt in die Statuszeile einen Text
     */
    public void showActionMessage(String s);     
    
    
    /**
     *  Zentriert das Fenster auf die angegebene Position
     */
    public void showPos (int x, int y);

    /**
     *  shows the winner list at game over
     */
    public void showWinnerlist (String[] winners);

    /**
     *  Behandelt das Fensterschlieﬂen
     */
    public void shutup();


    /**
     * Statusleiste der spielenden Roboter
     */
    public void showRobStatus(Roboter r);

}




