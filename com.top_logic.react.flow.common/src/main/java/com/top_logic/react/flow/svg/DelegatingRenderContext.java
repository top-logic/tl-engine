/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.svg;

/**
 * {@link RenderContext} that measures text through a base context and reports its own
 * {@link #getZoom() zoom} and {@link #isWidestVariants() measuring mode}.
 *
 * @see RenderContext#withZoom(double)
 * @see RenderContext#withWidestVariants()
 */
public class DelegatingRenderContext implements RenderContext {

	private final RenderContext _base;

	private final double _zoom;

	private final boolean _widestVariants;

	/**
	 * Creates a {@link DelegatingRenderContext}.
	 *
	 * @param base
	 *        The context to measure text with.
	 * @param zoom
	 *        See {@link #getZoom()}.
	 * @param widestVariants
	 *        See {@link #isWidestVariants()}.
	 */
	public DelegatingRenderContext(RenderContext base, double zoom, boolean widestVariants) {
		_base = base;
		_zoom = zoom;
		_widestVariants = widestVariants;
	}

	@Override
	public String getDefaultFontFamily() {
		return _base.getDefaultFontFamily();
	}

	@Override
	public String getDefaultFontDeclaration() {
		return _base.getDefaultFontDeclaration();
	}

	@Override
	public double getDefaultFontSizePx() {
		return _base.getDefaultFontSizePx();
	}

	@Override
	public TextMetrics measure(String text) {
		return _base.measure(text);
	}

	@Override
	public TextMetrics measure(String text, String fontFamily, double fontSize) {
		return _base.measure(text, fontFamily, fontSize);
	}

	@Override
	public TextMetrics measure(String text, String fontFamily, double fontSize, String fontWeight) {
		return _base.measure(text, fontFamily, fontSize, fontWeight);
	}

	@Override
	public double getZoom() {
		return _zoom;
	}

	@Override
	public boolean isWidestVariants() {
		return _widestVariants;
	}

	@Override
	public RenderContext withZoom(double zoom) {
		return new DelegatingRenderContext(_base, zoom, _widestVariants);
	}

	@Override
	public RenderContext withWidestVariants() {
		return new DelegatingRenderContext(_base, _zoom, true);
	}

}
