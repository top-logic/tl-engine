/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgWriter;
import com.top_logic.react.flow.svg.TextMetrics;
import com.top_logic.react.flow.data.Text;

/**
 * A single line of text.
 *
 * <p>
 * With {@link Text#isEllipsis()}, a text given less width than it needs is cut and ends with
 * {@link #ELLIPSIS}; the full text is then declared as tooltip of the drawn element.
 * </p>
 */
public interface TextOperations extends BoxOperations {

	/**
	 * The character ending a text that is cut to the width of its box.
	 */
	String ELLIPSIS = "…";

	@Override
	Text self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY,
			double availableWidth, double availableHeight) {
		TextMetrics metrics = measure(context, self().getValue());

		self().setBaseLine(metrics.getBaseLine());

		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(metrics.getWidth());
		self().setHeight(metrics.getHeight());
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		self().setShownValue(self().isEllipsis() ? cut(context, width) : null);

		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);
	}

	/**
	 * The text to draw in the given width, or <code>null</code> if the whole
	 * {@link Text#getValue() value} fits.
	 *
	 * <p>
	 * The result is the longest start of the value that, followed by {@link #ELLIPSIS}, fits the
	 * given width. If not even {@link #ELLIPSIS} fits, the result is empty.
	 * </p>
	 */
	default String cut(RenderContext context, double width) {
		String value = self().getValue();
		if (value == null || measure(context, value).getWidth() <= width) {
			return null;
		}

		// Longest prefix length known to fit and shortest one known not to fit.
		int fits = 0;
		int tooLong = value.length();
		while (tooLong - fits > 1) {
			int length = (fits + tooLong) >>> 1;
			if (measure(context, cutTo(value, length)).getWidth() <= width) {
				fits = length;
			} else {
				tooLong = length;
			}
		}
		String result = cutTo(value, fits);
		if (fits == 0 && measure(context, result).getWidth() > width) {
			return "";
		}
		return result;
	}

	/**
	 * The given text cut to the given length, followed by {@link #ELLIPSIS}.
	 */
	static String cutTo(String value, int length) {
		int end = length;
		if (end > 0 && Character.isHighSurrogate(value.charAt(end - 1))) {
			// Do not separate the halves of a character outside the basic multilingual plane.
			end--;
		}
		while (end > 0 && Character.isWhitespace(value.charAt(end - 1))) {
			end--;
		}
		return value.substring(0, end) + ELLIPSIS;
	}

	/**
	 * Measures the given text in the font of this text.
	 */
	default TextMetrics measure(RenderContext context, String text) {
		return context.measure(text, self().getFontFamily(), self().getFontSize(), self().getFontWeight());
	}

	@Override
	default void draw(SvgWriter out) {
		String shown = self().getShownValue();
		out.beginText(self().getX(), self().getY() + self().getBaseLine(),
			shown == null ? self().getValue() : shown);
		out.writeCssClass(self().getCssClass());
		out.setStroke(self().getStrokeStyle());
		out.setFill(self().getFillStyle());
		out.setTextStyle(self().getFontFamily(), self().getFontSize(), self().getFontWeight());
		if (shown != null) {
			TooltipOperations.writeTextTooltip(out, self().getValue());
		}
		out.endText();
	}

}
