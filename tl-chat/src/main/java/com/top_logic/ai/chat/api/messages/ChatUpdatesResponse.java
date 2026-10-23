package com.top_logic.ai.chat.api.messages;

/**
 * Response body for GET /chat/updates.
 */
public class ChatUpdatesResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ChatUpdatesResponse} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ChatUpdatesResponse create() {
		return new com.top_logic.ai.chat.api.messages.ChatUpdatesResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ChatUpdatesResponse} type in JSON format. */
	public static final String CHAT_UPDATES_RESPONSE__TYPE = "ChatUpdatesResponse";

	/** @see #isProcessing() */
	public static final String PROCESSING__PROP = "processing";

	/** @see #getMessages() */
	public static final String MESSAGES__PROP = "messages";

	/** @see #getTasksJson() */
	public static final String TASKS_JSON__PROP = "tasksJson";

	private boolean _processing = false;

	private final java.util.List<com.top_logic.ai.chat.api.messages.MessageDto> _messages = new java.util.ArrayList<>();

	private String _tasksJson = "";

	/**
	 * Creates a {@link ChatUpdatesResponse} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ChatUpdatesResponse#create()
	 */
	protected ChatUpdatesResponse() {
		super();
	}

	/**
	 * Whether an AI task is currently in progress.
	 */
	public final boolean isProcessing() {
		return _processing;
	}

	/**
	 * @see #isProcessing()
	 */
	public com.top_logic.ai.chat.api.messages.ChatUpdatesResponse setProcessing(boolean value) {
		internalSetProcessing(value);
		return this;
	}

	/** Internal setter for {@link #isProcessing()} without chain call utility. */
	protected final void internalSetProcessing(boolean value) {
		_processing = value;
	}

	/**
	 * All messages in the chat.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.MessageDto> getMessages() {
		return _messages;
	}

	/**
	 * @see #getMessages()
	 */
	public com.top_logic.ai.chat.api.messages.ChatUpdatesResponse setMessages(java.util.List<? extends com.top_logic.ai.chat.api.messages.MessageDto> value) {
		internalSetMessages(value);
		return this;
	}

	/** Internal setter for {@link #getMessages()} without chain call utility. */
	protected final void internalSetMessages(java.util.List<? extends com.top_logic.ai.chat.api.messages.MessageDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'messages' cannot be null.");
		_messages.clear();
		_messages.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getMessages()} list.
	 */
	public com.top_logic.ai.chat.api.messages.ChatUpdatesResponse addMessage(com.top_logic.ai.chat.api.messages.MessageDto value) {
		internalAddMessage(value);
		return this;
	}

	/** Implementation of {@link #addMessage(com.top_logic.ai.chat.api.messages.MessageDto)} without chain call utility. */
	protected final void internalAddMessage(com.top_logic.ai.chat.api.messages.MessageDto value) {
		_messages.add(value);
	}

	/**
	 * Removes a value from the {@link #getMessages()} list.
	 */
	public final void removeMessage(com.top_logic.ai.chat.api.messages.MessageDto value) {
		_messages.remove(value);
	}

	/**
	 * The open session's task list as JSON, in the format the <code>task_update</code> SSE frame
	 * carries (<code>TaskStore.toJson()</code>).
	 *
	 * <p>
	 * The stream replays nothing, so this is how a page that was reloaded in mid-run gets the
	 * progress it was showing back.
	 * </p>
	 */
	public final String getTasksJson() {
		return _tasksJson;
	}

	/**
	 * @see #getTasksJson()
	 */
	public com.top_logic.ai.chat.api.messages.ChatUpdatesResponse setTasksJson(String value) {
		internalSetTasksJson(value);
		return this;
	}

	/** Internal setter for {@link #getTasksJson()} without chain call utility. */
	protected final void internalSetTasksJson(String value) {
		_tasksJson = value;
	}

	@Override
	public String jsonType() {
		return CHAT_UPDATES_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			PROCESSING__PROP, 
			MESSAGES__PROP, 
			TASKS_JSON__PROP);
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
			case PROCESSING__PROP: return isProcessing();
			case MESSAGES__PROP: return getMessages();
			case TASKS_JSON__PROP: return getTasksJson();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case PROCESSING__PROP: internalSetProcessing((boolean) value); break;
			case MESSAGES__PROP: internalSetMessages(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.MessageDto.class, value)); break;
			case TASKS_JSON__PROP: internalSetTasksJson((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ChatUpdatesResponse readChatUpdatesResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ChatUpdatesResponse result = new com.top_logic.ai.chat.api.messages.ChatUpdatesResponse();
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
		out.name(PROCESSING__PROP);
		out.value(isProcessing());
		out.name(MESSAGES__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.MessageDto x : getMessages()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(TASKS_JSON__PROP);
		out.value(getTasksJson());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case PROCESSING__PROP: setProcessing(in.nextBoolean()); break;
			case MESSAGES__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.MessageDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.MessageDto.readMessageDto(in));
				}
				in.endArray();
				setMessages(newValue);
			}
			break;
			case TASKS_JSON__PROP: setTasksJson(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
