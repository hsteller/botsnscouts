package de.spline.rr;


import java.awt.image.*;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;


class CropperField {
    CropImageFilter cropper[][];
    Toolkit comp;
    int grid;
    

    public CropperField(int cols, int rows, int grid,Toolkit comp) {
	cropper = new CropImageFilter[cols][rows];
	this.comp = comp;
	this.grid = grid;
    }
    
    public Image cropImage(Image img, int col, int row) {
	if( cropper[col][row] == null ) 
	    cropper[col][row] = new CropImageFilter(col*grid, row*grid, grid, grid);

	
	return comp.createImage(new FilteredImageSource(img.getSource(), cropper[col][row]));
    }

    
public void multiCrop(Image img, int rowlength, int total, Image[] dest, MediaTracker mt, int imageID) {
	for(int i=0; i<total; i++) {
	    Image image = cropImage(img, i % rowlength, i / rowlength);
	    dest[i] = image;
	    if( mt != null) mt.addImage(image, imageID);
        }
}

}
