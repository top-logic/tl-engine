package com.top_logic.ai.chat.api.messages;

/**
 * One entry in a persisted chat: either a message or a completed task snapshot.
 */
public class PersistedChatEntry extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.PersistedChatEntry} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.PersistedChatEntry create() {
		return new com.top_logic.ai.chat.api.messages.PersistedChatEntry();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.PersistedChatEntry} type in JSON format. */
	public static final String PERSISTED_CHAT_ENTRY__TYPE = "PersistedChatEntry";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getMessageData() */
	public static final String MESSAGE_DATA__PROP = "messageData";

	/** @see #getTaskData() */
	public static final String TASK_DATA__PROP = "taskData";

	private String _type = "";

	private com.top_logic.ai.chat.api.messages.MessageDto _messageData = null;

	private com.top_logic.ai.chat.api.messages.CurrentTaskDto _taskData = null;

	/**
	 * Creates a {@link PersistedChatEntry} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.PersistedChatEntry#create()
	 */
	protected PersistedChatEntry() {
		super();
	}

	/**
	 * Entry type: "message" or "task_snapshot".
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.ai.chat.api.messages.PersistedChatEntry setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * The message data (populated when type="message").
	 */
	public final com.top_logic.ai.chat.api.messages.MessageDto getMessageData() {
		return _messageData;
	}

	/**
	 * @see #getMessageData()
	 */
	public com.top_logic.ai.chat.api.messages.PersistedChatEntry setMessageData(com.top_logic.ai.chat.api.messages.MessageDto value) {
		internalSetMessageData(value);
		return this;
	}

	/** Internal setter for {@link #getMessageData()} without chain call utility. */
	protected final void internalSetMessageData(com.top_logic.ai.chat.api.messages.MessageDto value) {
		_messageData = value;
	}

	/**
	 * Checks, whether {@link #getMessageData()} has a value.
	 */
	public final boolean hasMessageData() {
		return _messageData != null;
	}

	/**
	 * The task snapshot data (populated when type="task_snapshot").
	 */
	public final com.top_logic.ai.chat.api.messages.CurrentTaskDto getTaskData() {
		return _taskData;
	}

	/**
	 * @see #getTaskData()
	 */
	public com.top_logic.ai.chat.api.messages.PersistedChatEntry setTaskData(com.top_logic.ai.chat.api.messages.CurrentTaskDto value) {
		internalSetTaskData(value);
		return this;
	}

	/** Internal setter for {@link #getTaskData()} without chain call utility. */
	protected final void internalSetTaskData(com.top_logic.ai.chat.api.messages.CurrentTaskDto value) {
		_taskData = value;
	}

	/**
	 * Checks, whether {@link #getTaskData()} has a value.
	 */
	public final boolean hasTaskData() {
		return _taskData != null;
	}

	@Override
	public String jsonType() {
		return PERSISTED_CHAT_ENTRY__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TYPE__PROP, 
			MESSAGE_DATA__PROP, 
			TASK_DATA__PROP);
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
			case TYPE__PROP: return getType();
			case MESSAGE_DATA__PROP: return getMessageData();
			case TASK_DATA__PROP: return getTaskData();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TYPE__PROP: internalSetType((String) value); break;
			case MESSAGE_DATA__PROP: internalSetMessageData((com.top_logic.ai.chat.api.messages.MessageDto) value); break;
			case TASK_DATA__PROP: internalSetTaskData((com.top_logic.ai.chat.api.messages.CurrentTaskDto) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.PersistedChatEntry readPersistedChatEntry(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.PersistedChatEntry result = new com.top_logic.ai.chat.api.messages.PersistedChatEntry();
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
		out.name(TYPE__PROP);
		out.value(getType());
		if (hasMessageData()) {
			out.name(MESSAGE_DATA__PROP);
			getMessageData().writeTo(out);
		}
		if (hasTaskData()) {
			out.name(TASK_DATA__PROP);
			getTaskData().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MESSAGE_DATA__PROP: setMessageData(com.top_logic.ai.chat.api.messages.MessageDto.readMessageDto(in)); break;
			case TASK_DATA__PROP: setTaskData(com.top_logic.ai.chat.api.messages.CurrentTaskDto.readCurrentTaskDto(in)); break;
			default: super.readField(in, field);
		}
	}

}
