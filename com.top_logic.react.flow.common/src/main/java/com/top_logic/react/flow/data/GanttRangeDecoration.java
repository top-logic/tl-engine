package com.top_logic.react.flow.data;

/**
 * Coloured range between two positions.
 */
public interface GanttRangeDecoration extends GanttDecoration {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.GanttRangeDecoration} instance.
	 */
	static com.top_logic.react.flow.data.GanttRangeDecoration create() {
		return new com.top_logic.react.flow.data.impl.GanttRangeDecoration_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.GanttRangeDecoration} type in JSON format. */
	String GANTT_RANGE_DECORATION__TYPE = "GanttRangeDecoration";

	/** @see #getFrom() */
	String FROM__PROP = "from";

	/** @see #getTo() */
	String TO__PROP = "to";

	/** @see #isCanResize() */
	String CAN_RESIZE__PROP = "canResize";

	/**
	 * Start position of the range (layout units at zoom 100%).
	 */
	double getFrom();

	/**
	 * @see #getFrom()
	 */
	com.top_logic.react.flow.data.GanttRangeDecoration setFrom(double value);

	/**
	 * End position of the range (layout units at zoom 100%); must be {@code >= from}.
	 */
	double getTo();

	/**
	 * @see #getTo()
	 */
	com.top_logic.react.flow.data.GanttRangeDecoration setTo(double value);

	/**
	 * Whether the user may drag the range's edges to resize it.
	 */
	boolean isCanResize();

	/**
	 * @see #isCanResize()
	 */
	com.top_logic.react.flow.data.GanttRangeDecoration setCanResize(boolean value);

	@Override
	com.top_logic.react.flow.data.GanttRangeDecoration setId(String value);

	@Override
	com.top_logic.react.flow.data.GanttRangeDecoration setColor(String value);

	@Override
	com.top_logic.react.flow.data.GanttRangeDecoration setLabel(String value);

	@Override
	com.top_logic.react.flow.data.GanttRangeDecoration setCanMove(boolean value);

	@Override
	com.top_logic.react.flow.data.GanttRangeDecoration setRelevantFor(java.util.List<? extends String> value);

	@Override
	com.top_logic.react.flow.data.GanttRangeDecoration addRelevantFor(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.GanttRangeDecoration readGanttRangeDecoration(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.GanttRangeDecoration) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert GANTT_RANGE_DECORATION__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.GanttRangeDecoration_Impl result = new com.top_logic.react.flow.data.impl.GanttRangeDecoration_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

}
