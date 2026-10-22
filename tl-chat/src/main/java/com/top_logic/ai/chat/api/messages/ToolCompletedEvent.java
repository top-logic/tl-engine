package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for tool call completed.
 */
public class ToolCompletedEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ToolCompletedEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ToolCompletedEvent create() {
		return new com.top_logic.ai.chat.api.messages.ToolCompletedEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ToolCompletedEvent} type in JSON format. */
	public static final String TOOL_COMPLETED_EVENT__TYPE = "ToolCompletedEvent";

	/** @see #getToolCallId() */
	public static final String TOOL_CALL_ID__PROP = "toolCallId";

	/** @see #getToolName() */
	public static final String TOOL_NAME__PROP = "toolName";

	/** @see #getResult() */
	public static final String RESULT__PROP = "result";

	/** @see #getDurationMs() */
	public static final String DURATION_MS__PROP = "durationMs";

	private String _toolCallId = "";

	private String _toolName = "";

	private String _result = "";

	private long _durationMs = 0L;

	/**
	 * Creates a {@link ToolCompletedEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ToolCompletedEvent#create()
	 */
	protected ToolCompletedEvent() {
		super();
	}

	/**
	 * Unique tool call ID.
	 */
	public final String getToolCallId() {
		return _toolCallId;
	}

	/**
	 * @see #getToolCallId()
	 */
	public com.top_logic.ai.chat.api.messages.ToolCompletedEvent setToolCallId(String value) {
		internalSetToolCallId(value);
		return this;
	}

	/** Internal setter for {@link #getToolCallId()} without chain call utility. */
	protected final void internalSetToolCallId(String value) {
		_toolCallId = value;
	}

	/**
	 * Tool name.
	 */
	public final String getToolName() {
		return _toolName;
	}

	/**
	 * @see #getToolName()
	 */
	public com.top_logic.ai.chat.api.messages.ToolCompletedEvent setToolName(String value) {
		internalSetToolName(value);
		return this;
	}

	/** Internal setter for {@link #getToolName()} without chain call utility. */
	protected final void internalSetToolName(String value) {
		_toolName = value;
	}

	/**
	 * Result text.
	 */
	public final String getResult() {
		return _result;
	}

	/**
	 * @see #getResult()
	 */
	public com.top_logic.ai.chat.api.messages.ToolCompletedEvent setResult(String value) {
		internalSetResult(value);
		return this;
	}

	/** Internal setter for {@link #getResult()} without chain call utility. */
	protected final void internalSetResult(String value) {
		_result = value;
	}

	/**
	 * Duration in milliseconds.
	 */
	public final long getDurationMs() {
		return _durationMs;
	}

	/**
	 * @see #getDurationMs()
	 */
	public com.top_logic.ai.chat.api.messages.ToolCompletedEvent setDurationMs(long value) {
		internalSetDurationMs(value);
		return this;
	}

	/** Internal setter for {@link #getDurationMs()} without chain call utility. */
	protected final void internalSetDurationMs(long value) {
		_durationMs = value;
	}

	@Override
	public String jsonType() {
		return TOOL_COMPLETED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TOOL_CALL_ID__PROP, 
			TOOL_NAME__PROP, 
			RESULT__PROP, 
			DURATION_MS__PROP);
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
			case TOOL_CALL_ID__PROP: return getToolCallId();
			case TOOL_NAME__PROP: return getToolName();
			case RESULT__PROP: return getResult();
			case DURATION_MS__PROP: return getDurationMs();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TOOL_CALL_ID__PROP: internalSetToolCallId((String) value); break;
			case TOOL_NAME__PROP: internalSetToolName((String) value); break;
			case RESULT__PROP: internalSetResult((String) value); break;
			case DURATION_MS__PROP: internalSetDurationMs((long) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ToolCompletedEvent readToolCompletedEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ToolCompletedEvent result = new com.top_logic.ai.chat.api.messages.ToolCompletedEvent();
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
		out.name(TOOL_CALL_ID__PROP);
		out.value(getToolCallId());
		out.name(TOOL_NAME__PROP);
		out.value(getToolName());
		out.name(RESULT__PROP);
		out.value(getResult());
		out.name(DURATION_MS__PROP);
		out.value(getDurationMs());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TOOL_CALL_ID__PROP: setToolCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOOL_NAME__PROP: setToolName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RESULT__PROP: setResult(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DURATION_MS__PROP: setDurationMs(in.nextLong()); break;
			default: super.readField(in, field);
		}
	}

}
