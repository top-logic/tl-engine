package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.WindowCloseEvent}.
 */
public class WindowCloseEvent_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.WindowCloseEvent {

	private String _targetWindowId = "";

	private String _windowId = "";

	/**
	 * Creates a {@link WindowCloseEvent_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.WindowCloseEvent#create()
	 */
	public WindowCloseEvent_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.WINDOW_CLOSE_EVENT;
	}

	@Override
	public final String getTargetWindowId() {
		return _targetWindowId;
	}

	@Override
	public com.top_logic.layout.react.protocol.WindowCloseEvent setTargetWindowId(String value) {
		internalSetTargetWindowId(value);
		return this;
	}

	/** Internal setter for {@link #getTargetWindowId()} without chain call utility. */
	protected final void internalSetTargetWindowId(String value) {
		_listener.beforeSet(this, TARGET_WINDOW_ID__PROP, value);
		_targetWindowId = value;
		_listener.afterChanged(this, TARGET_WINDOW_ID__PROP);
	}

	@Override
	public final String getWindowId() {
		return _windowId;
	}

	@Override
	public com.top_logic.layout.react.protocol.WindowCloseEvent setWindowId(String value) {
		internalSetWindowId(value);
		return this;
	}

	/** Internal setter for {@link #getWindowId()} without chain call utility. */
	protected final void internalSetWindowId(String value) {
		_listener.beforeSet(this, WINDOW_ID__PROP, value);
		_windowId = value;
		_listener.afterChanged(this, WINDOW_ID__PROP);
	}

	@Override
	public String jsonType() {
		return WINDOW_CLOSE_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TARGET_WINDOW_ID__PROP, 
			WINDOW_ID__PROP);
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
			case TARGET_WINDOW_ID__PROP: return getTargetWindowId();
			case WINDOW_ID__PROP: return getWindowId();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TARGET_WINDOW_ID__PROP: internalSetTargetWindowId((String) value); break;
			case WINDOW_ID__PROP: internalSetWindowId((String) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TARGET_WINDOW_ID__PROP);
		out.value(getTargetWindowId());
		out.name(WINDOW_ID__PROP);
		out.value(getWindowId());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TARGET_WINDOW_ID__PROP: setTargetWindowId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case WINDOW_ID__PROP: setWindowId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
