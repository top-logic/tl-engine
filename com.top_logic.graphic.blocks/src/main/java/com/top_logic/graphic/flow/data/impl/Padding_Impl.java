package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Padding}.
 */
public class Padding_Impl extends com.top_logic.graphic.flow.data.impl.Decoration_Impl implements com.top_logic.graphic.flow.data.Padding {

	private double _top = 0.0d;

	private double _left = 0.0d;

	private double _bottom = 0.0d;

	private double _right = 0.0d;

	/**
	 * Creates a {@link Padding_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Padding#create()
	 */
	public Padding_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.PADDING;
	}

	@Override
	public final double getTop() {
		return _top;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setTop(double value) {
		internalSetTop(value);
		return this;
	}

	/** Internal setter for {@link #getTop()} without chain call utility. */
	protected final void internalSetTop(double value) {
		_listener.beforeSet(this, TOP__PROP, value);
		_top = value;
	}

	@Override
	public final double getLeft() {
		return _left;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setLeft(double value) {
		internalSetLeft(value);
		return this;
	}

	/** Internal setter for {@link #getLeft()} without chain call utility. */
	protected final void internalSetLeft(double value) {
		_listener.beforeSet(this, LEFT__PROP, value);
		_left = value;
	}

	@Override
	public final double getBottom() {
		return _bottom;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setBottom(double value) {
		internalSetBottom(value);
		return this;
	}

	/** Internal setter for {@link #getBottom()} without chain call utility. */
	protected final void internalSetBottom(double value) {
		_listener.beforeSet(this, BOTTOM__PROP, value);
		_bottom = value;
	}

	@Override
	public final double getRight() {
		return _right;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setRight(double value) {
		internalSetRight(value);
		return this;
	}

	/** Internal setter for {@link #getRight()} without chain call utility. */
	protected final void internalSetRight(double value) {
		_listener.beforeSet(this, RIGHT__PROP, value);
		_right = value;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setContent(com.top_logic.graphic.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Padding setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public String jsonType() {
		return PADDING__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			TOP__PROP, 
			LEFT__PROP, 
			BOTTOM__PROP, 
			RIGHT__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case TOP__PROP: return getTop();
			case LEFT__PROP: return getLeft();
			case BOTTOM__PROP: return getBottom();
			case RIGHT__PROP: return getRight();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TOP__PROP: internalSetTop((double) value); break;
			case LEFT__PROP: internalSetLeft((double) value); break;
			case BOTTOM__PROP: internalSetBottom((double) value); break;
			case RIGHT__PROP: internalSetRight((double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TOP__PROP);
		out.value(getTop());
		out.name(LEFT__PROP);
		out.value(getLeft());
		out.name(BOTTOM__PROP);
		out.value(getBottom());
		out.name(RIGHT__PROP);
		out.value(getRight());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TOP__PROP: setTop(in.nextDouble()); break;
			case LEFT__PROP: setLeft(in.nextDouble()); break;
			case BOTTOM__PROP: setBottom(in.nextDouble()); break;
			case RIGHT__PROP: setRight(in.nextDouble()); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public int typeId() {
		return PADDING__TYPE_ID;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TOP__ID);
		out.value(getTop());
		out.name(LEFT__ID);
		out.value(getLeft());
		out.name(BOTTOM__ID);
		out.value(getBottom());
		out.name(RIGHT__ID);
		out.value(getRight());
	}

	/** Helper for creating an object of type {@link com.top_logic.graphic.flow.data.Padding} from a polymorphic composition. */
	public static com.top_logic.graphic.flow.data.Padding readPadding_Content(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.impl.Padding_Impl result = new Padding_Impl();
		result.readContent(in);
		return result;
	}

	@Override
	protected void readField(de.haumacher.msgbuf.binary.DataReader in, int field) throws java.io.IOException {
		switch (field) {
			case TOP__ID: setTop(in.nextDouble()); break;
			case LEFT__ID: setLeft(in.nextDouble()); break;
			case BOTTOM__ID: setBottom(in.nextDouble()); break;
			case RIGHT__ID: setRight(in.nextDouble()); break;
			default: super.readField(in, field);
		}
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.Padding} type. */
	public static final String PADDING__XML_ELEMENT = "padding";

	/** XML attribute or element name of a {@link #getTop} property. */
	private static final String TOP__XML_ATTR = "top";

	/** XML attribute or element name of a {@link #getLeft} property. */
	private static final String LEFT__XML_ATTR = "left";

	/** XML attribute or element name of a {@link #getBottom} property. */
	private static final String BOTTOM__XML_ATTR = "bottom";

	/** XML attribute or element name of a {@link #getRight} property. */
	private static final String RIGHT__XML_ATTR = "right";

	@Override
	public String getXmlTagName() {
		return PADDING__XML_ELEMENT;
	}

	/** Serializes all fields that are written as XML attributes. */
	@Override
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeAttributes(out);
		out.writeAttribute(TOP__XML_ATTR, Double.toString(getTop()));
		out.writeAttribute(LEFT__XML_ATTR, Double.toString(getLeft()));
		out.writeAttribute(BOTTOM__XML_ATTR, Double.toString(getBottom()));
		out.writeAttribute(RIGHT__XML_ATTR, Double.toString(getRight()));
	}

	/** Serializes all fields that are written as XML elements. */
	@Override
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeElements(out);
		// No element fields.
	}

	/** Creates a new {@link com.top_logic.graphic.flow.data.Padding} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Padding_Impl readPadding_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		Padding_Impl result = new Padding_Impl();
		result.readContentXml(in);
		return result;
	}

	@Override
	protected void readFieldXmlAttribute(String name, String value) {
		switch (name) {
			case TOP__XML_ATTR: {
				setTop(Double.parseDouble(value));
				break;
			}
			case LEFT__XML_ATTR: {
				setLeft(Double.parseDouble(value));
				break;
			}
			case BOTTOM__XML_ATTR: {
				setBottom(Double.parseDouble(value));
				break;
			}
			case RIGHT__XML_ATTR: {
				setRight(Double.parseDouble(value));
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
			case TOP__XML_ATTR: {
				setTop(Double.parseDouble(in.getElementText()));
				break;
			}
			case LEFT__XML_ATTR: {
				setLeft(Double.parseDouble(in.getElementText()));
				break;
			}
			case BOTTOM__XML_ATTR: {
				setBottom(Double.parseDouble(in.getElementText()));
				break;
			}
			case RIGHT__XML_ATTR: {
				setRight(Double.parseDouble(in.getElementText()));
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
