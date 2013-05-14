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

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;

import org.apache.log4j.Category;

import com.sixlegs.image.png.PngImage;

abstract class ImageSet {
    static final Category CAT = Category.getInstance(ImageSet.class);

    

    static final Toolkit TOOLKIT = Toolkit.getDefaultToolkit();

    String name;

    int size;

    int rowlength;

    ImageSet(String aName, int aSize, int aRLength) {
        if (CAT.isDebugEnabled())
            CAT.debug(aName + " [" + getClass().getName() + "]");
        name = aName;
        size = aSize;
        rowlength = aRLength;
    }

    abstract Image getImage() throws IOException;

    static ImageSet getInstance(String resourceName, int aSize, int aRLength) {
        if (resourceName.endsWith(".png"))
            return new PngImageSet(resourceName, aSize, aRLength);
        else
            return new DefaultImageSet(resourceName, aSize, aRLength);
    }

    // Concrete ImageSet classes
    static class DefaultImageSet extends ImageSet {
        DefaultImageSet(String aName, int aSize, int aRLength) {
            super(aName, aSize, aRLength);
        }

        Image getImage() {
            return TOOLKIT.getImage(de.botsnscouts.BotsNScouts.class.getResource(name));
        }
    }

    static class PngImageSet extends ImageSet {
        PngImageSet(String aName, int aSize, int aRLength) {
            super(aName, aSize, aRLength);
        }

        Image getImage() throws IOException {
            return TOOLKIT.createImage(new PngImage(de.botsnscouts.BotsNScouts.class.getResource(name)));
        }
    }
}
