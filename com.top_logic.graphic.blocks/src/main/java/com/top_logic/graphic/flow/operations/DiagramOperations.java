/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import com.top_logic.graphic.blocks.model.Drawable;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Diagram;

/**
 * 
 */
public interface DiagramOperations extends Drawable {

	Diagram self();

	default void layout(RenderContext context) {
		self().getRoot().computeIntrinsicSize(context, 0, 0);
		self().getRoot().distributeSize(context, 0, 0, self().getRoot().getWidth(), self().getRoot().getHeight());
	}

	@Override
	default void draw(SvgWriter out) {
		out.beginSvg();
		out.dimensions(Double.toString(self().getRoot().getWidth()), Double.toString(self().getRoot().getHeight()), 0,
			0, self().getRoot().getWidth(),
			self().getRoot().getHeight());
		self().getRoot().draw(out);
		out.endSvg();
	}

}
