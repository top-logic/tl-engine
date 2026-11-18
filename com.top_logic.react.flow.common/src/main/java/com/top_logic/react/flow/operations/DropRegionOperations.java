/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgWriter;
import com.top_logic.react.flow.data.DropRegion;

/**
 * Operations for {@link DropRegion} diagram elements.
 *
 * <p>
 * A drop region marks the box a drop on a diagram is made onto: the {@code <drop>} of a
 * {@code <flow-diagram>} takes the user object of the region as its target. The region draws its
 * content only; the drop itself is handled by the view layer's drag and drop.
 * </p>
 */
public interface DropRegionOperations extends DecorationOperations {

	@Override
	DropRegion self();

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
		out.beginGroup();
		out.attachModel(self());
		out.writeCssClass(self().getCssClass());
		{
			DecorationOperations.super.draw(out);
		}
		out.endGroup();
	}

}
