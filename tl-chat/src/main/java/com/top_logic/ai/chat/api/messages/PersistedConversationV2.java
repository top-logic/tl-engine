package com.top_logic.ai.chat.api.messages;

/**
 * A complete conversation in the turn-based persistence format.
 */
public class PersistedConversationV2 extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.PersistedConversationV2} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.PersistedConversationV2 create() {
		return new com.top_logic.ai.chat.api.messages.PersistedConversationV2();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.PersistedConversationV2} type in JSON format. */
	public static final String PERSISTED_CONVERSATION_V_2__TYPE = "PersistedConversationV2";

	/** @see #getTurns() */
	public static final String TURNS__PROP = "turns";

	/** @see #getMetadata() */
	public static final String METADATA__PROP = "metadata";

	private final java.util.List<com.top_logic.ai.chat.api.messages.PersistedTurn> _turns = new java.util.ArrayList<>();

	private com.top_logic.ai.chat.api.messages.SessionMetadataV2 _metadata = null;

	/**
	 * Creates a {@link PersistedConversationV2} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.PersistedConversationV2#create()
	 */
	protected PersistedConversationV2() {
		super();
	}

	/**
	 * The turns in this conversation.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.PersistedTurn> getTurns() {
		return _turns;
	}

	/**
	 * @see #getTurns()
	 */
	public com.top_logic.ai.chat.api.messages.PersistedConversationV2 setTurns(java.util.List<? extends com.top_logic.ai.chat.api.messages.PersistedTurn> value) {
		internalSetTurns(value);
		return this;
	}

	/** Internal setter for {@link #getTurns()} without chain call utility. */
	protected final void internalSetTurns(java.util.List<? extends com.top_logic.ai.chat.api.messages.PersistedTurn> value) {
		if (value == null) throw new IllegalArgumentException("Property 'turns' cannot be null.");
		_turns.clear();
		_turns.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getTurns()} list.
	 */
	public com.top_logic.ai.chat.api.messages.PersistedConversationV2 addTurn(com.top_logic.ai.chat.api.messages.PersistedTurn value) {
		internalAddTurn(value);
		return this;
	}

	/** Implementation of {@link #addTurn(com.top_logic.ai.chat.api.messages.PersistedTurn)} without chain call utility. */
	protected final void internalAddTurn(com.top_logic.ai.chat.api.messages.PersistedTurn value) {
		_turns.add(value);
	}

	/**
	 * Removes a value from the {@link #getTurns()} list.
	 */
	public final void removeTurn(com.top_logic.ai.chat.api.messages.PersistedTurn value) {
		_turns.remove(value);
	}

	/**
	 * Session metadata.
	 */
	public final com.top_logic.ai.chat.api.messages.SessionMetadataV2 getMetadata() {
		return _metadata;
	}

	/**
	 * @see #getMetadata()
	 */
	public com.top_logic.ai.chat.api.messages.PersistedConversationV2 setMetadata(com.top_logic.ai.chat.api.messages.SessionMetadataV2 value) {
		internalSetMetadata(value);
		return this;
	}

	/** Internal setter for {@link #getMetadata()} without chain call utility. */
	protected final void internalSetMetadata(com.top_logic.ai.chat.api.messages.SessionMetadataV2 value) {
		_metadata = value;
	}

	/**
	 * Checks, whether {@link #getMetadata()} has a value.
	 */
	public final boolean hasMetadata() {
		return _metadata != null;
	}

	@Override
	public String jsonType() {
		return PERSISTED_CONVERSATION_V_2__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TURNS__PROP, 
			METADATA__PROP);
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
			case TURNS__PROP: return getTurns();
			case METADATA__PROP: return getMetadata();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TURNS__PROP: internalSetTurns(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.PersistedTurn.class, value)); break;
			case METADATA__PROP: internalSetMetadata((com.top_logic.ai.chat.api.messages.SessionMetadataV2) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.PersistedConversationV2 readPersistedConversationV2(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.PersistedConversationV2 result = new com.top_logic.ai.chat.api.messages.PersistedConversationV2();
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
		out.name(TURNS__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.PersistedTurn x : getTurns()) {
			x.writeTo(out);
		}
		out.endArray();
		if (hasMetadata()) {
			out.name(METADATA__PROP);
			getMetadata().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TURNS__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.PersistedTurn> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.PersistedTurn.readPersistedTurn(in));
				}
				in.endArray();
				setTurns(newValue);
			}
			break;
			case METADATA__PROP: setMetadata(com.top_logic.ai.chat.api.messages.SessionMetadataV2.readSessionMetadataV2(in)); break;
			default: super.readField(in, field);
		}
	}

}
