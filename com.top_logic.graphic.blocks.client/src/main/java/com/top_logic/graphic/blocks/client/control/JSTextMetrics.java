/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.client.control;

import com.top_logic.graphic.blocks.svg.TextMetrics;

/**
 * {@link TextMetrics} based on client-side canvas measurement.
 *
 * <p>
 * Height and baseline are derived from the canvas
 * {@code fontBoundingBoxAscent / fontBoundingBoxDescent}, matching the semantics of the AWT
 * {@code LineMetrics} used on the server. If a browser does not yet provide those properties (very
 * old engines), the smaller actual glyph bounding box is used as a fallback.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSTextMetrics implements TextMetrics {

	private final com.google.gwt.canvas.dom.client.TextMetrics _metrics;

	/**
	 * Creates a {@link JSTextMetrics}.
	 */
	public JSTextMetrics(com.google.gwt.canvas.dom.client.TextMetrics metrics) {
		_metrics = metrics;
	}

	@Override
	public double getWidth() {
		return _metrics.getWidth();
	}

	@Override
	public double getHeight() {
		return ascent(_metrics) + descent(_metrics);
	}

	@Override
	public double getBaseLine() {
		return ascent(_metrics);
	}

	private static final native double ascent(
			com.google.gwt.canvas.dom.client.TextMetrics metrics) /*-{
		var a = metrics.fontBoundingBoxAscent;
		if (a == null) {
			a = metrics.actualBoundingBoxAscent;
		}
		return a;
	}-*/;

	private static final native double descent(
			com.google.gwt.canvas.dom.client.TextMetrics metrics) /*-{
		var d = metrics.fontBoundingBoxDescent;
		if (d == null) {
			d = metrics.actualBoundingBoxDescent;
		}
		return d;
	}-*/;

}
