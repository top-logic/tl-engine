package com.top_logic.ai.chat.api.messages;

/**
 * Response body for POST /chat/send.
 */
public class ChatSendResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ChatSendResponse} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ChatSendResponse create() {
		return new com.top_logic.ai.chat.api.messages.ChatSendResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ChatSendResponse} type in JSON format. */
	public static final String CHAT_SEND_RESPONSE__TYPE = "ChatSendResponse";

	/** @see #getTaskId() */
	public static final String TASK_ID__PROP = "taskId";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	/** @see #getReason() */
	public static final String REASON__PROP = "reason";

	/** @see #getFeatures() */
	public static final String FEATURES__PROP = "features";

	private String _taskId = "";

	private String _status = "";

	private String _reason = "";

	private com.top_logic.ai.chat.api.messages.ConversationFeaturesDto _features = null;

	/**
	 * Creates a {@link ChatSendResponse} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ChatSendResponse#create()
	 */
	protected ChatSendResponse() {
		super();
	}

	/**
	 * Task ID for polling updates.
	 */
	public final String getTaskId() {
		return _taskId;
	}

	/**
	 * @see #getTaskId()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendResponse setTaskId(String value) {
		internalSetTaskId(value);
		return this;
	}

	/** Internal setter for {@link #getTaskId()} without chain call utility. */
	protected final void internalSetTaskId(String value) {
		_taskId = value;
	}

	/**
	 * e.g. "processing", or "forbidden" for a request the acting account may not make.
	 */
	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendResponse setStatus(String value) {
		internalSetStatus(value);
		return this;
	}

	/** Internal setter for {@link #getStatus()} without chain call utility. */
	protected final void internalSetStatus(String value) {
		_status = value;
	}

	/**
	 * Why the request was refused, in the acting user's language; empty unless it was.
	 */
	public final String getReason() {
		return _reason;
	}

	/**
	 * @see #getReason()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendResponse setReason(String value) {
		internalSetReason(value);
		return this;
	}

	/** Internal setter for {@link #getReason()} without chain call utility. */
	protected final void internalSetReason(String value) {
		_reason = value;
	}

	/**
	 * Which functions beyond sending the conversation the message was sent in supports. Absent for a
	 * send that names no conversation.
	 */
	public final com.top_logic.ai.chat.api.messages.ConversationFeaturesDto getFeatures() {
		return _features;
	}

	/**
	 * @see #getFeatures()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendResponse setFeatures(com.top_logic.ai.chat.api.messages.ConversationFeaturesDto value) {
		internalSetFeatures(value);
		return this;
	}

	/** Internal setter for {@link #getFeatures()} without chain call utility. */
	protected final void internalSetFeatures(com.top_logic.ai.chat.api.messages.ConversationFeaturesDto value) {
		_features = value;
	}

	/**
	 * Checks, whether {@link #getFeatures()} has a value.
	 */
	public final boolean hasFeatures() {
		return _features != null;
	}

	@Override
	public String jsonType() {
		return CHAT_SEND_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TASK_ID__PROP, 
			STATUS__PROP, 
			REASON__PROP, 
			FEATURES__PROP);
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
			case TASK_ID__PROP: return getTaskId();
			case STATUS__PROP: return getStatus();
			case REASON__PROP: return getReason();
			case FEATURES__PROP: return getFeatures();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TASK_ID__PROP: internalSetTaskId((String) value); break;
			case STATUS__PROP: internalSetStatus((String) value); break;
			case REASON__PROP: internalSetReason((String) value); break;
			case FEATURES__PROP: internalSetFeatures((com.top_logic.ai.chat.api.messages.ConversationFeaturesDto) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ChatSendResponse readChatSendResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ChatSendResponse result = new com.top_logic.ai.chat.api.messages.ChatSendResponse();
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
		out.name(TASK_ID__PROP);
		out.value(getTaskId());
		out.name(STATUS__PROP);
		out.value(getStatus());
		out.name(REASON__PROP);
		out.value(getReason());
		if (hasFeatures()) {
			out.name(FEATURES__PROP);
			getFeatures().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TASK_ID__PROP: setTaskId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REASON__PROP: setReason(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FEATURES__PROP: setFeatures(com.top_logic.ai.chat.api.messages.ConversationFeaturesDto.readConversationFeaturesDto(in)); break;
			default: super.readField(in, field);
		}
	}

}
