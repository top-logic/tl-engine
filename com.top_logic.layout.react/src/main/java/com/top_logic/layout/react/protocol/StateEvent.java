package com.top_logic.layout.react.protocol;

/**
 * Full state replacement for a React control.
 */
public interface StateEvent extends SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.StateEvent} instance.
	 */
	static com.top_logic.layout.react.protocol.StateEvent create() {
		return new com.top_logic.layout.react.protocol.impl.StateEvent_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.StateEvent} type in JSON format. */
	String STATE_EVENT__TYPE = "StateEvent";

	/** @see #getControlId() */
	String CONTROL_ID__PROP = "controlId";

	/** @see #getState() */
	String STATE__PROP = "state";

	/**
	 * The control ID this state belongs to.
	 */
	String getControlId();

	/**
	 * @see #getControlId()
	 */
	com.top_logic.layout.react.protocol.StateEvent setControlId(String value);

	/**
	 * The full state object serialized as a JSON string.
	 */
	String getState();

	/**
	 * @see #getState()
	 */
	com.top_logic.layout.react.protocol.StateEvent setState(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.StateEvent readStateEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.StateEvent_Impl result = new com.top_logic.layout.react.protocol.impl.StateEvent_Impl();
		result.readContent(in);
		return result;
	}

}
