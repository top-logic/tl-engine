package com.top_logic.ai.chat.api.messages;

/**
 * Request body for POST /chat/sessions.
 */
public class CreateSessionRequest extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.CreateSessionRequest} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.CreateSessionRequest create() {
		return new com.top_logic.ai.chat.api.messages.CreateSessionRequest();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.CreateSessionRequest} type in JSON format. */
	public static final String CREATE_SESSION_REQUEST__TYPE = "CreateSessionRequest";

	/** @see #getTitle() */
	public static final String TITLE__PROP = "title";

	private String _title = "";

	/**
	 * Creates a {@link CreateSessionRequest} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.CreateSessionRequest#create()
	 */
	protected CreateSessionRequest() {
		super();
	}

	/**
	 * Title for the new session.
	 */
	public final String getTitle() {
		return _title;
	}

	/**
	 * @see #getTitle()
	 */
	public com.top_logic.ai.chat.api.messages.CreateSessionRequest setTitle(String value) {
		internalSetTitle(value);
		return this;
	}

	/** Internal setter for {@link #getTitle()} without chain call utility. */
	protected final void internalSetTitle(String value) {
		_title = value;
	}

	@Override
	public String jsonType() {
		return CREATE_SESSION_REQUEST__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TITLE__PROP);
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
			case TITLE__PROP: return getTitle();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TITLE__PROP: internalSetTitle((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.CreateSessionRequest readCreateSessionRequest(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.CreateSessionRequest result = new com.top_logic.ai.chat.api.messages.CreateSessionRequest();
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
		out.name(TITLE__PROP);
		out.value(getTitle());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TITLE__PROP: setTitle(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
