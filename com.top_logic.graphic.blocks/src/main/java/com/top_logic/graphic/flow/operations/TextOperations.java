/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.blocks.svg.TextMetrics;
import com.top_logic.graphic.flow.data.Text;

/**
 * A single line of text.
 */
public interface TextOperations extends BoxOperations {

	@Override
	Text self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		TextMetrics metrics = context.measure(self().getValue());
		
		self().setBaseLine(metrics.getBaseLine());

		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(metrics.getWidth());
		self().setHeight(metrics.getHeight());
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void draw(SvgWriter out) {
		out.beginText(self().getX(), self().getY() + self().getBaseLine(), self().getValue());
		out.writeCssClass(self().getCssClass());
		out.setStroke(self().getStrokeStyle());
		out.setFill(self().getFillStyle());
		out.setTextStyle(self().getFontFamily(), self().getFontSize(), self().getFontWeight());
		out.endText();
	}

}
