package com.top_logic.graphic.flow.data;

public interface Empty extends Box, com.top_logic.graphic.flow.model.EmptyOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Empty} instance.
	 */
	static com.top_logic.graphic.flow.data.Empty create() {
		return new com.top_logic.graphic.flow.data.impl.Empty_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Empty} type in JSON format. */
	String EMPTY__TYPE = "Empty";

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Empty} type in binary format. */
	static final int EMPTY__TYPE_ID = 3;

	@Override
	com.top_logic.graphic.flow.data.Empty setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Empty setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Empty setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Empty setHeight(double value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Empty readEmpty(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.graphic.flow.data.impl.Empty_Impl result = new com.top_logic.graphic.flow.data.impl.Empty_Impl();
		result.readContent(in);
		return result;
	}

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Empty readEmpty(de.haumacher.msgbuf.binary.DataReader in) throws java.io.IOException {
		in.beginObject();
		com.top_logic.graphic.flow.data.Empty result = com.top_logic.graphic.flow.data.impl.Empty_Impl.readEmpty_Content(in);
		in.endObject();
		return result;
	}

	@Override
	default Empty self() {
		return this;
	}

	/** Creates a new {@link Empty} and reads properties from the content (attributes and inner tags) of the currently open element in the given {@link javax.xml.stream.XMLStreamReader}. */
	public static Empty readEmpty(javax.xml.stream.XMLStreamReader in) throws javax.xml.stream.XMLStreamException {
		in.nextTag();
		return com.top_logic.graphic.flow.data.impl.Empty_Impl.readEmpty_XmlContent(in);
	}

}
