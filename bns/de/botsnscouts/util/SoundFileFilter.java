package de.botsnscouts.util;

import java.io.*;

public class SoundFileFilter implements FilenameFilter{


    public boolean accept(File dir, String filename){
	if (filename == null)
	    return false;
	
	filename = filename.toLowerCase();
	
	if (filename.endsWith(".wav")) {
	    return true;
	} 
	else 
	    return false;
	
    } 
    

}
