package com.top_logic.ai.chat.api.messages;

/**
 * Summary of a chat session for the history panel.
 */
public class ChatSessionDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ChatSessionDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ChatSessionDto create() {
		return new com.top_logic.ai.chat.api.messages.ChatSessionDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ChatSessionDto} type in JSON format. */
	public static final String CHAT_SESSION_DTO__TYPE = "ChatSessionDto";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getTitle() */
	public static final String TITLE__PROP = "title";

	/** @see #getLastUpdated() */
	public static final String LAST_UPDATED__PROP = "lastUpdated";

	private String _id = "";

	private String _title = "";

	private String _lastUpdated = "";

	/**
	 * Creates a {@link ChatSessionDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ChatSessionDto#create()
	 */
	protected ChatSessionDto() {
		super();
	}

	/**
	 * Session ID (KnowledgeObject ID).
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSessionDto setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * User-editable session title.
	 */
	public final String getTitle() {
		return _title;
	}

	/**
	 * @see #getTitle()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSessionDto setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	/** Internal setter for {@link #getTitle()} without chain call utility. */
	protected final void internalSetTitle(String value) {
		_title = value;
	}

	/**
	 * ISO-8601 timestamp of last update.
	 */
	public final String getLastUpdated() {
		return _lastUpdated;
	}

	/**
	 * @see #getLastUpdated()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSessionDto setLastUpdated(String value) {
		internalSetLastUpdated(value);
		return this;
	}

	/** Internal setter for {@link #getLastUpdated()} without chain call utility. */
	protected final void internalSetLastUpdated(String value) {
		_lastUpdated = value;
	}

	@Override
	public String jsonType() {
		return CHAT_SESSION_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			TITLE__PROP, 
			LAST_UPDATED__PROP);
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
			case LAST_UPDATED__PROP: return getLastUpdated();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case TITLE__PROP: internalSetTitle((String) value); break;
			case LAST_UPDATED__PROP: internalSetLastUpdated((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ChatSessionDto readChatSessionDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ChatSessionDto result = new com.top_logic.ai.chat.api.messages.ChatSessionDto();
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
		out.name(LAST_UPDATED__PROP);
		out.value(getLastUpdated());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LAST_UPDATED__PROP: setLastUpdated(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
