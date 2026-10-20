package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for tool call started.
 */
public class ToolStartedEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ToolStartedEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ToolStartedEvent create() {
		return new com.top_logic.ai.chat.api.messages.ToolStartedEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ToolStartedEvent} type in JSON format. */
	public static final String TOOL_STARTED_EVENT__TYPE = "ToolStartedEvent";

	/** @see #getToolCallId() */
	public static final String TOOL_CALL_ID__PROP = "toolCallId";

	/** @see #getToolName() */
	public static final String TOOL_NAME__PROP = "toolName";

	/** @see #getArgsJson() */
	public static final String ARGS_JSON__PROP = "argsJson";

	private String _toolCallId = "";

	private String _toolName = "";

	private String _argsJson = "";

	/**
	 * Creates a {@link ToolStartedEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ToolStartedEvent#create()
	 */
	protected ToolStartedEvent() {
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
	public com.top_logic.ai.chat.api.messages.ToolStartedEvent setToolCallId(String value) {
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
	public com.top_logic.ai.chat.api.messages.ToolStartedEvent setToolName(String value) {
		internalSetToolName(value);
		return this;
	}

	/** Internal setter for {@link #getToolName()} without chain call utility. */
	protected final void internalSetToolName(String value) {
		_toolName = value;
	}

	/**
	 * Arguments as JSON string.
	 */
	public final String getArgsJson() {
		return _argsJson;
	}

	/**
	 * @see #getArgsJson()
	 */
	public com.top_logic.ai.chat.api.messages.ToolStartedEvent setArgsJson(String value) {
		internalSetArgsJson(value);
		return this;
	}

	/** Internal setter for {@link #getArgsJson()} without chain call utility. */
	protected final void internalSetArgsJson(String value) {
		_argsJson = value;
	}

	@Override
	public String jsonType() {
		return TOOL_STARTED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TOOL_CALL_ID__PROP, 
			TOOL_NAME__PROP, 
			ARGS_JSON__PROP);
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
			case ARGS_JSON__PROP: return getArgsJson();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TOOL_CALL_ID__PROP: internalSetToolCallId((String) value); break;
			case TOOL_NAME__PROP: internalSetToolName((String) value); break;
			case ARGS_JSON__PROP: internalSetArgsJson((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ToolStartedEvent readToolStartedEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ToolStartedEvent result = new com.top_logic.ai.chat.api.messages.ToolStartedEvent();
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
		out.name(ARGS_JSON__PROP);
		out.value(getArgsJson());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TOOL_CALL_ID__PROP: setToolCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOOL_NAME__PROP: setToolName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ARGS_JSON__PROP: setArgsJson(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
