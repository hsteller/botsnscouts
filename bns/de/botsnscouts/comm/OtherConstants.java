package de.botsnscouts.comm;

public interface OtherConstants {

   // public static final String REASON_QUIT           = "GONE";
    public static final String REASON_LOST_LIVES     = "LL";
    public static final String REASON_TIMEOUT        = "TO";
    public static final String REASON_RULE_VIOLATION = "RV";

  // for sending a sequence number in a message use MESSAGE_NUMBER=1234;
  // should be put into the last field if the message's String array
  // or appended in front of a notifychange in the form MESSAGE_NUMBER=1234,NTC(..)
  //                                     appended part: ^^^^^^^^^^^^^^^^^^^^
  public static final String MESSAGE_NUMBER = "message_number";

public static final int BOT_TURN_CLOCKWISE = 1;
public static final int BOT_TURN_COUNTER_CLOCKWISE=2;

}