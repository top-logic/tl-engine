package com.top_logic.ai.chat.api.messages;

/**
 * SSE event saying that subagents' news waits for the user's next message, because the lead has been
 * started for such news as often as it may between two messages of the user.
 */
public class LeadWakesExhaustedEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.LeadWakesExhaustedEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.LeadWakesExhaustedEvent create() {
		return new com.top_logic.ai.chat.api.messages.LeadWakesExhaustedEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.LeadWakesExhaustedEvent} type in JSON format. */
	public static final String LEAD_WAKES_EXHAUSTED_EVENT__TYPE = "LeadWakesExhaustedEvent";

	/** @see #getWakes() */
	public static final String WAKES__PROP = "wakes";

	private int _wakes = 0;

	/**
	 * Creates a {@link LeadWakesExhaustedEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.LeadWakesExhaustedEvent#create()
	 */
	protected LeadWakesExhaustedEvent() {
		super();
	}

	/**
	 * How many turns were started for subagents' news since the user's last message.
	 */
	public final int getWakes() {
		return _wakes;
	}

	/**
	 * @see #getWakes()
	 */
	public com.top_logic.ai.chat.api.messages.LeadWakesExhaustedEvent setWakes(int value) {
		internalSetWakes(value);
		return this;
	}

	/** Internal setter for {@link #getWakes()} without chain call utility. */
	protected final void internalSetWakes(int value) {
		_wakes = value;
	}

	@Override
	public String jsonType() {
		return LEAD_WAKES_EXHAUSTED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			WAKES__PROP);
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
			case WAKES__PROP: return getWakes();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case WAKES__PROP: internalSetWakes((int) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.LeadWakesExhaustedEvent readLeadWakesExhaustedEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.LeadWakesExhaustedEvent result = new com.top_logic.ai.chat.api.messages.LeadWakesExhaustedEvent();
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
		out.name(WAKES__PROP);
		out.value(getWakes());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case WAKES__PROP: setWakes(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
