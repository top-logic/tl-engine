/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.blocks.svg.TextMetrics;

/**
 * A single line of text.
 */
public class TextLine extends AbstractDrawElement {

	private String _value;

	private TextMetrics _metrics;

	/**
	 * Creates a {@link TextLine}.
	 */
	public TextLine(String value) {
		_value = value;
	}

	/**
	 * The displayed text.
	 */
	public String getValue() {
		return _value;
	}

	/**
	 * @see #getValue()
	 */
	public void setValue(String value) {
		_value = value;
	}

	@Override
	public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		_metrics = context.measure(_value);
		
		setX(offsetX);
		setY(offsetY);
		setWidth(_metrics.getWidth());
		setHeight(_metrics.getHeight());
	}

	@Override
	public void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		setX(offsetX);
		setY(offsetY);
		setWidth(width);
		setHeight(height);
	}

	@Override
	public void draw(SvgWriter out) {
		out.text(getX(), getY() + _metrics.getBaseLine(), _value);
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		json.name("value");
		json.value(_value);
	}

}
