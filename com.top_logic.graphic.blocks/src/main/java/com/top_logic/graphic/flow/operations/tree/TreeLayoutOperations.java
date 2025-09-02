/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.tree;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.DiagramDirection;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.data.TreeLayout;
import com.top_logic.graphic.flow.operations.layout.FloatingLayoutOperations;
import com.top_logic.graphic.flow.operations.tree.TreeRenderInfo.Column;

/**
 * Operations for a {@link TreeLayout}.
 * 
 * <p>
 * A tree layout in {@link DiagramDirection#LTR} direction looks like in the following picture:
 * </p>
 * 
 * <pre>
 * <code>
 *            bar   +-----+
 *             +--->| C1  |
 *             |    +-----+
 *  +-----+    |
 *  | P   | ---+        +-----+
 *  +-----+    +------->| C2  |
 *             |        +-----+
 *             |        
 *             | 
 *             |  +-----+      
 *             +->| C3  |      
 *                +-----+
 * </code>
 * </pre>
 */
public interface TreeLayoutOperations extends FloatingLayoutOperations {

	@Override
	TreeLayout self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		FloatingLayoutOperations.super.computeIntrinsicSize(context, offsetX, offsetY);

		TreeRenderInfo renderInfo =
			new TreeRenderInfo(
				self().isCompact(), self().getGapX(), self().getGapY(), self().getParentAlign(),
				self().getParentOffset(),
				self().getNodes(), self().getConnections());

		renderInfo.computeLayout();

		context.setRenderInfo(self(), renderInfo);

		self().setWidth(renderInfo.getWidth());
		self().setHeight(renderInfo.getHeight());
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		double dy = offsetY - self().getY();

		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);

		TreeRenderInfo info = context.getRenderInfo(self());

		// Offer available space to nodes.
		double x = offsetX;
		for (Column column : info.getColumns()) {
			double columnWidth = column.getWidth();

			for (Box node : column.getBoxes()) {
				node.distributeSize(context, x, node.getY() + dy, columnWidth, node.getHeight());
			}

			x += columnWidth;
			x += self().getGapX();
		}

		for (TreeConnection connection : self().getConnections()) {
			TreeConnector parent = connection.getParent();
			double fromX = fromX(parent.getAnchor());

			double toX = Double.MAX_VALUE;
			for (TreeConnector child : connection.getChildren()) {
				if (child == null) {
					// This is an invalid situation.
					continue;
				}

				Box anchor = child.getAnchor();

				// This should normally not happen and represents a hard to debug error in the
				// diagram model.
				if (anchor != null) {
					toX = Math.min(toX, toX(anchor));
				}
			}

			double barX = self().isCompact() ? toX - self().getGapX() / 2 : (fromX + toX) / 2;

			connection.setBarPosition(barX);
		}
	}

	@Override
	default void drawContents(SvgWriter out) {
		FloatingLayoutOperations.super.drawContents(out);

		for (TreeConnection connection : self().getConnections()) {
			out.write(connection);
		}
	}

	/** The X coordinate to place a parent connector. */
	default double fromX(Box box) {
		// TODO: Depends on self().getDirection() - formulated for LTR:
		return box.getRightX();
	}

	/** The X coordinate to place a child connector. */
	default double toX(Box box) {
		// TODO: Depends on self().getDirection() - formulated for LTR:
		return box.getX();
	}
}
