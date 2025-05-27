/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.Stack;

/**
 * Operations for {@link Stack} elements.
 */
public interface StackOperations extends BoxOperations {

	@Override
	Stack self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		double width = 0;
		double height = 0;
		for (Box content : self().getContents()) {
			content.computeIntrinsicSize(context, offsetX, offsetY);

			width = Math.max(width, content.getWidth());
			height = Math.max(height, content.getHeight());
		}

		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		for (Box content : self().getContents()) {
			content.distributeSize(context, offsetX, offsetY, width, height);
		}

		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void draw(SvgWriter out) {
		for (Box content : self().getContents()) {
			content.draw(out);
		}
	}

}
