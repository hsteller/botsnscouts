package de.botsnscouts.start;

/**
 * @author Miriam Busch - <miriam.busch@codimi.de>
 */
public class UnableToAnnounceGameException extends Exception {

    public UnableToAnnounceGameException() {
        super();
    }

    public UnableToAnnounceGameException(String mess) {
        super(mess);
    }

}
