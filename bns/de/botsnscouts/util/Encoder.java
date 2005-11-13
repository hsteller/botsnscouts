/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2005 scouties.                                *
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


/*
 * Created on 24.02.2005
 *
 * 
 */
package de.botsnscouts.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.log4j.Category;

/**
 * @author hendrik
 *
 * To reduce the deprecation warnings for using URLEncoder.encode(String)/URLDecoder.decode(String);
 * tp handle all encoding/decoding in one place.. 
 *
 */
public class Encoder {
    
    private static final Category CAT = Category.getInstance(Encoder.class);
    
    public static String encode(String toEncode) {
    	//TODO (replace me plz)
    	/*from the source of URLEncoder:
    	 * @deprecated The resulting string may vary depending on the platform's
	     *             default encoding. Instead, use the encode(String,String)
	     *             method to specify the encoding.*/
    	return URLEncoder.encode(toEncode);
    }
    
    public static String decode (String toDecode){
    	//TODO (replace me plz)
    	return URLDecoder.decode(toDecode);        
    }

    public static String commDecode(String toDecode) {
        try {
            return URLDecoder.decode(toDecode, "utf-8");
        }
        catch (UnsupportedEncodingException ex){
            CAT.error (ex);
            return decode(toDecode);
        }
    }
    
    public static String commEncode (String toEncode){
        try {
            return URLEncoder.encode(toEncode, "utf-8");
        }
        catch (UnsupportedEncodingException ex){
            CAT.error (ex);
            return encode(toEncode);
        }      
    }
    
    public static String propertyEncode(String toEncode) {
        return commEncode(toEncode);
    }
    
    public static String propertyDecode (String toDecode){
        return commDecode(toDecode);        
    }
    
}

