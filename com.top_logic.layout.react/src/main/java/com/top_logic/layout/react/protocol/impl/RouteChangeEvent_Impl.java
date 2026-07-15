package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.RouteChangeEvent}.
 */
public class RouteChangeEvent_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.RouteChangeEvent {

	private String _url = "";

	private boolean _replace = false;

	/**
	 * Creates a {@link RouteChangeEvent_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.RouteChangeEvent#create()
	 */
	public RouteChangeEvent_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.ROUTE_CHANGE_EVENT;
	}

	@Override
	public final String getUrl() {
		return _url;
	}

	@Override
	public com.top_logic.layout.react.protocol.RouteChangeEvent setUrl(String value) {
		internalSetUrl(value);
		return this;
	}

	/** Internal setter for {@link #getUrl()} without chain call utility. */
	protected final void internalSetUrl(String value) {
		_url = value;
	}

	@Override
	public final boolean isReplace() {
		return _replace;
	}

	@Override
	public com.top_logic.layout.react.protocol.RouteChangeEvent setReplace(boolean value) {
		internalSetReplace(value);
		return this;
	}

	/** Internal setter for {@link #isReplace()} without chain call utility. */
	protected final void internalSetReplace(boolean value) {
		_replace = value;
	}

	@Override
	public String jsonType() {
		return ROUTE_CHANGE_EVENT__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(URL__PROP);
		out.value(getUrl());
		out.name(REPLACE__PROP);
		out.value(isReplace());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case URL__PROP: setUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REPLACE__PROP: setReplace(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
