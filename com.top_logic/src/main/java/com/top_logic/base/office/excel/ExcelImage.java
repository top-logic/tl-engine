/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import java.io.File;

/**
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ExcelImage {

    private File image;
    
    private boolean centerX;
    
    private boolean centerY;

    /**
	 * Create an {@link ExcelImage}.
	 */
    public ExcelImage(File aFile) {
        this(aFile, true, true);
    }

    /**
	 * Create an {@link ExcelImage}.
	 */
    public ExcelImage(File aFile, boolean aCenterX, boolean aCenterY) {
        this.image = aFile;
        this.centerX = aCenterX;
        this.centerY = aCenterY;
    }

    /**
     * Returns the centerX.
     */
    public boolean isCenterX() {
        return (centerX);
    }

    /**
     * Returns the centerY.
     */
    public boolean isCenterY() {
        return (centerY);
    }

    /**
     * Returns the image.
     */
    public File getImage() {
        return (image);
    }

}
