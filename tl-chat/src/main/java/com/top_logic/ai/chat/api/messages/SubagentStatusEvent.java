package com.top_logic.ai.chat.api.messages;

/**
 * SSE event carrying subagent lifecycle status.
 *
 * <p>
 * Emitted for {@code subagent_started}, {@code subagent_running}, {@code subagent_completed},
 * {@code subagent_failed}, and {@code subagent_cancelled} events.
 * </p>
 */
public class SubagentStatusEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.SubagentStatusEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.SubagentStatusEvent create() {
		return new com.top_logic.ai.chat.api.messages.SubagentStatusEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.SubagentStatusEvent} type in JSON format. */
	public static final String SUBAGENT_STATUS_EVENT__TYPE = "SubagentStatusEvent";

	/** @see #getSubagentId() */
	public static final String SUBAGENT_ID__PROP = "subagentId";

	/** @see #getTargetAgentName() */
	public static final String TARGET_AGENT_NAME__PROP = "targetAgentName";

	/** @see #getDetail() */
	public static final String DETAIL__PROP = "detail";

	/** @see #getTotalTokens() */
	public static final String TOTAL_TOKENS__PROP = "totalTokens";

	/** @see #getDurationMs() */
	public static final String DURATION_MS__PROP = "durationMs";

	/** @see #getParentToolCallId() */
	public static final String PARENT_TOOL_CALL_ID__PROP = "parentToolCallId";

	/** @see #isQueued() */
	public static final String QUEUED__PROP = "queued";

	/** @see #getChildRunId() */
	public static final String CHILD_RUN_ID__PROP = "childRunId";

	/** @see #getStopReason() */
	public static final String STOP_REASON__PROP = "stopReason";

	private String _subagentId = "";

	private String _targetAgentName = "";

	private String _detail = "";

	private int _totalTokens = 0;

	private long _durationMs = 0L;

	private String _parentToolCallId = "";

	private boolean _queued = false;

	private String _childRunId = "";

	private String _stopReason = "";

	/**
	 * Creates a {@link SubagentStatusEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.SubagentStatusEvent#create()
	 */
	protected SubagentStatusEvent() {
		super();
	}

	/**
	 * The subagent job id within the session.
	 */
	public final String getSubagentId() {
		return _subagentId;
	}

	/**
	 * @see #getSubagentId()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentStatusEvent setSubagentId(String value) {
		internalSetSubagentId(value);
		return this;
	}

	/** Internal setter for {@link #getSubagentId()} without chain call utility. */
	protected final void internalSetSubagentId(String value) {
		_subagentId = value;
	}

	/**
	 * The delegated-to agent name.
	 */
	public final String getTargetAgentName() {
		return _targetAgentName;
	}

	/**
	 * @see #getTargetAgentName()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentStatusEvent setTargetAgentName(String value) {
		internalSetTargetAgentName(value);
		return this;
	}

	/** Internal setter for {@link #getTargetAgentName()} without chain call utility. */
	protected final void internalSetTargetAgentName(String value) {
		_targetAgentName = value;
	}

	/**
	 * The task brief (on start) or final text / error summary (on completion/failure).
	 */
	public final String getDetail() {
		return _detail;
	}

	/**
	 * @see #getDetail()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentStatusEvent setDetail(String value) {
		internalSetDetail(value);
		return this;
	}

	/** Internal setter for {@link #getDetail()} without chain call utility. */
	protected final void internalSetDetail(String value) {
		_detail = value;
	}

	/**
	 * Total tokens the subagent used (0 until completion).
	 */
	public final int getTotalTokens() {
		return _totalTokens;
	}

	/**
	 * @see #getTotalTokens()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentStatusEvent setTotalTokens(int value) {
		internalSetTotalTokens(value);
		return this;
	}

	/** Internal setter for {@link #getTotalTokens()} without chain call utility. */
	protected final void internalSetTotalTokens(int value) {
		_totalTokens = value;
	}

	/**
	 * Run duration in milliseconds (0 until completion).
	 */
	public final long getDurationMs() {
		return _durationMs;
	}

	/**
	 * @see #getDurationMs()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentStatusEvent setDurationMs(long value) {
		internalSetDurationMs(value);
		return this;
	}

	/** Internal setter for {@link #getDurationMs()} without chain call utility. */
	protected final void internalSetDurationMs(long value) {
		_durationMs = value;
	}

	/**
	 * The id of the lead's tool call that started the subagent, so the chat can show its card at that
	 * call; sent on start, on leaving the queue and on the end, so that a window which did not see the
	 * start finds the card.
	 */
	public final String getParentToolCallId() {
		return _parentToolCallId;
	}

	/**
	 * @see #getParentToolCallId()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentStatusEvent setParentToolCallId(String value) {
		internalSetParentToolCallId(value);
		return this;
	}

	/** Internal setter for {@link #getParentToolCallId()} without chain call utility. */
	protected final void internalSetParentToolCallId(String value) {
		_parentToolCallId = value;
	}

	/**
	 * Whether the subagent waits in the queue for a free slot rather than working; sent on start. A
	 * {@code subagent_running} event says when it has started.
	 */
	public final boolean isQueued() {
		return _queued;
	}

	/**
	 * @see #isQueued()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentStatusEvent setQueued(boolean value) {
		internalSetQueued(value);
		return this;
	}

	/** Internal setter for {@link #isQueued()} without chain call utility. */
	protected final void internalSetQueued(boolean value) {
		_queued = value;
	}

	/**
	 * The subagent's own run id, its transcript's key; sent on the end, empty if unknown.
	 */
	public final String getChildRunId() {
		return _childRunId;
	}

	/**
	 * @see #getChildRunId()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentStatusEvent setChildRunId(String value) {
		internalSetChildRunId(value);
		return this;
	}

	/** Internal setter for {@link #getChildRunId()} without chain call utility. */
	protected final void internalSetChildRunId(String value) {
		_childRunId = value;
	}

	/**
	 * On completion: why the subagent's run stopped, a {@code StopReason} name; empty otherwise.
	 */
	public final String getStopReason() {
		return _stopReason;
	}

	/**
	 * @see #getStopReason()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentStatusEvent setStopReason(String value) {
		internalSetStopReason(value);
		return this;
	}

	/** Internal setter for {@link #getStopReason()} without chain call utility. */
	protected final void internalSetStopReason(String value) {
		_stopReason = value;
	}

	@Override
	public String jsonType() {
		return SUBAGENT_STATUS_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SUBAGENT_ID__PROP, 
			TARGET_AGENT_NAME__PROP, 
			DETAIL__PROP, 
			TOTAL_TOKENS__PROP, 
			DURATION_MS__PROP, 
			PARENT_TOOL_CALL_ID__PROP, 
			QUEUED__PROP, 
			CHILD_RUN_ID__PROP, 
			STOP_REASON__PROP);
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
			case DETAIL__PROP: return getDetail();
			case TOTAL_TOKENS__PROP: return getTotalTokens();
			case DURATION_MS__PROP: return getDurationMs();
			case PARENT_TOOL_CALL_ID__PROP: return getParentToolCallId();
			case QUEUED__PROP: return isQueued();
			case CHILD_RUN_ID__PROP: return getChildRunId();
			case STOP_REASON__PROP: return getStopReason();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SUBAGENT_ID__PROP: internalSetSubagentId((String) value); break;
			case TARGET_AGENT_NAME__PROP: internalSetTargetAgentName((String) value); break;
			case DETAIL__PROP: internalSetDetail((String) value); break;
			case TOTAL_TOKENS__PROP: internalSetTotalTokens((int) value); break;
			case DURATION_MS__PROP: internalSetDurationMs((long) value); break;
			case PARENT_TOOL_CALL_ID__PROP: internalSetParentToolCallId((String) value); break;
			case QUEUED__PROP: internalSetQueued((boolean) value); break;
			case CHILD_RUN_ID__PROP: internalSetChildRunId((String) value); break;
			case STOP_REASON__PROP: internalSetStopReason((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.SubagentStatusEvent readSubagentStatusEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.SubagentStatusEvent result = new com.top_logic.ai.chat.api.messages.SubagentStatusEvent();
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
		out.name(DETAIL__PROP);
		out.value(getDetail());
		out.name(TOTAL_TOKENS__PROP);
		out.value(getTotalTokens());
		out.name(DURATION_MS__PROP);
		out.value(getDurationMs());
		out.name(PARENT_TOOL_CALL_ID__PROP);
		out.value(getParentToolCallId());
		out.name(QUEUED__PROP);
		out.value(isQueued());
		out.name(CHILD_RUN_ID__PROP);
		out.value(getChildRunId());
		out.name(STOP_REASON__PROP);
		out.value(getStopReason());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SUBAGENT_ID__PROP: setSubagentId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TARGET_AGENT_NAME__PROP: setTargetAgentName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DETAIL__PROP: setDetail(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOTAL_TOKENS__PROP: setTotalTokens(in.nextInt()); break;
			case DURATION_MS__PROP: setDurationMs(in.nextLong()); break;
			case PARENT_TOOL_CALL_ID__PROP: setParentToolCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case QUEUED__PROP: setQueued(in.nextBoolean()); break;
			case CHILD_RUN_ID__PROP: setChildRunId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STOP_REASON__PROP: setStopReason(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
