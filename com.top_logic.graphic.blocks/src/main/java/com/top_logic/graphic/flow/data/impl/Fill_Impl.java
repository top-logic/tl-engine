package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Fill}.
 */
public class Fill_Impl extends com.top_logic.graphic.flow.data.impl.Decoration_Impl implements com.top_logic.graphic.flow.data.Fill {

	private String _fillStyle = "black";

	/**
	 * Creates a {@link Fill_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Fill#create()
	 */
	public Fill_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.FILL;
	}

	@Override
	public final String getFillStyle() {
		return _fillStyle;
	}

	@Override
	public com.top_logic.graphic.flow.data.Fill setFillStyle(String value) {
		internalSetFillStyle(value);
		return this;
	}

	/** Internal setter for {@link #getFillStyle()} without chain call utility. */
	protected final void internalSetFillStyle(String value) {
		_listener.beforeSet(this, FILL_STYLE__PROP, value);
		_fillStyle = value;
	}

	@Override
	public com.top_logic.graphic.flow.data.Fill setContent(com.top_logic.graphic.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Fill setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Fill setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Fill setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Fill setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public String jsonType() {
		return FILL__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			FILL_STYLE__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case FILL_STYLE__PROP: return getFillStyle();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case FILL_STYLE__PROP: internalSetFillStyle((String) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(FILL_STYLE__PROP);
		out.value(getFillStyle());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case FILL_STYLE__PROP: {
				out.value(getFillStyle());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case FILL_STYLE__PROP: setFillStyle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(scope, in, field);
		}
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.Fill} type. */
	public static final String FILL__XML_ELEMENT = "fill";

	/** XML attribute or element name of a {@link #getFillStyle} property. */
	private static final String FILL_STYLE__XML_ATTR = "fill-style";

	@Override
	public String getXmlTagName() {
		return FILL__XML_ELEMENT;
	}

	/** Serializes all fields that are written as XML attributes. */
	@Override
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeAttributes(out);
		out.writeAttribute(FILL_STYLE__XML_ATTR, getFillStyle());
	}

	/** Serializes all fields that are written as XML elements. */
	@Override
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeElements(out);
		// No element fields.
	}

	/** Creates a new {@link com.top_logic.graphic.flow.data.Fill} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Fill_Impl readFill_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		Fill_Impl result = new Fill_Impl();
		result.readContentXml(in);
		return result;
	}

	@Override
	protected void readFieldXmlAttribute(String name, String value) {
		switch (name) {
			case FILL_STYLE__XML_ATTR: {
				setFillStyle(value);
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
			case FILL_STYLE__XML_ATTR: {
				setFillStyle(in.getElementText());
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
