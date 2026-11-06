package com.top_logic.ai.chat.api.messages;

/**
 * Reply to the command of {@code ChatControl} that deletes a chat of the account.
 */
public class SessionDeleteReply extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.SessionDeleteReply} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.SessionDeleteReply create() {
		return new com.top_logic.ai.chat.api.messages.SessionDeleteReply();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.SessionDeleteReply} type in JSON format. */
	public static final String SESSION_DELETE_REPLY__TYPE = "SessionDeleteReply";

	/** @see #isDeleted() */
	public static final String DELETED__PROP = "deleted";

	private boolean _deleted = false;

	/**
	 * Creates a {@link SessionDeleteReply} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.SessionDeleteReply#create()
	 */
	protected SessionDeleteReply() {
		super();
	}

	/**
	 * Whether the chat was deleted.
	 */
	public final boolean isDeleted() {
		return _deleted;
	}

	/**
	 * @see #isDeleted()
	 */
	public com.top_logic.ai.chat.api.messages.SessionDeleteReply setDeleted(boolean value) {
		internalSetDeleted(value);
		return this;
	}

	/** Internal setter for {@link #isDeleted()} without chain call utility. */
	protected final void internalSetDeleted(boolean value) {
		_deleted = value;
	}

	@Override
	public String jsonType() {
		return SESSION_DELETE_REPLY__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			DELETED__PROP);
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
			case DELETED__PROP: return isDeleted();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case DELETED__PROP: internalSetDeleted((boolean) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.SessionDeleteReply readSessionDeleteReply(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.SessionDeleteReply result = new com.top_logic.ai.chat.api.messages.SessionDeleteReply();
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
		out.name(DELETED__PROP);
		out.value(isDeleted());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case DELETED__PROP: setDeleted(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
