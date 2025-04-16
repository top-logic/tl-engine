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
 * A column of elements.
 */
public interface VerticalLayoutOperations extends BoxOperations {

	@Override
	com.top_logic.graphic.flow.data.VerticalLayout self();

	@Override
	default void draw(SvgWriter out) {
		for (BoxOperations e : self().getContents()) {
			e.draw(out);
		}
	}

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		double height = 0;
		double maxWidth = 0;

		self().setX(offsetX);
		self().setY(offsetY);

		double x = offsetX;
		double y = offsetY;

		for (int n = 0; n < self().getContents().size(); n++) {
			Box row = self().getContents().get(n);

			if (n > 0) {
				height += self().getGap();
			}
			y = offsetY + height;

			row.computeIntrinsicSize(context, x, y);

			height += row.getHeight();
			maxWidth = Math.max(maxWidth, row.getWidth());
		}

		self().setWidth(maxWidth);
		self().setHeight(height);
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		self().setX(offsetX);
		self().setY(offsetY);

		int cnt = self().getContents().size();
		double additionalSpace = height - self().getHeight();
		double additionalHeight =
			self().getFill() == SpaceDistribution.STRETCH_CONTENT && cnt > 0
				? additionalSpace / cnt
				: 0;
		double gap =
			self().getGap()
				+ (self().getFill() == SpaceDistribution.STRETCH_GAP && cnt > 1
					? additionalSpace / (cnt - 1)
					: 0);

		double elementY = offsetY;

		for (int n = 0; n < cnt; n++) {
			Box row = self().getContents().get(n);

			double elementHeight = row.getHeight() + additionalHeight;

			row.distributeSize(context, offsetX, elementY, width, elementHeight);

			elementY += elementHeight;
			elementY += gap;
		}

		self().setWidth(width);
		self().setHeight(height);
	}

}
