package com.top_logic.tl.ai.core.provider.anthropic.messages;

/**
 * Details of a stream error.
 */
public class StreamErrorInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo create() {
		return new com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo} type in JSON format. */
	public static final String STREAM_ERROR_INFO__TYPE = "StreamErrorInfo";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getMessage() */
	public static final String MESSAGE__PROP = "message";

	private String _type = "";

	private String _message = "";

	/**
	 * Creates a {@link StreamErrorInfo} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo#create()
	 */
	protected StreamErrorInfo() {
		super();
	}

	/**
	 * The error type.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * The error message.
	 */
	public final String getMessage() {
		return _message;
	}

	/**
	 * @see #getMessage()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo setMessage(String value) {
		internalSetMessage(value);
		return this;
	}

	/** Internal setter for {@link #getMessage()} without chain call utility. */
	protected final void internalSetMessage(String value) {
		_message = value;
	}

	@Override
	public String jsonType() {
		return STREAM_ERROR_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TYPE__PROP, 
			MESSAGE__PROP);
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
			case MESSAGE__PROP: return getMessage();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TYPE__PROP: internalSetType((String) value); break;
			case MESSAGE__PROP: internalSetMessage((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo readStreamErrorInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo result = new com.top_logic.tl.ai.core.provider.anthropic.messages.StreamErrorInfo();
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
		out.name(MESSAGE__PROP);
		out.value(getMessage());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MESSAGE__PROP: setMessage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
