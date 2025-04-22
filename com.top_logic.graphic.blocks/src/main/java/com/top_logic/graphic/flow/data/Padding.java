package com.top_logic.graphic.flow.data;

public interface Padding extends Decoration, com.top_logic.graphic.flow.operations.PaddingOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Padding} instance.
	 */
	static com.top_logic.graphic.flow.data.Padding create() {
		return new com.top_logic.graphic.flow.data.impl.Padding_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Padding} type in JSON format. */
	String PADDING__TYPE = "Padding";

	/** @see #getTop() */
	String TOP__PROP = "top";

	/** @see #getLeft() */
	String LEFT__PROP = "left";

	/** @see #getBottom() */
	String BOTTOM__PROP = "bottom";

	/** @see #getRight() */
	String RIGHT__PROP = "right";

	double getTop();

	/**
	 * @see #getTop()
	 */
	com.top_logic.graphic.flow.data.Padding setTop(double value);

	double getLeft();

	/**
	 * @see #getLeft()
	 */
	com.top_logic.graphic.flow.data.Padding setLeft(double value);

	double getBottom();

	/**
	 * @see #getBottom()
	 */
	com.top_logic.graphic.flow.data.Padding setBottom(double value);

	double getRight();

	/**
	 * @see #getRight()
	 */
	com.top_logic.graphic.flow.data.Padding setRight(double value);

	@Override
	com.top_logic.graphic.flow.data.Padding setContent(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.Padding setX(double value);

	@Override
	com.top_logic.graphic.flow.data.Padding setY(double value);

	@Override
	com.top_logic.graphic.flow.data.Padding setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.Padding setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.Padding setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.Padding setClientId(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Padding readPadding(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Padding) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert PADDING__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.Padding_Impl result = new com.top_logic.graphic.flow.data.impl.Padding_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default Padding self() {
		return this;
	}

}
