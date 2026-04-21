package com.top_logic.react.flow.data;

/**
 * A point item positioned at a single time on the axis.
 */
public interface GanttPoint extends GanttItem {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.GanttPoint} instance.
	 */
	static com.top_logic.react.flow.data.GanttPoint create() {
		return new com.top_logic.react.flow.data.impl.GanttPoint_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.GanttPoint} type in JSON format. */
	String GANTT_POINT__TYPE = "GanttPoint";

	/** @see #getAt() */
	String AT__PROP = "at";

	/**
	 * Position of the point on the axis; see {@link GanttAxis} for position semantics.
	 */
	double getAt();

	/**
	 * @see #getAt()
	 */
	com.top_logic.react.flow.data.GanttPoint setAt(double value);

	@Override
	com.top_logic.react.flow.data.GanttPoint setId(String value);

	@Override
	com.top_logic.react.flow.data.GanttPoint setUserObject(java.lang.Object value);

	@Override
	com.top_logic.react.flow.data.GanttPoint setRowModel(java.lang.Object value);

	@Override
	com.top_logic.react.flow.data.GanttPoint setRowId(String value);

	@Override
	com.top_logic.react.flow.data.GanttPoint setBox(com.top_logic.react.flow.data.Box value);

	@Override
	com.top_logic.react.flow.data.GanttPoint setCanMoveTime(boolean value);

	@Override
	com.top_logic.react.flow.data.GanttPoint setCanMoveRow(boolean value);

	@Override
	com.top_logic.react.flow.data.GanttPoint setCanBeEdgeSource(boolean value);

	@Override
	com.top_logic.react.flow.data.GanttPoint setCanBeEdgeTarget(boolean value);

	@Override
	com.top_logic.react.flow.data.GanttPoint setValidDropTargets(java.util.List<? extends java.lang.Object> value);

	@Override
	com.top_logic.react.flow.data.GanttPoint addValidDropTarget(java.lang.Object value);

	@Override
	com.top_logic.react.flow.data.GanttPoint setValidDropTargetIds(java.util.List<? extends String> value);

	@Override
	com.top_logic.react.flow.data.GanttPoint addValidDropTargetId(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.GanttPoint readGanttPoint(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.GanttPoint) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert GANTT_POINT__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.GanttPoint_Impl result = new com.top_logic.react.flow.data.impl.GanttPoint_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

}
