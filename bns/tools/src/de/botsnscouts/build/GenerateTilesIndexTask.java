package de.botsnscouts.build;

import java.io.*;
import java.util.*;
import org.apache.tools.ant.*;

public class GenerateTilesIndexTask extends Task{
    private String dir;

    public void setDir(String d){ dir=d; }

    public void execute() throws BuildException{
	try{
	    new GenerateTilesIndex().generate(dir);
	}catch(Throwable t){
	    t.printStackTrace(System.err);
	    throw new BuildException();
	}
    }
}
