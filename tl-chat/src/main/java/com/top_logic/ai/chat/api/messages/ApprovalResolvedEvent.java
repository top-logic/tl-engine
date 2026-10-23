package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for approval resolved.
 */
public class ApprovalResolvedEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ApprovalResolvedEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ApprovalResolvedEvent create() {
		return new com.top_logic.ai.chat.api.messages.ApprovalResolvedEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ApprovalResolvedEvent} type in JSON format. */
	public static final String APPROVAL_RESOLVED_EVENT__TYPE = "ApprovalResolvedEvent";

	/** @see #getToolCallId() */
	public static final String TOOL_CALL_ID__PROP = "toolCallId";

	/** @see #getApprovalRequestId() */
	public static final String APPROVAL_REQUEST_ID__PROP = "approvalRequestId";

	/** @see #getOutcome() */
	public static final String OUTCOME__PROP = "outcome";

	private String _toolCallId = "";

	private String _approvalRequestId = "";

	private String _outcome = "";

	/**
	 * Creates a {@link ApprovalResolvedEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ApprovalResolvedEvent#create()
	 */
	protected ApprovalResolvedEvent() {
		super();
	}

	/**
	 * Unique tool call ID.
	 */
	public final String getToolCallId() {
		return _toolCallId;
	}

	/**
	 * @see #getToolCallId()
	 */
	public com.top_logic.ai.chat.api.messages.ApprovalResolvedEvent setToolCallId(String value) {
		internalSetToolCallId(value);
		return this;
	}

	/** Internal setter for {@link #getToolCallId()} without chain call utility. */
	protected final void internalSetToolCallId(String value) {
		_toolCallId = value;
	}

	/**
	 * Approval request ID.
	 */
	public final String getApprovalRequestId() {
		return _approvalRequestId;
	}

	/**
	 * @see #getApprovalRequestId()
	 */
	public com.top_logic.ai.chat.api.messages.ApprovalResolvedEvent setApprovalRequestId(String value) {
		internalSetApprovalRequestId(value);
		return this;
	}

	/** Internal setter for {@link #getApprovalRequestId()} without chain call utility. */
	protected final void internalSetApprovalRequestId(String value) {
		_approvalRequestId = value;
	}

	/**
	 * Outcome: APPROVED, APPROVED_FOR_SESSION, REJECTED, TIMED_OUT.
	 */
	public final String getOutcome() {
		return _outcome;
	}

	/**
	 * @see #getOutcome()
	 */
	public com.top_logic.ai.chat.api.messages.ApprovalResolvedEvent setOutcome(String value) {
		internalSetOutcome(value);
		return this;
	}

	/** Internal setter for {@link #getOutcome()} without chain call utility. */
	protected final void internalSetOutcome(String value) {
		_outcome = value;
	}

	@Override
	public String jsonType() {
		return APPROVAL_RESOLVED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TOOL_CALL_ID__PROP, 
			APPROVAL_REQUEST_ID__PROP, 
			OUTCOME__PROP);
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
			case APPROVAL_REQUEST_ID__PROP: return getApprovalRequestId();
			case OUTCOME__PROP: return getOutcome();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TOOL_CALL_ID__PROP: internalSetToolCallId((String) value); break;
			case APPROVAL_REQUEST_ID__PROP: internalSetApprovalRequestId((String) value); break;
			case OUTCOME__PROP: internalSetOutcome((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ApprovalResolvedEvent readApprovalResolvedEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ApprovalResolvedEvent result = new com.top_logic.ai.chat.api.messages.ApprovalResolvedEvent();
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
		out.name(APPROVAL_REQUEST_ID__PROP);
		out.value(getApprovalRequestId());
		out.name(OUTCOME__PROP);
		out.value(getOutcome());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TOOL_CALL_ID__PROP: setToolCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case APPROVAL_REQUEST_ID__PROP: setApprovalRequestId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case OUTCOME__PROP: setOutcome(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
