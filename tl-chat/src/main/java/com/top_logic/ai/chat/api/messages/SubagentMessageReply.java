package com.top_logic.ai.chat.api.messages;

/**
 * Reply to the command of {@code ChatControl} that writes a message to a subagent.
 */
public class SubagentMessageReply extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.SubagentMessageReply} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.SubagentMessageReply create() {
		return new com.top_logic.ai.chat.api.messages.SubagentMessageReply();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.SubagentMessageReply} type in JSON format. */
	public static final String SUBAGENT_MESSAGE_REPLY__TYPE = "SubagentMessageReply";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	/** @see #getMessageId() */
	public static final String MESSAGE_ID__PROP = "messageId";

	private String _status = "";

	private String _messageId = null;

	/**
	 * Creates a {@link SubagentMessageReply} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.SubagentMessageReply#create()
	 */
	protected SubagentMessageReply() {
		super();
	}

	/**
	 * What became of the message, one of the codes of {@code AccountRuns.SubagentMessageStatus}.
	 */
	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentMessageReply setStatus(String value) {
		internalSetStatus(value);
		return this;
	}

	/** Internal setter for {@link #getStatus()} without chain call utility. */
	protected final void internalSetStatus(String value) {
		_status = value;
	}

	/**
	 * The id of the message that was sent; absent for a message that was not.
	 */
	public final String getMessageId() {
		return _messageId;
	}

	/**
	 * @see #getMessageId()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentMessageReply setMessageId(String value) {
		internalSetMessageId(value);
		return this;
	}

	/** Internal setter for {@link #getMessageId()} without chain call utility. */
	protected final void internalSetMessageId(String value) {
		_messageId = value;
	}

	/**
	 * Checks, whether {@link #getMessageId()} has a value.
	 */
	public final boolean hasMessageId() {
		return _messageId != null;
	}

	@Override
	public String jsonType() {
		return SUBAGENT_MESSAGE_REPLY__TYPE;
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
	public static com.top_logic.ai.chat.api.messages.SubagentMessageReply readSubagentMessageReply(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.SubagentMessageReply result = new com.top_logic.ai.chat.api.messages.SubagentMessageReply();
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
		if (hasMessageId()) {
			out.name(MESSAGE_ID__PROP);
			out.value(getMessageId());
		}
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
