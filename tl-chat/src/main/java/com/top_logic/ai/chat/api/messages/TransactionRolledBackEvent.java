package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for transaction rolled back.
 */
public class TransactionRolledBackEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.TransactionRolledBackEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.TransactionRolledBackEvent create() {
		return new com.top_logic.ai.chat.api.messages.TransactionRolledBackEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.TransactionRolledBackEvent} type in JSON format. */
	public static final String TRANSACTION_ROLLED_BACK_EVENT__TYPE = "TransactionRolledBackEvent";

	/** @see #getReason() */
	public static final String REASON__PROP = "reason";

	private String _reason = "";

	/**
	 * Creates a {@link TransactionRolledBackEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.TransactionRolledBackEvent#create()
	 */
	protected TransactionRolledBackEvent() {
		super();
	}

	/**
	 * Rollback reason.
	 */
	public final String getReason() {
		return _reason;
	}

	/**
	 * @see #getReason()
	 */
	public com.top_logic.ai.chat.api.messages.TransactionRolledBackEvent setReason(String value) {
		internalSetReason(value);
		return this;
	}

	/** Internal setter for {@link #getReason()} without chain call utility. */
	protected final void internalSetReason(String value) {
		_reason = value;
	}

	@Override
	public String jsonType() {
		return TRANSACTION_ROLLED_BACK_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			REASON__PROP);
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
			case REASON__PROP: return getReason();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case REASON__PROP: internalSetReason((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.TransactionRolledBackEvent readTransactionRolledBackEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.TransactionRolledBackEvent result = new com.top_logic.ai.chat.api.messages.TransactionRolledBackEvent();
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
		out.name(REASON__PROP);
		out.value(getReason());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case REASON__PROP: setReason(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
