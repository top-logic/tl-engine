package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for turn completed.
 */
public class TurnCompletedEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.TurnCompletedEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.TurnCompletedEvent create() {
		return new com.top_logic.ai.chat.api.messages.TurnCompletedEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.TurnCompletedEvent} type in JSON format. */
	public static final String TURN_COMPLETED_EVENT__TYPE = "TurnCompletedEvent";

	/** @see #getTurnNumber() */
	public static final String TURN_NUMBER__PROP = "turnNumber";

	/** @see #getTurnUsage() */
	public static final String TURN_USAGE__PROP = "turnUsage";

	/** @see #getContextWindow() */
	public static final String CONTEXT_WINDOW__PROP = "contextWindow";

	private int _turnNumber = 0;

	private com.top_logic.ai.chat.api.messages.TokenUsageDto _turnUsage = null;

	private int _contextWindow = 0;

	/**
	 * Creates a {@link TurnCompletedEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.TurnCompletedEvent#create()
	 */
	protected TurnCompletedEvent() {
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
	public com.top_logic.ai.chat.api.messages.TurnCompletedEvent setTurnNumber(int value) {
		internalSetTurnNumber(value);
		return this;
	}

	/** Internal setter for {@link #getTurnNumber()} without chain call utility. */
	protected final void internalSetTurnNumber(int value) {
		_turnNumber = value;
	}

	/**
	 * Token usage for this turn.
	 */
	public final com.top_logic.ai.chat.api.messages.TokenUsageDto getTurnUsage() {
		return _turnUsage;
	}

	/**
	 * @see #getTurnUsage()
	 */
	public com.top_logic.ai.chat.api.messages.TurnCompletedEvent setTurnUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto value) {
		internalSetTurnUsage(value);
		return this;
	}

	/** Internal setter for {@link #getTurnUsage()} without chain call utility. */
	protected final void internalSetTurnUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto value) {
		_turnUsage = value;
	}

	/**
	 * Checks, whether {@link #getTurnUsage()} has a value.
	 */
	public final boolean hasTurnUsage() {
		return _turnUsage != null;
	}

	/**
	 * Model context window in tokens, repeated here so the client can size the context ring from
	 * the same event that carries the provider-reported prompt size.
	 */
	public final int getContextWindow() {
		return _contextWindow;
	}

	/**
	 * @see #getContextWindow()
	 */
	public com.top_logic.ai.chat.api.messages.TurnCompletedEvent setContextWindow(int value) {
		internalSetContextWindow(value);
		return this;
	}

	/** Internal setter for {@link #getContextWindow()} without chain call utility. */
	protected final void internalSetContextWindow(int value) {
		_contextWindow = value;
	}

	@Override
	public String jsonType() {
		return TURN_COMPLETED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TURN_NUMBER__PROP, 
			TURN_USAGE__PROP, 
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
			case TURN_USAGE__PROP: return getTurnUsage();
			case CONTEXT_WINDOW__PROP: return getContextWindow();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TURN_NUMBER__PROP: internalSetTurnNumber((int) value); break;
			case TURN_USAGE__PROP: internalSetTurnUsage((com.top_logic.ai.chat.api.messages.TokenUsageDto) value); break;
			case CONTEXT_WINDOW__PROP: internalSetContextWindow((int) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.TurnCompletedEvent readTurnCompletedEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.TurnCompletedEvent result = new com.top_logic.ai.chat.api.messages.TurnCompletedEvent();
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
		if (hasTurnUsage()) {
			out.name(TURN_USAGE__PROP);
			getTurnUsage().writeTo(out);
		}
		out.name(CONTEXT_WINDOW__PROP);
		out.value(getContextWindow());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TURN_NUMBER__PROP: setTurnNumber(in.nextInt()); break;
			case TURN_USAGE__PROP: setTurnUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto.readTokenUsageDto(in)); break;
			case CONTEXT_WINDOW__PROP: setContextWindow(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
