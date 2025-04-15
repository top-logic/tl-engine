package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.TreeConnection}.
 */
public class TreeConnection_Impl extends com.top_logic.graphic.flow.data.impl.Widget_Impl implements com.top_logic.graphic.flow.data.TreeConnection {

	private com.top_logic.graphic.flow.data.TreeLayout _owner = null;

	private com.top_logic.graphic.flow.data.TreeConnector _parent = null;

	private final java.util.List<com.top_logic.graphic.flow.data.TreeConnector> _children = new de.haumacher.msgbuf.util.ReferenceList<>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.graphic.flow.data.TreeConnector element) {
			com.top_logic.graphic.flow.data.impl.TreeConnector_Impl added = (com.top_logic.graphic.flow.data.impl.TreeConnector_Impl) element;
			com.top_logic.graphic.flow.data.TreeConnection oldContainer = added.getConnection();
			if (oldContainer != null && oldContainer != this) {
				throw new IllegalStateException("Object may not be part of two different containers.");
			}
			_listener.beforeAdd(TreeConnection_Impl.this, CHILDREN__PROP, index, element);
			added.internalSetConnection(TreeConnection_Impl.this);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.graphic.flow.data.TreeConnector element) {
			com.top_logic.graphic.flow.data.impl.TreeConnector_Impl removed = (com.top_logic.graphic.flow.data.impl.TreeConnector_Impl) element;
			removed.internalSetConnection(null);
			_listener.afterRemove(TreeConnection_Impl.this, CHILDREN__PROP, index, element);
		}
	};

	private double _barPosition = 0.0d;

	/**
	 * Creates a {@link TreeConnection_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.TreeConnection#create()
	 */
	public TreeConnection_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.TREE_CONNECTION;
	}

	@Override
	public final com.top_logic.graphic.flow.data.TreeLayout getOwner() {
		return _owner;
	}

	/**
	 * Internal setter for updating derived field.
	 */
	com.top_logic.graphic.flow.data.TreeConnection setOwner(com.top_logic.graphic.flow.data.TreeLayout value) {
		internalSetOwner(value);
		return this;
	}

	/** Internal setter for {@link #getOwner()} without chain call utility. */
	protected final void internalSetOwner(com.top_logic.graphic.flow.data.TreeLayout value) {
		_listener.beforeSet(this, OWNER__PROP, value);
		if (value != null && _owner != null) {
			throw new IllegalStateException("Object may not be part of two different containers.");
		}
		_owner = value;
	}

	@Override
	public final boolean hasOwner() {
		return _owner != null;
	}

	@Override
	public final com.top_logic.graphic.flow.data.TreeConnector getParent() {
		return _parent;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnection setParent(com.top_logic.graphic.flow.data.TreeConnector value) {
		internalSetParent(value);
		return this;
	}

	/** Internal setter for {@link #getParent()} without chain call utility. */
	protected final void internalSetParent(com.top_logic.graphic.flow.data.TreeConnector value) {
		com.top_logic.graphic.flow.data.impl.TreeConnector_Impl before = (com.top_logic.graphic.flow.data.impl.TreeConnector_Impl) _parent;
		com.top_logic.graphic.flow.data.impl.TreeConnector_Impl after = (com.top_logic.graphic.flow.data.impl.TreeConnector_Impl) value;
		if (after != null) {
			com.top_logic.graphic.flow.data.TreeConnection oldContainer = after.getConnection();
			if (oldContainer != null && oldContainer != this) {
				throw new IllegalStateException("Object may not be part of two different containers.");
			}
		}
		_listener.beforeSet(this, PARENT__PROP, value);
		if (before != null) {
			before.internalSetConnection(null);
		}
		_parent = value;
		if (after != null) {
			after.internalSetConnection(this);
		}
	}

	@Override
	public final boolean hasParent() {
		return _parent != null;
	}

	@Override
	public final java.util.List<com.top_logic.graphic.flow.data.TreeConnector> getChildren() {
		return _children;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnection setChildren(java.util.List<? extends com.top_logic.graphic.flow.data.TreeConnector> value) {
		internalSetChildren(value);
		return this;
	}

	/** Internal setter for {@link #getChildren()} without chain call utility. */
	protected final void internalSetChildren(java.util.List<? extends com.top_logic.graphic.flow.data.TreeConnector> value) {
		if (value == null) throw new IllegalArgumentException("Property 'children' cannot be null.");
		_children.clear();
		_children.addAll(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnection addChildren(com.top_logic.graphic.flow.data.TreeConnector value) {
		internalAddChildren(value);
		return this;
	}

	/** Implementation of {@link #addChildren(com.top_logic.graphic.flow.data.TreeConnector)} without chain call utility. */
	protected final void internalAddChildren(com.top_logic.graphic.flow.data.TreeConnector value) {
		_children.add(value);
	}

	@Override
	public final void removeChildren(com.top_logic.graphic.flow.data.TreeConnector value) {
		_children.remove(value);
	}

	@Override
	public final double getBarPosition() {
		return _barPosition;
	}

	@Override
	public com.top_logic.graphic.flow.data.TreeConnection setBarPosition(double value) {
		internalSetBarPosition(value);
		return this;
	}

	/** Internal setter for {@link #getBarPosition()} without chain call utility. */
	protected final void internalSetBarPosition(double value) {
		_listener.beforeSet(this, BAR_POSITION__PROP, value);
		_barPosition = value;
	}

	@Override
	public String jsonType() {
		return TREE_CONNECTION__TYPE;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			OWNER__PROP, 
			PARENT__PROP, 
			CHILDREN__PROP, 
			BAR_POSITION__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case OWNER__PROP: return getOwner();
			case PARENT__PROP: return getParent();
			case CHILDREN__PROP: return getChildren();
			case BAR_POSITION__PROP: return getBarPosition();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case PARENT__PROP: internalSetParent((com.top_logic.graphic.flow.data.TreeConnector) value); break;
			case CHILDREN__PROP: internalSetChildren(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.graphic.flow.data.TreeConnector.class, value)); break;
			case BAR_POSITION__PROP: internalSetBarPosition((double) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(scope, out);
		if (hasParent()) {
			out.name(PARENT__PROP);
			getParent().writeTo(scope, out);
		}
		out.name(CHILDREN__PROP);
		out.beginArray();
		for (com.top_logic.graphic.flow.data.TreeConnector x : getChildren()) {
			x.writeTo(scope, out);
		}
		out.endArray();
		out.name(BAR_POSITION__PROP);
		out.value(getBarPosition());
	}

	@Override
	public void writeFieldValue(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field) throws java.io.IOException {
		switch (field) {
			case OWNER__PROP: {
				if (hasOwner()) {
					getOwner().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case PARENT__PROP: {
				if (hasParent()) {
					getParent().writeTo(scope, out);
				} else {
					out.nullValue();
				}
				break;
			}
			case CHILDREN__PROP: {
				out.beginArray();
				for (com.top_logic.graphic.flow.data.TreeConnector x : getChildren()) {
					x.writeTo(scope, out);
				}
				out.endArray();
				break;
			}
			case BAR_POSITION__PROP: {
				out.value(getBarPosition());
				break;
			}
			default: super.writeFieldValue(scope, out, field);
		}
	}

	@Override
	public void readField(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case PARENT__PROP: setParent(com.top_logic.graphic.flow.data.TreeConnector.readTreeConnector(scope, in)); break;
			case CHILDREN__PROP: {
				java.util.List<com.top_logic.graphic.flow.data.TreeConnector> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.graphic.flow.data.TreeConnector.readTreeConnector(scope, in));
				}
				in.endArray();
				setChildren(newValue);
			}
			break;
			case BAR_POSITION__PROP: setBarPosition(in.nextDouble()); break;
			default: super.readField(scope, in, field);
		}
	}

	@Override
	public void writeElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonWriter out, String field, Object element) throws java.io.IOException {
		switch (field) {
			case CHILDREN__PROP: {
				((com.top_logic.graphic.flow.data.TreeConnector) element).writeTo(scope, out);
				break;
			}
			default: super.writeElement(scope, out, field, element);
		}
	}

	@Override
	public Object readElement(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CHILDREN__PROP: {
				return com.top_logic.graphic.flow.data.TreeConnector.readTreeConnector(scope, in);
			}
			default: return super.readElement(scope, in, field);
		}
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.TreeConnection} type. */
	public static final String TREE_CONNECTION__XML_ELEMENT = "tree-connection";

	/** XML attribute or element name of a {@link #getOwner} property. */
	private static final String OWNER__XML_ATTR = "owner";

	/** XML attribute or element name of a {@link #getParent} property. */
	private static final String PARENT__XML_ATTR = "parent";

	/** XML attribute or element name of a {@link #getChildren} property. */
	private static final String CHILDREN__XML_ATTR = "children";

	/** XML attribute or element name of a {@link #getBarPosition} property. */
	private static final String BAR_POSITION__XML_ATTR = "bar-position";

	@Override
	public String getXmlTagName() {
		return TREE_CONNECTION__XML_ELEMENT;
	}

	/** Serializes all fields that are written as XML attributes. */
	@Override
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeAttributes(out);
		out.writeAttribute(BAR_POSITION__XML_ATTR, Double.toString(getBarPosition()));
	}

	/** Serializes all fields that are written as XML elements. */
	@Override
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeElements(out);
		if (hasOwner()) {
			out.writeStartElement(OWNER__XML_ATTR);
			getOwner().writeContent(out);
			out.writeEndElement();
		}
		if (hasParent()) {
			out.writeStartElement(PARENT__XML_ATTR);
			getParent().writeContent(out);
			out.writeEndElement();
		}
		out.writeStartElement(CHILDREN__XML_ATTR);
		for (com.top_logic.graphic.flow.data.TreeConnector element : getChildren()) {
			element.writeTo(out);
		}
		out.writeEndElement();
	}

	/** Creates a new {@link com.top_logic.graphic.flow.data.TreeConnection} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static TreeConnection_Impl readTreeConnection_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		TreeConnection_Impl result = new TreeConnection_Impl();
		result.readContentXml(in);
		return result;
	}

	@Override
	protected void readFieldXmlAttribute(String name, String value) {
		switch (name) {
			case BAR_POSITION__XML_ATTR: {
				setBarPosition(Double.parseDouble(value));
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
			case OWNER__XML_ATTR: {
				setOwner(com.top_logic.graphic.flow.data.impl.TreeLayout_Impl.readTreeLayout_XmlContent(in));
				break;
			}
			case PARENT__XML_ATTR: {
				setParent(com.top_logic.graphic.flow.data.impl.TreeConnector_Impl.readTreeConnector_XmlContent(in));
				break;
			}
			case CHILDREN__XML_ATTR: {
				internalReadChildrenListXml(in);
				break;
			}
			case BAR_POSITION__XML_ATTR: {
				setBarPosition(Double.parseDouble(in.getElementText()));
				break;
			}
			default: {
				super.readFieldXmlElement(in, localName);
			}
		}
	}

	private void internalReadChildrenListXml(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		while (true) {
			int event = in.nextTag();
			if (event == javax.xml.stream.XMLStreamConstants.END_ELEMENT) {
				break;
			}

			addChildren(com.top_logic.graphic.flow.data.impl.TreeConnector_Impl.readTreeConnector_XmlContent(in));
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Widget.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
