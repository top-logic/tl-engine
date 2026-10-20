package com.top_logic.ai.chat.api.messages;

/**
 * Request body for POST /chat/improve-prompt/cancel.
 */
public class CancelPromptImprovementRequest extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.CancelPromptImprovementRequest} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.CancelPromptImprovementRequest create() {
		return new com.top_logic.ai.chat.api.messages.CancelPromptImprovementRequest();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.CancelPromptImprovementRequest} type in JSON format. */
	public static final String CANCEL_PROMPT_IMPROVEMENT_REQUEST__TYPE = "CancelPromptImprovementRequest";

	/** @see #getRequestId() */
	public static final String REQUEST_ID__PROP = "requestId";

	private String _requestId = "";

	/**
	 * Creates a {@link CancelPromptImprovementRequest} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.CancelPromptImprovementRequest#create()
	 */
	protected CancelPromptImprovementRequest() {
		super();
	}

	/**
	 * Correlation ID of the request to cancel.
	 */
	public final String getRequestId() {
		return _requestId;
	}

	/**
	 * @see #getRequestId()
	 */
	public com.top_logic.ai.chat.api.messages.CancelPromptImprovementRequest setRequestId(String value) {
		internalSetRequestId(value);
		return this;
	}

	/** Internal setter for {@link #getRequestId()} without chain call utility. */
	protected final void internalSetRequestId(String value) {
		_requestId = value;
	}

	@Override
	public String jsonType() {
		return CANCEL_PROMPT_IMPROVEMENT_REQUEST__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			REQUEST_ID__PROP);
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
			case REQUEST_ID__PROP: return getRequestId();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case REQUEST_ID__PROP: internalSetRequestId((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.CancelPromptImprovementRequest readCancelPromptImprovementRequest(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.CancelPromptImprovementRequest result = new com.top_logic.ai.chat.api.messages.CancelPromptImprovementRequest();
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
		out.name(REQUEST_ID__PROP);
		out.value(getRequestId());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case REQUEST_ID__PROP: setRequestId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
