package com.top_logic.graphic.flow.data;

/**
 * A layout that allows to freely place boxes at arbitrary positions. The layout does not impose any restrictions to the positions and sizes of the placed boxes.
 */
public interface FloatingLayout extends Box, com.top_logic.graphic.flow.operations.layout.FloatingLayoutOperations {

	/**
	 * Creates a {@link com.top_logic.graphic.flow.data.FloatingLayout} instance.
	 */
	static com.top_logic.graphic.flow.data.FloatingLayout create() {
		return new com.top_logic.graphic.flow.data.impl.FloatingLayout_Impl();
	}

	/** Identifier for the {@link com.top_logic.graphic.flow.data.FloatingLayout} type in JSON format. */
	String FLOATING_LAYOUT__TYPE = "FloatingLayout";

	/** @see #getNodes() */
	String NODES__PROP = "nodes";

	/**
	 * Top-level floating elements.
	 */
	java.util.List<com.top_logic.graphic.flow.data.Box> getNodes();

	/**
	 * @see #getNodes()
	 */
	com.top_logic.graphic.flow.data.FloatingLayout setNodes(java.util.List<? extends com.top_logic.graphic.flow.data.Box> value);

	/**
	 * Adds a value to the {@link #getNodes()} list.
	 */
	com.top_logic.graphic.flow.data.FloatingLayout addNode(com.top_logic.graphic.flow.data.Box value);

	/**
	 * Removes a value from the {@link #getNodes()} list.
	 */
	void removeNode(com.top_logic.graphic.flow.data.Box value);

	@Override
	com.top_logic.graphic.flow.data.FloatingLayout setX(double value);

	@Override
	com.top_logic.graphic.flow.data.FloatingLayout setY(double value);

	@Override
	com.top_logic.graphic.flow.data.FloatingLayout setWidth(double value);

	@Override
	com.top_logic.graphic.flow.data.FloatingLayout setHeight(double value);

	@Override
	com.top_logic.graphic.flow.data.FloatingLayout setCssClass(String value);

	@Override
	com.top_logic.graphic.flow.data.FloatingLayout setUserObject(java.lang.Object value);

	@Override
	com.top_logic.graphic.flow.data.FloatingLayout setClientId(String value);

	@Override
	com.top_logic.graphic.flow.data.FloatingLayout setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.graphic.flow.data.FloatingLayout readFloatingLayout(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.graphic.flow.data.FloatingLayout) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert FLOATING_LAYOUT__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.graphic.flow.data.impl.FloatingLayout_Impl result = new com.top_logic.graphic.flow.data.impl.FloatingLayout_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default FloatingLayout self() {
		return this;
	}

}
