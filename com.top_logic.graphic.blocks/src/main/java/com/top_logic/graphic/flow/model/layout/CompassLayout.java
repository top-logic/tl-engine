/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model.layout;

import java.io.IOException;

import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.model.AbstractDrawElement;
import com.top_logic.graphic.flow.model.DrawElement;
import com.top_logic.graphic.flow.model.EmptyBlock;

/**
 * 
 * <pre>
 * +----------------------+
 * |        North         | vPadding
 * +----------------------+
 * |      |        |      |
 * | West | Center | East |
 * |      |        |      |
 * +----------------------+
 * |        South         | vPadding
 * +----------------------+
 * hpadding        hpadding
 * </pre>
 * 
 */
public class CompassLayout extends AbstractDrawElement {

	private DrawElement _north = new EmptyBlock();

	private DrawElement _south = new EmptyBlock();

	private DrawElement _west = new EmptyBlock();

	private DrawElement _east = new EmptyBlock();

	private DrawElement _center = new EmptyBlock();

	private double _hPadding;

	private double _vPadding;

	private double _centerHeight;

	@Override
	public void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		_north.computeIntrinsicSize(context, offsetX, offsetY);
		_south.computeIntrinsicSize(context, offsetX, offsetY);
		_west.computeIntrinsicSize(context, offsetX, offsetY);
		_east.computeIntrinsicSize(context, offsetX, offsetY);
		_center.computeIntrinsicSize(context, offsetX, offsetY);

		_hPadding = Math.max(_west.getWidth(), _east.getWidth());
		_vPadding = Math.max(_north.getHeight(), _south.getHeight());
		_centerHeight = Math.max(Math.max(_west.getHeight(), _east.getHeight()), _center.getHeight());
		double height = 2 * _vPadding + _centerHeight;
		double width = Math.max(Math.max(_north.getWidth(), _south.getWidth()), 2 * _hPadding + _center.getWidth());

		setWidth(width);
		setHeight(height);
	}

	@Override
	public void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		setX(offsetX);
		setY(offsetY);

		double centerWidth = width - 2 * _hPadding;
		_centerHeight = height - 2 * _vPadding;

		double centerX = offsetX + _hPadding;
		double centerY = offsetY + _vPadding;

		_north.distributeSize(context, offsetX, offsetY, width, _vPadding);
		_west.distributeSize(context, offsetX, centerY, _hPadding, _centerHeight);
		_center.distributeSize(context, centerX, centerY, centerWidth, _centerHeight);
		_east.distributeSize(context, centerX + centerWidth, centerY, _hPadding, _centerHeight);
		_south.distributeSize(context, offsetX, centerY + _centerHeight, width, _vPadding);

		setWidth(width);
		setHeight(height);
	}

	@Override
	public void draw(SvgWriter out) {
		_north.draw(out);
		_west.draw(out);
		_east.draw(out);
		_south.draw(out);

		_center.draw(out);
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		// No properties.
	}

}
