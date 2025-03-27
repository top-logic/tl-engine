/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * An empty space.
 */
public interface EmptyBlock extends DrawElement {

	@Override
	default void draw(SvgWriter out) {
		// No contents.
	}

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		self().setWidth(0);
		self().setHeight(0);
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);
	}

}
