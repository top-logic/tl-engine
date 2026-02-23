package com.top_logic.layout.react.protocol;

/**
 * Updates one or more properties on an element.
 */
public interface PropertyUpdate extends SSEEvent {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.PropertyUpdate} instance.
	 */
	static com.top_logic.layout.react.protocol.PropertyUpdate create() {
		return new com.top_logic.layout.react.protocol.impl.PropertyUpdate_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.PropertyUpdate} type in JSON format. */
	String PROPERTY_UPDATE__TYPE = "PropertyUpdate";

	/** @see #getElementId() */
	String ELEMENT_ID__PROP = "elementId";

	/** @see #getProperties() */
	String PROPERTIES__PROP = "properties";

	/**
	 * The ID of the target element.
	 */
	String getElementId();

	/**
	 * @see #getElementId()
	 */
	com.top_logic.layout.react.protocol.PropertyUpdate setElementId(String value);

	/**
	 * The properties to update.
	 */
	java.util.List<com.top_logic.layout.react.protocol.Property> getProperties();

	/**
	 * @see #getProperties()
	 */
	com.top_logic.layout.react.protocol.PropertyUpdate setProperties(java.util.List<? extends com.top_logic.layout.react.protocol.Property> value);

	/**
	 * Adds a value to the {@link #getProperties()} list.
	 */
	com.top_logic.layout.react.protocol.PropertyUpdate addProperty(com.top_logic.layout.react.protocol.Property value);

	/**
	 * Removes a value from the {@link #getProperties()} list.
	 */
	void removeProperty(com.top_logic.layout.react.protocol.Property value);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.PropertyUpdate readPropertyUpdate(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.PropertyUpdate_Impl result = new com.top_logic.layout.react.protocol.impl.PropertyUpdate_Impl();
		result.readContent(in);
		return result;
	}

}
