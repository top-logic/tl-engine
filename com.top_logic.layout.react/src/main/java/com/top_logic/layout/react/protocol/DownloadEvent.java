package com.top_logic.layout.react.protocol;

/**
 * Tells the client to download a file the server has prepared for this window.
 *
 * <p>
 * Sent when a command hands a file to the user - an export, a generated document. The client fetches
 * {@code url} as a download, so the browser saves the file instead of displaying it. The file can be
 * fetched once.
 * </p>
 */
public interface DownloadEvent extends com.top_logic.layout.react.protocol.SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.DownloadEvent} instance.
	 */
	static com.top_logic.layout.react.protocol.DownloadEvent create() {
		return new com.top_logic.layout.react.protocol.impl.DownloadEvent_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.DownloadEvent} type in JSON format. */
	String DOWNLOAD_EVENT__TYPE = "DownloadEvent";

	/** @see #getUrl() */
	String URL__PROP = "url";

	/** @see #getFileName() */
	String FILE_NAME__PROP = "fileName";

	/**
	 * The address of the prepared file, relative to the context path of the application.
	 */
	String getUrl();

	/**
	 * @see #getUrl()
	 */
	com.top_logic.layout.react.protocol.DownloadEvent setUrl(String value);

	/**
	 * The name the file is saved under.
	 */
	String getFileName();

	/**
	 * @see #getFileName()
	 */
	com.top_logic.layout.react.protocol.DownloadEvent setFileName(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.DownloadEvent readDownloadEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.DownloadEvent_Impl result = new com.top_logic.layout.react.protocol.impl.DownloadEvent_Impl();
		result.readContent(in);
		return result;
	}

}
