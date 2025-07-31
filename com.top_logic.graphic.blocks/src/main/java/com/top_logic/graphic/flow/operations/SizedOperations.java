/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.Padding;
import com.top_logic.graphic.flow.data.Sized;

/**
 * Custom operations for the {@link Padding} diagram element.
 */
public interface SizedOperations extends DecorationOperations {

	@Override
	Sized self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		self().setX(offsetX);
		self().setY(offsetY);

		Box content = self().getContent();
		content.computeIntrinsicSize(context, offsetX, offsetY);

		double width = content.getWidth();
		if (self().getMinWidth() != null) {
			width = Math.max(self().getMinWidth(), width);
		}
		if (self().getMaxWidth() != null) {
			width = Math.min(self().getMaxWidth(), width);
		}
		self().setWidth(width);

		double height = content.getHeight();
		if (self().getMinHeight() != null) {
			height = Math.max(self().getMinHeight(), height);
		}
		if (self().getMaxHeight() != null) {
			height = Math.min(self().getMaxHeight(), height);
		}
		self().setHeight(height);
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		self().setX(offsetX);
		self().setY(offsetY);

		self().getContent().distributeSize(context, offsetX, offsetY, width, height);

		self().setWidth(width);
		self().setHeight(height);
	}

}
