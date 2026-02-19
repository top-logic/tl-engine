package com.top_logic.ai.chat.api.messages;

/**
 * Response body for GET /chat/sessions.
 */
public class ChatSessionListResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ChatSessionListResponse} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ChatSessionListResponse create() {
		return new com.top_logic.ai.chat.api.messages.ChatSessionListResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ChatSessionListResponse} type in JSON format. */
	public static final String CHAT_SESSION_LIST_RESPONSE__TYPE = "ChatSessionListResponse";

	/** @see #getSessions() */
	public static final String SESSIONS__PROP = "sessions";

	private final java.util.List<com.top_logic.ai.chat.api.messages.ChatSessionDto> _sessions = new java.util.ArrayList<>();

	/**
	 * Creates a {@link ChatSessionListResponse} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ChatSessionListResponse#create()
	 */
	protected ChatSessionListResponse() {
		super();
	}

	/**
	 * All sessions for the current user, sorted by lastUpdated descending.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.ChatSessionDto> getSessions() {
		return _sessions;
	}

	/**
	 * @see #getSessions()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSessionListResponse setSessions(java.util.List<? extends com.top_logic.ai.chat.api.messages.ChatSessionDto> value) {
		internalSetSessions(value);
		return this;
	}

	/** Internal setter for {@link #getSessions()} without chain call utility. */
	protected final void internalSetSessions(java.util.List<? extends com.top_logic.ai.chat.api.messages.ChatSessionDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'sessions' cannot be null.");
		_sessions.clear();
		_sessions.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getSessions()} list.
	 */
	public com.top_logic.ai.chat.api.messages.ChatSessionListResponse addSession(com.top_logic.ai.chat.api.messages.ChatSessionDto value) {
		internalAddSession(value);
		return this;
	}

	/** Implementation of {@link #addSession(com.top_logic.ai.chat.api.messages.ChatSessionDto)} without chain call utility. */
	protected final void internalAddSession(com.top_logic.ai.chat.api.messages.ChatSessionDto value) {
		_sessions.add(value);
	}

	/**
	 * Removes a value from the {@link #getSessions()} list.
	 */
	public final void removeSession(com.top_logic.ai.chat.api.messages.ChatSessionDto value) {
		_sessions.remove(value);
	}

	@Override
	public String jsonType() {
		return CHAT_SESSION_LIST_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SESSIONS__PROP);
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
			case SESSIONS__PROP: return getSessions();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SESSIONS__PROP: internalSetSessions(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.ChatSessionDto.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ChatSessionListResponse readChatSessionListResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ChatSessionListResponse result = new com.top_logic.ai.chat.api.messages.ChatSessionListResponse();
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
		out.name(SESSIONS__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.ChatSessionDto x : getSessions()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SESSIONS__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.ChatSessionDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.ChatSessionDto.readChatSessionDto(in));
				}
				in.endArray();
				setSessions(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
