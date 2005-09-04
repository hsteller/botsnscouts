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

package de.botsnscouts.start;

import javax.swing.DefaultComboBoxModel;

import org.apache.log4j.Category;


import de.botsnscouts.util.Message;
import de.botsnscouts.widgets.TJComboBox;

class RoboBox extends TJComboBox {
    

    
    private boolean withEgal;

    public RoboBox(boolean egal) {
        String[] farben;

        withEgal = egal;
       // setOpaque(true);

        if (withEgal) {
            farben = new String[]{Message.say("Start", "mFarbeEgal"), Message.say("Start", "mFarbeGruen"), Message.say("Start", "mFarbeGelb"), Message.say("Start", "mFarbeRot"), Message.say("Start", "mFarbeBlau"), Message.say("Start", "mFarbeMagenta"), Message.say("Start", "mFarbeOrange"), Message.say("Start", "mFarbeGrau"), Message.say("Start", "mFarbeDunkelMagenta")};
        } else {
            farben = new String[]{Message.say("Start", "mFarbeGruen"), Message.say("Start", "mFarbeGelb"), Message.say("Start", "mFarbeRot"), Message.say("Start", "mFarbeBlau"), Message.say("Start", "mFarbeMagenta"), Message.say("Start", "mFarbeOrange"), Message.say("Start", "mFarbeGrau"), Message.say("Start", "mFarbeDunkelMagenta")};
        }

        setModel(new DefaultComboBoxModel(farben));       
        setRenderer(new RoboCellRenderer(true));
        setLightWeightPopupEnabled(false);
        setSelectedIndex(0);
    }
}
	
