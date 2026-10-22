package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * A background subagent a turn started; see {@code Part.SubagentPart}. Its steps are in its own
 * transcript, keyed by {@link #getChildRunId()}.
 */
public class SubagentPartDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.SubagentPartDto} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.SubagentPartDto create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.SubagentPartDto();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.SubagentPartDto} type in JSON format. */
	public static final String SUBAGENT_PART_DTO__TYPE = "SubagentPartDto";

	/** @see #getSubagentId() */
	public static final String SUBAGENT_ID__PROP = "subagentId";

	/** @see #getAgentName() */
	public static final String AGENT_NAME__PROP = "agentName";

	/** @see #getTask() */
	public static final String TASK__PROP = "task";

	/** @see #getParentToolCallId() */
	public static final String PARENT_TOOL_CALL_ID__PROP = "parentToolCallId";

	/** @see #getChildRunId() */
	public static final String CHILD_RUN_ID__PROP = "childRunId";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	/** @see #getSummary() */
	public static final String SUMMARY__PROP = "summary";

	private String _subagentId = "";

	private String _agentName = "";

	private String _task = "";

	private String _parentToolCallId = "";

	private String _childRunId = "";

	private String _status = "";

	private String _summary = "";

	/**
	 * Creates a {@link SubagentPartDto} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.SubagentPartDto#create()
	 */
	protected SubagentPartDto() {
		super();
	}

	/**
	 * The subagent's id within its chat, e.g. "sa-2".
	 */
	public final String getSubagentId() {
		return _subagentId;
	}

	/**
	 * @see #getSubagentId()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.SubagentPartDto setSubagentId(String value) {
		internalSetSubagentId(value);
		return this;
	}

	/** Internal setter for {@link #getSubagentId()} without chain call utility. */
	protected final void internalSetSubagentId(String value) {
		_subagentId = value;
	}

	/**
	 * The agent the subagent runs.
	 */
	public final String getAgentName() {
		return _agentName;
	}

	/**
	 * @see #getAgentName()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.SubagentPartDto setAgentName(String value) {
		internalSetAgentName(value);
		return this;
	}

	/** Internal setter for {@link #getAgentName()} without chain call utility. */
	protected final void internalSetAgentName(String value) {
		_agentName = value;
	}

	/**
	 * The task brief it was given.
	 */
	public final String getTask() {
		return _task;
	}

	/**
	 * @see #getTask()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.SubagentPartDto setTask(String value) {
		internalSetTask(value);
		return this;
	}

	/** Internal setter for {@link #getTask()} without chain call utility. */
	protected final void internalSetTask(String value) {
		_task = value;
	}

	/**
	 * The id of the lead's tool call that started it.
	 */
	public final String getParentToolCallId() {
		return _parentToolCallId;
	}

	/**
	 * @see #getParentToolCallId()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.SubagentPartDto setParentToolCallId(String value) {
		internalSetParentToolCallId(value);
		return this;
	}

	/** Internal setter for {@link #getParentToolCallId()} without chain call utility. */
	protected final void internalSetParentToolCallId(String value) {
		_parentToolCallId = value;
	}

	/**
	 * The run id of the child run; empty until known.
	 */
	public final String getChildRunId() {
		return _childRunId;
	}

	/**
	 * @see #getChildRunId()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.SubagentPartDto setChildRunId(String value) {
		internalSetChildRunId(value);
		return this;
	}

	/** Internal setter for {@link #getChildRunId()} without chain call utility. */
	protected final void internalSetChildRunId(String value) {
		_childRunId = value;
	}

	/**
	 * The job's state: RUNNING, DONE, FAILED or CANCELLED.
	 */
	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.SubagentPartDto setStatus(String value) {
		internalSetStatus(value);
		return this;
	}

	/** Internal setter for {@link #getStatus()} without chain call utility. */
	protected final void internalSetStatus(String value) {
		_status = value;
	}

	/**
	 * A short summary of how it ended; empty while it runs.
	 */
	public final String getSummary() {
		return _summary;
	}

	/**
	 * @see #getSummary()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.SubagentPartDto setSummary(String value) {
		internalSetSummary(value);
		return this;
	}

	/** Internal setter for {@link #getSummary()} without chain call utility. */
	protected final void internalSetSummary(String value) {
		_summary = value;
	}

	@Override
	public String jsonType() {
		return SUBAGENT_PART_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SUBAGENT_ID__PROP, 
			AGENT_NAME__PROP, 
			TASK__PROP, 
			PARENT_TOOL_CALL_ID__PROP, 
			CHILD_RUN_ID__PROP, 
			STATUS__PROP, 
			SUMMARY__PROP);
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
			case TASK__PROP: return getTask();
			case PARENT_TOOL_CALL_ID__PROP: return getParentToolCallId();
			case CHILD_RUN_ID__PROP: return getChildRunId();
			case STATUS__PROP: return getStatus();
			case SUMMARY__PROP: return getSummary();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SUBAGENT_ID__PROP: internalSetSubagentId((String) value); break;
			case AGENT_NAME__PROP: internalSetAgentName((String) value); break;
			case TASK__PROP: internalSetTask((String) value); break;
			case PARENT_TOOL_CALL_ID__PROP: internalSetParentToolCallId((String) value); break;
			case CHILD_RUN_ID__PROP: internalSetChildRunId((String) value); break;
			case STATUS__PROP: internalSetStatus((String) value); break;
			case SUMMARY__PROP: internalSetSummary((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.SubagentPartDto readSubagentPartDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.SubagentPartDto result = new com.top_logic.tl.ai.core.session.persist.messages.SubagentPartDto();
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
		out.name(TASK__PROP);
		out.value(getTask());
		out.name(PARENT_TOOL_CALL_ID__PROP);
		out.value(getParentToolCallId());
		out.name(CHILD_RUN_ID__PROP);
		out.value(getChildRunId());
		out.name(STATUS__PROP);
		out.value(getStatus());
		out.name(SUMMARY__PROP);
		out.value(getSummary());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SUBAGENT_ID__PROP: setSubagentId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case AGENT_NAME__PROP: setAgentName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TASK__PROP: setTask(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PARENT_TOOL_CALL_ID__PROP: setParentToolCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CHILD_RUN_ID__PROP: setChildRunId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SUMMARY__PROP: setSummary(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
