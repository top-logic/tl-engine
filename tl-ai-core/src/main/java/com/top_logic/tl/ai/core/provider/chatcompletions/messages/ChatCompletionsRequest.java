package com.top_logic.tl.ai.core.provider.chatcompletions.messages;

/**
 * Chat Completions API request body (<code>OpenAI</code>-compatible).
 */
public class ChatCompletionsRequest extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest create() {
		return new com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest} type in JSON format. */
	public static final String CHAT_COMPLETIONS_REQUEST__TYPE = "ChatCompletionsRequest";

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

	/** @see #getMessages() */
	public static final String MESSAGES__PROP = "messages";

	/** @see #getTools() */
	public static final String TOOLS__PROP = "tools";

	/** @see #getResponseFormat() */
	public static final String RESPONSE_FORMAT__PROP = "response_format";

	/** @see #getToolChoice() */
	public static final String TOOL_CHOICE__PROP = "tool_choice";

	private String _model = "";

	private int _maxTokens = 0;

	private boolean _stream = false;

	private Object _temperature = null;

	private Object _topP = null;

	private Object _messages = null;

	private Object _tools = null;

	private Object _responseFormat = null;

	private Object _toolChoice = null;

	/**
	 * Creates a {@link ChatCompletionsRequest} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest#create()
	 */
	protected ChatCompletionsRequest() {
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
	public com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest setModel(String value) {
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
	public com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest setMaxTokens(int value) {
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
	public com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest setStream(boolean value) {
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
	public com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest setTemperature(Object value) {
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
	public com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest setTopP(Object value) {
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
	 * The conversation messages.
	 */
	public final Object getMessages() {
		return _messages;
	}

	/**
	 * @see #getMessages()
	 */
	public com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest setMessages(Object value) {
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
	public com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest setTools(Object value) {
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
	 * Response format specification.
	 */
	public final Object getResponseFormat() {
		return _responseFormat;
	}

	/**
	 * @see #getResponseFormat()
	 */
	public com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest setResponseFormat(Object value) {
		internalSetResponseFormat(value);
		return this;
	}

	/** Internal setter for {@link #getResponseFormat()} without chain call utility. */
	protected final void internalSetResponseFormat(Object value) {
		_responseFormat = value;
	}

	/**
	 * Checks, whether {@link #getResponseFormat()} has a value.
	 */
	public final boolean hasResponseFormat() {
		return _responseFormat != null;
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
	public com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest setToolChoice(Object value) {
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

	@Override
	public String jsonType() {
		return CHAT_COMPLETIONS_REQUEST__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			MODEL__PROP, 
			MAX_TOKENS__PROP, 
			STREAM__PROP, 
			TEMPERATURE__PROP, 
			TOP_P__PROP, 
			MESSAGES__PROP, 
			TOOLS__PROP, 
			RESPONSE_FORMAT__PROP, 
			TOOL_CHOICE__PROP);
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
			case MESSAGES__PROP: return getMessages();
			case TOOLS__PROP: return getTools();
			case RESPONSE_FORMAT__PROP: return getResponseFormat();
			case TOOL_CHOICE__PROP: return getToolChoice();
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
			case MESSAGES__PROP: internalSetMessages((Object) value); break;
			case TOOLS__PROP: internalSetTools((Object) value); break;
			case RESPONSE_FORMAT__PROP: internalSetResponseFormat((Object) value); break;
			case TOOL_CHOICE__PROP: internalSetToolChoice((Object) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest readChatCompletionsRequest(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest result = new com.top_logic.tl.ai.core.provider.chatcompletions.messages.ChatCompletionsRequest();
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
		if (hasMessages()) {
			out.name(MESSAGES__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getMessages());
		}
		if (hasTools()) {
			out.name(TOOLS__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getTools());
		}
		if (hasResponseFormat()) {
			out.name(RESPONSE_FORMAT__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getResponseFormat());
		}
		if (hasToolChoice()) {
			out.name(TOOL_CHOICE__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getToolChoice());
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
			case MESSAGES__PROP: setMessages(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case TOOLS__PROP: setTools(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case RESPONSE_FORMAT__PROP: setResponseFormat(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case TOOL_CHOICE__PROP: setToolChoice(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			default: super.readField(in, field);
		}
	}

}
