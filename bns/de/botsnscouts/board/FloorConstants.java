/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2001 scouties.                                    *
 * Contact botsnscouts-devel@sf.net                                *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 2 of the License.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program, in a file called COPYING in the top
 directory of the Bots 'n' Scouts distribution; if not, write to 
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330, 
 Boston, MA  02111-1307  USA
 
 *******************************************************************/
 
package de.botsnscouts.board;


public interface FloorConstants {
  /** Boden Grube */
  public static final int BDGRUBE   = -1;
  /** Boden normaler Boden */
  public static final int BDNORMAL  =  0;
  /** Boden Reparaturfeld */
  public static final int BDREPA    =  1;
  /** Boden DrehElement */
  public static final int BDDREHEL  = 10;

  /** Drehelementrichtung im Uhrzeigersinn */
  public static final int DUHRZ   = 0;
  /** Drehelementrichtung gegen den Uhrzeigersinn */
  public static final int DGGUHRZ = 1;

  /* Fliessbaenderdesign : Endziffern stehen fuer: */
  /** Fliessband Richtung Norden */
  public static final int FNORD = 0;
  /** Fliessband Richtung Osten */
  public static final int FOST  = 1;
  /** Fliessband Richtung Süden */
  public static final int FSUED = 2;
  /** Fliessband Richtung Westen */
  public static final int FWEST = 3;

  // Fliessbänder - geradeaus
  // ========================
  /** Fliessband Richtung Norden, Geschwindigkeit 1 */
  public static final int FN1 = 100;
  /** Fliessband Richtung Osten,  Geschwindigkeit 1 */
  public static final int FO1 = 101;
  /** Fliessband Richtung Sueden, Geschwindigkeit 1 */
  public static final int FS1 = 102;
  /** Fliessband Richtung Westen, Geschwindigkeit 1 */
  public static final int FW1 = 103;

  /** Fliessband Richtung Norden, Geschwindigkeit 2 */
  public static final int FN2 = 200; //
  /** Fliessband Richtung Osten,  Geschwindigkeit 2 */
  public static final int FO2 = 201; //
  /** Fliessband Richtung Sueden, Geschwindigkeit 2 */
  public static final int FS2 = 202; //
  /** Fliessband Richtung Westen, Geschwindigkeit 2 */
  public static final int FW2 = 203; //

    // Fliessbaender - abbiegen -  normal

  /** Fließband Abbiegen (Richtung) Norden von Westen*/
  public static final int NVW1 = 120;
  /** Fließband Abbiegen (Richtung) Norden von Osten */
  public static final int NVO1 = 130;
  /** Fließband Abbiegen (Richtung) Osten von Norden */
  public static final int OVN1 = 121;
  /** Fließband Abbiegen (Richtung) Osten von Sueden */
  public static final int OVS1 = 131;
  /** Fließband Abbiegen (Richtung) Süden von Westen */
  public static final int SVW1 = 132;
  /** Fließband Abbiegen (Richtung) Süden von Osten */
  public static final int SVO1 = 122;
  /** Fließband Abbiegen (Richtung) Westen von Norden */
  public static final int WVN1 = 133;
  /** Fließband Abbiegen (Richtung) Westen von Süden */
  public static final int WVS1 = 123;
  /** Fließband Abbiegen (Richtung)  Norden von  Westen oder Osten */
  public static final int NVWO1 = 150;
  /** Fließband Abbiegen (Richtung)  Osten von   Norden oder Süden */
  public static final int OVNS1 = 151;
  /** Fließband Abbiegen (Richtung)  Süden von  Westen oder Osten */
  public static final int SVWO1 = 152;
  /** Fließband Abbiegen (Richtung)  Westen von  Nord oder Süden */
  public static final int WVNS1 = 153;

  // Fliessbaender - abbiegen - express
  //
  /** Express-Fließband Abbiegen (Richtung) Norden von Westen (kommend) */
  public static final int NVW2 = 220;
  /** Express-Fließband Abbiegen (Richtung) Norden von Osten */
  public static final int NVO2 = 230;
  /** Express-Fließband Abbiegen (Richtung) Osten von Norden */
  public static final int OVN2 = 221;
  /** Express-Fließband Abbiegen (Richtung) Osten von Sueden */
  public static final int OVS2 = 231;
  /** Express-Fließband Abbiegen (Richtung) Süden von Westen */
  public static final int SVW2 = 232;
  /** Express-Fließband Abbiegen (Richtung) Süden von Osten */
  public static final int SVO2 = 222;
  /** Express-Fließband Abbiegen (Richtung) Westen von Norden */
  public static final int WVN2 = 233;
  /** Express-Fließband Abbiegen (Richtung) Westen von Süden */
  public static final int WVS2 = 223;
  /** Express-Fließband Abbiegen (Richtung)  Norden von  Westen oder Osten */
  public static final int NVWO2 = 250;
  /** Express-Fließband Abbiegen (Richtung)  Osten von   Norden oder Süden */
  public static final int OVNS2 = 251;
  /** Express-Fließband Abbiegen (Richtung)  Süden von  Westen oder Osten */
  public static final int SVWO2 = 252;
  /** Express-Fließband Abbiegen (Richtung)  Westen von  Nord oder Süden */
  public static final int WVNS2 = 253;
}