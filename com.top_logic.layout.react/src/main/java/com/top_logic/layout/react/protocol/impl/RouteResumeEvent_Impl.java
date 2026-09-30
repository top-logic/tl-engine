package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.RouteResumeEvent}.
 */
public class RouteResumeEvent_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.RouteResumeEvent {

	private String _url = "";

	/**
	 * Creates a {@link RouteResumeEvent_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.RouteResumeEvent#create()
	 */
	public RouteResumeEvent_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.ROUTE_RESUME_EVENT;
	}

	@Override
	public final String getUrl() {
		return _url;
	}

	@Override
	public com.top_logic.layout.react.protocol.RouteResumeEvent setUrl(String value) {
		internalSetUrl(value);
		return this;
	}

	/** Internal setter for {@link #getUrl()} without chain call utility. */
	protected final void internalSetUrl(String value) {
		_url = value;
	}

	@Override
	public String jsonType() {
		return ROUTE_RESUME_EVENT__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(URL__PROP);
		out.value(getUrl());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case URL__PROP: setUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
