package com.top_logic.layout.react.protocol.impl;

/**
 * Implementation of {@link com.top_logic.layout.react.protocol.RangeReplacement}.
 */
public class RangeReplacement_Impl extends com.top_logic.layout.react.protocol.impl.SSEEvent_Impl implements com.top_logic.layout.react.protocol.RangeReplacement {

	private String _startId = "";

	private String _stopId = "";

	private String _html = "";

	/**
	 * Creates a {@link RangeReplacement_Impl} instance.
	 *
	 * @see com.top_logic.layout.react.protocol.RangeReplacement#create()
	 */
	public RangeReplacement_Impl() {
		super();
	}

	@Override
	public TypeKind kind() {
		return TypeKind.RANGE_REPLACEMENT;
	}

	@Override
	public final String getStartId() {
		return _startId;
	}

	@Override
	public com.top_logic.layout.react.protocol.RangeReplacement setStartId(String value) {
		internalSetStartId(value);
		return this;
	}

	/** Internal setter for {@link #getStartId()} without chain call utility. */
	protected final void internalSetStartId(String value) {
		_listener.beforeSet(this, START_ID__PROP, value);
		_startId = value;
		_listener.afterChanged(this, START_ID__PROP);
	}

	@Override
	public final String getStopId() {
		return _stopId;
	}

	@Override
	public com.top_logic.layout.react.protocol.RangeReplacement setStopId(String value) {
		internalSetStopId(value);
		return this;
	}

	/** Internal setter for {@link #getStopId()} without chain call utility. */
	protected final void internalSetStopId(String value) {
		_listener.beforeSet(this, STOP_ID__PROP, value);
		_stopId = value;
		_listener.afterChanged(this, STOP_ID__PROP);
	}

	@Override
	public final String getHtml() {
		return _html;
	}

	@Override
	public com.top_logic.layout.react.protocol.RangeReplacement setHtml(String value) {
		internalSetHtml(value);
		return this;
	}

	/** Internal setter for {@link #getHtml()} without chain call utility. */
	protected final void internalSetHtml(String value) {
		_listener.beforeSet(this, HTML__PROP, value);
		_html = value;
		_listener.afterChanged(this, HTML__PROP);
	}

	@Override
	public String jsonType() {
		return RANGE_REPLACEMENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			START_ID__PROP, 
			STOP_ID__PROP, 
			HTML__PROP);
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
			case START_ID__PROP: return getStartId();
			case STOP_ID__PROP: return getStopId();
			case HTML__PROP: return getHtml();
			default: return super.get(field);
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case START_ID__PROP: internalSetStartId((String) value); break;
			case STOP_ID__PROP: internalSetStopId((String) value); break;
			case HTML__PROP: internalSetHtml((String) value); break;
			default: super.set(field, value); break;
		}
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(START_ID__PROP);
		out.value(getStartId());
		out.name(STOP_ID__PROP);
		out.value(getStopId());
		out.name(HTML__PROP);
		out.value(getHtml());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case START_ID__PROP: setStartId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STOP_ID__PROP: setStopId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HTML__PROP: setHtml(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

	@Override
	public <R,A,E extends Throwable> R visit(com.top_logic.layout.react.protocol.SSEEvent.Visitor<R,A,E> v, A arg) throws E {
		return v.visit(this, arg);
	}

}
