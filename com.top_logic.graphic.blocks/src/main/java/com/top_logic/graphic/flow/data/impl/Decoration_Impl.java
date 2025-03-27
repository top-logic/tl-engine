package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Decoration}.
 */
public abstract class Decoration_Impl extends com.top_logic.graphic.flow.data.impl.Box_Impl implements com.top_logic.graphic.flow.data.Decoration {

	private com.top_logic.graphic.flow.data.Box _content = null;

	/**
	 * Creates a {@link Decoration_Impl} instance.
	 */
	public Decoration_Impl() {
		super();
	}

	@Override
	public final com.top_logic.graphic.flow.data.Box getContent() {
		return _content;
	}

	@Override
	public com.top_logic.graphic.flow.data.Decoration setContent(com.top_logic.graphic.flow.data.Box value) {
		internalSetContent(value);
		return this;
	}

	/** Internal setter for {@link #getContent()} without chain call utility. */
	protected final void internalSetContent(com.top_logic.graphic.flow.data.Box value) {
		_listener.beforeSet(this, CONTENT__PROP, value);
		_content = value;
	}

	@Override
	public final boolean hasContent() {
		return _content != null;
	}

	@Override
	public com.top_logic.graphic.flow.data.Decoration setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Decoration setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Decoration setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Decoration setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	private static java.util.List<String> PROPERTIES = java.util.Collections.unmodifiableList(
		java.util.Arrays.asList(
			CONTENT__PROP));

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case CONTENT__PROP: return getContent();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case CONTENT__PROP: internalSetContent((com.top_logic.graphic.flow.data.Box) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		if (hasContent()) {
			out.name(CONTENT__PROP);
			getContent().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CONTENT__PROP: setContent(com.top_logic.graphic.flow.data.Box.readBox(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		super.writeFields(out);
		if (hasContent()) {
			out.name(CONTENT__ID);
			getContent().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.binary.DataReader in, int field) throws java.io.IOException {
		switch (field) {
			case CONTENT__ID: setContent(com.top_logic.graphic.flow.data.Box.readBox(in)); break;
			default: super.readField(in, field);
		}
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.Decoration} type. */
	public static final String DECORATION__XML_ELEMENT = "decoration";

	/** XML attribute or element name of a {@link #getContent} property. */
	private static final String CONTENT__XML_ATTR = "content";

	/** Serializes all fields that are written as XML attributes. */
	@Override
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeAttributes(out);
	}

	/** Serializes all fields that are written as XML elements. */
	@Override
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		super.writeElements(out);
		if (hasContent()) {
			out.writeStartElement(CONTENT__XML_ATTR);
			getContent().writeTo(out);
			out.writeEndElement();
		}
	}

	/** Creates a new {@link com.top_logic.graphic.flow.data.Decoration} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Decoration_Impl readDecoration_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		switch (in.getLocalName()) {
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
			case CONTENT__XML_ATTR: {
				in.nextTag();
				setContent(com.top_logic.graphic.flow.data.impl.Box_Impl.readBox_XmlContent(in));
				internalSkipUntilMatchingEndElement(in);
				break;
			}
			default: {
				super.readFieldXmlElement(in, localName);
			}
		}
	}

	@Override
	public final <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return visit((com.top_logic.graphic.flow.data.Decoration.Visitor<R,A,E>) v, arg);
	}

}
