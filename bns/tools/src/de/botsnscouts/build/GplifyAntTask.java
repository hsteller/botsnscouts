
/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2005 scouties.                                    *
 * Contact botsnscouts-devel@sf.net                                *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 2 of the License.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program, in a file called COPYING in the top
 directory of the Bots 'n' Scouts distribution; if not, write to 
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
 Boston, MA  02111-1307  USA
 
 *******************************************************************/

package de.botsnscouts.build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

/**
 * 
 * Ant ant task that is supposed to traverse through our source tree and to add the GPL 
 * to each .java-file that doesn't already contain it.
 * 
 * @author Hendrik Steller
 * @version $Id$
 */
public class GplifyAntTask extends Task {
    
   /** Only process files with that ending; expected to be lowercase */ 
    private static final String FILE_EXT_FILTER = ".java";
    
    /** The file that contains the text to be added to the beginning of each .java file (the GPL) */
    private File fileWithTextToInsert;
    
    /** Don't add the text to files that contain this String */
    private String ignoreFilesWithThisText;
    
    /**If <code> fileWithTextToInsert</code> contains this String, it will be replaced with
     * the current year.    
     * */ 
    private String toReplaceWithDate;
    
    public void setIgnorewithtext(String s){
        ignoreFilesWithThisText = s;
        
    }
    
    /** The placeholder in <code>gpltext</code> that should be replaced with the current year.
     * 
     * @param token a regular expression the will be passed to {@link: java.lang.String#replaceAll(String,String)}
     */
    public void setReplacewithyear (String token){
        toReplaceWithDate = token;
    }
    
    public void setGplfile (File gpltext){
        fileWithTextToInsert = gpltext;
    }
    
    public void execute() throws BuildException{
        // stuff that could also go into the init-methode; but since execute() will be called
        // only once it doesn't really matter
        Project bnsproject = getProject();
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        String textToInsert = null;
    
       
        try {
            StringBuffer sb = readFileLineWiseIntoBuffer(fileWithTextToInsert);
            textToInsert = sb.toString().replaceAll(toReplaceWithDate,""+year );
        }
        catch (IOException ie){
            bnsproject.log("error reading the text to insert: "+ie.getMessage(), Project.MSG_ERR);
            throw new BuildException(ie);
        }
        
        bnsproject.log("adding text: ",Project.MSG_INFO);
        bnsproject.log(textToInsert, Project.MSG_INFO);
        
        File projectRoot = getProject().getBaseDir();
        FileFilter recursiveOnlyJava = createFilter();
        Collection filesToCheck = null;
        try {
            filesToCheck = collectFiles(projectRoot, recursiveOnlyJava);
        }
        catch (IOException ie){
            bnsproject.log("failed to create the list of files to process; IOException: "+ie.getMessage());
            throw new BuildException(ie);
        }
        
        boolean haveIgnoreText = ignoreFilesWithThisText != null 
        									  && !ignoreFilesWithThisText.trim().equals("");
       
        // for info messages: 
        Collection filesSkippedDueToError = new LinkedList();
        Collection filesIgnored = new LinkedList();
        try {
	        for (Iterator fs = filesToCheck.iterator();fs.hasNext(); ){
	            File f = (File) fs.next();
	            String content = null;
	            try { // trying to read the original context of the file:
	                StringBuffer sb = readFileLineWiseIntoBuffer(f);
	                content = sb.toString();
	                int pos = -1;
	                if (haveIgnoreText) {
	                    pos = sb.indexOf(ignoreFilesWithThisText);
	                }
	                if (pos < 0) { // we were either asked to process all files or the 
	                    // text that disqualifies files from being processed wasn't found in this file
	                    content = sb.toString();
	                }
	                else {
	                    filesIgnored.add(f.getAbsolutePath());                    
	                    content = null;
	                }
	            }
	            catch (IOException ie){
	                bnsproject.log("IOException during read: "+ie.getMessage(), Project.MSG_ERR);
	                bnsproject.log("ERROR - SKIPPED: "+f.getAbsolutePath(), Project.MSG_ERR);     
	                filesSkippedDueToError.add(f.getAbsolutePath());
	                content = null;
	            }
	            if (content != null) { // no error and file is not to be excluded from processing
	                try {
	                    bnsproject.log("Processing: "+f.getAbsolutePath()+"..", Project.MSG_INFO);
	                    PrintWriter out = new PrintWriter(new FileWriter(f, false));
	                    out.println(textToInsert);
	                    out.println();
	                    out.println(content);
	                    out.close();                    
	                }
	                catch (IOException ie){
	                    bnsproject.log("MAYDAY! I PROBABLY F*CKED UP: "
	                                    +f.getAbsolutePath()+" !!!", Project.MSG_ERR);
	                    bnsproject.log("Original content was: ", Project.MSG_ERR);
	                    bnsproject.log(content, Project.MSG_ERR);
	                    throw new BuildException("Quitting because of IO-error during file write:"
	                                    +ie.getMessage());
	                }
	               
	            }
	           
	
	        } // for
        }
        finally {
            int ignSize = filesIgnored.size();
            if (ignSize>0){
                for (Iterator it=filesIgnored.iterator();it.hasNext();){
                    bnsproject.log("ignored: "+it.next(), Project.MSG_INFO);
                }
            }
            int errSize = filesSkippedDueToError.size();
            if (errSize>0){
                for (Iterator it=filesSkippedDueToError.iterator();it.hasNext();){
                    bnsproject.log("SKIPPED (error): "+it.next(), Project.MSG_INFO);
                }
            }
            else {
                bnsproject.log("no errors", Project.MSG_INFO);
            }
        }
            
        
    }
    
    /** 
     * Will create a Collection of File objects:<br>
     * Starting in directory <code>curDir</code> it will add every file that matches <code>filter</code>.<br>  
     * Note: to process subdirectories, <code>filter</code> will have to accept directories.<br>
     * 
     * @param curDir The directory to start with
     * @param filter Collect only those files that are accepted by this filter
     * @return A Collection of File objects for all files that were accepted 
     * @throws IOException if <code>curDir</code> does not exist 
     */
    public static  Collection collectFiles (File curDir, FileFilter filter) throws IOException {    	        
    	if (curDir == null || !curDir.exists()){    	        	    
    	    throw new IOException("No such Directory: \""+curDir+"\"");
    	}    	    	    	
    	Collection files = new LinkedList();
    	Vector dirsToDo=new Vector();
    	dirsToDo.add(curDir);
    	while (dirsToDo.size()>0) {
    	    curDir = (File) dirsToDo.remove(0);    	        	    
    	    File [] all  = curDir.listFiles(filter);   	    
    	    for (int i=0;i<all.length;i++){
    	        if (all[i].isDirectory()) {
    	            dirsToDo.add(all[i]);
    	        }
    	        else {    	                	            	               
	                files.add(all[i]);    	           
    	        }    	               
    	    }    		    		    	
        }
    	return files;
    }
    

    /**
     *
     * Reads the content of the text file <code>file</code> into a StringBuffer;
     * will add a line break after each line (after each call of<code> BufferedReader.readLine()</code>) 
     * 
     *  
     * @param file A text file 
     * @return The content of the text file in a StringBuffer
     * @throws IOException if I/O stuff goes wrong while trying to read the file 
     */
    public static StringBuffer readFileLineWiseIntoBuffer(File file) throws IOException{
        BufferedReader in = null;
        StringBuffer sb=new StringBuffer();
        try {            
            in = new BufferedReader(new FileReader(file));
            String s = in.readLine();
            while (s!=null){
                sb.append(s).append('\n');
                s = in.readLine();
            }
            return sb;
        }
        finally { 
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ie) {                   
                }
            }
        }
    }
    
    /**
     * Creates a filter to determine the files to be 'gplified'.
     * 
     * @return a filter to determine the files to be 'gplified'
     */
    public static FileFilter createFilter() {
        FileFilter fil = new FileFilter(){                        
            public boolean accept(File f){
                return f.isDirectory() || 
                		  f.getName().toLowerCase().endsWith(FILE_EXT_FILTER);                	    
            }
            
            public String getDescription(){
                return "Directories and files ending with \""+FILE_EXT_FILTER+"\"";
            }
            
        };
        return fil;        
    }
 
}
