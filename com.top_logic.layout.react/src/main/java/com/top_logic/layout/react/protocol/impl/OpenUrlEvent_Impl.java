package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.OpenUrlEvent}.
 */
public class OpenUrlEvent_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.OpenUrlEvent {

	private String _url = "";

	private boolean _newWindow = true;

	/**
	 * Creates a {@link OpenUrlEvent_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.OpenUrlEvent#create()
	 */
	public OpenUrlEvent_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.OPEN_URL_EVENT;
	}

	@Override
	public final String getUrl() {
		return _url;
	}

	@Override
	public com.top_logic.layout.react.protocol.OpenUrlEvent setUrl(String value) {
		internalSetUrl(value);
		return this;
	}

	/** Internal setter for {@link #getUrl()} without chain call utility. */
	protected final void internalSetUrl(String value) {
		_url = value;
	}

	@Override
	public final boolean isNewWindow() {
		return _newWindow;
	}

	@Override
	public com.top_logic.layout.react.protocol.OpenUrlEvent setNewWindow(boolean value) {
		internalSetNewWindow(value);
		return this;
	}

	/** Internal setter for {@link #isNewWindow()} without chain call utility. */
	protected final void internalSetNewWindow(boolean value) {
		_newWindow = value;
	}

	@Override
	public String jsonType() {
		return OPEN_URL_EVENT__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(URL__PROP);
		out.value(getUrl());
		out.name(NEW_WINDOW__PROP);
		out.value(isNewWindow());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case URL__PROP: setUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case NEW_WINDOW__PROP: setNewWindow(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
