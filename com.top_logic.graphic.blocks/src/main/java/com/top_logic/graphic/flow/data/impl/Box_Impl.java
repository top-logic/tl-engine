package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Box}.
 */
public abstract class Box_Impl extends com.top_logic.graphic.flow.data.impl.Widget_Impl implements com.top_logic.graphic.flow.data.Box {

	private double _x = 0.0d;

	private double _y = 0.0d;

	private double _width = 0.0d;

	private double _height = 0.0d;

	/**
	 * Creates a {@link Box_Impl} instance.
	 */
	public Box_Impl() {
		super();
	}

	@Override
	public final double getX() {
		return _x;
	}

	@Override
	public com.top_logic.graphic.flow.data.Box setX(double value) {
		internalSetX(value);
		return this;
	}

	/** Internal setter for {@link #getX()} without chain call utility. */
	protected final void internalSetX(double value) {
		_listener.beforeSet(this, X__PROP, value);
		_x = value;
	}

	@Override
	public final double getY() {
		return _y;
	}

	@Override
	public com.top_logic.graphic.flow.data.Box setY(double value) {
		internalSetY(value);
		return this;
	}

	/** Internal setter for {@link #getY()} without chain call utility. */
	protected final void internalSetY(double value) {
		_listener.beforeSet(this, Y__PROP, value);
		_y = value;
	}

	@Override
	public final double getWidth() {
		return _width;
	}

	@Override
	public com.top_logic.graphic.flow.data.Box setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	/** Internal setter for {@link #getWidth()} without chain call utility. */
	protected final void internalSetWidth(double value) {
		_listener.beforeSet(this, WIDTH__PROP, value);
		_width = value;
	}

	@Override
	public final double getHeight() {
		return _height;
	}

	@Override
	public com.top_logic.graphic.flow.data.Box setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	/** Internal setter for {@link #getHeight()} without chain call utility. */
	protected final void internalSetHeight(double value) {
		_listener.beforeSet(this, HEIGHT__PROP, value);
		_height = value;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			X__PROP, 
			Y__PROP, 
			WIDTH__PROP, 
			HEIGHT__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case X__PROP: return getX();
			case Y__PROP: return getY();
			case WIDTH__PROP: return getWidth();
			case HEIGHT__PROP: return getHeight();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case X__PROP: internalSetX((double) value); break;
			case Y__PROP: internalSetY((double) value); break;
			case WIDTH__PROP: internalSetWidth((double) value); break;
			case HEIGHT__PROP: internalSetHeight((double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(X__PROP);
		out.value(getX());
		out.name(Y__PROP);
		out.value(getY());
		out.name(WIDTH__PROP);
		out.value(getWidth());
		out.name(HEIGHT__PROP);
		out.value(getHeight());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case X__PROP: setX(in.nextDouble()); break;
			case Y__PROP: setY(in.nextDouble()); break;
			case WIDTH__PROP: setWidth(in.nextDouble()); break;
			case HEIGHT__PROP: setHeight(in.nextDouble()); break;
			default: super.readField(in, field);
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(X__ID);
		out.value(getX());
		out.name(Y__ID);
		out.value(getY());
		out.name(WIDTH__ID);
		out.value(getWidth());
		out.name(HEIGHT__ID);
		out.value(getHeight());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.binary.DataReader in, int field) throws java.io.IOException {
		switch (field) {
			case X__ID: setX(in.nextDouble()); break;
			case Y__ID: setY(in.nextDouble()); break;
			case WIDTH__ID: setWidth(in.nextDouble()); break;
			case HEIGHT__ID: setHeight(in.nextDouble()); break;
			default: super.readField(in, field);
		}
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.Box} type. */
	public static final String BOX__XML_ELEMENT = "box";

	/** XML attribute or element name of a {@link #getX} property. */
	private static final String X__XML_ATTR = "x";

	/** XML attribute or element name of a {@link #getY} property. */
	private static final String Y__XML_ATTR = "y";

	/** XML attribute or element name of a {@link #getWidth} property. */
	private static final String WIDTH__XML_ATTR = "width";

	/** XML attribute or element name of a {@link #getHeight} property. */
	private static final String HEIGHT__XML_ATTR = "height";

	/** Serializes all fields that are written as XML attributes. */
	@Override
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeAttributes(out);
		out.writeAttribute(X__XML_ATTR, Double.toString(getX()));
		out.writeAttribute(Y__XML_ATTR, Double.toString(getY()));
		out.writeAttribute(WIDTH__XML_ATTR, Double.toString(getWidth()));
		out.writeAttribute(HEIGHT__XML_ATTR, Double.toString(getHeight()));
	}

	/** Serializes all fields that are written as XML elements. */
	@Override
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeElements(out);
		// No element fields.
	}

	/** Creates a new {@link com.top_logic.graphic.flow.data.Box} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Box_Impl readBox_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		switch (in.getLocalName()) {
			case Text_Impl.TEXT__XML_ELEMENT: {
				return com.top_logic.graphic.flow.data.impl.Text_Impl.readText_XmlContent(in);
			}

			case Image_Impl.IMAGE__XML_ELEMENT: {
				return com.top_logic.graphic.flow.data.impl.Image_Impl.readImage_XmlContent(in);
			}

			case Empty_Impl.EMPTY__XML_ELEMENT: {
				return com.top_logic.graphic.flow.data.impl.Empty_Impl.readEmpty_XmlContent(in);
			}

			case Align_Impl.ALIGN__XML_ELEMENT: {
				return com.top_logic.graphic.flow.data.impl.Align_Impl.readAlign_XmlContent(in);
			}

			case Border_Impl.BORDER__XML_ELEMENT: {
				return com.top_logic.graphic.flow.data.impl.Border_Impl.readBorder_XmlContent(in);
			}

			case Fill_Impl.FILL__XML_ELEMENT: {
				return com.top_logic.graphic.flow.data.impl.Fill_Impl.readFill_XmlContent(in);
			}

			case Padding_Impl.PADDING__XML_ELEMENT: {
				return com.top_logic.graphic.flow.data.impl.Padding_Impl.readPadding_XmlContent(in);
			}

			case CompassLayout_Impl.COMPASS_LAYOUT__XML_ELEMENT: {
				return com.top_logic.graphic.flow.data.impl.CompassLayout_Impl.readCompassLayout_XmlContent(in);
			}

			case GridLayout_Impl.GRID_LAYOUT__XML_ELEMENT: {
				return com.top_logic.graphic.flow.data.impl.GridLayout_Impl.readGridLayout_XmlContent(in);
			}

			case HorizontalLayout_Impl.HORIZONTAL_LAYOUT__XML_ELEMENT: {
				return com.top_logic.graphic.flow.data.impl.HorizontalLayout_Impl.readHorizontalLayout_XmlContent(in);
			}

			case VerticalLayout_Impl.VERTICAL_LAYOUT__XML_ELEMENT: {
				return com.top_logic.graphic.flow.data.impl.VerticalLayout_Impl.readVerticalLayout_XmlContent(in);
			}

			default: {
				internalSkipUntilMatchingEndElement(in);
				return null;
			}
		}
	}

	@Override
	protected void readFieldXmlAttribute(String name, String value) {
		switch (name) {
			case X__XML_ATTR: {
				setX(Double.parseDouble(value));
				break;
			}
			case Y__XML_ATTR: {
				setY(Double.parseDouble(value));
				break;
			}
			case WIDTH__XML_ATTR: {
				setWidth(Double.parseDouble(value));
				break;
			}
			case HEIGHT__XML_ATTR: {
				setHeight(Double.parseDouble(value));
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
			case X__XML_ATTR: {
				setX(Double.parseDouble(in.getElementText()));
				break;
			}
			case Y__XML_ATTR: {
				setY(Double.parseDouble(in.getElementText()));
				break;
			}
			case WIDTH__XML_ATTR: {
				setWidth(Double.parseDouble(in.getElementText()));
				break;
			}
			case HEIGHT__XML_ATTR: {
				setHeight(Double.parseDouble(in.getElementText()));
				break;
			}
			default: {
				super.readFieldXmlElement(in, localName);
			}
		}
	}

	@Override
	public final <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Widget.Visitor<R,A,E> v, A arg) throws E {
		return visit((com.top_logic.graphic.flow.data.Box.Visitor<R,A,E>) v, arg);
	}

}
