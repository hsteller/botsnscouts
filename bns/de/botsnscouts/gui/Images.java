package de.botsnscouts.gui;


import java.awt.*;
import java.io.*;
import java.awt.image.*;
/**
 * Diese Klasse lädt die verwendeten Bilder
 * andere Klassen können die static-Images über sie erreichen
 * @author [lp]
 */
public class Images {

    // Scout
    static Class c = de.botsnscouts.BotsNScouts.class;
    public  final static  Image SCOUTSCHLAF = Toolkit.getDefaultToolkit().getImage(c.getResource("images/scoutschlaf.gif"));
    public  final static  Image SCOUT = Toolkit.getDefaultToolkit().getImage(c.getResource("images/scout.gif"));
    // Klugscheisser
    public  final static  Image KSCHEISSER = Toolkit.getDefaultToolkit().getImage(c.getResource("images/kscheisser.gif"));
    public  final static  Image KSCHLAF = Toolkit.getDefaultToolkit().getImage(c.getResource("images/kschlaf.gif"));

}
