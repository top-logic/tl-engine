package com.top_logic.layout.react.protocol;

/**
 * Instructs the target window to close a previously opened window.
 */
public interface WindowCloseEvent extends SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.WindowCloseEvent} instance.
	 */
	static com.top_logic.layout.react.protocol.WindowCloseEvent create() {
		return new com.top_logic.layout.react.protocol.impl.WindowCloseEvent_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.WindowCloseEvent} type in JSON format. */
	String WINDOW_CLOSE_EVENT__TYPE = "WindowCloseEvent";

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
	com.top_logic.layout.react.protocol.WindowCloseEvent setTargetWindowId(String value);

	/**
	 * The window ID to close.
	 */
	String getWindowId();

	/**
	 * @see #getWindowId()
	 */
	com.top_logic.layout.react.protocol.WindowCloseEvent setWindowId(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.WindowCloseEvent readWindowCloseEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.WindowCloseEvent_Impl result = new com.top_logic.layout.react.protocol.impl.WindowCloseEvent_Impl();
		result.readContent(in);
		return result;
	}

}
