package com.top_logic.ai.chat.api.messages;

/**
 * Answer of POST /chat/subagents/{id}/message.
 */
public class SubagentMessageResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.SubagentMessageResponse} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.SubagentMessageResponse create() {
		return new com.top_logic.ai.chat.api.messages.SubagentMessageResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.SubagentMessageResponse} type in JSON format. */
	public static final String SUBAGENT_MESSAGE_RESPONSE__TYPE = "SubagentMessageResponse";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	/** @see #getMessageId() */
	public static final String MESSAGE_ID__PROP = "messageId";

	private String _status = "";

	private String _messageId = "";

	/**
	 * Creates a {@link SubagentMessageResponse} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.SubagentMessageResponse#create()
	 */
	protected SubagentMessageResponse() {
		super();
	}

	/**
	 * {@code sent} - the subagent reads it at the start of its next turn; {@code ended} - the
	 * subagent has ended and reads no more messages; {@code unknown} - no such subagent of this
	 * account; {@code empty} - there is no text.
	 */
	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentMessageResponse setStatus(String value) {
		internalSetStatus(value);
		return this;
	}

	/** Internal setter for {@link #getStatus()} without chain call utility. */
	protected final void internalSetStatus(String value) {
		_status = value;
	}

	/**
	 * The id of the message sent; the frame that says it was read names it.
	 */
	public final String getMessageId() {
		return _messageId;
	}

	/**
	 * @see #getMessageId()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentMessageResponse setMessageId(String value) {
		internalSetMessageId(value);
		return this;
	}

	/** Internal setter for {@link #getMessageId()} without chain call utility. */
	protected final void internalSetMessageId(String value) {
		_messageId = value;
	}

	@Override
	public String jsonType() {
		return SUBAGENT_MESSAGE_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			STATUS__PROP, 
			MESSAGE_ID__PROP);
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
			case STATUS__PROP: return getStatus();
			case MESSAGE_ID__PROP: return getMessageId();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case STATUS__PROP: internalSetStatus((String) value); break;
			case MESSAGE_ID__PROP: internalSetMessageId((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.SubagentMessageResponse readSubagentMessageResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.SubagentMessageResponse result = new com.top_logic.ai.chat.api.messages.SubagentMessageResponse();
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
		out.name(STATUS__PROP);
		out.value(getStatus());
		out.name(MESSAGE_ID__PROP);
		out.value(getMessageId());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MESSAGE_ID__PROP: setMessageId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
