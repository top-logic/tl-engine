/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.tree;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.EdgeDecoration;
import com.top_logic.graphic.flow.operations.WidgetOperations;

/**
 * Custom operations for {@link EdgeDecoration}s.
 */
public interface EdgeDecorationOperations extends WidgetOperations {

	@Override
	EdgeDecoration self();

	/**
	 * Layouts the contents.
	 */
	default void layout(RenderContext context) {
		Box content = self().getContent();
		content.computeIntrinsicSize(context, 0, 0);
		content.distributeSize(context, 0, 0, content.getWidth(), content.getHeight());
	}

	@Override
	default void draw(SvgWriter out) {
		self().getContent().draw(out);
	}
}
