package com.top_logic.graphic.flow.data;

public interface GridLayout extends Layout, com.top_logic.graphic.flow.operations.layout.GridLayoutOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.GridLayout} instance.
	 */
	static com.top_logic.graphic.flow.data.GridLayout create() {
		return new com.top_logic.graphic.flow.data.impl.GridLayout_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.GridLayout} type in JSON format. */
	String GRID_LAYOUT__TYPE = "GridLayout";

	/** @see #getRows() */
	String ROWS__PROP = "rows";

	/** @see #getCols() */
	String COLS__PROP = "cols";

	/** @see #getGapX() */
	String GAP_X__PROP = "gapX";

	/** @see #getGapY() */
	String GAP_Y__PROP = "gapY";

	/** @see #getRowHeight() */
	String ROW_HEIGHT__PROP = "rowHeight";

	/** @see #getColWidth() */
	String COL_WIDTH__PROP = "colWidth";

	int getRows();

	/**
	 * @see #getRows()
	 */
	com.top_logic.graphic.flow.data.GridLayout setRows(int value);

	int getCols();

	/**
	 * @see #getCols()
	 */
	com.top_logic.graphic.flow.data.GridLayout setCols(int value);

	double getGapX();

	/**
	 * @see #getGapX()
	 */
	com.top_logic.graphic.flow.data.GridLayout setGapX(double value);

	double getGapY();

	/**
	 * @see #getGapY()
	 */
	com.top_logic.graphic.flow.data.GridLayout setGapY(double value);

	java.util.List<Double> getRowHeight();

	/**
	 * @see #getRowHeight()
	 */
	com.top_logic.graphic.flow.data.GridLayout setRowHeight(java.util.List<? extends Double> value);

	/**
	 * Adds a value to the {@link #getRowHeight()} list.
	 */
	com.top_logic.graphic.flow.data.GridLayout addRowHeight(double value);

	/**
	 * Removes a value from the {@link #getRowHeight()} list.
	 */
	void removeRowHeight(double value);

	java.util.List<Double> getColWidth();

	/**
	 * @see #getColWidth()
	 */
	com.top_logic.graphic.flow.data.GridLayout setColWidth(java.util.List<? extends Double> value);

	/**
	 * Adds a value to the {@link #getColWidth()} list.
	 */
	com.top_logic.graphic.flow.data.GridLayout addColWidth(double value);

	/**
	 * Removes a value from the {@link #getColWidth()} list.
	 */
	void removeColWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.GridLayout setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value);

	@Override
	com.top_logic.graphic.flow.data.GridLayout addContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.GridLayout setX(double value);

	@Override
	com.top_logic.graphic.flow.data.GridLayout setY(double value);

	@Override
	com.top_logic.graphic.flow.data.GridLayout setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.GridLayout setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.GridLayout setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.GridLayout setClientId(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.GridLayout readGridLayout(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.GridLayout) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert GRID_LAYOUT__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.GridLayout_Impl result = new com.top_logic.graphic.flow.data.impl.GridLayout_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default GridLayout self() {
		return this;
	}

}
