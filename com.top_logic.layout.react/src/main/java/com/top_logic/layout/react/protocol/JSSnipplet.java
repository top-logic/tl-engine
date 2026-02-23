package com.top_logic.layout.react.protocol;

/**
 * Executes a JavaScript code snippet.
 */
public interface JSSnipplet extends SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.JSSnipplet} instance.
	 */
	static com.top_logic.layout.react.protocol.JSSnipplet create() {
		return new com.top_logic.layout.react.protocol.impl.JSSnipplet_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.JSSnipplet} type in JSON format. */
	String JSSNIPPLET__TYPE = "JSSnipplet";

	/** @see #getCode() */
	String CODE__PROP = "code";

	/**
	 * The JavaScript code to execute.
	 */
	String getCode();

	/**
	 * @see #getCode()
	 */
	com.top_logic.layout.react.protocol.JSSnipplet setCode(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.JSSnipplet readJSSnipplet(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.JSSnipplet_Impl result = new com.top_logic.layout.react.protocol.impl.JSSnipplet_Impl();
		result.readContent(in);
		return result;
	}

}
