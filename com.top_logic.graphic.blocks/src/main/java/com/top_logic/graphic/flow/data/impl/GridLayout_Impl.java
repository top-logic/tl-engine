package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.GridLayout}.
 */
public class GridLayout_Impl extends com.top_logic.graphic.flow.data.impl.Layout_Impl implements com.top_logic.graphic.flow.data.GridLayout {

	private int _rows = 0;

	private int _cols = 0;

	private double _gapX = 0.0d;

	private double _gapY = 0.0d;

	private final java.util.List<Double> _rowHeight = new de.haumacher.msgbuf.util.ReferenceList<>() {
		@Override
		protected void beforeAdd(int index, Double element) {
			_listener.beforeAdd(GridLayout_Impl.this, ROW_HEIGHT__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, Double element) {
			_listener.afterRemove(GridLayout_Impl.this, ROW_HEIGHT__PROP, index, element);
		}
	};

	private final java.util.List<Double> _colWidth = new de.haumacher.msgbuf.util.ReferenceList<>() {
		@Override
		protected void beforeAdd(int index, Double element) {
			_listener.beforeAdd(GridLayout_Impl.this, COL_WIDTH__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, Double element) {
			_listener.afterRemove(GridLayout_Impl.this, COL_WIDTH__PROP, index, element);
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

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
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
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
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
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ROWS__PROP: setRows(in.nextInt()); break;
			case COLS__PROP: setCols(in.nextInt()); break;
			case GAP_X__PROP: setGapX(in.nextDouble()); break;
			case GAP_Y__PROP: setGapY(in.nextDouble()); break;
			case ROW_HEIGHT__PROP: {
				in.beginArray();
				while (in.hasNext()) {
					addRowHeight(in.nextDouble());
				}
				in.endArray();
			}
			break;
			case COL_WIDTH__PROP: {
				in.beginArray();
				while (in.hasNext()) {
					addColWidth(in.nextDouble());
				}
				in.endArray();
			}
			break;
			default: super.readField(in, field);
		}
	}

	@Override
	public int typeId() {
		return GRID_LAYOUT__TYPE_ID;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(ROWS__ID);
		out.value(getRows());
		out.name(COLS__ID);
		out.value(getCols());
		out.name(GAP_X__ID);
		out.value(getGapX());
		out.name(GAP_Y__ID);
		out.value(getGapY());
		out.name(ROW_HEIGHT__ID);
		{
			java.util.List<Double> values = getRowHeight();
			out.beginArray(de.haumacher.msgbuf.binary.DataType.DOUBLE, values.size());
			for (double x : values) {
				out.value(x);
			}
			out.endArray();
		}
		out.name(COL_WIDTH__ID);
		{
			java.util.List<Double> values = getColWidth();
			out.beginArray(de.haumacher.msgbuf.binary.DataType.DOUBLE, values.size());
			for (double x : values) {
				out.value(x);
			}
			out.endArray();
		}
	}

	/** Helper for creating an object of type {@link com.top_logic.graphic.flow.data.GridLayout} from a polymorphic composition. */
	public static com.top_logic.graphic.flow.data.GridLayout readGridLayout_Content(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.impl.GridLayout_Impl result = new GridLayout_Impl();
		result.readContent(in);
		return result;
	}

	@Override
	protected void readField(de.haumacher.msgbuf.binary.DataReader in, int field) throws java.io.IOException {
		switch (field) {
			case ROWS__ID: setRows(in.nextInt()); break;
			case COLS__ID: setCols(in.nextInt()); break;
			case GAP_X__ID: setGapX(in.nextDouble()); break;
			case GAP_Y__ID: setGapY(in.nextDouble()); break;
			case ROW_HEIGHT__ID: {
				in.beginArray();
				while (in.hasNext()) {
					addRowHeight(in.nextDouble());
				}
				in.endArray();
			}
			break;
			case COL_WIDTH__ID: {
				in.beginArray();
				while (in.hasNext()) {
					addColWidth(in.nextDouble());
				}
				in.endArray();
			}
			break;
			default: super.readField(in, field);
		}
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.GridLayout} type. */
	public static final String GRID_LAYOUT__XML_ELEMENT = "grid-layout";

	/** XML attribute or element name of a {@link #getRows} property. */
	private static final String ROWS__XML_ATTR = "rows";

	/** XML attribute or element name of a {@link #getCols} property. */
	private static final String COLS__XML_ATTR = "cols";

	/** XML attribute or element name of a {@link #getGapX} property. */
	private static final String GAP_X__XML_ATTR = "gap-x";

	/** XML attribute or element name of a {@link #getGapY} property. */
	private static final String GAP_Y__XML_ATTR = "gap-y";

	/** XML attribute or element name of a {@link #getRowHeight} property. */
	private static final String ROW_HEIGHT__XML_ATTR = "row-height";

	/** XML attribute or element name of a {@link #getColWidth} property. */
	private static final String COL_WIDTH__XML_ATTR = "col-width";

	@Override
	public String getXmlTagName() {
		return GRID_LAYOUT__XML_ELEMENT;
	}

	/** Serializes all fields that are written as XML attributes. */
	@Override
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeAttributes(out);
		out.writeAttribute(ROWS__XML_ATTR, Integer.toString(getRows()));
		out.writeAttribute(COLS__XML_ATTR, Integer.toString(getCols()));
		out.writeAttribute(GAP_X__XML_ATTR, Double.toString(getGapX()));
		out.writeAttribute(GAP_Y__XML_ATTR, Double.toString(getGapY()));
		out.writeAttribute(ROW_HEIGHT__XML_ATTR, getRowHeight().stream().map(x -> Double.toString(x)).collect(java.util.stream.Collectors.joining(", ")));
		out.writeAttribute(COL_WIDTH__XML_ATTR, getColWidth().stream().map(x -> Double.toString(x)).collect(java.util.stream.Collectors.joining(", ")));
	}

	/** Serializes all fields that are written as XML elements. */
	@Override
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeElements(out);
		// No element fields.
	}

	/** Creates a new {@link com.top_logic.graphic.flow.data.GridLayout} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static GridLayout_Impl readGridLayout_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		GridLayout_Impl result = new GridLayout_Impl();
		result.readContentXml(in);
		return result;
	}

	@Override
	protected void readFieldXmlAttribute(String name, String value) {
		switch (name) {
			case ROWS__XML_ATTR: {
				setRows(Integer.parseInt(value));
				break;
			}
			case COLS__XML_ATTR: {
				setCols(Integer.parseInt(value));
				break;
			}
			case GAP_X__XML_ATTR: {
				setGapX(Double.parseDouble(value));
				break;
			}
			case GAP_Y__XML_ATTR: {
				setGapY(Double.parseDouble(value));
				break;
			}
			case ROW_HEIGHT__XML_ATTR: {
				setRowHeight(java.util.Arrays.stream(value.split("\\s*,\\s*")).map(x -> Double.parseDouble(x)).collect(java.util.stream.Collectors.toList()));
				break;
			}
			case COL_WIDTH__XML_ATTR: {
				setColWidth(java.util.Arrays.stream(value.split("\\s*,\\s*")).map(x -> Double.parseDouble(x)).collect(java.util.stream.Collectors.toList()));
				break;
			}
			default: {
				super.readFieldXmlAttribute(name, value);
			}
		}
	}

	@Override
	protected void readFieldXmlElement(javax.xml.stream.XMLStreamReader in, String localName) throws javax.xml.stream.XMLStreamException {
		switch (localName) {
			case ROWS__XML_ATTR: {
				setRows(Integer.parseInt(in.getElementText()));
				break;
			}
			case COLS__XML_ATTR: {
				setCols(Integer.parseInt(in.getElementText()));
				break;
			}
			case GAP_X__XML_ATTR: {
				setGapX(Double.parseDouble(in.getElementText()));
				break;
			}
			case GAP_Y__XML_ATTR: {
				setGapY(Double.parseDouble(in.getElementText()));
				break;
			}
			case ROW_HEIGHT__XML_ATTR: {
				setRowHeight(java.util.Arrays.stream(in.getElementText().split("\\s*,\\s*")).map(x -> Double.parseDouble(x)).collect(java.util.stream.Collectors.toList()));
				break;
			}
			case COL_WIDTH__XML_ATTR: {
				setColWidth(java.util.Arrays.stream(in.getElementText().split("\\s*,\\s*")).map(x -> Double.parseDouble(x)).collect(java.util.stream.Collectors.toList()));
				break;
			}
			default: {
				super.readFieldXmlElement(in, localName);
			}
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Layout.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
