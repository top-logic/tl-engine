/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.util;

import java.awt.Color;
import java.awt.GradientPaint;
import java.util.HashMap;
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

	private static final Map<String, Color> CSS_COLORS;

	static {
		Map<String, Color> map = new HashMap<>();
		map.put("black", parseColor("#000000"));
		map.put("silver", parseColor("#c0c0c0"));
		map.put("gray", parseColor("#808080"));
		map.put("white", parseColor("#ffffff"));
		map.put("maroon", parseColor("#800000"));
		map.put("red", parseColor("#ff0000"));
		map.put("purple", parseColor("#800080"));
		map.put("fuchsia", parseColor("#ff00ff"));
		map.put("green", parseColor("#008000"));
		map.put("lime", parseColor("#00ff00"));
		map.put("olive", parseColor("#808000"));
		map.put("yellow", parseColor("#ffff00"));
		map.put("navy", parseColor("#000080"));
		map.put("blue", parseColor("#0000ff"));
		map.put("teal", parseColor("#008080"));
		map.put("aqua", parseColor("#00ffff"));
		map.put("aliceblue", parseColor("#f0f8ff"));
		map.put("antiquewhite", parseColor("#faebd7"));
		map.put("aqua", parseColor("#00ffff"));
		map.put("aquamarine", parseColor("#7fffd4"));
		map.put("azure", parseColor("#f0ffff"));
		map.put("beige", parseColor("#f5f5dc"));
		map.put("bisque", parseColor("#ffe4c4"));
		map.put("black", parseColor("#000000"));
		map.put("blanchedalmond", parseColor("#ffebcd"));
		map.put("blue", parseColor("#0000ff"));
		map.put("blueviolet", parseColor("#8a2be2"));
		map.put("brown", parseColor("#a52a2a"));
		map.put("burlywood", parseColor("#deb887"));
		map.put("cadetblue", parseColor("#5f9ea0"));
		map.put("chartreuse", parseColor("#7fff00"));
		map.put("chocolate", parseColor("#d2691e"));
		map.put("coral", parseColor("#ff7f50"));
		map.put("cornflowerblue", parseColor("#6495ed"));
		map.put("cornsilk", parseColor("#fff8dc"));
		map.put("crimson", parseColor("#dc143c"));
		map.put("cyan", parseColor("#00ffff"));
		map.put("darkblue", parseColor("#00008b"));
		map.put("darkcyan", parseColor("#008b8b"));
		map.put("darkgoldenrod", parseColor("#b8860b"));
		map.put("darkgray", parseColor("#a9a9a9"));
		map.put("darkgreen", parseColor("#006400"));
		map.put("darkgrey", parseColor("#a9a9a9"));
		map.put("darkkhaki", parseColor("#bdb76b"));
		map.put("darkmagenta", parseColor("#8b008b"));
		map.put("darkolivegreen", parseColor("#556b2f"));
		map.put("darkorange", parseColor("#ff8c00"));
		map.put("darkorchid", parseColor("#9932cc"));
		map.put("darkred", parseColor("#8b0000"));
		map.put("darksalmon", parseColor("#e9967a"));
		map.put("darkseagreen", parseColor("#8fbc8f"));
		map.put("darkslateblue", parseColor("#483d8b"));
		map.put("darkslategray", parseColor("#2f4f4f"));
		map.put("darkslategrey", parseColor("#2f4f4f"));
		map.put("darkturquoise", parseColor("#00ced1"));
		map.put("darkviolet", parseColor("#9400d3"));
		map.put("deeppink", parseColor("#ff1493"));
		map.put("deepskyblue", parseColor("#00bfff"));
		map.put("dimgray", parseColor("#696969"));
		map.put("dimgrey", parseColor("#696969"));
		map.put("dodgerblue", parseColor("#1e90ff"));
		map.put("firebrick", parseColor("#b22222"));
		map.put("floralwhite", parseColor("#fffaf0"));
		map.put("forestgreen", parseColor("#228b22"));
		map.put("fuchsia", parseColor("#ff00ff"));
		map.put("gainsboro", parseColor("#dcdcdc"));
		map.put("ghostwhite", parseColor("#f8f8ff"));
		map.put("gold", parseColor("#ffd700"));
		map.put("goldenrod", parseColor("#daa520"));
		map.put("gray", parseColor("#808080"));
		map.put("green", parseColor("#008000"));
		map.put("greenyellow", parseColor("#adff2f"));
		map.put("grey", parseColor("#808080"));
		map.put("honeydew", parseColor("#f0fff0"));
		map.put("hotpink", parseColor("#ff69b4"));
		map.put("indianred", parseColor("#cd5c5c"));
		map.put("indigo", parseColor("#4b0082"));
		map.put("ivory", parseColor("#fffff0"));
		map.put("khaki", parseColor("#f0e68c"));
		map.put("lavender", parseColor("#e6e6fa"));
		map.put("lavenderblush", parseColor("#fff0f5"));
		map.put("lawngreen", parseColor("#7cfc00"));
		map.put("lemonchiffon", parseColor("#fffacd"));
		map.put("lightblue", parseColor("#add8e6"));
		map.put("lightcoral", parseColor("#f08080"));
		map.put("lightcyan", parseColor("#e0ffff"));
		map.put("lightgoldenrodyellow", parseColor("#fafad2"));
		map.put("lightgray", parseColor("#d3d3d3"));
		map.put("lightgreen", parseColor("#90ee90"));
		map.put("lightgrey", parseColor("#d3d3d3"));
		map.put("lightpink", parseColor("#ffb6c1"));
		map.put("lightsalmon", parseColor("#ffa07a"));
		map.put("lightseagreen", parseColor("#20b2aa"));
		map.put("lightskyblue", parseColor("#87cefa"));
		map.put("lightslategray", parseColor("#778899"));
		map.put("lightslategrey", parseColor("#778899"));
		map.put("lightsteelblue", parseColor("#b0c4de"));
		map.put("lightyellow", parseColor("#ffffe0"));
		map.put("lime", parseColor("#00ff00"));
		map.put("limegreen", parseColor("#32cd32"));
		map.put("linen", parseColor("#faf0e6"));
		map.put("magenta", parseColor("#ff00ff"));
		map.put("maroon", parseColor("#800000"));
		map.put("mediumaquamarine", parseColor("#66cdaa"));
		map.put("mediumblue", parseColor("#0000cd"));
		map.put("mediumorchid", parseColor("#ba55d3"));
		map.put("mediumpurple", parseColor("#9370db"));
		map.put("mediumseagreen", parseColor("#3cb371"));
		map.put("mediumslateblue", parseColor("#7b68ee"));
		map.put("mediumspringgreen", parseColor("#00fa9a"));
		map.put("mediumturquoise", parseColor("#48d1cc"));
		map.put("mediumvioletred", parseColor("#c71585"));
		map.put("midnightblue", parseColor("#191970"));
		map.put("mintcream", parseColor("#f5fffa"));
		map.put("mistyrose", parseColor("#ffe4e1"));
		map.put("moccasin", parseColor("#ffe4b5"));
		map.put("navajowhite", parseColor("#ffdead"));
		map.put("navy", parseColor("#000080"));
		map.put("oldlace", parseColor("#fdf5e6"));
		map.put("olive", parseColor("#808000"));
		map.put("olivedrab", parseColor("#6b8e23"));
		map.put("orange", parseColor("#ffa500"));
		map.put("orangered", parseColor("#ff4500"));
		map.put("orchid", parseColor("#da70d6"));
		map.put("palegoldenrod", parseColor("#eee8aa"));
		map.put("palegreen", parseColor("#98fb98"));
		map.put("paleturquoise", parseColor("#afeeee"));
		map.put("palevioletred", parseColor("#db7093"));
		map.put("papayawhip", parseColor("#ffefd5"));
		map.put("peachpuff", parseColor("#ffdab9"));
		map.put("peru", parseColor("#cd853f"));
		map.put("pink", parseColor("#ffc0cb"));
		map.put("plum", parseColor("#dda0dd"));
		map.put("powderblue", parseColor("#b0e0e6"));
		map.put("purple", parseColor("#800080"));
		map.put("rebeccapurple", parseColor("#663399"));
		map.put("red", parseColor("#ff0000"));
		map.put("rosybrown", parseColor("#bc8f8f"));
		map.put("royalblue", parseColor("#4169e1"));
		map.put("saddlebrown", parseColor("#8b4513"));
		map.put("salmon", parseColor("#fa8072"));
		map.put("sandybrown", parseColor("#f4a460"));
		map.put("seagreen", parseColor("#2e8b57"));
		map.put("seashell", parseColor("#fff5ee"));
		map.put("sienna", parseColor("#a0522d"));
		map.put("silver", parseColor("#c0c0c0"));
		map.put("skyblue", parseColor("#87ceeb"));
		map.put("slateblue", parseColor("#6a5acd"));
		map.put("slategray", parseColor("#708090"));
		map.put("slategrey", parseColor("#708090"));
		map.put("snow", parseColor("#fffafa"));
		map.put("springgreen", parseColor("#00ff7f"));
		map.put("steelblue", parseColor("#4682b4"));
		map.put("tan", parseColor("#d2b48c"));
		map.put("teal", parseColor("#008080"));
		map.put("thistle", parseColor("#d8bfd8"));
		map.put("tomato", parseColor("#ff6347"));
		map.put("transparent", parseColor("#00000000"));
		map.put("turquoise", parseColor("#40e0d0"));
		map.put("violet", parseColor("#ee82ee"));
		map.put("wheat", parseColor("#f5deb3"));
		map.put("white", parseColor("#ffffff"));
		map.put("whitesmoke", parseColor("#f5f5f5"));
		map.put("yellow", parseColor("#ffff00"));
		map.put("yellowgreen", parseColor("#9acd32"));
		CSS_COLORS = map;
	}

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

	/**
	 * Parses a CSS color sepcification in the form <code>#rrggbbaa</code> or a well-known color
	 * name such as <code>red</code>.
	 */
	public static Color cssColor(String css) {
		if (css.startsWith("#")) {
			return parseColor(css);
		}

		Color color = CSS_COLORS.get(css);
		if (color != null) {
			return color;
		}

		return Color.black;
	}

	private static Color parseColor(String hashColor) {
		if (hashColor.charAt(0) != '#') {
			return Color.black;
		}
		switch (hashColor.length()) {
			case 4:
				return new Color(
					duplicate(hex(hashColor.charAt(1))),
					duplicate(hex(hashColor.charAt(2))),
					duplicate(hex(hashColor.charAt(3))));
			case 5:
				return new Color(
					duplicate(hex(hashColor.charAt(1))),
					duplicate(hex(hashColor.charAt(2))),
					duplicate(hex(hashColor.charAt(3))),
					duplicate(hex(hashColor.charAt(4))));
			case 7:
				return new Color(hex(hashColor.substring(1)));
			case 9:
				return new Color(hex(hashColor.substring(1)), true);
		}
		return Color.black;
	}

	private static int hex(String hex) {
		return Integer.valueOf(hex, 16);
	}

	private static int duplicate(int hex) {
		return hex | hex << 4;
	}

	private static int hex(char ch) {
		return Character.digit(ch, 16);
	}

}

