package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Layout}.
 */
public abstract class Layout_Impl extends com.top_logic.graphic.flow.data.impl.Box_Impl implements com.top_logic.graphic.flow.data.Layout {

	private final java.util.List<com.top_logic.graphic.flow.data.Box> _contents = new de.haumacher.msgbuf.util.ReferenceList<>() {
		@Override
		protected void beforeAdd(int index, com.top_logic.graphic.flow.data.Box element) {
			_listener.beforeAdd(Layout_Impl.this, CONTENTS__PROP, index, element);
		}

		@Override
		protected void afterRemove(int index, com.top_logic.graphic.flow.data.Box element) {
			_listener.afterRemove(Layout_Impl.this, CONTENTS__PROP, index, element);
		}
	};

	/**
	 * Creates a {@link Layout_Impl} instance.
	 */
	public Layout_Impl() {
		super();
	}

	@Override
	public final java.util.List<com.top_logic.graphic.flow.data.Box> getContents() {
		return _contents;
	}

	@Override
	public com.top_logic.graphic.flow.data.Layout setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value) {
		internalSetContents(value);
		return this;
	}

	/** Internal setter for {@link #getContents()} without chain call utility. */
	protected final void internalSetContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value) {
		if (value == null) throw new IllegalArgumentException("Property 'contents' cannot be null.");
		_contents.clear();
		_contents.addAll(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.Layout addContent(com.top_logic.graphic.flow.data.Box value) {
		internalAddContent(value);
		return this;
	}

	/** Implementation of {@link #addContent(com.top_logic.graphic.flow.data.Box)} without chain call utility. */
	protected final void internalAddContent(com.top_logic.graphic.flow.data.Box value) {
		_contents.add(value);
	}

	@Override
	public final void removeContent(com.top_logic.graphic.flow.data.Box value) {
		_contents.remove(value);
	}

	@Override
	public com.top_logic.graphic.flow.data.Layout setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Layout setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Layout setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Layout setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			CONTENTS__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case CONTENTS__PROP: return getContents();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case CONTENTS__PROP: internalSetContents(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.graphic.flow.data.Box.class, value)); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(CONTENTS__PROP);
		out.beginArray();
		for (com.top_logic.graphic.flow.data.Box x : getContents()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CONTENTS__PROP: {
				java.util.List<com.top_logic.graphic.flow.data.Box> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.graphic.flow.data.Box.readBox(in));
				}
				in.endArray();
				setContents(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(CONTENTS__ID);
		{
			java.util.List<com.top_logic.graphic.flow.data.Box> values = getContents();
			out.beginArray(de.haumacher.msgbuf.binary.DataType.OBJECT, values.size());
			for (com.top_logic.graphic.flow.data.Box x : values) {
				x.writeTo(out);
			}
			out.endArray();
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.binary.DataReader in, int field) throws java.io.IOException {
		switch (field) {
			case CONTENTS__ID: {
				in.beginArray();
				while (in.hasNext()) {
					addContent(com.top_logic.graphic.flow.data.Box.readBox(in));
				}
				in.endArray();
			}
			break;
			default: super.readField(in, field);
		}
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.Layout} type. */
	public static final String LAYOUT__XML_ELEMENT = "layout";

	/** XML attribute or element name of a {@link #getContents} property. */
	private static final String CONTENTS__XML_ATTR = "contents";

	/** Serializes all fields that are written as XML attributes. */
	@Override
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeAttributes(out);
	}

	/** Serializes all fields that are written as XML elements. */
	@Override
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeElements(out);
		out.writeStartElement(CONTENTS__XML_ATTR);
		for (com.top_logic.graphic.flow.data.Box element : getContents()) {
			element.writeTo(out);
		}
		out.writeEndElement();
	}

	/** Creates a new {@link com.top_logic.graphic.flow.data.Layout} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Layout_Impl readLayout_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		switch (in.getLocalName()) {
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
			default: {
				super.readFieldXmlAttribute(name, value);
			}
		}
	}

	@Override
	protected void readFieldXmlElement(javax.xml.stream.XMLStreamReader in, String localName) throws javax.xml.stream.XMLStreamException {
		switch (localName) {
			case CONTENTS__XML_ATTR: {
				internalReadContentsListXml(in);
				break;
			}
			default: {
				super.readFieldXmlElement(in, localName);
			}
		}
	}

	private void internalReadContentsListXml(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		while (true) {
			int event = in.nextTag();
			if (event == javax.xml.stream.XMLStreamConstants.END_ELEMENT) {
				break;
			}

			addContent(com.top_logic.graphic.flow.data.impl.Box_Impl.readBox_XmlContent(in));
		}
	}

	@Override
	public final <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return visit((com.top_logic.graphic.flow.data.Layout.Visitor<R,A,E>) v, arg);
	}

}
