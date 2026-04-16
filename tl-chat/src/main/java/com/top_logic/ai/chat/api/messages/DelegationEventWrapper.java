package com.top_logic.ai.chat.api.messages;

/**
 * SSE wrapper for child-agent events forwarded during delegation.
 *
 * <p>
 * Carries the parent delegation's tool call ID so the browser can route child events
 * (tool calls, text deltas, token usage) to the correct delegation container.
 * </p>
 */
public class DelegationEventWrapper extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.DelegationEventWrapper} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.DelegationEventWrapper create() {
		return new com.top_logic.ai.chat.api.messages.DelegationEventWrapper();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.DelegationEventWrapper} type in JSON format. */
	public static final String DELEGATION_EVENT_WRAPPER__TYPE = "DelegationEventWrapper";

	/** @see #getDelegationToolCallId() */
	public static final String DELEGATION_TOOL_CALL_ID__PROP = "delegationToolCallId";

	/** @see #getEventType() */
	public static final String EVENT_TYPE__PROP = "eventType";

	/** @see #getEventData() */
	public static final String EVENT_DATA__PROP = "eventData";

	private String _delegationToolCallId = "";

	private String _eventType = "";

	private String _eventData = "";

	/**
	 * Creates a {@link DelegationEventWrapper} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.DelegationEventWrapper#create()
	 */
	protected DelegationEventWrapper() {
		super();
	}

	/**
	 * The parent agent's tool call ID for the delegate_to_* tool.
	 */
	public final String getDelegationToolCallId() {
		return _delegationToolCallId;
	}

	/**
	 * @see #getDelegationToolCallId()
	 */
	public com.top_logic.ai.chat.api.messages.DelegationEventWrapper setDelegationToolCallId(String value) {
		internalSetDelegationToolCallId(value);
		return this;
	}

	/** Internal setter for {@link #getDelegationToolCallId()} without chain call utility. */
	protected final void internalSetDelegationToolCallId(String value) {
		_delegationToolCallId = value;
	}

	/**
	 * The inner SSE event type (e.g. "tool_started", "text_delta", "run_completed").
	 */
	public final String getEventType() {
		return _eventType;
	}

	/**
	 * @see #getEventType()
	 */
	public com.top_logic.ai.chat.api.messages.DelegationEventWrapper setEventType(String value) {
		internalSetEventType(value);
		return this;
	}

	/** Internal setter for {@link #getEventType()} without chain call utility. */
	protected final void internalSetEventType(String value) {
		_eventType = value;
	}

	/**
	 * The inner event data as JSON string.
	 */
	public final String getEventData() {
		return _eventData;
	}

	/**
	 * @see #getEventData()
	 */
	public com.top_logic.ai.chat.api.messages.DelegationEventWrapper setEventData(String value) {
		internalSetEventData(value);
		return this;
	}

	/** Internal setter for {@link #getEventData()} without chain call utility. */
	protected final void internalSetEventData(String value) {
		_eventData = value;
	}

	@Override
	public String jsonType() {
		return DELEGATION_EVENT_WRAPPER__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			DELEGATION_TOOL_CALL_ID__PROP, 
			EVENT_TYPE__PROP, 
			EVENT_DATA__PROP);
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
			case DELEGATION_TOOL_CALL_ID__PROP: return getDelegationToolCallId();
			case EVENT_TYPE__PROP: return getEventType();
			case EVENT_DATA__PROP: return getEventData();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case DELEGATION_TOOL_CALL_ID__PROP: internalSetDelegationToolCallId((String) value); break;
			case EVENT_TYPE__PROP: internalSetEventType((String) value); break;
			case EVENT_DATA__PROP: internalSetEventData((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.DelegationEventWrapper readDelegationEventWrapper(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.DelegationEventWrapper result = new com.top_logic.ai.chat.api.messages.DelegationEventWrapper();
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
		out.name(DELEGATION_TOOL_CALL_ID__PROP);
		out.value(getDelegationToolCallId());
		out.name(EVENT_TYPE__PROP);
		out.value(getEventType());
		out.name(EVENT_DATA__PROP);
		out.value(getEventData());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case DELEGATION_TOOL_CALL_ID__PROP: setDelegationToolCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case EVENT_TYPE__PROP: setEventType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case EVENT_DATA__PROP: setEventData(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
