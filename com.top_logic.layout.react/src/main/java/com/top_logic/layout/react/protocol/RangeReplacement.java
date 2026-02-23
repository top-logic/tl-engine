package com.top_logic.layout.react.protocol;

/**
 * Replaces a range of elements from startId to stopId.
 */
public interface RangeReplacement extends SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.RangeReplacement} instance.
	 */
	static com.top_logic.layout.react.protocol.RangeReplacement create() {
		return new com.top_logic.layout.react.protocol.impl.RangeReplacement_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.RangeReplacement} type in JSON format. */
	String RANGE_REPLACEMENT__TYPE = "RangeReplacement";

	/** @see #getStartId() */
	String START_ID__PROP = "startId";

	/** @see #getStopId() */
	String STOP_ID__PROP = "stopId";

	/** @see #getHtml() */
	String HTML__PROP = "html";

	/**
	 * The ID of the first element in the range.
	 */
	String getStartId();

	/**
	 * @see #getStartId()
	 */
	com.top_logic.layout.react.protocol.RangeReplacement setStartId(String value);

	/**
	 * The ID of the last element in the range.
	 */
	String getStopId();

	/**
	 * @see #getStopId()
	 */
	com.top_logic.layout.react.protocol.RangeReplacement setStopId(String value);

	/**
	 * The replacement HTML.
	 */
	String getHtml();

	/**
	 * @see #getHtml()
	 */
	com.top_logic.layout.react.protocol.RangeReplacement setHtml(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.RangeReplacement readRangeReplacement(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.RangeReplacement_Impl result = new com.top_logic.layout.react.protocol.impl.RangeReplacement_Impl();
		result.readContent(in);
		return result;
	}

}
