package com.top_logic.react.flow.data;

/**
 * Layout for arbitrary directed graphs using hierarchical (layered) layout.
 *
 * <p>Nodes are positioned in layers by the Sugiyama algorithm. Edges are routed
 * orthogonally between layers. Node sizes are computed from widget intrinsic sizes
 * before the layout algorithm runs.</p>
 *
 * @Operations com.top_logic.react.flow.operations.layout.GraphLayoutOperations
 */
public interface GraphLayout extends FloatingLayout, com.top_logic.react.flow.operations.layout.GraphLayoutOperations {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.GraphLayout} instance.
	 */
	static com.top_logic.react.flow.data.GraphLayout create() {
		return new com.top_logic.react.flow.data.impl.GraphLayout_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.GraphLayout} type in JSON format. */
	String GRAPH_LAYOUT__TYPE = "GraphLayout";

	/** @see #getEdges() */
	String EDGES__PROP = "edges";

	/** @see #getLayerGap() */
	String LAYER_GAP__PROP = "layerGap";

	/** @see #getNodeGap() */
	String NODE_GAP__PROP = "nodeGap";

	/**
	 * Edges connecting nodes in this graph.
	 */
	java.util.List<com.top_logic.react.flow.data.GraphEdge> getEdges();

	/**
	 * @see #getEdges()
	 */
	com.top_logic.react.flow.data.GraphLayout setEdges(java.util.List<? extends com.top_logic.react.flow.data.GraphEdge> value);

	/**
	 * Adds a value to the {@link #getEdges()} list.
	 */
	com.top_logic.react.flow.data.GraphLayout addEdge(com.top_logic.react.flow.data.GraphEdge value);

	/**
	 * Removes a value from the {@link #getEdges()} list.
	 */
	void removeEdge(com.top_logic.react.flow.data.GraphEdge value);

	/**
	 * Gap between layers (perpendicular to layout direction).
	 */
	double getLayerGap();

	/**
	 * @see #getLayerGap()
	 */
	com.top_logic.react.flow.data.GraphLayout setLayerGap(double value);

	/**
	 * Gap between nodes within the same layer.
	 */
	double getNodeGap();

	/**
	 * @see #getNodeGap()
	 */
	com.top_logic.react.flow.data.GraphLayout setNodeGap(double value);

	@Override
	com.top_logic.react.flow.data.GraphLayout setNodes(java.util.List<? extends com.top_logic.react.flow.data.Box> value);

	@Override
	com.top_logic.react.flow.data.GraphLayout addNode(com.top_logic.react.flow.data.Box value);

	@Override
	com.top_logic.react.flow.data.GraphLayout setX(double value);

	@Override
	com.top_logic.react.flow.data.GraphLayout setY(double value);

	@Override
	com.top_logic.react.flow.data.GraphLayout setWidth(double value);

	@Override
	com.top_logic.react.flow.data.GraphLayout setHeight(double value);

	@Override
	com.top_logic.react.flow.data.GraphLayout setCssClass(String value);

	@Override
	com.top_logic.react.flow.data.GraphLayout setUserObject(java.lang.Object value);

	@Override
	com.top_logic.react.flow.data.GraphLayout setClientId(String value);

	@Override
	com.top_logic.react.flow.data.GraphLayout setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.GraphLayout readGraphLayout(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.GraphLayout) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert GRAPH_LAYOUT__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.GraphLayout_Impl result = new com.top_logic.react.flow.data.impl.GraphLayout_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default GraphLayout self() {
		return this;
	}

}
