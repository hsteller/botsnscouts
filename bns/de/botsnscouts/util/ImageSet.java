package de.spline.rr;

import java.io.*;
import java.awt.*;
import java.awt.image.*;


public class ImageSet {
    String name;
    int size;
    int rowlength;
    
    ImageSet( String aName, int aSize, int aRLength ) {
	name = aName;
	size = aSize;
	rowlength = aRLength;
    }
}
