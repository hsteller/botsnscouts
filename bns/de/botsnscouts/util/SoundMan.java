package de.botsnscouts.util;

import java.applet.Applet;
import java.applet.AudioClip;
import de.botsnscouts.BotsNScouts;

import org.apache.log4j.Category;

/** SoundMan configures, loads and plays the sounds
 *  To add a new sound, simply add a constant and add the filename
 *  to the array at the appropiate position.
 *  If you add a laser sound, add the constant to the laser sound array.
 */
public class SoundMan {
  static Category CAT = Category.getInstance(SoundMan.class);

    /** Sound of robot falling in a pit. */
    public static final int PIT = 0;

    /** Sound of robot laser */
    public static final int LASER1 = 1;

    /** Another Sound of robot laser */
    public static final int LASER2 = 2;

    /** Sound of robot reaching a flag. */
    public static final int FLAG_REACHED = 3;

    /** Sound of a board laser */
    public static final int BOARDLASER = 4;

    /** Sound of a robot pushing another one. */
    public static final int PUSHING = 5;

    /** How to locate the sounds relative to de.botsnscouts.BotsNScouts 
     *  Mind the ordering.
     */
    private static final String[] filenames = {
	"sounds/pit.wav",
	"sounds/laserhit.wav",
	"sounds/laser2.wav",
	"sounds/flag_reached.wav",
	"sounds/boardlaser.wav",
	"sounds/push.wav"
    };

    /** These sounds are robot laser sounds */
    private static final int[] laserSounds = {LASER1, LASER2};

    private static final AudioClip[] sounds = new AudioClip[filenames.length];

    private static int actualLaserSound=0;

    private static boolean soundsLoaded;
    private static boolean soundsEnabled;

  public SoundMan() {
    actualLaserSound=0;
  }

    public static synchronized void playSound(int sound){
	try {
	    if (soundsLoaded && soundsEnabled){
		sounds[sound].play();
	    } 
	}catch (ArrayIndexOutOfBoundsException ex){
	    CAT.error("Invalid sound constant chosen.");
	}
    } 

  public static synchronized void playNextLaserSound(){
      try {
	  playSound(laserSounds[actualLaserSound]);
	  actualLaserSound = ++actualLaserSound%laserSounds.length;
      } catch(ArrayIndexOutOfBoundsException ex){
	  CAT.error("Invalid laser sound constant chosen.");
      }
  }

  public static synchronized void playPitFallSound() {
      playSound(PIT);
  }

  // does it need to be synchronized ..??
  // does it hurt..?? Hmm, guess no ;-)
  public static synchronized void setSoundActive(boolean soundOn){
    soundsEnabled=soundOn;
    if (!soundsLoaded)
	loadSounds();
  }

  public synchronized static boolean isSoundActive(){
    return soundsEnabled&&soundsLoaded;
  }


  public static synchronized void loadSounds() {
      if (soundsLoaded || (!soundsEnabled))
	    return;
	Thread t = new BNSThread () {
		public void run() {
                  try {

		    SoundMan.CAT.debug("Initializing sounds..");

		    for (int i=0; i<sounds.length; i++){
			sounds[i] = loadSound(filenames[i]);
			if (sounds[i] == null)
			    throw new Exception("Sound "+filenames[i]+" was not loaded");
		    }
		    
		    soundsLoaded = true;

		  } catch (Throwable t) {
		    SoundMan.CAT.error("Error occured while loading sounds");
		    SoundMan.CAT.debug(t);
                    SoundMan.CAT.error("sounds deactivated for safety..");
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
