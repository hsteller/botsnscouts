package de.botsnscouts.comm;

public interface MessageID {

  public static final String AUSWERTUNG      = "mAusw";

  public static final String BOT_CRUSHED     = "mBotCrushed";
  public static final String BOT_IN_PIT      = "mGrubenopfer";
  public static final String BOT_LASER       = "mRobLaser";
  public static final String WISE_USED       = "mKlugSchKlick";
  public static final String BORD_LASER_SHOT = "mBoardLaser";
  public static final String FLAG_REACHED    = "mNextFlag";
  public static final String CHAT            = "mChat";
  public static final String PROG_DONE       = "mProgReceived";
  public static final String LAST_PROG       = "mLastProg";
  public static final String BOT_MOVE            = AUSWERTUNG+"Move";


  public static final String SOMEONE_QUIT    = "mAbmeldung";
  public static final String BOT_REMOVED     = "mHinr";

  public static final String SIGNAL_ACTION_START = "mSignalStart";
  public static final String SIGNAL_ACTION_STOP  = "mSignalStop";



}