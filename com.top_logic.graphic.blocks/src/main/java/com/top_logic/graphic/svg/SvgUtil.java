/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.svg;

import java.awt.Color;

/**
 * Utility methods for SVG creation.
 */
public class SvgUtil {

	public static String valueList(double[] values) {
		StringBuilder buffer = new StringBuilder();
		boolean first = true;
		for (double x : values) {
			if (first) {
				first = false;
			} else {
				buffer.append(' ');
			}
			buffer.append(x);
		}
		return buffer.toString();
	}

	/**
	 * Converts the given {@link Color} to a style value usable in HTML.
	 */
	public static String html(Color color) {
		StringBuilder buffer = new StringBuilder();
		buffer.append('#');
		appendHex2(buffer, color.getRed());
		appendHex2(buffer, color.getGreen());
		appendHex2(buffer, color.getBlue());
		int alpha = color.getAlpha();
		if (alpha < 255) {
			appendHex2(buffer, alpha);
		}
		return buffer.toString();
	}

	private static void appendHex2(StringBuilder buffer, int value) {
		String hex = Integer.toHexString(value);
		if (hex.length() < 2) {
			buffer.append('0');
		}
		buffer.append(hex);
	}

}
