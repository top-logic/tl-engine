package com.top_logic.layout.react.protocol;

/**
 * Tells the client to leave the page it displays: to load it again, or to load another address.
 *
 * <p>
 * Sent when what the page shows no longer holds as a whole - a login or logout replaced the session,
 * the user chose another language, the server discarded the window's tree. The client stops talking
 * to the server for the page it leaves: it sends no further command and does not react to the
 * answers that the server, which may already have replaced the session, gives to requests still on
 * their way.
 * </p>
 */
public interface PageNavigationEvent extends com.top_logic.layout.react.protocol.SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.PageNavigationEvent} instance.
	 */
	static com.top_logic.layout.react.protocol.PageNavigationEvent create() {
		return new com.top_logic.layout.react.protocol.impl.PageNavigationEvent_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.PageNavigationEvent} type in JSON format. */
	String PAGE_NAVIGATION_EVENT__TYPE = "PageNavigationEvent";

	/** @see #getUrl() */
	String URL__PROP = "url";

	/**
	 * The address to load, as a link names it: absolute, or relative to the address of the page. Empty
	 * loads the displayed page again. The address names a page that replaces the displayed one, not a
	 * download: the client does not talk to the server for the page it left any more.
	 */
	String getUrl();

	/**
	 * @see #getUrl()
	 */
	com.top_logic.layout.react.protocol.PageNavigationEvent setUrl(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.PageNavigationEvent readPageNavigationEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.PageNavigationEvent_Impl result = new com.top_logic.layout.react.protocol.impl.PageNavigationEvent_Impl();
		result.readContent(in);
		return result;
	}

}
