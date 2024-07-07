/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.svg;

/**
 * {@link TextMetrics} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TextMetricsImpl implements TextMetrics {

	private final double _width;

	private final double _height;

	private final double _baseLine;

	/**
	 * Creates a {@link TextMetricsImpl}.
	 */
	public TextMetricsImpl(double width, double height, double baseLine) {
		_width = width;
		_height = height;
		_baseLine = baseLine;
	}

	@Override
	public double getWidth() {
		return _width;
	}

	@Override
	public double getHeight() {
		return _height;
	}

	@Override
	public double getBaseLine() {
		return _baseLine;
	}
}