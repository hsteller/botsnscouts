package de.spline.rr;

/**
 * ConfigLoadException wird bei Problemen beim Lesen von config.txt
 * oder fehlender config.txt geworfen.
 * @author: Gero Eggers
 */

public class ConfigLoadException extends Exception {
  public ConfigLoadException () {
    super();
  }
  public ConfigLoadException (String s) {
    super (s);
  }
  
}
