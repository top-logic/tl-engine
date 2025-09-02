/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.tree;

import java.util.ArrayList;
import java.util.List;

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

		TreeRenderInfo renderInfo = new TreeRenderInfo(self().getNodes(), self().getConnections());

		// Enter tree nodes in columns and assign reasonable Y coordinates to nodes.
		double bottomY = -self().getGapY();
		for (Box root : renderInfo.getRoots()) {
			bottomY = layoutTree(renderInfo, 0, root, bottomY);
		}

		List<Double> columnWidths = new ArrayList<>();

		// Compute column widths.
		double minX = 0;
		for (Column column : renderInfo.getColumns()) {
			double maxWidth = 0;
			for (Box node : column.getBoxes()) {
				maxWidth = Math.max(maxWidth, node.getWidth());
			}

			columnWidths.add(maxWidth);

			minX += maxWidth;
			minX += self().getGapX();
		}

		context.setRenderInfo(self(), renderInfo);

		self().setWidth(minX - (renderInfo.getColumns().isEmpty() ? 0.0 : self().getGapX()));
		self().setHeight(bottomY);
	}

	/**
	 * Helper method to layout a single tree within a forest.
	 * 
	 * @param parentBottomY
	 *        The maximum Y coordinate used among all nodes placed so far (the parent and all of the
	 *        current node's preceding siblings and their sub-trees.
	 */
	default double layoutTree(TreeRenderInfo renderInfo, int level, Box node, double parentBottomY) {
		Column column = renderInfo.mkColumn(level);

		// The minimum Y coordinate, where the current node can be placed in its column.
		double minY;
		if (column.getBoxes().size() > 0) {
			Box last = column.getBoxes().get(column.getBoxes().size() - 1);
			minY = last.getBottomY() + self().getGapY();
		} else {
			minY = 0.0;
		}

		if (!self().isCompact()) {
			minY = Math.max(parentBottomY, minY);
		}

		column.addBox(node);

		double bottomY;

		List<Box> nextLevel = renderInfo.getChildren(node);
		if (nextLevel.isEmpty()) {
			// This is a leaf node.
			if (!self().isCompact()) {
				for (int l = level + 1; l < renderInfo.getColumns().size(); l++) {
					Column down = renderInfo.getColumns().get(l);
					if (down.size() > 0) {
						Box bottom = down.getBox(down.size() - 1);
						minY = Math.max(minY, bottom.getBottomY() + self().getGapY());
					}
				}
			}

			// Place node at the minimum Y coordinate possible.
			node.setY(minY);
			bottomY = minY + node.getHeight();
		} else {
			// Recursively place all children.
			double childBottomY = minY;
			for (Box child : nextLevel) {
				childBottomY = layoutTree(renderInfo, level + 1, child, childBottomY);
			}

			// Place parent node relative to its direct children.
			Box firstChild = nextLevel.get(0);
			Box lastChild = nextLevel.get(nextLevel.size() - 1);

			Box firstAnchor = renderInfo.getAnchor(firstChild);
			Box lastAnchor = renderInfo.getAnchor(lastChild);

			// Note: Anchor boxes are up to this point relative to the node's coordinate.
			double firstY = firstChild.getY() + firstAnchor.getY() + 0.5 * firstAnchor.getHeight();
			double lastY = lastChild.getY() + lastAnchor.getY() + 0.5 * lastAnchor.getHeight();

			// The Y coordinate of the center of the node's anchor box.
			double anchorY = firstY + (lastY - firstY) * self().getParentAlign() + self().getParentOffset();

			Box nodeAnchor = renderInfo.getAnchor(node);

			double nodeY = anchorY - nodeAnchor.getY() - 0.5 * nodeAnchor.getHeight();

			if (nodeY >= minY) {
				// Current node must be moved to the bottom to align with its children.
				minY = nodeY;
			} else {
				// Children must be moved to the bottom to align with the current node.

				// nodeY < minY: Amount to shift the subtree to the bottom.
				double shiftY = minY - nodeY;
				for (Box child : nextLevel) {
					renderInfo.shiftY(child, shiftY);
				}
				childBottomY += shiftY;

				nodeY += shiftY;
			}
			node.setY(nodeY);

			bottomY = Math.max(node.getBottomY(), childBottomY);
		}

		return bottomY;
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
