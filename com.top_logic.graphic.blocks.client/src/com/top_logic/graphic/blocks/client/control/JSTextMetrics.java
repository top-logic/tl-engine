/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.client.control;

import com.top_logic.graphic.blocks.svg.TextMetrics;

/**
 * {@link TextMetrics} based on client-side canvas measurement.
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
		return actualBoundingBoxAscent(_metrics) + actualBoundingBoxDescent(_metrics);
	}

	@Override
	public double getBaseLine() {
		return actualBoundingBoxAscent(_metrics);
	}

	private static final native double actualBoundingBoxAscent(
			com.google.gwt.canvas.dom.client.TextMetrics metrics) /*-{
		return metrics.actualBoundingBoxAscent;
	}-*/;

	private static final native double actualBoundingBoxDescent(
			com.google.gwt.canvas.dom.client.TextMetrics metrics) /*-{
		return metrics.actualBoundingBoxDescent;
	}-*/;

}
