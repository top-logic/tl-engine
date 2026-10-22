package com.top_logic.ai.chat.api.messages;

/**
 * Token usage breakdown.
 */
public class TokenUsageDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.TokenUsageDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.TokenUsageDto create() {
		return new com.top_logic.ai.chat.api.messages.TokenUsageDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.TokenUsageDto} type in JSON format. */
	public static final String TOKEN_USAGE_DTO__TYPE = "TokenUsageDto";

	/** @see #getInputTokens() */
	public static final String INPUT_TOKENS__PROP = "inputTokens";

	/** @see #getOutputTokens() */
	public static final String OUTPUT_TOKENS__PROP = "outputTokens";

	/** @see #getThinkingTokens() */
	public static final String THINKING_TOKENS__PROP = "thinkingTokens";

	/** @see #getCacheReadTokens() */
	public static final String CACHE_READ_TOKENS__PROP = "cacheReadTokens";

	/** @see #getCacheWriteTokens() */
	public static final String CACHE_WRITE_TOKENS__PROP = "cacheWriteTokens";

	private int _inputTokens = 0;

	private int _outputTokens = 0;

	private int _thinkingTokens = 0;

	private int _cacheReadTokens = 0;

	private int _cacheWriteTokens = 0;

	/**
	 * Creates a {@link TokenUsageDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.TokenUsageDto#create()
	 */
	protected TokenUsageDto() {
		super();
	}

	/**
	 * Input tokens.
	 */
	public final int getInputTokens() {
		return _inputTokens;
	}

	/**
	 * @see #getInputTokens()
	 */
	public com.top_logic.ai.chat.api.messages.TokenUsageDto setInputTokens(int value) {
		internalSetInputTokens(value);
		return this;
	}

	/** Internal setter for {@link #getInputTokens()} without chain call utility. */
	protected final void internalSetInputTokens(int value) {
		_inputTokens = value;
	}

	/**
	 * Output tokens.
	 */
	public final int getOutputTokens() {
		return _outputTokens;
	}

	/**
	 * @see #getOutputTokens()
	 */
	public com.top_logic.ai.chat.api.messages.TokenUsageDto setOutputTokens(int value) {
		internalSetOutputTokens(value);
		return this;
	}

	/** Internal setter for {@link #getOutputTokens()} without chain call utility. */
	protected final void internalSetOutputTokens(int value) {
		_outputTokens = value;
	}

	/**
	 * Thinking tokens.
	 */
	public final int getThinkingTokens() {
		return _thinkingTokens;
	}

	/**
	 * @see #getThinkingTokens()
	 */
	public com.top_logic.ai.chat.api.messages.TokenUsageDto setThinkingTokens(int value) {
		internalSetThinkingTokens(value);
		return this;
	}

	/** Internal setter for {@link #getThinkingTokens()} without chain call utility. */
	protected final void internalSetThinkingTokens(int value) {
		_thinkingTokens = value;
	}

	/**
	 * Cache read tokens.
	 */
	public final int getCacheReadTokens() {
		return _cacheReadTokens;
	}

	/**
	 * @see #getCacheReadTokens()
	 */
	public com.top_logic.ai.chat.api.messages.TokenUsageDto setCacheReadTokens(int value) {
		internalSetCacheReadTokens(value);
		return this;
	}

	/** Internal setter for {@link #getCacheReadTokens()} without chain call utility. */
	protected final void internalSetCacheReadTokens(int value) {
		_cacheReadTokens = value;
	}

	/**
	 * Cache write tokens.
	 */
	public final int getCacheWriteTokens() {
		return _cacheWriteTokens;
	}

	/**
	 * @see #getCacheWriteTokens()
	 */
	public com.top_logic.ai.chat.api.messages.TokenUsageDto setCacheWriteTokens(int value) {
		internalSetCacheWriteTokens(value);
		return this;
	}

	/** Internal setter for {@link #getCacheWriteTokens()} without chain call utility. */
	protected final void internalSetCacheWriteTokens(int value) {
		_cacheWriteTokens = value;
	}

	@Override
	public String jsonType() {
		return TOKEN_USAGE_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			INPUT_TOKENS__PROP, 
			OUTPUT_TOKENS__PROP, 
			THINKING_TOKENS__PROP, 
			CACHE_READ_TOKENS__PROP, 
			CACHE_WRITE_TOKENS__PROP);
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
			case INPUT_TOKENS__PROP: return getInputTokens();
			case OUTPUT_TOKENS__PROP: return getOutputTokens();
			case THINKING_TOKENS__PROP: return getThinkingTokens();
			case CACHE_READ_TOKENS__PROP: return getCacheReadTokens();
			case CACHE_WRITE_TOKENS__PROP: return getCacheWriteTokens();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case INPUT_TOKENS__PROP: internalSetInputTokens((int) value); break;
			case OUTPUT_TOKENS__PROP: internalSetOutputTokens((int) value); break;
			case THINKING_TOKENS__PROP: internalSetThinkingTokens((int) value); break;
			case CACHE_READ_TOKENS__PROP: internalSetCacheReadTokens((int) value); break;
			case CACHE_WRITE_TOKENS__PROP: internalSetCacheWriteTokens((int) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.TokenUsageDto readTokenUsageDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.TokenUsageDto result = new com.top_logic.ai.chat.api.messages.TokenUsageDto();
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
		out.name(INPUT_TOKENS__PROP);
		out.value(getInputTokens());
		out.name(OUTPUT_TOKENS__PROP);
		out.value(getOutputTokens());
		out.name(THINKING_TOKENS__PROP);
		out.value(getThinkingTokens());
		out.name(CACHE_READ_TOKENS__PROP);
		out.value(getCacheReadTokens());
		out.name(CACHE_WRITE_TOKENS__PROP);
		out.value(getCacheWriteTokens());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case INPUT_TOKENS__PROP: setInputTokens(in.nextInt()); break;
			case OUTPUT_TOKENS__PROP: setOutputTokens(in.nextInt()); break;
			case THINKING_TOKENS__PROP: setThinkingTokens(in.nextInt()); break;
			case CACHE_READ_TOKENS__PROP: setCacheReadTokens(in.nextInt()); break;
			case CACHE_WRITE_TOKENS__PROP: setCacheWriteTokens(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
