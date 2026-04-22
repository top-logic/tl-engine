package com.top_logic.react.flow.data;

/**
 * A rectangular region where a box may be dropped during drag.
 */
public interface DropArea extends de.haumacher.msgbuf.graph.SharedGraphNode {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.DropArea} instance.
	 */
	static com.top_logic.react.flow.data.DropArea create() {
		return new com.top_logic.react.flow.data.impl.DropArea_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.DropArea} type in JSON format. */
	String DROP_AREA__TYPE = "DropArea";

	/** @see #getX() */
	String X__PROP = "x";

	/** @see #getY() */
	String Y__PROP = "y";

	/** @see #getWidth() */
	String WIDTH__PROP = "width";

	/** @see #getHeight() */
	String HEIGHT__PROP = "height";

	/**
	 * X coordinate of the top-left corner (layout units).
	 */
	double getX();

	/**
	 * @see #getX()
	 */
	com.top_logic.react.flow.data.DropArea setX(double value);

	/**
	 * Y coordinate of the top-left corner (layout units).
	 */
	double getY();

	/**
	 * @see #getY()
	 */
	com.top_logic.react.flow.data.DropArea setY(double value);

	/**
	 * Width of the area (layout units).
	 */
	double getWidth();

	/**
	 * @see #getWidth()
	 */
	com.top_logic.react.flow.data.DropArea setWidth(double value);

	/**
	 * Height of the area (layout units).
	 */
	double getHeight();

	/**
	 * @see #getHeight()
	 */
	com.top_logic.react.flow.data.DropArea setHeight(double value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.DropArea readDropArea(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.DropArea) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert DROP_AREA__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.DropArea_Impl result = new com.top_logic.react.flow.data.impl.DropArea_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

}
