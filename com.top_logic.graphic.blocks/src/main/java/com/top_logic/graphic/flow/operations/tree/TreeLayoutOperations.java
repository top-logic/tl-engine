/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.DiagramDirection;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.data.TreeLayout;
import com.top_logic.graphic.flow.data.Widget;
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

		Set<Box> nodeSet = new HashSet<>(self().getNodes());

		// Mapping of anchor boxes to layouted top-level boxes. A tree connection may connect a
		// child node of a layouted box.
		Map<Box, Box> nodeForAnchor = new HashMap<>();
		Map<Box, Box> anchorForNode = new HashMap<>();
		for (TreeConnection connection : self().getConnections()) {
			enterAnchor(nodeForAnchor, anchorForNode, nodeSet, connection.getParent());
			for (TreeConnector child : connection.getChildren()) {
				enterAnchor(nodeForAnchor, anchorForNode, nodeSet, child);
			}
		}

		// The parent and children relations on layouted nodes.
		Map<Box, Box> parentForChildNode = new HashMap<>();
		Map<Box, List<Box>> childrenForParentNode = new HashMap<>();
		for (TreeConnection connection : self().getConnections()) {
			Box parentNode = nodeForAnchor.get(connection.getParent().getAnchor());
			List<Box> childNodes =
				connection.getChildren().stream()
					.map(TreeConnector::getAnchor)
					.map(nodeForAnchor::get)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			for (Box childNode : childNodes) {
				parentForChildNode.put(childNode, parentNode);
			}
			childrenForParentNode.put(parentNode, childNodes);
		}

		// The root nodes of the forest to layout.
		List<Box> roots = new ArrayList<>();
		for (Box node : self().getNodes()) {
			if (parentForChildNode.get(node) == null) {
				roots.add(node);
			}
		}
		
		// Sort roots by their (last) Y coordinates to get a stable order of the layout.
		roots.sort(Comparator.comparingDouble(b -> b.getY()));

		// List of columns to embed the tree to.
		List<List<Box>> columns = new ArrayList<>();

		// Enter tree nodes in columns and assign reasonable Y coordinates to nodes.
		double bottomY = -self().getGapY();
		for (Box root : roots) {
			bottomY = layoutTree(columns, childrenForParentNode, anchorForNode, 0, root, bottomY);
		}

		List<Double> columnWidths = new ArrayList<>();

		// Assign X coordinates to all nodes.
		double minX = 0;
		for (List<Box> column : columns) {
			double maxWidth = 0;
			for (Box node : column) {
				maxWidth = Math.max(maxWidth, node.getWidth());
			}

			columnWidths.add(maxWidth);

			minX += maxWidth;
			minX += self().getGapX();
		}

		context.setRenderInfo(self(), new TreeRenderInfo(columns, columnWidths));

		self().setWidth(minX - (columns.isEmpty() ? 0.0 : self().getGapX()));
		self().setHeight(bottomY);
	}

	/**
	 * Helper method to layout a single tree within a forest.
	 * 
	 * @param parentBottomY
	 *        The maximum Y coordinate used among all nodes placed so far (the parent and all of the
	 *        current node's preceding siblings and their sub-trees.
	 */
	default double layoutTree(List<List<Box>> columns, Map<Box, List<Box>> children, Map<Box, Box> anchorForNode,
			int level, Box node,
			double parentBottomY) {
		while (columns.size() <= level) {
			columns.add(new ArrayList<>());
		}
		List<Box> column = columns.get(level);

		// The minimum Y coordinate, where the current node can be placed in its column.
		double minY;
		if (column.size() > 0) {
			Box last = column.get(column.size() - 1);
			minY = last.getBottomY() + self().getGapY();
		} else {
			minY = 0.0;
		}

		if (!self().isCompact()) {
			minY = Math.max(parentBottomY, minY);
		}

		column.add(node);

		double bottomY;

		List<Box> nextLevel = children.getOrDefault(node, Collections.emptyList());
		if (nextLevel.isEmpty()) {
			// This is a leaf node.
			if (!self().isCompact()) {
				for (int l = level + 1; l < columns.size(); l++) {
					List<Box> down = columns.get(l);
					if (down.size() > 0) {
						Box bottom = down.get(down.size() - 1);
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
				childBottomY = layoutTree(columns, children, anchorForNode, level + 1, child, childBottomY);
			}

			Box firstChild = nextLevel.get(0);
			Box lastChild = nextLevel.get(nextLevel.size() - 1);

			Box firstAnchor = anchorForNode.getOrDefault(firstChild, firstChild);
			Box lastAnchor = anchorForNode.getOrDefault(lastChild, lastChild);

			double firstY = firstAnchor.getY() + 0.5 * firstAnchor.getHeight();
			double lastY = lastAnchor.getY() + 0.5 * lastAnchor.getHeight();

			// Place parent node relative to its direct children.
			double anchorY = firstY + (lastY - firstY) * self().getParentAlign() + self().getParentOffset();

			Box nodeAnchor = anchorForNode.getOrDefault(node, node);
			double nodeAnchorOffset = nodeAnchor.getY() - node.getY();
			double nodeY = anchorY - 0.5 * nodeAnchor.getHeight() - nodeAnchorOffset;

			if (nodeY >= minY) {
				// Current node must be moved to the bottom to align with its children.
				minY = nodeY;
			} else {
				// Children must be moved to the bottom to align with the current node.

				// Amount to shift the subtree to the bottom.
				double shiftY = minY - nodeY;
				for (Box child : nextLevel) {
					shiftY(children, child, shiftY);
				}
				childBottomY += shiftY;
			}
			node.setY(nodeY);

			bottomY = Math.max(node.getBottomY(), childBottomY);
		}

		return bottomY;
	}

	/**
	 * Shifts the subtree rooted at the given node to the bottom.
	 */
	default void shiftY(Map<Box, List<Box>> children, Box node, double shiftY) {
		node.setY(node.getY() + shiftY);
		for (Box child : children.getOrDefault(node, Collections.emptyList())) {
			shiftY(children, child, shiftY);
		}
	}

	/**
	 * Helper method to build the mapping from nodes (that are layouted) for anchor boxes (that are
	 * visually connected).
	 * 
	 * @param nodeForAnchor
	 *        The mapping from anchor to node that is built.
	 * @param anchorForNode
	 *        The mapping from node to its anchor that is built.
	 * @param nodes
	 *        all top-level tree nodes that are layouted.
	 * @param connector
	 *        The tree connector to analyze.
	 */
	default void enterAnchor(Map<Box, Box> nodeForAnchor, Map<Box, Box> anchorForNode, Set<Box> nodes,
			TreeConnector connector) {
		final Box anchor = connector.getAnchor();
		Box ancestor = anchor;
		while (ancestor != null) {
			if (nodes.contains(ancestor)) {
				nodeForAnchor.put(anchor, ancestor);
				anchorForNode.put(ancestor, anchor);
				break;
			}

			Widget parent = ancestor.getParent();
			if (parent instanceof Box) {
				ancestor = (Box) parent;
			} else {
				// Not found, most likely an error.
				break;
			}
		}
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
		int columnIndex = 0;
		for (List<Box> column : info.getColumns()) {
			double columnWidth = info.getColumnWidths().get(columnIndex++);

			for (Box node : column) {
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
