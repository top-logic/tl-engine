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

	private String _requestId = "";

	private String _type = "";

	private String _header = "";

	private String _prompt = "";

	private String _payload = "";

	private String _sessionId = "";

	private String _sessionTitle = "";

	private String _createdAt = "";

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
			CREATED_AT__PROP);
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
			default: super.readField(in, field);
		}
	}

}
