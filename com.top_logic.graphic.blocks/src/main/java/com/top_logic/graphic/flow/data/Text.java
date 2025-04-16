package com.top_logic.graphic.flow.data;

public interface Text extends Box, com.top_logic.graphic.flow.operations.TextOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Text} instance.
	 */
	static com.top_logic.graphic.flow.data.Text create() {
		return new com.top_logic.graphic.flow.data.impl.Text_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Text} type in JSON format. */
	String TEXT__TYPE = "Text";

	/** @see #getValue() */
	String VALUE__PROP = "value";

	/** @see #getBaseLine() */
	String BASE_LINE__PROP = "baseLine";

	String getValue();

	/**
	 * @see #getValue()
	 */
	com.top_logic.graphic.flow.data.Text setValue(String value);

	double getBaseLine();

	/**
	 * @see #getBaseLine()
	 */
	com.top_logic.graphic.flow.data.Text setBaseLine(double value);

	@Override
	com.top_logic.graphic.flow.data.Text setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Text setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Text setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Text setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.Text setUserObject(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Text readText(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Text) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert TEXT__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.Text_Impl result = new com.top_logic.graphic.flow.data.impl.Text_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default Text self() {
		return this;
	}

}
