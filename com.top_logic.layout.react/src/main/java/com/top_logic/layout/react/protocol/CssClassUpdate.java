package com.top_logic.layout.react.protocol;

/**
 * Updates the CSS class attribute of an element.
 */
public interface CssClassUpdate extends SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.CssClassUpdate} instance.
	 */
	static com.top_logic.layout.react.protocol.CssClassUpdate create() {
		return new com.top_logic.layout.react.protocol.impl.CssClassUpdate_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.CssClassUpdate} type in JSON format. */
	String CSS_CLASS_UPDATE__TYPE = "CssClassUpdate";

	/** @see #getElementId() */
	String ELEMENT_ID__PROP = "elementId";

	/** @see #getCssClass() */
	String CSS_CLASS__PROP = "cssClass";

	/**
	 * The ID of the target element.
	 */
	String getElementId();

	/**
	 * @see #getElementId()
	 */
	com.top_logic.layout.react.protocol.CssClassUpdate setElementId(String value);

	/**
	 * The new CSS class string.
	 */
	String getCssClass();

	/**
	 * @see #getCssClass()
	 */
	com.top_logic.layout.react.protocol.CssClassUpdate setCssClass(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.CssClassUpdate readCssClassUpdate(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.CssClassUpdate_Impl result = new com.top_logic.layout.react.protocol.impl.CssClassUpdate_Impl();
		result.readContent(in);
		return result;
	}

}
