package com.top_logic.ai.chat.api.messages;

/**
 * SSE event describing what the prompt of the request just sent is made of.
 *
 * <p>
 * Every figure is an estimate produced by the agent loop's token estimator, not a provider count:
 * the provider reports one prompt total and never splits it. A client showing these next to the
 * measured total must mark them as estimated.
 * </p>
 */
public class ContextCompositionEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ContextCompositionEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ContextCompositionEvent create() {
		return new com.top_logic.ai.chat.api.messages.ContextCompositionEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ContextCompositionEvent} type in JSON format. */
	public static final String CONTEXT_COMPOSITION_EVENT__TYPE = "ContextCompositionEvent";

	/** @see #getSystemPromptTokens() */
	public static final String SYSTEM_PROMPT_TOKENS__PROP = "systemPromptTokens";

	/** @see #getToolDefinitionTokens() */
	public static final String TOOL_DEFINITION_TOKENS__PROP = "toolDefinitionTokens";

	/** @see #getMessageTokens() */
	public static final String MESSAGE_TOKENS__PROP = "messageTokens";

	/** @see #getToolCount() */
	public static final String TOOL_COUNT__PROP = "toolCount";

	private int _systemPromptTokens = 0;

	private int _toolDefinitionTokens = 0;

	private int _messageTokens = 0;

	private int _toolCount = 0;

	/**
	 * Creates a {@link ContextCompositionEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ContextCompositionEvent#create()
	 */
	protected ContextCompositionEvent() {
		super();
	}

	/**
	 * Estimated tokens of the system prompt.
	 */
	public final int getSystemPromptTokens() {
		return _systemPromptTokens;
	}

	/**
	 * @see #getSystemPromptTokens()
	 */
	public com.top_logic.ai.chat.api.messages.ContextCompositionEvent setSystemPromptTokens(int value) {
		internalSetSystemPromptTokens(value);
		return this;
	}

	/** Internal setter for {@link #getSystemPromptTokens()} without chain call utility. */
	protected final void internalSetSystemPromptTokens(int value) {
		_systemPromptTokens = value;
	}

	/**
	 * Estimated tokens of the tool schemas sent with the request.
	 */
	public final int getToolDefinitionTokens() {
		return _toolDefinitionTokens;
	}

	/**
	 * @see #getToolDefinitionTokens()
	 */
	public com.top_logic.ai.chat.api.messages.ContextCompositionEvent setToolDefinitionTokens(int value) {
		internalSetToolDefinitionTokens(value);
		return this;
	}

	/** Internal setter for {@link #getToolDefinitionTokens()} without chain call utility. */
	protected final void internalSetToolDefinitionTokens(int value) {
		_toolDefinitionTokens = value;
	}

	/**
	 * Estimated tokens of the conversation messages.
	 */
	public final int getMessageTokens() {
		return _messageTokens;
	}

	/**
	 * @see #getMessageTokens()
	 */
	public com.top_logic.ai.chat.api.messages.ContextCompositionEvent setMessageTokens(int value) {
		internalSetMessageTokens(value);
		return this;
	}

	/** Internal setter for {@link #getMessageTokens()} without chain call utility. */
	protected final void internalSetMessageTokens(int value) {
		_messageTokens = value;
	}

	/**
	 * Number of tool schemas sent with the request.
	 */
	public final int getToolCount() {
		return _toolCount;
	}

	/**
	 * @see #getToolCount()
	 */
	public com.top_logic.ai.chat.api.messages.ContextCompositionEvent setToolCount(int value) {
		internalSetToolCount(value);
		return this;
	}

	/** Internal setter for {@link #getToolCount()} without chain call utility. */
	protected final void internalSetToolCount(int value) {
		_toolCount = value;
	}

	@Override
	public String jsonType() {
		return CONTEXT_COMPOSITION_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SYSTEM_PROMPT_TOKENS__PROP, 
			TOOL_DEFINITION_TOKENS__PROP, 
			MESSAGE_TOKENS__PROP, 
			TOOL_COUNT__PROP);
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
			case SYSTEM_PROMPT_TOKENS__PROP: return getSystemPromptTokens();
			case TOOL_DEFINITION_TOKENS__PROP: return getToolDefinitionTokens();
			case MESSAGE_TOKENS__PROP: return getMessageTokens();
			case TOOL_COUNT__PROP: return getToolCount();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SYSTEM_PROMPT_TOKENS__PROP: internalSetSystemPromptTokens((int) value); break;
			case TOOL_DEFINITION_TOKENS__PROP: internalSetToolDefinitionTokens((int) value); break;
			case MESSAGE_TOKENS__PROP: internalSetMessageTokens((int) value); break;
			case TOOL_COUNT__PROP: internalSetToolCount((int) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ContextCompositionEvent readContextCompositionEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ContextCompositionEvent result = new com.top_logic.ai.chat.api.messages.ContextCompositionEvent();
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
		out.name(SYSTEM_PROMPT_TOKENS__PROP);
		out.value(getSystemPromptTokens());
		out.name(TOOL_DEFINITION_TOKENS__PROP);
		out.value(getToolDefinitionTokens());
		out.name(MESSAGE_TOKENS__PROP);
		out.value(getMessageTokens());
		out.name(TOOL_COUNT__PROP);
		out.value(getToolCount());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SYSTEM_PROMPT_TOKENS__PROP: setSystemPromptTokens(in.nextInt()); break;
			case TOOL_DEFINITION_TOKENS__PROP: setToolDefinitionTokens(in.nextInt()); break;
			case MESSAGE_TOKENS__PROP: setMessageTokens(in.nextInt()); break;
			case TOOL_COUNT__PROP: setToolCount(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
