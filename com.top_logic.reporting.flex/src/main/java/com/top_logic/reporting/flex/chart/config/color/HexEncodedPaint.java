/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.color;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.form.format.ColorFormat;

/**
 * The class {@link HexEncodedPaint} serializes a {@link Color} with alpha or a
 * {@link GradientPaint} by their hex encoding.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
public class HexEncodedPaint extends AbstractConfigurationValueProvider<Paint> {

	private static final String COLOR = "[0-9A-Fa-f]{6}";

	private static final String ALPHA = "[0-9A-Fa-f]{2}";

	private static final String COLOR_WITH_APHPA = "#" + "(" + COLOR + ")" + "(" + ALPHA + ")?";

	private static final String S = "\\s*";

	private static final Pattern PATTERN =
		Pattern.compile(COLOR_WITH_APHPA + "(?:" + S + "-" + S + COLOR_WITH_APHPA + ")?");

	/** Singleton instance of {@link HexEncodedPaint} */
	public static final HexEncodedPaint INSTANCE = new HexEncodedPaint();

	private HexEncodedPaint() {
		super(Paint.class);
	}

	@Override
	protected String getSpecificationNonNull(Paint configValue) {
		if (configValue instanceof Color) {
			Color color = (Color) configValue;
			int alpha = color.getAlpha();
			if (alpha < 255) {
				String hexAlpha = "0" + Integer.toHexString(alpha);
				hexAlpha = hexAlpha.substring(hexAlpha.length() - 2);
				return ColorFormat.formatColor(color) + hexAlpha;
			}
			else {
				return ColorFormat.formatColor(color);
			}
		}
		else if (configValue instanceof GradientPaint) {
			Color c1 = ((GradientPaint) configValue).getColor1();
			Color c2 = ((GradientPaint) configValue).getColor2();
			return ColorFormat.formatColor(c1) + "-" + ColorFormat.formatColor(c2);
		}
		else {
			throw new UnsupportedOperationException("Paint not supported: " + configValue);
		}
	}

	@Override
	protected Paint getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		Matcher matcher = PATTERN.matcher(propertyValue);
		if (!matcher.matches()) {
			throw new ConfigurationException(
				I18NConstants.ERROR_INVALID_PAINT_FORMAT, propertyName, propertyValue);
		}
		
		String color1 = matcher.group(1);
		String alpha1 = matcher.group(2);
		Color startColor = parseColor(color1, alpha1);

		String color2 = matcher.group(3);
		String alpha2 = matcher.group(4);
		if (color2 != null) {
			Color endColor = parseColor(color2, alpha2);
			return new GradientPaint(0f, 0f, startColor, 100f, 100f, endColor);
		} else {
			return startColor;
		}
	}

	private Color parseColor(String colorPart, String alphaPart) {
		int alpha;
		if (alphaPart == null) {
			alpha = 0xFF;
		} else {
			alpha = Integer.parseInt(alphaPart, 16);
		}
		return new Color(alpha << 24 | Integer.parseInt(colorPart, 16), true);
	}

	/**
	 * Parses the given {@link Paint} value.
	 * 
	 * @param encodedPaint
	 *        {@link Paint} in the format <code>#RRGGBB[AA][-#RRGGBB[AA]]</code>.
	 * @return The parsed {@link Paint} instance.
	 */
	public static Paint getPaint(String encodedPaint) {
		try {
			return INSTANCE.getValue(null, encodedPaint);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}
}
