package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.VerticalLayout}.
 */
public class VerticalLayout_Impl extends com.top_logic.graphic.flow.data.impl.RowLayout_Impl implements com.top_logic.graphic.flow.data.VerticalLayout {

	/**
	 * Creates a {@link VerticalLayout_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.VerticalLayout#create()
	 */
	public VerticalLayout_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.VERTICAL_LAYOUT;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout setGap(double value) {
		internalSetGap(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout setFill(com.top_logic.graphic.flow.data.SpaceDistribution value) {
		internalSetFill(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value) {
		internalSetContents(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout addContent(com.top_logic.graphic.flow.data.Box value) {
		internalAddContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.VerticalLayout setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public String jsonType() {
		return VERTICAL_LAYOUT__TYPE;
	}

	@Override
	public int typeId() {
		return VERTICAL_LAYOUT__TYPE_ID;
	}

	/** Helper for creating an object of type {@link com.top_logic.graphic.flow.data.VerticalLayout} from a polymorphic composition. */
	public static com.top_logic.graphic.flow.data.VerticalLayout readVerticalLayout_Content(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.impl.VerticalLayout_Impl result = new VerticalLayout_Impl();
		result.readContent(in);
		return result;
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.VerticalLayout} type. */
	public static final String VERTICAL_LAYOUT__XML_ELEMENT = "vertical-layout";

	@Override
	public String getXmlTagName() {
		return VERTICAL_LAYOUT__XML_ELEMENT;
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

	/** Creates a new {@link com.top_logic.graphic.flow.data.VerticalLayout} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static VerticalLayout_Impl readVerticalLayout_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		VerticalLayout_Impl result = new VerticalLayout_Impl();
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
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.RowLayout.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
