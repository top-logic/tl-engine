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
		_listener.beforeSet(this, CONTROL_ID__PROP, value);
		_controlId = value;
		_listener.afterChanged(this, CONTROL_ID__PROP);
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
		_listener.beforeSet(this, STATE__PROP, value);
		_state = value;
		_listener.afterChanged(this, STATE__PROP);
	}

	@Override
	public String jsonType() {
		return STATE_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			CONTROL_ID__PROP, 
			STATE__PROP);
		PROPERTIES = java.util.Collections.unmodifiableList(local);
	}

	static final java.util.Set<String> TRANSIENT_PROPERTIES;
	static {
		java.util.HashSet<String> tmp = new java.util.HashSet<>();
		tmp.addAll(java.util.Arrays.asList(
				));
		TRANSIENT_PROPERTIES = java.util.Collections.unmodifiableSet(tmp);
	}

	@Override
	public java.util.List<String> properties() {
		return PROPERTIES;
	}

	@Override
	public java.util.Set<String> transientProperties() {
		return TRANSIENT_PROPERTIES;
	}

	@Override
	public Object get(String field) {
		switch (field) {
			case CONTROL_ID__PROP: return getControlId();
			case STATE__PROP: return getState();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case CONTROL_ID__PROP: internalSetControlId((String) value); break;
			case STATE__PROP: internalSetState((String) value); break;
			default: super.set(field, value); break;
		}
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
