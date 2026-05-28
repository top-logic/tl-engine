package com.top_logic.graphic.flow.data;

/**
 * A layout that connects nodes with one-to-many connectors forming a tree.
 */
public interface TreeLayout extends com.top_logic.graphic.flow.data.FloatingLayout, com.top_logic.graphic.flow.operations.tree.TreeLayoutOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.TreeLayout} instance.
	 */
	static com.top_logic.graphic.flow.data.TreeLayout create() {
		return new com.top_logic.graphic.flow.data.impl.TreeLayout_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.TreeLayout} type in JSON format. */
	String TREE_LAYOUT__TYPE = "TreeLayout";

	/** @see #isCompact() */
	String COMPACT__PROP = "compact";

	/** @see #getParentAlign() */
	String PARENT_ALIGN__PROP = "parentAlign";

	/** @see #getParentOffset() */
	String PARENT_OFFSET__PROP = "parentOffset";

	/** @see #getDirection() */
	String DIRECTION__PROP = "direction";

	/** @see #getGapX() */
	String GAP_X__PROP = "gapX";

	/** @see #getSibblingGapY() */
	String SIBBLING_GAP_Y__PROP = "sibblingGapY";

	/** @see #getSubtreeGapY() */
	String SUBTREE_GAP_Y__PROP = "subtreeGapY";

	/** @see #getChildSplitThreshold() */
	String CHILD_SPLIT_THRESHOLD__PROP = "childSplitThreshold";

	/** @see #getSubGridCols() */
	String SUB_GRID_COLS__PROP = "subGridCols";

	/** @see #getSubGridStartCol() */
	String SUB_GRID_START_COL__PROP = "subGridStartCol";

	/** @see #isRowWise() */
	String ROW_WISE__PROP = "rowWise";

	/** @see #getBridgeGapY() */
	String BRIDGE_GAP_Y__PROP = "bridgeGapY";

	/** @see #getStrokeStyle() */
	String STROKE_STYLE__PROP = "strokeStyle";

	/** @see #getThickness() */
	String THICKNESS__PROP = "thickness";

	/** @see #getConnections() */
	String CONNECTIONS__PROP = "connections";

	/**
	 * Whether to minimize vertical space required to layout the tree.
	 */
	boolean isCompact();

	/**
	 * @see #isCompact()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setCompact(boolean value);

	/**
	 * Factor to determine the placement of the parent node relative to it's children. 
	 * With a value of zero the center of the parent node is placed at the same Y coordinate 
	 * as the first of it's children. With a value of 1.0, the parent is aligned to its last child. 
	 * A value in between, places the parent corresponding to the ratio between the first 
	 * and the last child.
	 */
	double getParentAlign();

	/**
	 * @see #getParentAlign()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setParentAlign(double value);

	/**
	 * Offset to add to the parent Y coordinate after the alignment operation based on {@link #parentRatio}.
	 */
	double getParentOffset();

	/**
	 * @see #getParentOffset()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setParentOffset(double value);

	/**
	 * The layout direction of nodes (from top/parent to bottom/children)
	 */
	com.top_logic.graphic.flow.data.DiagramDirection getDirection();

	/**
	 * @see #getDirection()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setDirection(com.top_logic.graphic.flow.data.DiagramDirection value);

	/**
	 * Horizontal gap between columns of tree nodes.
	 */
	double getGapX();

	/**
	 * @see #getGapX()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setGapX(double value);

	/**
	 * Vertical gap between nodes in a tree column if they belong to the same parent.
	 */
	double getSibblingGapY();

	/**
	 * @see #getSibblingGapY()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setSibblingGapY(double value);

	/**
	 * Vertical gap between nodes in a tree column if they belong to different subtrees.
	 */
	double getSubtreeGapY();

	/**
	 * @see #getSubtreeGapY()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setSubtreeGapY(double value);

	/**
	 * Threshold above which a parent's children are split into a 2D sub-grid instead of a single
	 * vertical column.
	 *
	 * <p>If the number of children of some parent node exceeds this value, the children are
	 * arranged in a sub-grid. The exact layout depends on {@link #isRowWise()}:</p>
	 * <ul>
	 * <li>Column-wise (default): At most {@code childSplitThreshold} children per sub-column,
	 * fanning out into <code>C = ⌈M / childSplitThreshold⌉</code> sub-columns. Each sub-column
	 * has its own vertical bus; follow-up buses are connected to the primary bus via a horizontal
	 * bottom-bridge.</li>
	 * <li>Row-wise (when {@link #isRowWise()}): Children are placed row-major across
	 * <code>subGridCols</code> sub-columns (or <code>childSplitThreshold</code> if
	 * <code>subGridCols</code> is not set). The sub-grid contains only the direct children,
	 * subtrees of those direct children are rendered to the right of the sub-grid, and a single
	 * bus connects all sub-grid children.</li>
	 * </ul>
	 *
	 * <p>A value of {@code 0} disables sub-grid mode (legacy behavior, single column per parent).</p>
	 */
	int getChildSplitThreshold();

	/**
	 * @see #getChildSplitThreshold()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setChildSplitThreshold(int value);

	/**
	 * Number of sub-columns of the row-wise sub-grid (only relevant if {@link #isRowWise()}).
	 *
	 * <p>If set to a positive value, the row-wise algorithm distributes children across exactly
	 * this many sub-columns; the threshold for triggering sub-grid mode is still
	 * {@link #getChildSplitThreshold()}. If set to {@code 0}, the row-wise algorithm uses
	 * {@link #getChildSplitThreshold()} as the sub-column count (default behavior).</p>
	 *
	 * <p>Ignored in column-wise mode (the column count there is derived from
	 * <code>⌈M / childSplitThreshold⌉</code>).</p>
	 */
	int getSubGridCols();

	/**
	 * @see #getSubGridCols()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setSubGridCols(int value);

	/**
	 * Sub-column the first sub-grid child (child index 0) lands in (only relevant if
		 * {@link #isRowWise()}).
	 *
	 * <p>The row-wise algorithm normally places child <code>n</code> in sub-column
	 * <code>n mod C</code>. With {@code subGridStartCol = k} the placement becomes
	 * <code>(n + k) mod C</code>, so child 0 starts in sub-column <code>k</code>, child
	 * <code>C − k</code> wraps back to sub-column 0, and so on. Useful e.g. to leave the
	 * top-left sub-grid cells empty for visual balance with the parent node.</p>
	 *
	 * <p>Must be in the range <code>0 .. C − 1</code>; values outside that range are taken
	 * modulo <code>C</code>. Defaults to {@code 0}. Ignored in column-wise mode.</p>
	 */
	int getSubGridStartCol();

	/**
	 * @see #getSubGridStartCol()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setSubGridStartCol(int value);

	/**
	 * Whether to use the row-wise sub-grid algorithm instead of the column-wise one.
	 *
	 * <p>The row-wise algorithm places only the direct children inside the sub-grid (in
	 * <code>childSplitThreshold</code> sub-columns, row-major) and renders all sub-grid children's
	 * subtrees in a single column to the right of the sub-grid (so depth↔X correspondence is kept
	 * for descendants, regardless of which sub-column the direct child sits in). A single
	 * vertical bus is routed just outside the sub-grid for both the parent→direct-children and
	 * any sub-grid-child→subtree connections.</p>
	 *
	 * <p>Sub-grid Y-positions are adaptively packed: a child's row is the tightest position
	 * subject to (a) box clearance to the previous child in the same sub-column, (b) bus-stub
	 * clearance to siblings in earlier sub-columns, (c) — for sub-grid children with subtrees —
	 * non-overlap with the bus extent of any earlier subtree-bearing sibling.</p>
	 */
	boolean isRowWise();

	/**
	 * @see #isRowWise()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setRowWise(boolean value);

	/**
	 * Vertical gap between the bottom of the deepest sub-grid column and the bottom-bridge that
	 * connects all sub-grid columns. Only relevant in column-wise sub-grid mode (i.e. when
		 * {@link #getChildSplitThreshold()} triggers sub-grid mode and {@link #isRowWise()} is
		 * {@code false}).
	 */
	double getBridgeGapY();

	/**
	 * @see #getBridgeGapY()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setBridgeGapY(double value);

	/**
	 * Stroke style of tree connections.
	 */
	String getStrokeStyle();

	/**
	 * @see #getStrokeStyle()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setStrokeStyle(String value);

	/**
	 * Stroke width of tree connections.
	 */
	double getThickness();

	/**
	 * @see #getThickness()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setThickness(double value);

	/**
	 * The connections connecting the {@link #getNodes()} of the tree.
	 */
	java.util.List<com.top_logic.graphic.flow.data.TreeConnection> getConnections();

	/**
	 * @see #getConnections()
	 */
	com.top_logic.graphic.flow.data.TreeLayout setConnections(java.util.List<? extends com.top_logic.graphic.flow.data.TreeConnection> value);

	/**
	 * Adds a value to the {@link #getConnections()} list.
	 */
	com.top_logic.graphic.flow.data.TreeLayout addConnection(com.top_logic.graphic.flow.data.TreeConnection value);

	/**
	 * Removes a value from the {@link #getConnections()} list.
	 */
	void removeConnection(com.top_logic.graphic.flow.data.TreeConnection value);

	@Override
	com.top_logic.graphic.flow.data.TreeLayout setNodes(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value);

	@Override
	com.top_logic.graphic.flow.data.TreeLayout addNode(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.TreeLayout setX(double value);

	@Override
	com.top_logic.graphic.flow.data.TreeLayout setY(double value);

	@Override
	com.top_logic.graphic.flow.data.TreeLayout setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.TreeLayout setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.TreeLayout setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.TreeLayout setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.TreeLayout setClientId(String value);

	@Override
	com.top_logic.graphic.flow.data.TreeLayout setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.TreeLayout readTreeLayout(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.TreeLayout) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert TREE_LAYOUT__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.TreeLayout_Impl result = new com.top_logic.graphic.flow.data.impl.TreeLayout_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default TreeLayout self() {
		return this;
	}

}
