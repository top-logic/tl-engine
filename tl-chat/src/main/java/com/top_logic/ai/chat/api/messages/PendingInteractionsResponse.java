package com.top_logic.ai.chat.api.messages;

/**
 * Response body for GET /chat/interactions: pending requests across all of the current
 * user's chat sessions, oldest first.
 */
public class PendingInteractionsResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.PendingInteractionsResponse} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.PendingInteractionsResponse create() {
		return new com.top_logic.ai.chat.api.messages.PendingInteractionsResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.PendingInteractionsResponse} type in JSON format. */
	public static final String PENDING_INTERACTIONS_RESPONSE__TYPE = "PendingInteractionsResponse";

	/** @see #getInteractions() */
	public static final String INTERACTIONS__PROP = "interactions";

	private final java.util.List<com.top_logic.ai.chat.api.messages.PendingInteractionDto> _interactions = new java.util.ArrayList<>();

	/**
	 * Creates a {@link PendingInteractionsResponse} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.PendingInteractionsResponse#create()
	 */
	protected PendingInteractionsResponse() {
		super();
	}

	/**
	 * All pending requests awaiting the current user.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.PendingInteractionDto> getInteractions() {
		return _interactions;
	}

	/**
	 * @see #getInteractions()
	 */
	public com.top_logic.ai.chat.api.messages.PendingInteractionsResponse setInteractions(java.util.List<? extends com.top_logic.ai.chat.api.messages.PendingInteractionDto> value) {
		internalSetInteractions(value);
		return this;
	}

	/** Internal setter for {@link #getInteractions()} without chain call utility. */
	protected final void internalSetInteractions(java.util.List<? extends com.top_logic.ai.chat.api.messages.PendingInteractionDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'interactions' cannot be null.");
		_interactions.clear();
		_interactions.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getInteractions()} list.
	 */
	public com.top_logic.ai.chat.api.messages.PendingInteractionsResponse addInteraction(com.top_logic.ai.chat.api.messages.PendingInteractionDto value) {
		internalAddInteraction(value);
		return this;
	}

	/** Implementation of {@link #addInteraction(com.top_logic.ai.chat.api.messages.PendingInteractionDto)} without chain call utility. */
	protected final void internalAddInteraction(com.top_logic.ai.chat.api.messages.PendingInteractionDto value) {
		_interactions.add(value);
	}

	/**
	 * Removes a value from the {@link #getInteractions()} list.
	 */
	public final void removeInteraction(com.top_logic.ai.chat.api.messages.PendingInteractionDto value) {
		_interactions.remove(value);
	}

	@Override
	public String jsonType() {
		return PENDING_INTERACTIONS_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			INTERACTIONS__PROP);
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
			case INTERACTIONS__PROP: return getInteractions();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case INTERACTIONS__PROP: internalSetInteractions(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.PendingInteractionDto.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.PendingInteractionsResponse readPendingInteractionsResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.PendingInteractionsResponse result = new com.top_logic.ai.chat.api.messages.PendingInteractionsResponse();
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
		out.name(INTERACTIONS__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.PendingInteractionDto x : getInteractions()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case INTERACTIONS__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.PendingInteractionDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.PendingInteractionDto.readPendingInteractionDto(in));
				}
				in.endArray();
				setInteractions(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
