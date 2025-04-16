package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.TreeLayout}.
 */
public class TreeLayout_Impl extends com.top_logic.graphic.flow.data.impl.FloatingLayout_Impl implements com.top_logic.graphic.flow.data.TreeLayout {

	private boolean _compact = false;

	private com.top_logic.graphic.flow.data.DiagramDirection _direction = com.top_logic.graphic.flow.data.DiagramDirection.LTR;

	private double _gapX = 30;

	private double _gapY = 30;

	private String _strokeStyle = "black";

	private double _thickness = 1.0;

	private final java.util.List<com.top_logic.graphic.flow.data.TreeConnection> _connections = new de.haumacher.msgbuf.util.ReferenceList<>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.graphic.flow.data.TreeConnection element) {
			com.top_logic.graphic.flow.data.impl.TreeConnection_Impl added = (com.top_logic.graphic.flow.data.impl.TreeConnection_Impl) element;
			com.top_logic.graphic.flow.data.TreeLayout oldContainer = added.getOwner();
			if (oldContainer != null && oldContainer != this) {
				throw new IllegalStateException("Object may not be part of two different containers.");
			}
			_listener.beforeAdd(TreeLayout_Impl.this, CONNECTIONS__PROP, index, element);
			added.internalSetOwner(TreeLayout_Impl.this);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.graphic.flow.data.TreeConnection element) {
			com.top_logic.graphic.flow.data.impl.TreeConnection_Impl removed = (com.top_logic.graphic.flow.data.impl.TreeConnection_Impl) element;
			removed.internalSetOwner(null);
			_listener.afterRemove(TreeLayout_Impl.this, CONNECTIONS__PROP, index, element);
		}
	};

	/**
	 * Creates a {@link TreeLayout_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.TreeLayout#create()
	 */
	public TreeLayout_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.TREE_LAYOUT;
	}

	@Override
	public final boolean isCompact() {
		return _compact;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeLayout setCompact(boolean value) {
		internalSetCompact(value);
		return this;
	}

	/** Internal setter for {@link #isCompact()} without chain call utility. */
	protected final void internalSetCompact(boolean value) {
		_listener.beforeSet(this, COMPACT__PROP, value);
		_compact = value;
	}

	@Override
	public final com.top_logic.graphic.flow.data.DiagramDirection getDirection() {
		return _direction;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeLayout setDirection(com.top_logic.graphic.flow.data.DiagramDirection value) {
		internalSetDirection(value);
		return this;
	}

	/** Internal setter for {@link #getDirection()} without chain call utility. */
	protected final void internalSetDirection(com.top_logic.graphic.flow.data.DiagramDirection value) {
		if (value == null) throw new IllegalArgumentException("Property 'direction' cannot be null.");
		_listener.beforeSet(this, DIRECTION__PROP, value);
		_direction = value;
	}

	@Override
	public final double getGapX() {
		return _gapX;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeLayout setGapX(double value) {
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
	public com.top_logic.graphic.flow.data.TreeLayout setGapY(double value) {
		internalSetGapY(value);
		return this;
	}

	/** Internal setter for {@link #getGapY()} without chain call utility. */
	protected final void internalSetGapY(double value) {
		_listener.beforeSet(this, GAP_Y__PROP, value);
		_gapY = value;
	}

	@Override
	public final String getStrokeStyle() {
		return _strokeStyle;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeLayout setStrokeStyle(String value) {
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
	public com.top_logic.graphic.flow.data.TreeLayout setThickness(double value) {
		internalSetThickness(value);
		return this;
	}

	/** Internal setter for {@link #getThickness()} without chain call utility. */
	protected final void internalSetThickness(double value) {
		_listener.beforeSet(this, THICKNESS__PROP, value);
		_thickness = value;
	}

	@Override
	public final java.util.List<com.top_logic.graphic.flow.data.TreeConnection> getConnections() {
		return _connections;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeLayout setConnections(java.util.List<? extends com.top_logic.graphic.flow.data.TreeConnection> value) {
		internalSetConnections(value);
		return this;
	}

	/** Internal setter for {@link #getConnections()} without chain call utility. */
	protected final void internalSetConnections(java.util.List<? extends com.top_logic.graphic.flow.data.TreeConnection> value) {
		if (value == null) throw new IllegalArgumentException("Property 'connections' cannot be null.");
		_connections.clear();
		_connections.addAll(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeLayout addConnection(com.top_logic.graphic.flow.data.TreeConnection value) {
		internalAddConnection(value);
		return this;
	}

	/** Implementation of {@link #addConnection(com.top_logic.graphic.flow.data.TreeConnection)} without chain call utility. */
	protected final void internalAddConnection(com.top_logic.graphic.flow.data.TreeConnection value) {
		_connections.add(value);
	}

	@Override
	public final void removeConnection(com.top_logic.graphic.flow.data.TreeConnection value) {
		_connections.remove(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeLayout setNodes(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value) {
		internalSetNodes(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeLayout addNode(com.top_logic.graphic.flow.data.Box value) {
		internalAddNode(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeLayout setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeLayout setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeLayout setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeLayout setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public String jsonType() {
		return TREE_LAYOUT__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			COMPACT__PROP, 
			DIRECTION__PROP, 
			GAP_X__PROP, 
			GAP_Y__PROP, 
			STROKE_STYLE__PROP, 
			THICKNESS__PROP, 
			CONNECTIONS__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case COMPACT__PROP: return isCompact();
			case DIRECTION__PROP: return getDirection();
			case GAP_X__PROP: return getGapX();
			case GAP_Y__PROP: return getGapY();
			case STROKE_STYLE__PROP: return getStrokeStyle();
			case THICKNESS__PROP: return getThickness();
			case CONNECTIONS__PROP: return getConnections();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case COMPACT__PROP: internalSetCompact((boolean) value); break;
			case DIRECTION__PROP: internalSetDirection((com.top_logic.graphic.flow.data.DiagramDirection) value); break;
			case GAP_X__PROP: internalSetGapX((double) value); break;
			case GAP_Y__PROP: internalSetGapY((double) value); break;
			case STROKE_STYLE__PROP: internalSetStrokeStyle((String) value); break;
			case THICKNESS__PROP: internalSetThickness((double) value); break;
			case CONNECTIONS__PROP: internalSetConnections(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.graphic.flow.data.TreeConnection.class, value)); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(COMPACT__PROP);
		out.value(isCompact());
		out.name(DIRECTION__PROP);
		getDirection().writeTo(out);
		out.name(GAP_X__PROP);
		out.value(getGapX());
		out.name(GAP_Y__PROP);
		out.value(getGapY());
		out.name(STROKE_STYLE__PROP);
		out.value(getStrokeStyle());
		out.name(THICKNESS__PROP);
		out.value(getThickness());
		out.name(CONNECTIONS__PROP);
		out.beginArray();
		for (com.top_logic.graphic.flow.data.TreeConnection x : getConnections()) {
			x.writeTo(scope, out);
		}
		out.endArray();
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case COMPACT__PROP: {
				out.value(isCompact());
				break;
			}
			case DIRECTION__PROP: {
				getDirection().writeTo(out);
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
			case STROKE_STYLE__PROP: {
				out.value(getStrokeStyle());
				break;
			}
			case THICKNESS__PROP: {
				out.value(getThickness());
				break;
			}
			case CONNECTIONS__PROP: {
				out.beginArray();
				for (com.top_logic.graphic.flow.data.TreeConnection x : getConnections()) {
					x.writeTo(scope, out);
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
			case COMPACT__PROP: setCompact(in.nextBoolean()); break;
			case DIRECTION__PROP: setDirection(com.top_logic.graphic.flow.data.DiagramDirection.readDiagramDirection(in)); break;
			case GAP_X__PROP: setGapX(in.nextDouble()); break;
			case GAP_Y__PROP: setGapY(in.nextDouble()); break;
			case STROKE_STYLE__PROP: setStrokeStyle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case THICKNESS__PROP: setThickness(in.nextDouble()); break;
			case CONNECTIONS__PROP: {
				java.util.List<com.top_logic.graphic.flow.data.TreeConnection> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.graphic.flow.data.TreeConnection.readTreeConnection(scope, in));
				}
				in.endArray();
				setConnections(newValue);
			}
			break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case CONNECTIONS__PROP: {
				((com.top_logic.graphic.flow.data.TreeConnection) element).writeTo(scope, out);
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CONNECTIONS__PROP: {
				return com.top_logic.graphic.flow.data.TreeConnection.readTreeConnection(scope, in);
			}
			default: return super.readElement(scope, in, field);
		}
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.TreeLayout} type. */
	public static final String TREE_LAYOUT__XML_ELEMENT = "tree-layout";

	/** XML attribute or element name of a {@link #isCompact} property. */
	private static final String COMPACT__XML_ATTR = "compact";

	/** XML attribute or element name of a {@link #getDirection} property. */
	private static final String DIRECTION__XML_ATTR = "direction";

	/** XML attribute or element name of a {@link #getGapX} property. */
	private static final String GAP_X__XML_ATTR = "gap-x";

	/** XML attribute or element name of a {@link #getGapY} property. */
	private static final String GAP_Y__XML_ATTR = "gap-y";

	/** XML attribute or element name of a {@link #getStrokeStyle} property. */
	private static final String STROKE_STYLE__XML_ATTR = "stroke-style";

	/** XML attribute or element name of a {@link #getThickness} property. */
	private static final String THICKNESS__XML_ATTR = "thickness";

	/** XML attribute or element name of a {@link #getConnections} property. */
	private static final String CONNECTIONS__XML_ATTR = "connections";

	@Override
	public String getXmlTagName() {
		return TREE_LAYOUT__XML_ELEMENT;
	}

	/** Serializes all fields that are written as XML attributes. */
	@Override
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeAttributes(out);
		out.writeAttribute(COMPACT__XML_ATTR, Boolean.toString(isCompact()));
		out.writeAttribute(DIRECTION__XML_ATTR, getDirection().protocolName());
		out.writeAttribute(GAP_X__XML_ATTR, Double.toString(getGapX()));
		out.writeAttribute(GAP_Y__XML_ATTR, Double.toString(getGapY()));
		out.writeAttribute(STROKE_STYLE__XML_ATTR, getStrokeStyle());
		out.writeAttribute(THICKNESS__XML_ATTR, Double.toString(getThickness()));
	}

	/** Serializes all fields that are written as XML elements. */
	@Override
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeElements(out);
		out.writeStartElement(CONNECTIONS__XML_ATTR);
		for (com.top_logic.graphic.flow.data.TreeConnection element : getConnections()) {
			element.writeTo(out);
		}
		out.writeEndElement();
	}

	/** Creates a new {@link com.top_logic.graphic.flow.data.TreeLayout} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static TreeLayout_Impl readTreeLayout_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		TreeLayout_Impl result = new TreeLayout_Impl();
		result.readContentXml(in);
		return result;
	}

	@Override
	protected void readFieldXmlAttribute(String name, String value) {
		switch (name) {
			case COMPACT__XML_ATTR: {
				setCompact(Boolean.parseBoolean(value));
				break;
			}
			case DIRECTION__XML_ATTR: {
				setDirection(com.top_logic.graphic.flow.data.DiagramDirection.valueOfProtocol(value));
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
			case STROKE_STYLE__XML_ATTR: {
				setStrokeStyle(value);
				break;
			}
			case THICKNESS__XML_ATTR: {
				setThickness(Double.parseDouble(value));
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
			case COMPACT__XML_ATTR: {
				setCompact(Boolean.parseBoolean(in.getElementText()));
				break;
			}
			case DIRECTION__XML_ATTR: {
				setDirection(com.top_logic.graphic.flow.data.DiagramDirection.valueOfProtocol(in.getElementText()));
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
			case STROKE_STYLE__XML_ATTR: {
				setStrokeStyle(in.getElementText());
				break;
			}
			case THICKNESS__XML_ATTR: {
				setThickness(Double.parseDouble(in.getElementText()));
				break;
			}
			case CONNECTIONS__XML_ATTR: {
				internalReadConnectionsListXml(in);
				break;
			}
			default: {
				super.readFieldXmlElement(in, localName);
			}
		}
	}

	private void internalReadConnectionsListXml(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		while (true) {
			int event = in.nextTag();
			if (event == javax.xml.stream.XMLStreamConstants.END_ELEMENT) {
				break;
			}

			addConnection(com.top_logic.graphic.flow.data.impl.TreeConnection_Impl.readTreeConnection_XmlContent(in));
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
