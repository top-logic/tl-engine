package com.top_logic.layout.react.state.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.state.Badge}.
 */
public class Badge_Impl extends de.haumacher.msgbuf.data.AbstractDataObject implements com.top_logic.layout.react.state.Badge {

	private String _content = "";

	private boolean _dot = false;

	private com.top_logic.layout.react.state.Badge.Role _role = com.top_logic.layout.react.state.Badge.Role.BRAND;

	private String _label = "";

	/**
	 * Creates a {@link Badge_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.state.Badge#create()
	 */
	public Badge_Impl() {
		super();
	}

	@Override
	public final String getContent() {
		return _content;
	}

	@Override
	public com.top_logic.layout.react.state.Badge setContent(String value) {
		internalSetContent(value);
		return this;
	}

	/** Internal setter for {@link #getContent()} without chain call utility. */
	protected final void internalSetContent(String value) {
		_content = value;
	}

	@Override
	public final boolean isDot() {
		return _dot;
	}

	@Override
	public com.top_logic.layout.react.state.Badge setDot(boolean value) {
		internalSetDot(value);
		return this;
	}

	/** Internal setter for {@link #isDot()} without chain call utility. */
	protected final void internalSetDot(boolean value) {
		_dot = value;
	}

	@Override
	public final com.top_logic.layout.react.state.Badge.Role getRole() {
		return _role;
	}

	@Override
	public com.top_logic.layout.react.state.Badge setRole(com.top_logic.layout.react.state.Badge.Role value) {
		internalSetRole(value);
		return this;
	}

	/** Internal setter for {@link #getRole()} without chain call utility. */
	protected final void internalSetRole(com.top_logic.layout.react.state.Badge.Role value) {
		if (value == null) throw new IllegalArgumentException("Property 'role' cannot be null.");
		_role = value;
	}

	@Override
	public final String getLabel() {
		return _label;
	}

	@Override
	public com.top_logic.layout.react.state.Badge setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(CONTENT__PROP);
		out.value(getContent());
		out.name(DOT__PROP);
		out.value(isDot());
		out.name(ROLE__PROP);
		getRole().writeTo(out);
		out.name(LABEL__PROP);
		out.value(getLabel());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CONTENT__PROP: setContent(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DOT__PROP: setDot(in.nextBoolean()); break;
			case ROLE__PROP: setRole(com.top_logic.layout.react.state.Badge.Role.readRole(in)); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
