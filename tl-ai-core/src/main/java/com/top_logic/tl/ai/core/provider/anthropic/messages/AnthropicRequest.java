package com.top_logic.tl.ai.core.provider.anthropic.messages;

/**
 * Anthropic Messages API request body.
 */
public class AnthropicRequest extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest create() {
		return new com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest} type in JSON format. */
	public static final String ANTHROPIC_REQUEST__TYPE = "AnthropicRequest";

	/** @see #getModel() */
	public static final String MODEL__PROP = "model";

	/** @see #getMaxTokens() */
	public static final String MAX_TOKENS__PROP = "max_tokens";

	/** @see #isStream() */
	public static final String STREAM__PROP = "stream";

	/** @see #getTemperature() */
	public static final String TEMPERATURE__PROP = "temperature";

	/** @see #getTopP() */
	public static final String TOP_P__PROP = "top_p";

	/** @see #getTopK() */
	public static final String TOP_K__PROP = "top_k";

	/** @see #getSystem() */
	public static final String SYSTEM__PROP = "system";

	/** @see #getMessages() */
	public static final String MESSAGES__PROP = "messages";

	/** @see #getTools() */
	public static final String TOOLS__PROP = "tools";

	/** @see #getToolChoice() */
	public static final String TOOL_CHOICE__PROP = "tool_choice";

	/** @see #getThinking() */
	public static final String THINKING__PROP = "thinking";

	/** @see #getOutputConfig() */
	public static final String OUTPUT_CONFIG__PROP = "output_config";

	private String _model = "";

	private int _maxTokens = 0;

	private boolean _stream = false;

	private Object _temperature = null;

	private Object _topP = null;

	private Object _topK = null;

	private Object _system = null;

	private Object _messages = null;

	private Object _tools = null;

	private Object _toolChoice = null;

	private Object _thinking = null;

	private Object _outputConfig = null;

	/**
	 * Creates a {@link AnthropicRequest} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest#create()
	 */
	protected AnthropicRequest() {
		super();
	}

	/**
	 * The model identifier.
	 */
	public final String getModel() {
		return _model;
	}

	/**
	 * @see #getModel()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest setModel(String value) {
		internalSetModel(value);
		return this;
	}

	/** Internal setter for {@link #getModel()} without chain call utility. */
	protected final void internalSetModel(String value) {
		_model = value;
	}

	/**
	 * Maximum number of output tokens.
	 */
	public final int getMaxTokens() {
		return _maxTokens;
	}

	/**
	 * @see #getMaxTokens()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest setMaxTokens(int value) {
		internalSetMaxTokens(value);
		return this;
	}

	/** Internal setter for {@link #getMaxTokens()} without chain call utility. */
	protected final void internalSetMaxTokens(int value) {
		_maxTokens = value;
	}

	/**
	 * Whether to stream the response.
	 */
	public final boolean isStream() {
		return _stream;
	}

	/**
	 * @see #isStream()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest setStream(boolean value) {
		internalSetStream(value);
		return this;
	}

	/** Internal setter for {@link #isStream()} without chain call utility. */
	protected final void internalSetStream(boolean value) {
		_stream = value;
	}

	/**
	 * Sampling temperature.
	 */
	public final Object getTemperature() {
		return _temperature;
	}

	/**
	 * @see #getTemperature()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest setTemperature(Object value) {
		internalSetTemperature(value);
		return this;
	}

	/** Internal setter for {@link #getTemperature()} without chain call utility. */
	protected final void internalSetTemperature(Object value) {
		_temperature = value;
	}

	/**
	 * Checks, whether {@link #getTemperature()} has a value.
	 */
	public final boolean hasTemperature() {
		return _temperature != null;
	}

	/**
	 * Top-p sampling parameter.
	 */
	public final Object getTopP() {
		return _topP;
	}

	/**
	 * @see #getTopP()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest setTopP(Object value) {
		internalSetTopP(value);
		return this;
	}

	/** Internal setter for {@link #getTopP()} without chain call utility. */
	protected final void internalSetTopP(Object value) {
		_topP = value;
	}

	/**
	 * Checks, whether {@link #getTopP()} has a value.
	 */
	public final boolean hasTopP() {
		return _topP != null;
	}

	/**
	 * Top-k sampling parameter.
	 */
	public final Object getTopK() {
		return _topK;
	}

	/**
	 * @see #getTopK()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest setTopK(Object value) {
		internalSetTopK(value);
		return this;
	}

	/** Internal setter for {@link #getTopK()} without chain call utility. */
	protected final void internalSetTopK(Object value) {
		_topK = value;
	}

	/**
	 * Checks, whether {@link #getTopK()} has a value.
	 */
	public final boolean hasTopK() {
		return _topK != null;
	}

	/**
	 * System prompt content.
	 */
	public final Object getSystem() {
		return _system;
	}

	/**
	 * @see #getSystem()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest setSystem(Object value) {
		internalSetSystem(value);
		return this;
	}

	/** Internal setter for {@link #getSystem()} without chain call utility. */
	protected final void internalSetSystem(Object value) {
		_system = value;
	}

	/**
	 * Checks, whether {@link #getSystem()} has a value.
	 */
	public final boolean hasSystem() {
		return _system != null;
	}

	/**
	 * The conversation messages.
	 */
	public final Object getMessages() {
		return _messages;
	}

	/**
	 * @see #getMessages()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest setMessages(Object value) {
		internalSetMessages(value);
		return this;
	}

	/** Internal setter for {@link #getMessages()} without chain call utility. */
	protected final void internalSetMessages(Object value) {
		_messages = value;
	}

	/**
	 * Checks, whether {@link #getMessages()} has a value.
	 */
	public final boolean hasMessages() {
		return _messages != null;
	}

	/**
	 * Tool definitions.
	 */
	public final Object getTools() {
		return _tools;
	}

	/**
	 * @see #getTools()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest setTools(Object value) {
		internalSetTools(value);
		return this;
	}

	/** Internal setter for {@link #getTools()} without chain call utility. */
	protected final void internalSetTools(Object value) {
		_tools = value;
	}

	/**
	 * Checks, whether {@link #getTools()} has a value.
	 */
	public final boolean hasTools() {
		return _tools != null;
	}

	/**
	 * Tool choice configuration.
	 */
	public final Object getToolChoice() {
		return _toolChoice;
	}

	/**
	 * @see #getToolChoice()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest setToolChoice(Object value) {
		internalSetToolChoice(value);
		return this;
	}

	/** Internal setter for {@link #getToolChoice()} without chain call utility. */
	protected final void internalSetToolChoice(Object value) {
		_toolChoice = value;
	}

	/**
	 * Checks, whether {@link #getToolChoice()} has a value.
	 */
	public final boolean hasToolChoice() {
		return _toolChoice != null;
	}

	/**
	 * Extended thinking configuration.
	 */
	public final Object getThinking() {
		return _thinking;
	}

	/**
	 * @see #getThinking()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest setThinking(Object value) {
		internalSetThinking(value);
		return this;
	}

	/** Internal setter for {@link #getThinking()} without chain call utility. */
	protected final void internalSetThinking(Object value) {
		_thinking = value;
	}

	/**
	 * Checks, whether {@link #getThinking()} has a value.
	 */
	public final boolean hasThinking() {
		return _thinking != null;
	}

	/**
	 * Output configuration: thinking effort and the structured-output format.
	 */
	public final Object getOutputConfig() {
		return _outputConfig;
	}

	/**
	 * @see #getOutputConfig()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest setOutputConfig(Object value) {
		internalSetOutputConfig(value);
		return this;
	}

	/** Internal setter for {@link #getOutputConfig()} without chain call utility. */
	protected final void internalSetOutputConfig(Object value) {
		_outputConfig = value;
	}

	/**
	 * Checks, whether {@link #getOutputConfig()} has a value.
	 */
	public final boolean hasOutputConfig() {
		return _outputConfig != null;
	}

	@Override
	public String jsonType() {
		return ANTHROPIC_REQUEST__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			MODEL__PROP, 
			MAX_TOKENS__PROP, 
			STREAM__PROP, 
			TEMPERATURE__PROP, 
			TOP_P__PROP, 
			TOP_K__PROP, 
			SYSTEM__PROP, 
			MESSAGES__PROP, 
			TOOLS__PROP, 
			TOOL_CHOICE__PROP, 
			THINKING__PROP, 
			OUTPUT_CONFIG__PROP);
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
			case MODEL__PROP: return getModel();
			case MAX_TOKENS__PROP: return getMaxTokens();
			case STREAM__PROP: return isStream();
			case TEMPERATURE__PROP: return getTemperature();
			case TOP_P__PROP: return getTopP();
			case TOP_K__PROP: return getTopK();
			case SYSTEM__PROP: return getSystem();
			case MESSAGES__PROP: return getMessages();
			case TOOLS__PROP: return getTools();
			case TOOL_CHOICE__PROP: return getToolChoice();
			case THINKING__PROP: return getThinking();
			case OUTPUT_CONFIG__PROP: return getOutputConfig();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case MODEL__PROP: internalSetModel((String) value); break;
			case MAX_TOKENS__PROP: internalSetMaxTokens((int) value); break;
			case STREAM__PROP: internalSetStream((boolean) value); break;
			case TEMPERATURE__PROP: internalSetTemperature((Object) value); break;
			case TOP_P__PROP: internalSetTopP((Object) value); break;
			case TOP_K__PROP: internalSetTopK((Object) value); break;
			case SYSTEM__PROP: internalSetSystem((Object) value); break;
			case MESSAGES__PROP: internalSetMessages((Object) value); break;
			case TOOLS__PROP: internalSetTools((Object) value); break;
			case TOOL_CHOICE__PROP: internalSetToolChoice((Object) value); break;
			case THINKING__PROP: internalSetThinking((Object) value); break;
			case OUTPUT_CONFIG__PROP: internalSetOutputConfig((Object) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest readAnthropicRequest(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest result = new com.top_logic.tl.ai.core.provider.anthropic.messages.AnthropicRequest();
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
		out.name(MODEL__PROP);
		out.value(getModel());
		out.name(MAX_TOKENS__PROP);
		out.value(getMaxTokens());
		out.name(STREAM__PROP);
		out.value(isStream());
		if (hasTemperature()) {
			out.name(TEMPERATURE__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getTemperature());
		}
		if (hasTopP()) {
			out.name(TOP_P__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getTopP());
		}
		if (hasTopK()) {
			out.name(TOP_K__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getTopK());
		}
		if (hasSystem()) {
			out.name(SYSTEM__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getSystem());
		}
		if (hasMessages()) {
			out.name(MESSAGES__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getMessages());
		}
		if (hasTools()) {
			out.name(TOOLS__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getTools());
		}
		if (hasToolChoice()) {
			out.name(TOOL_CHOICE__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getToolChoice());
		}
		if (hasThinking()) {
			out.name(THINKING__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getThinking());
		}
		if (hasOutputConfig()) {
			out.name(OUTPUT_CONFIG__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getOutputConfig());
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case MODEL__PROP: setModel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MAX_TOKENS__PROP: setMaxTokens(in.nextInt()); break;
			case STREAM__PROP: setStream(in.nextBoolean()); break;
			case TEMPERATURE__PROP: setTemperature(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case TOP_P__PROP: setTopP(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case TOP_K__PROP: setTopK(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case SYSTEM__PROP: setSystem(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case MESSAGES__PROP: setMessages(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case TOOLS__PROP: setTools(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case TOOL_CHOICE__PROP: setToolChoice(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case THINKING__PROP: setThinking(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case OUTPUT_CONFIG__PROP: setOutputConfig(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			default: super.readField(in, field);
		}
	}

}
