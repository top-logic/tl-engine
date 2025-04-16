/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.model.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.DiagramDirection;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.data.TreeLayout;
import com.top_logic.graphic.flow.model.layout.FloatingLayoutOperations;

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
 * 
 * <pre>
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
		for (TreeConnection connection : self().getConnections()) {
			enterAnchor(nodeSet, nodeForAnchor, connection.getParent());
			for (TreeConnector child : connection.getChildren()) {
				enterAnchor(nodeSet, nodeForAnchor, child);
			}
		}

		// The parent and children relations on layouted nodes.
		Map<Box, Box> parentForChildNode = new HashMap<>();
		Map<Box, List<Box>> childrenForParentNode = new HashMap<>();
		for (TreeConnection connection : self().getConnections()) {
			Box parentNode = nodeForAnchor.get(connection.getParent().getAnchor());
			List<Box> childNodes =
				connection.getChildren().stream().map(TreeConnector::getAnchor).map(nodeForAnchor::get).toList();
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
		double minY = 0;
		for (Box root : roots) {
			minY = layoutTree(columns, childrenForParentNode, 0, root, minY);
		}

		// Assign X coordinates to all nodes.
		double minX = 0;
		for (List<Box> column : columns) {
			double maxWidth = 0;
			for (Box node : column) {
				node.setX(minX);

				maxWidth = Math.max(maxWidth, node.getWidth());
			}

			minX += maxWidth;
			minX += self().getGapX();
		}

		self().setWidth(minX - (columns.isEmpty() ? 0.0 : self().getGapX()));
		self().setHeight(minY - (roots.isEmpty() ? 0.0 : self().getGapY()));
	}

	private double layoutTree(List<List<Box>> columns, Map<Box, List<Box>> children, int level, Box node, double minY) {
		while (columns.size() <= level) {
			columns.add(new ArrayList<>());
		}
		List<Box> column = columns.get(level);

		if (column.size() > 0) {
			Box last = column.get(column.size() - 1);
			double lastY = last.getY() + last.getHeight() + self().getGapY();
			minY = Math.max(minY, lastY);
		}

		column.add(node);

		List<Box> nextLevel = children.getOrDefault(node, Collections.emptyList());
		if (nextLevel.isEmpty()) {
			node.setY(minY);

			minY += node.getHeight() + self().getGapY();
		} else {
			for (Box child : nextLevel) {
				minY = layoutTree(columns, children, level + 1, child, minY);
			}

			double firstY = nextLevel.get(0).getY();
			Box last = nextLevel.get(nextLevel.size() - 1);
			double lastY = last.getY() + last.getHeight();

			// Place node vertically in the middle of its direct children.
			node.setY((firstY + lastY - node.getHeight()) / 2);

			minY = Math.max(minY, lastY + self().getGapY());
		}

		return minY;
	}

	private void enterAnchor(Set<Box> nodes, Map<Box, Box> nodeForAnchor, TreeConnector connector) {
		Box anchor = connector.getAnchor();
		Box ancestor = anchor;
		while (ancestor != null) {
			if (nodes.contains(ancestor)) {
				nodeForAnchor.put(anchor, ancestor);
				break;
			}
		}
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		FloatingLayoutOperations.super.distributeSize(context, offsetX, offsetY, width, height);

		for (TreeConnection connection : self().getConnections()) {
			double fromX = fromX(connection.getParent().getAnchor());

			double toX = Double.MAX_VALUE;
			for (TreeConnector child : connection.getChildren()) {
				toX = Math.min(toX, toX(child.getAnchor()));
			}

			double barX = (fromX + toX) / 2;

			connection.setBarPosition(barX);
		}
	}

	@Override
	default void drawContents(SvgWriter out) {
		FloatingLayoutOperations.super.drawContents(out);

		for (TreeConnection connection : self().getConnections()) {
			connection.draw(out);
		}
	}

	default double fromX(Box box) {
		// TODO: Depends on self().getDirection() - formulated for LTR:
		return box.getX() + box.getWidth();
	}

	default double toX(Box box) {
		return box.getX();
	}
}
