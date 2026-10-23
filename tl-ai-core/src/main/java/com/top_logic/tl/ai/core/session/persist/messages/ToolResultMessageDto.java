package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * The outcome of one tool call, as the model sees it.
 */
public class ToolResultMessageDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto} type in JSON format. */
	public static final String TOOL_RESULT_MESSAGE_DTO__TYPE = "ToolResultMessageDto";

	/** @see #getToolCallId() */
	public static final String TOOL_CALL_ID__PROP = "toolCallId";

	/** @see #getToolName() */
	public static final String TOOL_NAME__PROP = "toolName";

	/** @see #getState() */
	public static final String STATE__PROP = "state";

	private String _toolCallId = "";

	private String _toolName = "";

	private com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto _state = null;

	/**
	 * Creates a {@link ToolResultMessageDto} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto#create()
	 */
	protected ToolResultMessageDto() {
		super();
	}

	/**
	 * ID of the tool call this result answers.
	 */
	public final String getToolCallId() {
		return _toolCallId;
	}

	/**
	 * @see #getToolCallId()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto setToolCallId(String value) {
		internalSetToolCallId(value);
		return this;
	}

	/** Internal setter for {@link #getToolCallId()} without chain call utility. */
	protected final void internalSetToolCallId(String value) {
		_toolCallId = value;
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
	public com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto setToolName(String value) {
		internalSetToolName(value);
		return this;
	}

	/** Internal setter for {@link #getToolName()} without chain call utility. */
	protected final void internalSetToolName(String value) {
		_toolName = value;
	}

	/**
	 * The state of the tool call.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto getState() {
		return _state;
	}

	/**
	 * @see #getState()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto setState(com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto value) {
		internalSetState(value);
		return this;
	}

	/** Internal setter for {@link #getState()} without chain call utility. */
	protected final void internalSetState(com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto value) {
		_state = value;
	}

	/**
	 * Checks, whether {@link #getState()} has a value.
	 */
	public final boolean hasState() {
		return _state != null;
	}

	@Override
	public String jsonType() {
		return TOOL_RESULT_MESSAGE_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TOOL_CALL_ID__PROP, 
			TOOL_NAME__PROP, 
			STATE__PROP);
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
			case STATE__PROP: return getState();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TOOL_CALL_ID__PROP: internalSetToolCallId((String) value); break;
			case TOOL_NAME__PROP: internalSetToolName((String) value); break;
			case STATE__PROP: internalSetState((com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto readToolResultMessageDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto result = new com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto();
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
		if (hasState()) {
			out.name(STATE__PROP);
			getState().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TOOL_CALL_ID__PROP: setToolCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOOL_NAME__PROP: setToolName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STATE__PROP: setState(com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto.readToolStateDto(in)); break;
			default: super.readField(in, field);
		}
	}

}
