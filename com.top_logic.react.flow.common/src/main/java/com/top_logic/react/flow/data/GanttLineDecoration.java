package com.top_logic.react.flow.data;

/**
 * Vertical line at a single position on the time axis.
 */
public interface GanttLineDecoration extends GanttDecoration {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.GanttLineDecoration} instance.
	 */
	static com.top_logic.react.flow.data.GanttLineDecoration create() {
		return new com.top_logic.react.flow.data.impl.GanttLineDecoration_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.GanttLineDecoration} type in JSON format. */
	String GANTT_LINE_DECORATION__TYPE = "GanttLineDecoration";

	/** @see #getAt() */
	String AT__PROP = "at";

	/**
	 * Position of the line on the axis; see {@link GanttAxis} for position semantics.
	 */
	double getAt();

	/**
	 * @see #getAt()
	 */
	com.top_logic.react.flow.data.GanttLineDecoration setAt(double value);

	@Override
	com.top_logic.react.flow.data.GanttLineDecoration setId(String value);

	@Override
	com.top_logic.react.flow.data.GanttLineDecoration setUserObject(java.lang.Object value);

	@Override
	com.top_logic.react.flow.data.GanttLineDecoration setColor(String value);

	@Override
	com.top_logic.react.flow.data.GanttLineDecoration setLabel(com.top_logic.react.flow.data.Box value);

	@Override
	com.top_logic.react.flow.data.GanttLineDecoration setCanMove(boolean value);

	@Override
	com.top_logic.react.flow.data.GanttLineDecoration setRelevantFor(java.util.List<? extends String> value);

	@Override
	com.top_logic.react.flow.data.GanttLineDecoration addRelevantFor(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.GanttLineDecoration readGanttLineDecoration(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.GanttLineDecoration) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert GANTT_LINE_DECORATION__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.GanttLineDecoration_Impl result = new com.top_logic.react.flow.data.impl.GanttLineDecoration_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

}
