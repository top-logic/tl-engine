/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.client.control;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.CanvasElement;
import com.google.gwt.dom.client.Document;

import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.TextMetrics;

import elemental2.dom.CSSStyleDeclaration;
import elemental2.dom.DomGlobal;
import elemental2.dom.Element;
import elemental2.dom.ViewCSS;
import jsinterop.base.Js;

/**
 * {@link RenderContext} based on client-side canvas logic.
 *
 * <p>
 * A text without a font family of its own is measured in the font family of the element the
 * diagram is displayed in, the font of the theme the diagram's text inherits on the screen. The
 * family is read from the computed style of that element by {@link #updateDefaultFont()}, once
 * before each layout.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSRenderContext implements RenderContext {

	/** CSS property the default font family is read from the container's computed style. */
	private static final String FONT_FAMILY_PROPERTY = "font-family";

	private final Context2d _context2d;

	private final Element _container;

	private String _defaultFontFamily = DEFAULT_FONT_DECLARATION;

	/**
	 * Creates a {@link JSRenderContext}.
	 *
	 * @param container
	 *        The element the diagram is displayed in, whose font is used for texts without a font
	 *        family of their own.
	 */
	public JSRenderContext(Element container) {
		_container = container;
		CanvasElement canvas = Document.get().createCanvasElement();
		_context2d = canvas.getContext2d();
	}

	/**
	 * Reads the font family of the container element from its computed style.
	 *
	 * <p>
	 * Must be called before a layout, since the theme font may change between layouts. If the
	 * container has no computed font family (e.g. because it is not displayed), the
	 * {@link #DEFAULT_FONT_DECLARATION} is used.
	 * </p>
	 */
	public void updateDefaultFont() {
		ViewCSS view = Js.uncheckedCast(DomGlobal.window);
		CSSStyleDeclaration style = view.getComputedStyle(_container);
		String family = style == null ? null : style.getPropertyValue(FONT_FAMILY_PROPERTY);
		_defaultFontFamily = (family == null || family.trim().isEmpty()) ? DEFAULT_FONT_DECLARATION : family;
	}

	/**
	 * The font family list of the container the diagram is displayed in.
	 *
	 * @see #updateDefaultFont()
	 */
	@Override
	public String getDefaultFontFamily() {
		return _defaultFontFamily;
	}

	/**
	 * The font family list of the container the diagram is displayed in, as it is stated on the
	 * root of the drawn diagram.
	 *
	 * @see #getDefaultFontFamily()
	 */
	@Override
	public String getDefaultFontDeclaration() {
		return _defaultFontFamily;
	}

	@Override
	public TextMetrics measure(String text) {
		return measure(text, null, 0, null);
	}

	@Override
	public TextMetrics measure(String text, String fontFamily, double fontSize, String fontWeight) {
		String family = (fontFamily != null && !fontFamily.isEmpty()) ? fontFamily : _defaultFontFamily;
		double size = fontSize > 0 ? fontSize : getDefaultFontSizePx();
		String weight = (fontWeight != null && !fontWeight.isEmpty()) ? fontWeight + " " : "";
		_context2d.setFont(weight + (int) size + "px " + family);
		com.google.gwt.canvas.dom.client.TextMetrics metrics = _context2d.measureText(text);
		return new JSTextMetrics(metrics);
	}

}
