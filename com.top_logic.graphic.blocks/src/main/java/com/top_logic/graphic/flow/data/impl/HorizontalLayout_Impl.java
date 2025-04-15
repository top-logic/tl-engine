package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.HorizontalLayout}.
 */
public class HorizontalLayout_Impl extends com.top_logic.graphic.flow.data.impl.RowLayout_Impl implements com.top_logic.graphic.flow.data.HorizontalLayout {

	/**
	 * Creates a {@link HorizontalLayout_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.HorizontalLayout#create()
	 */
	public HorizontalLayout_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.HORIZONTAL_LAYOUT;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setGap(double value) {
		internalSetGap(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setFill(com.top_logic.graphic.flow.data.SpaceDistribution value) {
		internalSetFill(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setContents(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value) {
		internalSetContents(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout addContent(com.top_logic.graphic.flow.data.Box value) {
		internalAddContent(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setX(double value) {
		internalSetX(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setY(double value) {
		internalSetY(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setWidth(double value) {
		internalSetWidth(value);
		return this;
	}

	@Override
	public com.top_logic.graphic.flow.data.HorizontalLayout setHeight(double value) {
		internalSetHeight(value);
		return this;
	}

	@Override
	public String jsonType() {
		return HORIZONTAL_LAYOUT__TYPE;
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.HorizontalLayout} type. */
	public static final String HORIZONTAL_LAYOUT__XML_ELEMENT = "horizontal-layout";

	@Override
	public String getXmlTagName() {
		return HORIZONTAL_LAYOUT__XML_ELEMENT;
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

	/** Creates a new {@link com.top_logic.graphic.flow.data.HorizontalLayout} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static HorizontalLayout_Impl readHorizontalLayout_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		HorizontalLayout_Impl result = new HorizontalLayout_Impl();
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
