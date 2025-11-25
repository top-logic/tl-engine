/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.ContextMenu;

import de.haumacher.msgbuf.graph.AbstractSharedGraphNode;

/**
 * Custom operations for {@link ContextMenu}.
 */
public interface ContextMenuOperations extends DecorationOperations {

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		// No visible content.
		self().getContent().distributeSize(context, offsetX, offsetY, width, height);
		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void draw(SvgWriter out) {
		out.beginGroup(self());

		int id = ((AbstractSharedGraphNode) self()).id();
		if (id != 0) {
			// See HTMLConstants#TL_CONTEXT_MENU_ATTR, which is not available here.
			out.writeAttribute("data-context-menu", Integer.toString(id));
		}

		drawContent(out);

		out.endGroup();
	}
}
