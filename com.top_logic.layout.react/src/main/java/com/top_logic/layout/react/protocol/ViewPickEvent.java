package com.top_logic.layout.react.protocol;

/**
 * Instructs the target (main) window to enter "select view" pick mode.
 */
public interface ViewPickEvent extends com.top_logic.layout.react.protocol.SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.ViewPickEvent} instance.
	 */
	static com.top_logic.layout.react.protocol.ViewPickEvent create() {
		return new com.top_logic.layout.react.protocol.impl.ViewPickEvent_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.ViewPickEvent} type in JSON format. */
	String VIEW_PICK_EVENT__TYPE = "ViewPickEvent";

	/** @see #getToken() */
	String TOKEN__PROP = "token";

	/** @see #getTargetWindowId() */
	String TARGET_WINDOW_ID__PROP = "targetWindowId";

	/**
	 * Correlation token identifying the pending pick registration on the server.
	 */
	String getToken();

	/**
	 * @see #getToken()
	 */
	com.top_logic.layout.react.protocol.ViewPickEvent setToken(String value);

	/**
	 * The window ID that should enter pick mode (the designer's opener / main window).
	 */
	String getTargetWindowId();

	/**
	 * @see #getTargetWindowId()
	 */
	com.top_logic.layout.react.protocol.ViewPickEvent setTargetWindowId(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.ViewPickEvent readViewPickEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.ViewPickEvent_Impl result = new com.top_logic.layout.react.protocol.impl.ViewPickEvent_Impl();
		result.readContent(in);
		return result;
	}

}
