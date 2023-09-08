/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.resource;

import java.awt.Image;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.binary.BinaryData;

/**
 * Help loading Images using the FileManger.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class ImageManager {

    /**
     * Cannot be created
     */
    public ImageManager() {
        super();
    }

    /**
     * Return the requested image.
     * 
     * @param    aName        The name of the image (including its path).
     * @return   The requested image.
     * @throws   Exception    If getting the image fails for a reason.
     */
    public static Image getImage(String aName) throws Exception {
		BinaryData theFile = FileManager.getInstance().getDataOrNull(aName);
		if (theFile == null)
            return null;
       
		try (InputStream in = theFile.getStream()) {
			return ImageIO.read(in);
		}
    }
}


