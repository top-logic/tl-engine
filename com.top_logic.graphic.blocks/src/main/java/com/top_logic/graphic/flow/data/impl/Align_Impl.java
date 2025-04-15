package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Align}.
 */
public class Align_Impl extends com.top_logic.graphic.flow.data.impl.Decoration_Impl implements com.top_logic.graphic.flow.data.Align {

	private com.top_logic.graphic.flow.data.Alignment _xAlign = com.top_logic.graphic.flow.data.Alignment.START;

	private com.top_logic.graphic.flow.data.Alignment _yAlign = com.top_logic.graphic.flow.data.Alignment.START;

	/**
	 * Creates a {@link Align_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Align#create()
	 */
	public Align_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.ALIGN;
	}

	@Override
	public final com.top_logic.graphic.flow.data.Alignment getXAlign() {
		return _xAlign;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setXAlign(com.top_logic.graphic.flow.data.Alignment value) {
		internalSetXAlign(value);
		return this;
	}

	/** Internal setter for {@link #getXAlign()} without chain call utility. */
	protected final void internalSetXAlign(com.top_logic.graphic.flow.data.Alignment value) {
		if (value == null) throw new IllegalArgumentException("Property 'xAlign' cannot be null.");
		_listener.beforeSet(this, X_ALIGN__PROP, value);
		_xAlign = value;
	}

	@Override
	public final com.top_logic.graphic.flow.data.Alignment getYAlign() {
		return _yAlign;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setYAlign(com.top_logic.graphic.flow.data.Alignment value) {
		internalSetYAlign(value);
		return this;
	}

	/** Internal setter for {@link #getYAlign()} without chain call utility. */
	protected final void internalSetYAlign(com.top_logic.graphic.flow.data.Alignment value) {
		if (value == null) throw new IllegalArgumentException("Property 'yAlign' cannot be null.");
		_listener.beforeSet(this, Y_ALIGN__PROP, value);
		_yAlign = value;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setContent(com.top_logic.graphic.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Align setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public String jsonType() {
		return ALIGN__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			X_ALIGN__PROP, 
			Y_ALIGN__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case X_ALIGN__PROP: return getXAlign();
			case Y_ALIGN__PROP: return getYAlign();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case X_ALIGN__PROP: internalSetXAlign((com.top_logic.graphic.flow.data.Alignment) value); break;
			case Y_ALIGN__PROP: internalSetYAlign((com.top_logic.graphic.flow.data.Alignment) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(X_ALIGN__PROP);
		getXAlign().writeTo(out);
		out.name(Y_ALIGN__PROP);
		getYAlign().writeTo(out);
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case X_ALIGN__PROP: {
				getXAlign().writeTo(out);
				break;
			}
			case Y_ALIGN__PROP: {
				getYAlign().writeTo(out);
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case X_ALIGN__PROP: setXAlign(com.top_logic.graphic.flow.data.Alignment.readAlignment(in)); break;
			case Y_ALIGN__PROP: setYAlign(com.top_logic.graphic.flow.data.Alignment.readAlignment(in)); break;
			default: super.readField(scope, in, field);
		}
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.Align} type. */
	public static final String ALIGN__XML_ELEMENT = "align";

	/** XML attribute or element name of a {@link #getXAlign} property. */
	private static final String X_ALIGN__XML_ATTR = "x-align";

	/** XML attribute or element name of a {@link #getYAlign} property. */
	private static final String Y_ALIGN__XML_ATTR = "y-align";

	@Override
	public String getXmlTagName() {
		return ALIGN__XML_ELEMENT;
	}

	/** Serializes all fields that are written as XML attributes. */
	@Override
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeAttributes(out);
		out.writeAttribute(X_ALIGN__XML_ATTR, getXAlign().protocolName());
		out.writeAttribute(Y_ALIGN__XML_ATTR, getYAlign().protocolName());
	}

	/** Serializes all fields that are written as XML elements. */
	@Override
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeElements(out);
		// No element fields.
	}

	/** Creates a new {@link com.top_logic.graphic.flow.data.Align} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Align_Impl readAlign_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		Align_Impl result = new Align_Impl();
		result.readContentXml(in);
		return result;
	}

	@Override
	protected void readFieldXmlAttribute(String name, String value) {
		switch (name) {
			case X_ALIGN__XML_ATTR: {
				setXAlign(com.top_logic.graphic.flow.data.Alignment.valueOfProtocol(value));
				break;
			}
			case Y_ALIGN__XML_ATTR: {
				setYAlign(com.top_logic.graphic.flow.data.Alignment.valueOfProtocol(value));
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
			case X_ALIGN__XML_ATTR: {
				setXAlign(com.top_logic.graphic.flow.data.Alignment.valueOfProtocol(in.getElementText()));
				break;
			}
			case Y_ALIGN__XML_ATTR: {
				setYAlign(com.top_logic.graphic.flow.data.Alignment.valueOfProtocol(in.getElementText()));
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
