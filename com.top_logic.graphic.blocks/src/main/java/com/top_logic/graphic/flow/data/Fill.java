package com.top_logic.graphic.flow.data;

public interface Fill extends Decoration, com.top_logic.graphic.flow.model.FillOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Fill} instance.
	 */
	static com.top_logic.graphic.flow.data.Fill create() {
		return new com.top_logic.graphic.flow.data.impl.Fill_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Fill} type in JSON format. */
	String FILL__TYPE = "Fill";

	/** @see #getFillStyle() */
	String FILL_STYLE__PROP = "fillStyle";

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Fill} type in binary format. */
	static final int FILL__TYPE_ID = 6;

	/** Identifier for the property {@link #getFillStyle()} in binary format. */
	static final int FILL_STYLE__ID = 6;

	String getFillStyle();

	/**
	 * @see #getFillStyle()
	 */
	com.top_logic.graphic.flow.data.Fill setFillStyle(String value);

	@Override
	com.top_logic.graphic.flow.data.Fill setContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.Fill setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Fill setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Fill setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Fill setHeight(double value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Fill readFill(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.impl.Fill_Impl result = new com.top_logic.graphic.flow.data.impl.Fill_Impl();
		result.readContent(in);
		return result;
	}

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Fill readFill(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		in.beginObject();
		com.top_logic.graphic.flow.data.Fill result = com.top_logic.graphic.flow.data.impl.Fill_Impl.readFill_Content(in);
		in.endObject();
		return result;
	}

	@Override
	default Fill self() {
		return this;
	}

	/** Creates a new {@link Fill} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Fill readFill(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return com.top_logic.graphic.flow.data.impl.Fill_Impl.readFill_XmlContent(in);
	}

}
