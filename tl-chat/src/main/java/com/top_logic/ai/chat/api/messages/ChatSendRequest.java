package com.top_logic.ai.chat.api.messages;

/**
 * Request body for POST /chat/send.
 */
public class ChatSendRequest extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ChatSendRequest} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ChatSendRequest create() {
		return new com.top_logic.ai.chat.api.messages.ChatSendRequest();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ChatSendRequest} type in JSON format. */
	public static final String CHAT_SEND_REQUEST__TYPE = "ChatSendRequest";

	/** @see #getText() */
	public static final String TEXT__PROP = "text";

	/** @see #getSessionId() */
	public static final String SESSION_ID__PROP = "sessionId";

	private String _text = "";

	private String _sessionId = "";

	/**
	 * Creates a {@link ChatSendRequest} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ChatSendRequest#create()
	 */
	protected ChatSendRequest() {
		super();
	}

	/**
	 * User message text.
	 */
	public final String getText() {
		return _text;
	}

	/**
	 * @see #getText()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendRequest setText(String value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(String value) {
		_text = value;
	}

	/**
	 * Active session ID; if set, messages will be persisted to this session.
	 */
	public final String getSessionId() {
		return _sessionId;
	}

	/**
	 * @see #getSessionId()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendRequest setSessionId(String value) {
		internalSetSessionId(value);
		return this;
	}

	/** Internal setter for {@link #getSessionId()} without chain call utility. */
	protected final void internalSetSessionId(String value) {
		_sessionId = value;
	}

	@Override
	public String jsonType() {
		return CHAT_SEND_REQUEST__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TEXT__PROP, 
			SESSION_ID__PROP);
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
			case TEXT__PROP: return getText();
			case SESSION_ID__PROP: return getSessionId();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TEXT__PROP: internalSetText((String) value); break;
			case SESSION_ID__PROP: internalSetSessionId((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ChatSendRequest readChatSendRequest(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ChatSendRequest result = new com.top_logic.ai.chat.api.messages.ChatSendRequest();
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
		out.name(TEXT__PROP);
		out.value(getText());
		out.name(SESSION_ID__PROP);
		out.value(getSessionId());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SESSION_ID__PROP: setSessionId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
