package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for approval required.
 */
public class ApprovalRequiredEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent create() {
		return new com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent} type in JSON format. */
	public static final String APPROVAL_REQUIRED_EVENT__TYPE = "ApprovalRequiredEvent";

	/** @see #getToolCallId() */
	public static final String TOOL_CALL_ID__PROP = "toolCallId";

	/** @see #getApprovalRequestId() */
	public static final String APPROVAL_REQUEST_ID__PROP = "approvalRequestId";

	/** @see #getToolName() */
	public static final String TOOL_NAME__PROP = "toolName";

	/** @see #getArgsJson() */
	public static final String ARGS_JSON__PROP = "argsJson";

	/** @see #getDisplayReason() */
	public static final String DISPLAY_REASON__PROP = "displayReason";

	/** @see #getRemainingMs() */
	public static final String REMAINING_MS__PROP = "remainingMs";

	/** @see #getToolMode() */
	public static final String TOOL_MODE__PROP = "toolMode";

	private String _toolCallId = "";

	private String _approvalRequestId = "";

	private String _toolName = "";

	private String _argsJson = "";

	private String _displayReason = "";

	private long _remainingMs = 0L;

	private String _toolMode = "";

	/**
	 * Creates a {@link ApprovalRequiredEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent#create()
	 */
	protected ApprovalRequiredEvent() {
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
	public com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent setToolCallId(String value) {
		internalSetToolCallId(value);
		return this;
	}

	/** Internal setter for {@link #getToolCallId()} without chain call utility. */
	protected final void internalSetToolCallId(String value) {
		_toolCallId = value;
	}

	/**
	 * Approval request ID for responding.
	 */
	public final String getApprovalRequestId() {
		return _approvalRequestId;
	}

	/**
	 * @see #getApprovalRequestId()
	 */
	public com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent setApprovalRequestId(String value) {
		internalSetApprovalRequestId(value);
		return this;
	}

	/** Internal setter for {@link #getApprovalRequestId()} without chain call utility. */
	protected final void internalSetApprovalRequestId(String value) {
		_approvalRequestId = value;
	}

	/**
	 * Tool name needing approval.
	 */
	public final String getToolName() {
		return _toolName;
	}

	/**
	 * @see #getToolName()
	 */
	public com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent setToolName(String value) {
		internalSetToolName(value);
		return this;
	}

	/** Internal setter for {@link #getToolName()} without chain call utility. */
	protected final void internalSetToolName(String value) {
		_toolName = value;
	}

	/**
	 * Arguments as JSON string.
	 */
	public final String getArgsJson() {
		return _argsJson;
	}

	/**
	 * @see #getArgsJson()
	 */
	public com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent setArgsJson(String value) {
		internalSetArgsJson(value);
		return this;
	}

	/** Internal setter for {@link #getArgsJson()} without chain call utility. */
	protected final void internalSetArgsJson(String value) {
		_argsJson = value;
	}

	/**
	 * Human-readable reason for approval.
	 */
	public final String getDisplayReason() {
		return _displayReason;
	}

	/**
	 * @see #getDisplayReason()
	 */
	public com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent setDisplayReason(String value) {
		internalSetDisplayReason(value);
		return this;
	}

	/** Internal setter for {@link #getDisplayReason()} without chain call utility. */
	protected final void internalSetDisplayReason(String value) {
		_displayReason = value;
	}

	/**
	 * Milliseconds left for the user to decide, counted from the moment this event was sent.
	 * Zero when the request has no time left; the client shows the remaining time so the user
	 * knows there is a clock.
	 */
	public final long getRemainingMs() {
		return _remainingMs;
	}

	/**
	 * @see #getRemainingMs()
	 */
	public com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent setRemainingMs(long value) {
		internalSetRemainingMs(value);
		return this;
	}

	/** Internal setter for {@link #getRemainingMs()} without chain call utility. */
	protected final void internalSetRemainingMs(long value) {
		_remainingMs = value;
	}

	/**
	 * What kind of change the tool makes: READ, USER_SCOPED, DATA_WRITE, STRUCTURAL or EXTERNAL.
	 *
	 * <p>
	 * Why approval is being asked, in a form a UI can phrase in the user's own language;
	 * {@link #getDisplayReason()} is the same thing written once, in English. Empty for an event from
	 * before the mode was carried.
	 * </p>
	 */
	public final String getToolMode() {
		return _toolMode;
	}

	/**
	 * @see #getToolMode()
	 */
	public com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent setToolMode(String value) {
		internalSetToolMode(value);
		return this;
	}

	/** Internal setter for {@link #getToolMode()} without chain call utility. */
	protected final void internalSetToolMode(String value) {
		_toolMode = value;
	}

	@Override
	public String jsonType() {
		return APPROVAL_REQUIRED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TOOL_CALL_ID__PROP, 
			APPROVAL_REQUEST_ID__PROP, 
			TOOL_NAME__PROP, 
			ARGS_JSON__PROP, 
			DISPLAY_REASON__PROP, 
			REMAINING_MS__PROP, 
			TOOL_MODE__PROP);
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
			case TOOL_NAME__PROP: return getToolName();
			case ARGS_JSON__PROP: return getArgsJson();
			case DISPLAY_REASON__PROP: return getDisplayReason();
			case REMAINING_MS__PROP: return getRemainingMs();
			case TOOL_MODE__PROP: return getToolMode();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TOOL_CALL_ID__PROP: internalSetToolCallId((String) value); break;
			case APPROVAL_REQUEST_ID__PROP: internalSetApprovalRequestId((String) value); break;
			case TOOL_NAME__PROP: internalSetToolName((String) value); break;
			case ARGS_JSON__PROP: internalSetArgsJson((String) value); break;
			case DISPLAY_REASON__PROP: internalSetDisplayReason((String) value); break;
			case REMAINING_MS__PROP: internalSetRemainingMs((long) value); break;
			case TOOL_MODE__PROP: internalSetToolMode((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent readApprovalRequiredEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent result = new com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent();
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
		out.name(TOOL_NAME__PROP);
		out.value(getToolName());
		out.name(ARGS_JSON__PROP);
		out.value(getArgsJson());
		out.name(DISPLAY_REASON__PROP);
		out.value(getDisplayReason());
		out.name(REMAINING_MS__PROP);
		out.value(getRemainingMs());
		out.name(TOOL_MODE__PROP);
		out.value(getToolMode());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TOOL_CALL_ID__PROP: setToolCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case APPROVAL_REQUEST_ID__PROP: setApprovalRequestId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOOL_NAME__PROP: setToolName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ARGS_JSON__PROP: setArgsJson(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DISPLAY_REASON__PROP: setDisplayReason(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REMAINING_MS__PROP: setRemainingMs(in.nextLong()); break;
			case TOOL_MODE__PROP: setToolMode(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
