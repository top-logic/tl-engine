package com.top_logic.graphic.flow.data.impl;

/**
 * Implementation of {@link com.top_logic.graphic.flow.data.Widget}.
 */
public abstract class Widget_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.graphic.flow.data.Widget {

	/**
	 * Creates a {@link Widget_Impl} instance.
	 */
	public Widget_Impl() {
		super();
	}

	protected de.haumacher.msgbuf.observer.Listener _listener = de.haumacher.msgbuf.observer.Listener.NONE;

	@Override
	public com.top_logic.graphic.flow.data.Widget registerListener(de.haumacher.msgbuf.observer.Listener l) {
		internalRegisterListener(l);
		return this;
	}

	protected final void internalRegisterListener(de.haumacher.msgbuf.observer.Listener l) {
		_listener = de.haumacher.msgbuf.observer.Listener.register(_listener, l);
	}

	@Override
	public com.top_logic.graphic.flow.data.Widget unregisterListener(de.haumacher.msgbuf.observer.Listener l) {
		internalUnregisterListener(l);
		return this;
	}

	protected final void internalUnregisterListener(de.haumacher.msgbuf.observer.Listener l) {
		_listener = de.haumacher.msgbuf.observer.Listener.unregister(_listener, l);
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		out.beginArray();
		out.value(jsonType());
		writeContent(out);
		out.endArray();
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		out.beginObject();
		out.name(0);
		out.value(typeId());
		writeFields(out);
		out.endObject();
	}

	/**
	 * Serializes all fields of this instance to the given binary output.
	 *
	 * @param out
	 *        The binary output to write to.
	 * @throws java.io.IOException If writing fails.
	 */
	protected void writeFields(de.haumacher.msgbuf.binary.DataWriter out) throws java.io.IOException {
		// No fields to write, hook for subclasses.
	}

	/** Helper for reading all fields of this instance. */
	protected final void readContent(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		while (in.hasNext()) {
			int field = in.nextName();
			readField(in, field);
		}
	}

	/** Consumes the value for the field with the given ID and assigns its value. */
	protected void readField(de.haumacher.msgbuf.binary.DataReader in, int field) throws java.io.IOException {
		switch (field) {
			default: in.skipValue(); 
		}
	}

	/** XML element name representing a {@link com.top_logic.graphic.flow.data.Widget} type. */
	public static final String WIDGET__XML_ELEMENT = "widget";

	@Override
	public final void writeContent(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		writeAttributes(out);
		writeElements(out);
	}

	/** Serializes all fields that are written as XML attributes. */
	protected void writeAttributes(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
	}

	/** Serializes all fields that are written as XML elements. */
	protected void writeElements(javax.xml.stream.XMLStreamWriter out) throws javax.xml.stream.XMLStreamException {
		// No element fields.
	}

	/** Creates a new {@link com.top_logic.graphic.flow.data.Widget} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Widget_Impl readWidget_XmlContent(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		switch (in.getLocalName()) {
			case Text_Impl.TEXT__XML_ELEMENT: {
				return com.top_logic.graphic.flow.data.impl.Text_Impl.readText_XmlContent(in);
			}

			case Image_Impl.IMAGE__XML_ELEMENT: {
				return com.top_logic.graphic.flow.data.impl.Image_Impl.readImage_XmlContent(in);
			}

			case Empty_Impl.EMPTY__XML_ELEMENT: {
				return com.top_logic.graphic.flow.data.impl.Empty_Impl.readEmpty_XmlContent(in);
			}

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

			case CompassLayout_Impl.COMPASS_LAYOUT__XML_ELEMENT: {
				return com.top_logic.graphic.flow.data.impl.CompassLayout_Impl.readCompassLayout_XmlContent(in);
			}

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

	/** Reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	protected final void readContentXml(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		for (int n = 0, cnt = in.getAttributeCount(); n < cnt; n++) {
			String name = in.getAttributeLocalName(n);
			String value = in.getAttributeValue(n);

			readFieldXmlAttribute(name, value);
		}
		while (true) {
			int event = in.nextTag();
			if (event == javax.xml.stream.XMLStreamConstants.END_ELEMENT) {
				break;
			}
			assert event == javax.xml.stream.XMLStreamConstants.START_ELEMENT;

			String localName = in.getLocalName();
			readFieldXmlElement(in, localName);
		}
	}

	/** Parses the given attribute value and assigns it to the field with the given name. */
	protected void readFieldXmlAttribute(String name, String value) {
		switch (name) {
			default: {
				// Skip unknown attribute.
			}
		}
	}

	/** Reads the element under the cursor and assigns its contents to the field with the given name. */
	protected void readFieldXmlElement(javax.xml.stream.XMLStreamReader in, String localName) throws javax.xml.stream.XMLStreamException {
		switch (localName) {
			default: {
				internalSkipUntilMatchingEndElement(in);
			}
		}
	}

	protected static final void internalSkipUntilMatchingEndElement(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		int level = 0;
		while (true) {
			switch (in.next()) {
				case javax.xml.stream.XMLStreamConstants.START_ELEMENT: level++; break;
				case javax.xml.stream.XMLStreamConstants.END_ELEMENT: if (level == 0) { return; } else { level--; break; }
			}
		}
	}

}
