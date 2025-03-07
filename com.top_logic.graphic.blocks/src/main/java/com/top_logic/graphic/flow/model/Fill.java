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

/**
 * Rectangle with a background color.
 */
public class Fill extends Decoration {

	private String _fillStyle;

	/**
	 * The fill style.
	 */
	public String getFillStyle() {
		return _fillStyle;
	}

	/**
	 * @see #getFillStyle()
	 */
	public Fill setFillStyle(String fillStyle) {
		_fillStyle = fillStyle;
		return this;
	}

	@Override
	public void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		setX(offsetX);
		setY(offsetY);
		setWidth(width);
		setHeight(height);
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		// TODO: Automatically created
	}

	@Override
	public void draw(SvgWriter out) {
		out.beginPath();
		out.setStroke("none");
		out.setFill(_fillStyle);
		out.beginData();
		out.moveToAbs(getX(), getY());
		out.lineToHorizontalAbs(getWidth());
		out.lineToVerticalRel(getHeight());
		out.lineToHorizontalAbs(-getWidth());
		out.closePath();
		out.endData();
		out.endPath();

		super.draw(out);
	}

}
