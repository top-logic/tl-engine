package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * Thinking/reasoning content part.
 */
public class ThinkingPartDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto} type in JSON format. */
	public static final String THINKING_PART_DTO__TYPE = "ThinkingPartDto";

	/** @see #getText() */
	public static final String TEXT__PROP = "text";

	/** @see #getContinuityToken() */
	public static final String CONTINUITY_TOKEN__PROP = "continuityToken";

	/** @see #getProvider() */
	public static final String PROVIDER__PROP = "provider";

	private String _text = "";

	private String _continuityToken = "";

	private String _provider = "";

	/**
	 * Creates a {@link ThinkingPartDto} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto#create()
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
	public com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto setText(String value) {
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
	public com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto setContinuityToken(String value) {
		internalSetContinuityToken(value);
		return this;
	}

	/** Internal setter for {@link #getContinuityToken()} without chain call utility. */
	protected final void internalSetContinuityToken(String value) {
		_continuityToken = value;
	}

	/**
	 * Catalog ID of the provider that produced the block, e.g. "anthropic".
	 *
	 * <p>
	 * A continuity token only means something to the provider that minted it, so the origin has to
	 * survive persistence: without it a reloaded conversation cannot tell whether a thinking block
	 * may be replayed to the model now in force. Absent in transcripts written before this field.
	 * </p>
	 */
	public final String getProvider() {
		return _provider;
	}

	/**
	 * @see #getProvider()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto setProvider(String value) {
		internalSetProvider(value);
		return this;
	}

	/** Internal setter for {@link #getProvider()} without chain call utility. */
	protected final void internalSetProvider(String value) {
		_provider = value;
	}

	@Override
	public String jsonType() {
		return THINKING_PART_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TEXT__PROP, 
			CONTINUITY_TOKEN__PROP, 
			PROVIDER__PROP);
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
			case PROVIDER__PROP: return getProvider();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TEXT__PROP: internalSetText((String) value); break;
			case CONTINUITY_TOKEN__PROP: internalSetContinuityToken((String) value); break;
			case PROVIDER__PROP: internalSetProvider((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto readThinkingPartDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto result = new com.top_logic.tl.ai.core.session.persist.messages.ThinkingPartDto();
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
		out.name(PROVIDER__PROP);
		out.value(getProvider());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTINUITY_TOKEN__PROP: setContinuityToken(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PROVIDER__PROP: setProvider(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
