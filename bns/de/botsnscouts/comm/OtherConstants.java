package de.botsnscouts.comm;

import de.botsnscouts.util.Directions;

public interface OtherConstants {

   // public static final String REASON_QUIT           = "GONE";
    public static final String REASON_LOST_LIVES     = "LL";
    public static final String REASON_TIMEOUT        = "TO";
    public static final String REASON_RULE_VIOLATION = "RV";
    public static final String REASON_SERVER_SHUTDOWN = "server shutdown";
    
    public static final String REQUEST_PUSHERS_PUSH_MULTIPLE = "ISPPMB";

  // for sending a sequence number in a message use MESSAGE_NUMBER=1234;
  // should be put into the last field if the message's String array
  // or appended in front of a notifychange in the form MESSAGE_NUMBER=1234,NTC(..)
  //                                     appended part: ^^^^^^^^^^^^^^^^^^^^
  public static final String MESSAGE_NUMBER = "message_number";

public static final int BOT_TURN_CLOCKWISE = Directions.BOT_TURN_CLOCKWISE;
public static final int BOT_TURN_COUNTER_CLOCKWISE=Directions.BOT_TURN_COUNTER_CLOCKWISE;

}