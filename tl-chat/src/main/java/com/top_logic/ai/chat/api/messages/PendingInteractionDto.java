package com.top_logic.ai.chat.api.messages;

/**
 * A pending human-in-the-loop request (approval/question/choice) awaiting the current user,
 * with the chat session it belongs to so the client can link to it.
 */
public class PendingInteractionDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.PendingInteractionDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.PendingInteractionDto create() {
		return new com.top_logic.ai.chat.api.messages.PendingInteractionDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.PendingInteractionDto} type in JSON format. */
	public static final String PENDING_INTERACTION_DTO__TYPE = "PendingInteractionDto";

	/** @see #getRequestId() */
	public static final String REQUEST_ID__PROP = "requestId";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getHeader() */
	public static final String HEADER__PROP = "header";

	/** @see #getPrompt() */
	public static final String PROMPT__PROP = "prompt";

	/** @see #getPayload() */
	public static final String PAYLOAD__PROP = "payload";

	/** @see #getSessionId() */
	public static final String SESSION_ID__PROP = "sessionId";

	/** @see #getSessionTitle() */
	public static final String SESSION_TITLE__PROP = "sessionTitle";

	/** @see #getCreatedAt() */
	public static final String CREATED_AT__PROP = "createdAt";

	/** @see #getRemainingMs() */
	public static final String REMAINING_MS__PROP = "remainingMs";

	/** @see #getToolMode() */
	public static final String TOOL_MODE__PROP = "toolMode";

	/** @see #isMultiSelect() */
	public static final String MULTI_SELECT__PROP = "multiSelect";

	/** @see #getOptions() */
	public static final String OPTIONS__PROP = "options";

	private String _requestId = "";

	private String _type = "";

	private String _header = "";

	private String _prompt = "";

	private String _payload = "";

	private String _sessionId = "";

	private String _sessionTitle = "";

	private String _createdAt = "";

	private long _remainingMs = 0L;

	private String _toolMode = "";

	private boolean _multiSelect = false;

	private final java.util.List<com.top_logic.ai.chat.api.messages.InteractionOptionDto> _options = new java.util.ArrayList<>();

	/**
	 * Creates a {@link PendingInteractionDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.PendingInteractionDto#create()
	 */
	protected PendingInteractionDto() {
		super();
	}

	/**
	 * Correlation ID of the underlying <code>InteractionRequest</code> (used for /chat/respond).
	 */
	public final String getRequestId() {
		return _requestId;
	}

	/**
	 * @see #getRequestId()
	 */
	public com.top_logic.ai.chat.api.messages.PendingInteractionDto setRequestId(String value) {
		internalSetRequestId(value);
		return this;
	}

	/** Internal setter for {@link #getRequestId()} without chain call utility. */
	protected final void internalSetRequestId(String value) {
		_requestId = value;
	}

	/**
	 * Request kind: "approval", "question", or "choice".
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.ai.chat.api.messages.PendingInteractionDto setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * Short label (e.g. "Approval").
	 */
	public final String getHeader() {
		return _header;
	}

	/**
	 * @see #getHeader()
	 */
	public com.top_logic.ai.chat.api.messages.PendingInteractionDto setHeader(String value) {
		internalSetHeader(value);
		return this;
	}

	/** Internal setter for {@link #getHeader()} without chain call utility. */
	protected final void internalSetHeader(String value) {
		_header = value;
	}

	/**
	 * The prompt presented to the responder (raw markdown). For approvals this is the display
	 * reason.
	 */
	public final String getPrompt() {
		return _prompt;
	}

	/**
	 * @see #getPrompt()
	 */
	public com.top_logic.ai.chat.api.messages.PendingInteractionDto setPrompt(String value) {
		internalSetPrompt(value);
		return this;
	}

	/** Internal setter for {@link #getPrompt()} without chain call utility. */
	protected final void internalSetPrompt(String value) {
		_prompt = value;
	}

	/**
	 * Structured request data. For approvals this is the tool call's argument JSON, so the client
	 * can rebuild the approval prompt when the user re-opens the chat.
	 */
	public final String getPayload() {
		return _payload;
	}

	/**
	 * @see #getPayload()
	 */
	public com.top_logic.ai.chat.api.messages.PendingInteractionDto setPayload(String value) {
		internalSetPayload(value);
		return this;
	}

	/** Internal setter for {@link #getPayload()} without chain call utility. */
	protected final void internalSetPayload(String value) {
		_payload = value;
	}

	/**
	 * ID of the chat session that raised this request.
	 */
	public final String getSessionId() {
		return _sessionId;
	}

	/**
	 * @see #getSessionId()
	 */
	public com.top_logic.ai.chat.api.messages.PendingInteractionDto setSessionId(String value) {
		internalSetSessionId(value);
		return this;
	}

	/** Internal setter for {@link #getSessionId()} without chain call utility. */
	protected final void internalSetSessionId(String value) {
		_sessionId = value;
	}

	/**
	 * Title of the chat session that raised this request.
	 */
	public final String getSessionTitle() {
		return _sessionTitle;
	}

	/**
	 * @see #getSessionTitle()
	 */
	public com.top_logic.ai.chat.api.messages.PendingInteractionDto setSessionTitle(String value) {
		internalSetSessionTitle(value);
		return this;
	}

	/** Internal setter for {@link #getSessionTitle()} without chain call utility. */
	protected final void internalSetSessionTitle(String value) {
		_sessionTitle = value;
	}

	/**
	 * ISO-8601 timestamp of when the request was created.
	 */
	public final String getCreatedAt() {
		return _createdAt;
	}

	/**
	 * @see #getCreatedAt()
	 */
	public com.top_logic.ai.chat.api.messages.PendingInteractionDto setCreatedAt(String value) {
		internalSetCreatedAt(value);
		return this;
	}

	/** Internal setter for {@link #getCreatedAt()} without chain call utility. */
	protected final void internalSetCreatedAt(String value) {
		_createdAt = value;
	}

	/**
	 * Milliseconds left for the user to respond, counted from the moment this response was
	 * written. Zero when the request's deadline has already passed, so a client can tell a
	 * request it can still answer from one that is only waiting to be cleaned up.
	 */
	public final long getRemainingMs() {
		return _remainingMs;
	}

	/**
	 * @see #getRemainingMs()
	 */
	public com.top_logic.ai.chat.api.messages.PendingInteractionDto setRemainingMs(long value) {
		internalSetRemainingMs(value);
		return this;
	}

	/** Internal setter for {@link #getRemainingMs()} without chain call utility. */
	protected final void internalSetRemainingMs(long value) {
		_remainingMs = value;
	}

	/**
	 * For an approval, the tool's mode ("USER_SCOPED", "DATA_WRITE", "STRUCTURAL", "EXTERNAL"), so a
	 * client can phrase the question in the reader's language rather than repeat the server's
	 * English reason. Empty when the run that asked is no longer waiting, or for a question.
	 */
	public final String getToolMode() {
		return _toolMode;
	}

	/**
	 * @see #getToolMode()
	 */
	public com.top_logic.ai.chat.api.messages.PendingInteractionDto setToolMode(String value) {
		internalSetToolMode(value);
		return this;
	}

	/** Internal setter for {@link #getToolMode()} without chain call utility. */
	protected final void internalSetToolMode(String value) {
		_toolMode = value;
	}

	/**
	 * For a question of type "choice", whether it takes more than one answer, so a rebuilt card
	 * offers the checkboxes the live one offered rather than silently asking a different question.
	 * Read off the run that is still waiting; never <code>true</code> for an approval.
	 */
	public final boolean isMultiSelect() {
		return _multiSelect;
	}

	/**
	 * @see #isMultiSelect()
	 */
	public com.top_logic.ai.chat.api.messages.PendingInteractionDto setMultiSelect(boolean value) {
		internalSetMultiSelect(value);
		return this;
	}

	/** Internal setter for {@link #isMultiSelect()} without chain call utility. */
	protected final void internalSetMultiSelect(boolean value) {
		_multiSelect = value;
	}

	/**
	 * For a question of type "choice", the options to offer, in the order the agent gave them.
	 *
	 * <p>
	 * The same options {@link #getPayload()} holds as the raw JSON the model wrote, parsed here so that
	 * a client rebuilding the card does not have to know the two element shapes that JSON may
	 * have. Empty for an approval and for a free-text question.
	 * </p>
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.InteractionOptionDto> getOptions() {
		return _options;
	}

	/**
	 * @see #getOptions()
	 */
	public com.top_logic.ai.chat.api.messages.PendingInteractionDto setOptions(java.util.List<? extends com.top_logic.ai.chat.api.messages.InteractionOptionDto> value) {
		internalSetOptions(value);
		return this;
	}

	/** Internal setter for {@link #getOptions()} without chain call utility. */
	protected final void internalSetOptions(java.util.List<? extends com.top_logic.ai.chat.api.messages.InteractionOptionDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'options' cannot be null.");
		_options.clear();
		_options.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getOptions()} list.
	 */
	public com.top_logic.ai.chat.api.messages.PendingInteractionDto addOption(com.top_logic.ai.chat.api.messages.InteractionOptionDto value) {
		internalAddOption(value);
		return this;
	}

	/** Implementation of {@link #addOption(com.top_logic.ai.chat.api.messages.InteractionOptionDto)} without chain call utility. */
	protected final void internalAddOption(com.top_logic.ai.chat.api.messages.InteractionOptionDto value) {
		_options.add(value);
	}

	/**
	 * Removes a value from the {@link #getOptions()} list.
	 */
	public final void removeOption(com.top_logic.ai.chat.api.messages.InteractionOptionDto value) {
		_options.remove(value);
	}

	@Override
	public String jsonType() {
		return PENDING_INTERACTION_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			REQUEST_ID__PROP, 
			TYPE__PROP, 
			HEADER__PROP, 
			PROMPT__PROP, 
			PAYLOAD__PROP, 
			SESSION_ID__PROP, 
			SESSION_TITLE__PROP, 
			CREATED_AT__PROP, 
			REMAINING_MS__PROP, 
			TOOL_MODE__PROP, 
			MULTI_SELECT__PROP, 
			OPTIONS__PROP);
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
			case REQUEST_ID__PROP: return getRequestId();
			case TYPE__PROP: return getType();
			case HEADER__PROP: return getHeader();
			case PROMPT__PROP: return getPrompt();
			case PAYLOAD__PROP: return getPayload();
			case SESSION_ID__PROP: return getSessionId();
			case SESSION_TITLE__PROP: return getSessionTitle();
			case CREATED_AT__PROP: return getCreatedAt();
			case REMAINING_MS__PROP: return getRemainingMs();
			case TOOL_MODE__PROP: return getToolMode();
			case MULTI_SELECT__PROP: return isMultiSelect();
			case OPTIONS__PROP: return getOptions();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case REQUEST_ID__PROP: internalSetRequestId((String) value); break;
			case TYPE__PROP: internalSetType((String) value); break;
			case HEADER__PROP: internalSetHeader((String) value); break;
			case PROMPT__PROP: internalSetPrompt((String) value); break;
			case PAYLOAD__PROP: internalSetPayload((String) value); break;
			case SESSION_ID__PROP: internalSetSessionId((String) value); break;
			case SESSION_TITLE__PROP: internalSetSessionTitle((String) value); break;
			case CREATED_AT__PROP: internalSetCreatedAt((String) value); break;
			case REMAINING_MS__PROP: internalSetRemainingMs((long) value); break;
			case TOOL_MODE__PROP: internalSetToolMode((String) value); break;
			case MULTI_SELECT__PROP: internalSetMultiSelect((boolean) value); break;
			case OPTIONS__PROP: internalSetOptions(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.InteractionOptionDto.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.PendingInteractionDto readPendingInteractionDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.PendingInteractionDto result = new com.top_logic.ai.chat.api.messages.PendingInteractionDto();
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
		out.name(REQUEST_ID__PROP);
		out.value(getRequestId());
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(HEADER__PROP);
		out.value(getHeader());
		out.name(PROMPT__PROP);
		out.value(getPrompt());
		out.name(PAYLOAD__PROP);
		out.value(getPayload());
		out.name(SESSION_ID__PROP);
		out.value(getSessionId());
		out.name(SESSION_TITLE__PROP);
		out.value(getSessionTitle());
		out.name(CREATED_AT__PROP);
		out.value(getCreatedAt());
		out.name(REMAINING_MS__PROP);
		out.value(getRemainingMs());
		out.name(TOOL_MODE__PROP);
		out.value(getToolMode());
		out.name(MULTI_SELECT__PROP);
		out.value(isMultiSelect());
		out.name(OPTIONS__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.InteractionOptionDto x : getOptions()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case REQUEST_ID__PROP: setRequestId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HEADER__PROP: setHeader(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PROMPT__PROP: setPrompt(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PAYLOAD__PROP: setPayload(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SESSION_ID__PROP: setSessionId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SESSION_TITLE__PROP: setSessionTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CREATED_AT__PROP: setCreatedAt(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REMAINING_MS__PROP: setRemainingMs(in.nextLong()); break;
			case TOOL_MODE__PROP: setToolMode(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MULTI_SELECT__PROP: setMultiSelect(in.nextBoolean()); break;
			case OPTIONS__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.InteractionOptionDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.InteractionOptionDto.readInteractionOptionDto(in));
				}
				in.endArray();
				setOptions(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
