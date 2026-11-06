package com.top_logic.ai.chat.api.messages;

/**
 * SSE event saying that the run of a chat waits for a free worker of the installation, and where in
 * the queue; or, with position 0, that it waits no longer.
 */
public class RunWaitingEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.RunWaitingEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.RunWaitingEvent create() {
		return new com.top_logic.ai.chat.api.messages.RunWaitingEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.RunWaitingEvent} type in JSON format. */
	public static final String RUN_WAITING_EVENT__TYPE = "RunWaitingEvent";

	/** @see #getSessionId() */
	public static final String SESSION_ID__PROP = "sessionId";

	/** @see #getPosition() */
	public static final String POSITION__PROP = "position";

	/** @see #getText() */
	public static final String TEXT__PROP = "text";

	private String _sessionId = "";

	private int _position = 0;

	private String _text = "";

	/**
	 * Creates a {@link RunWaitingEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.RunWaitingEvent#create()
	 */
	protected RunWaitingEvent() {
		super();
	}

	/**
	 * The chat whose run waits.
	 */
	public final String getSessionId() {
		return _sessionId;
	}

	/**
	 * @see #getSessionId()
	 */
	public com.top_logic.ai.chat.api.messages.RunWaitingEvent setSessionId(String value) {
		internalSetSessionId(value);
		return this;
	}

	/** Internal setter for {@link #getSessionId()} without chain call utility. */
	protected final void internalSetSessionId(String value) {
		_sessionId = value;
	}

	/**
	 * The run's place among the runs waiting for a free worker, counted from 1; 0 once it no longer
	 * waits - it started, or it was cancelled.
	 */
	public final int getPosition() {
		return _position;
	}

	/**
	 * @see #getPosition()
	 */
	public com.top_logic.ai.chat.api.messages.RunWaitingEvent setPosition(int value) {
		internalSetPosition(value);
		return this;
	}

	/** Internal setter for {@link #getPosition()} without chain call utility. */
	protected final void internalSetPosition(int value) {
		_position = value;
	}

	/**
	 * The notice line to show while the run waits; empty when {@link #getPosition()} is 0.
	 */
	public final String getText() {
		return _text;
	}

	/**
	 * @see #getText()
	 */
	public com.top_logic.ai.chat.api.messages.RunWaitingEvent setText(String value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(String value) {
		_text = value;
	}

	@Override
	public String jsonType() {
		return RUN_WAITING_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SESSION_ID__PROP, 
			POSITION__PROP, 
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
			case SESSION_ID__PROP: return getSessionId();
			case POSITION__PROP: return getPosition();
			case TEXT__PROP: return getText();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SESSION_ID__PROP: internalSetSessionId((String) value); break;
			case POSITION__PROP: internalSetPosition((int) value); break;
			case TEXT__PROP: internalSetText((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.RunWaitingEvent readRunWaitingEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.RunWaitingEvent result = new com.top_logic.ai.chat.api.messages.RunWaitingEvent();
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
		out.name(POSITION__PROP);
		out.value(getPosition());
		out.name(TEXT__PROP);
		out.value(getText());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SESSION_ID__PROP: setSessionId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case POSITION__PROP: setPosition(in.nextInt()); break;
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
