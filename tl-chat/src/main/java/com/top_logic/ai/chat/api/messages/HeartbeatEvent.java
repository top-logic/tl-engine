package com.top_logic.ai.chat.api.messages;

/**
 * SSE heartbeat event payload.
 */
public class HeartbeatEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.HeartbeatEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.HeartbeatEvent create() {
		return new com.top_logic.ai.chat.api.messages.HeartbeatEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.HeartbeatEvent} type in JSON format. */
	public static final String HEARTBEAT_EVENT__TYPE = "HeartbeatEvent";

	/** @see #getTs() */
	public static final String TS__PROP = "ts";

	private long _ts = 0L;

	/**
	 * Creates a {@link HeartbeatEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.HeartbeatEvent#create()
	 */
	protected HeartbeatEvent() {
		super();
	}

	/**
	 * Current server timestamp in milliseconds.
	 */
	public final long getTs() {
		return _ts;
	}

	/**
	 * @see #getTs()
	 */
	public com.top_logic.ai.chat.api.messages.HeartbeatEvent setTs(long value) {
		internalSetTs(value);
		return this;
	}

	/** Internal setter for {@link #getTs()} without chain call utility. */
	protected final void internalSetTs(long value) {
		_ts = value;
	}

	@Override
	public String jsonType() {
		return HEARTBEAT_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TS__PROP);
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
			case TS__PROP: return getTs();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TS__PROP: internalSetTs((long) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.HeartbeatEvent readHeartbeatEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.HeartbeatEvent result = new com.top_logic.ai.chat.api.messages.HeartbeatEvent();
		result.readContent(in);
		return result;
	}

	@Override
	public final void writeTo(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		writeContent(out);
	}

	@Override
	protected void writeFields(de.haumacher.msgbuf.json.JsonWriter out) throws java.io.IOException {
		super.writeFields(out);
		out.name(TS__PROP);
		out.value(getTs());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TS__PROP: setTs(in.nextLong()); break;
			default: super.readField(in, field);
		}
	}

}
