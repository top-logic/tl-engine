/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations.tree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.EdgeDecoration;
import com.top_logic.react.flow.data.TreeConnection;
import com.top_logic.react.flow.data.TreeConnector;
import com.top_logic.react.flow.data.Widget;

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
 * If a parent node has more than {@link #_childSplitThreshold} children, its children are
 * arranged in a 2D sub-grid. Two layouts are supported (selected by {@link #_rowWise}):
 * </p>
 * <ul>
 * <li>Column-wise (default): column-major distribution into <code>R = childSplitThreshold</code>
 * rows × <code>C = ⌈N/R⌉</code> sub-columns; each sub-column packs independently and has its own
 * bus, follow-up buses are connected to the primary bus by a horizontal bottom-bridge.</li>
 * <li>Row-wise: only the direct children sit in the sub-grid (in
 * <code>C = childSplitThreshold</code> sub-columns, row-major), all sub-grid children's subtrees
 * are routed to a single column to the right of the sub-grid (so depth↔X correspondence is kept
 * for descendants), and one vertical bus is routed just outside the sub-grid for both the
 * parent→direct-children fan-out and any sub-grid-child→subtree connections.</li>
 * </ul>
 */
public class TreeRenderInfo {

	/**
	 * Layout metadata for a parent node whose children were laid out in a 2D sub-grid (because the
	 * fan-out exceeds {@link TreeRenderInfo#_childSplitThreshold}).
	 *
	 * <p>
	 * All coordinates are in the global tree-layout frame (after all subtree shifts have been
	 * applied) and are kept consistent by {@link TreeRenderInfo#shiftSubtree}.
	 * </p>
	 */
	public static class GridInfo {

		/**
		 * Layout flavor: {@link #COLUMN_WISE} = legacy column-major sub-grid with per-column buses
		 * and bottom-bridge; {@link #ROW_WISE} = direct children in row-major sub-grid, subtrees
		 * in a single column to the right, single bus.
		 */
		public enum Kind {
			/** Children placed column-major; each sub-column has its own bus; follow-up buses
			 * meet a horizontal bottom-bridge. */
			COLUMN_WISE,
			/** Children placed row-major; only direct children in the sub-grid; subtrees
			 * routed to a single post-grid column; single vertical bus. */
			ROW_WISE
		}

		private final Kind _kind;

		/** Children grouped per sub-column (only set for {@link Kind#COLUMN_WISE}). */
		private final List<List<TreeNode>> _cols;

		private final double[] _colX;

		private final double[] _colW;

		/** Bus X coordinates. {@link Kind#COLUMN_WISE}: one entry per sub-column. {@link Kind#ROW_WISE}:
		 * single-element array with the main parent→children bus. */
		private final double[] _busX;

		/** Bottom-bridge Y; only meaningful for {@link Kind#COLUMN_WISE}. */
		private double _bridgeY;

		/** X coordinate of the post-grid column where all sub-grid children's subtrees sit; only
		 * meaningful for {@link Kind#ROW_WISE}. */
		private double _postGridX;

		/** X coordinate of the bus that sub-grid children with subtrees use for their outgoing
		 * connections; only meaningful for {@link Kind#ROW_WISE}. Equals {@code postGridX − gapX/2}
		 * by construction. */
		private double _childBusX;

		private GridInfo(Kind kind, List<List<TreeNode>> cols, double[] colX, double[] colW,
				double[] busX, double bridgeY, double postGridX, double childBusX) {
			_kind = kind;
			_cols = cols;
			_colX = colX;
			_colW = colW;
			_busX = busX;
			_bridgeY = bridgeY;
			_postGridX = postGridX;
			_childBusX = childBusX;
		}

		static GridInfo columnWise(List<List<TreeNode>> cols, double[] colX, double[] colW,
				double[] busX, double bridgeY) {
			return new GridInfo(Kind.COLUMN_WISE, cols, colX, colW, busX, bridgeY, 0, 0);
		}

		static GridInfo rowWise(double[] colX, double[] colW, double mainBusX, double childBusX,
				double postGridX) {
			return new GridInfo(Kind.ROW_WISE, null, colX, colW, new double[] { mainBusX }, 0,
				postGridX, childBusX);
		}

		/** The layout kind. */
		public Kind getKind() {
			return _kind;
		}

		/**
		 * Children grouped per sub-column, in original child order. Outer list is indexed by
		 * sub-column (0..C-1).
		 *
		 * <p>Only meaningful for {@link Kind#COLUMN_WISE}; {@code null} for {@link Kind#ROW_WISE}.</p>
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
		 * Width of each sub-column.
		 */
		public double[] getColW() {
			return _colW;
		}

		/**
		 * X coordinate of the vertical bus per sub-column.
		 *
		 * <p>For {@link Kind#COLUMN_WISE}: per-column bus. The primary parent bus is at
		 * <code>busX[0]</code>; additional buses for follow-up sub-columns are at
		 * <code>busX[c]</code> for <code>c &gt;= 1</code>.</p>
		 *
		 * <p>For {@link Kind#ROW_WISE}: single-element array with the main parent→children bus.</p>
		 */
		public double[] getBusX() {
			return _busX;
		}

		/**
		 * Y coordinate of the horizontal bottom-bridge that connects the primary bus to the buses
		 * of all follow-up sub-columns.
		 *
		 * <p>Only meaningful for {@link Kind#COLUMN_WISE}.</p>
		 */
		public double getBridgeY() {
			return _bridgeY;
		}

		/**
		 * X coordinate of the post-grid column where all sub-grid children's subtrees sit.
		 *
		 * <p>Only meaningful for {@link Kind#ROW_WISE}.</p>
		 */
		public double getPostGridX() {
			return _postGridX;
		}

		/**
		 * X coordinate of the bus that sub-grid children with subtrees use for their outgoing
		 * connections.
		 *
		 * <p>Only meaningful for {@link Kind#ROW_WISE}.</p>
		 */
		public double getChildBusX() {
			return _childBusX;
		}

		/**
		 * X coordinate of the bus where the parent's stub for the given child fans out from.
		 *
		 * <p>For {@link Kind#COLUMN_WISE}: the bus of the child's sub-column.
		 * For {@link Kind#ROW_WISE}: the single main bus.</p>
		 */
		public double getBarPositionFor(TreeNode child) {
			if (_kind == Kind.ROW_WISE) {
				return _busX[0];
			}
			int c = getColIdx(child);
			return _busX[c];
		}

		/**
		 * Sub-column index of the given child within this grid, or <code>-1</code> if the node is
		 * not a child placed in this grid.
		 *
		 * <p>Only meaningful for {@link Kind#COLUMN_WISE}.</p>
		 */
		public int getColIdx(TreeNode child) {
			if (_cols == null) {
				return -1;
			}
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
			}
			for (int b = 0; b < _busX.length; b++) {
				_busX[b] += dx;
			}
			if (_kind == Kind.COLUMN_WISE) {
				_bridgeY += dy;
			} else {
				_postGridX += dx;
				_childBusX += dx;
			}
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

	private final int _childSplitThreshold;

	private final boolean _rowWise;

	private final int _subGridCols;

	private final int _subGridStartCol;

	private final double _bridgeGapY;

	private final Map<TreeNode, GridInfo> _gridInfos = new HashMap<>();

	/**
	 * Maximum width of any decoration on a connection that targets the given child node. Used to
	 * reserve enough horizontal space along the entry stub (bus → child) to draw the decoration
	 * above it.
	 */
	private final Map<TreeNode, Double> _maxDecoWidth = new HashMap<>();

	private double _width;

	private double _height;

	/**
	 * Creates a {@link TreeRenderInfo}.
	 */
	public TreeRenderInfo(boolean compact, double gapX, double siblingGapY, double subtreeGapY, double parentAlign,
			double parentOffset, int childSplitThreshold, boolean rowWise, int subGridCols, int subGridStartCol,
			double bridgeGapY, List<Box> nodes,
			List<TreeConnection> connections) {
		_compact = compact;
		_gapX = gapX;
		_siblingGapY = siblingGapY;
		_subtreeGapY = subtreeGapY;
		_parentAlign = parentAlign;
		_parentOffset = parentOffset;
		_childSplitThreshold = childSplitThreshold;
		_rowWise = rowWise;
		_subGridCols = subGridCols;
		_subGridStartCol = subGridStartCol;
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

		// Precompute per-child max decoration width across all incoming connections. The packing
		// step uses this to widen the entry-stub side of the column gap so a decoration drawn
		// above the stub does not overflow into the parent or a sibling subtree.
		_maxDecoWidth.clear();
		for (TreeConnection conn : _connections) {
			TreeNode child = _nodeForAnchor.get(conn.getChild().getAnchor());
			if (child == null) {
				continue;
			}
			double w = 0;
			for (EdgeDecoration deco : conn.getDecorations()) {
				w = Math.max(w, deco.getContent().getWidth());
			}
			if (w > 0) {
				_maxDecoWidth.merge(child, w, Math::max);
			}
		}

		// Bottom-up layout per root subtree (positions are relative to subtree origin).
		for (TreeNode root : _roots) {
			layoutSubtree(root);
		}

		// Stack root subtrees vertically into the global frame. In compact mode, the vertical
		// position of each root is determined by a per-box collision check against already-placed
		// roots (with _subtreeGapY clearance), so a shallow root can slide up alongside a deep
		// root's grandchildren subtree if their X-ranges do not conflict.
		List<double[]> placed = _compact ? new ArrayList<>() : null;
		double curY = 0;
		double maxRight = 0;
		boolean firstRoot = true;
		for (TreeNode root : _roots) {
			double minX = subtreeMinX(root);
			double dx = -minX;
			if (_compact) {
				shiftSubtree(root, dx, 0);
				List<double[]> candidate = new ArrayList<>();
				collectBoxes(root, candidate);
				double dy;
				if (firstRoot) {
					dy = -subtreeMinY(root);
					firstRoot = false;
				} else {
					dy = minCompactDy(placed, candidate, _subtreeGapY);
				}
				shiftSubtree(root, 0, dy);
				for (double[] cb : candidate) {
					placed.add(new double[] { cb[0], cb[1] + dy, cb[2], cb[3] });
				}
			} else {
				double minY = subtreeMinY(root);
				double dy;
				if (firstRoot) {
					dy = -minY;
					firstRoot = false;
				} else {
					dy = curY - minY + _subtreeGapY;
				}
				shiftSubtree(root, dx, dy);
			}
			curY = Math.max(curY, subtreeMaxY(root));
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

		if (_childSplitThreshold > 0 && children.size() > _childSplitThreshold) {
			if (_rowWise) {
				packGridRowWise(node);
			} else {
				packGridColumnWise(node);
			}
		} else {
			packLinear(node);
		}

		normalizeSubtree(node);
	}

	/**
	 * Linear packing: stack children vertically in a single column right of the parent.
	 *
	 * <p>
	 * Layout rule for the column gap: the bus sits at <code>parent.right + gapX/2</code>, the
	 * stub from bus to child has length <code>max(gapX/2, maxDecoWidth)</code> so a decoration
	 * drawn above the stub fits without overflowing.
	 * </p>
	 *
	 * <p>
	 * In {@link #_compact compact} mode the children's subtrees are first aligned on a shared
	 * depth-column raster ({@link #rasterizeSiblingColumns(List)}): boxes of the same depth
	 * occupy the same column strip, and all buses between two depths share one X coordinate.
	 * Each child subtree is then placed as a rigid unit at the topmost vertical position where
	 * none of its boxes and connection lines conflicts with the already placed siblings
	 * ({@link #topmostFitDy(List, SubtreeBoxes, double)}) — with {@link #_siblingGapY} between
	 * the two subtree roots and {@link #_subtreeGapY} for every other pair. A small subtree
	 * following a deep sibling thereby slides up into existing free space (e.g. directly below
	 * the sibling's root), while a subtree that does not fit completely into free space remains
	 * below. In non-compact mode children are stacked strictly below the full bounding box of
	 * the previous sibling, separated by {@link #_siblingGapY}.
	 * </p>
	 */
	private void packLinear(TreeNode node) {
		List<TreeNode> children = node.getChildren();
		double maxDeco = maxDecoWidth(children);
		double stub = Math.max(_gapX / 2, maxDeco);
		double childOriginX = node.getBox().getWidth() + _gapX / 2 + stub;

		if (_compact) {
			rasterizeSiblingColumns(children);

			// Per-child subtree boxes: the child's own box (root) vs. its descendants. Sibling
			// roots (direct children of {@code node}) share the same direct parent, so their
			// pairwise Y-clearance is _siblingGapY; every other pair crosses a subtree boundary
			// (root vs. other subtree's descendant, or descendant vs. descendant) and uses
			// _subtreeGapY.
			List<SubtreeBoxes> placed = new ArrayList<>();
			boolean first = true;
			for (TreeNode child : children) {
				double dx = childOriginX - subtreeMinX(child);
				shiftSubtree(child, dx, 0);
				SubtreeBoxes candidate = collectSubtree(child);
				double topDy = -subtreeMinY(child);
				double dy;
				if (first) {
					dy = topDy;
					first = false;
				} else {
					dy = topmostFitDy(placed, candidate, topDy);
				}
				shiftSubtree(child, 0, dy);
				placed.add(candidate.shifted(dy));
			}
		} else {
			// Align every child's outgoing-bus column to a single X derived from the widest
			// direct sibling. Without this each child's bus would sit greedily at its own right
			// edge plus _gapX/2 - a wider sibling's box would then poke past a narrower
			// sibling's bus column, producing a visual cross of box and bus line.
			alignSiblingBuses(children);

			double curY = 0;
			for (TreeNode child : children) {
				double dx = childOriginX - subtreeMinX(child);
				double dy = curY - subtreeMinY(child);
				shiftSubtree(child, dx, dy);
				curY = subtreeMaxY(child) + _siblingGapY;
			}
		}

		// Center parent based on parentAlign (between first and last child anchors).
		placeParent(node, children.get(0), children.get(children.size() - 1));
	}

	/**
	 * Aligns the children's subtrees on a shared depth-column raster.
	 *
	 * <p>
	 * The raster is measured over all sibling subtrees: column {@code d} is as wide as the
	 * widest box at depth {@code d} (the children form depth 0), and the bus between depths
	 * {@code d} and {@code d + 1} sits {@code gapX/2} right of column {@code d}. Every node
	 * moves to its depth's column start and every parent's outgoing bus to the depth's shared
	 * bus position ({@link TreeNode#getBusXOverride() bus-X override}). This reserves the strip
	 * width a sibling's wider box needs in a column that another sibling's subtree passes
	 * through — the precondition for placing a following sibling into free space beside a
	 * preceding sibling's subtree without crossing its bus.
	 * </p>
	 *
	 * <p>
	 * A sub-grid parent keeps its internal grid geometry: only its main bus (and with it the
	 * complete grid block) moves to the shared bus position of its depth; measurement does not
	 * descend into the grid.
	 * </p>
	 */
	private void rasterizeSiblingColumns(List<TreeNode> children) {
		List<double[]> cols = new ArrayList<>();
		for (TreeNode child : children) {
			measureColumns(child, 0, cols);
		}

		double[] colX = new double[cols.size()];
		double[] busX = new double[cols.size()];
		for (int d = 0; d < cols.size(); d++) {
			colX[d] = d == 0 ? 0 : busX[d - 1] + Math.max(_gapX / 2, cols.get(d)[1]);
			busX[d] = colX[d] + cols.get(d)[0] + _gapX / 2;
		}

		for (TreeNode child : children) {
			applyRaster(child, 0, colX, busX);
		}
	}

	/**
	 * Records the maximum box width ({@code [0]}) and incoming-decoration width ({@code [1]})
	 * per depth for {@link #rasterizeSiblingColumns(List)}. Does not descend into sub-grid
	 * parents — their interior keeps its own geometry.
	 */
	private void measureColumns(TreeNode node, int depth, List<double[]> cols) {
		while (cols.size() <= depth) {
			cols.add(new double[] { 0, 0 });
		}
		double[] col = cols.get(depth);
		col[0] = Math.max(col[0], node.getBox().getWidth());
		Double deco = _maxDecoWidth.get(node);
		if (deco != null) {
			col[1] = Math.max(col[1], deco);
		}
		if (_gridInfos.containsKey(node)) {
			return;
		}
		for (TreeNode child : node.getChildren()) {
			measureColumns(child, depth + 1, cols);
		}
	}

	/**
	 * Moves {@code node} to its depth's raster column and its outgoing bus to the depth's
	 * shared bus position; recurses into linear children. A sub-grid parent's whole grid block
	 * is shifted so that its main bus lands on the shared bus position.
	 */
	private void applyRaster(TreeNode node, int depth, double[] colX, double[] busX) {
		double dx = colX[depth] - node.getX();
		if (dx != 0) {
			shiftSubtree(node, dx, 0);
		}
		if (node.getChildren().isEmpty()) {
			return;
		}
		node.setBusXOverride(busX[depth]);
		GridInfo gi = _gridInfos.get(node);
		if (gi != null) {
			double busDelta = busX[depth] - gi.getBusX()[0];
			if (busDelta != 0) {
				for (TreeNode grand : node.getChildren()) {
					shiftSubtree(grand, busDelta, 0);
				}
				gi.shift(busDelta, 0);
			}
			return;
		}
		for (TreeNode child : node.getChildren()) {
			applyRaster(child, depth + 1, colX, busX);
		}
	}

	/**
	 * Box collection for a single (already laid out) subtree: the subtree's root box separated
	 * from its descendant boxes. Used by compact packing to distinguish sibling-root vs
	 * cross-subtree pairs when computing the minimum vertical shift.
	 */
	private static final class SubtreeBoxes {
		/** {@code [x, y, w, h]} of the subtree's root node. */
		final double[] _root;

		/** {@code [x, y, w, h]} of every descendant (children, grandchildren, ...). */
		final List<double[]> _descendants;

		SubtreeBoxes(double[] root, List<double[]> descendants) {
			_root = root;
			_descendants = descendants;
		}

		/** Returns a copy with every box shifted by {@code dy} in Y. */
		SubtreeBoxes shifted(double dy) {
			double[] root = new double[] { _root[0], _root[1] + dy, _root[2], _root[3] };
			List<double[]> desc = new ArrayList<>(_descendants.size());
			for (double[] b : _descendants) {
				desc.add(new double[] { b[0], b[1] + dy, b[2], b[3] });
			}
			return new SubtreeBoxes(root, desc);
		}
	}

	/**
	 * Recursively collects every visual obstacle of the subtree rooted at {@code node}: each
	 * node's box, plus phantom rectangles for every internal node's connection graphics
	 * ({@link #addBus(TreeNode, List)}), so that compact placement never puts a sibling box
	 * onto a bus or stub line. Each entry is {@code [x, y, w, h]} in the current coordinate
	 * frame.
	 */
	private void collectBoxes(TreeNode node, List<double[]> result) {
		Box box = node.getBox();
		result.add(new double[] { node.getX(), node.getY(), box.getWidth(), box.getHeight() });
		addBus(node, result);
		for (TreeNode child : node.getChildren()) {
			collectBoxes(child, result);
		}
	}

	/** Collects {@code node}'s subtree into a {@link SubtreeBoxes} (root + descendants). */
	private SubtreeBoxes collectSubtree(TreeNode node) {
		Box box = node.getBox();
		double[] root = new double[] { node.getX(), node.getY(), box.getWidth(), box.getHeight() };
		List<double[]> desc = new ArrayList<>();
		addBus(node, desc);
		for (TreeNode child : node.getChildren()) {
			collectBoxes(child, desc);
		}
		return new SubtreeBoxes(root, desc);
	}

	/**
	 * Non-compact counterpart of {@link #rasterizeSiblingColumns(List)}, aligning the depth-1
	 * bus column only: shifts each child's descendants to the right so that every child of the
	 * current parent has its outgoing bus at the same X — derived from the widest direct
	 * child's natural bus column ({@code maxChildWidth + _gapX/2} in the child's local frame).
	 * A child's own box
	 * stays at X = 0; only its descendant columns move. The {@link TreeNode#getBusXOverride()
	 * bus-X override} on each affected child records the new bus column so that connection
	 * drawing in {@link TreeLayoutOperations#distributeSize} uses it instead of recomputing
	 * the bus position from the child's box width. A child that is itself a sub-grid parent
	 * has its {@link GridInfo} shifted by the same amount, so that its bus and stub rendering
	 * (which takes the bar X from the {@link GridInfo}) stays aligned with the shifted
	 * descendant columns — and in particular does not cross a compacted sibling placed in the
	 * space left of the shared bus column.
	 *
	 * <p>
	 * Must be called before children are shifted into the parent's frame: the override is
	 * stored in the child's local frame and is updated by {@link #shiftSubtree} together with
	 * the rest of the subtree when the child is moved to {@code childOriginX}.
	 * </p>
	 */
	private void alignSiblingBuses(List<TreeNode> children) {
		double maxChildWidth = 0;
		for (TreeNode child : children) {
			double w = child.getBox().getWidth();
			if (w > maxChildWidth) {
				maxChildWidth = w;
			}
		}
		for (TreeNode child : children) {
			if (child.getChildren().isEmpty()) {
				continue;
			}
			double extra = maxChildWidth - child.getBox().getWidth();
			if (extra > 0) {
				for (TreeNode grand : child.getChildren()) {
					shiftSubtree(grand, extra, 0);
				}
				GridInfo gi = _gridInfos.get(child);
				if (gi != null) {
					gi.shift(extra, 0);
				}
			}
			child.setBusXOverride(maxChildWidth + _gapX / 2);
		}
	}

	/**
	 * Appends width- or height-0 phantom rectangles representing {@code node}'s outgoing
	 * connection graphics to {@code result}, if {@code node} has children. Compact packing
	 * treats these as obstacles so that no sibling box is placed onto a drawn line.
	 *
	 * <p>
	 * For a linear parent this is the vertical bus alone (at the {@link
	 * TreeNode#getBusXOverride() bus-X override} or {@code node.right + _gapX/2}, spanning the
	 * anchor mid-Ys of {@code node} and its direct children): the parent stem and the stubs
	 * into the children stay between the bus and the adjacent column strips and cannot cross
	 * sibling boxes. A sub-grid parent's stubs, in contrast, cross its sub-column strips, so
	 * {@link #addGridBus(TreeNode, GridInfo, List)} contributes the complete line set.
	 * </p>
	 */
	private void addBus(TreeNode node, List<double[]> result) {
		List<TreeNode> children = node.getChildren();
		if (children.isEmpty()) {
			return;
		}
		GridInfo gi = _gridInfos.get(node);
		if (gi != null) {
			addGridBus(node, gi, result);
			return;
		}
		double busX = node.hasBusXOverride()
			? node.getBusXOverride()
			: node.getX() + node.getBox().getWidth() + _gapX / 2;
		double topY = anchorMid(node);
		double botY = topY;
		for (TreeNode child : children) {
			double midY = anchorMid(child);
			if (midY < topY) {
				topY = midY;
			}
			if (midY > botY) {
				botY = midY;
			}
		}
		result.add(new double[] { busX, topY, 0, botY - topY });
	}

	/**
	 * Grid variant of {@link #addBus(TreeNode, List)}: the parent stem, the vertical buses, the
	 * stubs from the bus across the sub-columns into each child, the bottom bridge of a
	 * column-wise grid, and the stems and shared child bus of subtree-bearing row-wise sub-grid
	 * children. These lines cross sub-column strips, so compact packing must not place a
	 * sibling box into the seemingly free space they run through.
	 */
	private void addGridBus(TreeNode node, GridInfo gi, List<double[]> result) {
		double parentMid = anchorMid(node);
		Box parentAnchor = node.getAnchor();
		double parentRight = node.getX() + parentAnchor.getX() + parentAnchor.getWidth();

		if (gi.getKind() == GridInfo.Kind.ROW_WISE) {
			double mainBusX = gi.getBusX()[0];
			result.add(new double[] { parentRight, parentMid, mainBusX - parentRight, 0 });

			double topY = parentMid;
			double botY = parentMid;
			double childBusTop = Double.POSITIVE_INFINITY;
			double childBusBot = Double.NEGATIVE_INFINITY;
			for (TreeNode child : node.getChildren()) {
				double midY = anchorMid(child);
				topY = Math.min(topY, midY);
				botY = Math.max(botY, midY);

				double childLeft = child.getX();
				result.add(new double[] { Math.min(mainBusX, childLeft), midY,
					Math.abs(childLeft - mainBusX), 0 });

				if (!child.getChildren().isEmpty()) {
					Box childAnchor = child.getAnchor();
					double childRight = child.getX() + childAnchor.getX() + childAnchor.getWidth();
					result.add(new double[] { childRight, midY, gi.getChildBusX() - childRight, 0 });
					childBusTop = Math.min(childBusTop, midY);
					childBusBot = Math.max(childBusBot, midY);
					for (TreeNode grand : child.getChildren()) {
						double grandMid = anchorMid(grand);
						childBusTop = Math.min(childBusTop, grandMid);
						childBusBot = Math.max(childBusBot, grandMid);
					}
				}
			}
			result.add(new double[] { mainBusX, topY, 0, botY - topY });
			if (childBusTop <= childBusBot) {
				result.add(
					new double[] { gi.getChildBusX(), childBusTop, 0, childBusBot - childBusTop });
			}
		} else {
			double[] busX = gi.getBusX();
			double bridgeY = gi.getBridgeY();
			List<List<TreeNode>> cols = gi.getCols();
			result.add(new double[] { parentRight, parentMid, busX[0] - parentRight, 0 });
			for (int c = 0; c < gi.getColCount(); c++) {
				List<TreeNode> col = cols.get(c);
				double topY = c == 0 ? Math.min(parentMid, anchorMid(col.get(0))) : anchorMid(col.get(0));
				result.add(new double[] { busX[c], topY, 0, bridgeY - topY });
				for (TreeNode child : col) {
					double midY = anchorMid(child);
					double childLeft = child.getX();
					result.add(new double[] { Math.min(busX[c], childLeft), midY,
						Math.abs(childLeft - busX[c]), 0 });
				}
			}
			if (gi.getColCount() > 1) {
				result.add(new double[] { busX[0], bridgeY, busX[gi.getColCount() - 1] - busX[0], 0 });
			}
		}
	}

	/**
	 * Y coordinate of the node anchor's vertical mid-point.
	 */
	private double anchorMid(TreeNode node) {
		Box anchor = node.getAnchor();
		return node.getY() + anchor.getY() + 0.5 * anchor.getHeight();
	}

	/**
	 * Topmost vertical position (as shift to apply to {@code candidate}) at which the candidate
	 * subtree fits without conflicting with any already {@code placed} sibling subtree.
	 *
	 * <p>
	 * Tried positions are {@code topDy} (the top of the packing) and, for every pair of a
	 * placed obstacle and a candidate box that share X range, the position placing the
	 * candidate box directly below that obstacle. The first (topmost) conflict-free position
	 * wins; the position below all overlapping obstacles is always conflict-free, so the search
	 * cannot fail. Clearances match {@link #fits(List, SubtreeBoxes, double)}:
	 * {@link #_siblingGapY} between the two subtree roots (direct siblings under the same
	 * parent), {@link #_subtreeGapY} for every pair that crosses the subtree boundary
	 * (root↔descendant or descendant↔descendant).
	 * </p>
	 */
	private double topmostFitDy(List<SubtreeBoxes> placed, SubtreeBoxes candidate, double topDy) {
		TreeSet<Double> tries = new TreeSet<>();
		tries.add(topDy);
		double below = Double.NEGATIVE_INFINITY;
		for (SubtreeBoxes pst : placed) {
			below = Math.max(below, enterTry(tries, pst._root, candidate._root, _siblingGapY, topDy));
			for (double[] cd : candidate._descendants) {
				below = Math.max(below, enterTry(tries, pst._root, cd, _subtreeGapY, topDy));
			}
			for (double[] pd : pst._descendants) {
				below = Math.max(below, enterTry(tries, pd, candidate._root, _subtreeGapY, topDy));
				for (double[] cd : candidate._descendants) {
					below = Math.max(below, enterTry(tries, pd, cd, _subtreeGapY, topDy));
				}
			}
		}
		if (below == Double.NEGATIVE_INFINITY) {
			// Candidate's X extent disjoint from everything placed: no Y constraint at all -
			// keep the natural top-of-subtree position.
			return topDy;
		}
		for (double dy : tries) {
			if (fits(placed, candidate, dy)) {
				return dy;
			}
		}
		return below;
	}

	/**
	 * Registers the position that places the candidate box {@code cb} directly below the placed
	 * box {@code pb} as a try position for {@link #topmostFitDy(List, SubtreeBoxes, double)} —
	 * provided the two boxes share X range and the position is not above {@code topDy} — and
	 * returns it as this pair's contribution to the below-everything fallback.
	 */
	private double enterTry(TreeSet<Double> tries, double[] pb, double[] cb, double gapY, double topDy) {
		if (pb[0] + pb[2] <= cb[0] || cb[0] + cb[2] <= pb[0]) {
			return Double.NEGATIVE_INFINITY;
		}
		double dy = pb[1] + pb[3] + gapY - cb[1];
		if (dy >= topDy) {
			tries.add(dy);
		}
		return dy;
	}

	/**
	 * Whether the candidate, shifted by {@code dy}, conflicts with none of the placed sibling
	 * subtrees.
	 */
	private boolean fits(List<SubtreeBoxes> placed, SubtreeBoxes candidate, double dy) {
		for (SubtreeBoxes pst : placed) {
			if (conflicts(pst._root, candidate._root, dy, _siblingGapY)) {
				return false;
			}
			for (double[] cd : candidate._descendants) {
				if (conflicts(pst._root, cd, dy, _subtreeGapY)) {
					return false;
				}
			}
			for (double[] pd : pst._descendants) {
				if (conflicts(pd, candidate._root, dy, _subtreeGapY)) {
					return false;
				}
				for (double[] cd : candidate._descendants) {
					if (conflicts(pd, cd, dy, _subtreeGapY)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Whether the placed box {@code pb} and the candidate box {@code cb}, shifted by
	 * {@code dy}, share X range and violate the vertical clearance {@code gapY}.
	 */
	private boolean conflicts(double[] pb, double[] cb, double dy, double gapY) {
		if (pb[0] + pb[2] <= cb[0] || cb[0] + cb[2] <= pb[0]) {
			return false;
		}
		double top = cb[1] + dy;
		return top < pb[1] + pb[3] + gapY && top + cb[3] + gapY > pb[1];
	}

	/**
	 * Minimum vertical shift to apply to {@code candidate} (a flat list of boxes belonging to a
	 * single subtree) so that no candidate box overlaps any {@code placed} box, with
	 * {@code _gapX} horizontal and {@code gapY} vertical clearance. Used when every pair crosses
	 * a subtree boundary (e.g. root-level stacking of independent root subtrees).
	 */
	private double minCompactDy(List<double[]> placed, List<double[]> candidate, double gapY) {
		double dy = Double.NEGATIVE_INFINITY;
		for (double[] pb : placed) {
			for (double[] cb : candidate) {
				dy = Math.max(dy, yClearance(pb, cb, gapY));
			}
		}
		return dy == Double.NEGATIVE_INFINITY ? 0 : dy;
	}

	/**
	 * Y-shift the candidate {@code cb} must receive so that it clears the placed box {@code pb}
	 * by {@code gapY} vertically — provided the two boxes overlap in X. Returns
	 * {@link Double#NEGATIVE_INFINITY} when the X extents are disjoint and no Y constraint
	 * applies.
	 *
	 * <p>
	 * "Disjoint in X" means strictly non-overlapping ranges: the column structure of the tree
	 * layout already enforces a horizontal gap between subtree columns, so any X-disjoint pair
	 * is safely separated horizontally. Using a wider clearance threshold here would force
	 * unnecessary Y-stacking for slightly-mismatched box widths (e.g. a wider leaf above a
	 * narrower subtree-bearing sibling would push the subtree's grandchildren down even though
	 * they sit in the next column over).
	 * </p>
	 */
	private double yClearance(double[] pb, double[] cb, double gapY) {
		if (pb[0] + pb[2] <= cb[0] || cb[0] + cb[2] <= pb[0]) {
			return Double.NEGATIVE_INFINITY;
		}
		return pb[1] + pb[3] + gapY - cb[1];
	}

	/**
	 * Maximum decoration width across the given children's incoming connections.
	 */
	private double maxDecoWidth(List<TreeNode> children) {
		double m = 0;
		for (TreeNode ch : children) {
			Double w = _maxDecoWidth.get(ch);
			if (w != null && w > m) {
				m = w;
			}
		}
		return m;
	}

	/**
	 * Column-wise grid packing (legacy): distribute children into <code>R = childSplitThreshold</code>
	 * rows × <code>C = ⌈N/R⌉</code> sub-columns, column-major. Each sub-column packs independently
	 * in Y; a bottom-bridge is placed below the deepest sub-column connecting all sub-column buses.
	 */
	private void packGridColumnWise(TreeNode node) {
		List<TreeNode> children = node.getChildren();
		int M = children.size();
		int R = Math.min(M, _childSplitThreshold);
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

		// X positions per sub-column. Each bus sits gapX/2 to the right of the previous column's
		// right edge (or the parent's right edge for column 0); the stub from the bus into the
		// child column has length max(gapX/2, maxDecoWidth_of_column) so decorations drawn above
		// the stub fit.
		double[] colX = new double[C];
		double[] busX = new double[C];
		double prevRight = node.getBox().getWidth();
		for (int c = 0; c < C; c++) {
			double maxDeco = maxDecoWidth(cols.get(c));
			double stub = Math.max(_gapX / 2, maxDeco);
			busX[c] = prevRight + _gapX / 2;
			colX[c] = busX[c] + stub;
			prevRight = colX[c] + colW[c];
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
		_gridInfos.put(node, GridInfo.columnWise(cols, colX, colW, busX, bridgeY));

		// Center parent against first/last child of the primary sub-column.
		List<TreeNode> primary = cols.get(0);
		placeParent(node, primary.get(0), primary.get(primary.size() - 1));
	}

	/**
	 * Row-wise grid packing: only the direct children sit in the sub-grid, distributed row-major
	 * over <code>C = childSplitThreshold</code> sub-columns. The subtrees of those direct children
	 * are routed to a single post-grid column to the right of the sub-grid (so depth↔X
	 * correspondence is preserved for grandchildren and deeper). One vertical bus is routed just
	 * outside the sub-grid (at {@code childBusX = postGridX − gapX/2}) — both the parent's
	 * fan-out to the direct children and any sub-grid-child→subtree connection use this bus.
	 *
	 * <p>Sub-grid Y-positions are computed adaptively. The tightest possible step between
	 * consecutive children is {@code stepMin = h/2 + sibblingGapY} (so a bus stub passes by the
	 * next-column siblings exactly {@code sibblingGapY} away from their box edge — the minimum
	 * node-to-node distance). Three constraints can push a child further down:</p>
	 * <ol>
	 * <li>Bus-stub clearance from siblings in earlier sub-columns of the same row.</li>
	 * <li>Box clearance to the previous child in the same sub-column (across rows).</li>
	 * <li>For subtree-bearing children: non-overlap with the bus extent of any earlier
	 * subtree-bearing sibling at the shared {@code childBusX}.</li>
	 * </ol>
	 */
	private void packGridRowWise(TreeNode node) {
		List<TreeNode> children = node.getChildren();
		int M = children.size();
		// Number of sub-columns: explicit subGridCols if set, otherwise the threshold doubles as
		// the column count.
		int colCount = _subGridCols > 0 ? _subGridCols : _childSplitThreshold;
		int C = Math.min(M, colCount);
		// Sub-column where child 0 lands. Child n lands in column (n + startCol) mod C, so a
		// positive startCol leaves the first startCol cells of row 0 empty (the top-left of the
		// sub-grid). Normalized to the range [0, C-1].
		int startCol = C > 0 ? ((_subGridStartCol % C) + C) % C : 0;

		// Sub-column widths: max direct-child box width per sub-column. Direct children sit in
		// the sub-grid; their subtrees are shifted out to postGridX, so colW only depends on the
		// box width of the children placed in that column (not their subtree bbox).
		double[] colW = new double[C];
		for (int i = 0; i < M; i++) {
			int c = (i + startCol) % C;
			double w = children.get(i).getBox().getWidth();
			if (w > colW[c]) {
				colW[c] = w;
			}
		}

		// Decoration width for stubs into sub-column 0 (the closest sub-column to the parent
		// bus); subsequent sub-columns get arbitrarily wide stubs from the parent bus, plenty of
		// room for decorations.
		double maxDecoCol0 = 0;
		for (int i = 0; i < M; i++) {
			if (((i + startCol) % C) == 0) {
				Double d = _maxDecoWidth.get(children.get(i));
				if (d != null && d > maxDecoCol0) {
					maxDecoCol0 = d;
				}
			}
		}

		// X positions: parent bus, sub-columns, post-grid column, child bus.
		double busX = node.getBox().getWidth() + _gapX / 2;
		double[] colX = new double[C];
		colX[0] = busX + Math.max(_gapX / 2, maxDecoCol0);
		for (int c = 1; c < C; c++) {
			colX[c] = colX[c - 1] + colW[c - 1] + _gapX;
		}
		double postGridX = colX[C - 1] + colW[C - 1] + _gapX;
		double childBusX = postGridX - _gapX / 2;

		// Adaptive Y stack with per-column bottom, per-column "max past stub Y crossing this col"
		// (from past children whose own col is > this col), and bus-bottom tracking. curYPost is
		// the post-grid contour: the topmost Y where the next descendant block may start without
		// colliding with an earlier block (or its bus at childBusX). It is a lower bound only —
		// a block's preferred position is the one its own subtree packing produced.
		double[] prevColBottom = new double[C];
		Arrays.fill(prevColBottom, Double.NEGATIVE_INFINITY);
		double[] prevStubsCrossingCol = new double[C];
		Arrays.fill(prevStubsCrossingCol, Double.NEGATIVE_INFINITY);
		double prevBusBottom = Double.NEGATIVE_INFINITY;
		double curYPost = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < M; i++) {
			TreeNode ch = children.get(i);
			int c = (i + startCol) % C;
			Box chBox = ch.getBox();
			Box chAnchor = ch.getAnchor();
			double h = chBox.getHeight();
			// Offset of the anchor mid-Y within the box (where the bus stub actually meets the
			// child). For nodes with anchor == box this equals h/2; for nodes with the anchor
			// only covering part of the box (e.g. a label rendered above a smaller "anchor"
			// content area) this is the y of the anchor's vertical centre relative to the box's
			// top edge.
			double anchorMid = chAnchor.getY() + 0.5 * chAnchor.getHeight();
			boolean hasSubtree = !ch.getChildren().isEmpty();

			double yi = 0;
			// (1) Bus-stub clearance: stub.midY = yi + anchorMid must clear (by sibblingGapY) the
			// boxes of all previously placed children in earlier sub-columns of the same row —
			// my stub passes through their column at this Y, so it must sit below their box
			// bottom plus sibblingGapY.
			for (int cprev = 0; cprev < c; cprev++) {
				double bound = prevColBottom[cprev] + _siblingGapY - anchorMid;
				if (bound > yi) {
					yi = bound;
				}
			}
			// (2) Same sub-column: box clearance to previous child in this column.
			if (prevColBottom[c] > Double.NEGATIVE_INFINITY) {
				double bound = prevColBottom[c] + _siblingGapY;
				if (bound > yi) {
					yi = bound;
				}
			}
			// (3) Bus non-overlap for subtree-bearing children.
			if (hasSubtree && prevBusBottom > Double.NEGATIVE_INFINITY) {
				double bound = prevBusBottom + _siblingGapY - anchorMid;
				if (bound > yi) {
					yi = bound;
				}
			}
			// (4) Past-stub clearance for my BOX: any stub from a previously placed child in a
			// later sub-column (c_past > c) crosses my column at that stub's Y; my box must not
			// contain such a stub Y (with sibblingGapY clearance). prevStubsCrossingCol[c] tracks
			// the maximum past stub Y that crosses my column.
			if (prevStubsCrossingCol[c] > Double.NEGATIVE_INFINITY) {
				double bound = prevStubsCrossingCol[c] + _siblingGapY;
				if (bound > yi) {
					yi = bound;
				}
			}

			// 1. Place ch's whole subtree at the sub-grid slot (colX[c], yi).
			double dxAll = colX[c] - ch.getX();
			double dyAll = yi - ch.getY();
			shiftSubtree(ch, dxAll, dyAll);

			if (hasSubtree) {
				// 2. Shift only the descendants to the post-grid: shift the entire ch-subtree
				// (so ch's _gridInfo, if any, follows) and unshift ch alone, leaving ch in the
				// sub-grid slot but its children in the post-grid column. The X-shift anchors
				// on the descendant block's minimum extent — NOT on the first child: with a
				// nested sub-grid whose subGridStartCol places the first child in a follow-up
				// column, the first child is not the leftmost descendant, and anchoring on it
				// would drag the nested left column back into this sub-grid and the nested main
				// bus left of ch's own box. Anchoring the block extent keeps the leftmost
				// nested column at postGridX and — by construction — the nested main bus at
				// childBusX.
				double grandMinX = Double.POSITIVE_INFINITY;
				double grandMinY = Double.POSITIVE_INFINITY;
				double grandMinAnchorMid = Double.POSITIVE_INFINITY;
				for (TreeNode grand : ch.getChildren()) {
					grandMinX = Math.min(grandMinX, subtreeMinX(grand));
					grandMinY = Math.min(grandMinY, subtreeMinY(grand));
					Box grandAnchor = grand.getAnchor();
					grandMinAnchorMid = Math.min(grandMinAnchorMid,
						grand.getY() + grandAnchor.getY() + 0.5 * grandAnchor.getHeight());
				}

				// (3b) Bus non-overlap at childBusX, second half: my bus.top is
				// min(ch.anchorMidY, topmost grandchild anchorMidY). Constraint (3) above only
				// handles the ch.anchorMidY half (by pushing yi up). For the grandchild half we
				// push curYPost up so that the topmost grandchild's anchor midY lands
				// sibblingGapY below the deepest past bus bottom. Without this, a parent with a
				// shallow subtree (few/short post-grid descendants) following a parent with a
				// deep anchor (long label above the anchor → big anchorMidY) would have its
				// bus.top at the grandchild side overlap with the past bus at childBusX.
				if (prevBusBottom > Double.NEGATIVE_INFINITY) {
					double curYPostMin = prevBusBottom + _siblingGapY - (grandMinAnchorMid - grandMinY);
					if (curYPost < curYPostMin) {
						curYPost = curYPostMin;
					}
				}

				double dxDesc = postGridX - grandMinX;
				// Y: the subtree packing already placed ch relative to its children according to
				// parentAlign/parentOffset (placeParent), and the slot shift above moved that
				// geometry rigidly — so the preferred Y-shift is zero. Push the block down to
				// the post-grid contour only when the preferred position would collide with an
				// earlier descendant block or its bus at childBusX.
				double dyDesc = Math.max(0, curYPost - grandMinY);
				shiftSubtree(ch, dxDesc, dyDesc);
				ch.setX(ch.getX() - dxDesc);
				ch.setY(ch.getY() - dyDesc);
				// ch's outgoing-bus X: just outside the sub-grid, regardless of where ch's
				// natural bus would land. Used by drawLinearConn / barPosition for ch's outgoing
				// connections; if ch itself is high-fan-out and has its own GridInfo the
				// shiftSubtree above already moved its primary bus to the same childBusX.
				ch.setBusXOverride(childBusX);

				// Track this parent's bus bottom for constraint (3) on subsequent children. The
				// bus extends from min(ch.anchor.midY, all grandchild anchor midYs) to max(...);
				// only max matters here.
				double busBot = yi + anchorMid;
				for (TreeNode grand : ch.getChildren()) {
					Box grandAnchor = grand.getAnchor();
					double mid = grand.getY() + grandAnchor.getY() + 0.5 * grandAnchor.getHeight();
					if (mid > busBot) {
						busBot = mid;
					}
				}
				if (busBot > prevBusBottom) {
					prevBusBottom = busBot;
				}

				// Advance post-grid contour: deepest descendant bottom + sibblingGapY.
				double grandBottom = Double.NEGATIVE_INFINITY;
				for (TreeNode grand : ch.getChildren()) {
					double b = subtreeMaxY(grand);
					if (b > grandBottom) {
						grandBottom = b;
					}
				}
				curYPost = grandBottom + _siblingGapY;
			}

			prevColBottom[c] = yi + h;

			// Update the per-column "max past stub Y crossing this col" with my own stub:
			// (a) The main-bus → me stub crosses cols 0..c-1 at y = yi + anchorMid.
			// (b) If me has a subtree, the long horizontal stem from me's anchor right edge to
			//     childBusX crosses cols c+1..C-1 at the same Y (= my anchor mid). Without (b)
			//     the box of a later sub-grid child in a higher col could contain this stem at
			//     its label-Y range — particularly when that later child has a tall box (label
			//     above a smaller anchor), so its box.top is well above its anchor.
			double myStubY = yi + anchorMid;
			for (int cprev = 0; cprev < c; cprev++) {
				if (myStubY > prevStubsCrossingCol[cprev]) {
					prevStubsCrossingCol[cprev] = myStubY;
				}
			}
			if (hasSubtree) {
				for (int cnext = c + 1; cnext < C; cnext++) {
					if (myStubY > prevStubsCrossingCol[cnext]) {
						prevStubsCrossingCol[cnext] = myStubY;
					}
				}
			}
		}

		_gridInfos.put(node, GridInfo.rowWise(colX, colW, busX, childBusX, postGridX));

		// Center parent against the first / last sub-grid child.
		placeParent(node, children.get(0), children.get(M - 1));
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
		if (node.hasBusXOverride()) {
			node.setBusXOverride(node.getBusXOverride() + dx);
		}
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
		GridInfo gi = _gridInfos.get(node);
		if (gi != null && gi.getKind() == GridInfo.Kind.ROW_WISE) {
			m = Math.max(m, gi.getPostGridX());
		}
		for (TreeNode c : node.getChildren()) {
			m = Math.max(m, subtreeMaxX(c));
		}
		return m;
	}

	private double subtreeMaxY(TreeNode node) {
		double m = node.getY() + node.getBox().getHeight();
		GridInfo gi = _gridInfos.get(node);
		if (gi != null && gi.getKind() == GridInfo.Kind.COLUMN_WISE) {
			m = Math.max(m, gi.getBridgeY());
		}
		for (TreeNode c : node.getChildren()) {
			m = Math.max(m, subtreeMaxY(c));
		}
		return m;
	}

}
