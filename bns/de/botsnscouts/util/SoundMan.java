package de.botsnscouts.util;

import java.applet.Applet;
import java.applet.AudioClip;
import de.botsnscouts.BotsNScouts;

import org.apache.log4j.Category;


public class SoundMan {
  static Category CAT = Category.getInstance(SoundMan.class);


  private static int actualLaserSound=0;
  private static int numberOfLaserSounds=2;

  private static AudioClip [] laserSounds = new AudioClip[numberOfLaserSounds];
  private static AudioClip pitSound;

  private static boolean soundsLoaded;
  private static boolean soundsActive;

  public SoundMan() {
    actualLaserSound=0;
  }

  public static synchronized void playNextLaserSound(){
    if (soundsLoaded&&soundsActive){
      laserSounds[actualLaserSound].play();
      actualLaserSound = ++actualLaserSound%numberOfLaserSounds;
    }
  }

  public static synchronized void playPitFallSound() {
    if (soundsLoaded&&soundsActive)
      pitSound.play();
  }

  // does it need to be synchronized ..??
  // does it hurt..?? Hmm, guess no ;-)
  public static synchronized void setSoundActive(boolean soundOn){
    soundsActive=soundOn;
  }

  public synchronized static boolean isSoundActive(){
    return soundsActive&&soundsLoaded;
  }


  public static synchronized void loadSounds() {
	if (soundsLoaded)
	    return;
	Thread t = new Thread () {
		public void run() {
                  try {

		    CAT.debug("Initializing sounds..");
	            soundsLoaded=false;

                    pitSound = loadSound("sounds/pit.wav");
		    laserSounds [0] = loadSound("sounds/laserhit.wav");
		    laserSounds [1] = loadSound("sounds/laser2.wav");

                    boolean error = false;
		    if (laserSounds [0] == null) {
			CAT.warn("laserhit.wav not located :-(");
			error=true;
		    }
		    if (laserSounds [1] == null){
			CAT.warn("laser2.wav not located :-(");
			error = true;
		    }
		    if (error)
			CAT.error("Failed to load sounds; sounds deactivated");
		    else{
			CAT.debug("Sounds loaded!");
			soundsLoaded=true;
		    }
                 }
                 catch (Throwable t) {
                    CAT.error("Error occured while loading sounds");
                    CAT.error("sounds deactivated for safety..");
                    soundsLoaded=false;
                 }
		}
	    };
	t.start();
    }

    private static AudioClip loadSound(String relPath){
      AudioClip a;
      if (CAT.isDebugEnabled())
        CAT.debug("loading sound: "+relPath);
      a = Applet.newAudioClip(BotsNScouts.class.getResource(relPath));
      if (CAT.isDebugEnabled())
        CAT.debug("loaded");
      return a;
    }
}