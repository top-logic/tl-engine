package com.top_logic.ai.chat.api.messages;

/**
 * Request body for POST /chat/truncate: drop a turn and everything after it.
 */
public class TruncateRequest extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.TruncateRequest} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.TruncateRequest create() {
		return new com.top_logic.ai.chat.api.messages.TruncateRequest();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.TruncateRequest} type in JSON format. */
	public static final String TRUNCATE_REQUEST__TYPE = "TruncateRequest";

	/** @see #getSessionId() */
	public static final String SESSION_ID__PROP = "sessionId";

	/** @see #getFromTurn() */
	public static final String FROM_TURN__PROP = "fromTurn";

	private String _sessionId = "";

	private int _fromTurn = 0;

	/**
	 * Creates a {@link TruncateRequest} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.TruncateRequest#create()
	 */
	protected TruncateRequest() {
		super();
	}

	/**
	 * Session to truncate.
	 */
	public final String getSessionId() {
		return _sessionId;
	}

	/**
	 * @see #getSessionId()
	 */
	public com.top_logic.ai.chat.api.messages.TruncateRequest setSessionId(String value) {
		internalSetSessionId(value);
		return this;
	}

	/** Internal setter for {@link #getSessionId()} without chain call utility. */
	protected final void internalSetSessionId(String value) {
		_sessionId = value;
	}

	/**
	 * Zero-based index of the first turn to remove. Everything from it to the end goes with it,
	 * because a conversation is a sequence: what followed was said in reply to what is being
	 * removed and cannot stand without it.
	 */
	public final int getFromTurn() {
		return _fromTurn;
	}

	/**
	 * @see #getFromTurn()
	 */
	public com.top_logic.ai.chat.api.messages.TruncateRequest setFromTurn(int value) {
		internalSetFromTurn(value);
		return this;
	}

	/** Internal setter for {@link #getFromTurn()} without chain call utility. */
	protected final void internalSetFromTurn(int value) {
		_fromTurn = value;
	}

	@Override
	public String jsonType() {
		return TRUNCATE_REQUEST__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SESSION_ID__PROP, 
			FROM_TURN__PROP);
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
			case SESSION_ID__PROP: return getSessionId();
			case FROM_TURN__PROP: return getFromTurn();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SESSION_ID__PROP: internalSetSessionId((String) value); break;
			case FROM_TURN__PROP: internalSetFromTurn((int) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.TruncateRequest readTruncateRequest(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.TruncateRequest result = new com.top_logic.ai.chat.api.messages.TruncateRequest();
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
		out.name(SESSION_ID__PROP);
		out.value(getSessionId());
		out.name(FROM_TURN__PROP);
		out.value(getFromTurn());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SESSION_ID__PROP: setSessionId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case FROM_TURN__PROP: setFromTurn(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
