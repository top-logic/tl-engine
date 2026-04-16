package com.top_logic.react.flow.data;

/**
 * Time axis configuration for a {@link GanttLayout}.
 *
 * <h3>Positions vs. TimeValues</h3>
 *
 * <p>
 * All numeric position-like fields in the Gantt model ({@link #getRangeMin},
	 * {@link #getRangeMax}, {@link GanttTick#getPosition}, {@link GanttSpan#getStart},
	 * {@link GanttSpan#getEnd}, {@link GanttMilestone#getAt}, {@link GanttLineDecoration#getAt},
	 * {@link GanttRangeDecoration#getFrom}, {@link GanttRangeDecoration#getTo}) are
 * <strong>positions in layout units</strong> — plain doubles shipped with the
 * synchronised diagram model and used on the client for geometric layout, hit-testing
 * and rendering.
 * </p>
 *
 * <p>
 * The application-specific <em>time value</em> (e.g. {@code java.time.LocalDate},
 * a sprint identifier, a millisecond timestamp) is <strong>not</strong> part of this
 * model and never travels to the client. It lives only server-side, inside the
 * application's axis provider. The provider converts between its opaque time values
 * and chart positions via {@code toPosition(timeValue) → position} /
 * {@code fromPosition(position) → timeValue}; the client sees only the result.
 * </p>
 *
 * <p>
 * By convention, one position unit at {@link #getCurrentZoom() zoom 1.0} corresponds
 * to one pixel on the chart. What one position unit <em>means in domain terms</em>
 * (e.g. one day, one sprint, one second) is defined by the axis provider referenced
 * by {@link #getProviderId() providerId}.
 * </p>
 */
public interface GanttAxis extends de.haumacher.msgbuf.graph.SharedGraphNode {

	/**
	 * Creates a {@link com.top_logic.react.flow.data.GanttAxis} instance.
	 */
	static com.top_logic.react.flow.data.GanttAxis create() {
		return new com.top_logic.react.flow.data.impl.GanttAxis_Impl();
	}

	/** Identifier for the {@link com.top_logic.react.flow.data.GanttAxis} type in JSON format. */
	String GANTT_AXIS__TYPE = "GanttAxis";

	/** @see #getProviderId() */
	String PROVIDER_ID__PROP = "providerId";

	/** @see #getRangeMin() */
	String RANGE_MIN__PROP = "rangeMin";

	/** @see #getRangeMax() */
	String RANGE_MAX__PROP = "rangeMax";

	/** @see #getCurrentZoom() */
	String CURRENT_ZOOM__PROP = "currentZoom";

	/** @see #getSnapGranularity() */
	String SNAP_GRANULARITY__PROP = "snapGranularity";

	/** @see #getCurrentTicks() */
	String CURRENT_TICKS__PROP = "currentTicks";

	/**
	 * Name of the server-side axis provider that computes ticks and snap data, and
	 * converts between the application's opaque time values and chart positions.
	 */
	String getProviderId();

	/**
	 * @see #getProviderId()
	 */
	com.top_logic.react.flow.data.GanttAxis setProviderId(String value);

	/**
	 * Lowest position representable on the axis, in layout units (pixels at zoom 1.0).
	 * The domain meaning of one unit is defined by the associated {@link AxisProvider}.
	 */
	double getRangeMin();

	/**
	 * @see #getRangeMin()
	 */
	com.top_logic.react.flow.data.GanttAxis setRangeMin(double value);

	/**
	 * Highest position representable on the axis, in layout units (pixels at zoom 1.0).
	 * The domain meaning of one unit is defined by the associated {@link AxisProvider}.
	 */
	double getRangeMax();

	/**
	 * @see #getRangeMax()
	 */
	com.top_logic.react.flow.data.GanttAxis setRangeMax(double value);

	/**
	 * Current zoom factor: number of pixels per position unit. {@code 1.0} renders one
	 * position unit as one pixel. Written by the client on zoom changes; the server reacts
	 * by recomputing {@link #getCurrentTicks() ticks} and {@link #getSnapGranularity snap
	 * granularity} via the provider.
	 */
	double getCurrentZoom();

	/**
	 * @see #getCurrentZoom()
	 */
	com.top_logic.react.flow.data.GanttAxis setCurrentZoom(double value);

	/**
	 * Granularity used by the client to snap drag positions, in position units (not pixels).
	 * A value of {@code 1.0} means: snap to the nearest integer position unit.
	 */
	double getSnapGranularity();

	/**
	 * @see #getSnapGranularity()
	 */
	com.top_logic.react.flow.data.GanttAxis setSnapGranularity(double value);

	/**
	 * Axis ticks computed for the current zoom, produced by the {@link AxisProvider}.
	 */
	java.util.List<com.top_logic.react.flow.data.GanttTick> getCurrentTicks();

	/**
	 * @see #getCurrentTicks()
	 */
	com.top_logic.react.flow.data.GanttAxis setCurrentTicks(java.util.List<? extends com.top_logic.react.flow.data.GanttTick> value);

	/**
	 * Adds a value to the {@link #getCurrentTicks()} list.
	 */
	com.top_logic.react.flow.data.GanttAxis addCurrentTick(com.top_logic.react.flow.data.GanttTick value);

	/**
	 * Removes a value from the {@link #getCurrentTicks()} list.
	 */
	void removeCurrentTick(com.top_logic.react.flow.data.GanttTick value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.react.flow.data.GanttAxis readGanttAxis(de.haumacher.msgbuf.graph.Scope scope, de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		if (in.peek() == de.haumacher.msgbuf.json.JsonToken.NUMBER) {
			return (com.top_logic.react.flow.data.GanttAxis) scope.resolveOrFail(in.nextInt());
		}
		in.beginArray();
		String type = in.nextString();
		assert GANTT_AXIS__TYPE.equals(type);
		int id = in.nextInt();
		com.top_logic.react.flow.data.impl.GanttAxis_Impl result = new com.top_logic.react.flow.data.impl.GanttAxis_Impl();
		scope.readData(result, id, in);
		in.endArray();
		return result;
	}

}
