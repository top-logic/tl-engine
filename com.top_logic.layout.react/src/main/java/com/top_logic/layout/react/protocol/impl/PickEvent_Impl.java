package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.PickEvent}.
 */
public class PickEvent_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.PickEvent {

	private String _token = "";

	private String _targetWindowId = "";

	private String _kind = "";

	/**
	 * Creates a {@link PickEvent_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.PickEvent#create()
	 */
	public PickEvent_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.PICK_EVENT;
	}

	@Override
	public final String getToken() {
		return _token;
	}

	@Override
	public com.top_logic.layout.react.protocol.PickEvent setToken(String value) {
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
	public com.top_logic.layout.react.protocol.PickEvent setTargetWindowId(String value) {
		internalSetTargetWindowId(value);
		return this;
	}

	/** Internal setter for {@link #getTargetWindowId()} without chain call utility. */
	protected final void internalSetTargetWindowId(String value) {
		_targetWindowId = value;
	}

	@Override
	public final String getKind() {
		return _kind;
	}

	@Override
	public com.top_logic.layout.react.protocol.PickEvent setKind(String value) {
		internalSetKind(value);
		return this;
	}

	/** Internal setter for {@link #getKind()} without chain call utility. */
	protected final void internalSetKind(String value) {
		_kind = value;
	}

	@Override
	public String jsonType() {
		return PICK_EVENT__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TOKEN__PROP);
		out.value(getToken());
		out.name(TARGET_WINDOW_ID__PROP);
		out.value(getTargetWindowId());
		out.name(KIND__PROP);
		out.value(getKind());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TOKEN__PROP: setToken(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TARGET_WINDOW_ID__PROP: setTargetWindowId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case KIND__PROP: setKind(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
