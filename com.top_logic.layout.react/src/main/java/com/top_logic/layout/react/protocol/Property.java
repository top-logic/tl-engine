package com.top_logic.layout.react.protocol;

/**
 * A single property name-value pair.
 */
public interface Property extends de.haumacher.msgbuf.data.DataObject, de.haumacher.msgbuf.observer.Observable {

	/**
	 * Creates a {@link com.top_logic.layout.react.protocol.Property} instance.
	 */
	static com.top_logic.layout.react.protocol.Property create() {
		return new com.top_logic.layout.react.protocol.impl.Property_Impl();
	}

	/** Identifier for the {@link com.top_logic.layout.react.protocol.Property} type in JSON format. */
	String PROPERTY__TYPE = "Property";

	/** @see #getName() */
	String NAME__PROP = "name";

	/** @see #getValue() */
	String VALUE__PROP = "value";

	/**
	 * The property name.
	 */
	String getName();

	/**
	 * @see #getName()
	 */
	com.top_logic.layout.react.protocol.Property setName(String value);

	/**
	 * The property value rendered as a string.
	 */
	String getValue();

	/**
	 * @see #getValue()
	 */
	com.top_logic.layout.react.protocol.Property setValue(String value);

	@Override
	public com.top_logic.layout.react.protocol.Property registerListener(de.haumacher.msgbuf.observer.Listener l);

	@Override
	public com.top_logic.layout.react.protocol.Property unregisterListener(de.haumacher.msgbuf.observer.Listener l);

	/** Reads a new instance from the given reader. */
	static com.top_logic.layout.react.protocol.Property readProperty(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.layout.react.protocol.impl.Property_Impl result = new com.top_logic.layout.react.protocol.impl.Property_Impl();
		result.readContent(in);
		return result;
	}

}
