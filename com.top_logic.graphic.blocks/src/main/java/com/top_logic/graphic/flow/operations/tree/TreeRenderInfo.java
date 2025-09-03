/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.data.Widget;

/**
 * Temporary information for tree layouts.
 */
public class TreeRenderInfo {

	class Column {
		private List<TreeNode> _boxes = new ArrayList<>();

		private double _width;

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
		public double getWidth() {
			return _width;
		}

		/**
		 * All layouted boxes that are placed in this column.
		 */
		public List<TreeNode> getBoxes() {
			return _boxes;
		}

		/**
		 * TODO
		 *
		 * @param root
		 */
		public void addBox(TreeNode node) {
			int index = _boxes.size();
			_boxes.add(node);
			node.setColumn(this, index);
		}

		/**
		 * TODO
		 *
		 * @return
		 */
		public int size() {
			return _boxes.size();
		}

		/**
		 * TODO
		 *
		 * @param index
		 * @return
		 */
		public TreeNode getNode(int index) {
			return _boxes.get(index);
		}

		public void computeWidth() {
			double maxWidth = 0;
			for (TreeNode node : getBoxes()) {
				maxWidth = Math.max(maxWidth, node.getBox().getWidth());
			}

			_width = maxWidth;
		}

		/**
		 * TODO
		 */
		public double getOffsetX() {
			return _offsetX;
		}

		public void setOffsetX(double offsetX) {
			_offsetX = offsetX;

		}

		/**
		 * TODO
		 *
		 * @return
		 */
		public double getHeight() {
			return size() == 0 ? 0 : getLast().getBottom();
		}

		/**
		 * TODO
		 *
		 * @return
		 */
		public TreeNode getLast() {
			return size() == 0 ? null : getNode(size() - 1);
		}
	}

	private final Map<Box, TreeNode> _nodeForBox;

	private final Map<Box, TreeNode> _nodeForAnchor;

	private final List<TreeNode> _roots;

	private final List<Column> _columns = new ArrayList<>();

	private final double _gapX;

	private final double _siblingGapY;

	private final double _subtreeGapY;

	private final boolean _compact;

	private final double _parentAlign;

	private final double _parentOffset;

	private double _width;

	private double _height;

	/**
	 * Creates a {@link TreeRenderInfo}.
	 */
	public TreeRenderInfo(boolean compact, double gapX, double siblingGapY, double subtreeGapY, double parentAlign,
			double parentOffset, List<Box> nodes, List<TreeConnection> connections) {
		_compact = compact;
		_gapX = gapX;
		_siblingGapY = siblingGapY;
		_subtreeGapY = subtreeGapY;
		_parentAlign = parentAlign;
		_parentOffset = parentOffset;
		_nodeForBox = new HashMap<>();
		for (Box node : nodes) {
			_nodeForBox.put(node, new TreeNode(node));
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
		for (TreeNode node : _nodeForBox.values()) {
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
			TreeNode node = _nodeForBox.get(ancestor);
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

	public TreeNode getNodeForAnchor(Box anchor) {
		return _nodeForAnchor.get(anchor);
	}

	/**
	 * Shifts the subtree rooted at the given node to the bottom by the given amount.
	 */
	public void shiftY(TreeNode node, double shiftY) {
		node.setY(node.getY() + shiftY);
		for (TreeNode child : node.getChildren()) {
			shiftY(child, shiftY);
		}
	}

	/**
	 * Arranges nodes in columns so that a parent node is placed in a column left to the column of
	 * its children.
	 */
	public void computeLayout(RenderContext context) {
		// Insert tree nodes into columns.
		for (TreeNode root : getRoots()) {
			enterColumns(0, root);
		}

		// Compute column widths.
		double width = 0;
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

		// Compute internal layout of nodes. This is required before computing the tree layout,
		// since the internal position of the node's anchor must be known.
		for (TreeNode node : _nodeForBox.values()) {
			// Offer the whole column width to the node, but restrict its height to its intrinsic
			// height. Place the node at (0,0) to make internal node coordinates relative. The node
			// is placed externally by a transformed group when rendering.
			node.getBox().distributeSize(context, 0, 0, node.getColumn().getWidth(), node.getBox().getHeight());
		}

		// Enter tree nodes in columns and assign reasonable Y coordinates to nodes.
		for (TreeNode root : getRoots()) {
			layoutTree(root);
		}

		double height = 0;
		for (Column column : getColumns()) {
			height = Math.max(height, column.getHeight());
		}

		_width = width;
		_height = height;
	}

	private void enterColumns(int level, TreeNode root) {
		Column column = mkColumn(level);
		column.addBox(root);

		List<TreeNode> nextLevel = root.getChildren();
		for (TreeNode child : nextLevel) {
			enterColumns(level + 1, child);
		}
	}

	/**
	 * Helper method to layout a single tree within a forest.
	 */
	private void layoutTree(TreeNode root) {
		// The minimum Y coordinate, where the current node can be placed in its column.
		double minY;
		if (root.getIndex() == 0) {
			minY = 0;
		} else {
			TreeNode predecessor = root.getColumnPredecessor();
			boolean sameParent = predecessor.getParent() == root.getParent();
			minY = predecessor.getBottom() + (sameParent ? _siblingGapY : _subtreeGapY);
		}

		List<TreeNode> nextLevel = root.getChildren();
		if (nextLevel.isEmpty()) {
			// This is a leaf node.
			if (!_compact) {
				// Use the minimum Y coordinate that has free space in all columns to the right.
				for (int l = root.getLevel() + 1; l < getColumns().size(); l++) {
					Column down = getColumns().get(l);
					if (down.size() > 0) {
						TreeNode bottom = down.getNode(down.size() - 1);
						minY = Math.max(minY, bottom.getBottom() + _subtreeGapY);
					}
				}
			}

			// Place node at the minimum Y coordinate possible.
			root.setY(minY);
		} else {
			// Recursively place all children.
			for (TreeNode child : nextLevel) {
				layoutTree(child);
			}

			// Place parent node relative to its direct children.
			TreeNode firstChild = nextLevel.get(0);
			TreeNode lastChild = nextLevel.get(nextLevel.size() - 1);

			Box firstAnchor = firstChild.getAnchor();
			Box lastAnchor = lastChild.getAnchor();

			// Note: Anchor boxes are relative to the node's coordinate (which is 0,0 for each
			// node).
			double firstY = firstChild.getY() + firstAnchor.getY() + 0.5 * firstAnchor.getHeight();
			double lastY = lastChild.getY() + lastAnchor.getY() + 0.5 * lastAnchor.getHeight();

			// The Y coordinate of the center of the node's anchor box.
			double anchorY = firstY + (lastY - firstY) * _parentAlign + _parentOffset;

			Box nodeAnchor = root.getAnchor();

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

				nodeY += shiftY;
			}
			root.setY(nodeY);
		}
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

	/**
	 * TODO
	 *
	 * @return
	 */
	public Collection<TreeNode> getNodes() {
		return _nodeForBox.values();
	}

}
