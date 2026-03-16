package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.WindowOpenEvent}.
 */
public class WindowOpenEvent_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.WindowOpenEvent {

	private String _targetWindowId = "";

	private String _windowId = "";

	private int _width = 0;

	private int _height = 0;

	private String _title = "";

	private boolean _resizable = false;

	/**
	 * Creates a {@link WindowOpenEvent_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.WindowOpenEvent#create()
	 */
	public WindowOpenEvent_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.WINDOW_OPEN_EVENT;
	}

	@Override
	public final String getTargetWindowId() {
		return _targetWindowId;
	}

	@Override
	public com.top_logic.layout.react.protocol.WindowOpenEvent setTargetWindowId(String value) {
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
	public com.top_logic.layout.react.protocol.WindowOpenEvent setWindowId(String value) {
		internalSetWindowId(value);
		return this;
	}

	/** Internal setter for {@link #getWindowId()} without chain call utility. */
	protected final void internalSetWindowId(String value) {
		_windowId = value;
	}

	@Override
	public final int getWidth() {
		return _width;
	}

	@Override
	public com.top_logic.layout.react.protocol.WindowOpenEvent setWidth(int value) {
		internalSetWidth(value);
		return this;
	}

	/** Internal setter for {@link #getWidth()} without chain call utility. */
	protected final void internalSetWidth(int value) {
		_width = value;
	}

	@Override
	public final int getHeight() {
		return _height;
	}

	@Override
	public com.top_logic.layout.react.protocol.WindowOpenEvent setHeight(int value) {
		internalSetHeight(value);
		return this;
	}

	/** Internal setter for {@link #getHeight()} without chain call utility. */
	protected final void internalSetHeight(int value) {
		_height = value;
	}

	@Override
	public final String getTitle() {
		return _title;
	}

	@Override
	public com.top_logic.layout.react.protocol.WindowOpenEvent setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	/** Internal setter for {@link #getTitle()} without chain call utility. */
	protected final void internalSetTitle(String value) {
		_title = value;
	}

	@Override
	public final boolean isResizable() {
		return _resizable;
	}

	@Override
	public com.top_logic.layout.react.protocol.WindowOpenEvent setResizable(boolean value) {
		internalSetResizable(value);
		return this;
	}

	/** Internal setter for {@link #isResizable()} without chain call utility. */
	protected final void internalSetResizable(boolean value) {
		_resizable = value;
	}

	@Override
	public String jsonType() {
		return WINDOW_OPEN_EVENT__TYPE;
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TARGET_WINDOW_ID__PROP);
		out.value(getTargetWindowId());
		out.name(WINDOW_ID__PROP);
		out.value(getWindowId());
		out.name(WIDTH__PROP);
		out.value(getWidth());
		out.name(HEIGHT__PROP);
		out.value(getHeight());
		out.name(TITLE__PROP);
		out.value(getTitle());
		out.name(RESIZABLE__PROP);
		out.value(isResizable());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TARGET_WINDOW_ID__PROP: setTargetWindowId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case WINDOW_ID__PROP: setWindowId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case WIDTH__PROP: setWidth(in.nextInt()); break;
			case HEIGHT__PROP: setHeight(in.nextInt()); break;
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RESIZABLE__PROP: setResizable(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
