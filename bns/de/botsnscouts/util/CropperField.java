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
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;

public class CropperField {
    CropImageFilter cropper[][];

    Toolkit comp;

    int grid;

    public CropperField(int cols, int rows, int grid, Toolkit comp) {
        cropper = new CropImageFilter[cols][rows];
        this.comp = comp;
        this.grid = grid;
    }

    public Image cropImage(Image img, int col, int row) {
        if (cropper[col][row] == null)
            cropper[col][row] = new CropImageFilter(col * grid, row * grid, grid, grid);

        return comp.createImage(new FilteredImageSource(img.getSource(), cropper[col][row]));
    }

    public void multiCrop(Image img, int rowlength, int total, Image[] dest, MediaTracker mt, int imageID) {
        for (int i = 0; i < total; i++) {
            Image image = cropImage(img, i % rowlength, i / rowlength);
            dest[i] = image;
            if (mt != null)
                mt.addImage(image, imageID);
        }
    }

}
