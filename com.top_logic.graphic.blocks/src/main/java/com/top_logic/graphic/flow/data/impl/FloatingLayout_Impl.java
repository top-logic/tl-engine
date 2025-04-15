package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.FloatingLayout}.
 */
public class FloatingLayout_Impl extends com.top_logic.graphic.flow.data.impl.Box_Impl implements com.top_logic.graphic.flow.data.FloatingLayout {

	private final java.util.List<com.top_logic.graphic.flow.data.Box> _nodes = new de.haumacher.msgbuf.util.ReferenceList<>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.graphic.flow.data.Box element) {
			com.top_logic.graphic.flow.data.impl.Box_Impl added = (com.top_logic.graphic.flow.data.impl.Box_Impl) element;
			com.top_logic.graphic.flow.data.Box oldContainer = added.getParent();
			if (oldContainer != null && oldContainer != this) {
				throw new IllegalStateException("Object may not be part of two different containers.");
			}
			_listener.beforeAdd(FloatingLayout_Impl.this, NODES__PROP, index, element);
			added.internalSetParent(FloatingLayout_Impl.this);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.graphic.flow.data.Box element) {
			com.top_logic.graphic.flow.data.impl.Box_Impl removed = (com.top_logic.graphic.flow.data.impl.Box_Impl) element;
			removed.internalSetParent(null);
			_listener.afterRemove(FloatingLayout_Impl.this, NODES__PROP, index, element);
		}
	};

	/**
	 * Creates a {@link FloatingLayout_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.FloatingLayout#create()
	 */
	public FloatingLayout_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.FLOATING_LAYOUT;
	}

	@Override
	public final java.util.List<com.top_logic.graphic.flow.data.Box> getNodes() {
		return _nodes;
	}

	@Override
	public com.top_logic.graphic.flow.data.FloatingLayout setNodes(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value) {
		internalSetNodes(value);
		return this;
	}

	/** Internal setter for {@link #getNodes()} without chain call utility. */
	protected final void internalSetNodes(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value) {
		if (value == null) throw new IllegalArgumentException("Property 'nodes' cannot be null.");
		_nodes.clear();
		_nodes.addAll(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.FloatingLayout addNode(com.top_logic.graphic.flow.data.Box value) {
		internalAddNode(value);
		return this;
	}

	/** Implementation of {@link #addNode(com.top_logic.graphic.flow.data.Box)} without chain call utility. */
	protected final void internalAddNode(com.top_logic.graphic.flow.data.Box value) {
		_nodes.add(value);
	}

	@Override
	public final void removeNode(com.top_logic.graphic.flow.data.Box value) {
		_nodes.remove(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.FloatingLayout setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.FloatingLayout setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.FloatingLayout setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.FloatingLayout setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public String jsonType() {
		return FLOATING_LAYOUT__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			NODES__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case NODES__PROP: return getNodes();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NODES__PROP: internalSetNodes(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.graphic.flow.data.Box.class, value)); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		out.name(NODES__PROP);
		out.beginArray();
		for (com.top_logic.graphic.flow.data.Box x : getNodes()) {
			x.writeTo(scope, out);
		}
		out.endArray();
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case NODES__PROP: {
				out.beginArray();
				for (com.top_logic.graphic.flow.data.Box x : getNodes()) {
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
			case NODES__PROP: {
				java.util.List<com.top_logic.graphic.flow.data.Box> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.graphic.flow.data.Box.readBox(scope, in));
				}
				in.endArray();
				setNodes(newValue);
			}
			break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case NODES__PROP: {
				((com.top_logic.graphic.flow.data.Box) element).writeTo(scope, out);
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NODES__PROP: {
				return com.top_logic.graphic.flow.data.Box.readBox(scope, in);
			}
			default: return super.readElement(scope, in, field);
		}
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.FloatingLayout} type. */
	public static final String FLOATING_LAYOUT__XML_ELEMENT = "floating-layout";

	/** XML attribute or element name of a {@link #getNodes} property. */
	private static final String NODES__XML_ATTR = "nodes";

	@Override
	public String getXmlTagName() {
		return FLOATING_LAYOUT__XML_ELEMENT;
	}

	/** Serializes all fields that are written as XML attributes. */
	@Override
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeAttributes(out);
	}

	/** Serializes all fields that are written as XML elements. */
	@Override
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeElements(out);
		out.writeStartElement(NODES__XML_ATTR);
		for (com.top_logic.graphic.flow.data.Box element : getNodes()) {
			element.writeTo(out);
		}
		out.writeEndElement();
	}

	/** Creates a new {@link com.top_logic.graphic.flow.data.FloatingLayout} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static FloatingLayout_Impl readFloatingLayout_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		FloatingLayout_Impl result = new FloatingLayout_Impl();
		result.readContentXml(in);
		return result;
	}

	@Override
	protected void readFieldXmlAttribute(String name, String value) {
		switch (name) {
			default: {
				super.readFieldXmlAttribute(name, value);
			}
		}
	}

	@Override
	protected void readFieldXmlElement(javax.xml.stream.XMLStreamReader in, String localName) throws javax.xml.stream.XMLStreamException {
		switch (localName) {
			case NODES__XML_ATTR: {
				internalReadNodesListXml(in);
				break;
			}
			default: {
				super.readFieldXmlElement(in, localName);
			}
		}
	}

	private void internalReadNodesListXml(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		while (true) {
			int event = in.nextTag();
			if (event == javax.xml.stream.XMLStreamConstants.END_ELEMENT) {
				break;
			}

			addNode(com.top_logic.graphic.flow.data.impl.Box_Impl.readBox_XmlContent(in));
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
