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
		self().setRenderInfo(renderInfo);

		renderInfo.computeLayout(context);

		self().setWidth(renderInfo.getWidth());
		self().setHeight(renderInfo.getHeight());
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);

		TreeRenderInfo info = treeInfo();

		// Note: The tree nodes have already their size assigned during the layout computation.
		for (TreeConnection connection : self().getConnections()) {
			TreeConnector parent = connection.getParent();
			Box parentAnchor = parent.getAnchor();
			
			TreeNode parentNode = info.getNodeForAnchor(parentAnchor);

			double fromX = parentNode.getX() + parentNode.getColumn().getWidth();
			double barX = fromX + self().getGapX() / 2;

			connection.setBarPosition(barX);
		}
	}

	@Override
	default void drawContents(SvgWriter out) {
		TreeRenderInfo info = treeInfo();

		for (TreeNode node : info.getNodes()) {
			out.beginGroup();
			out.translate(node.getX(), node.getY());

			out.write(node.getBox());

			out.endGroup();
		}

		for (TreeConnection connection : self().getConnections()) {
			out.write(connection);
		}
	}

	/**
	 * The rendering information that keeps the tree layout.
	 */
	default TreeRenderInfo treeInfo() {
		return (TreeRenderInfo) self().getRenderInfo();
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
