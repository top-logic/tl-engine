package com.top_logic.layout.react.protocol;

/**
 * Tells the client that the history move to this URL, refused over unsaved changes, may now be
 * made: the changes are resolved.
 */
public interface RouteResumeEvent extends com.top_logic.layout.react.protocol.SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.RouteResumeEvent} instance.
	 */
	static com.top_logic.layout.react.protocol.RouteResumeEvent create() {
		return new com.top_logic.layout.react.protocol.impl.RouteResumeEvent_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.RouteResumeEvent} type in JSON format. */
	String ROUTE_RESUME_EVENT__TYPE = "RouteResumeEvent";

	/** @see #getUrl() */
	String URL__PROP = "url";

	/**
	 * The URL of the refused move (relative to view base).
	 */
	String getUrl();

	/**
	 * @see #getUrl()
	 */
	com.top_logic.layout.react.protocol.RouteResumeEvent setUrl(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.RouteResumeEvent readRouteResumeEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.RouteResumeEvent_Impl result = new com.top_logic.layout.react.protocol.impl.RouteResumeEvent_Impl();
		result.readContent(in);
		return result;
	}

}
