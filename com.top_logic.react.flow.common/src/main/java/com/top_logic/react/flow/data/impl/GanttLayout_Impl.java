package com.top_logic.react.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.react.flow.data.GanttLayout}.
 */
public class GanttLayout_Impl extends com.top_logic.react.flow.data.impl.Layout_Impl implements com.top_logic.react.flow.data.GanttLayout {

	private final java.util.List<com.top_logic.react.flow.data.GanttRow> _rootRows = new de.haumacher.msgbuf.util.ReferenceList<com.top_logic.react.flow.data.GanttRow>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.react.flow.data.GanttRow element) {
			_listener.beforeAdd(GanttLayout_Impl.this, ROOT_ROWS__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.react.flow.data.GanttRow element) {
			_listener.afterRemove(GanttLayout_Impl.this, ROOT_ROWS__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(GanttLayout_Impl.this, ROOT_ROWS__PROP);
		}
	};

	private final java.util.List<com.top_logic.react.flow.data.GanttItem> _items = new de.haumacher.msgbuf.util.ReferenceList<com.top_logic.react.flow.data.GanttItem>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.react.flow.data.GanttItem element) {
			_listener.beforeAdd(GanttLayout_Impl.this, ITEMS__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.react.flow.data.GanttItem element) {
			_listener.afterRemove(GanttLayout_Impl.this, ITEMS__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(GanttLayout_Impl.this, ITEMS__PROP);
		}
	};

	private final java.util.List<com.top_logic.react.flow.data.GanttEdge> _edges = new de.haumacher.msgbuf.util.ReferenceList<com.top_logic.react.flow.data.GanttEdge>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.react.flow.data.GanttEdge element) {
			_listener.beforeAdd(GanttLayout_Impl.this, EDGES__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.react.flow.data.GanttEdge element) {
			_listener.afterRemove(GanttLayout_Impl.this, EDGES__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(GanttLayout_Impl.this, EDGES__PROP);
		}
	};

	private final java.util.List<com.top_logic.react.flow.data.GanttDecoration> _decorations = new de.haumacher.msgbuf.util.ReferenceList<com.top_logic.react.flow.data.GanttDecoration>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.react.flow.data.GanttDecoration element) {
			_listener.beforeAdd(GanttLayout_Impl.this, DECORATIONS__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.react.flow.data.GanttDecoration element) {
			_listener.afterRemove(GanttLayout_Impl.this, DECORATIONS__PROP, index, element);
		}

		@Override
		protected void afterChanged() {
			_listener.afterChanged(GanttLayout_Impl.this, DECORATIONS__PROP);
		}
	};

	private com.top_logic.react.flow.data.GanttAxis _axis = null;

	private double _rowMinContentHeight = 24.0;

	private double _rowPadding = 4.0;

	private double _indentWidth = 16.0;

	private double _axisHeight = 24.0;

	private double _rowLabelMinWidth = 200.0;

	private double _rowLabelPadding = 4.0;

	private transient double _columnWidth = 0.0d;

	/**
	 * Creates a {@link GanttLayout_Impl} instance.
	 *
	 * @see com.top_logic.react.flow.data.GanttLayout#create()
	 */
	public GanttLayout_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.GANTT_LAYOUT;
	}

	@Override
	public final java.util.List<com.top_logic.react.flow.data.GanttRow> getRootRows() {
		return _rootRows;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setRootRows(java.util.List<? extends com.top_logic.react.flow.data.GanttRow> value) {
		internalSetRootRows(value);
		return this;
	}

	/** Internal setter for {@link #getRootRows()} without chain call utility. */
	protected final void internalSetRootRows(java.util.List<? extends com.top_logic.react.flow.data.GanttRow> value) {
		if (value == null) throw new IllegalArgumentException("Property 'rootRows' cannot be null.");
		_rootRows.clear();
		_rootRows.addAll(value);
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout addRootRow(com.top_logic.react.flow.data.GanttRow value) {
		internalAddRootRow(value);
		return this;
	}

	/** Implementation of {@link #addRootRow(com.top_logic.react.flow.data.GanttRow)} without chain call utility. */
	protected final void internalAddRootRow(com.top_logic.react.flow.data.GanttRow value) {
		_rootRows.add(value);
	}

	@Override
	public final void removeRootRow(com.top_logic.react.flow.data.GanttRow value) {
		_rootRows.remove(value);
	}

	@Override
	public final java.util.List<com.top_logic.react.flow.data.GanttItem> getItems() {
		return _items;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setItems(java.util.List<? extends com.top_logic.react.flow.data.GanttItem> value) {
		internalSetItems(value);
		return this;
	}

	/** Internal setter for {@link #getItems()} without chain call utility. */
	protected final void internalSetItems(java.util.List<? extends com.top_logic.react.flow.data.GanttItem> value) {
		if (value == null) throw new IllegalArgumentException("Property 'items' cannot be null.");
		_items.clear();
		_items.addAll(value);
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout addItem(com.top_logic.react.flow.data.GanttItem value) {
		internalAddItem(value);
		return this;
	}

	/** Implementation of {@link #addItem(com.top_logic.react.flow.data.GanttItem)} without chain call utility. */
	protected final void internalAddItem(com.top_logic.react.flow.data.GanttItem value) {
		_items.add(value);
	}

	@Override
	public final void removeItem(com.top_logic.react.flow.data.GanttItem value) {
		_items.remove(value);
	}

	@Override
	public final java.util.List<com.top_logic.react.flow.data.GanttEdge> getEdges() {
		return _edges;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setEdges(java.util.List<? extends com.top_logic.react.flow.data.GanttEdge> value) {
		internalSetEdges(value);
		return this;
	}

	/** Internal setter for {@link #getEdges()} without chain call utility. */
	protected final void internalSetEdges(java.util.List<? extends com.top_logic.react.flow.data.GanttEdge> value) {
		if (value == null) throw new IllegalArgumentException("Property 'edges' cannot be null.");
		_edges.clear();
		_edges.addAll(value);
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout addEdge(com.top_logic.react.flow.data.GanttEdge value) {
		internalAddEdge(value);
		return this;
	}

	/** Implementation of {@link #addEdge(com.top_logic.react.flow.data.GanttEdge)} without chain call utility. */
	protected final void internalAddEdge(com.top_logic.react.flow.data.GanttEdge value) {
		_edges.add(value);
	}

	@Override
	public final void removeEdge(com.top_logic.react.flow.data.GanttEdge value) {
		_edges.remove(value);
	}

	@Override
	public final java.util.List<com.top_logic.react.flow.data.GanttDecoration> getDecorations() {
		return _decorations;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setDecorations(java.util.List<? extends com.top_logic.react.flow.data.GanttDecoration> value) {
		internalSetDecorations(value);
		return this;
	}

	/** Internal setter for {@link #getDecorations()} without chain call utility. */
	protected final void internalSetDecorations(java.util.List<? extends com.top_logic.react.flow.data.GanttDecoration> value) {
		if (value == null) throw new IllegalArgumentException("Property 'decorations' cannot be null.");
		_decorations.clear();
		_decorations.addAll(value);
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout addDecoration(com.top_logic.react.flow.data.GanttDecoration value) {
		internalAddDecoration(value);
		return this;
	}

	/** Implementation of {@link #addDecoration(com.top_logic.react.flow.data.GanttDecoration)} without chain call utility. */
	protected final void internalAddDecoration(com.top_logic.react.flow.data.GanttDecoration value) {
		_decorations.add(value);
	}

	@Override
	public final void removeDecoration(com.top_logic.react.flow.data.GanttDecoration value) {
		_decorations.remove(value);
	}

	@Override
	public final com.top_logic.react.flow.data.GanttAxis getAxis() {
		return _axis;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setAxis(com.top_logic.react.flow.data.GanttAxis value) {
		internalSetAxis(value);
		return this;
	}

	/** Internal setter for {@link #getAxis()} without chain call utility. */
	protected final void internalSetAxis(com.top_logic.react.flow.data.GanttAxis value) {
		_listener.beforeSet(this, AXIS__PROP, value);
		_axis = value;
		_listener.afterChanged(this, AXIS__PROP);
	}

	@Override
	public final boolean hasAxis() {
		return _axis != null;
	}

	@Override
	public final double getRowMinContentHeight() {
		return _rowMinContentHeight;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setRowMinContentHeight(double value) {
		internalSetRowMinContentHeight(value);
		return this;
	}

	/** Internal setter for {@link #getRowMinContentHeight()} without chain call utility. */
	protected final void internalSetRowMinContentHeight(double value) {
		_listener.beforeSet(this, ROW_MIN_CONTENT_HEIGHT__PROP, value);
		_rowMinContentHeight = value;
		_listener.afterChanged(this, ROW_MIN_CONTENT_HEIGHT__PROP);
	}

	@Override
	public final double getRowPadding() {
		return _rowPadding;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setRowPadding(double value) {
		internalSetRowPadding(value);
		return this;
	}

	/** Internal setter for {@link #getRowPadding()} without chain call utility. */
	protected final void internalSetRowPadding(double value) {
		_listener.beforeSet(this, ROW_PADDING__PROP, value);
		_rowPadding = value;
		_listener.afterChanged(this, ROW_PADDING__PROP);
	}

	@Override
	public final double getIndentWidth() {
		return _indentWidth;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setIndentWidth(double value) {
		internalSetIndentWidth(value);
		return this;
	}

	/** Internal setter for {@link #getIndentWidth()} without chain call utility. */
	protected final void internalSetIndentWidth(double value) {
		_listener.beforeSet(this, INDENT_WIDTH__PROP, value);
		_indentWidth = value;
		_listener.afterChanged(this, INDENT_WIDTH__PROP);
	}

	@Override
	public final double getAxisHeight() {
		return _axisHeight;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setAxisHeight(double value) {
		internalSetAxisHeight(value);
		return this;
	}

	/** Internal setter for {@link #getAxisHeight()} without chain call utility. */
	protected final void internalSetAxisHeight(double value) {
		_listener.beforeSet(this, AXIS_HEIGHT__PROP, value);
		_axisHeight = value;
		_listener.afterChanged(this, AXIS_HEIGHT__PROP);
	}

	@Override
	public final double getRowLabelMinWidth() {
		return _rowLabelMinWidth;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setRowLabelMinWidth(double value) {
		internalSetRowLabelMinWidth(value);
		return this;
	}

	/** Internal setter for {@link #getRowLabelMinWidth()} without chain call utility. */
	protected final void internalSetRowLabelMinWidth(double value) {
		_listener.beforeSet(this, ROW_LABEL_MIN_WIDTH__PROP, value);
		_rowLabelMinWidth = value;
		_listener.afterChanged(this, ROW_LABEL_MIN_WIDTH__PROP);
	}

	@Override
	public final double getRowLabelPadding() {
		return _rowLabelPadding;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setRowLabelPadding(double value) {
		internalSetRowLabelPadding(value);
		return this;
	}

	/** Internal setter for {@link #getRowLabelPadding()} without chain call utility. */
	protected final void internalSetRowLabelPadding(double value) {
		_listener.beforeSet(this, ROW_LABEL_PADDING__PROP, value);
		_rowLabelPadding = value;
		_listener.afterChanged(this, ROW_LABEL_PADDING__PROP);
	}

	@Override
	public final double getColumnWidth() {
		return _columnWidth;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setColumnWidth(double value) {
		internalSetColumnWidth(value);
		return this;
	}

	/** Internal setter for {@link #getColumnWidth()} without chain call utility. */
	protected final void internalSetColumnWidth(double value) {
		_listener.beforeSet(this, COLUMN_WIDTH__PROP, value);
		_columnWidth = value;
		_listener.afterChanged(this, COLUMN_WIDTH__PROP);
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setContents(java.util.List<? extends com.top_logic.react.flow.data.Box> value) {
		internalSetContents(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout addContent(com.top_logic.react.flow.data.Box value) {
		internalAddContent(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setCssClass(String value) {
		internalSetCssClass(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setUserObject(java.lang.Object value) {
		internalSetUserObject(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setClientId(String value) {
		internalSetClientId(value);
		return this;
	}

	@Override
	public com.top_logic.react.flow.data.GanttLayout setRenderInfo(java.lang.Object value) {
		internalSetRenderInfo(value);
		return this;
	}

	@Override
	public String jsonType() {
		return GANTT_LAYOUT__TYPE;
	}

	@SuppressWarnings("hiding")
	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ROOT_ROWS__PROP, 
			ITEMS__PROP, 
			EDGES__PROP, 
			DECORATIONS__PROP, 
			AXIS__PROP, 
			ROW_MIN_CONTENT_HEIGHT__PROP, 
			ROW_PADDING__PROP, 
			INDENT_WIDTH__PROP, 
			AXIS_HEIGHT__PROP, 
			ROW_LABEL_MIN_WIDTH__PROP, 
			ROW_LABEL_PADDING__PROP, 
			COLUMN_WIDTH__PROP);
		java.util.List<String> tmp = new java.util.ArrayList<>();
		tmp.addAll(com.top_logic.react.flow.data.impl.Layout_Impl.PROPERTIES);
		tmp.addAll(local);
		PROPERTIES = java.util.Collections.unmodifiableList(tmp);
	}

	@SuppressWarnings("hiding")
	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(com.top_logic.react.flow.data.impl.Layout_Impl.TRANSIENT_PROPERTIES);
		tmp.addAll(java.util.Arrays.asList(
				COLUMN_WIDTH__PROP));
		TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(tmp);
	}

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
			case ROOT_ROWS__PROP: return getRootRows();
			case ITEMS__PROP: return getItems();
			case EDGES__PROP: return getEdges();
			case DECORATIONS__PROP: return getDecorations();
			case AXIS__PROP: return getAxis();
			case ROW_MIN_CONTENT_HEIGHT__PROP: return getRowMinContentHeight();
			case ROW_PADDING__PROP: return getRowPadding();
			case INDENT_WIDTH__PROP: return getIndentWidth();
			case AXIS_HEIGHT__PROP: return getAxisHeight();
			case ROW_LABEL_MIN_WIDTH__PROP: return getRowLabelMinWidth();
			case ROW_LABEL_PADDING__PROP: return getRowLabelPadding();
			case COLUMN_WIDTH__PROP: return getColumnWidth();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ROOT_ROWS__PROP: internalSetRootRows(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.react.flow.data.GanttRow.class, value)); break;
			case ITEMS__PROP: internalSetItems(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.react.flow.data.GanttItem.class, value)); break;
			case EDGES__PROP: internalSetEdges(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.react.flow.data.GanttEdge.class, value)); break;
			case DECORATIONS__PROP: internalSetDecorations(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.react.flow.data.GanttDecoration.class, value)); break;
			case AXIS__PROP: internalSetAxis((com.top_logic.react.flow.data.GanttAxis) value); break;
			case ROW_MIN_CONTENT_HEIGHT__PROP: internalSetRowMinContentHeight((double) value); break;
			case ROW_PADDING__PROP: internalSetRowPadding((double) value); break;
			case INDENT_WIDTH__PROP: internalSetIndentWidth((double) value); break;
			case AXIS_HEIGHT__PROP: internalSetAxisHeight((double) value); break;
			case ROW_LABEL_MIN_WIDTH__PROP: internalSetRowLabelMinWidth((double) value); break;
			case ROW_LABEL_PADDING__PROP: internalSetRowLabelPadding((double) value); break;
			case COLUMN_WIDTH__PROP: internalSetColumnWidth((double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(ROOT_ROWS__PROP);
		out.beginArray();
		for (com.top_logic.react.flow.data.GanttRow x : getRootRows()) {
			x.writeTo(scope, out);
		}
		out.endArray();
		out.name(ITEMS__PROP);
		out.beginArray();
		for (com.top_logic.react.flow.data.GanttItem x : getItems()) {
			x.writeTo(scope, out);
		}
		out.endArray();
		out.name(EDGES__PROP);
		out.beginArray();
		for (com.top_logic.react.flow.data.GanttEdge x : getEdges()) {
			x.writeTo(scope, out);
		}
		out.endArray();
		out.name(DECORATIONS__PROP);
		out.beginArray();
		for (com.top_logic.react.flow.data.GanttDecoration x : getDecorations()) {
			x.writeTo(scope, out);
		}
		out.endArray();
		if (hasAxis()) {
			out.name(AXIS__PROP);
			getAxis().writeTo(scope, out);
		}
		out.name(ROW_MIN_CONTENT_HEIGHT__PROP);
		out.value(getRowMinContentHeight());
		out.name(ROW_PADDING__PROP);
		out.value(getRowPadding());
		out.name(INDENT_WIDTH__PROP);
		out.value(getIndentWidth());
		out.name(AXIS_HEIGHT__PROP);
		out.value(getAxisHeight());
		out.name(ROW_LABEL_MIN_WIDTH__PROP);
		out.value(getRowLabelMinWidth());
		out.name(ROW_LABEL_PADDING__PROP);
		out.value(getRowLabelPadding());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case ROOT_ROWS__PROP: {
				out.beginArray();
				for (com.top_logic.react.flow.data.GanttRow x : getRootRows()) {
					x.writeTo(scope, out);
				}
				out.endArray();
				break;
			}
			case ITEMS__PROP: {
				out.beginArray();
				for (com.top_logic.react.flow.data.GanttItem x : getItems()) {
					x.writeTo(scope, out);
				}
				out.endArray();
				break;
			}
			case EDGES__PROP: {
				out.beginArray();
				for (com.top_logic.react.flow.data.GanttEdge x : getEdges()) {
					x.writeTo(scope, out);
				}
				out.endArray();
				break;
			}
			case DECORATIONS__PROP: {
				out.beginArray();
				for (com.top_logic.react.flow.data.GanttDecoration x : getDecorations()) {
					x.writeTo(scope, out);
				}
				out.endArray();
				break;
			}
			case AXIS__PROP: {
				if (hasAxis()) {
					getAxis().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case ROW_MIN_CONTENT_HEIGHT__PROP: {
				out.value(getRowMinContentHeight());
				break;
			}
			case ROW_PADDING__PROP: {
				out.value(getRowPadding());
				break;
			}
			case INDENT_WIDTH__PROP: {
				out.value(getIndentWidth());
				break;
			}
			case AXIS_HEIGHT__PROP: {
				out.value(getAxisHeight());
				break;
			}
			case ROW_LABEL_MIN_WIDTH__PROP: {
				out.value(getRowLabelMinWidth());
				break;
			}
			case ROW_LABEL_PADDING__PROP: {
				out.value(getRowLabelPadding());
				break;
			}
			case COLUMN_WIDTH__PROP: {
				out.value(getColumnWidth());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ROOT_ROWS__PROP: {
				java.util.List<com.top_logic.react.flow.data.GanttRow> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.react.flow.data.GanttRow.readGanttRow(scope, in));
				}
				in.endArray();
				setRootRows(newValue);
			}
			break;
			case ITEMS__PROP: {
				java.util.List<com.top_logic.react.flow.data.GanttItem> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.react.flow.data.GanttItem.readGanttItem(scope, in));
				}
				in.endArray();
				setItems(newValue);
			}
			break;
			case EDGES__PROP: {
				java.util.List<com.top_logic.react.flow.data.GanttEdge> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.react.flow.data.GanttEdge.readGanttEdge(scope, in));
				}
				in.endArray();
				setEdges(newValue);
			}
			break;
			case DECORATIONS__PROP: {
				java.util.List<com.top_logic.react.flow.data.GanttDecoration> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.react.flow.data.GanttDecoration.readGanttDecoration(scope, in));
				}
				in.endArray();
				setDecorations(newValue);
			}
			break;
			case AXIS__PROP: setAxis(com.top_logic.react.flow.data.GanttAxis.readGanttAxis(scope, in)); break;
			case ROW_MIN_CONTENT_HEIGHT__PROP: setRowMinContentHeight(in.nextDouble()); break;
			case ROW_PADDING__PROP: setRowPadding(in.nextDouble()); break;
			case INDENT_WIDTH__PROP: setIndentWidth(in.nextDouble()); break;
			case AXIS_HEIGHT__PROP: setAxisHeight(in.nextDouble()); break;
			case ROW_LABEL_MIN_WIDTH__PROP: setRowLabelMinWidth(in.nextDouble()); break;
			case ROW_LABEL_PADDING__PROP: setRowLabelPadding(in.nextDouble()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case ROOT_ROWS__PROP: {
				((com.top_logic.react.flow.data.GanttRow) element).writeTo(scope, out);
				break;
			}
			case ITEMS__PROP: {
				((com.top_logic.react.flow.data.GanttItem) element).writeTo(scope, out);
				break;
			}
			case EDGES__PROP: {
				((com.top_logic.react.flow.data.GanttEdge) element).writeTo(scope, out);
				break;
			}
			case DECORATIONS__PROP: {
				((com.top_logic.react.flow.data.GanttDecoration) element).writeTo(scope, out);
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ROOT_ROWS__PROP: {
				return com.top_logic.react.flow.data.GanttRow.readGanttRow(scope, in);
			}
			case ITEMS__PROP: {
				return com.top_logic.react.flow.data.GanttItem.readGanttItem(scope, in);
			}
			case EDGES__PROP: {
				return com.top_logic.react.flow.data.GanttEdge.readGanttEdge(scope, in);
			}
			case DECORATIONS__PROP: {
				return com.top_logic.react.flow.data.GanttDecoration.readGanttDecoration(scope, in);
			}
			default: return super.readElement(scope, in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.react.flow.data.Layout.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
