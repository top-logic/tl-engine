package com.top_logic.ai.chat.api.messages;

/**
 * Reply to a command of {@code ChatControl} that sends a message: a send, an edited send and the
 * regeneration of an answer.
 */
public class ChatSendReply extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ChatSendReply} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ChatSendReply create() {
		return new com.top_logic.ai.chat.api.messages.ChatSendReply();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ChatSendReply} type in JSON format. */
	public static final String CHAT_SEND_REPLY__TYPE = "ChatSendReply";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	/** @see #getReason() */
	public static final String REASON__PROP = "reason";

	/** @see #getSessionId() */
	public static final String SESSION_ID__PROP = "sessionId";

	/** @see #getFeatures() */
	public static final String FEATURES__PROP = "features";

	/** @see #getRemovedTurns() */
	public static final String REMOVED_TURNS__PROP = "removedTurns";

	/** @see #getText() */
	public static final String TEXT__PROP = "text";

	private String _status = "";

	private String _reason = null;

	private String _sessionId = null;

	private com.top_logic.ai.chat.api.messages.ConversationFeaturesDto _features = null;

	private Integer _removedTurns = null;

	private String _text = null;

	/**
	 * Creates a {@link ChatSendReply} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ChatSendReply#create()
	 */
	protected ChatSendReply() {
		super();
	}

	/**
	 * What became of the request, one of the status codes of {@code ChatControl}: the message
	 * went ({@code processing}), the chat answers something else, the request is forbidden or
	 * rejected, it names nothing there is, or it was malformed.
	 */
	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendReply setStatus(String value) {
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
	public com.top_logic.ai.chat.api.messages.ChatSendReply setReason(String value) {
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

	/**
	 * The chat the message went to; absent for a message that went nowhere.
	 */
	public final String getSessionId() {
		return _sessionId;
	}

	/**
	 * @see #getSessionId()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendReply setSessionId(String value) {
		internalSetSessionId(value);
		return this;
	}

	/** Internal setter for {@link #getSessionId()} without chain call utility. */
	protected final void internalSetSessionId(String value) {
		_sessionId = value;
	}

	/**
	 * Checks, whether {@link #getSessionId()} has a value.
	 */
	public final boolean hasSessionId() {
		return _sessionId != null;
	}

	/**
	 * What the chat the message went to supports; absent for a message that went nowhere.
	 */
	public final com.top_logic.ai.chat.api.messages.ConversationFeaturesDto getFeatures() {
		return _features;
	}

	/**
	 * @see #getFeatures()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendReply setFeatures(com.top_logic.ai.chat.api.messages.ConversationFeaturesDto value) {
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

	/**
	 * How many turns an edited send or a regeneration dropped; absent for a plain send.
	 */
	public final Integer getRemovedTurns() {
		return _removedTurns;
	}

	/**
	 * @see #getRemovedTurns()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendReply setRemovedTurns(Integer value) {
		internalSetRemovedTurns(value);
		return this;
	}

	/** Internal setter for {@link #getRemovedTurns()} without chain call utility. */
	protected final void internalSetRemovedTurns(Integer value) {
		_removedTurns = value;
	}

	/**
	 * Checks, whether {@link #getRemovedTurns()} has a value.
	 */
	public final boolean hasRemovedTurns() {
		return _removedTurns != null;
	}

	/**
	 * The message a regeneration sent again; absent for any other send.
	 */
	public final String getText() {
		return _text;
	}

	/**
	 * @see #getText()
	 */
	public com.top_logic.ai.chat.api.messages.ChatSendReply setText(String value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(String value) {
		_text = value;
	}

	/**
	 * Checks, whether {@link #getText()} has a value.
	 */
	public final boolean hasText() {
		return _text != null;
	}

	@Override
	public String jsonType() {
		return CHAT_SEND_REPLY__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			STATUS__PROP, 
			REASON__PROP, 
			SESSION_ID__PROP, 
			FEATURES__PROP, 
			REMOVED_TURNS__PROP, 
			TEXT__PROP);
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
			case SESSION_ID__PROP: return getSessionId();
			case FEATURES__PROP: return getFeatures();
			case REMOVED_TURNS__PROP: return getRemovedTurns();
			case TEXT__PROP: return getText();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case STATUS__PROP: internalSetStatus((String) value); break;
			case REASON__PROP: internalSetReason((String) value); break;
			case SESSION_ID__PROP: internalSetSessionId((String) value); break;
			case FEATURES__PROP: internalSetFeatures((com.top_logic.ai.chat.api.messages.ConversationFeaturesDto) value); break;
			case REMOVED_TURNS__PROP: internalSetRemovedTurns((Integer) value); break;
			case TEXT__PROP: internalSetText((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ChatSendReply readChatSendReply(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ChatSendReply result = new com.top_logic.ai.chat.api.messages.ChatSendReply();
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
		if (hasSessionId()) {
			out.name(SESSION_ID__PROP);
			out.value(getSessionId());
		}
		if (hasFeatures()) {
			out.name(FEATURES__PROP);
			getFeatures().writeTo(out);
		}
		if (hasRemovedTurns()) {
			out.name(REMOVED_TURNS__PROP);
			out.value(getRemovedTurns());
		}
		if (hasText()) {
			out.name(TEXT__PROP);
			out.value(getText());
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REASON__PROP: setReason(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SESSION_ID__PROP: setSessionId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FEATURES__PROP: setFeatures(com.top_logic.ai.chat.api.messages.ConversationFeaturesDto.readConversationFeaturesDto(in)); break;
			case REMOVED_TURNS__PROP: setRemovedTurns(in.nextInt()); break;
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
