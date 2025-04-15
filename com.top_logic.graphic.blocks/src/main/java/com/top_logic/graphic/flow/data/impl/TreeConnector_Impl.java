package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.TreeConnector}.
 */
public class TreeConnector_Impl extends com.top_logic.graphic.flow.data.impl.Widget_Impl implements com.top_logic.graphic.flow.data.TreeConnector {

	private com.top_logic.graphic.flow.data.TreeConnection _connection = null;

	private com.top_logic.graphic.flow.data.Box _anchor = null;

	private double _connectPosition = 0.0d;

	/**
	 * Creates a {@link TreeConnector_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.TreeConnector#create()
	 */
	public TreeConnector_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.TREE_CONNECTOR;
	}

	@Override
	public final com.top_logic.graphic.flow.data.TreeConnection getConnection() {
		return _connection;
	}

	/**
	 * Internal setter for updating derived field.
	 */
	com.top_logic.graphic.flow.data.TreeConnector setConnection(com.top_logic.graphic.flow.data.TreeConnection value) {
		internalSetConnection(value);
		return this;
	}

	/** Internal setter for {@link #getConnection()} without chain call utility. */
	protected final void internalSetConnection(com.top_logic.graphic.flow.data.TreeConnection value) {
		_listener.beforeSet(this, CONNECTION__PROP, value);
		if (value != null && _connection != null) {
			throw new IllegalStateException("Object may not be part of two different containers.");
		}
		_connection = value;
	}

	@Override
	public final boolean hasConnection() {
		return _connection != null;
	}

	@Override
	public final com.top_logic.graphic.flow.data.Box getAnchor() {
		return _anchor;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnector setAnchor(com.top_logic.graphic.flow.data.Box value) {
		internalSetAnchor(value);
		return this;
	}

	/** Internal setter for {@link #getAnchor()} without chain call utility. */
	protected final void internalSetAnchor(com.top_logic.graphic.flow.data.Box value) {
		_listener.beforeSet(this, ANCHOR__PROP, value);
		_anchor = value;
	}

	@Override
	public final boolean hasAnchor() {
		return _anchor != null;
	}

	@Override
	public final double getConnectPosition() {
		return _connectPosition;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnector setConnectPosition(double value) {
		internalSetConnectPosition(value);
		return this;
	}

	/** Internal setter for {@link #getConnectPosition()} without chain call utility. */
	protected final void internalSetConnectPosition(double value) {
		_listener.beforeSet(this, CONNECT_POSITION__PROP, value);
		_connectPosition = value;
	}

	@Override
	public String jsonType() {
		return TREE_CONNECTOR__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			CONNECTION__PROP, 
			ANCHOR__PROP, 
			CONNECT_POSITION__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case CONNECTION__PROP: return getConnection();
			case ANCHOR__PROP: return getAnchor();
			case CONNECT_POSITION__PROP: return getConnectPosition();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ANCHOR__PROP: internalSetAnchor((com.top_logic.graphic.flow.data.Box) value); break;
			case CONNECT_POSITION__PROP: internalSetConnectPosition((double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		if (hasAnchor()) {
			out.name(ANCHOR__PROP);
			getAnchor().writeTo(scope, out);
		}
		out.name(CONNECT_POSITION__PROP);
		out.value(getConnectPosition());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case CONNECTION__PROP: {
				if (hasConnection()) {
					getConnection().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case ANCHOR__PROP: {
				if (hasAnchor()) {
					getAnchor().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case CONNECT_POSITION__PROP: {
				out.value(getConnectPosition());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ANCHOR__PROP: setAnchor(com.top_logic.graphic.flow.data.Box.readBox(scope, in)); break;
			case CONNECT_POSITION__PROP: setConnectPosition(in.nextDouble()); break;
			default: super.readField(scope, in, field);
		}
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.TreeConnector} type. */
	public static final String TREE_CONNECTOR__XML_ELEMENT = "tree-connector";

	/** XML attribute or element name of a {@link #getConnection} property. */
	private static final String CONNECTION__XML_ATTR = "connection";

	/** XML attribute or element name of a {@link #getAnchor} property. */
	private static final String ANCHOR__XML_ATTR = "anchor";

	/** XML attribute or element name of a {@link #getConnectPosition} property. */
	private static final String CONNECT_POSITION__XML_ATTR = "connect-position";

	@Override
	public String getXmlTagName() {
		return TREE_CONNECTOR__XML_ELEMENT;
	}

	/** Serializes all fields that are written as XML attributes. */
	@Override
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeAttributes(out);
		out.writeAttribute(CONNECT_POSITION__XML_ATTR, Double.toString(getConnectPosition()));
	}

	/** Serializes all fields that are written as XML elements. */
	@Override
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeElements(out);
		if (hasConnection()) {
			out.writeStartElement(CONNECTION__XML_ATTR);
			getConnection().writeContent(out);
			out.writeEndElement();
		}
		if (hasAnchor()) {
			out.writeStartElement(ANCHOR__XML_ATTR);
			getAnchor().writeTo(out);
			out.writeEndElement();
		}
	}

	/** Creates a new {@link com.top_logic.graphic.flow.data.TreeConnector} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static TreeConnector_Impl readTreeConnector_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		TreeConnector_Impl result = new TreeConnector_Impl();
		result.readContentXml(in);
		return result;
	}

	@Override
	protected void readFieldXmlAttribute(String name, String value) {
		switch (name) {
			case CONNECT_POSITION__XML_ATTR: {
				setConnectPosition(Double.parseDouble(value));
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
			case CONNECTION__XML_ATTR: {
				setConnection(com.top_logic.graphic.flow.data.impl.TreeConnection_Impl.readTreeConnection_XmlContent(in));
				break;
			}
			case ANCHOR__XML_ATTR: {
				in.nextTag();
				setAnchor(com.top_logic.graphic.flow.data.impl.Box_Impl.readBox_XmlContent(in));
				internalSkipUntilMatchingEndElement(in);
				break;
			}
			case CONNECT_POSITION__XML_ATTR: {
				setConnectPosition(Double.parseDouble(in.getElementText()));
				break;
			}
			default: {
				super.readFieldXmlElement(in, localName);
			}
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Widget.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
