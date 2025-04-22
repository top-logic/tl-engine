/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.layout;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.operations.BoxOperations;

/**
 * 
 * <pre>
 * +----------------------+
 * |        North         | vPadding
 * +----------------------+
 * |      |        |      |
 * | West | Center | East |
 * |      |        |      |
 * +----------------------+
 * |        South         | vPadding
 * +----------------------+
 * hpadding        hpadding
 * </pre>
 */
public interface CompassLayoutOperations extends BoxOperations {

	@Override
	com.top_logic.graphic.flow.data.CompassLayout self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		self().getNorth().computeIntrinsicSize(context, offsetX, offsetY);
		self().getSouth().computeIntrinsicSize(context, offsetX, offsetY);
		self().getWest().computeIntrinsicSize(context, offsetX, offsetY);
		self().getEast().computeIntrinsicSize(context, offsetX, offsetY);
		self().getCenter().computeIntrinsicSize(context, offsetX, offsetY);

		self().setHPadding(Math.max(self().getWest().getWidth(), self().getEast().getWidth()));
		self().setVPadding(Math.max(self().getNorth().getHeight(), self().getSouth().getHeight()));
		self().setCenterHeight(Math.max(Math.max(self().getWest().getHeight(), self().getEast().getHeight()),
			self().getCenter().getHeight()));
		double height = 2 * self().getVPadding() + self().getCenterHeight();
		double width = Math.max(Math.max(self().getNorth().getWidth(), self().getSouth().getWidth()),
			2 * self().getHPadding() + self().getCenter().getWidth());

		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		self().setX(offsetX);
		self().setY(offsetY);

		double centerWidth = width - 2 * self().getHPadding();
		self().setCenterHeight(height - 2 * self().getVPadding());

		double centerX = offsetX + self().getHPadding();
		double centerY = offsetY + self().getVPadding();

		self().getNorth().distributeSize(context, offsetX, offsetY, width, self().getVPadding());
		self().getWest().distributeSize(context, offsetX, centerY, self().getHPadding(), self().getCenterHeight());
		self().getCenter().distributeSize(context, centerX, centerY, centerWidth, self().getCenterHeight());
		self().getEast().distributeSize(context, centerX + centerWidth, centerY, self().getHPadding(),
			self().getCenterHeight());
		self().getSouth().distributeSize(context, offsetX, centerY + self().getCenterHeight(), width,
			self().getVPadding());

		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void draw(SvgWriter out) {
		out.write(self().getNorth());
		out.write(self().getWest());
		out.write(self().getEast());
		out.write(self().getSouth());

		out.write(self().getCenter());
	}

}
