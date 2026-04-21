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

	/** @see #getSourceModel() */
	String SOURCE_MODEL__PROP = "sourceModel";

	/** @see #getSourceItemId() */
	String SOURCE_ITEM_ID__PROP = "sourceItemId";

	/** @see #getSourceEndpoint() */
	String SOURCE_ENDPOINT__PROP = "sourceEndpoint";

	/** @see #getTargetModel() */
	String TARGET_MODEL__PROP = "targetModel";

	/** @see #getTargetItemId() */
	String TARGET_ITEM_ID__PROP = "targetItemId";

	/** @see #getTargetEndpoint() */
	String TARGET_ENDPOINT__PROP = "targetEndpoint";

	/** @see #getEnforce() */
	String ENFORCE__PROP = "enforce";

	/** @see #getStrokeColor() */
	String STROKE_COLOR__PROP = "strokeColor";

	/** @see #getStrokeWidth() */
	String STROKE_WIDTH__PROP = "strokeWidth";

	/** @see #getDashes() */
	String DASHES__PROP = "dashes";

	/** @see #getViolatedStrokeColor() */
	String VIOLATED_STROKE_COLOR__PROP = "violatedStrokeColor";

	/** @see #getViolatedStrokeWidth() */
	String VIOLATED_STROKE_WIDTH__PROP = "violatedStrokeWidth";

	/** @see #getViolatedDashes() */
	String VIOLATED_DASHES__PROP = "violatedDashes";

	/**
	 * Opaque identifier, unique within a {@link GanttLayout}. Populated by the {@code gantt(...)} aggregator.
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
	 * Business object identifying the source item. Resolved to
	 * {@link #getSourceItemId() sourceItemId} by the {@code gantt(...)} aggregator.
	 */
	java.lang.Object getSourceModel();

	/**
	 * @see #getSourceModel()
	 */
	com.top_logic.react.flow.data.GanttEdge setSourceModel(java.lang.Object value);

	/**
	 * Checks, whether {@link #getSourceModel()} has a value.
	 */
	boolean hasSourceModel();

	/**
	 * ID of the source item. Wire-format field; populated by the {@code gantt(...)} aggregator
	 * from {@link #getSourceModel() sourceModel}.
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
	 * Business object identifying the target item. Resolved to
	 * {@link #getTargetItemId() targetItemId} by the {@code gantt(...)} aggregator.
	 */
	java.lang.Object getTargetModel();

	/**
	 * @see #getTargetModel()
	 */
	com.top_logic.react.flow.data.GanttEdge setTargetModel(java.lang.Object value);

	/**
	 * Checks, whether {@link #getTargetModel()} has a value.
	 */
	boolean hasTargetModel();

	/**
	 * ID of the target item. Wire-format field; populated by the {@code gantt(...)} aggregator
	 * from {@link #getTargetModel() targetModel}.
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
	 * Constraint enforcement mode (NONE or STRICT).
	 */
	com.top_logic.react.flow.data.GanttEnforce getEnforce();

	/**
	 * @see #getEnforce()
	 */
	com.top_logic.react.flow.data.GanttEdge setEnforce(com.top_logic.react.flow.data.GanttEnforce value);

	/**
	 * Stroke color for the normal (non-violated) state.
	 */
	String getStrokeColor();

	/**
	 * @see #getStrokeColor()
	 */
	com.top_logic.react.flow.data.GanttEdge setStrokeColor(String value);

	/**
	 * Line thickness in pixels for the normal state.
	 */
	double getStrokeWidth();

	/**
	 * @see #getStrokeWidth()
	 */
	com.top_logic.react.flow.data.GanttEdge setStrokeWidth(double value);

	/**
	 * Dash pattern for the normal state (alternating dash/gap lengths in pixels). Empty = solid.
	 */
	java.util.List<Double> getDashes();

	/**
	 * @see #getDashes()
	 */
	com.top_logic.react.flow.data.GanttEdge setDashes(java.util.List<? extends Double> value);

	/**
	 * Adds a value to the {@link #getDashes()} list.
	 */
	com.top_logic.react.flow.data.GanttEdge addDash(double value);

	/**
	 * Removes a value from the {@link #getDashes()} list.
	 */
	void removeDash(double value);

	/**
	 * Stroke color applied when the constraint is violated (enforce != NONE and
	 * source endpoint position > target endpoint position). Empty = use normal strokeColor.
	 */
	String getViolatedStrokeColor();

	/**
	 * @see #getViolatedStrokeColor()
	 */
	com.top_logic.react.flow.data.GanttEdge setViolatedStrokeColor(String value);

	/**
	 * Checks, whether {@link #getViolatedStrokeColor()} has a value.
	 */
	boolean hasViolatedStrokeColor();

	/**
	 * Line thickness when violated. Zero = use normal strokeWidth.
	 */
	double getViolatedStrokeWidth();

	/**
	 * @see #getViolatedStrokeWidth()
	 */
	com.top_logic.react.flow.data.GanttEdge setViolatedStrokeWidth(double value);

	/**
	 * Dash pattern when violated. Empty = use normal dashes.
	 */
	java.util.List<Double> getViolatedDashes();

	/**
	 * @see #getViolatedDashes()
	 */
	com.top_logic.react.flow.data.GanttEdge setViolatedDashes(java.util.List<? extends Double> value);

	/**
	 * Adds a value to the {@link #getViolatedDashes()} list.
	 */
	com.top_logic.react.flow.data.GanttEdge addViolatedDash(double value);

	/**
	 * Removes a value from the {@link #getViolatedDashes()} list.
	 */
	void removeViolatedDash(double value);

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
