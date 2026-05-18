package com.top_logic.ai.chat.api.messages;

/**
 * Thinking/reasoning content part.
 */
public class ThinkingPartDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ThinkingPartDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ThinkingPartDto create() {
		return new com.top_logic.ai.chat.api.messages.ThinkingPartDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ThinkingPartDto} type in JSON format. */
	public static final String THINKING_PART_DTO__TYPE = "ThinkingPartDto";

	/** @see #getText() */
	public static final String TEXT__PROP = "text";

	/** @see #getContinuityToken() */
	public static final String CONTINUITY_TOKEN__PROP = "continuityToken";

	private String _text = "";

	private String _continuityToken = "";

	/**
	 * Creates a {@link ThinkingPartDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ThinkingPartDto#create()
	 */
	protected ThinkingPartDto() {
		super();
	}

	/**
	 * The thinking text.
	 */
	public final String getText() {
		return _text;
	}

	/**
	 * @see #getText()
	 */
	public com.top_logic.ai.chat.api.messages.ThinkingPartDto setText(String value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(String value) {
		_text = value;
	}

	/**
	 * Opaque provider token for multi-turn continuity (signature / encrypted_content).
	 */
	public final String getContinuityToken() {
		return _continuityToken;
	}

	/**
	 * @see #getContinuityToken()
	 */
	public com.top_logic.ai.chat.api.messages.ThinkingPartDto setContinuityToken(String value) {
		internalSetContinuityToken(value);
		return this;
	}

	/** Internal setter for {@link #getContinuityToken()} without chain call utility. */
	protected final void internalSetContinuityToken(String value) {
		_continuityToken = value;
	}

	@Override
	public String jsonType() {
		return THINKING_PART_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TEXT__PROP, 
			CONTINUITY_TOKEN__PROP);
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
			case TEXT__PROP: return getText();
			case CONTINUITY_TOKEN__PROP: return getContinuityToken();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TEXT__PROP: internalSetText((String) value); break;
			case CONTINUITY_TOKEN__PROP: internalSetContinuityToken((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ThinkingPartDto readThinkingPartDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ThinkingPartDto result = new com.top_logic.ai.chat.api.messages.ThinkingPartDto();
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
		out.name(TEXT__PROP);
		out.value(getText());
		out.name(CONTINUITY_TOKEN__PROP);
		out.value(getContinuityToken());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTINUITY_TOKEN__PROP: setContinuityToken(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
