package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.ViewPickEvent}.
 */
public class ViewPickEvent_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.ViewPickEvent {

	private String _token = "";

	private String _targetWindowId = "";

	/**
	 * Creates a {@link ViewPickEvent_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.ViewPickEvent#create()
	 */
	public ViewPickEvent_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.VIEW_PICK_EVENT;
	}

	@Override
	public final String getToken() {
		return _token;
	}

	@Override
	public com.top_logic.layout.react.protocol.ViewPickEvent setToken(String value) {
		internalSetToken(value);
		return this;
	}

	/** Internal setter for {@link #getToken()} without chain call utility. */
	protected final void internalSetToken(String value) {
		_token = value;
	}

	@Override
	public final String getTargetWindowId() {
		return _targetWindowId;
	}

	@Override
	public com.top_logic.layout.react.protocol.ViewPickEvent setTargetWindowId(String value) {
		internalSetTargetWindowId(value);
		return this;
	}

	/** Internal setter for {@link #getTargetWindowId()} without chain call utility. */
	protected final void internalSetTargetWindowId(String value) {
		_targetWindowId = value;
	}

	@Override
	public String jsonType() {
		return VIEW_PICK_EVENT__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TOKEN__PROP);
		out.value(getToken());
		out.name(TARGET_WINDOW_ID__PROP);
		out.value(getTargetWindowId());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TOKEN__PROP: setToken(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TARGET_WINDOW_ID__PROP: setTargetWindowId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
