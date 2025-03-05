/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.svg.RenderContext;

/**
 * Inserts some padding around another element.
 */
public class Padding extends Decoration {

	private double _top;

	private double _left;

	private double _right;

	private double _bottom;

	private DrawElement _content;

	@Override
	public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		setX(offsetX);
		setY(offsetY);

		double x = offsetX + _left;
		double y = offsetY + _top;

		_content.computeIntrinsicSize(context, x, y);

		setWidth(_left + _content.getWidth() + _right);
		setHeight(_top + _content.getHeight() + _bottom);
	}

	@Override
	public void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		setX(offsetX);
		setY(offsetY);

		_content.distributeSize(context, offsetX + _left, offsetY + _top, width - _left - _right,
			height - _top - _bottom);

		setWidth(width);
		setHeight(height);
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		// TODO: Automatically created

	}

}
