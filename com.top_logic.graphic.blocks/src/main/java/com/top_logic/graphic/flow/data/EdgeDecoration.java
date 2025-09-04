package com.top_logic.graphic.flow.data;

/**
 * A decoration placed on an edge.
 */
public interface EdgeDecoration extends Widget, com.top_logic.graphic.flow.operations.tree.EdgeDecorationOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.EdgeDecoration} instance.
	 */
	static com.top_logic.graphic.flow.data.EdgeDecoration create() {
		return new com.top_logic.graphic.flow.data.impl.EdgeDecoration_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.EdgeDecoration} type in JSON format. */
	String EDGE_DECORATION__TYPE = "EdgeDecoration";

	/** @see #getLinePosition() */
	String LINE_POSITION__PROP = "linePosition";

	/** @see #getOffsetPosition() */
	String OFFSET_POSITION__PROP = "offsetPosition";

	/** @see #getContent() */
	String CONTENT__PROP = "content";

	/**
	 * A ratio of the length of the edge that determines the position where the content is placed on the edge. 
	 * A value of <code>0.0</code> places the content at the start position of the edge. 
	 * A value of <code>1.0</code> places the content at the end position of the edge.
	 */
	double getLinePosition();

	/**
	 * @see #getLinePosition()
	 */
	com.top_logic.graphic.flow.data.EdgeDecoration setLinePosition(double value);

	/**
	 * The position of the content box that is matched with the line position.
	 */
	com.top_logic.graphic.flow.data.OffsetPosition getOffsetPosition();

	/**
	 * @see #getOffsetPosition()
	 */
	com.top_logic.graphic.flow.data.EdgeDecoration setOffsetPosition(com.top_logic.graphic.flow.data.OffsetPosition value);

	/**
	 * The content to render relative to the edge.
	 */
	com.top_logic.graphic.flow.data.Box getContent();

	/**
	 * @see #getContent()
	 */
	com.top_logic.graphic.flow.data.EdgeDecoration setContent(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Checks, whether {@link #getContent()} has a value.
	 */
	boolean hasContent();

	@Override
	com.top_logic.graphic.flow.data.EdgeDecoration setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.EdgeDecoration setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.EdgeDecoration setClientId(String value);

	@Override
	com.top_logic.graphic.flow.data.EdgeDecoration setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.EdgeDecoration readEdgeDecoration(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.EdgeDecoration) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert EDGE_DECORATION__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.EdgeDecoration_Impl result = new com.top_logic.graphic.flow.data.impl.EdgeDecoration_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default EdgeDecoration self() {
		return this;
	}

}
