package com.top_logic.ai.chat.api.messages;

/**
 * One background subagent, as GET /chat/subagents lists it.
 */
public class SubagentJobDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.SubagentJobDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.SubagentJobDto create() {
		return new com.top_logic.ai.chat.api.messages.SubagentJobDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.SubagentJobDto} type in JSON format. */
	public static final String SUBAGENT_JOB_DTO__TYPE = "SubagentJobDto";

	/** @see #getSubagentId() */
	public static final String SUBAGENT_ID__PROP = "subagentId";

	/** @see #getTargetAgentName() */
	public static final String TARGET_AGENT_NAME__PROP = "targetAgentName";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	/** @see #getTotalTokens() */
	public static final String TOTAL_TOKENS__PROP = "totalTokens";

	/** @see #getDurationMs() */
	public static final String DURATION_MS__PROP = "durationMs";

	/** @see #isHasTranscript() */
	public static final String HAS_TRANSCRIPT__PROP = "hasTranscript";

	/** @see #getParentToolCallId() */
	public static final String PARENT_TOOL_CALL_ID__PROP = "parentToolCallId";

	/** @see #getTask() */
	public static final String TASK__PROP = "task";

	/** @see #getChildRunId() */
	public static final String CHILD_RUN_ID__PROP = "childRunId";

	/** @see #getCost() */
	public static final String COST__PROP = "cost";

	/** @see #getCurrency() */
	public static final String CURRENCY__PROP = "currency";

	/** @see #getStopReason() */
	public static final String STOP_REASON__PROP = "stopReason";

	/** @see #getDetail() */
	public static final String DETAIL__PROP = "detail";

	private String _subagentId = "";

	private String _targetAgentName = "";

	private String _status = "";

	private long _totalTokens = 0L;

	private long _durationMs = 0L;

	private boolean _hasTranscript = false;

	private String _parentToolCallId = "";

	private String _task = "";

	private String _childRunId = "";

	private Double _cost = null;

	private String _currency = null;

	private String _stopReason = "";

	private String _detail = "";

	/**
	 * Creates a {@link SubagentJobDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.SubagentJobDto#create()
	 */
	protected SubagentJobDto() {
		super();
	}

	/**
	 * The subagent's id.
	 */
	public final String getSubagentId() {
		return _subagentId;
	}

	/**
	 * @see #getSubagentId()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentJobDto setSubagentId(String value) {
		internalSetSubagentId(value);
		return this;
	}

	/** Internal setter for {@link #getSubagentId()} without chain call utility. */
	protected final void internalSetSubagentId(String value) {
		_subagentId = value;
	}

	/**
	 * The name of the agent it runs.
	 */
	public final String getTargetAgentName() {
		return _targetAgentName;
	}

	/**
	 * @see #getTargetAgentName()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentJobDto setTargetAgentName(String value) {
		internalSetTargetAgentName(value);
		return this;
	}

	/** Internal setter for {@link #getTargetAgentName()} without chain call utility. */
	protected final void internalSetTargetAgentName(String value) {
		_targetAgentName = value;
	}

	/**
	 * The name of its {@code SubagentJobStatus}.
	 */
	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentJobDto setStatus(String value) {
		internalSetStatus(value);
		return this;
	}

	/** Internal setter for {@link #getStatus()} without chain call utility. */
	protected final void internalSetStatus(String value) {
		_status = value;
	}

	/**
	 * Tokens its run used; 0 until it has ended.
	 */
	public final long getTotalTokens() {
		return _totalTokens;
	}

	/**
	 * @see #getTotalTokens()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentJobDto setTotalTokens(long value) {
		internalSetTotalTokens(value);
		return this;
	}

	/** Internal setter for {@link #getTotalTokens()} without chain call utility. */
	protected final void internalSetTotalTokens(long value) {
		_totalTokens = value;
	}

	/**
	 * How long its run took; 0 until it has ended.
	 */
	public final long getDurationMs() {
		return _durationMs;
	}

	/**
	 * @see #getDurationMs()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentJobDto setDurationMs(long value) {
		internalSetDurationMs(value);
		return this;
	}

	/** Internal setter for {@link #getDurationMs()} without chain call utility. */
	protected final void internalSetDurationMs(long value) {
		_durationMs = value;
	}

	/**
	 * Whether its recorded run is available, so that the client offers a "view transcript"
	 * affordance only where GET /chat/subagents/{id}/transcript has something to answer.
	 */
	public final boolean isHasTranscript() {
		return _hasTranscript;
	}

	/**
	 * @see #isHasTranscript()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentJobDto setHasTranscript(boolean value) {
		internalSetHasTranscript(value);
		return this;
	}

	/** Internal setter for {@link #isHasTranscript()} without chain call utility. */
	protected final void internalSetHasTranscript(boolean value) {
		_hasTranscript = value;
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
	public com.top_logic.ai.chat.api.messages.SubagentJobDto setParentToolCallId(String value) {
		internalSetParentToolCallId(value);
		return this;
	}

	/** Internal setter for {@link #getParentToolCallId()} without chain call utility. */
	protected final void internalSetParentToolCallId(String value) {
		_parentToolCallId = value;
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
	public com.top_logic.ai.chat.api.messages.SubagentJobDto setTask(String value) {
		internalSetTask(value);
		return this;
	}

	/** Internal setter for {@link #getTask()} without chain call utility. */
	protected final void internalSetTask(String value) {
		_task = value;
	}

	/**
	 * Its own run id; empty while unknown.
	 */
	public final String getChildRunId() {
		return _childRunId;
	}

	/**
	 * @see #getChildRunId()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentJobDto setChildRunId(String value) {
		internalSetChildRunId(value);
		return this;
	}

	/** Internal setter for {@link #getChildRunId()} without chain call utility. */
	protected final void internalSetChildRunId(String value) {
		_childRunId = value;
	}

	/**
	 * What its run cost, in {@link #getCurrency()}; absent while its run has not reported one.
	 */
	public final Double getCost() {
		return _cost;
	}

	/**
	 * @see #getCost()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentJobDto setCost(Double value) {
		internalSetCost(value);
		return this;
	}

	/** Internal setter for {@link #getCost()} without chain call utility. */
	protected final void internalSetCost(Double value) {
		_cost = value;
	}

	/**
	 * Checks, whether {@link #getCost()} has a value.
	 */
	public final boolean hasCost() {
		return _cost != null;
	}

	/**
	 * The ISO 4217 code of {@link #getCost()}; absent with it.
	 */
	public final String getCurrency() {
		return _currency;
	}

	/**
	 * @see #getCurrency()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentJobDto setCurrency(String value) {
		internalSetCurrency(value);
		return this;
	}

	/** Internal setter for {@link #getCurrency()} without chain call utility. */
	protected final void internalSetCurrency(String value) {
		_currency = value;
	}

	/**
	 * Checks, whether {@link #getCurrency()} has a value.
	 */
	public final boolean hasCurrency() {
		return _currency != null;
	}

	/**
	 * Why its run stopped, a {@code StopReason} name; empty until it has ended.
	 */
	public final String getStopReason() {
		return _stopReason;
	}

	/**
	 * @see #getStopReason()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentJobDto setStopReason(String value) {
		internalSetStopReason(value);
		return this;
	}

	/** Internal setter for {@link #getStopReason()} without chain call utility. */
	protected final void internalSetStopReason(String value) {
		_stopReason = value;
	}

	/**
	 * Its final text once it has ended - the error summary for a failed one - and its task while it
	 * runs, so that the client can rebuild a finished row after a reload.
	 */
	public final String getDetail() {
		return _detail;
	}

	/**
	 * @see #getDetail()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentJobDto setDetail(String value) {
		internalSetDetail(value);
		return this;
	}

	/** Internal setter for {@link #getDetail()} without chain call utility. */
	protected final void internalSetDetail(String value) {
		_detail = value;
	}

	@Override
	public String jsonType() {
		return SUBAGENT_JOB_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SUBAGENT_ID__PROP, 
			TARGET_AGENT_NAME__PROP, 
			STATUS__PROP, 
			TOTAL_TOKENS__PROP, 
			DURATION_MS__PROP, 
			HAS_TRANSCRIPT__PROP, 
			PARENT_TOOL_CALL_ID__PROP, 
			TASK__PROP, 
			CHILD_RUN_ID__PROP, 
			COST__PROP, 
			CURRENCY__PROP, 
			STOP_REASON__PROP, 
			DETAIL__PROP);
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
			case TARGET_AGENT_NAME__PROP: return getTargetAgentName();
			case STATUS__PROP: return getStatus();
			case TOTAL_TOKENS__PROP: return getTotalTokens();
			case DURATION_MS__PROP: return getDurationMs();
			case HAS_TRANSCRIPT__PROP: return isHasTranscript();
			case PARENT_TOOL_CALL_ID__PROP: return getParentToolCallId();
			case TASK__PROP: return getTask();
			case CHILD_RUN_ID__PROP: return getChildRunId();
			case COST__PROP: return getCost();
			case CURRENCY__PROP: return getCurrency();
			case STOP_REASON__PROP: return getStopReason();
			case DETAIL__PROP: return getDetail();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SUBAGENT_ID__PROP: internalSetSubagentId((String) value); break;
			case TARGET_AGENT_NAME__PROP: internalSetTargetAgentName((String) value); break;
			case STATUS__PROP: internalSetStatus((String) value); break;
			case TOTAL_TOKENS__PROP: internalSetTotalTokens((long) value); break;
			case DURATION_MS__PROP: internalSetDurationMs((long) value); break;
			case HAS_TRANSCRIPT__PROP: internalSetHasTranscript((boolean) value); break;
			case PARENT_TOOL_CALL_ID__PROP: internalSetParentToolCallId((String) value); break;
			case TASK__PROP: internalSetTask((String) value); break;
			case CHILD_RUN_ID__PROP: internalSetChildRunId((String) value); break;
			case COST__PROP: internalSetCost((Double) value); break;
			case CURRENCY__PROP: internalSetCurrency((String) value); break;
			case STOP_REASON__PROP: internalSetStopReason((String) value); break;
			case DETAIL__PROP: internalSetDetail((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.SubagentJobDto readSubagentJobDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.SubagentJobDto result = new com.top_logic.ai.chat.api.messages.SubagentJobDto();
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
		out.name(TARGET_AGENT_NAME__PROP);
		out.value(getTargetAgentName());
		out.name(STATUS__PROP);
		out.value(getStatus());
		out.name(TOTAL_TOKENS__PROP);
		out.value(getTotalTokens());
		out.name(DURATION_MS__PROP);
		out.value(getDurationMs());
		out.name(HAS_TRANSCRIPT__PROP);
		out.value(isHasTranscript());
		out.name(PARENT_TOOL_CALL_ID__PROP);
		out.value(getParentToolCallId());
		out.name(TASK__PROP);
		out.value(getTask());
		out.name(CHILD_RUN_ID__PROP);
		out.value(getChildRunId());
		if (hasCost()) {
			out.name(COST__PROP);
			out.value(getCost());
		}
		if (hasCurrency()) {
			out.name(CURRENCY__PROP);
			out.value(getCurrency());
		}
		out.name(STOP_REASON__PROP);
		out.value(getStopReason());
		out.name(DETAIL__PROP);
		out.value(getDetail());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SUBAGENT_ID__PROP: setSubagentId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TARGET_AGENT_NAME__PROP: setTargetAgentName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOTAL_TOKENS__PROP: setTotalTokens(in.nextLong()); break;
			case DURATION_MS__PROP: setDurationMs(in.nextLong()); break;
			case HAS_TRANSCRIPT__PROP: setHasTranscript(in.nextBoolean()); break;
			case PARENT_TOOL_CALL_ID__PROP: setParentToolCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TASK__PROP: setTask(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CHILD_RUN_ID__PROP: setChildRunId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case COST__PROP: setCost(in.nextDouble()); break;
			case CURRENCY__PROP: setCurrency(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STOP_REASON__PROP: setStopReason(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DETAIL__PROP: setDetail(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
