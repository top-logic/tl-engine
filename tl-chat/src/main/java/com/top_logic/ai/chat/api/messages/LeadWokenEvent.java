package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for a lead turn that starts without a message of the user, because subagents finished
 * or asked the lead something while no turn of it ran. Precedes the turn's {@code run_started}.
 */
public class LeadWokenEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.LeadWokenEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.LeadWokenEvent create() {
		return new com.top_logic.ai.chat.api.messages.LeadWokenEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.LeadWokenEvent} type in JSON format. */
	public static final String LEAD_WOKEN_EVENT__TYPE = "LeadWokenEvent";

	/** @see #getFinished() */
	public static final String FINISHED__PROP = "finished";

	/** @see #getAsking() */
	public static final String ASKING__PROP = "asking";

	private int _finished = 0;

	private int _asking = 0;

	/**
	 * Creates a {@link LeadWokenEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.LeadWokenEvent#create()
	 */
	protected LeadWokenEvent() {
		super();
	}

	/**
	 * How many subagents' results the turn opens with.
	 */
	public final int getFinished() {
		return _finished;
	}

	/**
	 * @see #getFinished()
	 */
	public com.top_logic.ai.chat.api.messages.LeadWokenEvent setFinished(int value) {
		internalSetFinished(value);
		return this;
	}

	/** Internal setter for {@link #getFinished()} without chain call utility. */
	protected final void internalSetFinished(int value) {
		_finished = value;
	}

	/**
	 * How many subagents' questions it opens with.
	 */
	public final int getAsking() {
		return _asking;
	}

	/**
	 * @see #getAsking()
	 */
	public com.top_logic.ai.chat.api.messages.LeadWokenEvent setAsking(int value) {
		internalSetAsking(value);
		return this;
	}

	/** Internal setter for {@link #getAsking()} without chain call utility. */
	protected final void internalSetAsking(int value) {
		_asking = value;
	}

	@Override
	public String jsonType() {
		return LEAD_WOKEN_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			FINISHED__PROP, 
			ASKING__PROP);
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
			case FINISHED__PROP: return getFinished();
			case ASKING__PROP: return getAsking();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case FINISHED__PROP: internalSetFinished((int) value); break;
			case ASKING__PROP: internalSetAsking((int) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.LeadWokenEvent readLeadWokenEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.LeadWokenEvent result = new com.top_logic.ai.chat.api.messages.LeadWokenEvent();
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
		out.name(FINISHED__PROP);
		out.value(getFinished());
		out.name(ASKING__PROP);
		out.value(getAsking());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case FINISHED__PROP: setFinished(in.nextInt()); break;
			case ASKING__PROP: setAsking(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
