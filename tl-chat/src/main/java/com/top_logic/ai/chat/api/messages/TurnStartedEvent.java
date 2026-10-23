package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for turn started.
 */
public class TurnStartedEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.TurnStartedEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.TurnStartedEvent create() {
		return new com.top_logic.ai.chat.api.messages.TurnStartedEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.TurnStartedEvent} type in JSON format. */
	public static final String TURN_STARTED_EVENT__TYPE = "TurnStartedEvent";

	/** @see #getTurnNumber() */
	public static final String TURN_NUMBER__PROP = "turnNumber";

	/** @see #getUsedTokens() */
	public static final String USED_TOKENS__PROP = "usedTokens";

	/** @see #getContextWindow() */
	public static final String CONTEXT_WINDOW__PROP = "contextWindow";

	private int _turnNumber = 0;

	private int _usedTokens = 0;

	private int _contextWindow = 0;

	/**
	 * Creates a {@link TurnStartedEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.TurnStartedEvent#create()
	 */
	protected TurnStartedEvent() {
		super();
	}

	/**
	 * 1-based turn number.
	 */
	public final int getTurnNumber() {
		return _turnNumber;
	}

	/**
	 * @see #getTurnNumber()
	 */
	public com.top_logic.ai.chat.api.messages.TurnStartedEvent setTurnNumber(int value) {
		internalSetTurnNumber(value);
		return this;
	}

	/** Internal setter for {@link #getTurnNumber()} without chain call utility. */
	protected final void internalSetTurnNumber(int value) {
		_turnNumber = value;
	}

	/**
	 * Estimated tokens already in the conversation.
	 */
	public final int getUsedTokens() {
		return _usedTokens;
	}

	/**
	 * @see #getUsedTokens()
	 */
	public com.top_logic.ai.chat.api.messages.TurnStartedEvent setUsedTokens(int value) {
		internalSetUsedTokens(value);
		return this;
	}

	/** Internal setter for {@link #getUsedTokens()} without chain call utility. */
	protected final void internalSetUsedTokens(int value) {
		_usedTokens = value;
	}

	/**
	 * Model context window in tokens.
	 */
	public final int getContextWindow() {
		return _contextWindow;
	}

	/**
	 * @see #getContextWindow()
	 */
	public com.top_logic.ai.chat.api.messages.TurnStartedEvent setContextWindow(int value) {
		internalSetContextWindow(value);
		return this;
	}

	/** Internal setter for {@link #getContextWindow()} without chain call utility. */
	protected final void internalSetContextWindow(int value) {
		_contextWindow = value;
	}

	@Override
	public String jsonType() {
		return TURN_STARTED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TURN_NUMBER__PROP, 
			USED_TOKENS__PROP, 
			CONTEXT_WINDOW__PROP);
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
			case TURN_NUMBER__PROP: return getTurnNumber();
			case USED_TOKENS__PROP: return getUsedTokens();
			case CONTEXT_WINDOW__PROP: return getContextWindow();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TURN_NUMBER__PROP: internalSetTurnNumber((int) value); break;
			case USED_TOKENS__PROP: internalSetUsedTokens((int) value); break;
			case CONTEXT_WINDOW__PROP: internalSetContextWindow((int) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.TurnStartedEvent readTurnStartedEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.TurnStartedEvent result = new com.top_logic.ai.chat.api.messages.TurnStartedEvent();
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
		out.name(TURN_NUMBER__PROP);
		out.value(getTurnNumber());
		out.name(USED_TOKENS__PROP);
		out.value(getUsedTokens());
		out.name(CONTEXT_WINDOW__PROP);
		out.value(getContextWindow());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TURN_NUMBER__PROP: setTurnNumber(in.nextInt()); break;
			case USED_TOKENS__PROP: setUsedTokens(in.nextInt()); break;
			case CONTEXT_WINDOW__PROP: setContextWindow(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
