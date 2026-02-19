package com.top_logic.ai.chat.api.messages;

/**
 * Full persisted chat state: ordered list of entries (messages interleaved with task snapshots).
 */
public class PersistedChat extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.PersistedChat} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.PersistedChat create() {
		return new com.top_logic.ai.chat.api.messages.PersistedChat();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.PersistedChat} type in JSON format. */
	public static final String PERSISTED_CHAT__TYPE = "PersistedChat";

	/** @see #getEntries() */
	public static final String ENTRIES__PROP = "entries";

	private final java.util.List<com.top_logic.ai.chat.api.messages.PersistedChatEntry> _entries = new java.util.ArrayList<>();

	/**
	 * Creates a {@link PersistedChat} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.PersistedChat#create()
	 */
	protected PersistedChat() {
		super();
	}

	/**
	 * All entries in display order.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.PersistedChatEntry> getEntries() {
		return _entries;
	}

	/**
	 * @see #getEntries()
	 */
	public com.top_logic.ai.chat.api.messages.PersistedChat setEntries(java.util.List<? extends com.top_logic.ai.chat.api.messages.PersistedChatEntry> value) {
		internalSetEntries(value);
		return this;
	}

	/** Internal setter for {@link #getEntries()} without chain call utility. */
	protected final void internalSetEntries(java.util.List<? extends com.top_logic.ai.chat.api.messages.PersistedChatEntry> value) {
		if (value == null) throw new IllegalArgumentException("Property 'entries' cannot be null.");
		_entries.clear();
		_entries.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getEntries()} list.
	 */
	public com.top_logic.ai.chat.api.messages.PersistedChat addEntry(com.top_logic.ai.chat.api.messages.PersistedChatEntry value) {
		internalAddEntry(value);
		return this;
	}

	/** Implementation of {@link #addEntry(com.top_logic.ai.chat.api.messages.PersistedChatEntry)} without chain call utility. */
	protected final void internalAddEntry(com.top_logic.ai.chat.api.messages.PersistedChatEntry value) {
		_entries.add(value);
	}

	/**
	 * Removes a value from the {@link #getEntries()} list.
	 */
	public final void removeEntry(com.top_logic.ai.chat.api.messages.PersistedChatEntry value) {
		_entries.remove(value);
	}

	@Override
	public String jsonType() {
		return PERSISTED_CHAT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ENTRIES__PROP);
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
			case ENTRIES__PROP: return getEntries();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ENTRIES__PROP: internalSetEntries(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.PersistedChatEntry.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.PersistedChat readPersistedChat(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.PersistedChat result = new com.top_logic.ai.chat.api.messages.PersistedChat();
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
		out.name(ENTRIES__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.PersistedChatEntry x : getEntries()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ENTRIES__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.PersistedChatEntry> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.PersistedChatEntry.readPersistedChatEntry(in));
				}
				in.endArray();
				setEntries(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
