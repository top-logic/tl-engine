package com.top_logic.ai.chat.api.messages;

/**
 * Reply to the command of {@code ChatControl} that opens a chat of the account in the window's
 * conversation.
 */
public class SessionOpenReply extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.SessionOpenReply} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.SessionOpenReply create() {
		return new com.top_logic.ai.chat.api.messages.SessionOpenReply();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.SessionOpenReply} type in JSON format. */
	public static final String SESSION_OPEN_REPLY__TYPE = "SessionOpenReply";

	/** @see #isOpened() */
	public static final String OPENED__PROP = "opened";

	/** @see #getRunState() */
	public static final String RUN_STATE__PROP = "runState";

	private boolean _opened = false;

	private com.top_logic.ai.chat.api.messages.RunStateEvent _runState = null;

	/**
	 * Creates a {@link SessionOpenReply} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.SessionOpenReply#create()
	 */
	protected SessionOpenReply() {
		super();
	}

	/**
	 * Whether the chat was opened.
	 */
	public final boolean isOpened() {
		return _opened;
	}

	/**
	 * @see #isOpened()
	 */
	public com.top_logic.ai.chat.api.messages.SessionOpenReply setOpened(boolean value) {
		internalSetOpened(value);
		return this;
	}

	/** Internal setter for {@link #isOpened()} without chain call utility. */
	protected final void internalSetOpened(boolean value) {
		_opened = value;
	}

	/**
	 * The opened chat's run state, as the {@code run_state} frame of the stream carries it; absent
	 * for a chat that was not opened.
	 *
	 * <p>
	 * In the reply rather than on the stream, because the browser applies it once the transcript
	 * it belongs to is on screen, which is after the command has been answered.
	 * </p>
	 */
	public final com.top_logic.ai.chat.api.messages.RunStateEvent getRunState() {
		return _runState;
	}

	/**
	 * @see #getRunState()
	 */
	public com.top_logic.ai.chat.api.messages.SessionOpenReply setRunState(com.top_logic.ai.chat.api.messages.RunStateEvent value) {
		internalSetRunState(value);
		return this;
	}

	/** Internal setter for {@link #getRunState()} without chain call utility. */
	protected final void internalSetRunState(com.top_logic.ai.chat.api.messages.RunStateEvent value) {
		_runState = value;
	}

	/**
	 * Checks, whether {@link #getRunState()} has a value.
	 */
	public final boolean hasRunState() {
		return _runState != null;
	}

	@Override
	public String jsonType() {
		return SESSION_OPEN_REPLY__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			OPENED__PROP, 
			RUN_STATE__PROP);
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
			case OPENED__PROP: return isOpened();
			case RUN_STATE__PROP: return getRunState();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case OPENED__PROP: internalSetOpened((boolean) value); break;
			case RUN_STATE__PROP: internalSetRunState((com.top_logic.ai.chat.api.messages.RunStateEvent) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.SessionOpenReply readSessionOpenReply(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.SessionOpenReply result = new com.top_logic.ai.chat.api.messages.SessionOpenReply();
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
		out.name(OPENED__PROP);
		out.value(isOpened());
		if (hasRunState()) {
			out.name(RUN_STATE__PROP);
			getRunState().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case OPENED__PROP: setOpened(in.nextBoolean()); break;
			case RUN_STATE__PROP: setRunState(com.top_logic.ai.chat.api.messages.RunStateEvent.readRunStateEvent(in)); break;
			default: super.readField(in, field);
		}
	}

}
