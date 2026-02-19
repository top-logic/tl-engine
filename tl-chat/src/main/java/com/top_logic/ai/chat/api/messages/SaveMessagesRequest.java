package com.top_logic.ai.chat.api.messages;

/**
 * Request body for PUT /chat/sessions/{id}/messages.
 */
public class SaveMessagesRequest extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.SaveMessagesRequest} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.SaveMessagesRequest create() {
		return new com.top_logic.ai.chat.api.messages.SaveMessagesRequest();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.SaveMessagesRequest} type in JSON format. */
	public static final String SAVE_MESSAGES_REQUEST__TYPE = "SaveMessagesRequest";

	/** @see #getMessagesJson() */
	public static final String MESSAGES_JSON__PROP = "messagesJson";

	private String _messagesJson = "";

	/**
	 * Creates a {@link SaveMessagesRequest} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.SaveMessagesRequest#create()
	 */
	protected SaveMessagesRequest() {
		super();
	}

	/**
	 * Messages JSON blob to save.
	 */
	public final String getMessagesJson() {
		return _messagesJson;
	}

	/**
	 * @see #getMessagesJson()
	 */
	public com.top_logic.ai.chat.api.messages.SaveMessagesRequest setMessagesJson(String value) {
		internalSetMessagesJson(value);
		return this;
	}

	/** Internal setter for {@link #getMessagesJson()} without chain call utility. */
	protected final void internalSetMessagesJson(String value) {
		_messagesJson = value;
	}

	@Override
	public String jsonType() {
		return SAVE_MESSAGES_REQUEST__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			MESSAGES_JSON__PROP);
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
			case MESSAGES_JSON__PROP: return getMessagesJson();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case MESSAGES_JSON__PROP: internalSetMessagesJson((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.SaveMessagesRequest readSaveMessagesRequest(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.SaveMessagesRequest result = new com.top_logic.ai.chat.api.messages.SaveMessagesRequest();
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
		out.name(MESSAGES_JSON__PROP);
		out.value(getMessagesJson());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case MESSAGES_JSON__PROP: setMessagesJson(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
