/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import java.awt.Color;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link Format} parsing the hexcode of a color (#rrggbb) into the corresponding {@link Color}
 * object.
 * 
 * <p>
 * The three-digit variants (#rgb) are also supported for convenience.
 * </p>
 * 
 * <p>
 * Colors with alpha values are also supported (#rrggbbaa) including the short-cut variant (#rgba).
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ColorFormat extends Format {

	/**
	 * Name for the full transparent color.
	 */
	public static final String TRANSPARENT = "transparent";

	/**
	 * Singleton {@link ColorFormat} instance.
	 */
	public static final ColorFormat INSTANCE = new ColorFormat();

	/**
	 * Mapping of known color values to their color codes.
	 */
	public static Map<String, Integer> COLORS = colors(
		TRANSPARENT, "#00000000",
		"AliceBlue", "#F0F8FF",
		"AntiqueWhite", "#FAEBD7",
		"Aqua", "#00FFFF",
		"Aquamarine", "#7FFFD4",
		"Azure", "#F0FFFF",
		"Beige", "#F5F5DC",
		"Bisque", "#FFE4C4",
		"Black", "#000000",
		"BlanchedAlmond", "#FFEBCD",
		"Blue", "#0000FF",
		"BlueViolet", "#8A2BE2",
		"Brown", "#A52A2A",
		"BurlyWood", "#DEB887",
		"CadetBlue", "#5F9EA0",
		"Chartreuse", "#7FFF00",
		"Chocolate", "#D2691E",
		"Coral", "#FF7F50",
		"CornflowerBlue", "#6495ED",
		"Cornsilk", "#FFF8DC",
		"Crimson", "#DC143C",
		"Cyan", "#00FFFF",
		"DarkBlue", "#00008B",
		"DarkCyan", "#008B8B",
		"DarkGoldenRod", "#B8860B",
		"DarkGray", "#A9A9A9",
		"DarkGrey", "#A9A9A9",
		"DarkGreen", "#006400",
		"DarkKhaki", "#BDB76B",
		"DarkMagenta", "#8B008B",
		"DarkOliveGreen", "#556B2F",
		"DarkOrange", "#FF8C00",
		"DarkOrchid", "#9932CC",
		"DarkRed", "#8B0000",
		"DarkSalmon", "#E9967A",
		"DarkSeaGreen", "#8FBC8F",
		"DarkSlateBlue", "#483D8B",
		"DarkSlateGray", "#2F4F4F",
		"DarkSlateGrey", "#2F4F4F",
		"DarkTurquoise", "#00CED1",
		"DarkViolet", "#9400D3",
		"DeepPink", "#FF1493",
		"DeepSkyBlue", "#00BFFF",
		"DimGray", "#696969",
		"DimGrey", "#696969",
		"DodgerBlue", "#1E90FF",
		"FireBrick", "#B22222",
		"FloralWhite", "#FFFAF0",
		"ForestGreen", "#228B22",
		"Fuchsia", "#FF00FF",
		"Gainsboro", "#DCDCDC",
		"GhostWhite", "#F8F8FF",
		"Gold", "#FFD700",
		"GoldenRod", "#DAA520",
		"Gray", "#808080",
		"Grey", "#808080",
		"Green", "#008000",
		"GreenYellow", "#ADFF2F",
		"HoneyDew", "#F0FFF0",
		"HotPink", "#FF69B4",
		"IndianRed", "#CD5C5C",
		"Indigo", "#4B0082",
		"Ivory", "#FFFFF0",
		"Khaki", "#F0E68C",
		"Lavender", "#E6E6FA",
		"LavenderBlush", "#FFF0F5",
		"LawnGreen", "#7CFC00",
		"LemonChiffon", "#FFFACD",
		"LightBlue", "#ADD8E6",
		"LightCoral", "#F08080",
		"LightCyan", "#E0FFFF",
		"LightGoldenRodYellow", "#FAFAD2",
		"LightGray", "#D3D3D3",
		"LightGrey", "#D3D3D3",
		"LightGreen", "#90EE90",
		"LightPink", "#FFB6C1",
		"LightSalmon", "#FFA07A",
		"LightSeaGreen", "#20B2AA",
		"LightSkyBlue", "#87CEFA",
		"LightSlateGray", "#778899",
		"LightSlateGrey", "#778899",
		"LightSteelBlue", "#B0C4DE",
		"LightYellow", "#FFFFE0",
		"Lime", "#00FF00",
		"LimeGreen", "#32CD32",
		"Linen", "#FAF0E6",
		"Magenta", "#FF00FF",
		"Maroon", "#800000",
		"MediumAquaMarine", "#66CDAA",
		"MediumBlue", "#0000CD",
		"MediumOrchid", "#BA55D3",
		"MediumPurple", "#9370DB",
		"MediumSeaGreen", "#3CB371",
		"MediumSlateBlue", "#7B68EE",
		"MediumSpringGreen", "#00FA9A",
		"MediumTurquoise", "#48D1CC",
		"MediumVioletRed", "#C71585",
		"MidnightBlue", "#191970",
		"MintCream", "#F5FFFA",
		"MistyRose", "#FFE4E1",
		"Moccasin", "#FFE4B5",
		"NavajoWhite", "#FFDEAD",
		"Navy", "#000080",
		"OldLace", "#FDF5E6",
		"Olive", "#808000",
		"OliveDrab", "#6B8E23",
		"Orange", "#FFA500",
		"OrangeRed", "#FF4500",
		"Orchid", "#DA70D6",
		"PaleGoldenRod", "#EEE8AA",
		"PaleGreen", "#98FB98",
		"PaleTurquoise", "#AFEEEE",
		"PaleVioletRed", "#DB7093",
		"PapayaWhip", "#FFEFD5",
		"PeachPuff", "#FFDAB9",
		"Peru", "#CD853F",
		"Pink", "#FFC0CB",
		"Plum", "#DDA0DD",
		"PowderBlue", "#B0E0E6",
		"Purple", "#800080",
		"RebeccaPurple", "#663399",
		"Red", "#FF0000",
		"RosyBrown", "#BC8F8F",
		"RoyalBlue", "#4169E1",
		"SaddleBrown", "#8B4513",
		"Salmon", "#FA8072",
		"SandyBrown", "#F4A460",
		"SeaGreen", "#2E8B57",
		"SeaShell", "#FFF5EE",
		"Sienna", "#A0522D",
		"Silver", "#C0C0C0",
		"SkyBlue", "#87CEEB",
		"SlateBlue", "#6A5ACD",
		"SlateGray", "#708090",
		"SlateGrey", "#708090",
		"Snow", "#FFFAFA",
		"SpringGreen", "#00FF7F",
		"SteelBlue", "#4682B4",
		"Tan", "#D2B48C",
		"Teal", "#008080",
		"Thistle", "#D8BFD8",
		"Tomato", "#FF6347",
		"Turquoise", "#40E0D0",
		"Violet", "#EE82EE",
		"Wheat", "#F5DEB3",
		"White", "#FFFFFF",
		"WhiteSmoke", "#F5F5F5",
		"Yellow", "#FFFF00",
		"YellowGreen", "#9ACD32");

	private ColorFormat() {
		// Singleton constructor.
	}
	
	/** 
	 * Returns the {@link StringBuffer} toAppendTo lenghtened by the hex code of the given color.
	 * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer, java.text.FieldPosition)
	 */
	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		if (toAppendTo == null || pos == null){
			throw new NullPointerException("\"pos\" and \"toAppendTo\" have to be non-null");
		}
		else{
			try {
				Color color = (Color) obj;
				if (color.getAlpha() == 0) {
					return toAppendTo.append(TRANSPARENT);
				}
				String hex = hexValue(color);
				return toAppendTo.append(hex);
			}
			catch (ClassCastException e){
				throw new IllegalArgumentException("The object \"obj\" has to be a java.awt.Color object");
			}
		}
	}

	/** 
	 * Returns the color coded by the hex code in the source beginning from the index of
	 * the ParsePosition. If the source is to short, the errorIndex of the ParsePosition
	 * is set to the length of the source
	 * 
	 * @see java.text.Format#parseObject(java.lang.String, java.text.ParsePosition)
	 */
	@Override
	public Object parseObject(String source, ParsePosition pos) {
		if (pos == null)
			throw new NullPointerException("pos is null");
		int index = pos.getIndex();
		if (index >= source.length()) {
			setError(pos, index);
			return null;
		}

		int code;
		if (source.charAt(index) == '#') {
			index++;

			Integer codeOrError = parse(pos, source, index);
			if (codeOrError == null) {
				return null;
			} else {
				code = codeOrError.intValue();
			}
		} else {
			Integer knownCode = lookupColorName(source, index);
			if (knownCode != null) {
				code = knownCode.intValue();
				pos.setIndex(source.length());
			} else {
				setError(pos, index);
				return null;
			}
		}

		Color c = colorValue(code);
		return c;
	}

	private Integer lookupColorName(String source, int index) {
		return COLORS.get(source.substring(index).toLowerCase());
	}

	private static Integer parse(ParsePosition pos, String source, int index) {
		int code = 0;
		for (int cnt = 0; cnt < 8; cnt++) {
			if (index >= source.length()) {
				return processShortCut(pos, index, cnt, code);
			}
			char ch = Character.toUpperCase(source.charAt(index));
			
			int digit;
			if (Character.isDigit(ch)) {
				digit = ch - '0';
			} else if (ch == 'A' || ch == 'B' || ch == 'C' || ch == 'D' || ch == 'E' || ch == 'F') {
				digit = ch - 'A' + 10;
			} else {
				return processShortCut(pos, index, cnt, code);
			}

			code = (code << 4) | digit;
			index++;
		}

		setIndex(pos, index);
		return moveAlpha(code);
	}

	private static Integer processShortCut(ParsePosition pos, int index, int cnt, int code) {
		setIndex(pos, index);
		if (cnt == 3) {
			// Short format without alpha.
			return addAlpha(expand(code));
		} else if (cnt == 4) {
			// Short format with alpha.
			return moveAlpha(expand(code));
		} else if (cnt == 6) {
			// Normal format without alpha.
			return addAlpha(code);
		} else {
			setError(pos, index);
			return null;
		}
	}

	private static void setError(ParsePosition pos, int index) {
		if (pos != null) {
			pos.setErrorIndex(index);
		}
	}

	private static int moveAlpha(int rrggbbaa) {
		int aa = rrggbbaa & 0x000000FF;
		int rrggbb = rrggbbaa >>> 8;
		return (aa << 24) | rrggbb;
	}

	private static void setIndex(ParsePosition pos, int index) {
		if (pos != null) {
			pos.setIndex(index);
		}
	}

	private static int addAlpha(int rgb) {
		return 0xff000000 | rgb;
	}

	private static int expand(int code) {
		int r = code & 0x000F;
		int g = code & 0x00F0;
		int b = code & 0x0F00;
		int a = code & 0xF000;
		return r | (r << 4) | (g << 4) | (g << 8) | (b << 8) | (b << 12) | (a << 12) | (a << 16);
	}

	private String hexValue(Color color) {
		String red = hex(color.getRed());
		String green = hex(color.getGreen());
		String blue = hex(color.getBlue());
		int alphaValue = color.getAlpha();
		if (alphaValue != 255) {
			String alpha = hex(alphaValue);
			return "#" + red + green + blue + alpha;
		} else {
			return "#" + red + green + blue;
		}
	}

	private String hex(int value) {
		int high = value & 0xF0;
		int low = value & 0x0F;
		return "" + hexDigit(high >>> 4) + hexDigit(low);
	}

	private char hexDigit(int digit) {
		return (char) (digit <= 9 ? '0' + digit : 'A' + (digit - 10));
	}

	private Color colorValue(int code) {
		return new Color(code, true);
	}

	private static Map<String, Integer> colors(String... colors) {
		Map<String, Integer> result = new HashMap<>();
		for (int n = 0, cnt = colors.length; n < cnt; n+=2) {
			String color = colors[n + 1];
			int code = parse(null, color, 1);
			result.put(colors[n].toLowerCase(), code);
		}
		return Collections.unmodifiableMap(result);
	}

	/**
	 * Parses the given color definition.
	 */
	public static Color parseColor(String colorSpec) {
		try {
			return (Color) ColorFormat.INSTANCE.parseObject(colorSpec);
		} catch (ParseException ex) {
			throw new IllegalArgumentException("Not a color spec: " + colorSpec, ex);
		}
	}

	/**
	 * Formats the given color.
	 */
	public static String formatColor(Color color) {
		return ColorFormat.INSTANCE.format(color);
	}
}
