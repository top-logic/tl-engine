package com.top_logic.layout.react.protocol;

/**
 * Tells the client to update the browser URL ({@code pushState} or {@code replaceState}).
 * Sent after a route-forming navigation on the server.
 */
public interface RouteChangeEvent extends com.top_logic.layout.react.protocol.SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.RouteChangeEvent} instance.
	 */
	static com.top_logic.layout.react.protocol.RouteChangeEvent create() {
		return new com.top_logic.layout.react.protocol.impl.RouteChangeEvent_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.RouteChangeEvent} type in JSON format. */
	String ROUTE_CHANGE_EVENT__TYPE = "RouteChangeEvent";

	/** @see #getUrl() */
	String URL__PROP = "url";

	/** @see #isReplace() */
	String REPLACE__PROP = "replace";

	/**
	 * The new URL path to display in the browser address bar (relative to view base).
	 */
	String getUrl();

	/**
	 * @see #getUrl()
	 */
	com.top_logic.layout.react.protocol.RouteChangeEvent setUrl(String value);

	/**
	 * If true, use {@code replaceState} instead of {@code pushState} (no new history entry).
	 */
	boolean isReplace();

	/**
	 * @see #isReplace()
	 */
	com.top_logic.layout.react.protocol.RouteChangeEvent setReplace(boolean value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.RouteChangeEvent readRouteChangeEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.RouteChangeEvent_Impl result = new com.top_logic.layout.react.protocol.impl.RouteChangeEvent_Impl();
		result.readContent(in);
		return result;
	}

}
