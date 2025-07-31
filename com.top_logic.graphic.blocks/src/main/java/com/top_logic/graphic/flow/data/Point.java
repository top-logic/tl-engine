package com.top_logic.graphic.flow.data;

/**
 * A 2D point.
 */
public interface Point extends de.haumacher.msgbuf.graph.SharedGraphNode {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.Point} instance.
	 */
	static com.top_logic.graphic.flow.data.Point create() {
		return new com.top_logic.graphic.flow.data.impl.Point_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.Point} type in JSON format. */
	String POINT__TYPE = "Point";

	/** @see #getX() */
	String X__PROP = "x";

	/** @see #getY() */
	String Y__PROP = "y";

	/**
	 * The X coordinate of the point.
	 */
	double getX();

	/**
	 * @see #getX()
	 */
	com.top_logic.graphic.flow.data.Point setX(double value);

	/**
	 * The Y coordinate of the point.
	 */
	double getY();

	/**
	 * @see #getY()
	 */
	com.top_logic.graphic.flow.data.Point setY(double value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.Point readPoint(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.Point) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert POINT__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.Point_Impl result = new com.top_logic.graphic.flow.data.impl.Point_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

}
