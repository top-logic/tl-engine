package com.top_logic.react.flow.data;

/**
 * A task with start and end on the time axis.
 */
public interface GanttSpan extends GanttItem {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.GanttSpan} instance.
	 */
	static com.top_logic.react.flow.data.GanttSpan create() {
		return new com.top_logic.react.flow.data.impl.GanttSpan_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.GanttSpan} type in JSON format. */
	String GANTT_SPAN__TYPE = "GanttSpan";

	/** @see #getStart() */
	String START__PROP = "start";

	/** @see #getEnd() */
	String END__PROP = "end";

	/** @see #isCanResizeStart() */
	String CAN_RESIZE_START__PROP = "canResizeStart";

	/** @see #isCanResizeEnd() */
	String CAN_RESIZE_END__PROP = "canResizeEnd";

	double getStart();

	/**
	 * @see #getStart()
	 */
	com.top_logic.react.flow.data.GanttSpan setStart(double value);

	double getEnd();

	/**
	 * @see #getEnd()
	 */
	com.top_logic.react.flow.data.GanttSpan setEnd(double value);

	boolean isCanResizeStart();

	/**
	 * @see #isCanResizeStart()
	 */
	com.top_logic.react.flow.data.GanttSpan setCanResizeStart(boolean value);

	boolean isCanResizeEnd();

	/**
	 * @see #isCanResizeEnd()
	 */
	com.top_logic.react.flow.data.GanttSpan setCanResizeEnd(boolean value);

	@Override
	com.top_logic.react.flow.data.GanttSpan setId(String value);

	@Override
	com.top_logic.react.flow.data.GanttSpan setRowId(String value);

	@Override
	com.top_logic.react.flow.data.GanttSpan setBox(com.top_logic.react.flow.data.Box value);

	@Override
	com.top_logic.react.flow.data.GanttSpan setCanMoveTime(boolean value);

	@Override
	com.top_logic.react.flow.data.GanttSpan setCanMoveRow(boolean value);

	@Override
	com.top_logic.react.flow.data.GanttSpan setCanBeEdgeSource(boolean value);

	@Override
	com.top_logic.react.flow.data.GanttSpan setCanBeEdgeTarget(boolean value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.GanttSpan readGanttSpan(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.GanttSpan) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert GANTT_SPAN__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.GanttSpan_Impl result = new com.top_logic.react.flow.data.impl.GanttSpan_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

}
