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

package de.botsnscouts.util;

import java.awt.*;
import java.util.Hashtable;

import de.botsnscouts.gui.SACanvas;
import de.botsnscouts.gui.OldRobotStatusImpl;

/**
 * Holds the mapping of the color and the image of a robot
 * to the visual ID of this robot
 * @author Lukasz Pekacki
 */

public class BotVis {
  private static Hashtable playerBotVisHash;
     public static final Color GREEN  = new Color(4,156,52);
    public static final Color YELLOW = new Color(251,253,4);
    public static final Color RED    = new Color(252,2,4);
    public static final Color BLUE   = new Color(4,2,250);
    public static final Color ROSA   = new Color(251,2,251);
    public static final Color ORANGE = new Color(233,94,4);
    public static final Color GRAY   = new Color(220,222,220);
    public static final Color VIOLET = new Color(155,2,203);
  public static final Color [] robocolor = {GREEN,YELLOW,RED,BLUE,ROSA,ORANGE,GRAY,VIOLET};

  public static final int [] roboLaserSound = { SoundMan.LASER_GREEN,
                                                SoundMan.LASER_YELLOW,
                                                SoundMan.LASER_RED,
                                                SoundMan.LASER_BLUE,
                                                SoundMan.LASER_ROSA,
                                                SoundMan.LASER_ORANGE,
                                                SoundMan.LASER_GRAY,
                                                SoundMan.LASER_VIOLET
                                              };

  public static void initBotVis( Hashtable hash ) {
    playerBotVisHash = hash;
  }




  public static int getBotVisByName( String name ) throws IllegalStateException {
    if( playerBotVisHash == null )
      throw new IllegalStateException( "BotVis not yet initialized!" );

    return ((Integer)(playerBotVisHash.get( name ))).intValue();
  }

  public static Color getBotColorByName(String name){
    return SACanvas.robocolor[getBotVisByName(name)];
  }

  public static Image getBotIconByName(String name) {
    return OldRobotStatusImpl.getRobotImages()[getBotVisByName(name)];
  }

  public static int getBotLaserSoundByName(String name) {
    return roboLaserSound[getBotVisByName(name)];
  }

/*

    public static final Color GREEN  = new Color(4,156,52);
    public static final Color YELLOW = new Color(251,253,4);
    public static final Color RED    = new Color(252,2,4);
    public static final Color BLUE   = new Color(4,2,250);
    public static final Color ROSA   = new Color(251,2,251);
    public static final Color ORANGE = new Color(233,94,4);
    public static final Color GRAY   = new Color(220,222,220);
    public static final Color VIOLET = new Color(155,2,203);

    public static final Color[] color = {GREEN,YELLOW,RED,BLUE,ROSA,ORANGE,GRAY,VIOLET};
    public static final Image[] images 	= ImageMan.getImages( ImageMan.ROBOS );


    public Color getColor(int visualID) {
	return color[visualID];
    }

    public Image getImage (int visualID) {
	return images[visualID*4];
    }

    public Image getFacing (int visualID, int facing) {
	return images[(visualID*4)+facing];
    }
*/
}



