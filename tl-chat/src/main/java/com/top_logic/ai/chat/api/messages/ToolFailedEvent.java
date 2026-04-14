package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for tool call failed.
 */
public class ToolFailedEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ToolFailedEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ToolFailedEvent create() {
		return new com.top_logic.ai.chat.api.messages.ToolFailedEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ToolFailedEvent} type in JSON format. */
	public static final String TOOL_FAILED_EVENT__TYPE = "ToolFailedEvent";

	/** @see #getToolCallId() */
	public static final String TOOL_CALL_ID__PROP = "toolCallId";

	/** @see #getToolName() */
	public static final String TOOL_NAME__PROP = "toolName";

	/** @see #getErrorMessage() */
	public static final String ERROR_MESSAGE__PROP = "errorMessage";

	/** @see #getDurationMs() */
	public static final String DURATION_MS__PROP = "durationMs";

	private String _toolCallId = "";

	private String _toolName = "";

	private String _errorMessage = "";

	private long _durationMs = 0L;

	/**
	 * Creates a {@link ToolFailedEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ToolFailedEvent#create()
	 */
	protected ToolFailedEvent() {
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
	public com.top_logic.ai.chat.api.messages.ToolFailedEvent setToolCallId(String value) {
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
	public com.top_logic.ai.chat.api.messages.ToolFailedEvent setToolName(String value) {
		internalSetToolName(value);
		return this;
	}

	/** Internal setter for {@link #getToolName()} without chain call utility. */
	protected final void internalSetToolName(String value) {
		_toolName = value;
	}

	/**
	 * Error message.
	 */
	public final String getErrorMessage() {
		return _errorMessage;
	}

	/**
	 * @see #getErrorMessage()
	 */
	public com.top_logic.ai.chat.api.messages.ToolFailedEvent setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	/** Internal setter for {@link #getErrorMessage()} without chain call utility. */
	protected final void internalSetErrorMessage(String value) {
		_errorMessage = value;
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
	public com.top_logic.ai.chat.api.messages.ToolFailedEvent setDurationMs(long value) {
		internalSetDurationMs(value);
		return this;
	}

	/** Internal setter for {@link #getDurationMs()} without chain call utility. */
	protected final void internalSetDurationMs(long value) {
		_durationMs = value;
	}

	@Override
	public String jsonType() {
		return TOOL_FAILED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TOOL_CALL_ID__PROP, 
			TOOL_NAME__PROP, 
			ERROR_MESSAGE__PROP, 
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
			case ERROR_MESSAGE__PROP: return getErrorMessage();
			case DURATION_MS__PROP: return getDurationMs();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TOOL_CALL_ID__PROP: internalSetToolCallId((String) value); break;
			case TOOL_NAME__PROP: internalSetToolName((String) value); break;
			case ERROR_MESSAGE__PROP: internalSetErrorMessage((String) value); break;
			case DURATION_MS__PROP: internalSetDurationMs((long) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ToolFailedEvent readToolFailedEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ToolFailedEvent result = new com.top_logic.ai.chat.api.messages.ToolFailedEvent();
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
		out.name(ERROR_MESSAGE__PROP);
		out.value(getErrorMessage());
		out.name(DURATION_MS__PROP);
		out.value(getDurationMs());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TOOL_CALL_ID__PROP: setToolCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOOL_NAME__PROP: setToolName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ERROR_MESSAGE__PROP: setErrorMessage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DURATION_MS__PROP: setDurationMs(in.nextLong()); break;
			default: super.readField(in, field);
		}
	}

}
