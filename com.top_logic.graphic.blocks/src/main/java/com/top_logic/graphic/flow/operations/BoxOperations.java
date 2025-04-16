/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import com.top_logic.graphic.blocks.model.Drawable;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.flow.data.Box;

/**
 * 
 */
public interface BoxOperations extends Drawable, MapLike {

	/**
	 * The {@link Box} data.
	 */
	@Override
	Box self();

	/**
	 * The X coordinate of the right border of this box.
	 * 
	 * @see Box#getX()
	 */
	default double getRightX() {
		return self().getX() + self().getWidth();
	}

	/**
	 * The Y coordinate of the bottom border of this box.
	 * 
	 * @see Box#getY()
	 */
	default double getBottomY() {
		return self().getY() + self().getHeight();
	}

	void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY);

	void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height);

}
