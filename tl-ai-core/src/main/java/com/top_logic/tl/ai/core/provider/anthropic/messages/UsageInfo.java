package com.top_logic.tl.ai.core.provider.anthropic.messages;

public class UsageInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo create() {
		return new com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo} type in JSON format. */
	public static final String USAGE_INFO__TYPE = "UsageInfo";

	/** @see #getInputTokens() */
	public static final String INPUT_TOKENS__PROP = "input_tokens";

	/** @see #getOutputTokens() */
	public static final String OUTPUT_TOKENS__PROP = "output_tokens";

	/** @see #getCacheReadInputTokens() */
	public static final String CACHE_READ_INPUT_TOKENS__PROP = "cache_read_input_tokens";

	/** @see #getCacheCreationInputTokens() */
	public static final String CACHE_CREATION_INPUT_TOKENS__PROP = "cache_creation_input_tokens";

	private int _inputTokens = 0;

	private int _outputTokens = 0;

	private int _cacheReadInputTokens = 0;

	private int _cacheCreationInputTokens = 0;

	/**
	 * Creates a {@link UsageInfo} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo#create()
	 */
	protected UsageInfo() {
		super();
	}

	public final int getInputTokens() {
		return _inputTokens;
	}

	/**
	 * @see #getInputTokens()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo setInputTokens(int value) {
		internalSetInputTokens(value);
		return this;
	}

	/** Internal setter for {@link #getInputTokens()} without chain call utility. */
	protected final void internalSetInputTokens(int value) {
		_inputTokens = value;
	}

	public final int getOutputTokens() {
		return _outputTokens;
	}

	/**
	 * @see #getOutputTokens()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo setOutputTokens(int value) {
		internalSetOutputTokens(value);
		return this;
	}

	/** Internal setter for {@link #getOutputTokens()} without chain call utility. */
	protected final void internalSetOutputTokens(int value) {
		_outputTokens = value;
	}

	public final int getCacheReadInputTokens() {
		return _cacheReadInputTokens;
	}

	/**
	 * @see #getCacheReadInputTokens()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo setCacheReadInputTokens(int value) {
		internalSetCacheReadInputTokens(value);
		return this;
	}

	/** Internal setter for {@link #getCacheReadInputTokens()} without chain call utility. */
	protected final void internalSetCacheReadInputTokens(int value) {
		_cacheReadInputTokens = value;
	}

	public final int getCacheCreationInputTokens() {
		return _cacheCreationInputTokens;
	}

	/**
	 * @see #getCacheCreationInputTokens()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo setCacheCreationInputTokens(int value) {
		internalSetCacheCreationInputTokens(value);
		return this;
	}

	/** Internal setter for {@link #getCacheCreationInputTokens()} without chain call utility. */
	protected final void internalSetCacheCreationInputTokens(int value) {
		_cacheCreationInputTokens = value;
	}

	@Override
	public String jsonType() {
		return USAGE_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			INPUT_TOKENS__PROP, 
			OUTPUT_TOKENS__PROP, 
			CACHE_READ_INPUT_TOKENS__PROP, 
			CACHE_CREATION_INPUT_TOKENS__PROP);
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
			case CACHE_READ_INPUT_TOKENS__PROP: return getCacheReadInputTokens();
			case CACHE_CREATION_INPUT_TOKENS__PROP: return getCacheCreationInputTokens();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case INPUT_TOKENS__PROP: internalSetInputTokens((int) value); break;
			case OUTPUT_TOKENS__PROP: internalSetOutputTokens((int) value); break;
			case CACHE_READ_INPUT_TOKENS__PROP: internalSetCacheReadInputTokens((int) value); break;
			case CACHE_CREATION_INPUT_TOKENS__PROP: internalSetCacheCreationInputTokens((int) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo readUsageInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo result = new com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo();
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
		out.name(CACHE_READ_INPUT_TOKENS__PROP);
		out.value(getCacheReadInputTokens());
		out.name(CACHE_CREATION_INPUT_TOKENS__PROP);
		out.value(getCacheCreationInputTokens());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case INPUT_TOKENS__PROP: setInputTokens(in.nextInt()); break;
			case OUTPUT_TOKENS__PROP: setOutputTokens(in.nextInt()); break;
			case CACHE_READ_INPUT_TOKENS__PROP: setCacheReadInputTokens(in.nextInt()); break;
			case CACHE_CREATION_INPUT_TOKENS__PROP: setCacheCreationInputTokens(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
