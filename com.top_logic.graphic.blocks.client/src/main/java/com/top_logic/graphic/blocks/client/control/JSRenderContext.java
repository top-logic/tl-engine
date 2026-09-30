/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.client.control;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.TextMetrics;

/**
 * {@link RenderContext} based on client-side canvas logic.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSRenderContext implements RenderContext {

	private final Context2d _context2d;

	private final String _defaultFontSize;

	/**
	 * Creates a {@link JSRenderContext} using the {@link RenderContext#DEFAULT_FONT_SIZE_PX
	 * standard default font size}.
	 */
	public JSRenderContext() {
		this(formatPx(DEFAULT_FONT_SIZE_PX));
	}

	private static String formatPx(double value) {
		int intValue = (int) value;
		if (value == intValue) {
			return intValue + "px";
		}
		return value + "px";
	}

	/**
	 * Creates a {@link JSRenderContext} with a custom default {@code font-size}.
	 *
	 * @param defaultFontSize
	 *        Default {@code font-size} (e.g. {@code "16px"}) applied to texts that do not specify
	 *        their own size.
	 */
	public JSRenderContext(String defaultFontSize) {
		_defaultFontSize = defaultFontSize;
		CanvasElement canvas = Document.get().createCanvasElement();
		_context2d = canvas.getContext2d();
	}

	@Override
	public TextMetrics measure(String text, String fontFamily, String fontSize, String fontWeight) {
		// Resolve nulls to the context defaults. The canvas font is reset per call so that the
		// measurement matches the font that will actually be rendered on the corresponding
		// SVG <text> element.
		String family = fontFamily != null ? fontFamily : DEFAULT_FONT_FAMILY;
		String size = fontSize != null ? fontSize : _defaultFontSize;
		String weight = fontWeight != null ? fontWeight : "normal";

		_context2d.setFont(weight + " " + size + " " + family);

		com.google.gwt.canvas.dom.client.TextMetrics metrics = _context2d.measureText(text);
		return new JSTextMetrics(metrics);
	}

}
