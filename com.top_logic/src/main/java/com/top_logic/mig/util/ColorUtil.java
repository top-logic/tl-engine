/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.util;

import java.awt.Color;
import java.awt.GradientPaint;
import java.util.Map;

import com.top_logic.basic.col.MapBuilder;

/**
 * The ColorUtil contains useful static methods for colors.
 *
 * TODO move to tl-basic. Add darker/brighter wit a Factor as found
 *      in {@link Color}.
 *
 * @author  <a href="mailto:tdi@top-logic.com">Thomas Dickhut</a>
 */
public class ColorUtil {

    /**color name usable in theme (may be used in css color attributes) */
    public static final String THEME_COLOR_BLACK   = "black";
    /**color name usable in theme (may be used in css color attributes) */
    public static final String THEME_COLOR_MAROON  = "maroon";
    /**color name usable in theme (may be used in css color attributes) */
    public static final String THEME_COLOR_GREEN   = "green";
    /**color name usable in theme (may be used in css color attributes) */
    public static final String THEME_COLOR_OLIVE   = "olive";
    /**color name usable in theme (may be used in css color attributes) */
    public static final String THEME_COLOR_NAVY    = "navy";
    /**color name usable in theme (may be used in css color attributes) */
    public static final String THEME_COLOR_PURPLE  = "purple";
    /**color name usable in theme (may be used in css color attributes) */
    public static final String THEME_COLOR_TEAL    = "teal";
    /**color name usable in theme (may be used in css color attributes) */
    public static final String THEME_COLOR_SILVER  = "silver";
    /**color name usable in theme (may be used in css color attributes) */
    public static final String THEME_COLOR_GRAY    = "gray";
    /**color name usable in theme (may be used in css color attributes) */
    public static final String THEME_COLOR_RED     = "red";
    /**color name usable in theme (may be used in css color attributes) */
    public static final String THEME_COLOR_LIME    = "lime";
    /**color name usable in theme (may be used in css color attributes) */
    public static final String THEME_COLOR_YELLOW  = "yellow";
    /**color name usable in theme (may be used in css color attributes) */
    public static final String THEME_COLOR_BLUE    = "blue";
    /**color name usable in theme (may be used in css color attributes) */
    public static final String THEME_COLOR_FUCHSIA = "fuchsia";
    /**color name usable in theme (may be used in css color attributes) */
    public static final String THEME_COLOR_AQUA    = "aqua";
    /**color name usable in theme (may be used in css color attributes) */
    public static final String THEME_COLOR_WHITE   = "white";

    /** prefix used for theme colors given as r-g-b values */
    public static final String THEME_COLOR_R_G_B_PREFIX = "#";

    public static final Map THEME_COLOR_MAP = (new MapBuilder())
                                                    .put(THEME_COLOR_BLACK,   Color.BLACK)
                                                    .put(THEME_COLOR_MAROON,  new Color(0x80, 0x00, 0x00))
                                                    .put(THEME_COLOR_GREEN,   new Color(0x00, 0x80, 0x00))
                                                    .put(THEME_COLOR_OLIVE,   new Color(0x80, 0x80, 0x00))
                                                    .put(THEME_COLOR_NAVY,    new Color(0x00, 0x00, 0x80))
                                                    .put(THEME_COLOR_PURPLE,  new Color(0x80, 0x00, 0x80))
                                                    .put(THEME_COLOR_TEAL,    new Color(0x00, 0x80, 0x80))
                                                    .put(THEME_COLOR_SILVER,  new Color(0xc0, 0xc0, 0xc0))
                                                    .put(THEME_COLOR_GRAY,    new Color(0x80, 0x80, 0x80))
                                                    .put(THEME_COLOR_RED,     new Color(0xff, 0x00, 0x00))
                                                    .put(THEME_COLOR_LIME,    new Color(0x00, 0xFF, 0x00))
                                                    .put(THEME_COLOR_YELLOW,  new Color(0xFF, 0xFF, 0x00))
                                                    .put(THEME_COLOR_BLUE,    new Color(0x00, 0x00, 0xFF))
                                                    .put(THEME_COLOR_FUCHSIA, new Color(0xFF, 0x00, 0xFF))
                                                    .put(THEME_COLOR_AQUA,    new Color(0x00, 0xFF, 0xFF))
                                                    .put(THEME_COLOR_WHITE,   Color.WHITE)
                                                    .toMap();

    /**
     * This method returns for the given color and intensive value a darker
     * color. E.g. the given color and the returned color can be used for a
     * {@link java.awt.GradientPaint}.
     *
     * This methods reduces brightness linear for all colors, consider using
     * {@link Color#darker()} which uses a different approach.
     *
     * @param anIntensiveValue
     *        A intensive value (0..255).
     * @param aColor
     *        A {@link Color}. Must not be <code>null</code>.
     * @return Returns a darker color as the given color.
     */
    public static Color getDarkerColor(Color aColor, int anIntensiveValue) {
        int theRed   = aColor.getRed()  - anIntensiveValue > 0 ? aColor.getRed()   - anIntensiveValue : 0;
        int theGreen = aColor.getGreen()- anIntensiveValue > 0 ? aColor.getGreen() - anIntensiveValue : 0;
        int theBlue  = aColor.getBlue() - anIntensiveValue > 0 ? aColor.getBlue()  - anIntensiveValue : 0;

        return new Color(theRed, theGreen, theBlue);
    }

    /**
     * This method returns for the given color and intensive value a brighter
     * color. E.g. the given color and the returned color can be used for a
     * {@link java.awt.GradientPaint}.
     *
     * This methods reduces brightness linear for all colors, consider using
     * {@link Color#brighter()} which uses a different approach.
     *
     * @param anIntensiveValue
     *        A intensive value (0..255).
     * @param aColor
     *        A {@link Color}. Must not be <code>null</code>.
     * @return Returns a brighter color as the given color.
     */
    public static Color getBrighterColor(Color aColor, int anIntensiveValue) {
        int theRed   = aColor.getRed()  + anIntensiveValue < 256 ? aColor.getRed()   + anIntensiveValue : 255;
        int theGreen = aColor.getGreen()+ anIntensiveValue < 256 ? aColor.getGreen() + anIntensiveValue : 255;
        int theBlue  = aColor.getBlue() + anIntensiveValue < 256 ? aColor.getBlue()  + anIntensiveValue : 255;

        return new Color(theRed, theGreen, theBlue);
    }

    public static Color adaptColorIntensity(Color aColor, int anIntensiveValue) {
        if (anIntensiveValue > 0) {
            return getBrighterColor(aColor, anIntensiveValue);
        } else {
            return getDarkerColor(aColor, -anIntensiveValue);
        }
    }

    /**
     * This method returns for the given color and intensive value a
     * {@link GradientPaint} with zero-filled coordinates values (x1, y1, x2,
     * y2).
     *
     * @param anIntensiveValue
     *        A intensive value (0..255).
     * @param aColor
     *        A {@link Color}. Must not be <code>null</code>.
     * @return Returns for the given color and intensive value a
     *         {@link GradientPaint}.
     */
    public static GradientPaint getGradientPaintFor(Color aColor, int anIntensiveValue) {
        return new GradientPaint(0, 0, aColor, 0, 0, getDarkerColor(aColor, anIntensiveValue));
    }

}

