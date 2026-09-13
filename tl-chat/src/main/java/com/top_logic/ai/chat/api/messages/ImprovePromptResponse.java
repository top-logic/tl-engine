package com.top_logic.ai.chat.api.messages;

/**
 * Response body for POST /chat/improve-prompt.
 */
public class ImprovePromptResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ImprovePromptResponse} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ImprovePromptResponse create() {
		return new com.top_logic.ai.chat.api.messages.ImprovePromptResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ImprovePromptResponse} type in JSON format. */
	public static final String IMPROVE_PROMPT_RESPONSE__TYPE = "ImprovePromptResponse";

	/** @see #getRequestId() */
	public static final String REQUEST_ID__PROP = "requestId";

	/** @see #getImprovedText() */
	public static final String IMPROVED_TEXT__PROP = "improvedText";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	private String _requestId = "";

	private String _improvedText = "";

	private String _status = "";

	/**
	 * Creates a {@link ImprovePromptResponse} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ImprovePromptResponse#create()
	 */
	protected ImprovePromptResponse() {
		super();
	}

	/**
	 * Correlation ID from the request.
	 */
	public final String getRequestId() {
		return _requestId;
	}

	/**
	 * @see #getRequestId()
	 */
	public com.top_logic.ai.chat.api.messages.ImprovePromptResponse setRequestId(String value) {
		internalSetRequestId(value);
		return this;
	}

	/** Internal setter for {@link #getRequestId()} without chain call utility. */
	protected final void internalSetRequestId(String value) {
		_requestId = value;
	}

	/**
	 * Improved draft text. Empty unless {@link #getStatus()} is "completed".
	 */
	public final String getImprovedText() {
		return _improvedText;
	}

	/**
	 * @see #getImprovedText()
	 */
	public com.top_logic.ai.chat.api.messages.ImprovePromptResponse setImprovedText(String value) {
		internalSetImprovedText(value);
		return this;
	}

	/** Internal setter for {@link #getImprovedText()} without chain call utility. */
	protected final void internalSetImprovedText(String value) {
		_improvedText = value;
	}

	/**
	 * Result status: "completed", "cancelled", or "stale".
	 */
	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.ai.chat.api.messages.ImprovePromptResponse setStatus(String value) {
		internalSetStatus(value);
		return this;
	}

	/** Internal setter for {@link #getStatus()} without chain call utility. */
	protected final void internalSetStatus(String value) {
		_status = value;
	}

	@Override
	public String jsonType() {
		return IMPROVE_PROMPT_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			REQUEST_ID__PROP, 
			IMPROVED_TEXT__PROP, 
			STATUS__PROP);
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
			case IMPROVED_TEXT__PROP: return getImprovedText();
			case STATUS__PROP: return getStatus();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case REQUEST_ID__PROP: internalSetRequestId((String) value); break;
			case IMPROVED_TEXT__PROP: internalSetImprovedText((String) value); break;
			case STATUS__PROP: internalSetStatus((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ImprovePromptResponse readImprovePromptResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ImprovePromptResponse result = new com.top_logic.ai.chat.api.messages.ImprovePromptResponse();
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
		out.name(IMPROVED_TEXT__PROP);
		out.value(getImprovedText());
		out.name(STATUS__PROP);
		out.value(getStatus());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case REQUEST_ID__PROP: setRequestId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case IMPROVED_TEXT__PROP: setImprovedText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
