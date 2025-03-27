package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.RowLayout}.
 */
public abstract class RowLayout_Impl extends com.top_logic.graphic.flow.data.impl.Layout_Impl implements com.top_logic.graphic.flow.data.RowLayout {

	private double _gap = 0.0d;

	private com.top_logic.graphic.flow.data.SpaceDistribution _fill = com.top_logic.graphic.flow.data.SpaceDistribution.NONE;

	/**
	 * Creates a {@link RowLayout_Impl} instance.
	 */
	public RowLayout_Impl() {
		super();
	}

	@Override
	public final double getGap() {
		return _gap;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setGap(double value) {
		internalSetGap(value);
		return this;
	}

	/** Internal setter for {@link #getGap()} without chain call utility. */
	protected final void internalSetGap(double value) {
		_listener.beforeSet(this, GAP__PROP, value);
		_gap = value;
	}

	@Override
	public final com.top_logic.graphic.flow.data.SpaceDistribution getFill() {
		return _fill;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setFill(com.top_logic.graphic.flow.data.SpaceDistribution value) {
		internalSetFill(value);
		return this;
	}

	/** Internal setter for {@link #getFill()} without chain call utility. */
	protected final void internalSetFill(com.top_logic.graphic.flow.data.SpaceDistribution value) {
		if (value == null) throw new IllegalArgumentException("Property 'fill' cannot be null.");
		_listener.beforeSet(this, FILL__PROP, value);
		_fill = value;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value) {
		internalSetContents(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout addContent(com.top_logic.graphic.flow.data.Box value) {
		internalAddContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.RowLayout setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			GAP__PROP, 
			FILL__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case GAP__PROP: return getGap();
			case FILL__PROP: return getFill();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case GAP__PROP: internalSetGap((double) value); break;
			case FILL__PROP: internalSetFill((com.top_logic.graphic.flow.data.SpaceDistribution) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(GAP__PROP);
		out.value(getGap());
		out.name(FILL__PROP);
		getFill().writeTo(out);
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case GAP__PROP: setGap(in.nextDouble()); break;
			case FILL__PROP: setFill(com.top_logic.graphic.flow.data.SpaceDistribution.readSpaceDistribution(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(GAP__ID);
		out.value(getGap());
		out.name(FILL__ID);
		getFill().writeTo(out);
	}

	@Override
	protected void readField(de.haumacher.msgbuf.binary.DataReader in, int field) throws java.io.IOException {
		switch (field) {
			case GAP__ID: setGap(in.nextDouble()); break;
			case FILL__ID: setFill(com.top_logic.graphic.flow.data.SpaceDistribution.readSpaceDistribution(in)); break;
			default: super.readField(in, field);
		}
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.RowLayout} type. */
	public static final String ROW_LAYOUT__XML_ELEMENT = "row-layout";

	/** XML attribute or element name of a {@link #getGap} property. */
	private static final String GAP__XML_ATTR = "gap";

	/** XML attribute or element name of a {@link #getFill} property. */
	private static final String FILL__XML_ATTR = "fill";

	/** Serializes all fields that are written as XML attributes. */
	@Override
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeAttributes(out);
		out.writeAttribute(GAP__XML_ATTR, Double.toString(getGap()));
		out.writeAttribute(FILL__XML_ATTR, getFill().protocolName());
	}

	/** Serializes all fields that are written as XML elements. */
	@Override
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeElements(out);
		// No element fields.
	}

	/** Creates a new {@link com.top_logic.graphic.flow.data.RowLayout} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static RowLayout_Impl readRowLayout_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		switch (in.getLocalName()) {
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
			case GAP__XML_ATTR: {
				setGap(Double.parseDouble(value));
				break;
			}
			case FILL__XML_ATTR: {
				setFill(com.top_logic.graphic.flow.data.SpaceDistribution.valueOfProtocol(value));
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
			case GAP__XML_ATTR: {
				setGap(Double.parseDouble(in.getElementText()));
				break;
			}
			case FILL__XML_ATTR: {
				setFill(com.top_logic.graphic.flow.data.SpaceDistribution.valueOfProtocol(in.getElementText()));
				break;
			}
			default: {
				super.readFieldXmlElement(in, localName);
			}
		}
	}

	@Override
	public final <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Layout.Visitor<R,A,E> v, A arg) throws E {
		return visit((com.top_logic.graphic.flow.data.RowLayout.Visitor<R,A,E>) v, arg);
	}

}
