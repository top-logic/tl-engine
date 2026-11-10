package com.top_logic.layout.react.protocol;

/**
 * Tells the client to open an address a command computed - a web page, an e-mail to write.
 *
 * <p>
 * Sent when a command opens an address outside the application, e.g. the homepage of the selected
 * object. In a window of its own, the page of the application stays as it is; in the window of the
 * application, the address replaces the page as a {@link PageNavigationEvent} does.
 * </p>
 */
public interface OpenUrlEvent extends com.top_logic.layout.react.protocol.SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.OpenUrlEvent} instance.
	 */
	static com.top_logic.layout.react.protocol.OpenUrlEvent create() {
		return new com.top_logic.layout.react.protocol.impl.OpenUrlEvent_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.OpenUrlEvent} type in JSON format. */
	String OPEN_URL_EVENT__TYPE = "OpenUrlEvent";

	/** @see #getUrl() */
	String URL__PROP = "url";

	/** @see #isNewWindow() */
	String NEW_WINDOW__PROP = "newWindow";

	/**
	 * The address to open, as a link names it: absolute, or relative to the address of the page.
	 */
	String getUrl();

	/**
	 * @see #getUrl()
	 */
	com.top_logic.layout.react.protocol.OpenUrlEvent setUrl(String value);

	/**
	 * Whether the address opens in a browser window (or tab) of its own instead of the window of the
	 * application.
	 */
	boolean isNewWindow();

	/**
	 * @see #isNewWindow()
	 */
	com.top_logic.layout.react.protocol.OpenUrlEvent setNewWindow(boolean value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.OpenUrlEvent readOpenUrlEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.OpenUrlEvent_Impl result = new com.top_logic.layout.react.protocol.impl.OpenUrlEvent_Impl();
		result.readContent(in);
		return result;
	}

}
