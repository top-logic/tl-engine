package com.top_logic.layout.react.protocol;

/**
 * Instructs the target window to enter pick mode: the user clicks an element there and the client
 * reports what was hit back to the server.
 */
public interface PickEvent extends com.top_logic.layout.react.protocol.SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.PickEvent} instance.
	 */
	static com.top_logic.layout.react.protocol.PickEvent create() {
		return new com.top_logic.layout.react.protocol.impl.PickEvent_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.PickEvent} type in JSON format. */
	String PICK_EVENT__TYPE = "PickEvent";

	/** @see #getToken() */
	String TOKEN__PROP = "token";

	/** @see #getTargetWindowId() */
	String TARGET_WINDOW_ID__PROP = "targetWindowId";

	/** @see #getKind() */
	String KIND__PROP = "kind";

	/**
	 * Correlation token identifying the pending pick registration on the server.
	 */
	String getToken();

	/**
	 * @see #getToken()
	 */
	com.top_logic.layout.react.protocol.PickEvent setToken(String value);

	/**
	 * The window ID that should enter pick mode.
	 */
	String getTargetWindowId();

	/**
	 * @see #getTargetWindowId()
	 */
	com.top_logic.layout.react.protocol.PickEvent setTargetWindowId(String value);

	/**
	 * What the click is resolved to, as the external name of a {@code PickKind}: a view source
	 * ("view") or a mounted control ("control").
	 */
	String getKind();

	/**
	 * @see #getKind()
	 */
	com.top_logic.layout.react.protocol.PickEvent setKind(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.PickEvent readPickEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.PickEvent_Impl result = new com.top_logic.layout.react.protocol.impl.PickEvent_Impl();
		result.readContent(in);
		return result;
	}

}
