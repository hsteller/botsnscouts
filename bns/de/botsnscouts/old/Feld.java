package de.botsnscouts.start;

/** Ein einzelnes Spielfeld.
 * @author: Dirk Materlik, Gero Eggers
 */

public class Feld
{
  /** Die Koordinaten des Feldes. */
  public int x,y;


    /** Es existiert links (im Westen) eine Wand */
    public boolean l_exist;
    
    /** Es existiert rechts (im Westen) eine Wand */
    public boolean r_exist;
    
    /** Es existiert oben (im Norden) eine Wand */
    public boolean o_exist;
    
    /** Es existiert unten (im Sueden) eine Wand */
    public boolean u_exist;
    

    /** Kennziffer des 1. Wand-Elements in der Wand links */
    public int l_WandEl1;
    
    /** Kennziffer des 1. Wand-Elements in der Wand rechts */
    public int r_WandEl1;
    
    /** Kennziffer des 1. Wand-Elements in der Wand oben */
    public int o_WandEl1;
    
    /** Kennziffer des 1. Wand-Elements in der Wand unten */
    public int u_WandEl1;

    /** Kennziffer des 2. Wand-Elements in der Wand links */
    public int l_WandEl2;
    
    /** Kennziffer des 2. Wand-Elements in der Wand rechts */
    public int r_WandEl2;
    
    /** Kennziffer des 2. Wand-Elements in der Wand oben */
    public int o_WandEl2;
    
    /** Kennziffer des 2. Wand-Elements in der Wand unten */
    public int u_WandEl2;

    
    /** Spezifikations-Kennziffer des 1. Wand-Elements in der Wand links */
    public int l_WandEl1Spez;

    /** Spezifikations-Kennziffer des 1. Wand-Elements in der Wand rechts */
    public int r_WandEl1Spez;

    /** Spezifikations-Kennziffer des 1. Wand-Elements in der Wand oben */
    public int o_WandEl1Spez;

    /** Spezifikations-Kennziffer des 1. Wand-Elements in der Wand unten */
    public int u_WandEl1Spez;

    /** Spezifikations-Kennziffer des 2. Wand-Elements in der Wand links */
    public int l_WandEl2Spez;

    /** Spezifikations-Kennziffer des 2. Wand-Elements in der Wand rechts */
    public int r_WandEl2Spez;

    /** Spezifikations-Kennziffer des 2. Wand-Elements in der Wand oben */
    public int o_WandEl2Spez;

    /** Spezifikations-Kennziffer des 2. Wand-Elements in der Wand unten */
    public int u_WandEl2Spez;
    
    /** Kennziffer des Boden-Typs */
    public int bodenTyp;
    /** Spezifikations-Kennziffer des Bodens (enthält Crusherphasen, und Staerke des Reperaturfelds) */
    public int bodenSpez;
    

  /***** Protected Konstanten *****/

  // Wandgerätetypen
  // ================
   
  /** Kein Wandgerät in der Wand */
  protected static final int WKEINS = 0;
  /**  Laser in der Wand */
  protected static final int WLASER = 1;
  /**  Pusher in der Wand */
  protected static final int WPUSHER = 2;
  
  //  Bodentypen
  //  ==========
  
  /** Boden Grube */
  protected static final int BDGRUBE   = -1;
  /** Boden normaler Boden */
  protected static final int BDNORMAL  =  0;
  /** Boden Reparaturfeld */
  protected static final int BDREPA    =  1;
  /** Boden DrehElement */
  protected static final int BDDREHEL  = 10;

  /** Drehelementrichtung im Uhrzeigersinn */
  protected static final int DUHRZ   = 0;
  /** Drehelementrichtung gegen den Uhrzeigersinn */
  protected static final int DGGUHRZ = 1;

  /* Fliessbaenderdesign : Endziffern stehen fuer: */
  /** Fliessband Richtung Norden */
  protected static final int FNORD = 0;
  /** Fliessband Richtung Osten */
  protected static final int FOST  = 1;
  /** Fliessband Richtung Süden */
  protected static final int FSUED = 2;
  /** Fliessband Richtung Westen */
  protected static final int FWEST = 3;


    // Fliessbänder 
    // =============
    // Anmerkung zur Kennziffernvergabe:
    // nn0 Richtung Norden
    // nn1 Richtung Osten
    // nn2 Richtung Sueden
    // nn3 Richtung Westen
    // n0n  "normales" Fliessband
    // n1n  Crusher
    // n2n "linkskurve"
    // n3n "rechtskurve
    // n5n  Einbiegen aus zwei Richtungen 
    // 1nn  Fliessbandgeschwindigkeit ist 1
    // 2nn  Fliessbandgeschwindigkeit ist 2
 
  // Fliessbänder - geradeaus
  // ========================
  /** Fliessband Richtung Norden, Geschwindigkeit 1 */
  protected static final int FN1 = 100;
  /** Fliessband Richtung Osten,  Geschwindigkeit 1 */
  protected static final int FO1 = 101;
  /** Fliessband Richtung Sueden, Geschwindigkeit 1 */
  protected static final int FS1 = 102;
  /** Fliessband Richtung Westen, Geschwindigkeit 1 */
  protected static final int FW1 = 103;

  /** Fliessband Richtung Norden, Geschwindigkeit 2 */
  protected static final int FN2 = 200; //
  /** Fliessband Richtung Osten,  Geschwindigkeit 2 */
  protected static final int FO2 = 201; // 
  /** Fliessband Richtung Sueden, Geschwindigkeit 2 */
  protected static final int FS2 = 202; // 
  /** Fliessband Richtung Westen, Geschwindigkeit 2 */
  protected static final int FW2 = 203; // 

  // Fliessbaender - Crusher
  // =======================
  // in der dritten Dimension (indizes 1-n) von Boden
  // sind die aktiven Registerphasen abgelegt

  /** Crusher (Fliessband) Richtung Norden, Geschwindigkeit 1 */
  protected static final int CRN1 = 110;
  /** Crusher (Fliessband) Richtung Osten,  Geschwindigkeit 1 */
  protected static final int CRO1 = 111; 
  /** Crusher (Fliessband) Richtung Sueden, Geschwindigkeit 1 */
  protected static final int CRS1 = 112; 
  /** Crusher (Fliessband) Richtung Westen, Geschwindigkeit 1 */
  protected static final int CRW1 = 113; 
  /** Crusher (Fliessband) Richtung Norden, Geschwindigkeit 2 */
  protected static final int CRN2 = 210; 
  /** Crusher (Fliessband) Richtung Osten,  Geschwindigkeit 2 */
  protected static final int CRO2 = 211; 
  /** Crusher (Fliessband) Richtung Sueden, Geschwindigkeit 2 */
  protected static final int CRS2 = 212; 
  /** Crusher (Fliessband) Richtung Westen, Geschwindigkeit 2 */
  protected static final int CRW2 = 213; 

  // Fliessbaender - abbiegen -  normal
    //
  /** Fließband Abbiegen (Richtung) Norden von Westen*/
  protected static final int NVW1 = 120;   
  /** Fließband Abbiegen (Richtung) Norden von Osten */
  protected static final int NVO1 = 130;   
  /** Fließband Abbiegen (Richtung) Osten von Norden */
  protected static final int OVN1 = 121;   
  /** Fließband Abbiegen (Richtung) Osten von Sueden */
  protected static final int OVS1 = 131;   
  /** Fließband Abbiegen (Richtung) Süden von Westen */
  protected static final int SVW1 = 132;   
  /** Fließband Abbiegen (Richtung) Süden von Osten */
  protected static final int SVO1 = 122;    
  /** Fließband Abbiegen (Richtung) Westen von Norden */
  protected static final int WVN1 = 133;   
  /** Fließband Abbiegen (Richtung) Westen von Süden */
  protected static final int WVS1 = 123;   
  /** Fließband Abbiegen (Richtung)  Norden von  Westen oder Osten */
  protected static final int NVWO1 = 150;   
  /** Fließband Abbiegen (Richtung)  Osten von   Norden oder Süden */
  protected static final int OVNS1 = 151;   
  /** Fließband Abbiegen (Richtung)  Süden von  Westen oder Osten */
  protected static final int SVWO1 = 152;   
  /** Fließband Abbiegen (Richtung)  Westen von  Nord oder Süden */
  protected static final int WVNS1 = 153;           

  // Fliessbaender - abbiegen - express
  //
  /** Express-Fließband Abbiegen (Richtung) Norden von Westen (kommend) */
  protected static final int NVW2 = 220;   
  /** Express-Fließband Abbiegen (Richtung) Norden von Osten */
  protected static final int NVO2 = 230;   
  /** Express-Fließband Abbiegen (Richtung) Osten von Norden */
  protected static final int OVN2 = 221;   
  /** Express-Fließband Abbiegen (Richtung) Osten von Sueden */
  protected static final int OVS2 = 231;   
  /** Express-Fließband Abbiegen (Richtung) Süden von Westen */
  protected static final int SVW2 = 232;   
  /** Express-Fließband Abbiegen (Richtung) Süden von Osten */
  protected static final int SVO2 = 222;    
  /** Express-Fließband Abbiegen (Richtung) Westen von Norden */
  protected static final int WVN2 = 233;   
  /** Express-Fließband Abbiegen (Richtung) Westen von Süden */
  protected static final int WVS2 = 223;   
  /** Express-Fließband Abbiegen (Richtung)  Norden von  Westen oder Osten */
  protected static final int NVWO2 = 250;   
  /** Express-Fließband Abbiegen (Richtung)  Osten von   Norden oder Süden */
  protected static final int OVNS2 = 251;   
  /** Express-Fließband Abbiegen (Richtung)  Süden von  Westen oder Osten */
  protected static final int SVWO2 = 252;   
  /** Express-Fließband Abbiegen (Richtung)  Westen von  Nord oder Süden */
  protected static final int WVNS2 = 253;

  /** Gibt true zurueck, wenn ein Crusher mit Spezifikationszahl spez
    in phase aktiv ist. False sonst.
    */
  public boolean isCrusherActive(int spez,int phase)
    {
      return((spez>>(phase-1))%2==1);
    }
  /** Gibt true zurueck, wenn ein Pusher mit Spezifikationszahl spez
    in phase aktiv ist. False sonst.
    */
  public boolean isPusherActive(int spez,int phase)
    {
      return((spez>>(phase-1))%2==1);
    }



}




