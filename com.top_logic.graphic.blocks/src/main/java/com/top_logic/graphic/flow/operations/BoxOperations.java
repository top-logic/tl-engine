/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.Widget;

/**
 * Custom operations defined for a {@link Box}.
 */
public interface BoxOperations extends WidgetOperations {

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

	/**
	 * The top-level {@link Diagram}, this box is part of.
	 */
	default Diagram getDiagram() {
		Widget ancestor = self().getParent();
		while (ancestor instanceof Box) {
			Box box = (Box) ancestor;
			ancestor = box.getParent();
		}
		return (Diagram) ancestor;
	}

	/**
	 * Computes the preferred size of this element and all of its children.
	 * 
	 * <p>
	 * After this method completes, the properties {@link Box#getWidth()} and
	 * {@link Box#getHeight()} must be set to the preferred size of this {@link Box}.
	 * </p>
	 */
	void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY);

	/**
	 * Places this diagram element and its children at the given coordinates and adjust its
	 * dimensions to the given maximum width and height.
	 */
	void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height);

}
