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

import java.awt.image.*;
import java.awt.Image;

import java.awt.*;


public class CropperField2 {
    Image cropper[][];
    Toolkit comp;
    int grid;
    Image img;


    public CropperField2(int cols, int rows, int grid, Image img) {
	cropper = new Image[cols][rows];
//	this.comp = comp;
        this.img = img;
        this.grid = grid;
    }

    public Image cropImage(int col, int row) {
			if( cropper[col][row] != null )
	            return cropper[col][row];
	
	        BufferedImage bi = new BufferedImage( grid, grid, BufferedImage.TYPE_4BYTE_ABGR );
	        Graphics g = bi.createGraphics();
	        g.drawImage( img, 0, 0, grid, grid, col*grid, row*grid, (col+1)*grid, (row+1)*grid, null );
	        g.dispose();
	        return bi;

    }


    public void multiCrop(Image img, int rowlength, int total, Image[] dest,  int imageID)
    {
	for(int i=0; i<total; i++) {
	    Image image = cropImage( i % rowlength, i / rowlength);
	    dest[i] = image;
        }
    }

}
