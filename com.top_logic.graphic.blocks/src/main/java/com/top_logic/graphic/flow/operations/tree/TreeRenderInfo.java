/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.EdgeDecoration;
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.data.Widget;

/**
 * Temporary information for tree layouts.
 */
public class TreeRenderInfo {

	class Column {
		private List<TreeNode> _boxes = new ArrayList<>();

		private double _columnWidth;

		private double _offsetX;

		private int _level;

		private double _decorationWidth;

		/**
		 * Creates a {@link Column}.
		 *
		 * @param level
		 *        See {@link #getLevel()}.
		 */
		public Column(int level) {
			_level = level;
		}

		/**
		 * The level of the nodes that are placed to this column. A root node has level 0, a child
		 * node has the level of its parent incremented by 1.
		 */
		public int getLevel() {
			return _level;
		}

		/**
		 * The width of this column (maximum width of nodes placed in this column).
		 */
		public double getWidth() {
			return _columnWidth;
		}

		/**
		 * All layouted boxes that are placed in this column.
		 */
		public List<TreeNode> getBoxes() {
			return _boxes;
		}

		/**
		 * Adds a new node to this {@link Column}.
		 */
		public void addNode(TreeNode node) {
			int index = _boxes.size();
			_boxes.add(node);
			node.setColumn(this, index);
		}

		/**
		 * The number of nodes in this column.
		 */
		public int size() {
			return _boxes.size();
		}

		/**
		 * The node with the given index.
		 * 
		 * @see TreeNode#getIndex()
		 */
		public TreeNode getNode(int index) {
			return _boxes.get(index);
		}

		/**
		 * Computes the width of this column from the maximum width of its nodes.
		 */
		public void computeWidth() {
			double maxWidth = 0;
			for (TreeNode node : getBoxes()) {
				maxWidth = Math.max(maxWidth, node.getBox().getWidth());
			}

			_columnWidth = maxWidth;
		}

		/**
		 * The X coordinate of the left border of this column.
		 */
		public double getOffsetX() {
			return _offsetX;
		}

		/**
		 * @see #getOffsetX()
		 */
		public void setOffsetX(double offsetX) {
			_offsetX = offsetX;
		}

		/**
		 * The maximum width of decorations placed on the left of this column.
		 */
		public double getDecorationWidth() {
			return _decorationWidth;
		}

		/**
		 * @see #getDecorationWidth()
		 */
		public void requestDecorationWidth(double decorationWidth) {
			if (decorationWidth > _decorationWidth) {
				_decorationWidth = decorationWidth;
			}
		}
	}

	private final Map<Box, TreeNode> _nodeForBox;

	private final Map<Box, TreeNode> _nodeForAnchor;

	private final List<TreeNode> _roots;

	private final List<Column> _columns = new ArrayList<>();

	private final List<TreeConnection> _connections;

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
		_connections = connections;

		// Create tree nodes from node boxes.
		_nodeForBox = new LinkedHashMap<>();
		for (Box node : nodes) {
			_nodeForBox.put(node, new TreeNode(node));
		}

		// Resolve anchor boxes within nodes.
		_nodeForAnchor = new HashMap<>();
		for (TreeConnection connection : connections) {
			enterAnchor(connection.getParent());
			enterAnchor(connection.getChild());
		}

		// Reconstruct tree structure.
		for (TreeConnection connection : connections) {
			TreeNode parentNode = _nodeForAnchor.get(connection.getParent().getAnchor());
			TreeNode childNode = _nodeForAnchor.get(connection.getChild().getAnchor());

			childNode.setParent(parentNode);
			parentNode.addChild(childNode);
		}

		// Find root nodes.
		_roots = new ArrayList<>();
		for (TreeNode node : getNodes()) {
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
		if (_nodeForAnchor.containsKey(anchor)) {
			return;
		}
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
	 * Layouted nodes without parents.
	 */
	public List<TreeNode> getRoots() {
		return _roots;
	}

	/**
	 * All nodes of this tree.
	 */
	public Collection<TreeNode> getNodes() {
		return _nodeForBox.values();
	}

	/**
	 * The width of the complete tree.
	 */
	public double getWidth() {
		return _width;
	}

	/**
	 * The height of the complete tree.
	 */
	public double getHeight() {
		return _height;
	}

	/**
	 * The organization of content boxes into columns.
	 */
	public List<Column> getColumns() {
		return _columns;
	}

	/**
	 * Resolves the tree node with the given anchor box.
	 */
	public TreeNode getNodeForAnchor(Box anchor) {
		return _nodeForAnchor.get(anchor);
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
		for (Column column : getColumns()) {
			column.computeWidth();
		}

		// Reserve space for decorations.
		for (TreeConnection connection : _connections) {
			Column column = getNodeForAnchor(connection.getChild().getAnchor()).getColumn();

			double additionalWidth = 0;
			for (EdgeDecoration decoration : connection.getDecorations()) {
				double decorationWidth = decoration.getContent().getWidth();
				additionalWidth = Math.max(additionalWidth,
					decorationWidth
					// Free space due to the column gap.
					- _gapX / 2
					// Free space due to the anchor width.
					- (column.getWidth() - connection.getChild().getAnchor().getWidth()) / 2);
			}

			column.requestDecorationWidth(additionalWidth);
		}

		// Place columns and compute overall width.
		double width = 0;
		if (!getColumns().isEmpty()) {
			for (Column column : getColumns()) {
				// Additional width reserved for decorations.
				width += column.getDecorationWidth();

				column.setOffsetX(width);

				width += column.getWidth();

				// Gap to the next column.
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

		_width = width;
	}

	private void enterColumns(int level, TreeNode root) {
		Column column = mkColumn(level);
		column.addNode(root);

		for (TreeNode child : root.getChildren()) {
			enterColumns(level + 1, child);
		}
	}

	/**
	 * Get or create a column for the given tree depth.
	 */
	private Column mkColumn(int level) {
		while (_columns.size() <= level) {
			_columns.add(new Column(level));
		}
		return _columns.get(level);
	}

	/**
	 * Helper method to layout a single tree within a forest.
	 */
	private void layoutTree(TreeNode node) {
		// The minimum Y coordinate, where the current node can be placed in its column.
		double minY;
		if (node.getLevel() == 0 && node.getIndex() > 0 && !_compact) {
			// A whole new tree is layouted, below an existing one. Do not mix contents in a line.
			minY = _height + _subtreeGapY;
		} else if (node.getIndex() == 0) {
			minY = 0;
		} else {
			TreeNode predecessor = node.getColumnPredecessor();
			boolean sameParent = predecessor.getParent() == node.getParent();
			minY = predecessor.getBottom() + (sameParent ? _siblingGapY : _subtreeGapY);
		}

		List<TreeNode> nextLevel = node.getChildren();
		double nodeY;
		if (nextLevel.isEmpty()) {
			// This is a leaf node, place it at the minimum Y coordinate possible.
			nodeY = minY;
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

			Box nodeAnchor = node.getAnchor();
			nodeY = anchorY - 0.5 * nodeAnchor.getHeight() - nodeAnchor.getY();

			if (nodeY >= minY) {
				// Node position is OK.
			} else {
				// nodeY < minY, amount to shift the subtree to the bottom.
				double shiftY = minY - nodeY;

				// Place node at its minimum Y position and shift children to the bottom.
				nodeY = minY;

				// Children must be moved to the bottom to align with the current node.
				for (TreeNode child : nextLevel) {
					shiftY(child, shiftY);
				}
			}
		}

		node.setY(nodeY);
		_height = Math.max(_height, node.getBottom());
	}

	/**
	 * Shifts the subtree rooted at the given node to the bottom by the given amount.
	 */
	private void shiftY(TreeNode node, double shiftY) {
		node.setY(node.getY() + shiftY);
		_height = Math.max(_height, node.getBottom());
	
		for (TreeNode child : node.getChildren()) {
			shiftY(child, shiftY);
		}
	}

}
