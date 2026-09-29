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
		_startId = value;
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
		_stopId = value;
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
		_html = value;
	}

	@Override
	public String jsonType() {
		return RANGE_REPLACEMENT__TYPE;
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
