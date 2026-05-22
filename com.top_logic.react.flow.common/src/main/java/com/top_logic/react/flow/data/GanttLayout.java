package com.top_logic.react.flow.data;

/**
 * A layout placing {@link GanttItem} boxes along a time axis, grouped into rows.
 *
 * <p>
 * Row heights are computed per-row from the maximum item intrinsic height plus padding, with
 * {@link #rowMinContentHeight} as a floor. The effective total height of a row is:
 * {@code max(rowMinContentHeight, max(item.intrinsicHeight in row)) + 2 * rowPadding}.
 * All items in the same row receive the same final content height so that neighbouring boxes
 * can grow together.
 * </p>
 *
 * <p>
 * Inherits {@link Layout#getContents()} — the layout populates it with the
 * {@link GanttItem#getBox()} elements so that standard layout/render dispatch reaches them.
 * </p>
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

	/** @see #getIndentWidth() */
	String INDENT_WIDTH__PROP = "indentWidth";

	/** @see #getRowLabelMinWidth() */
	String ROW_LABEL_MIN_WIDTH__PROP = "rowLabelMinWidth";

	/** @see #getRowLabelPadding() */
	String ROW_LABEL_PADDING__PROP = "rowLabelPadding";

	/** @see #getColumnWidth() */
	String COLUMN_WIDTH__PROP = "columnWidth";

	/** @see #getScrollX() */
	String SCROLL_X__PROP = "scrollX";

	/** @see #getScrollY() */
	String SCROLL_Y__PROP = "scrollY";

	/** @see #getFrozenRows() */
	String FROZEN_ROWS__PROP = "frozenRows";

	/** @see #getHeaderHeight() */
	String HEADER_HEIGHT__PROP = "headerHeight";

	/** @see #getDataHeight() */
	String DATA_HEIGHT__PROP = "dataHeight";

	/** @see #getPanStartX() */
	String PAN_START_X__PROP = "panStartX";

	/** @see #getPanStartY() */
	String PAN_START_Y__PROP = "panStartY";

	/** @see #getPanStartScrollX() */
	String PAN_START_SCROLL_X__PROP = "panStartScrollX";

	/** @see #getPanStartScrollY() */
	String PAN_START_SCROLL_Y__PROP = "panStartScrollY";

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
	 * Horizontal indentation per row hierarchy level, in pixels.
	 */
	double getIndentWidth();

	/**
	 * @see #getIndentWidth()
	 */
	com.top_logic.react.flow.data.GanttLayout setIndentWidth(double value);

	/**
	 * Minimum width of the row label column, in pixels.
	 *
	 * <p>
	 * The effective column width is
	 * {@code max(rowLabelMinWidth, maxLabelIntrinsicWidth + 2 * rowLabelPadding)} so the column
	 * grows automatically to fit the widest label across all rows.
	 * </p>
	 */
	double getRowLabelMinWidth();

	/**
	 * @see #getRowLabelMinWidth()
	 */
	com.top_logic.react.flow.data.GanttLayout setRowLabelMinWidth(double value);

	/**
	 * Horizontal inset (left and right each) between the label column border and each label box,
	 * in pixels.
	 *
	 * <p>
	 * The effective content width offered to each label box is
	 * {@code columnWidth - 2 * rowLabelPadding - depth * indentWidth}.
	 * </p>
	 */
	double getRowLabelPadding();

	/**
	 * @see #getRowLabelPadding()
	 */
	com.top_logic.react.flow.data.GanttLayout setRowLabelPadding(double value);

	/**
	 * The effective label column width as computed by the layout algorithm, in pixels.
	 *
	 * <p>
	 * This is the result of {@code max(rowLabelMinWidth, maxLabelIntrinsic + 2 * rowLabelPadding)}
	 * and is stored here so that the drawing pass can use it without recomputation.
	 * </p>
	 */
	double getColumnWidth();

	/**
	 * @see #getColumnWidth()
	 */
	com.top_logic.react.flow.data.GanttLayout setColumnWidth(double value);

	/**
	 * Horizontal scroll offset, in position units (not pixels).
	 *
	 * <p>
	 * The pixel offset applied to the content and header groups is {@code scrollX * currentZoom}.
	 * Using position units ensures that the viewport stays centered on the same time point
	 * when zoom changes.
	 * </p>
	 */
	double getScrollX();

	/**
	 * @see #getScrollX()
	 */
	com.top_logic.react.flow.data.GanttLayout setScrollX(double value);

	/**
	 * Vertical scroll offset, in pixels.
	 *
	 * <p>
	 * Applied to the content and sidebar groups. Not affected by zoom changes (row heights
	 * do not change when zooming).
	 * </p>
	 */
	double getScrollY();

	/**
	 * @see #getScrollY()
	 */
	com.top_logic.react.flow.data.GanttLayout setScrollY(double value);

	/**
	 * Number of leading top-level rows in {@link #getRootRows()} that form the frozen header.
	 *
	 * <p>
	 * If {@code frozenRows = 3}, the first three root rows (e.g. Year, Month, Markers) stay
	 * visible at the top and do not scroll vertically. Their labels form the frozen corner.
	 * {@code 0} means no frozen header.
	 * </p>
	 */
	int getFrozenRows();

	/**
	 * @see #getFrozenRows()
	 */
	com.top_logic.react.flow.data.GanttLayout setFrozenRows(int value);

	/**
	 * Height of the frozen header area, in pixels. Computed during layout from the
	 * accumulated heights of the first {@link #getFrozenRows()} root rows and their descendants.
	 */
	double getHeaderHeight();

	/**
	 * @see #getHeaderHeight()
	 */
	com.top_logic.react.flow.data.GanttLayout setHeaderHeight(double value);

	/**
	 * Total height of the scrollable data rows, in pixels. Computed during layout.
	 * Used for scroll bounds: {@code maxScrollY = max(0, dataHeight - viewportContentHeight)}.
	 */
	double getDataHeight();

	/**
	 * @see #getDataHeight()
	 */
	com.top_logic.react.flow.data.GanttLayout setDataHeight(double value);

	/**
	 * Pan start X coordinate in SVG space (set on pointer down).
	 */
	double getPanStartX();

	/**
	 * @see #getPanStartX()
	 */
	com.top_logic.react.flow.data.GanttLayout setPanStartX(double value);

	/**
	 * Pan start Y coordinate in SVG space (set on pointer down).
	 */
	double getPanStartY();

	/**
	 * @see #getPanStartY()
	 */
	com.top_logic.react.flow.data.GanttLayout setPanStartY(double value);

	/**
	 * The {@link #getScrollX()} value at the start of a pan gesture.
	 */
	double getPanStartScrollX();

	/**
	 * @see #getPanStartScrollX()
	 */
	com.top_logic.react.flow.data.GanttLayout setPanStartScrollX(double value);

	/**
	 * The {@link #getScrollY()} value at the start of a pan gesture.
	 */
	double getPanStartScrollY();

	/**
	 * @see #getPanStartScrollY()
	 */
	com.top_logic.react.flow.data.GanttLayout setPanStartScrollY(double value);

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
