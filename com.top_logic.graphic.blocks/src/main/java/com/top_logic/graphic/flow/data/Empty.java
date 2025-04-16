package com.top_logic.graphic.flow.data;

public interface Empty extends Box, com.top_logic.graphic.flow.operations.EmptyOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Empty} instance.
	 */
	static com.top_logic.graphic.flow.data.Empty create() {
		return new com.top_logic.graphic.flow.data.impl.Empty_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Empty} type in JSON format. */
	String EMPTY__TYPE = "Empty";

	@Override
	com.top_logic.graphic.flow.data.Empty setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Empty setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Empty setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Empty setHeight(double value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Empty readEmpty(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Empty) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert EMPTY__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.Empty_Impl result = new com.top_logic.graphic.flow.data.impl.Empty_Impl();
		scope.readData(result, id, in);
		in.endArray();
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
