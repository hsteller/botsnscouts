/*
 * Created on 13.09.2005
 *
 *
 */
package de.botsnscouts.server;

/**
 * @version $Id$
 */
public class RegistrationException extends Exception {
    private Exception nested = null;
    
    public RegistrationException() {
        super();
    };
    
    public RegistrationException(String s) {
        super(s);
    }
    
    public RegistrationException(String s, Exception nested) {
        super(s);
        this.nested = nested;
    }
    
    public RegistrationException(Exception nested) {
        this.nested = nested;
    }
    
    public String getMessage() {
        if (nested != null)
            return super.getMessage() + "[" + nested.getMessage() + "]";
        else
            return super.getMessage();
    }
}