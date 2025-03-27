package com.top_logic.graphic.flow.data;

public interface GridLayout extends Layout, com.top_logic.graphic.flow.model.layout.GridLayout {

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

	/** Identifier for the {@link com.top_logic.graphic.flow.data.GridLayout} type in binary format. */
	static final int GRID_LAYOUT__TYPE_ID = 9;

	/** Identifier for the property {@link #getRows()} in binary format. */
	static final int ROWS__ID = 6;

	/** Identifier for the property {@link #getCols()} in binary format. */
	static final int COLS__ID = 7;

	/** Identifier for the property {@link #getGapX()} in binary format. */
	static final int GAP_X__ID = 8;

	/** Identifier for the property {@link #getGapY()} in binary format. */
	static final int GAP_Y__ID = 9;

	/** Identifier for the property {@link #getRowHeight()} in binary format. */
	static final int ROW_HEIGHT__ID = 10;

	/** Identifier for the property {@link #getColWidth()} in binary format. */
	static final int COL_WIDTH__ID = 11;

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

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.GridLayout readGridLayout(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.impl.GridLayout_Impl result = new com.top_logic.graphic.flow.data.impl.GridLayout_Impl();
		result.readContent(in);
		return result;
	}

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.GridLayout readGridLayout(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		in.beginObject();
		com.top_logic.graphic.flow.data.GridLayout result = com.top_logic.graphic.flow.data.impl.GridLayout_Impl.readGridLayout_Content(in);
		in.endObject();
		return result;
	}

	@Override
	default GridLayout self() {
		return this;
	}

	/** Creates a new {@link GridLayout} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static GridLayout readGridLayout(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return com.top_logic.graphic.flow.data.impl.GridLayout_Impl.readGridLayout_XmlContent(in);
	}

}
