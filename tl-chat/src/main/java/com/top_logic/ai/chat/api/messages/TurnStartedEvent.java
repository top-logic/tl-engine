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

	private int _turnNumber = 0;

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

	@Override
	public String jsonType() {
		return TURN_STARTED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TURN_NUMBER__PROP);
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
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TURN_NUMBER__PROP: internalSetTurnNumber((int) value); break;
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
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TURN_NUMBER__PROP: setTurnNumber(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
