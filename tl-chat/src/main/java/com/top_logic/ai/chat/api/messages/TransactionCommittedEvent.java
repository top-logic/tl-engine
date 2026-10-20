package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for transaction committed.
 */
public class TransactionCommittedEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.TransactionCommittedEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.TransactionCommittedEvent create() {
		return new com.top_logic.ai.chat.api.messages.TransactionCommittedEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.TransactionCommittedEvent} type in JSON format. */
	public static final String TRANSACTION_COMMITTED_EVENT__TYPE = "TransactionCommittedEvent";

	/** @see #getRevisionNumber() */
	public static final String REVISION_NUMBER__PROP = "revisionNumber";

	private long _revisionNumber = 0L;

	/**
	 * Creates a {@link TransactionCommittedEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.TransactionCommittedEvent#create()
	 */
	protected TransactionCommittedEvent() {
		super();
	}

	/**
	 * Revision number, -1 if no changes.
	 */
	public final long getRevisionNumber() {
		return _revisionNumber;
	}

	/**
	 * @see #getRevisionNumber()
	 */
	public com.top_logic.ai.chat.api.messages.TransactionCommittedEvent setRevisionNumber(long value) {
		internalSetRevisionNumber(value);
		return this;
	}

	/** Internal setter for {@link #getRevisionNumber()} without chain call utility. */
	protected final void internalSetRevisionNumber(long value) {
		_revisionNumber = value;
	}

	@Override
	public String jsonType() {
		return TRANSACTION_COMMITTED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			REVISION_NUMBER__PROP);
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
			case REVISION_NUMBER__PROP: return getRevisionNumber();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case REVISION_NUMBER__PROP: internalSetRevisionNumber((long) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.TransactionCommittedEvent readTransactionCommittedEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.TransactionCommittedEvent result = new com.top_logic.ai.chat.api.messages.TransactionCommittedEvent();
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
		out.name(REVISION_NUMBER__PROP);
		out.value(getRevisionNumber());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case REVISION_NUMBER__PROP: setRevisionNumber(in.nextLong()); break;
			default: super.readField(in, field);
		}
	}

}
