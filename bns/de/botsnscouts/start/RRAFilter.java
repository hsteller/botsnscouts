package de.botsnscouts.start;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.net.*;
import de.botsnscouts.board.*;
import de.botsnscouts.util.*;

class RRAFilter implements FilenameFilter{

    public RRAFilter(){}

    public boolean accept(File dir, String name){
	try{
	    // endsWith(".rra") ???
	    boolean isRRA=name.toLowerCase().endsWith(".rra");
	    return isRRA;
	} catch(Throwable t){return false;}
    }

}
