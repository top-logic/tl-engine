package com.top_logic.layout.react.protocol;

/**
 * Tells the client to correct the URL after a vetoed back-navigation.
 * Sent when a dirty-channel veto prevents the requested navigation.
 */
public interface RouteVetoEvent extends SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.RouteVetoEvent} instance.
	 */
	static com.top_logic.layout.react.protocol.RouteVetoEvent create() {
		return new com.top_logic.layout.react.protocol.impl.RouteVetoEvent_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.RouteVetoEvent} type in JSON format. */
	String ROUTE_VETO_EVENT__TYPE = "RouteVetoEvent";

	/** @see #getCurrentUrl() */
	String CURRENT_URL__PROP = "currentUrl";

	/**
	 * The current (correct) URL to restore in the address bar.
	 */
	String getCurrentUrl();

	/**
	 * @see #getCurrentUrl()
	 */
	com.top_logic.layout.react.protocol.RouteVetoEvent setCurrentUrl(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.RouteVetoEvent readRouteVetoEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.RouteVetoEvent_Impl result = new com.top_logic.layout.react.protocol.impl.RouteVetoEvent_Impl();
		result.readContent(in);
		return result;
	}

}
