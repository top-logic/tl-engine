package com.top_logic.graphic.flow.data;

/**
 * An empty region.
 */
public interface Empty extends Box, com.top_logic.graphic.flow.operations.EmptyOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Empty} instance.
	 */
	static com.top_logic.graphic.flow.data.Empty create() {
		return new com.top_logic.graphic.flow.data.impl.Empty_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Empty} type in JSON format. */
	String EMPTY__TYPE = "Empty";

	/** @see #getMinWidth() */
	String MIN_WIDTH__PROP = "minWidth";

	/** @see #getMinHeight() */
	String MIN_HEIGHT__PROP = "minHeight";

	/**
	 * The minimum width of the element to allow to reserve some space.
	 */
	double getMinWidth();

	/**
	 * @see #getMinWidth()
	 */
	com.top_logic.graphic.flow.data.Empty setMinWidth(double value);

	/**
	 * The minimum height of the element to allow to reserve some space.
	 */
	double getMinHeight();

	/**
	 * @see #getMinHeight()
	 */
	com.top_logic.graphic.flow.data.Empty setMinHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.Empty setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Empty setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Empty setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Empty setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.Empty setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.Empty setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.Empty setClientId(String value);

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

}
