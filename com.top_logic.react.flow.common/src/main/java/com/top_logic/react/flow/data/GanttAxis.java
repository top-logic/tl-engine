package com.top_logic.react.flow.data;

/**
 * Time axis configuration for a {@link GanttLayout}.
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
	 * Name of the server-side axis provider that computes ticks and snap data.
	 */
	String getProviderId();

	/**
	 * @see #getProviderId()
	 */
	com.top_logic.react.flow.data.GanttAxis setProviderId(String value);

	/**
	 * Minimum representable position (zoom 100%).
	 */
	double getRangeMin();

	/**
	 * @see #getRangeMin()
	 */
	com.top_logic.react.flow.data.GanttAxis setRangeMin(double value);

	/**
	 * Maximum representable position (zoom 100%).
	 */
	double getRangeMax();

	/**
	 * @see #getRangeMax()
	 */
	com.top_logic.react.flow.data.GanttAxis setRangeMax(double value);

	/**
	 * Current zoom (pixels per position unit). Written by the client on zoom changes.
	 */
	double getCurrentZoom();

	/**
	 * @see #getCurrentZoom()
	 */
	com.top_logic.react.flow.data.GanttAxis setCurrentZoom(double value);

	/**
	 * Granularity for client-side drag snap, in position units.
	 */
	double getSnapGranularity();

	/**
	 * @see #getSnapGranularity()
	 */
	com.top_logic.react.flow.data.GanttAxis setSnapGranularity(double value);

	/**
	 * Ticks computed for the current zoom.
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
