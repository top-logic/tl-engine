package com.top_logic.ai.chat.api.messages;

/**
 * Tool call part with invocation details and result.
 */
public class ToolCallPartDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ToolCallPartDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ToolCallPartDto create() {
		return new com.top_logic.ai.chat.api.messages.ToolCallPartDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ToolCallPartDto} type in JSON format. */
	public static final String TOOL_CALL_PART_DTO__TYPE = "ToolCallPartDto";

	/** @see #getToolName() */
	public static final String TOOL_NAME__PROP = "toolName";

	/** @see #getArgsJson() */
	public static final String ARGS_JSON__PROP = "argsJson";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	/** @see #getResult() */
	public static final String RESULT__PROP = "result";

	/** @see #getResultRef() */
	public static final String RESULT_REF__PROP = "resultRef";

	/** @see #getErrorMessage() */
	public static final String ERROR_MESSAGE__PROP = "errorMessage";

	/** @see #getDenyReason() */
	public static final String DENY_REASON__PROP = "denyReason";

	/** @see #getDurationMs() */
	public static final String DURATION_MS__PROP = "durationMs";

	private String _toolName = "";

	private String _argsJson = "";

	private String _status = "";

	private String _result = "";

	private String _resultRef = "";

	private String _errorMessage = "";

	private String _denyReason = "";

	private long _durationMs = 0L;

	/**
	 * Creates a {@link ToolCallPartDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ToolCallPartDto#create()
	 */
	protected ToolCallPartDto() {
		super();
	}

	/**
	 * Name of the tool that was called.
	 */
	public final String getToolName() {
		return _toolName;
	}

	/**
	 * @see #getToolName()
	 */
	public com.top_logic.ai.chat.api.messages.ToolCallPartDto setToolName(String value) {
		internalSetToolName(value);
		return this;
	}

	/** Internal setter for {@link #getToolName()} without chain call utility. */
	protected final void internalSetToolName(String value) {
		_toolName = value;
	}

	/**
	 * JSON-encoded arguments passed to the tool.
	 */
	public final String getArgsJson() {
		return _argsJson;
	}

	/**
	 * @see #getArgsJson()
	 */
	public com.top_logic.ai.chat.api.messages.ToolCallPartDto setArgsJson(String value) {
		internalSetArgsJson(value);
		return this;
	}

	/** Internal setter for {@link #getArgsJson()} without chain call utility. */
	protected final void internalSetArgsJson(String value) {
		_argsJson = value;
	}

	/**
	 * Execution status of the tool call.
	 */
	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.ai.chat.api.messages.ToolCallPartDto setStatus(String value) {
		internalSetStatus(value);
		return this;
	}

	/** Internal setter for {@link #getStatus()} without chain call utility. */
	protected final void internalSetStatus(String value) {
		_status = value;
	}

	/**
	 * The result returned by the tool.
	 */
	public final String getResult() {
		return _result;
	}

	/**
	 * @see #getResult()
	 */
	public com.top_logic.ai.chat.api.messages.ToolCallPartDto setResult(String value) {
		internalSetResult(value);
		return this;
	}

	/** Internal setter for {@link #getResult()} without chain call utility. */
	protected final void internalSetResult(String value) {
		_result = value;
	}

	/**
	 * Reference to an externally stored result.
	 */
	public final String getResultRef() {
		return _resultRef;
	}

	/**
	 * @see #getResultRef()
	 */
	public com.top_logic.ai.chat.api.messages.ToolCallPartDto setResultRef(String value) {
		internalSetResultRef(value);
		return this;
	}

	/** Internal setter for {@link #getResultRef()} without chain call utility. */
	protected final void internalSetResultRef(String value) {
		_resultRef = value;
	}

	/**
	 * Error message if the tool call failed.
	 */
	public final String getErrorMessage() {
		return _errorMessage;
	}

	/**
	 * @see #getErrorMessage()
	 */
	public com.top_logic.ai.chat.api.messages.ToolCallPartDto setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	/** Internal setter for {@link #getErrorMessage()} without chain call utility. */
	protected final void internalSetErrorMessage(String value) {
		_errorMessage = value;
	}

	/**
	 * Reason the tool call was denied.
	 */
	public final String getDenyReason() {
		return _denyReason;
	}

	/**
	 * @see #getDenyReason()
	 */
	public com.top_logic.ai.chat.api.messages.ToolCallPartDto setDenyReason(String value) {
		internalSetDenyReason(value);
		return this;
	}

	/** Internal setter for {@link #getDenyReason()} without chain call utility. */
	protected final void internalSetDenyReason(String value) {
		_denyReason = value;
	}

	/**
	 * Duration of the tool call in milliseconds.
	 */
	public final long getDurationMs() {
		return _durationMs;
	}

	/**
	 * @see #getDurationMs()
	 */
	public com.top_logic.ai.chat.api.messages.ToolCallPartDto setDurationMs(long value) {
		internalSetDurationMs(value);
		return this;
	}

	/** Internal setter for {@link #getDurationMs()} without chain call utility. */
	protected final void internalSetDurationMs(long value) {
		_durationMs = value;
	}

	@Override
	public String jsonType() {
		return TOOL_CALL_PART_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TOOL_NAME__PROP, 
			ARGS_JSON__PROP, 
			STATUS__PROP, 
			RESULT__PROP, 
			RESULT_REF__PROP, 
			ERROR_MESSAGE__PROP, 
			DENY_REASON__PROP, 
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
			case TOOL_NAME__PROP: return getToolName();
			case ARGS_JSON__PROP: return getArgsJson();
			case STATUS__PROP: return getStatus();
			case RESULT__PROP: return getResult();
			case RESULT_REF__PROP: return getResultRef();
			case ERROR_MESSAGE__PROP: return getErrorMessage();
			case DENY_REASON__PROP: return getDenyReason();
			case DURATION_MS__PROP: return getDurationMs();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TOOL_NAME__PROP: internalSetToolName((String) value); break;
			case ARGS_JSON__PROP: internalSetArgsJson((String) value); break;
			case STATUS__PROP: internalSetStatus((String) value); break;
			case RESULT__PROP: internalSetResult((String) value); break;
			case RESULT_REF__PROP: internalSetResultRef((String) value); break;
			case ERROR_MESSAGE__PROP: internalSetErrorMessage((String) value); break;
			case DENY_REASON__PROP: internalSetDenyReason((String) value); break;
			case DURATION_MS__PROP: internalSetDurationMs((long) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ToolCallPartDto readToolCallPartDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ToolCallPartDto result = new com.top_logic.ai.chat.api.messages.ToolCallPartDto();
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
		out.name(TOOL_NAME__PROP);
		out.value(getToolName());
		out.name(ARGS_JSON__PROP);
		out.value(getArgsJson());
		out.name(STATUS__PROP);
		out.value(getStatus());
		out.name(RESULT__PROP);
		out.value(getResult());
		out.name(RESULT_REF__PROP);
		out.value(getResultRef());
		out.name(ERROR_MESSAGE__PROP);
		out.value(getErrorMessage());
		out.name(DENY_REASON__PROP);
		out.value(getDenyReason());
		out.name(DURATION_MS__PROP);
		out.value(getDurationMs());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TOOL_NAME__PROP: setToolName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ARGS_JSON__PROP: setArgsJson(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RESULT__PROP: setResult(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RESULT_REF__PROP: setResultRef(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ERROR_MESSAGE__PROP: setErrorMessage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DENY_REASON__PROP: setDenyReason(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DURATION_MS__PROP: setDurationMs(in.nextLong()); break;
			default: super.readField(in, field);
		}
	}

}
