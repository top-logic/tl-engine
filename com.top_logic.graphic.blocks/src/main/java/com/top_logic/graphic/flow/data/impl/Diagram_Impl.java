package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Diagram}.
 */
public class Diagram_Impl extends com.top_logic.graphic.flow.data.impl.Widget_Impl implements com.top_logic.graphic.flow.data.Diagram {

	/**
	 * Creates a {@link Diagram_Impl} instance.
	 *
	 * @see com.top_logic.graphic.flow.data.Diagram#create()
	 */
	public Diagram_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.DIAGRAM;
	}

	@Override
	public String jsonType() {
		return DIAGRAM__TYPE;
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.Diagram} type. */
	public static final String DIAGRAM__XML_ELEMENT = "diagram";

	@Override
	public String getXmlTagName() {
		return DIAGRAM__XML_ELEMENT;
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

	/** Creates a new {@link com.top_logic.graphic.flow.data.Diagram} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Diagram_Impl readDiagram_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		Diagram_Impl result = new Diagram_Impl();
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
	public <R,A,E extends Throwable> R visit(com.top_logic.graphic.flow.data.Widget.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
