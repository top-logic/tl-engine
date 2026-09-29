package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.StateEvent}.
 */
public class StateEvent_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.StateEvent {

	private String _controlId = "";

	private String _state = "";

	/**
	 * Creates a {@link StateEvent_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.StateEvent#create()
	 */
	public StateEvent_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.STATE_EVENT;
	}

	@Override
	public final String getControlId() {
		return _controlId;
	}

	@Override
	public com.top_logic.layout.react.protocol.StateEvent setControlId(String value) {
		internalSetControlId(value);
		return this;
	}

	/** Internal setter for {@link #getControlId()} without chain call utility. */
	protected final void internalSetControlId(String value) {
		_controlId = value;
	}

	@Override
	public final String getState() {
		return _state;
	}

	@Override
	public com.top_logic.layout.react.protocol.StateEvent setState(String value) {
		internalSetState(value);
		return this;
	}

	/** Internal setter for {@link #getState()} without chain call utility. */
	protected final void internalSetState(String value) {
		_state = value;
	}

	@Override
	public String jsonType() {
		return STATE_EVENT__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(CONTROL_ID__PROP);
		out.value(getControlId());
		out.name(STATE__PROP);
		out.value(getState());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CONTROL_ID__PROP: setControlId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STATE__PROP: setState(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
