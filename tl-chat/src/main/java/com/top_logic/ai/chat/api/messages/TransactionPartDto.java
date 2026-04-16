package com.top_logic.ai.chat.api.messages;

/**
 * Transaction event part.
 */
public class TransactionPartDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.TransactionPartDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.TransactionPartDto create() {
		return new com.top_logic.ai.chat.api.messages.TransactionPartDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.TransactionPartDto} type in JSON format. */
	public static final String TRANSACTION_PART_DTO__TYPE = "TransactionPartDto";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getRevisionNumber() */
	public static final String REVISION_NUMBER__PROP = "revisionNumber";

	/** @see #getReason() */
	public static final String REASON__PROP = "reason";

	private String _type = "";

	private long _revisionNumber = 0L;

	private String _reason = "";

	/**
	 * Creates a {@link TransactionPartDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.TransactionPartDto#create()
	 */
	protected TransactionPartDto() {
		super();
	}

	/**
	 * The transaction type.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.ai.chat.api.messages.TransactionPartDto setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * The revision number.
	 */
	public final long getRevisionNumber() {
		return _revisionNumber;
	}

	/**
	 * @see #getRevisionNumber()
	 */
	public com.top_logic.ai.chat.api.messages.TransactionPartDto setRevisionNumber(long value) {
		internalSetRevisionNumber(value);
		return this;
	}

	/** Internal setter for {@link #getRevisionNumber()} without chain call utility. */
	protected final void internalSetRevisionNumber(long value) {
		_revisionNumber = value;
	}

	/**
	 * Optional reason for the transaction.
	 */
	public final String getReason() {
		return _reason;
	}

	/**
	 * @see #getReason()
	 */
	public com.top_logic.ai.chat.api.messages.TransactionPartDto setReason(String value) {
		internalSetReason(value);
		return this;
	}

	/** Internal setter for {@link #getReason()} without chain call utility. */
	protected final void internalSetReason(String value) {
		_reason = value;
	}

	@Override
	public String jsonType() {
		return TRANSACTION_PART_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TYPE__PROP, 
			REVISION_NUMBER__PROP, 
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
			case TYPE__PROP: return getType();
			case REVISION_NUMBER__PROP: return getRevisionNumber();
			case REASON__PROP: return getReason();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TYPE__PROP: internalSetType((String) value); break;
			case REVISION_NUMBER__PROP: internalSetRevisionNumber((long) value); break;
			case REASON__PROP: internalSetReason((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.TransactionPartDto readTransactionPartDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.TransactionPartDto result = new com.top_logic.ai.chat.api.messages.TransactionPartDto();
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
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(REVISION_NUMBER__PROP);
		out.value(getRevisionNumber());
		out.name(REASON__PROP);
		out.value(getReason());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REVISION_NUMBER__PROP: setRevisionNumber(in.nextLong()); break;
			case REASON__PROP: setReason(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
