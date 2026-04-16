package com.top_logic.react.flow.data;

/**
 * A layout placing {@link GanttItem} boxes along a time axis, grouped into rows.
 *
 * Inherits {@code contents} from {@link Layout} — the layout populates it with the
 * {@link GanttItem#getBox()} elements so that standard layout/render dispatch reaches them.
 */
public interface GanttLayout extends Layout, com.top_logic.react.flow.operations.layout.GanttLayoutOperations {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.GanttLayout} instance.
	 */
	static com.top_logic.react.flow.data.GanttLayout create() {
		return new com.top_logic.react.flow.data.impl.GanttLayout_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.GanttLayout} type in JSON format. */
	String GANTT_LAYOUT__TYPE = "GanttLayout";

	/** @see #getRootRows() */
	String ROOT_ROWS__PROP = "rootRows";

	/** @see #getItems() */
	String ITEMS__PROP = "items";

	/** @see #getEdges() */
	String EDGES__PROP = "edges";

	/** @see #getDecorations() */
	String DECORATIONS__PROP = "decorations";

	/** @see #getAxis() */
	String AXIS__PROP = "axis";

	/** @see #getRowHeight() */
	String ROW_HEIGHT__PROP = "rowHeight";

	/** @see #getIndentWidth() */
	String INDENT_WIDTH__PROP = "indentWidth";

	/** @see #getAxisHeight() */
	String AXIS_HEIGHT__PROP = "axisHeight";

	/** @see #getRowLabelWidth() */
	String ROW_LABEL_WIDTH__PROP = "rowLabelWidth";

	/**
	 * Root rows of the forest.
	 */
	java.util.List<com.top_logic.react.flow.data.GanttRow> getRootRows();

	/**
	 * @see #getRootRows()
	 */
	com.top_logic.react.flow.data.GanttLayout setRootRows(java.util.List<? extends com.top_logic.react.flow.data.GanttRow> value);

	/**
	 * Adds a value to the {@link #getRootRows()} list.
	 */
	com.top_logic.react.flow.data.GanttLayout addRootRow(com.top_logic.react.flow.data.GanttRow value);

	/**
	 * Removes a value from the {@link #getRootRows()} list.
	 */
	void removeRootRow(com.top_logic.react.flow.data.GanttRow value);

	/**
	 * All items in the chart (referenced by row via {@link GanttItem#getRowId()}).
	 */
	java.util.List<com.top_logic.react.flow.data.GanttItem> getItems();

	/**
	 * @see #getItems()
	 */
	com.top_logic.react.flow.data.GanttLayout setItems(java.util.List<? extends com.top_logic.react.flow.data.GanttItem> value);

	/**
	 * Adds a value to the {@link #getItems()} list.
	 */
	com.top_logic.react.flow.data.GanttLayout addItem(com.top_logic.react.flow.data.GanttItem value);

	/**
	 * Removes a value from the {@link #getItems()} list.
	 */
	void removeItem(com.top_logic.react.flow.data.GanttItem value);

	/**
	 * Dependency edges between items.
	 */
	java.util.List<com.top_logic.react.flow.data.GanttEdge> getEdges();

	/**
	 * @see #getEdges()
	 */
	com.top_logic.react.flow.data.GanttLayout setEdges(java.util.List<? extends com.top_logic.react.flow.data.GanttEdge> value);

	/**
	 * Adds a value to the {@link #getEdges()} list.
	 */
	com.top_logic.react.flow.data.GanttLayout addEdge(com.top_logic.react.flow.data.GanttEdge value);

	/**
	 * Removes a value from the {@link #getEdges()} list.
	 */
	void removeEdge(com.top_logic.react.flow.data.GanttEdge value);

	/**
	 * Decorations overlaid on the chart (lines, ranges).
	 */
	java.util.List<com.top_logic.react.flow.data.GanttDecoration> getDecorations();

	/**
	 * @see #getDecorations()
	 */
	com.top_logic.react.flow.data.GanttLayout setDecorations(java.util.List<? extends com.top_logic.react.flow.data.GanttDecoration> value);

	/**
	 * Adds a value to the {@link #getDecorations()} list.
	 */
	com.top_logic.react.flow.data.GanttLayout addDecoration(com.top_logic.react.flow.data.GanttDecoration value);

	/**
	 * Removes a value from the {@link #getDecorations()} list.
	 */
	void removeDecoration(com.top_logic.react.flow.data.GanttDecoration value);

	/**
	 * Time axis configuration.
	 */
	com.top_logic.react.flow.data.GanttAxis getAxis();

	/**
	 * @see #getAxis()
	 */
	com.top_logic.react.flow.data.GanttLayout setAxis(com.top_logic.react.flow.data.GanttAxis value);

	/**
	 * Checks, whether {@link #getAxis()} has a value.
	 */
	boolean hasAxis();

	/**
	 * Height allocated per row, in pixels at zoom 100%.
	 */
	double getRowHeight();

	/**
	 * @see #getRowHeight()
	 */
	com.top_logic.react.flow.data.GanttLayout setRowHeight(double value);

	/**
	 * Horizontal indentation per row hierarchy level, in pixels.
	 */
	double getIndentWidth();

	/**
	 * @see #getIndentWidth()
	 */
	com.top_logic.react.flow.data.GanttLayout setIndentWidth(double value);

	/**
	 * Height of the axis header, in pixels.
	 */
	double getAxisHeight();

	/**
	 * @see #getAxisHeight()
	 */
	com.top_logic.react.flow.data.GanttLayout setAxisHeight(double value);

	/**
	 * Width of the row label column, in pixels.
	 */
	double getRowLabelWidth();

	/**
	 * @see #getRowLabelWidth()
	 */
	com.top_logic.react.flow.data.GanttLayout setRowLabelWidth(double value);

	@Override
	com.top_logic.react.flow.data.GanttLayout setContents(java.util.List<? extends com.top_logic.react.flow.data.Box> value);

	@Override
	com.top_logic.react.flow.data.GanttLayout addContent(com.top_logic.react.flow.data.Box value);

	@Override
	com.top_logic.react.flow.data.GanttLayout setX(double value);

	@Override
	com.top_logic.react.flow.data.GanttLayout setY(double value);

	@Override
	com.top_logic.react.flow.data.GanttLayout setWidth(double value);

	@Override
	com.top_logic.react.flow.data.GanttLayout setHeight(double value);

	@Override
	com.top_logic.react.flow.data.GanttLayout setCssClass(String value);

	@Override
	com.top_logic.react.flow.data.GanttLayout setUserObject(java.lang.Object value);

	@Override
	com.top_logic.react.flow.data.GanttLayout setClientId(String value);

	@Override
	com.top_logic.react.flow.data.GanttLayout setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.GanttLayout readGanttLayout(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.GanttLayout) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert GANTT_LAYOUT__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.GanttLayout_Impl result = new com.top_logic.react.flow.data.impl.GanttLayout_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default GanttLayout self() {
		return this;
	}

}
