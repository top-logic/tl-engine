/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgWriter;
import com.top_logic.react.flow.data.Box;

/**
 * {@link BoxOperations} that wraps a single other {@link BoxOperations}.
 */
public interface DecorationOperations extends BoxOperations {

	@Override
	com.top_logic.react.flow.data.Decoration self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		Box content = self().getContent();
		content.computeIntrinsicSize(context, offsetX, offsetY);
		self().setWidth(content.getWidth());
		self().setHeight(content.getHeight());
	}

	@Override
	default void draw(SvgWriter out) {
		drawContent(out);
	}

	/**
	 * Writes the decorated content.
	 */
	default void drawContent(SvgWriter out) {
		out.write(self().getContent());
	}

}
