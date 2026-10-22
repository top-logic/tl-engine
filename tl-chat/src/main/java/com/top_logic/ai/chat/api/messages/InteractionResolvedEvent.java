package com.top_logic.ai.chat.api.messages;

/**
 * SSE event telling a chat UI that a human-in-the-loop request it may be showing has been answered.
 *
 * <p>
 * Sent to the other windows of the responder's account when one of them answers an approval or a
 * question. The window the run belongs to learns the same thing from the run's own
 * {@code approval_resolved} frame; a window that only listed the request - a second tab, a reloaded
 * page - has no run to hear it from, and would keep showing a card nobody is waiting on.
 * </p>
 */
public class InteractionResolvedEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.InteractionResolvedEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.InteractionResolvedEvent create() {
		return new com.top_logic.ai.chat.api.messages.InteractionResolvedEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.InteractionResolvedEvent} type in JSON format. */
	public static final String INTERACTION_RESOLVED_EVENT__TYPE = "InteractionResolvedEvent";

	/** @see #getRequestId() */
	public static final String REQUEST_ID__PROP = "requestId";

	/** @see #getOutcome() */
	public static final String OUTCOME__PROP = "outcome";

	private String _requestId = "";

	private String _outcome = "";

	/**
	 * Creates a {@link InteractionResolvedEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.InteractionResolvedEvent#create()
	 */
	protected InteractionResolvedEvent() {
		super();
	}

	/**
	 * Correlation id of the approval or question that was answered.
	 */
	public final String getRequestId() {
		return _requestId;
	}

	/**
	 * @see #getRequestId()
	 */
	public com.top_logic.ai.chat.api.messages.InteractionResolvedEvent setRequestId(String value) {
		internalSetRequestId(value);
		return this;
	}

	/** Internal setter for {@link #getRequestId()} without chain call utility. */
	protected final void internalSetRequestId(String value) {
		_requestId = value;
	}

	/**
	 * What the responder decided: "APPROVED", "APPROVED_FOR_SESSION", "REJECTED", or the answer of
	 * a question.
	 */
	public final String getOutcome() {
		return _outcome;
	}

	/**
	 * @see #getOutcome()
	 */
	public com.top_logic.ai.chat.api.messages.InteractionResolvedEvent setOutcome(String value) {
		internalSetOutcome(value);
		return this;
	}

	/** Internal setter for {@link #getOutcome()} without chain call utility. */
	protected final void internalSetOutcome(String value) {
		_outcome = value;
	}

	@Override
	public String jsonType() {
		return INTERACTION_RESOLVED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			REQUEST_ID__PROP, 
			OUTCOME__PROP);
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
			case REQUEST_ID__PROP: return getRequestId();
			case OUTCOME__PROP: return getOutcome();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case REQUEST_ID__PROP: internalSetRequestId((String) value); break;
			case OUTCOME__PROP: internalSetOutcome((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.InteractionResolvedEvent readInteractionResolvedEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.InteractionResolvedEvent result = new com.top_logic.ai.chat.api.messages.InteractionResolvedEvent();
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
		out.name(REQUEST_ID__PROP);
		out.value(getRequestId());
		out.name(OUTCOME__PROP);
		out.value(getOutcome());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case REQUEST_ID__PROP: setRequestId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case OUTCOME__PROP: setOutcome(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
