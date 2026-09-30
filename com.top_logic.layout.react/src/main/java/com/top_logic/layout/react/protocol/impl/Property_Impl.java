package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.Property}.
 */
public class Property_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.layout.react.protocol.Property {

	private String _name = "";

	private String _value = "";

	/**
	 * Creates a {@link Property_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.Property#create()
	 */
	public Property_Impl() {
		super();
	}

	@Override
	public final String getName() {
		return _name;
	}

	@Override
	public com.top_logic.layout.react.protocol.Property setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	@Override
	public final String getValue() {
		return _value;
	}

	@Override
	public com.top_logic.layout.react.protocol.Property setValue(String value) {
		internalSetValue(value);
		return this;
	}

	/** Internal setter for {@link #getValue()} without chain call utility. */
	protected final void internalSetValue(String value) {
		_value = value;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(NAME__PROP);
		out.value(getName());
		out.name(VALUE__PROP);
		out.value(getValue());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case VALUE__PROP: setValue(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
