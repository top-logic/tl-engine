/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * Rectangle with a background color.
 */
public interface Fill extends Decoration {

	@Override
	com.top_logic.graphic.flow.data.Fill self();

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		self().getContent().distributeSize(context, offsetX, offsetY, width, height);
		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void draw(SvgWriter out) {
		out.beginPath();
		out.setStroke("none");
		out.setFill(self().getFillStyle());
		out.beginData();
		out.moveToAbs(self().getX(), self().getY());
		out.lineToHorizontalAbs(self().getX() + self().getWidth());
		out.lineToVerticalAbs(self().getY() + self().getHeight());
		out.lineToHorizontalAbs(self().getX());
		out.closePath();
		out.endData();
		out.endPath();

		Decoration.super.draw(out);
	}

}
