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
import com.top_logic.graphic.flow.data.TreeConnection;
import com.top_logic.graphic.flow.data.TreeConnector;
import com.top_logic.graphic.flow.data.Widget;

/**
 * Temporary information for tree layouts.
 *
 * <p>
 * The layout algorithm runs bottom-up per subtree: each {@link TreeNode} computes its subtree
 * bounding box, then its parent positions the child subtrees in its own local frame. After the
 * recursive pass the per-root subtrees are stacked vertically into the global tree-layout frame.
 * </p>
 *
 * <p>
 * If a parent node has more than {@link #_maxPerCol} children, its children are arranged in a 2D
 * grid: column-major distribution into <code>R = maxPerCol</code> rows × <code>C = ⌈N/R⌉</code>
 * columns. Each grid sub-column packs independently. The resulting metadata is stored as a
 * {@link GridInfo} for use by the renderer.
 * </p>
 */
public class TreeRenderInfo {

	/**
	 * Layout metadata for a parent node whose children were laid out in a 2D sub-grid (because the
	 * fan-out exceeds {@link TreeRenderInfo#_maxPerCol}).
	 *
	 * <p>
	 * All coordinates are in the global tree-layout frame (after all subtree shifts have been
	 * applied) and are kept consistent by {@link TreeRenderInfo#shiftSubtree}.
	 * </p>
	 */
	public static class GridInfo {

		private final List<List<TreeNode>> _cols;

		private final double[] _colX;

		private final double[] _colW;

		private final double[] _busX;

		private double _bridgeY;

		GridInfo(List<List<TreeNode>> cols, double[] colX, double[] colW, double[] busX, double bridgeY) {
			_cols = cols;
			_colX = colX;
			_colW = colW;
			_busX = busX;
			_bridgeY = bridgeY;
		}

		/**
		 * Children grouped per sub-column, in original child order. Outer list is indexed by
		 * sub-column (0..C-1).
		 */
		public List<List<TreeNode>> getCols() {
			return _cols;
		}

		/**
		 * The number of sub-columns.
		 */
		public int getColCount() {
			return _colX.length;
		}

		/**
		 * X coordinate of the left edge of each sub-column.
		 */
		public double[] getColX() {
			return _colX;
		}

		/**
		 * Width of each sub-column (max subtree width of children placed in that sub-column).
		 */
		public double[] getColW() {
			return _colW;
		}

		/**
		 * X coordinate of the vertical bus per sub-column. The primary parent bus is at
		 * <code>busX[0]</code>; additional buses for follow-up sub-columns are at
		 * <code>busX[c]</code> for <code>c &gt;= 1</code>.
		 */
		public double[] getBusX() {
			return _busX;
		}

		/**
		 * Y coordinate of the horizontal bottom-bridge that connects the primary bus to the buses
		 * of all follow-up sub-columns.
		 */
		public double getBridgeY() {
			return _bridgeY;
		}

		/**
		 * Sub-column index of the given child within this grid, or <code>-1</code> if the node is
		 * not a child placed in this grid.
		 */
		public int getColIdx(TreeNode child) {
			for (int c = 0; c < _cols.size(); c++) {
				if (_cols.get(c).contains(child)) {
					return c;
				}
			}
			return -1;
		}

		void shift(double dx, double dy) {
			for (int c = 0; c < _colX.length; c++) {
				_colX[c] += dx;
				_busX[c] += dx;
			}
			_bridgeY += dy;
		}
	}

	private final Map<Box, TreeNode> _nodeForBox;

	private final Map<Box, TreeNode> _nodeForAnchor;

	private final List<TreeNode> _roots;

	private final List<TreeConnection> _connections;

	private final double _gapX;

	private final double _siblingGapY;

	private final double _subtreeGapY;

	private final boolean _compact;

	private final double _parentAlign;

	private final double _parentOffset;

	private final int _maxPerCol;

	private final double _bridgeGapY;

	private final Map<TreeNode, GridInfo> _gridInfos = new HashMap<>();

	private double _width;

	private double _height;

	/**
	 * Creates a {@link TreeRenderInfo}.
	 */
	public TreeRenderInfo(boolean compact, double gapX, double siblingGapY, double subtreeGapY, double parentAlign,
			double parentOffset, int maxPerCol, double bridgeGapY, List<Box> nodes,
			List<TreeConnection> connections) {
		_compact = compact;
		_gapX = gapX;
		_siblingGapY = siblingGapY;
		_subtreeGapY = subtreeGapY;
		_parentAlign = parentAlign;
		_parentOffset = parentOffset;
		_maxPerCol = maxPerCol;
		_bridgeGapY = bridgeGapY;
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
	 * Resolves the tree node with the given anchor box.
	 */
	public TreeNode getNodeForAnchor(Box anchor) {
		return _nodeForAnchor.get(anchor);
	}

	/**
	 * Returns grid metadata for the given parent node, or <code>null</code> if it is not a
	 * grid-mode parent (i.e. its children fit into a single sub-column).
	 */
	public GridInfo getGridInfo(TreeNode parent) {
		return _gridInfos.get(parent);
	}

	/**
	 * Lays out the tree.
	 *
	 * <p>
	 * Each subtree is first laid out independently in its own local frame; the per-root subtrees
	 * are then stacked vertically into the global frame. Per-parent independent packing means that
	 * unrelated subtrees no longer share global column constraints in Y. Parents with high fan-out
	 * use a 2D sub-grid; the relevant metadata is stored as {@link GridInfo} per parent.
	 * </p>
	 */
	public void computeLayout(RenderContext context) {
		// Distribute the box's intrinsic size in its own coordinate system (origin at 0,0). The
		// tree node's outer position is tracked separately in {@link TreeNode#getX()} /
		// {@link TreeNode#getY()}.
		for (TreeNode node : getNodes()) {
			node.getBox().distributeSize(context, 0, 0, node.getBox().getWidth(), node.getBox().getHeight());
		}

		// Bottom-up layout per root subtree (positions are relative to subtree origin).
		for (TreeNode root : _roots) {
			layoutSubtree(root);
		}

		// Stack root subtrees vertically into the global frame.
		double curY = 0;
		double maxRight = 0;
		boolean firstRoot = true;
		for (TreeNode root : _roots) {
			double minX = subtreeMinX(root);
			double minY = subtreeMinY(root);
			double dx = -minX;
			double dy;
			if (firstRoot) {
				dy = -minY;
				firstRoot = false;
			} else {
				dy = curY - minY + (_compact ? _siblingGapY : _subtreeGapY);
			}
			shiftSubtree(root, dx, dy);
			curY = subtreeMaxY(root);
			maxRight = Math.max(maxRight, subtreeMaxX(root));
		}

		_width = maxRight;
		_height = curY;
	}

	/**
	 * Recursive bottom-up layout of one subtree. After this call, the subtree's nodes have
	 * positions in a local frame; the bounding box of the subtree is anchored at (0, 0) by
	 * {@link #normalizeSubtree(TreeNode)}.
	 */
	private void layoutSubtree(TreeNode node) {
		List<TreeNode> children = node.getChildren();
		if (children.isEmpty()) {
			node.setX(0);
			node.setY(0);
			return;
		}

		for (TreeNode child : children) {
			layoutSubtree(child);
		}

		if (_maxPerCol > 0 && children.size() > _maxPerCol) {
			packGrid(node);
		} else {
			packLinear(node);
		}

		normalizeSubtree(node);
	}

	/**
	 * Linear packing: stack children vertically in a single column right of the parent.
	 */
	private void packLinear(TreeNode node) {
		double childOriginX = node.getBox().getWidth() + _gapX;
		double curY = 0;
		List<TreeNode> children = node.getChildren();
		for (TreeNode child : children) {
			double dx = childOriginX - subtreeMinX(child);
			double dy = curY - subtreeMinY(child);
			shiftSubtree(child, dx, dy);
			curY = subtreeMaxY(child) + _siblingGapY;
		}

		// Center parent based on parentAlign (between first and last child anchors).
		placeParent(node, children.get(0), children.get(children.size() - 1));
	}

	/**
	 * Grid packing: distribute children into <code>R = maxPerCol</code> rows × <code>C = ⌈N/R⌉</code>
	 * sub-columns, column-major. Each sub-column packs independently in Y; a bottom-bridge is
	 * placed below the deepest sub-column.
	 */
	private void packGrid(TreeNode node) {
		List<TreeNode> children = node.getChildren();
		int M = children.size();
		int R = Math.min(M, _maxPerCol);
		int C = (M + R - 1) / R;

		// Column-major distribution: children 0..R-1 go into sub-column 0, etc.
		List<List<TreeNode>> cols = new ArrayList<>(C);
		for (int c = 0; c < C; c++) {
			cols.add(new ArrayList<>());
		}
		for (int i = 0; i < M; i++) {
			cols.get(i / R).add(children.get(i));
		}

		// Sub-column widths from subtree bboxes.
		double[] colW = new double[C];
		for (int c = 0; c < C; c++) {
			double w = 0;
			for (TreeNode ch : cols.get(c)) {
				w = Math.max(w, subtreeMaxX(ch) - subtreeMinX(ch));
			}
			colW[c] = w;
		}

		// X positions per sub-column.
		double childOriginX = node.getBox().getWidth() + _gapX;
		double[] colX = new double[C];
		double[] busX = new double[C];
		double cx = childOriginX;
		for (int c = 0; c < C; c++) {
			colX[c] = cx;
			busX[c] = cx - _gapX / 2;
			cx += colW[c] + _gapX;
		}

		// Pack each sub-column independently.
		double maxBottom = 0;
		for (int c = 0; c < C; c++) {
			double curY = 0;
			for (TreeNode ch : cols.get(c)) {
				double dx = colX[c] - subtreeMinX(ch);
				double dy = curY - subtreeMinY(ch);
				shiftSubtree(ch, dx, dy);
				curY = subtreeMaxY(ch) + _siblingGapY;
			}
			maxBottom = Math.max(maxBottom, curY - _siblingGapY);
		}

		double bridgeY = maxBottom + _bridgeGapY;
		_gridInfos.put(node, new GridInfo(cols, colX, colW, busX, bridgeY));

		// Center parent against first/last child of the primary sub-column.
		List<TreeNode> primary = cols.get(0);
		placeParent(node, primary.get(0), primary.get(primary.size() - 1));
	}

	/**
	 * Place the parent node based on first / last child anchor positions and parentAlign.
	 */
	private void placeParent(TreeNode node, TreeNode first, TreeNode last) {
		Box firstAnchor = first.getAnchor();
		Box lastAnchor = last.getAnchor();
		double firstY = first.getY() + firstAnchor.getY() + 0.5 * firstAnchor.getHeight();
		double lastY = last.getY() + lastAnchor.getY() + 0.5 * lastAnchor.getHeight();
		double anchorY = firstY + (lastY - firstY) * _parentAlign + _parentOffset;
		Box nodeAnchor = node.getAnchor();
		node.setX(0);
		node.setY(anchorY - 0.5 * nodeAnchor.getHeight() - nodeAnchor.getY());
	}

	/**
	 * Shifts the subtree so its bounding box is anchored at (0, 0).
	 */
	private void normalizeSubtree(TreeNode node) {
		double minX = subtreeMinX(node);
		double minY = subtreeMinY(node);
		if (minX != 0 || minY != 0) {
			shiftSubtree(node, -minX, -minY);
		}
	}

	/**
	 * Shifts the entire subtree (positions and any grid metadata) by the given amount.
	 */
	private void shiftSubtree(TreeNode node, double dx, double dy) {
		node.setX(node.getX() + dx);
		node.setY(node.getY() + dy);
		GridInfo gi = _gridInfos.get(node);
		if (gi != null) {
			gi.shift(dx, dy);
		}
		for (TreeNode child : node.getChildren()) {
			shiftSubtree(child, dx, dy);
		}
	}

	private double subtreeMinX(TreeNode node) {
		double m = node.getX();
		for (TreeNode c : node.getChildren()) {
			m = Math.min(m, subtreeMinX(c));
		}
		return m;
	}

	private double subtreeMinY(TreeNode node) {
		double m = node.getY();
		for (TreeNode c : node.getChildren()) {
			m = Math.min(m, subtreeMinY(c));
		}
		return m;
	}

	private double subtreeMaxX(TreeNode node) {
		double m = node.getX() + node.getBox().getWidth();
		for (TreeNode c : node.getChildren()) {
			m = Math.max(m, subtreeMaxX(c));
		}
		return m;
	}

	private double subtreeMaxY(TreeNode node) {
		double m = node.getY() + node.getBox().getHeight();
		GridInfo gi = _gridInfos.get(node);
		if (gi != null) {
			m = Math.max(m, gi.getBridgeY());
		}
		for (TreeNode c : node.getChildren()) {
			m = Math.max(m, subtreeMaxY(c));
		}
		return m;
	}

}
