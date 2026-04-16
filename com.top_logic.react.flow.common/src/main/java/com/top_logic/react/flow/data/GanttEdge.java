package com.top_logic.react.flow.data;

/**
 * A dependency edge between two Gantt items.
 */
public interface GanttEdge extends de.haumacher.msgbuf.graph.SharedGraphNode {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.GanttEdge} instance.
	 */
	static com.top_logic.react.flow.data.GanttEdge create() {
		return new com.top_logic.react.flow.data.impl.GanttEdge_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.GanttEdge} type in JSON format. */
	String GANTT_EDGE__TYPE = "GanttEdge";

	/** @see #getId() */
	String ID__PROP = "id";

	/** @see #getUserObject() */
	String USER_OBJECT__PROP = "userObject";

	/** @see #getSourceItemId() */
	String SOURCE_ITEM_ID__PROP = "sourceItemId";

	/** @see #getSourceEndpoint() */
	String SOURCE_ENDPOINT__PROP = "sourceEndpoint";

	/** @see #getTargetItemId() */
	String TARGET_ITEM_ID__PROP = "targetItemId";

	/** @see #getTargetEndpoint() */
	String TARGET_ENDPOINT__PROP = "targetEndpoint";

	/** @see #getEnforce() */
	String ENFORCE__PROP = "enforce";

	/**
	 * Opaque identifier, unique within a {@link GanttLayout}.
	 */
	String getId();

	/**
	 * @see #getId()
	 */
	com.top_logic.react.flow.data.GanttEdge setId(String value);

	/**
	 * Application-defined business object backing this edge.
	 * Server-only (transient); used as the identity from which the technical
	 * {@link #getId() id} is derived for client-side cross-references.
	 */
	java.lang.Object getUserObject();

	/**
	 * @see #getUserObject()
	 */
	com.top_logic.react.flow.data.GanttEdge setUserObject(java.lang.Object value);

	/**
	 * Checks, whether {@link #getUserObject()} has a value.
	 */
	boolean hasUserObject();

	/**
	 * ID of the source item.
	 */
	String getSourceItemId();

	/**
	 * @see #getSourceItemId()
	 */
	com.top_logic.react.flow.data.GanttEdge setSourceItemId(String value);

	/**
	 * Which end of the source item the edge attaches to.
	 */
	com.top_logic.react.flow.data.GanttEndpoint getSourceEndpoint();

	/**
	 * @see #getSourceEndpoint()
	 */
	com.top_logic.react.flow.data.GanttEdge setSourceEndpoint(com.top_logic.react.flow.data.GanttEndpoint value);

	/**
	 * ID of the target item.
	 */
	String getTargetItemId();

	/**
	 * @see #getTargetItemId()
	 */
	com.top_logic.react.flow.data.GanttEdge setTargetItemId(String value);

	/**
	 * Which end of the target item the edge attaches to.
	 */
	com.top_logic.react.flow.data.GanttEndpoint getTargetEndpoint();

	/**
	 * @see #getTargetEndpoint()
	 */
	com.top_logic.react.flow.data.GanttEdge setTargetEndpoint(com.top_logic.react.flow.data.GanttEndpoint value);

	/**
	 * Constraint semantic applied to this edge (STRICT, WARN, or NONE).
	 */
	com.top_logic.react.flow.data.GanttEnforce getEnforce();

	/**
	 * @see #getEnforce()
	 */
	com.top_logic.react.flow.data.GanttEdge setEnforce(com.top_logic.react.flow.data.GanttEnforce value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.GanttEdge readGanttEdge(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.GanttEdge) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert GANTT_EDGE__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.GanttEdge_Impl result = new com.top_logic.react.flow.data.impl.GanttEdge_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

}
