package de.botsnscouts.start;

import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.geom.*;
import javax.swing.border.*;
import java.net.*;
import javax.swing.filechooser.FileFilter;
import java.util.*;
import java.io.*;
import de.botsnscouts.util.*;

class SpfFilter implements FilenameFilter{
    public SpfFilter(){}
    public boolean accept(File dir, String name){
	try{
	    // endsWith(".spf") ???
	    return name.toLowerCase().endsWith(".spf");
	} catch(Throwable t){return false;}
    }
}
