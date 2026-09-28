package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for streaming text delta.
 */
public class TextDeltaEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.TextDeltaEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.TextDeltaEvent create() {
		return new com.top_logic.ai.chat.api.messages.TextDeltaEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.TextDeltaEvent} type in JSON format. */
	public static final String TEXT_DELTA_EVENT__TYPE = "TextDeltaEvent";

	/** @see #getText() */
	public static final String TEXT__PROP = "text";

	/** @see #getEnd() */
	public static final String END__PROP = "end";

	/** @see #getSessionId() */
	public static final String SESSION_ID__PROP = "sessionId";

	private String _text = "";

	private long _end = 0L;

	private String _sessionId = "";

	/**
	 * Creates a {@link TextDeltaEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.TextDeltaEvent#create()
	 */
	protected TextDeltaEvent() {
		super();
	}

	/**
	 * The text chunk.
	 */
	public final String getText() {
		return _text;
	}

	/**
	 * @see #getText()
	 */
	public com.top_logic.ai.chat.api.messages.TextDeltaEvent setText(String value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(String value) {
		_text = value;
	}

	/**
	 * How many characters of text the run has streamed, this chunk included; zero where the
	 * position is not known, as for a delegated agent's text.
	 */
	public final long getEnd() {
		return _end;
	}

	/**
	 * @see #getEnd()
	 */
	public com.top_logic.ai.chat.api.messages.TextDeltaEvent setEnd(long value) {
		internalSetEnd(value);
		return this;
	}

	/** Internal setter for {@link #getEnd()} without chain call utility. */
	protected final void internalSetEnd(long value) {
		_end = value;
	}

	/**
	 * The chat the run writes into, so a window that is opening another chat meanwhile does not
	 * take it for that chat's text. Empty when it is not known.
	 */
	public final String getSessionId() {
		return _sessionId;
	}

	/**
	 * @see #getSessionId()
	 */
	public com.top_logic.ai.chat.api.messages.TextDeltaEvent setSessionId(String value) {
		internalSetSessionId(value);
		return this;
	}

	/** Internal setter for {@link #getSessionId()} without chain call utility. */
	protected final void internalSetSessionId(String value) {
		_sessionId = value;
	}

	@Override
	public String jsonType() {
		return TEXT_DELTA_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TEXT__PROP, 
			END__PROP, 
			SESSION_ID__PROP);
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
			case TEXT__PROP: return getText();
			case END__PROP: return getEnd();
			case SESSION_ID__PROP: return getSessionId();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TEXT__PROP: internalSetText((String) value); break;
			case END__PROP: internalSetEnd((long) value); break;
			case SESSION_ID__PROP: internalSetSessionId((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.TextDeltaEvent readTextDeltaEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.TextDeltaEvent result = new com.top_logic.ai.chat.api.messages.TextDeltaEvent();
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
		out.name(TEXT__PROP);
		out.value(getText());
		out.name(END__PROP);
		out.value(getEnd());
		out.name(SESSION_ID__PROP);
		out.value(getSessionId());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case END__PROP: setEnd(in.nextLong()); break;
			case SESSION_ID__PROP: setSessionId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
