package com.top_logic.tl.ai.core.provider.openai.messages;

/**
 * {@code response.created}, {@code response.completed}, {@code response.incomplete}, {@code response.failed}.
 */
public class ResponseLifecycleEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.openai.messages.ResponseLifecycleEvent} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.openai.messages.ResponseLifecycleEvent create() {
		return new com.top_logic.tl.ai.core.provider.openai.messages.ResponseLifecycleEvent();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.openai.messages.ResponseLifecycleEvent} type in JSON format. */
	public static final String RESPONSE_LIFECYCLE_EVENT__TYPE = "ResponseLifecycleEvent";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getResponse() */
	public static final String RESPONSE__PROP = "response";

	private String _type = "";

	private com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo _response = null;

	/**
	 * Creates a {@link ResponseLifecycleEvent} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.openai.messages.ResponseLifecycleEvent#create()
	 */
	protected ResponseLifecycleEvent() {
		super();
	}

	/**
	 * The event type identifier.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.ResponseLifecycleEvent setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * The response metadata.
	 */
	public final com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo getResponse() {
		return _response;
	}

	/**
	 * @see #getResponse()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.ResponseLifecycleEvent setResponse(com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo value) {
		internalSetResponse(value);
		return this;
	}

	/** Internal setter for {@link #getResponse()} without chain call utility. */
	protected final void internalSetResponse(com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo value) {
		_response = value;
	}

	/**
	 * Checks, whether {@link #getResponse()} has a value.
	 */
	public final boolean hasResponse() {
		return _response != null;
	}

	@Override
	public String jsonType() {
		return RESPONSE_LIFECYCLE_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TYPE__PROP, 
			RESPONSE__PROP);
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
			case RESPONSE__PROP: return getResponse();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TYPE__PROP: internalSetType((String) value); break;
			case RESPONSE__PROP: internalSetResponse((com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.openai.messages.ResponseLifecycleEvent readResponseLifecycleEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.openai.messages.ResponseLifecycleEvent result = new com.top_logic.tl.ai.core.provider.openai.messages.ResponseLifecycleEvent();
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
		if (hasResponse()) {
			out.name(RESPONSE__PROP);
			getResponse().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RESPONSE__PROP: setResponse(com.top_logic.tl.ai.core.provider.openai.messages.ResponseInfo.readResponseInfo(in)); break;
			default: super.readField(in, field);
		}
	}

}
