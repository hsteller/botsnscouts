package de.botsnscouts.util;

import java.io.*;
import java.awt.*;
import java.awt.image.*;

import com.sixlegs.image.png.PngImage;

import org.apache.log4j.Category;

abstract class ImageSet {
    static final Category CAT = Category.getInstance( ImageSet.class );
    static final Class CLASS = de.botsnscouts.BotsNScouts.class;
    static final Toolkit TOOLKIT = Toolkit.getDefaultToolkit();

    String name;
    int size;
    int rowlength;

    ImageSet( String aName, int aSize, int aRLength ) {
        if( CAT.isDebugEnabled() )
            CAT.debug( aName + " [" + getClass().getName() + "]" );
	name = aName;
	size = aSize;
	rowlength = aRLength;
    }

    abstract Image getImage() throws IOException;

    static ImageSet getInstance(String resourceName, int aSize, int aRLength) {
        if( resourceName.endsWith(".png") )
            return new PngImageSet( resourceName, aSize, aRLength );
        else
            return new DefaultImageSet( resourceName, aSize, aRLength );
    }

    // Concrete ImageSet classes
    static class DefaultImageSet extends ImageSet {
        DefaultImageSet( String aName, int aSize, int aRLength ) {
            super( aName, aSize, aRLength );
        }

        Image getImage() {
            return TOOLKIT.getImage( CLASS.getResource( name ) );
        }
    }

    static class PngImageSet extends ImageSet {
        PngImageSet( String aName, int aSize, int aRLength ) {
            super( aName, aSize, aRLength );
        }

        Image getImage() throws IOException {
            return TOOLKIT.createImage(
                new PngImage( CLASS.getResource( name ) ));
        }
    }
}
