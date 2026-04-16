package com.top_logic.ai.chat.api.messages;

/**
 * Agent delegation part.
 */
public class DelegationPartDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.DelegationPartDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.DelegationPartDto create() {
		return new com.top_logic.ai.chat.api.messages.DelegationPartDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.DelegationPartDto} type in JSON format. */
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

	private String _fromAgent = "";

	private String _toAgent = "";

	private String _inputSummary = "";

	private String _resultSummary = "";

	private String _childSessionId = "";

	private long _durationMs = 0L;

	/**
	 * Creates a {@link DelegationPartDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.DelegationPartDto#create()
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
	public com.top_logic.ai.chat.api.messages.DelegationPartDto setFromAgent(String value) {
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
	public com.top_logic.ai.chat.api.messages.DelegationPartDto setToAgent(String value) {
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
	public com.top_logic.ai.chat.api.messages.DelegationPartDto setInputSummary(String value) {
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
	public com.top_logic.ai.chat.api.messages.DelegationPartDto setResultSummary(String value) {
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
	public com.top_logic.ai.chat.api.messages.DelegationPartDto setChildSessionId(String value) {
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
	public com.top_logic.ai.chat.api.messages.DelegationPartDto setDurationMs(long value) {
		internalSetDurationMs(value);
		return this;
	}

	/** Internal setter for {@link #getDurationMs()} without chain call utility. */
	protected final void internalSetDurationMs(long value) {
		_durationMs = value;
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
			case FROM_AGENT__PROP: return getFromAgent();
			case TO_AGENT__PROP: return getToAgent();
			case INPUT_SUMMARY__PROP: return getInputSummary();
			case RESULT_SUMMARY__PROP: return getResultSummary();
			case CHILD_SESSION_ID__PROP: return getChildSessionId();
			case DURATION_MS__PROP: return getDurationMs();
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
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.DelegationPartDto readDelegationPartDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.DelegationPartDto result = new com.top_logic.ai.chat.api.messages.DelegationPartDto();
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
			default: super.readField(in, field);
		}
	}

}
