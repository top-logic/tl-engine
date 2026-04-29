package com.top_logic.ai.chat.api.messages;

/**
 * Error event part.
 */
public class ErrorPartDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ErrorPartDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ErrorPartDto create() {
		return new com.top_logic.ai.chat.api.messages.ErrorPartDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ErrorPartDto} type in JSON format. */
	public static final String ERROR_PART_DTO__TYPE = "ErrorPartDto";

	/** @see #getKind() */
	public static final String KIND__PROP = "kind";

	/** @see #getErrorMessage() */
	public static final String ERROR_MESSAGE__PROP = "errorMessage";

	private String _kind = "";

	private String _errorMessage = "";

	/**
	 * Creates a {@link ErrorPartDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ErrorPartDto#create()
	 */
	protected ErrorPartDto() {
		super();
	}

	/**
	 * The kind of error.
	 */
	public final String getKind() {
		return _kind;
	}

	/**
	 * @see #getKind()
	 */
	public com.top_logic.ai.chat.api.messages.ErrorPartDto setKind(String value) {
		internalSetKind(value);
		return this;
	}

	/** Internal setter for {@link #getKind()} without chain call utility. */
	protected final void internalSetKind(String value) {
		_kind = value;
	}

	/**
	 * The error message text.
	 */
	public final String getErrorMessage() {
		return _errorMessage;
	}

	/**
	 * @see #getErrorMessage()
	 */
	public com.top_logic.ai.chat.api.messages.ErrorPartDto setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	/** Internal setter for {@link #getErrorMessage()} without chain call utility. */
	protected final void internalSetErrorMessage(String value) {
		_errorMessage = value;
	}

	@Override
	public String jsonType() {
		return ERROR_PART_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			KIND__PROP, 
			ERROR_MESSAGE__PROP);
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
			case KIND__PROP: return getKind();
			case ERROR_MESSAGE__PROP: return getErrorMessage();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case KIND__PROP: internalSetKind((String) value); break;
			case ERROR_MESSAGE__PROP: internalSetErrorMessage((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ErrorPartDto readErrorPartDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ErrorPartDto result = new com.top_logic.ai.chat.api.messages.ErrorPartDto();
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
		out.name(KIND__PROP);
		out.value(getKind());
		out.name(ERROR_MESSAGE__PROP);
		out.value(getErrorMessage());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case KIND__PROP: setKind(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ERROR_MESSAGE__PROP: setErrorMessage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
