/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.charsize;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

/**
 * Generic {@link CharSizeMap} for installed fonts.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class FontCharSizeMap implements CharSizeMap {

    /** The font to use for this CharSizeMap. */
    private final Font font;

    /** Is internal needed for computing character size. */
    private final FontRenderContext frc;

    /** The size of the reference character. */
    private final double referenceSize;


    /**
     * Creates a new instance of this class for the given font.
     */
    public FontCharSizeMap(String fontName) {
        this(new Font(fontName, Font.PLAIN, 12));
    }

    /**
     * Creates a new instance of this class, using the given parameters to create the font.
     * See {@link Font#Font(String,int,int)} for parameters.
     */
    public FontCharSizeMap(String fontName, int style) {
        this(new Font(fontName, style, 12));
    }

    /**
     * Creates a new instance of this class, using the given parameters to create the font.
     * See {@link Font#Font(String,int,int)} for parameters.
     */
    public FontCharSizeMap(String fontName, int style, int size) {
        this(new Font(fontName, style, size));
    }

    /**
     * Creates a new instance of this class using the given font.
     */
    public FontCharSizeMap(Font font) {
        this.font = font;
        frc = new FontRenderContext(new AffineTransform(), false, true);
        referenceSize = font.getStringBounds(Character.toString(REFERENCE_CHARACTER), frc).getWidth();
    }


    /**
     * This method returns the font.
     */
    public Font getFont() {
        return this.font;
    }



    @Override
	public double getSize(char character) {
        return getSize(Character.toString(character));
    }

    @Override
	public double getSize(String string) {
        double width = string == null ? 0 : font.getStringBounds(string, frc).getWidth();
        return width / referenceSize;
    }
}
