package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.RouteVetoEvent}.
 */
public class RouteVetoEvent_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.RouteVetoEvent {

	private String _currentUrl = "";

	/**
	 * Creates a {@link RouteVetoEvent_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.RouteVetoEvent#create()
	 */
	public RouteVetoEvent_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.ROUTE_VETO_EVENT;
	}

	@Override
	public final String getCurrentUrl() {
		return _currentUrl;
	}

	@Override
	public com.top_logic.layout.react.protocol.RouteVetoEvent setCurrentUrl(String value) {
		internalSetCurrentUrl(value);
		return this;
	}

	/** Internal setter for {@link #getCurrentUrl()} without chain call utility. */
	protected final void internalSetCurrentUrl(String value) {
		_currentUrl = value;
	}

	@Override
	public String jsonType() {
		return ROUTE_VETO_EVENT__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(CURRENT_URL__PROP);
		out.value(getCurrentUrl());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CURRENT_URL__PROP: setCurrentUrl(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
