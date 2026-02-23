package com.top_logic.layout.react.protocol;

/**
 * Replaces an entire element.
 */
public interface ElementReplacement extends SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.ElementReplacement} instance.
	 */
	static com.top_logic.layout.react.protocol.ElementReplacement create() {
		return new com.top_logic.layout.react.protocol.impl.ElementReplacement_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.ElementReplacement} type in JSON format. */
	String ELEMENT_REPLACEMENT__TYPE = "ElementReplacement";

	/** @see #getElementId() */
	String ELEMENT_ID__PROP = "elementId";

	/** @see #getHtml() */
	String HTML__PROP = "html";

	/**
	 * The ID of the element to replace.
	 */
	String getElementId();

	/**
	 * @see #getElementId()
	 */
	com.top_logic.layout.react.protocol.ElementReplacement setElementId(String value);

	/**
	 * The replacement HTML.
	 */
	String getHtml();

	/**
	 * @see #getHtml()
	 */
	com.top_logic.layout.react.protocol.ElementReplacement setHtml(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.ElementReplacement readElementReplacement(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.ElementReplacement_Impl result = new com.top_logic.layout.react.protocol.impl.ElementReplacement_Impl();
		result.readContent(in);
		return result;
	}

}
