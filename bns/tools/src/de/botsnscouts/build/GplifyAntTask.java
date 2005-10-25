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
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

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
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        BufferedReader in = null;
        try {
            StringBuffer sb=new StringBuffer();
            in = new BufferedReader(new FileReader(fileWithTextToInsert));
            String s = in.readLine();
            while (s!=null){
                sb.append(s).append('\n');
                s = in.readLine();
            }
            String text = sb.toString().replaceAll(toReplaceWithDate,""+year );
           
            // TODO process the files instead..
            System.out.println(text);
            
            
        }
        catch (IOException ie){
            throw new BuildException("IOException while reading the text to insert; message: "+ie.getMessage());
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
    
 
}
