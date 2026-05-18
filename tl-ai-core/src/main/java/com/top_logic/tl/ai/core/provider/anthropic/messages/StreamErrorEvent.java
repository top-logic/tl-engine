package com.top_logic.tl.ai.core.provider.anthropic.messages;

/**
 * Error payload within an {@code error} SSE event.
 */
public class StreamErrorEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorEvent} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorEvent create() {
		return new com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorEvent();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorEvent} type in JSON format. */
	public static final String STREAM_ERROR_EVENT__TYPE = "StreamErrorEvent";

	/** @see #getError() */
	public static final String ERROR__PROP = "error";

	private com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo _error = null;

	/**
	 * Creates a {@link StreamErrorEvent} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorEvent#create()
	 */
	protected StreamErrorEvent() {
		super();
	}

	/**
	 * The error details.
	 */
	public final com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo getError() {
		return _error;
	}

	/**
	 * @see #getError()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorEvent setError(com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo value) {
		internalSetError(value);
		return this;
	}

	/** Internal setter for {@link #getError()} without chain call utility. */
	protected final void internalSetError(com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo value) {
		_error = value;
	}

	/**
	 * Checks, whether {@link #getError()} has a value.
	 */
	public final boolean hasError() {
		return _error != null;
	}

	@Override
	public String jsonType() {
		return STREAM_ERROR_EVENT__TYPE;
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
			case ERROR__PROP: internalSetError((com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorEvent readStreamErrorEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorEvent result = new com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorEvent();
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
		if (hasError()) {
			out.name(ERROR__PROP);
			getError().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ERROR__PROP: setError(com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo.readStreamErrorInfo(in)); break;
			default: super.readField(in, field);
		}
	}

}
