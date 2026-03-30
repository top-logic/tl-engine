package com.top_logic.react.flow.data;

/**
 * A waypoint on an edge path.
 */
public interface GraphWaypoint extends de.haumacher.msgbuf.graph.SharedGraphNode {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.GraphWaypoint} instance.
	 */
	static com.top_logic.react.flow.data.GraphWaypoint create() {
		return new com.top_logic.react.flow.data.impl.GraphWaypoint_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.GraphWaypoint} type in JSON format. */
	String GRAPH_WAYPOINT__TYPE = "GraphWaypoint";

	/** @see #getX() */
	String X__PROP = "x";

	/** @see #getY() */
	String Y__PROP = "y";

	double getX();

	/**
	 * @see #getX()
	 */
	com.top_logic.react.flow.data.GraphWaypoint setX(double value);

	double getY();

	/**
	 * @see #getY()
	 */
	com.top_logic.react.flow.data.GraphWaypoint setY(double value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.GraphWaypoint readGraphWaypoint(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.GraphWaypoint) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert GRAPH_WAYPOINT__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.GraphWaypoint_Impl result = new com.top_logic.react.flow.data.impl.GraphWaypoint_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

}
