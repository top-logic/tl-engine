/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.client.control;

import com.top_logic.react.flow.svg.TextMetrics;

/**
 * {@link TextMetrics} based on client-side canvas measurement.
 *
 * <p>
 * Height and baseline are taken from the line metrics of the font (canvas
 * {@code fontBoundingBoxAscent} and {@code fontBoundingBoxDescent}), not from the outline of the
 * glyphs of the measured text. Texts with the same font therefore have the same height and baseline
 * independent of their letters, matching the AWT {@link java.awt.font.LineMetrics} used for the
 * server-side measurement.
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
		return fontBoundingBoxAscent(_metrics) + fontBoundingBoxDescent(_metrics);
	}

	@Override
	public double getBaseLine() {
		return fontBoundingBoxAscent(_metrics);
	}

	private static final native double fontBoundingBoxAscent(
			com.google.gwt.canvas.dom.client.TextMetrics metrics) /*-{
		return metrics.fontBoundingBoxAscent;
	}-*/;

	private static final native double fontBoundingBoxDescent(
			com.google.gwt.canvas.dom.client.TextMetrics metrics) /*-{
		return metrics.fontBoundingBoxDescent;
	}-*/;

}
