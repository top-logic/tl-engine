package com.top_logic.ai.chat.api.messages;

/**
 * Response body for GET /chat/sessions/{id} (full session load).
 */
public class ChatSessionDetailResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse create() {
		return new com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse} type in JSON format. */
	public static final String CHAT_SESSION_DETAIL_RESPONSE__TYPE = "ChatSessionDetailResponse";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getTitle() */
	public static final String TITLE__PROP = "title";

	/** @see #getMessagesJson() */
	public static final String MESSAGES_JSON__PROP = "messagesJson";

	/** @see #getTurnsJson() */
	public static final String TURNS_JSON__PROP = "turnsJson";

	/** @see #getLastPromptUsage() */
	public static final String LAST_PROMPT_USAGE__PROP = "lastPromptUsage";

	/** @see #getContextWindow() */
	public static final String CONTEXT_WINDOW__PROP = "contextWindow";

	/** @see #getLastModel() */
	public static final String LAST_MODEL__PROP = "lastModel";

	private String _id = "";

	private String _title = "";

	private String _messagesJson = "";

	private String _turnsJson = "";

	private com.top_logic.ai.chat.api.messages.TokenUsageDto _lastPromptUsage = null;

	private int _contextWindow = 0;

	private String _lastModel = "";

	/**
	 * Creates a {@link ChatSessionDetailResponse} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse#create()
	 */
	protected ChatSessionDetailResponse() {
		super();
	}

	/**
	 * Session ID.
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * Session title.
	 */
	public final String getTitle() {
		return _title;
	}

	/**
	 * @see #getTitle()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	/** Internal setter for {@link #getTitle()} without chain call utility. */
	protected final void internalSetTitle(String value) {
		_title = value;
	}

	/**
	 * Messages JSON blob (raw string, parsed by client).
	 */
	public final String getMessagesJson() {
		return _messagesJson;
	}

	/**
	 * @see #getMessagesJson()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse setMessagesJson(String value) {
		internalSetMessagesJson(value);
		return this;
	}

	/** Internal setter for {@link #getMessagesJson()} without chain call utility. */
	protected final void internalSetMessagesJson(String value) {
		_messagesJson = value;
	}

	/**
	 * Turn-based entries (V2 format) as a JSON array of <code>PersistedTurn</code> objects
	 * (see <code>com.top_logic.tl.ai.core.session.persist</code>). When present, takes
	 * precedence over entries.
	 */
	public final String getTurnsJson() {
		return _turnsJson;
	}

	/**
	 * @see #getTurnsJson()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse setTurnsJson(String value) {
		internalSetTurnsJson(value);
		return this;
	}

	/** Internal setter for {@link #getTurnsJson()} without chain call utility. */
	protected final void internalSetTurnsJson(String value) {
		_turnsJson = value;
	}

	/**
	 * Token usage of the last agent-loop iteration of the session's last turn.
	 *
	 * <p>
	 * Its prompt size (input + cache read + cache write) is the exact context the model saw when
	 * the session was last active, so re-opening a session restores the context ring to a measured
	 * number rather than an estimate. Absent for a session whose transcript predates the field, and
	 * for a session that has not run a turn yet.
	 * </p>
	 */
	public final com.top_logic.ai.chat.api.messages.TokenUsageDto getLastPromptUsage() {
		return _lastPromptUsage;
	}

	/**
	 * @see #getLastPromptUsage()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse setLastPromptUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto value) {
		internalSetLastPromptUsage(value);
		return this;
	}

	/** Internal setter for {@link #getLastPromptUsage()} without chain call utility. */
	protected final void internalSetLastPromptUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto value) {
		_lastPromptUsage = value;
	}

	/**
	 * Checks, whether {@link #getLastPromptUsage()} has a value.
	 */
	public final boolean hasLastPromptUsage() {
		return _lastPromptUsage != null;
	}

	/**
	 * Context window in tokens of the model that ran the session's last turn, or zero when that
	 * model is no longer configured.
	 */
	public final int getContextWindow() {
		return _contextWindow;
	}

	/**
	 * @see #getContextWindow()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse setContextWindow(int value) {
		internalSetContextWindow(value);
		return this;
	}

	/** Internal setter for {@link #getContextWindow()} without chain call utility. */
	protected final void internalSetContextWindow(int value) {
		_contextWindow = value;
	}

	/**
	 * Model that ran the session's last turn, empty when the session has not run one.
	 */
	public final String getLastModel() {
		return _lastModel;
	}

	/**
	 * @see #getLastModel()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse setLastModel(String value) {
		internalSetLastModel(value);
		return this;
	}

	/** Internal setter for {@link #getLastModel()} without chain call utility. */
	protected final void internalSetLastModel(String value) {
		_lastModel = value;
	}

	@Override
	public String jsonType() {
		return CHAT_SESSION_DETAIL_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			TITLE__PROP, 
			MESSAGES_JSON__PROP, 
			TURNS_JSON__PROP, 
			LAST_PROMPT_USAGE__PROP, 
			CONTEXT_WINDOW__PROP, 
			LAST_MODEL__PROP);
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
			case ID__PROP: return getId();
			case TITLE__PROP: return getTitle();
			case MESSAGES_JSON__PROP: return getMessagesJson();
			case TURNS_JSON__PROP: return getTurnsJson();
			case LAST_PROMPT_USAGE__PROP: return getLastPromptUsage();
			case CONTEXT_WINDOW__PROP: return getContextWindow();
			case LAST_MODEL__PROP: return getLastModel();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case TITLE__PROP: internalSetTitle((String) value); break;
			case MESSAGES_JSON__PROP: internalSetMessagesJson((String) value); break;
			case TURNS_JSON__PROP: internalSetTurnsJson((String) value); break;
			case LAST_PROMPT_USAGE__PROP: internalSetLastPromptUsage((com.top_logic.ai.chat.api.messages.TokenUsageDto) value); break;
			case CONTEXT_WINDOW__PROP: internalSetContextWindow((int) value); break;
			case LAST_MODEL__PROP: internalSetLastModel((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse readChatSessionDetailResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse result = new com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse();
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
		out.name(ID__PROP);
		out.value(getId());
		out.name(TITLE__PROP);
		out.value(getTitle());
		out.name(MESSAGES_JSON__PROP);
		out.value(getMessagesJson());
		out.name(TURNS_JSON__PROP);
		out.value(getTurnsJson());
		if (hasLastPromptUsage()) {
			out.name(LAST_PROMPT_USAGE__PROP);
			getLastPromptUsage().writeTo(out);
		}
		out.name(CONTEXT_WINDOW__PROP);
		out.value(getContextWindow());
		out.name(LAST_MODEL__PROP);
		out.value(getLastModel());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MESSAGES_JSON__PROP: setMessagesJson(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TURNS_JSON__PROP: setTurnsJson(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LAST_PROMPT_USAGE__PROP: setLastPromptUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto.readTokenUsageDto(in)); break;
			case CONTEXT_WINDOW__PROP: setContextWindow(in.nextInt()); break;
			case LAST_MODEL__PROP: setLastModel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
