package com.top_logic.ai.chat.api.messages;

/**
 * Reply to a command of {@code ChatControl} that answers with what became of the request alone:
 * the answer to a request of a run, the auto-approve switch, a rating, the start of a prompt
 * improvement.
 */
public class ChatStatusReply extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ChatStatusReply} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ChatStatusReply create() {
		return new com.top_logic.ai.chat.api.messages.ChatStatusReply();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ChatStatusReply} type in JSON format. */
	public static final String CHAT_STATUS_REPLY__TYPE = "ChatStatusReply";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	/** @see #getReason() */
	public static final String REASON__PROP = "reason";

	private String _status = "";

	private String _reason = null;

	/**
	 * Creates a {@link ChatStatusReply} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ChatStatusReply#create()
	 */
	protected ChatStatusReply() {
		super();
	}

	/**
	 * What became of the request, one of the status codes of {@code ChatControl}.
	 */
	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.ai.chat.api.messages.ChatStatusReply setStatus(String value) {
		internalSetStatus(value);
		return this;
	}

	/** Internal setter for {@link #getStatus()} without chain call utility. */
	protected final void internalSetStatus(String value) {
		_status = value;
	}

	/**
	 * Why the request was refused, a sentence in the user's language; absent unless it was.
	 */
	public final String getReason() {
		return _reason;
	}

	/**
	 * @see #getReason()
	 */
	public com.top_logic.ai.chat.api.messages.ChatStatusReply setReason(String value) {
		internalSetReason(value);
		return this;
	}

	/** Internal setter for {@link #getReason()} without chain call utility. */
	protected final void internalSetReason(String value) {
		_reason = value;
	}

	/**
	 * Checks, whether {@link #getReason()} has a value.
	 */
	public final boolean hasReason() {
		return _reason != null;
	}

	@Override
	public String jsonType() {
		return CHAT_STATUS_REPLY__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			STATUS__PROP, 
			REASON__PROP);
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
			case REASON__PROP: return getReason();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case STATUS__PROP: internalSetStatus((String) value); break;
			case REASON__PROP: internalSetReason((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ChatStatusReply readChatStatusReply(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ChatStatusReply result = new com.top_logic.ai.chat.api.messages.ChatStatusReply();
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
		if (hasReason()) {
			out.name(REASON__PROP);
			out.value(getReason());
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REASON__PROP: setReason(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
