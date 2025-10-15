/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.layout;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.SpaceDistribution;
import com.top_logic.graphic.flow.operations.BoxOperations;

/**
 * A row of elements.
 */
public interface HorizontalLayoutOperations extends BoxOperations {

	@Override
	com.top_logic.graphic.flow.data.HorizontalLayout self();

	@Override
	default void draw(SvgWriter out) {
		for (BoxOperations e : self().getContents()) {
			out.write(e);
		}
	}

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		double width = 0;
		double maxHeight = 0;

		self().setX(offsetX);
		self().setY(offsetY);

		double x = offsetX;
		double y = offsetY;
		double gap = self().getGap();

		for (int n = 0; n < self().getContents().size(); n++) {
			Box col = self().getContents().get(n);

			if (n > 0) {
				width += gap;
			}
			x = offsetX + width;

			col.computeIntrinsicSize(context, x, y);

			width += col.getWidth();
			maxHeight = Math.max(maxHeight, col.getHeight());
		}

		self().setWidth(width);
		self().setHeight(maxHeight);
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		self().setX(offsetX);
		self().setY(offsetY);

		double additionalSpace = width - self().getWidth();
		int cnt = self().getContents().size();
		double additionalWidth =
			self().getFill() == SpaceDistribution.STRETCH_CONTENT && cnt > 0 ? additionalSpace / cnt : 0;
		double gap = self().getGap()
			+ (self().getFill() == SpaceDistribution.STRETCH_GAP && cnt > 1 ? additionalSpace / (cnt - 1)
				: 0);
		
		double elementX = offsetX;

		for (int n = 0; n < cnt; n++) {
			Box col = self().getContents().get(n);

			double elementWidth = col.getWidth() + additionalWidth;

			col.distributeSize(context, elementX, offsetY, elementWidth, height);

			elementX += elementWidth;
			elementX += gap;
		}

		self().setWidth(width);
		self().setHeight(height);
	}

}