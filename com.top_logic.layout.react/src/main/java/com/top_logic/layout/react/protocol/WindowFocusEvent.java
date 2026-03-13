package com.top_logic.layout.react.protocol;

/**
 * Instructs the target window to focus a previously opened window.
 */
public interface WindowFocusEvent extends SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.WindowFocusEvent} instance.
	 */
	static com.top_logic.layout.react.protocol.WindowFocusEvent create() {
		return new com.top_logic.layout.react.protocol.impl.WindowFocusEvent_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.WindowFocusEvent} type in JSON format. */
	String WINDOW_FOCUS_EVENT__TYPE = "WindowFocusEvent";

	/** @see #getTargetWindowId() */
	String TARGET_WINDOW_ID__PROP = "targetWindowId";

	/** @see #getWindowId() */
	String WINDOW_ID__PROP = "windowId";

	/**
	 * The window ID of the window that opened the target.
	 */
	String getTargetWindowId();

	/**
	 * @see #getTargetWindowId()
	 */
	com.top_logic.layout.react.protocol.WindowFocusEvent setTargetWindowId(String value);

	/**
	 * The window ID to focus.
	 */
	String getWindowId();

	/**
	 * @see #getWindowId()
	 */
	com.top_logic.layout.react.protocol.WindowFocusEvent setWindowId(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.WindowFocusEvent readWindowFocusEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.WindowFocusEvent_Impl result = new com.top_logic.layout.react.protocol.impl.WindowFocusEvent_Impl();
		result.readContent(in);
		return result;
	}

}
