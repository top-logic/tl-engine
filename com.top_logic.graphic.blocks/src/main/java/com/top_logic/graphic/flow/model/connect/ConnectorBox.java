/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model.connect;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.model.AbstractDrawElement;
import com.top_logic.graphic.flow.model.DrawElement;
import com.top_logic.graphic.flow.model.EmptyBlock;

/**
 * 
 */
public class ConnectorBox extends AbstractDrawElement {

	private DrawElement _content = new EmptyBlock();

	private Socket _top = new Socket();

	private Socket _left = new Socket();

	private Socket _right = new Socket();

	private Socket _bottom = new Socket();

	private Socket _center = new Socket();

	@Override
	public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		_content.computeIntrinsicSize(context, offsetX, offsetY);
		setWidth(_content.getWidth());
		setHeight(_content.getHeight());
	}

	@Override
	public void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		_content.distributeSize(context, offsetX, offsetY, width, height);

		setX(offsetX);
		setY(offsetY);
		setWidth(width);
		setHeight(height);

		double centerX = offsetX + width / 2;
		double centerY = offsetY + height / 2;
		_center.setPos(centerX, centerY);
		_top.setPos(centerX, offsetY);
		_bottom.setPos(centerX, offsetY + height);
		_left.setPos(offsetX, centerY);
		_right.setPos(offsetX + width, centerY);
	}

	@Override
	public void draw(SvgWriter out) {
		_content.draw(out);
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		// No properties.
	}

}
