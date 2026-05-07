/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.tree;

import java.util.List;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.DiagramDirection;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeLayout;
import com.top_logic.graphic.flow.operations.layout.FloatingLayoutOperations;
import com.top_logic.graphic.flow.operations.tree.TreeRenderInfo.GridInfo;

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

		for (TreeConnection connection : self().getConnections()) {
			connection.layout(context);
		}

		TreeRenderInfo renderInfo =
			new TreeRenderInfo(
				self().isCompact(), self().getGapX(), self().getSibblingGapY(), self().getSubtreeGapY(),
				self().getParentAlign(), self().getParentOffset(), self().getChildSplitThreshold(),
				self().isRowWise(), self().getBridgeGapY(),
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
		// Bar position resolution (priority order):
		// 1. If the parent has a sub-grid GridInfo, use the bar X for the specific child (per
		// sub-column for column-wise, single shared main bus for row-wise).
		// 2. Else if the parent has a bus-X override (set when it is a sub-grid child of a row-wise
		// parent and has its own subtree → its bus must be routed at the surrounding parent's
		// childBusX, not in the middle of its own sub-grid slot).
		// 3. Else the natural linear bar sits gapX/2 to the right of the parent's right edge —
		// the entry stub may be longer than gapX/2 to make room for edge decorations, but the
		// parent-side gap stays constant.
		for (TreeConnection connection : self().getConnections()) {
			TreeNode parentNode = info.getNodeForAnchor(connection.getParent().getAnchor());
			TreeNode childNode = info.getNodeForAnchor(connection.getChild().getAnchor());

			GridInfo gi = info.getGridInfo(parentNode);
			double barX;
			if (gi != null) {
				barX = gi.getBarPositionFor(childNode);
			} else if (parentNode.hasBusXOverride()) {
				barX = parentNode.getBusXOverride();
			} else {
				barX = parentNode.getX() + parentNode.getBox().getWidth() + self().getGapX() / 2;
			}

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

		// Draw central bus structure for grid-mode parents (primary bus, bottom bridge, follow-up
		// column buses). Per-connection drawing then only adds the horizontal stub from the
		// relevant sub-column bus into the child's left edge.
		for (TreeNode parent : info.getNodes()) {
			GridInfo gi = info.getGridInfo(parent);
			if (gi != null) {
				drawGridBuses(out, parent, gi);
			}
		}

		for (TreeConnection connection : self().getConnections()) {
			out.write(connection);
		}
	}

	/**
	 * Dispatches to the kind-specific bus rendering for a sub-grid parent.
	 */
	default void drawGridBuses(SvgWriter out, TreeNode parent, GridInfo gi) {
		switch (gi.getKind()) {
			case COLUMN_WISE:
				drawGridBusesColumnWise(out, parent, gi);
				break;
			case ROW_WISE:
				drawGridBusesRowWise(out, parent, gi);
				break;
		}
	}

	/**
	 * Draws the central bus structure for a column-wise sub-grid parent: the parent → primary
	 * bus segment, the primary bus down to the bottom bridge, the bottom bridge across all
	 * sub-column buses, and each follow-up sub-column's vertical bus.
	 */
	default void drawGridBusesColumnWise(SvgWriter out, TreeNode parent, GridInfo gi) {
		Box parentAnchor = parent.getAnchor();
		double parentRight = parent.getX() + parent.getBox().getWidth();
		double parentMidY = parent.getY() + parentAnchor.getY() + 0.5 * parentAnchor.getHeight();

		double[] busX = gi.getBusX();
		double bridgeY = gi.getBridgeY();
		int colCount = gi.getColCount();
		List<List<TreeNode>> cols = gi.getCols();

		// Primary column: parent → primary bus, primary bus from top connection (or parentMidY)
		// down to the bottom bridge.
		List<TreeNode> primary = cols.get(0);
		double primaryFirstMidY = anchorMidY(primary.get(0));
		double primaryTopY = Math.min(parentMidY, primaryFirstMidY);

		out.beginPath();
		out.setStrokeWidth(self().getThickness());
		out.setStroke(self().getStrokeStyle());
		out.setFill("none");
		out.beginData();
		// Parent → primary bus.
		out.moveToAbs(parentRight, parentMidY);
		out.lineToHorizontalAbs(busX[0]);
		// Primary bus vertical down to bridge.
		out.moveToAbs(busX[0], primaryTopY);
		out.lineToVerticalAbs(bridgeY);
		// Bottom bridge.
		if (colCount > 1) {
			out.moveToAbs(busX[0], bridgeY);
			out.lineToHorizontalAbs(busX[colCount - 1]);
		}
		// Follow-up column buses (from each column's first child connection up... drawn from
		// bridge upwards via a single vertical segment).
		for (int c = 1; c < colCount; c++) {
			double firstMidY = anchorMidY(cols.get(c).get(0));
			out.moveToAbs(busX[c], firstMidY);
			out.lineToVerticalAbs(bridgeY);
		}
		out.endData();
		out.endPath();
	}

	/**
	 * Draws the central bus structure for a row-wise sub-grid parent: parent → main bus segment,
	 * a single vertical main bus that spans all direct children's anchor mid-Ys (plus the parent
	 * mid-Y to ensure the parent stem touches the bus). The per-connection drawing
	 * ({@code TreeConnection}) adds the entry stubs from the bus into each child.
	 */
	default void drawGridBusesRowWise(SvgWriter out, TreeNode parent, GridInfo gi) {
		Box parentAnchor = parent.getAnchor();
		double parentRight = parent.getX() + parent.getBox().getWidth();
		double parentMidY = parent.getY() + parentAnchor.getY() + 0.5 * parentAnchor.getHeight();

		double mainBusX = gi.getBusX()[0];

		// Y range of the main bus: covers parent mid-Y and all direct children's anchor mid-Ys.
		double busTop = parentMidY;
		double busBot = parentMidY;
		for (TreeNode child : parent.getChildren()) {
			double midY = anchorMidY(child);
			if (midY < busTop) {
				busTop = midY;
			}
			if (midY > busBot) {
				busBot = midY;
			}
		}

		out.beginPath();
		out.setStrokeWidth(self().getThickness());
		out.setStroke(self().getStrokeStyle());
		out.setFill("none");
		out.beginData();
		// Parent → main bus.
		out.moveToAbs(parentRight, parentMidY);
		out.lineToHorizontalAbs(mainBusX);
		// Main bus vertical.
		out.moveToAbs(mainBusX, busTop);
		out.lineToVerticalAbs(busBot);
		out.endData();
		out.endPath();
	}

	/**
	 * Y coordinate of the anchor's vertical mid-point in the global frame.
	 */
	default double anchorMidY(TreeNode node) {
		Box anchor = node.getAnchor();
		return node.getY() + anchor.getY() + 0.5 * anchor.getHeight();
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
