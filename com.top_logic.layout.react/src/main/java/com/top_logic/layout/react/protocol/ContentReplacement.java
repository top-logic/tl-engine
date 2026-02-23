package com.top_logic.layout.react.protocol;

/**
 * Replaces the inner content of an element.
 */
public interface ContentReplacement extends SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.ContentReplacement} instance.
	 */
	static com.top_logic.layout.react.protocol.ContentReplacement create() {
		return new com.top_logic.layout.react.protocol.impl.ContentReplacement_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.ContentReplacement} type in JSON format. */
	String CONTENT_REPLACEMENT__TYPE = "ContentReplacement";

	/** @see #getElementId() */
	String ELEMENT_ID__PROP = "elementId";

	/** @see #getHtml() */
	String HTML__PROP = "html";

	/**
	 * The ID of the target element.
	 */
	String getElementId();

	/**
	 * @see #getElementId()
	 */
	com.top_logic.layout.react.protocol.ContentReplacement setElementId(String value);

	/**
	 * The new inner HTML.
	 */
	String getHtml();

	/**
	 * @see #getHtml()
	 */
	com.top_logic.layout.react.protocol.ContentReplacement setHtml(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.ContentReplacement readContentReplacement(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.ContentReplacement_Impl result = new com.top_logic.layout.react.protocol.impl.ContentReplacement_Impl();
		result.readContent(in);
		return result;
	}

}
