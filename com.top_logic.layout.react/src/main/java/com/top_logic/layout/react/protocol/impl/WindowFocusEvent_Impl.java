package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.WindowFocusEvent}.
 */
public class WindowFocusEvent_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.WindowFocusEvent {

	private String _targetWindowId = "";

	private String _windowId = "";

	/**
	 * Creates a {@link WindowFocusEvent_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.WindowFocusEvent#create()
	 */
	public WindowFocusEvent_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.WINDOW_FOCUS_EVENT;
	}

	@Override
	public final String getTargetWindowId() {
		return _targetWindowId;
	}

	@Override
	public com.top_logic.layout.react.protocol.WindowFocusEvent setTargetWindowId(String value) {
		internalSetTargetWindowId(value);
		return this;
	}

	/** Internal setter for {@link #getTargetWindowId()} without chain call utility. */
	protected final void internalSetTargetWindowId(String value) {
		_targetWindowId = value;
	}

	@Override
	public final String getWindowId() {
		return _windowId;
	}

	@Override
	public com.top_logic.layout.react.protocol.WindowFocusEvent setWindowId(String value) {
		internalSetWindowId(value);
		return this;
	}

	/** Internal setter for {@link #getWindowId()} without chain call utility. */
	protected final void internalSetWindowId(String value) {
		_windowId = value;
	}

	@Override
	public String jsonType() {
		return WINDOW_FOCUS_EVENT__TYPE;
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
