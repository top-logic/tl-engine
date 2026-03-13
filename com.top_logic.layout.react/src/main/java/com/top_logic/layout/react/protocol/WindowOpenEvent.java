package com.top_logic.layout.react.protocol;

/**
 * Instructs the target window to open a new browser window.
 * Only the window matching targetWindowId should act on this event.
 */
public interface WindowOpenEvent extends SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.WindowOpenEvent} instance.
	 */
	static com.top_logic.layout.react.protocol.WindowOpenEvent create() {
		return new com.top_logic.layout.react.protocol.impl.WindowOpenEvent_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.WindowOpenEvent} type in JSON format. */
	String WINDOW_OPEN_EVENT__TYPE = "WindowOpenEvent";

	/** @see #getTargetWindowId() */
	String TARGET_WINDOW_ID__PROP = "targetWindowId";

	/** @see #getWindowId() */
	String WINDOW_ID__PROP = "windowId";

	/** @see #getWidth() */
	String WIDTH__PROP = "width";

	/** @see #getHeight() */
	String HEIGHT__PROP = "height";

	/** @see #getTitle() */
	String TITLE__PROP = "title";

	/** @see #isResizable() */
	String RESIZABLE__PROP = "resizable";

	/**
	 * The window ID of the opener (for client-side filtering).
	 */
	String getTargetWindowId();

	/**
	 * @see #getTargetWindowId()
	 */
	com.top_logic.layout.react.protocol.WindowOpenEvent setTargetWindowId(String value);

	/**
	 * The window ID for the new window.
	 */
	String getWindowId();

	/**
	 * @see #getWindowId()
	 */
	com.top_logic.layout.react.protocol.WindowOpenEvent setWindowId(String value);

	/**
	 * Window width in pixels.
	 */
	int getWidth();

	/**
	 * @see #getWidth()
	 */
	com.top_logic.layout.react.protocol.WindowOpenEvent setWidth(int value);

	/**
	 * Window height in pixels.
	 */
	int getHeight();

	/**
	 * @see #getHeight()
	 */
	com.top_logic.layout.react.protocol.WindowOpenEvent setHeight(int value);

	/**
	 * Window title.
	 */
	String getTitle();

	/**
	 * @see #getTitle()
	 */
	com.top_logic.layout.react.protocol.WindowOpenEvent setTitle(String value);

	/**
	 * Whether the window is resizable.
	 */
	boolean isResizable();

	/**
	 * @see #isResizable()
	 */
	com.top_logic.layout.react.protocol.WindowOpenEvent setResizable(boolean value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.WindowOpenEvent readWindowOpenEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.WindowOpenEvent_Impl result = new com.top_logic.layout.react.protocol.impl.WindowOpenEvent_Impl();
		result.readContent(in);
		return result;
	}

}
