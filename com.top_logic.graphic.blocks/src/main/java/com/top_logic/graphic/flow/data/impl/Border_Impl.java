package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Border}.
 */
public class Border_Impl extends com.top_logic.graphic.flow.data.impl.Decoration_Impl implements com.top_logic.graphic.flow.data.Border {

	private String _strokeStyle = "black";

	private double _thickness = 1.0;

	private boolean _top = true;

	private boolean _left = true;

	private boolean _bottom = true;

	private boolean _right = true;

	private final java.util.List<Double> _dashes = new de.haumacher.msgbuf.util.ReferenceList<>() {
		@Override
		protected void beforeAdd(int index, Double element) {
			_listener.beforeAdd(Border_Impl.this, DASHES__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, Double element) {
			_listener.afterRemove(Border_Impl.this, DASHES__PROP, index, element);
		}
	};

	/**
	 * Creates a {@link Border_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Border#create()
	 */
	public Border_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.BORDER;
	}

	@Override
	public final String getStrokeStyle() {
		return _strokeStyle;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setStrokeStyle(String value) {
		internalSetStrokeStyle(value);
		return this;
	}

	/** Internal setter for {@link #getStrokeStyle()} without chain call utility. */
	protected final void internalSetStrokeStyle(String value) {
		_listener.beforeSet(this, STROKE_STYLE__PROP, value);
		_strokeStyle = value;
	}

	@Override
	public final double getThickness() {
		return _thickness;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setThickness(double value) {
		internalSetThickness(value);
		return this;
	}

	/** Internal setter for {@link #getThickness()} without chain call utility. */
	protected final void internalSetThickness(double value) {
		_listener.beforeSet(this, THICKNESS__PROP, value);
		_thickness = value;
	}

	@Override
	public final boolean isTop() {
		return _top;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setTop(boolean value) {
		internalSetTop(value);
		return this;
	}

	/** Internal setter for {@link #isTop()} without chain call utility. */
	protected final void internalSetTop(boolean value) {
		_listener.beforeSet(this, TOP__PROP, value);
		_top = value;
	}

	@Override
	public final boolean isLeft() {
		return _left;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setLeft(boolean value) {
		internalSetLeft(value);
		return this;
	}

	/** Internal setter for {@link #isLeft()} without chain call utility. */
	protected final void internalSetLeft(boolean value) {
		_listener.beforeSet(this, LEFT__PROP, value);
		_left = value;
	}

	@Override
	public final boolean isBottom() {
		return _bottom;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setBottom(boolean value) {
		internalSetBottom(value);
		return this;
	}

	/** Internal setter for {@link #isBottom()} without chain call utility. */
	protected final void internalSetBottom(boolean value) {
		_listener.beforeSet(this, BOTTOM__PROP, value);
		_bottom = value;
	}

	@Override
	public final boolean isRight() {
		return _right;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setRight(boolean value) {
		internalSetRight(value);
		return this;
	}

	/** Internal setter for {@link #isRight()} without chain call utility. */
	protected final void internalSetRight(boolean value) {
		_listener.beforeSet(this, RIGHT__PROP, value);
		_right = value;
	}

	@Override
	public final java.util.List<Double> getDashes() {
		return _dashes;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setDashes(java.util.List<? extends Double> value) {
		internalSetDashes(value);
		return this;
	}

	/** Internal setter for {@link #getDashes()} without chain call utility. */
	protected final void internalSetDashes(java.util.List<? extends Double> value) {
		_dashes.clear();
		_dashes.addAll(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.Border addDashe(double value) {
		internalAddDashe(value);
		return this;
	}

	/** Implementation of {@link #addDashe(double)} without chain call utility. */
	protected final void internalAddDashe(double value) {
		_dashes.add(value);
	}

	@Override
	public final void removeDashe(double value) {
		_dashes.remove(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setContent(com.top_logic.graphic.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Border setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public String jsonType() {
		return BORDER__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			STROKE_STYLE__PROP, 
			THICKNESS__PROP, 
			TOP__PROP, 
			LEFT__PROP, 
			BOTTOM__PROP, 
			RIGHT__PROP, 
			DASHES__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case STROKE_STYLE__PROP: return getStrokeStyle();
			case THICKNESS__PROP: return getThickness();
			case TOP__PROP: return isTop();
			case LEFT__PROP: return isLeft();
			case BOTTOM__PROP: return isBottom();
			case RIGHT__PROP: return isRight();
			case DASHES__PROP: return getDashes();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case STROKE_STYLE__PROP: internalSetStrokeStyle((String) value); break;
			case THICKNESS__PROP: internalSetThickness((double) value); break;
			case TOP__PROP: internalSetTop((boolean) value); break;
			case LEFT__PROP: internalSetLeft((boolean) value); break;
			case BOTTOM__PROP: internalSetBottom((boolean) value); break;
			case RIGHT__PROP: internalSetRight((boolean) value); break;
			case DASHES__PROP: internalSetDashes(de.haumacher.msgbuf.util.Conversions.asList(Double.class, value)); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(STROKE_STYLE__PROP);
		out.value(getStrokeStyle());
		out.name(THICKNESS__PROP);
		out.value(getThickness());
		out.name(TOP__PROP);
		out.value(isTop());
		out.name(LEFT__PROP);
		out.value(isLeft());
		out.name(BOTTOM__PROP);
		out.value(isBottom());
		out.name(RIGHT__PROP);
		out.value(isRight());
		out.name(DASHES__PROP);
		out.beginArray();
		for (double x : getDashes()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case STROKE_STYLE__PROP: setStrokeStyle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case THICKNESS__PROP: setThickness(in.nextDouble()); break;
			case TOP__PROP: setTop(in.nextBoolean()); break;
			case LEFT__PROP: setLeft(in.nextBoolean()); break;
			case BOTTOM__PROP: setBottom(in.nextBoolean()); break;
			case RIGHT__PROP: setRight(in.nextBoolean()); break;
			case DASHES__PROP: {
				java.util.List<Double> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(in.nextDouble());
				}
				in.endArray();
				setDashes(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

	@Override
	public int typeId() {
		return BORDER__TYPE_ID;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(STROKE_STYLE__ID);
		out.value(getStrokeStyle());
		out.name(THICKNESS__ID);
		out.value(getThickness());
		out.name(TOP__ID);
		out.value(isTop());
		out.name(LEFT__ID);
		out.value(isLeft());
		out.name(BOTTOM__ID);
		out.value(isBottom());
		out.name(RIGHT__ID);
		out.value(isRight());
		out.name(DASHES__ID);
		{
			java.util.List<Double> values = getDashes();
			out.beginArray(de.haumacher.msgbuf.binary.DataType.DOUBLE, values.size());
			for (double x : values) {
				out.value(x);
			}
			out.endArray();
		}
	}

	/** Helper for creating an object of type {@link com.top_logic.graphic.flow.data.Border} from a polymorphic composition. */
	public static com.top_logic.graphic.flow.data.Border readBorder_Content(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.impl.Border_Impl result = new Border_Impl();
		result.readContent(in);
		return result;
	}

	@Override
	protected void readField(de.haumacher.msgbuf.binary.DataReader in, int field) throws java.io.IOException {
		switch (field) {
			case STROKE_STYLE__ID: setStrokeStyle(in.nextString()); break;
			case THICKNESS__ID: setThickness(in.nextDouble()); break;
			case TOP__ID: setTop(in.nextBoolean()); break;
			case LEFT__ID: setLeft(in.nextBoolean()); break;
			case BOTTOM__ID: setBottom(in.nextBoolean()); break;
			case RIGHT__ID: setRight(in.nextBoolean()); break;
			case DASHES__ID: {
				in.beginArray();
				while (in.hasNext()) {
					addDashe(in.nextDouble());
				}
				in.endArray();
			}
			break;
			default: super.readField(in, field);
		}
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.Border} type. */
	public static final String BORDER__XML_ELEMENT = "border";

	/** XML attribute or element name of a {@link #getStrokeStyle} property. */
	private static final String STROKE_STYLE__XML_ATTR = "stroke-style";

	/** XML attribute or element name of a {@link #getThickness} property. */
	private static final String THICKNESS__XML_ATTR = "thickness";

	/** XML attribute or element name of a {@link #isTop} property. */
	private static final String TOP__XML_ATTR = "top";

	/** XML attribute or element name of a {@link #isLeft} property. */
	private static final String LEFT__XML_ATTR = "left";

	/** XML attribute or element name of a {@link #isBottom} property. */
	private static final String BOTTOM__XML_ATTR = "bottom";

	/** XML attribute or element name of a {@link #isRight} property. */
	private static final String RIGHT__XML_ATTR = "right";

	/** XML attribute or element name of a {@link #getDashes} property. */
	private static final String DASHES__XML_ATTR = "dashes";

	@Override
	public String getXmlTagName() {
		return BORDER__XML_ELEMENT;
	}

	/** Serializes all fields that are written as XML attributes. */
	@Override
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeAttributes(out);
		out.writeAttribute(STROKE_STYLE__XML_ATTR, getStrokeStyle());
		out.writeAttribute(THICKNESS__XML_ATTR, Double.toString(getThickness()));
		out.writeAttribute(TOP__XML_ATTR, Boolean.toString(isTop()));
		out.writeAttribute(LEFT__XML_ATTR, Boolean.toString(isLeft()));
		out.writeAttribute(BOTTOM__XML_ATTR, Boolean.toString(isBottom()));
		out.writeAttribute(RIGHT__XML_ATTR, Boolean.toString(isRight()));
		out.writeAttribute(DASHES__XML_ATTR, getDashes().stream().map(x -> Double.toString(x)).collect(java.util.stream.Collectors.joining(", ")));
	}

	/** Serializes all fields that are written as XML elements. */
	@Override
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeElements(out);
		// No element fields.
	}

	/** Creates a new {@link com.top_logic.graphic.flow.data.Border} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Border_Impl readBorder_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		Border_Impl result = new Border_Impl();
		result.readContentXml(in);
		return result;
	}

	@Override
	protected void readFieldXmlAttribute(String name, String value) {
		switch (name) {
			case STROKE_STYLE__XML_ATTR: {
				setStrokeStyle(value);
				break;
			}
			case THICKNESS__XML_ATTR: {
				setThickness(Double.parseDouble(value));
				break;
			}
			case TOP__XML_ATTR: {
				setTop(Boolean.parseBoolean(value));
				break;
			}
			case LEFT__XML_ATTR: {
				setLeft(Boolean.parseBoolean(value));
				break;
			}
			case BOTTOM__XML_ATTR: {
				setBottom(Boolean.parseBoolean(value));
				break;
			}
			case RIGHT__XML_ATTR: {
				setRight(Boolean.parseBoolean(value));
				break;
			}
			case DASHES__XML_ATTR: {
				setDashes(java.util.Arrays.stream(value.split("\\s*,\\s*")).map(x -> Double.parseDouble(x)).collect(java.util.stream.Collectors.toList()));
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
			case STROKE_STYLE__XML_ATTR: {
				setStrokeStyle(in.getElementText());
				break;
			}
			case THICKNESS__XML_ATTR: {
				setThickness(Double.parseDouble(in.getElementText()));
				break;
			}
			case TOP__XML_ATTR: {
				setTop(Boolean.parseBoolean(in.getElementText()));
				break;
			}
			case LEFT__XML_ATTR: {
				setLeft(Boolean.parseBoolean(in.getElementText()));
				break;
			}
			case BOTTOM__XML_ATTR: {
				setBottom(Boolean.parseBoolean(in.getElementText()));
				break;
			}
			case RIGHT__XML_ATTR: {
				setRight(Boolean.parseBoolean(in.getElementText()));
				break;
			}
			case DASHES__XML_ATTR: {
				setDashes(java.util.Arrays.stream(in.getElementText().split("\\s*,\\s*")).map(x -> Double.parseDouble(x)).collect(java.util.stream.Collectors.toList()));
				break;
			}
			default: {
				super.readFieldXmlElement(in, localName);
			}
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Decoration.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
