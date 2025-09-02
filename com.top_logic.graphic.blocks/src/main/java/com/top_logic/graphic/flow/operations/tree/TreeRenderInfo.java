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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.data.Widget;

/**
 * Temporary information for tree layouts.
 */
public class TreeRenderInfo {

	class Column {
		List<TreeNode> _boxes = new ArrayList<>();

		double _width;

		private double _offsetX;

		private int _level;

		/**
		 * Creates a {@link Column}.
		 *
		 * @param level
		 */
		public Column(int level) {
			_level = level;
		}

		/**
		 * TODO
		 */
		public int getLevel() {
			return _level;
		}

		/**
		 * The width of this column (maximum width of nodes placed in this column).
		 */
		double getWidth() {
			return _width;
		}

		/**
		 * All layouted boxes that are placed in this column.
		 */
		List<TreeNode> getBoxes() {
			return _boxes;
		}

		/**
		 * TODO
		 *
		 * @param root
		 */
		void addBox(TreeNode node) {
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
		TreeNode getBox(int i) {
			return _boxes.get(i);
		}

		public void computeWidth() {
			double maxWidth = 0;
			for (TreeNode node : getBoxes()) {
				maxWidth = Math.max(maxWidth, node.getBox().getWidth());
			}

			_width = maxWidth;
		}

		public void setOffsetX(double offsetX) {
			_offsetX = offsetX;

		}

		/**
		 * The Y coordinate of the box that is inserted to this column next.
		 */
		public double getOffsetY() {
			if (getBoxes().size() > 0) {
				TreeNode last = getBoxes().get(getBoxes().size() - 1);
				return last.getBox().getBottomY() + _gapY;
			} else {
				return 0.0;
			}
		}
	}

	private final List<Column> _columns = new ArrayList<>();

	private Map<Box, TreeNode> _nodeForAnchor;

	private List<TreeNode> _roots;

	private double _gapY;

	private boolean _compact;

	private double _parentAlign;

	private double _parentOffset;

	private double _gapX;

	private double _width;

	private double _height;

	private Map<Box, TreeNode> _nodeSet;

	/**
	 * Creates a {@link TreeRenderInfo}.
	 */
	public TreeRenderInfo(boolean compact, double gapX, double gapY, double parentAlign, double parentOffset,
			List<Box> nodes, List<TreeConnection> connections) {
		_compact = compact;
		_gapX = gapX;
		_gapY = gapY;
		_parentAlign = parentAlign;
		_parentOffset = parentOffset;
		_nodeSet = new HashMap<>();
		for (Box node : nodes) {
			_nodeSet.put(node, new TreeNode(node));
		}

		_nodeForAnchor = new HashMap<>();
		for (TreeConnection connection : connections) {
			enterAnchor(connection.getParent());
			for (TreeConnector child : connection.getChildren()) {
				enterAnchor(child);
			}
		}

		for (TreeConnection connection : connections) {
			TreeNode parentNode = _nodeForAnchor.get(connection.getParent().getAnchor());
			List<TreeNode> childNodes =
				connection.getChildren().stream()
					.map(TreeConnector::getAnchor)
					.map(_nodeForAnchor::get)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			for (TreeNode childNode : childNodes) {
				childNode.setParent(parentNode);
			}
			parentNode.setChildren(Collections.unmodifiableList(childNodes));
		}

		_roots = new ArrayList<>();
		for (TreeNode node : _nodeSet.values()) {
			if (node.getParent() == null) {
				_roots.add(node);
			}
		}

		// Sort roots by their (last) Y coordinates to get a stable order of the layout.
		_roots.sort(Comparator.comparingDouble(b -> b.getBox().getY()));
	}

	/**
	 * Helper method to build the mapping from nodes (that are layouted) for anchor boxes (that are
	 * visually connected).
	 * 
	 * @param connector
	 *        The tree connector to analyze.
	 */
	private void enterAnchor(TreeConnector connector) {
		final Box anchor = connector.getAnchor();
		Box ancestor = anchor;
		while (ancestor != null) {
			TreeNode node = _nodeSet.get(ancestor);
			if (node != null) {
				node.setAnchor(anchor);
				_nodeForAnchor.put(anchor, node);
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
	public List<TreeNode> getRoots() {
		return _roots;
	}

	/**
	 * Get or create a column for the given tree depth.
	 */
	public Column mkColumn(int level) {
		while (_columns.size() <= level) {
			_columns.add(new Column(level));
		}
		return _columns.get(level);
	}

	/**
	 * TODO
	 *
	 * @param node
	 * @return
	 */
	public Box getAnchor(TreeNode node) {
		return node.getAnchor();
	}

	/**
	 * Shifts the subtree rooted at the given node to the bottom.
	 */
	public void shiftY(TreeNode node, double shiftY) {
		node.getBox().setY(node.getBox().getY() + shiftY);
		for (TreeNode child : node.getChildren()) {
			shiftY(child, shiftY);
		}
	}

	/**
	 * Arranges nodes in columns so that a parent node is placed in a column left to the column of
	 * its children.
	 */
	public void computeLayout() {
		double width = 0;
		double height = 0;

		// Enter tree nodes in columns and assign reasonable Y coordinates to nodes.
		if (!getRoots().isEmpty()) {
			height -= -_gapY;
			for (TreeNode root : getRoots()) {
				height = layoutTree(0, root, height);
			}
		}

		// Compute column widths.
		if (!getColumns().isEmpty()) {
			for (Column column : getColumns()) {
				column.setOffsetX(width);
				column.computeWidth();

				width += column.getWidth();
				width += _gapX;
			}

			// Subtract last gap.
			width -= _gapX;
		}

		_width = width;
		_height = height;
	}

	/**
	 * Helper method to layout a single tree within a forest.
	 * 
	 * @param parentBottomY
	 *        The maximum Y coordinate used among all nodes placed so far (the parent and all of the
	 *        current node's preceding siblings and their sub-trees.
	 */
	private double layoutTree(int level, TreeNode root, double parentBottomY) {
		Column column = mkColumn(level);

		// The minimum Y coordinate, where the current node can be placed in its column.
		double minY = column.getOffsetY();
		if (_compact) {
			minY = Math.max(parentBottomY, minY);
		}

		column.addBox(root);

		double bottomY;

		List<TreeNode> nextLevel = root.getChildren();
		if (nextLevel.isEmpty()) {
			// This is a leaf node.
			if (!_compact) {
				for (int l = level + 1; l < getColumns().size(); l++) {
					Column down = getColumns().get(l);
					if (down.size() > 0) {
						TreeNode bottom = down.getBox(down.size() - 1);
						minY = Math.max(minY, bottom.getBox().getBottomY() + _gapY);
					}
				}
			}

			// Place node at the minimum Y coordinate possible.
			root.getBox().setY(minY);
			bottomY = minY + root.getBox().getHeight();
		} else {
			// Recursively place all children.
			double childBottomY = minY;
			for (TreeNode child : nextLevel) {
				childBottomY = layoutTree(level + 1, child, childBottomY);
			}

			// Place parent node relative to its direct children.
			TreeNode firstChild = nextLevel.get(0);
			TreeNode lastChild = nextLevel.get(nextLevel.size() - 1);

			Box firstAnchor = getAnchor(firstChild);
			Box lastAnchor = getAnchor(lastChild);

			// Note: Anchor boxes are up to this point relative to the node's coordinate.
			double firstY = firstChild.getBox().getY() + firstAnchor.getY() + 0.5 * firstAnchor.getHeight();
			double lastY = lastChild.getBox().getY() + lastAnchor.getY() + 0.5 * lastAnchor.getHeight();

			// The Y coordinate of the center of the node's anchor box.
			double anchorY = firstY + (lastY - firstY) * _parentAlign + _parentOffset;

			Box nodeAnchor = getAnchor(root);

			double nodeY = anchorY - nodeAnchor.getY() - 0.5 * nodeAnchor.getHeight();

			if (nodeY >= minY) {
				// Current node must be moved to the bottom to align with its children.
				minY = nodeY;
			} else {
				// Children must be moved to the bottom to align with the current node.

				// nodeY < minY: Amount to shift the subtree to the bottom.
				double shiftY = minY - nodeY;
				for (TreeNode child : nextLevel) {
					shiftY(child, shiftY);
				}
				childBottomY += shiftY;

				nodeY += shiftY;
			}
			root.getBox().setY(nodeY);

			bottomY = Math.max(root.getBox().getBottomY(), childBottomY);
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
