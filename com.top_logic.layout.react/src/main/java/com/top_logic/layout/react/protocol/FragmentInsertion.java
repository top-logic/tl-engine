package com.top_logic.layout.react.protocol;

/**
 * Inserts an HTML fragment relative to an element.
 */
public interface FragmentInsertion extends SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.FragmentInsertion} instance.
	 */
	static com.top_logic.layout.react.protocol.FragmentInsertion create() {
		return new com.top_logic.layout.react.protocol.impl.FragmentInsertion_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.FragmentInsertion} type in JSON format. */
	String FRAGMENT_INSERTION__TYPE = "FragmentInsertion";

	/** @see #getElementId() */
	String ELEMENT_ID__PROP = "elementId";

	/** @see #getPosition() */
	String POSITION__PROP = "position";

	/** @see #getHtml() */
	String HTML__PROP = "html";

	/**
	 * The ID of the reference element.
	 */
	String getElementId();

	/**
	 * @see #getElementId()
	 */
	com.top_logic.layout.react.protocol.FragmentInsertion setElementId(String value);

	/**
	 * The position relative to the element ({@code beforeBegin}, {@code afterBegin}, {@code beforeEnd}, {@code afterEnd}).
	 */
	String getPosition();

	/**
	 * @see #getPosition()
	 */
	com.top_logic.layout.react.protocol.FragmentInsertion setPosition(String value);

	/**
	 * The HTML to insert.
	 */
	String getHtml();

	/**
	 * @see #getHtml()
	 */
	com.top_logic.layout.react.protocol.FragmentInsertion setHtml(String value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.FragmentInsertion readFragmentInsertion(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.FragmentInsertion_Impl result = new com.top_logic.layout.react.protocol.impl.FragmentInsertion_Impl();
		result.readContent(in);
		return result;
	}

}
