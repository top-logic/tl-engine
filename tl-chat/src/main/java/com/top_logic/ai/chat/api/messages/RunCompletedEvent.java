package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for run completed.
 */
public class RunCompletedEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.RunCompletedEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.RunCompletedEvent create() {
		return new com.top_logic.ai.chat.api.messages.RunCompletedEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.RunCompletedEvent} type in JSON format. */
	public static final String RUN_COMPLETED_EVENT__TYPE = "RunCompletedEvent";

	/** @see #getRunId() */
	public static final String RUN_ID__PROP = "runId";

	/** @see #getReason() */
	public static final String REASON__PROP = "reason";

	/** @see #getTotalUsage() */
	public static final String TOTAL_USAGE__PROP = "totalUsage";

	private String _runId = "";

	private String _reason = "";

	private com.top_logic.ai.chat.api.messages.TokenUsageDto _totalUsage = null;

	/**
	 * Creates a {@link RunCompletedEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.RunCompletedEvent#create()
	 */
	protected RunCompletedEvent() {
		super();
	}

	/**
	 * The run ID.
	 */
	public final String getRunId() {
		return _runId;
	}

	/**
	 * @see #getRunId()
	 */
	public com.top_logic.ai.chat.api.messages.RunCompletedEvent setRunId(String value) {
		internalSetRunId(value);
		return this;
	}

	/** Internal setter for {@link #getRunId()} without chain call utility. */
	protected final void internalSetRunId(String value) {
		_runId = value;
	}

	/**
	 * Stop reason: COMPLETE, MAX_TURNS, ERROR, ABORTED.
	 */
	public final String getReason() {
		return _reason;
	}

	/**
	 * @see #getReason()
	 */
	public com.top_logic.ai.chat.api.messages.RunCompletedEvent setReason(String value) {
		internalSetReason(value);
		return this;
	}

	/** Internal setter for {@link #getReason()} without chain call utility. */
	protected final void internalSetReason(String value) {
		_reason = value;
	}

	/**
	 * Total token usage for the run.
	 */
	public final com.top_logic.ai.chat.api.messages.TokenUsageDto getTotalUsage() {
		return _totalUsage;
	}

	/**
	 * @see #getTotalUsage()
	 */
	public com.top_logic.ai.chat.api.messages.RunCompletedEvent setTotalUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto value) {
		internalSetTotalUsage(value);
		return this;
	}

	/** Internal setter for {@link #getTotalUsage()} without chain call utility. */
	protected final void internalSetTotalUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto value) {
		_totalUsage = value;
	}

	/**
	 * Checks, whether {@link #getTotalUsage()} has a value.
	 */
	public final boolean hasTotalUsage() {
		return _totalUsage != null;
	}

	@Override
	public String jsonType() {
		return RUN_COMPLETED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			RUN_ID__PROP, 
			REASON__PROP, 
			TOTAL_USAGE__PROP);
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
			case RUN_ID__PROP: return getRunId();
			case REASON__PROP: return getReason();
			case TOTAL_USAGE__PROP: return getTotalUsage();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case RUN_ID__PROP: internalSetRunId((String) value); break;
			case REASON__PROP: internalSetReason((String) value); break;
			case TOTAL_USAGE__PROP: internalSetTotalUsage((com.top_logic.ai.chat.api.messages.TokenUsageDto) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.RunCompletedEvent readRunCompletedEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.RunCompletedEvent result = new com.top_logic.ai.chat.api.messages.RunCompletedEvent();
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
		out.name(RUN_ID__PROP);
		out.value(getRunId());
		out.name(REASON__PROP);
		out.value(getReason());
		if (hasTotalUsage()) {
			out.name(TOTAL_USAGE__PROP);
			getTotalUsage().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case RUN_ID__PROP: setRunId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REASON__PROP: setReason(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOTAL_USAGE__PROP: setTotalUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto.readTokenUsageDto(in)); break;
			default: super.readField(in, field);
		}
	}

}
