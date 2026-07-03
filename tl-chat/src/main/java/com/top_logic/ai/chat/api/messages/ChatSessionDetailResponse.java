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

	/** @see #getEntries() */
	public static final String ENTRIES__PROP = "entries";

	/** @see #getTurnsJson() */
	public static final String TURNS_JSON__PROP = "turnsJson";

	private String _id = "";

	private String _title = "";

	private String _messagesJson = "";

	private final java.util.List<com.top_logic.ai.chat.api.messages.PersistedChatEntry> _entries = new java.util.ArrayList<>();

	private String _turnsJson = "";

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
	 * Structured entries (messages + task snapshots). V1 format.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.PersistedChatEntry> getEntries() {
		return _entries;
	}

	/**
	 * @see #getEntries()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse setEntries(java.util.List<? extends com.top_logic.ai.chat.api.messages.PersistedChatEntry> value) {
		internalSetEntries(value);
		return this;
	}

	/** Internal setter for {@link #getEntries()} without chain call utility. */
	protected final void internalSetEntries(java.util.List<? extends com.top_logic.ai.chat.api.messages.PersistedChatEntry> value) {
		if (value == null) throw new IllegalArgumentException("Property 'entries' cannot be null.");
		_entries.clear();
		_entries.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getEntries()} list.
	 */
	public com.top_logic.ai.chat.api.messages.ChatSessionDetailResponse addEntry(com.top_logic.ai.chat.api.messages.PersistedChatEntry value) {
		internalAddEntry(value);
		return this;
	}

	/** Implementation of {@link #addEntry(com.top_logic.ai.chat.api.messages.PersistedChatEntry)} without chain call utility. */
	protected final void internalAddEntry(com.top_logic.ai.chat.api.messages.PersistedChatEntry value) {
		_entries.add(value);
	}

	/**
	 * Removes a value from the {@link #getEntries()} list.
	 */
	public final void removeEntry(com.top_logic.ai.chat.api.messages.PersistedChatEntry value) {
		_entries.remove(value);
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
			ENTRIES__PROP, 
			TURNS_JSON__PROP);
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
			case ENTRIES__PROP: return getEntries();
			case TURNS_JSON__PROP: return getTurnsJson();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case TITLE__PROP: internalSetTitle((String) value); break;
			case MESSAGES_JSON__PROP: internalSetMessagesJson((String) value); break;
			case ENTRIES__PROP: internalSetEntries(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.PersistedChatEntry.class, value)); break;
			case TURNS_JSON__PROP: internalSetTurnsJson((String) value); break;
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
		out.name(ENTRIES__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.PersistedChatEntry x : getEntries()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(TURNS_JSON__PROP);
		out.value(getTurnsJson());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MESSAGES_JSON__PROP: setMessagesJson(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ENTRIES__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.PersistedChatEntry> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.PersistedChatEntry.readPersistedChatEntry(in));
				}
				in.endArray();
				setEntries(newValue);
			}
			break;
			case TURNS_JSON__PROP: setTurnsJson(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
