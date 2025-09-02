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

import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.data.Widget;

/**
 * Temporary information for tree layouts.
 */
public class TreeRenderInfo {

	static class Column {
		List<Box> _boxes = new ArrayList<>();

		double _width;

		/**
		 * The width of this column (maximum width of nodes placed in this column).
		 */
		double getWidth() {
			return _width;
		}

		/**
		 * All layouted boxes that are placed in this column.
		 */
		List<Box> getBoxes() {
			return _boxes;
		}

		/**
		 * TODO
		 *
		 * @param node
		 */
		void addBox(Box node) {
			_boxes.add(node);
		}

		/**
		 * TODO
		 *
		 * @return
		 */
		int size() {
			return _boxes.size();
		}

		/**
		 * TODO
		 *
		 * @param i
		 * @return
		 */
		Box getBox(int i) {
			return _boxes.get(i);
		}
	}

	private final List<Column> _columns = new ArrayList<>();

	private Map<Box, Box> _nodeForAnchor;

	private Map<Box, Box> _anchorForNode;

	private List<Box> _roots;

	private Map<Box, Box> _parentForChildNode;

	private Map<Box, List<Box>> _childrenForParentNode;

	private double _gapY;

	private boolean _compact;

	private double _parentAlign;

	private double _parentOffset;

	private double _gapX;

	private double _width;

	private double _height;

	/**
	 * Creates a {@link TreeRenderInfo}.
	 */
	public TreeRenderInfo(boolean compact, double gapX, double gapY, double parentAlign, double parentOffset,
			List<Box> nodes,
			List<TreeConnection> connections) {
		_compact = compact;
		_gapX = gapX;
		_gapY = gapY;
		_parentAlign = parentAlign;
		_parentOffset = parentOffset;
		Set<Box> nodeSet = new HashSet<>(nodes);

		_nodeForAnchor = new HashMap<>();
		_anchorForNode = new HashMap<>();
		for (TreeConnection connection : connections) {
			enterAnchor(_nodeForAnchor, _anchorForNode, nodeSet, connection.getParent());
			for (TreeConnector child : connection.getChildren()) {
				enterAnchor(_nodeForAnchor, _anchorForNode, nodeSet, child);
			}
		}

		_parentForChildNode = new HashMap<>();
		_childrenForParentNode = new HashMap<>();
		for (TreeConnection connection : connections) {
			Box parentNode = _nodeForAnchor.get(connection.getParent().getAnchor());
			List<Box> childNodes =
				connection.getChildren().stream()
					.map(TreeConnector::getAnchor)
					.map(_nodeForAnchor::get)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			for (Box childNode : childNodes) {
				_parentForChildNode.put(childNode, parentNode);
			}
			_childrenForParentNode.put(parentNode, childNodes);
		}

		_roots = new ArrayList<>();
		for (Box node : nodes) {
			if (_parentForChildNode.get(node) == null) {
				_roots.add(node);
			}
		}

		// Sort roots by their (last) Y coordinates to get a stable order of the layout.
		_roots.sort(Comparator.comparingDouble(b -> b.getY()));
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
	private void enterAnchor(Map<Box, Box> nodeForAnchor, Map<Box, Box> anchorForNode, Set<Box> nodes,
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

	/**
	 * The organization of content boxes into columns.
	 */
	public List<Column> getColumns() {
		return _columns;
	}

	/**
	 * Layouted nodes without parents.
	 */
	public List<Box> getRoots() {
		return _roots;
	}

	/**
	 * Get or create a column for the given tree depth.
	 */
	public Column mkColumn(int level) {
		while (_columns.size() <= level) {
			_columns.add(new Column());
		}
		return _columns.get(level);
	}

	/**
	 * The children nodes for the given parent node.
	 */
	public List<Box> getChildren(Box node) {
		return _childrenForParentNode.getOrDefault(node, Collections.emptyList());
	}

	/**
	 * TODO
	 *
	 * @param node
	 * @return
	 */
	public Box getAnchor(Box node) {
		return _anchorForNode.getOrDefault(node, node);
	}

	/**
	 * Shifts the subtree rooted at the given node to the bottom.
	 */
	public void shiftY(Box node, double shiftY) {
		node.setY(node.getY() + shiftY);
		for (Box child : getChildren(node)) {
			shiftY(child, shiftY);
		}
	}

	/**
	 * Arranges nodes in columns so that a parent node is placed in a column left to the column of
	 * its children.
	 */
	public void computeLayout() {
		double bottomY = -_gapY;

		// Enter tree nodes in columns and assign reasonable Y coordinates to nodes.
		for (Box root : getRoots()) {
			bottomY = layoutTree(0, root, bottomY);
		}

		// Compute column widths.
		double minX = 0;
		for (Column column : getColumns()) {
			double maxWidth = 0;
			for (Box node : column.getBoxes()) {
				maxWidth = Math.max(maxWidth, node.getWidth());
			}

			column._width = maxWidth;

			minX += maxWidth;
			minX += _gapX;
		}

		_width = minX - (getColumns().isEmpty() ? 0.0 : _gapX);
		_height = bottomY;
	}

	/**
	 * Helper method to layout a single tree within a forest.
	 * 
	 * @param parentBottomY
	 *        The maximum Y coordinate used among all nodes placed so far (the parent and all of the
	 *        current node's preceding siblings and their sub-trees.
	 */
	private double layoutTree(int level, Box node, double parentBottomY) {
		Column column = mkColumn(level);

		// The minimum Y coordinate, where the current node can be placed in its column.
		double minY;
		if (column.getBoxes().size() > 0) {
			Box last = column.getBoxes().get(column.getBoxes().size() - 1);
			minY = last.getBottomY() + _gapY;
		} else {
			minY = 0.0;
		}

		if (_compact) {
			minY = Math.max(parentBottomY, minY);
		}

		column.addBox(node);

		double bottomY;

		List<Box> nextLevel = getChildren(node);
		if (nextLevel.isEmpty()) {
			// This is a leaf node.
			if (!_compact) {
				for (int l = level + 1; l < getColumns().size(); l++) {
					Column down = getColumns().get(l);
					if (down.size() > 0) {
						Box bottom = down.getBox(down.size() - 1);
						minY = Math.max(minY, bottom.getBottomY() + _gapY);
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
				childBottomY = layoutTree(level + 1, child, childBottomY);
			}

			// Place parent node relative to its direct children.
			Box firstChild = nextLevel.get(0);
			Box lastChild = nextLevel.get(nextLevel.size() - 1);

			Box firstAnchor = getAnchor(firstChild);
			Box lastAnchor = getAnchor(lastChild);

			// Note: Anchor boxes are up to this point relative to the node's coordinate.
			double firstY = firstChild.getY() + firstAnchor.getY() + 0.5 * firstAnchor.getHeight();
			double lastY = lastChild.getY() + lastAnchor.getY() + 0.5 * lastAnchor.getHeight();

			// The Y coordinate of the center of the node's anchor box.
			double anchorY = firstY + (lastY - firstY) * _parentAlign + _parentOffset;

			Box nodeAnchor = getAnchor(node);

			double nodeY = anchorY - nodeAnchor.getY() - 0.5 * nodeAnchor.getHeight();

			if (nodeY >= minY) {
				// Current node must be moved to the bottom to align with its children.
				minY = nodeY;
			} else {
				// Children must be moved to the bottom to align with the current node.

				// nodeY < minY: Amount to shift the subtree to the bottom.
				double shiftY = minY - nodeY;
				for (Box child : nextLevel) {
					shiftY(child, shiftY);
				}
				childBottomY += shiftY;

				nodeY += shiftY;
			}
			node.setY(nodeY);

			bottomY = Math.max(node.getBottomY(), childBottomY);
		}

		return bottomY;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public double getWidth() {
		return _width;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public double getHeight() {
		return _height;
	}

}
