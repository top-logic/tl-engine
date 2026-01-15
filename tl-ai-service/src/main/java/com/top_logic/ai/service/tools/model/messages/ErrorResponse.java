package com.top_logic.ai.service.tools.model.messages;

/**
 * Generic error response for tool operations.
 */
public class ErrorResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.model.messages.ErrorResponse} instance.
	 */
	public static com.top_logic.ai.service.tools.model.messages.ErrorResponse create() {
		return new com.top_logic.ai.service.tools.model.messages.ErrorResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.model.messages.ErrorResponse} type in JSON format. */
	public static final String ERROR_RESPONSE__TYPE = "ErrorResponse";

	/** @see #getError() */
	public static final String ERROR__PROP = "error";

	private String _error = "";

	/**
	 * Creates a {@link ErrorResponse} instance.
	 *
	 * @see com.top_logic.ai.service.tools.model.messages.ErrorResponse#create()
	 */
	protected ErrorResponse() {
		super();
	}

	/**
	 * Error message describing what went wrong.
	 */
	public final String getError() {
		return _error;
	}

	/**
	 * @see #getError()
	 */
	public com.top_logic.ai.service.tools.model.messages.ErrorResponse setError(String value) {
		internalSetError(value);
		return this;
	}

	/** Internal setter for {@link #getError()} without chain call utility. */
	protected final void internalSetError(String value) {
		_error = value;
	}

	@Override
	public String jsonType() {
		return ERROR_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ERROR__PROP);
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
			case ERROR__PROP: return getError();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ERROR__PROP: internalSetError((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.model.messages.ErrorResponse readErrorResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.model.messages.ErrorResponse result = new com.top_logic.ai.service.tools.model.messages.ErrorResponse();
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
		out.name(ERROR__PROP);
		out.value(getError());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ERROR__PROP: setError(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
