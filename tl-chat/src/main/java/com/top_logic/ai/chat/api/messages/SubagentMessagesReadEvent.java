package com.top_logic.ai.chat.api.messages;

/**
 * Inner data of the {@code user_message_read} frame of a background subagent: it has read these
 * messages of the user at the start of a turn.
 */
public class SubagentMessagesReadEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.SubagentMessagesReadEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.SubagentMessagesReadEvent create() {
		return new com.top_logic.ai.chat.api.messages.SubagentMessagesReadEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.SubagentMessagesReadEvent} type in JSON format. */
	public static final String SUBAGENT_MESSAGES_READ_EVENT__TYPE = "SubagentMessagesReadEvent";

	/** @see #getMessages() */
	public static final String MESSAGES__PROP = "messages";

	private final java.util.List<com.top_logic.ai.chat.api.messages.SubagentMessageDto> _messages = new java.util.ArrayList<>();

	/**
	 * Creates a {@link SubagentMessagesReadEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.SubagentMessagesReadEvent#create()
	 */
	protected SubagentMessagesReadEvent() {
		super();
	}

	/**
	 * The messages read, in the order they were sent.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.SubagentMessageDto> getMessages() {
		return _messages;
	}

	/**
	 * @see #getMessages()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentMessagesReadEvent setMessages(java.util.List<? extends com.top_logic.ai.chat.api.messages.SubagentMessageDto> value) {
		internalSetMessages(value);
		return this;
	}

	/** Internal setter for {@link #getMessages()} without chain call utility. */
	protected final void internalSetMessages(java.util.List<? extends com.top_logic.ai.chat.api.messages.SubagentMessageDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'messages' cannot be null.");
		_messages.clear();
		_messages.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getMessages()} list.
	 */
	public com.top_logic.ai.chat.api.messages.SubagentMessagesReadEvent addMessage(com.top_logic.ai.chat.api.messages.SubagentMessageDto value) {
		internalAddMessage(value);
		return this;
	}

	/** Implementation of {@link #addMessage(com.top_logic.ai.chat.api.messages.SubagentMessageDto)} without chain call utility. */
	protected final void internalAddMessage(com.top_logic.ai.chat.api.messages.SubagentMessageDto value) {
		_messages.add(value);
	}

	/**
	 * Removes a value from the {@link #getMessages()} list.
	 */
	public final void removeMessage(com.top_logic.ai.chat.api.messages.SubagentMessageDto value) {
		_messages.remove(value);
	}

	@Override
	public String jsonType() {
		return SUBAGENT_MESSAGES_READ_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			MESSAGES__PROP);
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
			case MESSAGES__PROP: return getMessages();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case MESSAGES__PROP: internalSetMessages(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.SubagentMessageDto.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.SubagentMessagesReadEvent readSubagentMessagesReadEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.SubagentMessagesReadEvent result = new com.top_logic.ai.chat.api.messages.SubagentMessagesReadEvent();
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
		out.name(MESSAGES__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.SubagentMessageDto x : getMessages()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case MESSAGES__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.SubagentMessageDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.SubagentMessageDto.readSubagentMessageDto(in));
				}
				in.endArray();
				setMessages(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
