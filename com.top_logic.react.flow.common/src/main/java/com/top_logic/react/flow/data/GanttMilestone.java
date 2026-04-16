package com.top_logic.react.flow.data;

/**
 * A milestone (point in time).
 */
public interface GanttMilestone extends GanttItem {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.GanttMilestone} instance.
	 */
	static com.top_logic.react.flow.data.GanttMilestone create() {
		return new com.top_logic.react.flow.data.impl.GanttMilestone_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.GanttMilestone} type in JSON format. */
	String GANTT_MILESTONE__TYPE = "GanttMilestone";

	/** @see #getAt() */
	String AT__PROP = "at";

	/**
	 * Position of the milestone on the axis; see {@link GanttAxis} for position semantics.
	 */
	double getAt();

	/**
	 * @see #getAt()
	 */
	com.top_logic.react.flow.data.GanttMilestone setAt(double value);

	@Override
	com.top_logic.react.flow.data.GanttMilestone setId(String value);

	@Override
	com.top_logic.react.flow.data.GanttMilestone setRowId(String value);

	@Override
	com.top_logic.react.flow.data.GanttMilestone setBox(com.top_logic.react.flow.data.Box value);

	@Override
	com.top_logic.react.flow.data.GanttMilestone setCanMoveTime(boolean value);

	@Override
	com.top_logic.react.flow.data.GanttMilestone setCanMoveRow(boolean value);

	@Override
	com.top_logic.react.flow.data.GanttMilestone setCanBeEdgeSource(boolean value);

	@Override
	com.top_logic.react.flow.data.GanttMilestone setCanBeEdgeTarget(boolean value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.GanttMilestone readGanttMilestone(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.GanttMilestone) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert GANTT_MILESTONE__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.GanttMilestone_Impl result = new com.top_logic.react.flow.data.impl.GanttMilestone_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

}
