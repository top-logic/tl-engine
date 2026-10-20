package com.top_logic.ai.chat.api.messages;

/**
 * One subagent whose news a lead turn opens with.
 */
public class WokenSubagent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.WokenSubagent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.WokenSubagent create() {
		return new com.top_logic.ai.chat.api.messages.WokenSubagent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.WokenSubagent} type in JSON format. */
	public static final String WOKEN_SUBAGENT__TYPE = "WokenSubagent";

	/** @see #getSubagentId() */
	public static final String SUBAGENT_ID__PROP = "subagentId";

	/** @see #getAgentName() */
	public static final String AGENT_NAME__PROP = "agentName";

	/** @see #getReason() */
	public static final String REASON__PROP = "reason";

	/** @see #getParentToolCallId() */
	public static final String PARENT_TOOL_CALL_ID__PROP = "parentToolCallId";

	/** @see #getQuestion() */
	public static final String QUESTION__PROP = "question";

	private String _subagentId = "";

	private String _agentName = "";

	private String _reason = "";

	private String _parentToolCallId = "";

	private String _question = "";

	/**
	 * Creates a {@link WokenSubagent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.WokenSubagent#create()
	 */
	protected WokenSubagent() {
		super();
	}

	/**
	 * The subagent's id within its chat, e.g. {@code sa-2}.
	 */
	public final String getSubagentId() {
		return _subagentId;
	}

	/**
	 * @see #getSubagentId()
	 */
	public com.top_logic.ai.chat.api.messages.WokenSubagent setSubagentId(String value) {
		internalSetSubagentId(value);
		return this;
	}

	/** Internal setter for {@link #getSubagentId()} without chain call utility. */
	protected final void internalSetSubagentId(String value) {
		_subagentId = value;
	}

	/**
	 * The agent it runs.
	 */
	public final String getAgentName() {
		return _agentName;
	}

	/**
	 * @see #getAgentName()
	 */
	public com.top_logic.ai.chat.api.messages.WokenSubagent setAgentName(String value) {
		internalSetAgentName(value);
		return this;
	}

	/** Internal setter for {@link #getAgentName()} without chain call utility. */
	protected final void internalSetAgentName(String value) {
		_agentName = value;
	}

	/**
	 * Why it reported back: {@code FINISHED}, {@code FAILED}, {@code CANCELLED} or {@code ASKS}.
	 */
	public final String getReason() {
		return _reason;
	}

	/**
	 * @see #getReason()
	 */
	public com.top_logic.ai.chat.api.messages.WokenSubagent setReason(String value) {
		internalSetReason(value);
		return this;
	}

	/** Internal setter for {@link #getReason()} without chain call utility. */
	protected final void internalSetReason(String value) {
		_reason = value;
	}

	/**
	 * The id of the lead's tool call that started it, where its card stands; empty if unknown.
	 */
	public final String getParentToolCallId() {
		return _parentToolCallId;
	}

	/**
	 * @see #getParentToolCallId()
	 */
	public com.top_logic.ai.chat.api.messages.WokenSubagent setParentToolCallId(String value) {
		internalSetParentToolCallId(value);
		return this;
	}

	/** Internal setter for {@link #getParentToolCallId()} without chain call utility. */
	protected final void internalSetParentToolCallId(String value) {
		_parentToolCallId = value;
	}

	/**
	 * For {@code ASKS}, the start of what it asks; empty otherwise.
	 */
	public final String getQuestion() {
		return _question;
	}

	/**
	 * @see #getQuestion()
	 */
	public com.top_logic.ai.chat.api.messages.WokenSubagent setQuestion(String value) {
		internalSetQuestion(value);
		return this;
	}

	/** Internal setter for {@link #getQuestion()} without chain call utility. */
	protected final void internalSetQuestion(String value) {
		_question = value;
	}

	@Override
	public String jsonType() {
		return WOKEN_SUBAGENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SUBAGENT_ID__PROP, 
			AGENT_NAME__PROP, 
			REASON__PROP, 
			PARENT_TOOL_CALL_ID__PROP, 
			QUESTION__PROP);
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
			case SUBAGENT_ID__PROP: return getSubagentId();
			case AGENT_NAME__PROP: return getAgentName();
			case REASON__PROP: return getReason();
			case PARENT_TOOL_CALL_ID__PROP: return getParentToolCallId();
			case QUESTION__PROP: return getQuestion();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SUBAGENT_ID__PROP: internalSetSubagentId((String) value); break;
			case AGENT_NAME__PROP: internalSetAgentName((String) value); break;
			case REASON__PROP: internalSetReason((String) value); break;
			case PARENT_TOOL_CALL_ID__PROP: internalSetParentToolCallId((String) value); break;
			case QUESTION__PROP: internalSetQuestion((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.WokenSubagent readWokenSubagent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.WokenSubagent result = new com.top_logic.ai.chat.api.messages.WokenSubagent();
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
		out.name(SUBAGENT_ID__PROP);
		out.value(getSubagentId());
		out.name(AGENT_NAME__PROP);
		out.value(getAgentName());
		out.name(REASON__PROP);
		out.value(getReason());
		out.name(PARENT_TOOL_CALL_ID__PROP);
		out.value(getParentToolCallId());
		out.name(QUESTION__PROP);
		out.value(getQuestion());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SUBAGENT_ID__PROP: setSubagentId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case AGENT_NAME__PROP: setAgentName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REASON__PROP: setReason(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PARENT_TOOL_CALL_ID__PROP: setParentToolCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case QUESTION__PROP: setQuestion(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
