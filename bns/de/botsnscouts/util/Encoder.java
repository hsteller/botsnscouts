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
        return URLEncoder.encode(toEncode);
    }
    
    public static String decode (String toDecode){
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
