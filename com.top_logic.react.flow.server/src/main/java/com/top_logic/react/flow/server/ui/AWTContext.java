/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.ui;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.TextMetricsImpl;

/**
 * {@link RenderContext} based on AWT {@link Font}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class AWTContext implements RenderContext {

	private final Font _font;

	private final FontRenderContext _fontRenderContext;

	/**
	 * Creates a {@link AWTContext}.
	 */
	public AWTContext(float textSize) {
		_font = Font.decode("Arial").deriveFont(textSize);
		// Anti-aliased + fractional-metrics rendering matches how browsers actually paint SVG
		// text. The default FontRenderContext (no anti-aliasing, no fractional metrics) produces
		// noticeably shorter advance widths than the browser, especially for bold weights, which
		// would let text overflow its measured box.
		_fontRenderContext = new FontRenderContext(null, true, true);
	}

	@Override
	public TextMetricsImpl measure(String text) {
		return measureWithFont(_font, text);
	}

	@Override
	public TextMetricsImpl measure(String text, String fontFamily, double fontSize, String fontWeight) {
		String family = (fontFamily != null && !fontFamily.isEmpty()) ? fontFamily : "Arial";
		float size = fontSize > 0 ? (float) fontSize : _font.getSize2D();
		int style = "bold".equalsIgnoreCase(fontWeight) ? Font.BOLD : Font.PLAIN;
		Font font = new Font(family, style, 1).deriveFont(size);
		return measureWithFont(font, text);
	}

	private TextMetricsImpl measureWithFont(Font font, String text) {
		LineMetrics metrics = font.getLineMetrics(text, _fontRenderContext);
		Rectangle2D bounds = font.getStringBounds(text, _fontRenderContext);

		return new TextMetricsImpl(
			bounds.getWidth(),
			bounds.getHeight(),
			metrics.getAscent());
	}

}
