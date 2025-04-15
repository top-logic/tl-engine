/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.flow.data.Box;

/**
 * Inserts some padding around another element.
 */
public interface PaddingOperations extends DecorationOperations {

	@Override
	com.top_logic.graphic.flow.data.Padding self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		self().setX(offsetX);
		self().setY(offsetY);

		double x = offsetX + self().getLeft();
		double y = offsetY + self().getTop();

		Box content = self().getContent();
		content.computeIntrinsicSize(context, x, y);

		self().setWidth(self().getLeft() + content.getWidth() + self().getRight());
		self().setHeight(self().getTop() + content.getHeight() + self().getBottom());
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		self().setX(offsetX);
		self().setY(offsetY);

		self().getContent().distributeSize(context, offsetX + self().getLeft(), offsetY + self().getTop(),
			width - self().getLeft() - self().getRight(),
			height - self().getTop() - self().getBottom());

		self().setWidth(width);
		self().setHeight(height);
	}

}
