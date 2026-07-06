package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * Approval request part for tool calls requiring user confirmation.
 */
public class ApprovalPartDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto} type in JSON format. */
	public static final String APPROVAL_PART_DTO__TYPE = "ApprovalPartDto";

	/** @see #getToolCallId() */
	public static final String TOOL_CALL_ID__PROP = "toolCallId";

	/** @see #getToolName() */
	public static final String TOOL_NAME__PROP = "toolName";

	/** @see #getArgsJson() */
	public static final String ARGS_JSON__PROP = "argsJson";

	/** @see #getDisplayReason() */
	public static final String DISPLAY_REASON__PROP = "displayReason";

	/** @see #getDecision() */
	public static final String DECISION__PROP = "decision";

	/** @see #getUserComment() */
	public static final String USER_COMMENT__PROP = "userComment";

	/** @see #getResolvedAt() */
	public static final String RESOLVED_AT__PROP = "resolvedAt";

	private String _toolCallId = "";

	private String _toolName = "";

	private String _argsJson = "";

	private String _displayReason = "";

	private String _decision = "";

	private String _userComment = "";

	private long _resolvedAt = 0L;

	/**
	 * Creates a {@link ApprovalPartDto} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto#create()
	 */
	protected ApprovalPartDto() {
		super();
	}

	/**
	 * ID of the tool call requiring approval.
	 */
	public final String getToolCallId() {
		return _toolCallId;
	}

	/**
	 * @see #getToolCallId()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto setToolCallId(String value) {
		internalSetToolCallId(value);
		return this;
	}

	/** Internal setter for {@link #getToolCallId()} without chain call utility. */
	protected final void internalSetToolCallId(String value) {
		_toolCallId = value;
	}

	/**
	 * Name of the tool requiring approval.
	 */
	public final String getToolName() {
		return _toolName;
	}

	/**
	 * @see #getToolName()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto setToolName(String value) {
		internalSetToolName(value);
		return this;
	}

	/** Internal setter for {@link #getToolName()} without chain call utility. */
	protected final void internalSetToolName(String value) {
		_toolName = value;
	}

	/**
	 * JSON-encoded arguments for the tool call.
	 */
	public final String getArgsJson() {
		return _argsJson;
	}

	/**
	 * @see #getArgsJson()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto setArgsJson(String value) {
		internalSetArgsJson(value);
		return this;
	}

	/** Internal setter for {@link #getArgsJson()} without chain call utility. */
	protected final void internalSetArgsJson(String value) {
		_argsJson = value;
	}

	/**
	 * Reason displayed to the user for the approval request.
	 */
	public final String getDisplayReason() {
		return _displayReason;
	}

	/**
	 * @see #getDisplayReason()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto setDisplayReason(String value) {
		internalSetDisplayReason(value);
		return this;
	}

	/** Internal setter for {@link #getDisplayReason()} without chain call utility. */
	protected final void internalSetDisplayReason(String value) {
		_displayReason = value;
	}

	/**
	 * The user's decision.
	 */
	public final String getDecision() {
		return _decision;
	}

	/**
	 * @see #getDecision()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto setDecision(String value) {
		internalSetDecision(value);
		return this;
	}

	/** Internal setter for {@link #getDecision()} without chain call utility. */
	protected final void internalSetDecision(String value) {
		_decision = value;
	}

	/**
	 * Optional comment from the user.
	 */
	public final String getUserComment() {
		return _userComment;
	}

	/**
	 * @see #getUserComment()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto setUserComment(String value) {
		internalSetUserComment(value);
		return this;
	}

	/** Internal setter for {@link #getUserComment()} without chain call utility. */
	protected final void internalSetUserComment(String value) {
		_userComment = value;
	}

	/**
	 * Timestamp when the approval was resolved.
	 */
	public final long getResolvedAt() {
		return _resolvedAt;
	}

	/**
	 * @see #getResolvedAt()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto setResolvedAt(long value) {
		internalSetResolvedAt(value);
		return this;
	}

	/** Internal setter for {@link #getResolvedAt()} without chain call utility. */
	protected final void internalSetResolvedAt(long value) {
		_resolvedAt = value;
	}

	@Override
	public String jsonType() {
		return APPROVAL_PART_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TOOL_CALL_ID__PROP, 
			TOOL_NAME__PROP, 
			ARGS_JSON__PROP, 
			DISPLAY_REASON__PROP, 
			DECISION__PROP, 
			USER_COMMENT__PROP, 
			RESOLVED_AT__PROP);
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
			case ARGS_JSON__PROP: return getArgsJson();
			case DISPLAY_REASON__PROP: return getDisplayReason();
			case DECISION__PROP: return getDecision();
			case USER_COMMENT__PROP: return getUserComment();
			case RESOLVED_AT__PROP: return getResolvedAt();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TOOL_CALL_ID__PROP: internalSetToolCallId((String) value); break;
			case TOOL_NAME__PROP: internalSetToolName((String) value); break;
			case ARGS_JSON__PROP: internalSetArgsJson((String) value); break;
			case DISPLAY_REASON__PROP: internalSetDisplayReason((String) value); break;
			case DECISION__PROP: internalSetDecision((String) value); break;
			case USER_COMMENT__PROP: internalSetUserComment((String) value); break;
			case RESOLVED_AT__PROP: internalSetResolvedAt((long) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto readApprovalPartDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto result = new com.top_logic.tl.ai.core.session.persist.messages.ApprovalPartDto();
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
		out.name(ARGS_JSON__PROP);
		out.value(getArgsJson());
		out.name(DISPLAY_REASON__PROP);
		out.value(getDisplayReason());
		out.name(DECISION__PROP);
		out.value(getDecision());
		out.name(USER_COMMENT__PROP);
		out.value(getUserComment());
		out.name(RESOLVED_AT__PROP);
		out.value(getResolvedAt());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TOOL_CALL_ID__PROP: setToolCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOOL_NAME__PROP: setToolName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ARGS_JSON__PROP: setArgsJson(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DISPLAY_REASON__PROP: setDisplayReason(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DECISION__PROP: setDecision(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case USER_COMMENT__PROP: setUserComment(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RESOLVED_AT__PROP: setResolvedAt(in.nextLong()); break;
			default: super.readField(in, field);
		}
	}

}
