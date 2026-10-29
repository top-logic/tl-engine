package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for a model call that is asked again after a pause, because the model was rate limited
 * or overloaded.
 */
public class RetryScheduledEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.RetryScheduledEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.RetryScheduledEvent create() {
		return new com.top_logic.ai.chat.api.messages.RetryScheduledEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.RetryScheduledEvent} type in JSON format. */
	public static final String RETRY_SCHEDULED_EVENT__TYPE = "RetryScheduledEvent";

	/** @see #getKind() */
	public static final String KIND__PROP = "kind";

	/** @see #getAttempt() */
	public static final String ATTEMPT__PROP = "attempt";

	/** @see #getWaitMs() */
	public static final String WAIT_MS__PROP = "waitMs";

	private String _kind = "";

	private int _attempt = 0;

	private long _waitMs = 0L;

	/**
	 * Creates a {@link RetryScheduledEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.RetryScheduledEvent#create()
	 */
	protected RetryScheduledEvent() {
		super();
	}

	/**
	 * Why the call is repeated: RATE_LIMITED or OVERLOADED.
	 */
	public final String getKind() {
		return _kind;
	}

	/**
	 * @see #getKind()
	 */
	public com.top_logic.ai.chat.api.messages.RetryScheduledEvent setKind(String value) {
		internalSetKind(value);
		return this;
	}

	/** Internal setter for {@link #getKind()} without chain call utility. */
	protected final void internalSetKind(String value) {
		_kind = value;
	}

	/**
	 * Which retry this is, 1 for the first.
	 */
	public final int getAttempt() {
		return _attempt;
	}

	/**
	 * @see #getAttempt()
	 */
	public com.top_logic.ai.chat.api.messages.RetryScheduledEvent setAttempt(int value) {
		internalSetAttempt(value);
		return this;
	}

	/** Internal setter for {@link #getAttempt()} without chain call utility. */
	protected final void internalSetAttempt(int value) {
		_attempt = value;
	}

	/**
	 * How long the run waits before it asks again, in milliseconds.
	 */
	public final long getWaitMs() {
		return _waitMs;
	}

	/**
	 * @see #getWaitMs()
	 */
	public com.top_logic.ai.chat.api.messages.RetryScheduledEvent setWaitMs(long value) {
		internalSetWaitMs(value);
		return this;
	}

	/** Internal setter for {@link #getWaitMs()} without chain call utility. */
	protected final void internalSetWaitMs(long value) {
		_waitMs = value;
	}

	@Override
	public String jsonType() {
		return RETRY_SCHEDULED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			KIND__PROP, 
			ATTEMPT__PROP, 
			WAIT_MS__PROP);
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
			case KIND__PROP: return getKind();
			case ATTEMPT__PROP: return getAttempt();
			case WAIT_MS__PROP: return getWaitMs();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case KIND__PROP: internalSetKind((String) value); break;
			case ATTEMPT__PROP: internalSetAttempt((int) value); break;
			case WAIT_MS__PROP: internalSetWaitMs((long) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.RetryScheduledEvent readRetryScheduledEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.RetryScheduledEvent result = new com.top_logic.ai.chat.api.messages.RetryScheduledEvent();
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
		out.name(KIND__PROP);
		out.value(getKind());
		out.name(ATTEMPT__PROP);
		out.value(getAttempt());
		out.name(WAIT_MS__PROP);
		out.value(getWaitMs());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case KIND__PROP: setKind(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ATTEMPT__PROP: setAttempt(in.nextInt()); break;
			case WAIT_MS__PROP: setWaitMs(in.nextLong()); break;
			default: super.readField(in, field);
		}
	}

}
