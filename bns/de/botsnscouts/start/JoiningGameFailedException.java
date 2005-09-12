/*
 * Created on 09.09.2005
 *
 */
package de.botsnscouts.start;

/**
 * @author Hendrik Steller
 * @version $Id$
 */
public class JoiningGameFailedException extends Exception {

    private Exception possibleReason;
    
    public JoiningGameFailedException (){
        super();
    }
    public JoiningGameFailedException (String reason){
        super(reason);
    }
    public JoiningGameFailedException (Exception reason){
        super();
        possibleReason = reason;
    }
    public Exception getPossibleReason(){
        return possibleReason;
    }
    
}
