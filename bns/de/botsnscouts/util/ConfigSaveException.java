package de.botsnscouts.util;

/**
 * ConfigSaveException wird Problemen beim Speichern in config.txt 
 * oder fehlender config.txt geworfen.
 *
 */

public class ConfigSaveException extends Exception {
  public ConfigSaveException () {
    super();
  }
  public ConfigSaveException (String s) {
    super (s);
  }
  
}
