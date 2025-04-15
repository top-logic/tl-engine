/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model.layout;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.FloatingLayout;
import com.top_logic.graphic.flow.model.BoxOperations;

/**
 * Operations for a {@link FloatingLayout}.
 */
public interface FloatingLayoutOperations extends BoxOperations {

	@Override
	FloatingLayout self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		self().setX(offsetX);
		self().setY(offsetY);

		double width = 0;
		double height = 0;
		for (Box node : self().getNodes()) {
			node.computeIntrinsicSize(context, node.getX(), node.getY());

			width = Math.max(width, node.getX() + node.getWidth());
			height = Math.max(height, node.getY() + node.getHeight());
		}

		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		for (Box node : self().getNodes()) {
			double nodeWidth = Math.min(width, node.getWidth());
			double nodeHeight = Math.min(height, node.getHeight());
			double nodeX = Math.min(width - nodeWidth, node.getX());
			double nodeY = Math.min(height - nodeHeight, node.getY());

			node.distributeSize(context, nodeX, nodeY, nodeWidth, nodeHeight);
		}

		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);
	}

	@Override
	default void draw(SvgWriter out) {
		out.beginGroup();
		out.translate(self().getX(), self().getY());

		for (Box node : self().getNodes()) {
			node.draw(out);
		}

		out.endGroup();
	}

}
