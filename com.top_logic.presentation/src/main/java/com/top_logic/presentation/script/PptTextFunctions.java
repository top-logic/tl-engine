/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.script;

import java.awt.Font;
import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.DoubleDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.presentation.text.TextMeasure;
import com.top_logic.presentation.text.TextMetrics;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions for laying out text on PowerPoint slides.
 *
 * <p>
 * All lengths are given in points (1/72 inch), font sizes as well.
 * </p>
 */
@ScriptPrefix("ppt")
public class PptTextFunctions extends TLScriptFunctions {

	/**
	 * Key of the width of the widest line in the result of a text measurement.
	 */
	public static final String WIDTH = "width";

	/**
	 * Key of the number of lines in the result of a text measurement.
	 */
	public static final String LINES = "lines";

	/**
	 * Key of the height of the text in the result of a text measurement.
	 */
	public static final String HEIGHT = "height";

	/**
	 * Key of the height of a single line in the result of a text measurement.
	 */
	public static final String LINE_HEIGHT = "lineHeight";

	/**
	 * Key of the flag in the result of a text measurement telling whether the font is installed.
	 */
	public static final String FONT_AVAILABLE = "fontAvailable";

	/**
	 * Measures the space a text takes on a PowerPoint slide.
	 *
	 * <p>
	 * The text is laid out exactly like in a text box without inner margins that is rendered into
	 * an image. Each line break in the text starts a new paragraph. If a width is given, the text
	 * is wrapped at word boundaries to fit this width, otherwise only at its line breaks.
	 * </p>
	 *
	 * <p>
	 * The result is a dictionary with the following entries:
	 * </p>
	 *
	 * <ul>
	 * <li>{@link #WIDTH}: The width of the widest line in points.</li>
	 * <li>{@link #LINES}: The number of lines after wrapping.</li>
	 * <li>{@link #HEIGHT}: The height of the text in points. A text box needs this height plus its
	 * inner top and bottom margins to show the whole text.</li>
	 * <li>{@link #LINE_HEIGHT}: The distance between two lines in points.</li>
	 * <li>{@link #FONT_AVAILABLE}: Whether the font is installed on the server. A font that is not
	 * installed is replaced by a standard font, so the measurement does not match the text shown
	 * by PowerPoint on a computer having the font.</li>
	 * </ul>
	 *
	 * @param text
	 *        The text to measure.
	 * @param font
	 *        The name of the font family, e.g. <code>Arial</code>.
	 * @param size
	 *        The font size in points.
	 * @param width
	 *        The width in points available for the text. If not given, the text is not wrapped.
	 * @param bold
	 *        Whether the text is set in bold face.
	 * @param italic
	 *        Whether the text is set in italics.
	 * @return The dictionary describing the extent of the text.
	 */
	@Label("Measure text on a slide")
	@SideEffectFree
	public static Map<String, Object> measureText(
			@Mandatory String text,
			@StringDefault(Font.SANS_SERIF) String font,
			@DoubleDefault(18) double size,
			Double width,
			boolean bold,
			boolean italic) {
		if (!TextMeasure.isValidFontSize(size)) {
			throw new TopLogicException(I18NConstants.ERROR_INVALID_FONT_SIZE__SIZE_MIN_MAX.fill(size,
				TextMeasure.MIN_FONT_SIZE, TextMeasure.MAX_FONT_SIZE));
		}
		if (width != null && !(width.doubleValue() > 0)) {
			throw new TopLogicException(I18NConstants.ERROR_INVALID_TEXT_WIDTH__WIDTH.fill(width));
		}

		TextMetrics metrics = TextMeasure.measure(text, font, size, bold, italic, width);

		Map<String, Object> result = new LinkedHashMap<>();
		result.put(WIDTH, Double.valueOf(metrics.width()));
		result.put(LINES, Double.valueOf(metrics.lines()));
		result.put(HEIGHT, Double.valueOf(metrics.height()));
		result.put(LINE_HEIGHT, Double.valueOf(metrics.lineHeight()));
		result.put(FONT_AVAILABLE, Boolean.valueOf(metrics.fontAvailable()));
		return result;
	}

}
