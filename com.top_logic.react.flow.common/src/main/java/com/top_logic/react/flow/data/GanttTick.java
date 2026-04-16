package com.top_logic.react.flow.data;

/**
 * A tick on the Gantt time axis.
 */
public interface GanttTick extends de.haumacher.msgbuf.graph.SharedGraphNode {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.GanttTick} instance.
	 */
	static com.top_logic.react.flow.data.GanttTick create() {
		return new com.top_logic.react.flow.data.impl.GanttTick_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.GanttTick} type in JSON format. */
	String GANTT_TICK__TYPE = "GanttTick";

	/** @see #getPosition() */
	String POSITION__PROP = "position";

	/** @see #getLabel() */
	String LABEL__PROP = "label";

	/** @see #getEmphasis() */
	String EMPHASIS__PROP = "emphasis";

	/**
	 * Position of the tick on the axis; see {@link GanttAxis} for position semantics.
	 */
	double getPosition();

	/**
	 * @see #getPosition()
	 */
	com.top_logic.react.flow.data.GanttTick setPosition(double value);

	/**
	 * Label box drawn at the tick.
	 *
	 * <p>
	 * Using a {@link Box} instead of a plain string gives the {@link AxisProvider} full control
	 * over the label appearance: emphasis ticks can carry bold text, coloured year markers, etc.,
	 * without any special-casing in the chart rendering code. The label is owned by
	 * {@link GanttLayout#getContents()} and rendered through the standard contents dispatch.
	 * </p>
	 */
	com.top_logic.react.flow.data.Box getLabel();

	/**
	 * @see #getLabel()
	 */
	com.top_logic.react.flow.data.GanttTick setLabel(com.top_logic.react.flow.data.Box value);

	/**
	 * Checks, whether {@link #getLabel()} has a value.
	 */
	boolean hasLabel();

	/**
	 * Emphasis in [0..1] — renderer interpolates stroke width and label prominence.
	 */
	double getEmphasis();

	/**
	 * @see #getEmphasis()
	 */
	com.top_logic.react.flow.data.GanttTick setEmphasis(double value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.GanttTick readGanttTick(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.GanttTick) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert GANTT_TICK__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.GanttTick_Impl result = new com.top_logic.react.flow.data.impl.GanttTick_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

}
