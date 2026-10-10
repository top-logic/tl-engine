package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * Agent delegation part.
 */
public class DelegationPartDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto} type in JSON format. */
	public static final String DELEGATION_PART_DTO__TYPE = "DelegationPartDto";

	/** @see #getFromAgent() */
	public static final String FROM_AGENT__PROP = "fromAgent";

	/** @see #getToAgent() */
	public static final String TO_AGENT__PROP = "toAgent";

	/** @see #getInputSummary() */
	public static final String INPUT_SUMMARY__PROP = "inputSummary";

	/** @see #getResultSummary() */
	public static final String RESULT_SUMMARY__PROP = "resultSummary";

	/** @see #getChildSessionId() */
	public static final String CHILD_SESSION_ID__PROP = "childSessionId";

	/** @see #getDurationMs() */
	public static final String DURATION_MS__PROP = "durationMs";

	/** @see #getChildParts() */
	public static final String CHILD_PARTS__PROP = "childParts";

	/** @see #getTurnsUsed() */
	public static final String TURNS_USED__PROP = "turnsUsed";

	/** @see #getTotalUsage() */
	public static final String TOTAL_USAGE__PROP = "totalUsage";

	/** @see #getStopReason() */
	public static final String STOP_REASON__PROP = "stopReason";

	/** @see #getParentToolCallId() */
	public static final String PARENT_TOOL_CALL_ID__PROP = "parentToolCallId";

	private String _fromAgent = "";

	private String _toAgent = "";

	private String _inputSummary = "";

	private String _resultSummary = "";

	private String _childSessionId = "";

	private long _durationMs = 0L;

	private final java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedPart> _childParts = new java.util.ArrayList<>();

	private int _turnsUsed = 0;

	private com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto _totalUsage = null;

	private String _stopReason = "";

	private String _parentToolCallId = "";

	/**
	 * Creates a {@link DelegationPartDto} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto#create()
	 */
	protected DelegationPartDto() {
		super();
	}

	/**
	 * The agent delegating the task.
	 */
	public final String getFromAgent() {
		return _fromAgent;
	}

	/**
	 * @see #getFromAgent()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto setFromAgent(String value) {
		internalSetFromAgent(value);
		return this;
	}

	/** Internal setter for {@link #getFromAgent()} without chain call utility. */
	protected final void internalSetFromAgent(String value) {
		_fromAgent = value;
	}

	/**
	 * The agent receiving the task.
	 */
	public final String getToAgent() {
		return _toAgent;
	}

	/**
	 * @see #getToAgent()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto setToAgent(String value) {
		internalSetToAgent(value);
		return this;
	}

	/** Internal setter for {@link #getToAgent()} without chain call utility. */
	protected final void internalSetToAgent(String value) {
		_toAgent = value;
	}

	/**
	 * Summary of the input given to the delegate.
	 */
	public final String getInputSummary() {
		return _inputSummary;
	}

	/**
	 * @see #getInputSummary()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto setInputSummary(String value) {
		internalSetInputSummary(value);
		return this;
	}

	/** Internal setter for {@link #getInputSummary()} without chain call utility. */
	protected final void internalSetInputSummary(String value) {
		_inputSummary = value;
	}

	/**
	 * Summary of the result from the delegate.
	 */
	public final String getResultSummary() {
		return _resultSummary;
	}

	/**
	 * @see #getResultSummary()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto setResultSummary(String value) {
		internalSetResultSummary(value);
		return this;
	}

	/** Internal setter for {@link #getResultSummary()} without chain call utility. */
	protected final void internalSetResultSummary(String value) {
		_resultSummary = value;
	}

	/**
	 * Session ID of the child agent session.
	 */
	public final String getChildSessionId() {
		return _childSessionId;
	}

	/**
	 * @see #getChildSessionId()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto setChildSessionId(String value) {
		internalSetChildSessionId(value);
		return this;
	}

	/** Internal setter for {@link #getChildSessionId()} without chain call utility. */
	protected final void internalSetChildSessionId(String value) {
		_childSessionId = value;
	}

	/**
	 * Duration of the delegation in milliseconds.
	 */
	public final long getDurationMs() {
		return _durationMs;
	}

	/**
	 * @see #getDurationMs()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto setDurationMs(long value) {
		internalSetDurationMs(value);
		return this;
	}

	/** Internal setter for {@link #getDurationMs()} without chain call utility. */
	protected final void internalSetDurationMs(long value) {
		_durationMs = value;
	}

	/**
	 * Nested child parts from the sub-agent's execution.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedPart> getChildParts() {
		return _childParts;
	}

	/**
	 * @see #getChildParts()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto setChildParts(java.util.List<? extends com.top_logic.tl.ai.core.session.persist.messages.PersistedPart> value) {
		internalSetChildParts(value);
		return this;
	}

	/** Internal setter for {@link #getChildParts()} without chain call utility. */
	protected final void internalSetChildParts(java.util.List<? extends com.top_logic.tl.ai.core.session.persist.messages.PersistedPart> value) {
		if (value == null) throw new IllegalArgumentException("Property 'childParts' cannot be null.");
		_childParts.clear();
		_childParts.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getChildParts()} list.
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto addChildPart(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart value) {
		internalAddChildPart(value);
		return this;
	}

	/** Implementation of {@link #addChildPart(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart)} without chain call utility. */
	protected final void internalAddChildPart(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart value) {
		_childParts.add(value);
	}

	/**
	 * Removes a value from the {@link #getChildParts()} list.
	 */
	public final void removeChildPart(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart value) {
		_childParts.remove(value);
	}

	/**
	 * Number of LLM iterations the sub-agent executed, or -1 if unknown.
	 */
	public final int getTurnsUsed() {
		return _turnsUsed;
	}

	/**
	 * @see #getTurnsUsed()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto setTurnsUsed(int value) {
		internalSetTurnsUsed(value);
		return this;
	}

	/** Internal setter for {@link #getTurnsUsed()} without chain call utility. */
	protected final void internalSetTurnsUsed(int value) {
		_turnsUsed = value;
	}

	/**
	 * Cumulative token usage of the sub-agent.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto getTotalUsage() {
		return _totalUsage;
	}

	/**
	 * @see #getTotalUsage()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto setTotalUsage(com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto value) {
		internalSetTotalUsage(value);
		return this;
	}

	/** Internal setter for {@link #getTotalUsage()} without chain call utility. */
	protected final void internalSetTotalUsage(com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto value) {
		_totalUsage = value;
	}

	/**
	 * Checks, whether {@link #getTotalUsage()} has a value.
	 */
	public final boolean hasTotalUsage() {
		return _totalUsage != null;
	}

	/**
	 * Why the sub-agent stopped (e.g. COMPLETE, MAX_TURNS, ERROR).
	 */
	public final String getStopReason() {
		return _stopReason;
	}

	/**
	 * @see #getStopReason()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto setStopReason(String value) {
		internalSetStopReason(value);
		return this;
	}

	/** Internal setter for {@link #getStopReason()} without chain call utility. */
	protected final void internalSetStopReason(String value) {
		_stopReason = value;
	}

	/**
	 * The id of the lead's {@code delegate_to_*} call, where the chat draws the delegation's card;
	 * empty for a log written before the card was drawn.
	 */
	public final String getParentToolCallId() {
		return _parentToolCallId;
	}

	/**
	 * @see #getParentToolCallId()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto setParentToolCallId(String value) {
		internalSetParentToolCallId(value);
		return this;
	}

	/** Internal setter for {@link #getParentToolCallId()} without chain call utility. */
	protected final void internalSetParentToolCallId(String value) {
		_parentToolCallId = value;
	}

	@Override
	public String jsonType() {
		return DELEGATION_PART_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			FROM_AGENT__PROP, 
			TO_AGENT__PROP, 
			INPUT_SUMMARY__PROP, 
			RESULT_SUMMARY__PROP, 
			CHILD_SESSION_ID__PROP, 
			DURATION_MS__PROP, 
			CHILD_PARTS__PROP, 
			TURNS_USED__PROP, 
			TOTAL_USAGE__PROP, 
			STOP_REASON__PROP, 
			PARENT_TOOL_CALL_ID__PROP);
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
			case FROM_AGENT__PROP: return getFromAgent();
			case TO_AGENT__PROP: return getToAgent();
			case INPUT_SUMMARY__PROP: return getInputSummary();
			case RESULT_SUMMARY__PROP: return getResultSummary();
			case CHILD_SESSION_ID__PROP: return getChildSessionId();
			case DURATION_MS__PROP: return getDurationMs();
			case CHILD_PARTS__PROP: return getChildParts();
			case TURNS_USED__PROP: return getTurnsUsed();
			case TOTAL_USAGE__PROP: return getTotalUsage();
			case STOP_REASON__PROP: return getStopReason();
			case PARENT_TOOL_CALL_ID__PROP: return getParentToolCallId();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case FROM_AGENT__PROP: internalSetFromAgent((String) value); break;
			case TO_AGENT__PROP: internalSetToAgent((String) value); break;
			case INPUT_SUMMARY__PROP: internalSetInputSummary((String) value); break;
			case RESULT_SUMMARY__PROP: internalSetResultSummary((String) value); break;
			case CHILD_SESSION_ID__PROP: internalSetChildSessionId((String) value); break;
			case DURATION_MS__PROP: internalSetDurationMs((long) value); break;
			case CHILD_PARTS__PROP: internalSetChildParts(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart.class, value)); break;
			case TURNS_USED__PROP: internalSetTurnsUsed((int) value); break;
			case TOTAL_USAGE__PROP: internalSetTotalUsage((com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto) value); break;
			case STOP_REASON__PROP: internalSetStopReason((String) value); break;
			case PARENT_TOOL_CALL_ID__PROP: internalSetParentToolCallId((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto readDelegationPartDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto result = new com.top_logic.tl.ai.core.session.persist.messages.DelegationPartDto();
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
		out.name(FROM_AGENT__PROP);
		out.value(getFromAgent());
		out.name(TO_AGENT__PROP);
		out.value(getToAgent());
		out.name(INPUT_SUMMARY__PROP);
		out.value(getInputSummary());
		out.name(RESULT_SUMMARY__PROP);
		out.value(getResultSummary());
		out.name(CHILD_SESSION_ID__PROP);
		out.value(getChildSessionId());
		out.name(DURATION_MS__PROP);
		out.value(getDurationMs());
		out.name(CHILD_PARTS__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.session.persist.messages.PersistedPart x : getChildParts()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(TURNS_USED__PROP);
		out.value(getTurnsUsed());
		if (hasTotalUsage()) {
			out.name(TOTAL_USAGE__PROP);
			getTotalUsage().writeTo(out);
		}
		out.name(STOP_REASON__PROP);
		out.value(getStopReason());
		out.name(PARENT_TOOL_CALL_ID__PROP);
		out.value(getParentToolCallId());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case FROM_AGENT__PROP: setFromAgent(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TO_AGENT__PROP: setToAgent(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case INPUT_SUMMARY__PROP: setInputSummary(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RESULT_SUMMARY__PROP: setResultSummary(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CHILD_SESSION_ID__PROP: setChildSessionId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DURATION_MS__PROP: setDurationMs(in.nextLong()); break;
			case CHILD_PARTS__PROP: {
				java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedPart> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart.readPersistedPart(in));
				}
				in.endArray();
				setChildParts(newValue);
			}
			break;
			case TURNS_USED__PROP: setTurnsUsed(in.nextInt()); break;
			case TOTAL_USAGE__PROP: setTotalUsage(com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto.readTokenUsageDto(in)); break;
			case STOP_REASON__PROP: setStopReason(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PARENT_TOOL_CALL_ID__PROP: setParentToolCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
