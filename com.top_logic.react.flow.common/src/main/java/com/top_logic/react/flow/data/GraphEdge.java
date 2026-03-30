package com.top_logic.react.flow.data;

/**
 * Edge in a {@link GraphLayout} with orthogonal waypoints.
 *
 * @Operations com.top_logic.react.flow.operations.GraphEdgeOperations
 */
public interface GraphEdge extends Widget, com.top_logic.react.flow.operations.GraphEdgeOperations {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.GraphEdge} instance.
	 */
	static com.top_logic.react.flow.data.GraphEdge create() {
		return new com.top_logic.react.flow.data.impl.GraphEdge_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.GraphEdge} type in JSON format. */
	String GRAPH_EDGE__TYPE = "GraphEdge";

	/** @see #getSource() */
	String SOURCE__PROP = "source";

	/** @see #getTarget() */
	String TARGET__PROP = "target";

	/** @see #getWaypoints() */
	String WAYPOINTS__PROP = "waypoints";

	/** @see #getStrokeStyle() */
	String STROKE_STYLE__PROP = "strokeStyle";

	/** @see #getThickness() */
	String THICKNESS__PROP = "thickness";

	/** @see #getDashes() */
	String DASHES__PROP = "dashes";

	/** @see #getDecorations() */
	String DECORATIONS__PROP = "decorations";

	/**
	 * Source node. The node is owned by the enclosing {@link GraphLayout}, not by this edge.
	 */
	com.top_logic.react.flow.data.Box getSource();

	/**
	 * @see #getSource()
	 */
	com.top_logic.react.flow.data.GraphEdge setSource(com.top_logic.react.flow.data.Box value);

	/**
	 * Checks, whether {@link #getSource()} has a value.
	 */
	boolean hasSource();

	/**
	 * Target node. The node is owned by the enclosing {@link GraphLayout}, not by this edge.
	 */
	com.top_logic.react.flow.data.Box getTarget();

	/**
	 * @see #getTarget()
	 */
	com.top_logic.react.flow.data.GraphEdge setTarget(com.top_logic.react.flow.data.Box value);

	/**
	 * Checks, whether {@link #getTarget()} has a value.
	 */
	boolean hasTarget();

	/**
	 * Waypoints computed by the layout algorithm. Forms an orthogonal polyline.
	 */
	java.util.List<com.top_logic.react.flow.data.GraphWaypoint> getWaypoints();

	/**
	 * @see #getWaypoints()
	 */
	com.top_logic.react.flow.data.GraphEdge setWaypoints(java.util.List<? extends com.top_logic.react.flow.data.GraphWaypoint> value);

	/**
	 * Adds a value to the {@link #getWaypoints()} list.
	 */
	com.top_logic.react.flow.data.GraphEdge addWaypoint(com.top_logic.react.flow.data.GraphWaypoint value);

	/**
	 * Removes a value from the {@link #getWaypoints()} list.
	 */
	void removeWaypoint(com.top_logic.react.flow.data.GraphWaypoint value);

	/**
	 * Stroke color.
	 */
	String getStrokeStyle();

	/**
	 * @see #getStrokeStyle()
	 */
	com.top_logic.react.flow.data.GraphEdge setStrokeStyle(String value);

	/**
	 * Line thickness.
	 */
	double getThickness();

	/**
	 * @see #getThickness()
	 */
	com.top_logic.react.flow.data.GraphEdge setThickness(double value);

	/**
	 * Dash pattern (alternating dash/gap lengths).
	 */
	java.util.List<Double> getDashes();

	/**
	 * @see #getDashes()
	 */
	com.top_logic.react.flow.data.GraphEdge setDashes(java.util.List<? extends Double> value);

	/**
	 * Adds a value to the {@link #getDashes()} list.
	 */
	com.top_logic.react.flow.data.GraphEdge addDash(double value);

	/**
	 * Removes a value from the {@link #getDashes()} list.
	 */
	void removeDash(double value);

	/**
	 * Decorations placed on the edge (arrow heads, diamonds, labels).
	 */
	java.util.List<com.top_logic.react.flow.data.EdgeDecoration> getDecorations();

	/**
	 * @see #getDecorations()
	 */
	com.top_logic.react.flow.data.GraphEdge setDecorations(java.util.List<? extends com.top_logic.react.flow.data.EdgeDecoration> value);

	/**
	 * Adds a value to the {@link #getDecorations()} list.
	 */
	com.top_logic.react.flow.data.GraphEdge addDecoration(com.top_logic.react.flow.data.EdgeDecoration value);

	/**
	 * Removes a value from the {@link #getDecorations()} list.
	 */
	void removeDecoration(com.top_logic.react.flow.data.EdgeDecoration value);

	@Override
	com.top_logic.react.flow.data.GraphEdge setCssClass(String value);

	@Override
	com.top_logic.react.flow.data.GraphEdge setUserObject(java.lang.Object value);

	@Override
	com.top_logic.react.flow.data.GraphEdge setClientId(String value);

	@Override
	com.top_logic.react.flow.data.GraphEdge setRenderInfo(java.lang.Object value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.GraphEdge readGraphEdge(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.GraphEdge) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert GRAPH_EDGE__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.GraphEdge_Impl result = new com.top_logic.react.flow.data.impl.GraphEdge_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

	@Override
	default GraphEdge self() {
		return this;
	}

}
