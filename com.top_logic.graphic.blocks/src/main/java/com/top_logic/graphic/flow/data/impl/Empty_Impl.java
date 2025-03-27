package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Empty}.
 */
public class Empty_Impl extends com.top_logic.graphic.flow.data.impl.Box_Impl implements com.top_logic.graphic.flow.data.Empty {

	/**
	 * Creates a {@link Empty_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Empty#create()
	 */
	public Empty_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.EMPTY;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.Empty setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public String jsonType() {
		return EMPTY__TYPE;
	}

	@Override
	public int typeId() {
		return EMPTY__TYPE_ID;
	}

	/** Helper for creating an object of type {@link com.top_logic.graphic.flow.data.Empty} from a polymorphic composition. */
	public static com.top_logic.graphic.flow.data.Empty readEmpty_Content(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.impl.Empty_Impl result = new Empty_Impl();
		result.readContent(in);
		return result;
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.Empty} type. */
	public static final String EMPTY__XML_ELEMENT = "empty";

	@Override
	public String getXmlTagName() {
		return EMPTY__XML_ELEMENT;
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
		// No element fields.
	}

	/** Creates a new {@link com.top_logic.graphic.flow.data.Empty} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Empty_Impl readEmpty_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		Empty_Impl result = new Empty_Impl();
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
			default: {
				super.readFieldXmlElement(in, localName);
			}
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Box.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
