package com.top_logic.tl.ai.core.provider.openai.messages;

/**
 * Shared envelope for event type dispatch.
 */
public class OpenAIEventEnvelope extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.openai.messages.OpenAIEventEnvelope} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.openai.messages.OpenAIEventEnvelope create() {
		return new com.top_logic.tl.ai.core.provider.openai.messages.OpenAIEventEnvelope();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.openai.messages.OpenAIEventEnvelope} type in JSON format. */
	public static final String OPEN_AIEVENT_ENVELOPE__TYPE = "OpenAIEventEnvelope";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	private String _type = "";

	/**
	 * Creates a {@link OpenAIEventEnvelope} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.openai.messages.OpenAIEventEnvelope#create()
	 */
	protected OpenAIEventEnvelope() {
		super();
	}

	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OpenAIEventEnvelope setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	@Override
	public String jsonType() {
		return OPEN_AIEVENT_ENVELOPE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TYPE__PROP);
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
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TYPE__PROP: internalSetType((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.openai.messages.OpenAIEventEnvelope readOpenAIEventEnvelope(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.openai.messages.OpenAIEventEnvelope result = new com.top_logic.tl.ai.core.provider.openai.messages.OpenAIEventEnvelope();
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
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
