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
		_listener.beforeSet(this, TARGET_WINDOW_ID__PROP, value);
		_targetWindowId = value;
		_listener.afterChanged(this, TARGET_WINDOW_ID__PROP);
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
		_listener.beforeSet(this, WINDOW_ID__PROP, value);
		_windowId = value;
		_listener.afterChanged(this, WINDOW_ID__PROP);
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
		_listener.beforeSet(this, WIDTH__PROP, value);
		_width = value;
		_listener.afterChanged(this, WIDTH__PROP);
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
		_listener.beforeSet(this, HEIGHT__PROP, value);
		_height = value;
		_listener.afterChanged(this, HEIGHT__PROP);
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
		_listener.beforeSet(this, TITLE__PROP, value);
		_title = value;
		_listener.afterChanged(this, TITLE__PROP);
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
		_listener.beforeSet(this, RESIZABLE__PROP, value);
		_resizable = value;
		_listener.afterChanged(this, RESIZABLE__PROP);
	}

	@Override
	public String jsonType() {
		return WINDOW_OPEN_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TARGET_WINDOW_ID__PROP, 
			WINDOW_ID__PROP, 
			WIDTH__PROP, 
			HEIGHT__PROP, 
			TITLE__PROP, 
			RESIZABLE__PROP);
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
			case WIDTH__PROP: return getWidth();
			case HEIGHT__PROP: return getHeight();
			case TITLE__PROP: return getTitle();
			case RESIZABLE__PROP: return isResizable();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TARGET_WINDOW_ID__PROP: internalSetTargetWindowId((String) value); break;
			case WINDOW_ID__PROP: internalSetWindowId((String) value); break;
			case WIDTH__PROP: internalSetWidth((int) value); break;
			case HEIGHT__PROP: internalSetHeight((int) value); break;
			case TITLE__PROP: internalSetTitle((String) value); break;
			case RESIZABLE__PROP: internalSetResizable((boolean) value); break;
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
