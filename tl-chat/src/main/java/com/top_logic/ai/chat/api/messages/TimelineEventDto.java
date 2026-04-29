package com.top_logic.ai.chat.api.messages;

/**
 * One timeline event (tool call or agent call).
 */
public class TimelineEventDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.TimelineEventDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.TimelineEventDto create() {
		return new com.top_logic.ai.chat.api.messages.TimelineEventDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.TimelineEventDto} type in JSON format. */
	public static final String TIMELINE_EVENT_DTO__TYPE = "TimelineEventDto";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getToolName() */
	public static final String TOOL_NAME__PROP = "toolName";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getArgsJson() */
	public static final String ARGS_JSON__PROP = "argsJson";

	/** @see #getResult() */
	public static final String RESULT__PROP = "result";

	/** @see #getFromAgent() */
	public static final String FROM_AGENT__PROP = "fromAgent";

	/** @see #getToAgent() */
	public static final String TO_AGENT__PROP = "toAgent";

	/** @see #getInputSummary() */
	public static final String INPUT_SUMMARY__PROP = "inputSummary";

	/** @see #getResultSummary() */
	public static final String RESULT_SUMMARY__PROP = "resultSummary";

	/** @see #getDurationMs() */
	public static final String DURATION_MS__PROP = "durationMs";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	private String _id = "";

	private String _type = "";

	private String _toolName = "";

	private String _name = "";

	private String _argsJson = "";

	private String _result = "";

	private String _fromAgent = "";

	private String _toAgent = "";

	private String _inputSummary = "";

	private String _resultSummary = "";

	private long _durationMs = 0L;

	private String _status = "";

	/**
	 * Creates a {@link TimelineEventDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.TimelineEventDto#create()
	 */
	protected TimelineEventDto() {
		super();
	}

	/**
	 * Event identifier.
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.ai.chat.api.messages.TimelineEventDto setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * Event type: "tool_call" or "agent_call".
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.ai.chat.api.messages.TimelineEventDto setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * Tool name (for tool_call events).
	 */
	public final String getToolName() {
		return _toolName;
	}

	/**
	 * @see #getToolName()
	 */
	public com.top_logic.ai.chat.api.messages.TimelineEventDto setToolName(String value) {
		internalSetToolName(value);
		return this;
	}

	/** Internal setter for {@link #getToolName()} without chain call utility. */
	protected final void internalSetToolName(String value) {
		_toolName = value;
	}

	/**
	 * Event name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.ai.chat.api.messages.TimelineEventDto setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Tool arguments as JSON string (for tool_call events).
	 */
	public final String getArgsJson() {
		return _argsJson;
	}

	/**
	 * @see #getArgsJson()
	 */
	public com.top_logic.ai.chat.api.messages.TimelineEventDto setArgsJson(String value) {
		internalSetArgsJson(value);
		return this;
	}

	/** Internal setter for {@link #getArgsJson()} without chain call utility. */
	protected final void internalSetArgsJson(String value) {
		_argsJson = value;
	}

	/**
	 * Tool result (for tool_call events).
	 */
	public final String getResult() {
		return _result;
	}

	/**
	 * @see #getResult()
	 */
	public com.top_logic.ai.chat.api.messages.TimelineEventDto setResult(String value) {
		internalSetResult(value);
		return this;
	}

	/** Internal setter for {@link #getResult()} without chain call utility. */
	protected final void internalSetResult(String value) {
		_result = value;
	}

	/**
	 * Source agent name (for agent_call events).
	 */
	public final String getFromAgent() {
		return _fromAgent;
	}

	/**
	 * @see #getFromAgent()
	 */
	public com.top_logic.ai.chat.api.messages.TimelineEventDto setFromAgent(String value) {
		internalSetFromAgent(value);
		return this;
	}

	/** Internal setter for {@link #getFromAgent()} without chain call utility. */
	protected final void internalSetFromAgent(String value) {
		_fromAgent = value;
	}

	/**
	 * Target agent name (for agent_call events).
	 */
	public final String getToAgent() {
		return _toAgent;
	}

	/**
	 * @see #getToAgent()
	 */
	public com.top_logic.ai.chat.api.messages.TimelineEventDto setToAgent(String value) {
		internalSetToAgent(value);
		return this;
	}

	/** Internal setter for {@link #getToAgent()} without chain call utility. */
	protected final void internalSetToAgent(String value) {
		_toAgent = value;
	}

	/**
	 * Input summary (for agent_call events).
	 */
	public final String getInputSummary() {
		return _inputSummary;
	}

	/**
	 * @see #getInputSummary()
	 */
	public com.top_logic.ai.chat.api.messages.TimelineEventDto setInputSummary(String value) {
		internalSetInputSummary(value);
		return this;
	}

	/** Internal setter for {@link #getInputSummary()} without chain call utility. */
	protected final void internalSetInputSummary(String value) {
		_inputSummary = value;
	}

	/**
	 * Result summary (for agent_call events).
	 */
	public final String getResultSummary() {
		return _resultSummary;
	}

	/**
	 * @see #getResultSummary()
	 */
	public com.top_logic.ai.chat.api.messages.TimelineEventDto setResultSummary(String value) {
		internalSetResultSummary(value);
		return this;
	}

	/** Internal setter for {@link #getResultSummary()} without chain call utility. */
	protected final void internalSetResultSummary(String value) {
		_resultSummary = value;
	}

	/**
	 * Duration in milliseconds (for tool_call events).
	 */
	public final long getDurationMs() {
		return _durationMs;
	}

	/**
	 * @see #getDurationMs()
	 */
	public com.top_logic.ai.chat.api.messages.TimelineEventDto setDurationMs(long value) {
		internalSetDurationMs(value);
		return this;
	}

	/** Internal setter for {@link #getDurationMs()} without chain call utility. */
	protected final void internalSetDurationMs(long value) {
		_durationMs = value;
	}

	/**
	 * Tool call status: "success" or "error" (for tool_call events).
	 */
	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.ai.chat.api.messages.TimelineEventDto setStatus(String value) {
		internalSetStatus(value);
		return this;
	}

	/** Internal setter for {@link #getStatus()} without chain call utility. */
	protected final void internalSetStatus(String value) {
		_status = value;
	}

	@Override
	public String jsonType() {
		return TIMELINE_EVENT_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			TYPE__PROP, 
			TOOL_NAME__PROP, 
			NAME__PROP, 
			ARGS_JSON__PROP, 
			RESULT__PROP, 
			FROM_AGENT__PROP, 
			TO_AGENT__PROP, 
			INPUT_SUMMARY__PROP, 
			RESULT_SUMMARY__PROP, 
			DURATION_MS__PROP, 
			STATUS__PROP);
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
			case ID__PROP: return getId();
			case TYPE__PROP: return getType();
			case TOOL_NAME__PROP: return getToolName();
			case NAME__PROP: return getName();
			case ARGS_JSON__PROP: return getArgsJson();
			case RESULT__PROP: return getResult();
			case FROM_AGENT__PROP: return getFromAgent();
			case TO_AGENT__PROP: return getToAgent();
			case INPUT_SUMMARY__PROP: return getInputSummary();
			case RESULT_SUMMARY__PROP: return getResultSummary();
			case DURATION_MS__PROP: return getDurationMs();
			case STATUS__PROP: return getStatus();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case TYPE__PROP: internalSetType((String) value); break;
			case TOOL_NAME__PROP: internalSetToolName((String) value); break;
			case NAME__PROP: internalSetName((String) value); break;
			case ARGS_JSON__PROP: internalSetArgsJson((String) value); break;
			case RESULT__PROP: internalSetResult((String) value); break;
			case FROM_AGENT__PROP: internalSetFromAgent((String) value); break;
			case TO_AGENT__PROP: internalSetToAgent((String) value); break;
			case INPUT_SUMMARY__PROP: internalSetInputSummary((String) value); break;
			case RESULT_SUMMARY__PROP: internalSetResultSummary((String) value); break;
			case DURATION_MS__PROP: internalSetDurationMs((long) value); break;
			case STATUS__PROP: internalSetStatus((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.TimelineEventDto readTimelineEventDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.TimelineEventDto result = new com.top_logic.ai.chat.api.messages.TimelineEventDto();
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
		out.name(ID__PROP);
		out.value(getId());
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(TOOL_NAME__PROP);
		out.value(getToolName());
		out.name(NAME__PROP);
		out.value(getName());
		out.name(ARGS_JSON__PROP);
		out.value(getArgsJson());
		out.name(RESULT__PROP);
		out.value(getResult());
		out.name(FROM_AGENT__PROP);
		out.value(getFromAgent());
		out.name(TO_AGENT__PROP);
		out.value(getToAgent());
		out.name(INPUT_SUMMARY__PROP);
		out.value(getInputSummary());
		out.name(RESULT_SUMMARY__PROP);
		out.value(getResultSummary());
		out.name(DURATION_MS__PROP);
		out.value(getDurationMs());
		out.name(STATUS__PROP);
		out.value(getStatus());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOOL_NAME__PROP: setToolName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ARGS_JSON__PROP: setArgsJson(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RESULT__PROP: setResult(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FROM_AGENT__PROP: setFromAgent(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TO_AGENT__PROP: setToAgent(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case INPUT_SUMMARY__PROP: setInputSummary(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RESULT_SUMMARY__PROP: setResultSummary(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DURATION_MS__PROP: setDurationMs(in.nextLong()); break;
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
