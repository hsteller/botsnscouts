package de.botsnscouts.gui;

import de.botsnscouts.comm.ClientAntwort;


public abstract class AbstractMessageAction  {
  public abstract void invoke(ClientAntwort messageData);

}