package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.GridLayout}.
 */
public class GridLayout_Impl extends com.top_logic.graphic.flow.data.impl.Layout_Impl implements com.top_logic.graphic.flow.data.GridLayout {

	private int _rows = 0;

	private int _cols = 0;

	private double _gapX = 0.0d;

	private double _gapY = 0.0d;

	private final java.util.List<Double> _rowHeight = new de.haumacher.msgbuf.util.ReferenceList<Double>() {
		@Override
		protected void beforeAdd(int index, Double element) {
			_listener.beforeAdd(GridLayout_Impl.this, ROW_HEIGHT__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, Double element) {
			_listener.afterRemove(GridLayout_Impl.this, ROW_HEIGHT__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(GridLayout_Impl.this, ROW_HEIGHT__PROP);
		}
	};

	private final java.util.List<Double> _colWidth = new de.haumacher.msgbuf.util.ReferenceList<Double>() {
		@Override
		protected void beforeAdd(int index, Double element) {
			_listener.beforeAdd(GridLayout_Impl.this, COL_WIDTH__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, Double element) {
			_listener.afterRemove(GridLayout_Impl.this, COL_WIDTH__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(GridLayout_Impl.this, COL_WIDTH__PROP);
		}
	};

	/**
	 * Creates a {@link GridLayout_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.GridLayout#create()
	 */
	public GridLayout_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.GRID_LAYOUT;
	}

	@Override
	public final int getRows() {
		return _rows;
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout setRows(int value) {
		internalSetRows(value);
		return this;
	}

	/** Internal setter for {@link #getRows()} without chain call utility. */
	protected final void internalSetRows(int value) {
		_listener.beforeSet(this, ROWS__PROP, value);
		_rows = value;
		_listener.afterChanged(this, ROWS__PROP);
	}

	@Override
	public final int getCols() {
		return _cols;
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout setCols(int value) {
		internalSetCols(value);
		return this;
	}

	/** Internal setter for {@link #getCols()} without chain call utility. */
	protected final void internalSetCols(int value) {
		_listener.beforeSet(this, COLS__PROP, value);
		_cols = value;
		_listener.afterChanged(this, COLS__PROP);
	}

	@Override
	public final double getGapX() {
		return _gapX;
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout setGapX(double value) {
		internalSetGapX(value);
		return this;
	}

	/** Internal setter for {@link #getGapX()} without chain call utility. */
	protected final void internalSetGapX(double value) {
		_listener.beforeSet(this, GAP_X__PROP, value);
		_gapX = value;
		_listener.afterChanged(this, GAP_X__PROP);
	}

	@Override
	public final double getGapY() {
		return _gapY;
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout setGapY(double value) {
		internalSetGapY(value);
		return this;
	}

	/** Internal setter for {@link #getGapY()} without chain call utility. */
	protected final void internalSetGapY(double value) {
		_listener.beforeSet(this, GAP_Y__PROP, value);
		_gapY = value;
		_listener.afterChanged(this, GAP_Y__PROP);
	}

	@Override
	public final java.util.List<Double> getRowHeight() {
		return _rowHeight;
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout setRowHeight(java.util.List<? extends Double> value) {
		internalSetRowHeight(value);
		return this;
	}

	/** Internal setter for {@link #getRowHeight()} without chain call utility. */
	protected final void internalSetRowHeight(java.util.List<? extends Double> value) {
		_rowHeight.clear();
		_rowHeight.addAll(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout addRowHeight(double value) {
		internalAddRowHeight(value);
		return this;
	}

	/** Implementation of {@link #addRowHeight(double)} without chain call utility. */
	protected final void internalAddRowHeight(double value) {
		_rowHeight.add(value);
	}

	@Override
	public final void removeRowHeight(double value) {
		_rowHeight.remove(value);
	}

	@Override
	public final java.util.List<Double> getColWidth() {
		return _colWidth;
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout setColWidth(java.util.List<? extends Double> value) {
		internalSetColWidth(value);
		return this;
	}

	/** Internal setter for {@link #getColWidth()} without chain call utility. */
	protected final void internalSetColWidth(java.util.List<? extends Double> value) {
		_colWidth.clear();
		_colWidth.addAll(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout addColWidth(double value) {
		internalAddColWidth(value);
		return this;
	}

	/** Implementation of {@link #addColWidth(double)} without chain call utility. */
	protected final void internalAddColWidth(double value) {
		_colWidth.add(value);
	}

	@Override
	public final void removeColWidth(double value) {
		_colWidth.remove(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value) {
		internalSetContents(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout addContent(com.top_logic.graphic.flow.data.Box value) {
		internalAddContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.GridLayout setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return GRID_LAYOUT__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			ROWS__PROP, 
			COLS__PROP, 
			GAP_X__PROP, 
			GAP_Y__PROP, 
			ROW_HEIGHT__PROP, 
			COL_WIDTH__PROP));

	private static java.util.Set<String> TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(new java.util.HashSet<>(
			java.util.Arrays.asList(
				)));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public java.util.Set<String> transientProperties() {
		return TRANSIENT_PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case ROWS__PROP: return getRows();
			case COLS__PROP: return getCols();
			case GAP_X__PROP: return getGapX();
			case GAP_Y__PROP: return getGapY();
			case ROW_HEIGHT__PROP: return getRowHeight();
			case COL_WIDTH__PROP: return getColWidth();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ROWS__PROP: internalSetRows((int) value); break;
			case COLS__PROP: internalSetCols((int) value); break;
			case GAP_X__PROP: internalSetGapX((double) value); break;
			case GAP_Y__PROP: internalSetGapY((double) value); break;
			case ROW_HEIGHT__PROP: internalSetRowHeight(de.haumacher.msgbuf.util.Conversions.asList(Double.class, value)); break;
			case COL_WIDTH__PROP: internalSetColWidth(de.haumacher.msgbuf.util.Conversions.asList(Double.class, value)); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(ROWS__PROP);
		out.value(getRows());
		out.name(COLS__PROP);
		out.value(getCols());
		out.name(GAP_X__PROP);
		out.value(getGapX());
		out.name(GAP_Y__PROP);
		out.value(getGapY());
		out.name(ROW_HEIGHT__PROP);
		out.beginArray();
		for (double x : getRowHeight()) {
			out.value(x);
		}
		out.endArray();
		out.name(COL_WIDTH__PROP);
		out.beginArray();
		for (double x : getColWidth()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case ROWS__PROP: {
				out.value(getRows());
				break;
			}
			case COLS__PROP: {
				out.value(getCols());
				break;
			}
			case GAP_X__PROP: {
				out.value(getGapX());
				break;
			}
			case GAP_Y__PROP: {
				out.value(getGapY());
				break;
			}
			case ROW_HEIGHT__PROP: {
				out.beginArray();
				for (double x : getRowHeight()) {
					out.value(x);
				}
				out.endArray();
				break;
			}
			case COL_WIDTH__PROP: {
				out.beginArray();
				for (double x : getColWidth()) {
					out.value(x);
				}
				out.endArray();
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ROWS__PROP: setRows(in.nextInt()); break;
			case COLS__PROP: setCols(in.nextInt()); break;
			case GAP_X__PROP: setGapX(in.nextDouble()); break;
			case GAP_Y__PROP: setGapY(in.nextDouble()); break;
			case ROW_HEIGHT__PROP: {
				java.util.List<Double> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(in.nextDouble());
				}
				in.endArray();
				setRowHeight(newValue);
			}
			break;
			case COL_WIDTH__PROP: {
				java.util.List<Double> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(in.nextDouble());
				}
				in.endArray();
				setColWidth(newValue);
			}
			break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case ROW_HEIGHT__PROP: {
				out.value(((double) element));
				break;
			}
			case COL_WIDTH__PROP: {
				out.value(((double) element));
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ROW_HEIGHT__PROP: {
				return in.nextDouble();
			}
			case COL_WIDTH__PROP: {
				return in.nextDouble();
			}
			default: return super.readElement(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Layout.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
