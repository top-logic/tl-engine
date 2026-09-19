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

	/** @see #getToolResultTokens() */
	public static final String TOOL_RESULT_TOKENS__PROP = "toolResultTokens";

	/** @see #getToolCount() */
	public static final String TOOL_COUNT__PROP = "toolCount";

	/** @see #getLoadedSkills() */
	public static final String LOADED_SKILLS__PROP = "loadedSkills";

	private int _systemPromptTokens = 0;

	private int _toolDefinitionTokens = 0;

	private int _messageTokens = 0;

	private int _toolResultTokens = 0;

	private int _toolCount = 0;

	private final java.util.List<String> _loadedSkills = new java.util.ArrayList<>();

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
	 * Estimated tokens of the conversation messages, tool results included.
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
	 * Estimated tokens of the tool results within {@link #getMessageTokens()}.
	 */
	public final int getToolResultTokens() {
		return _toolResultTokens;
	}

	/**
	 * @see #getToolResultTokens()
	 */
	public com.top_logic.ai.chat.api.messages.ContextCompositionEvent setToolResultTokens(int value) {
		internalSetToolResultTokens(value);
		return this;
	}

	/** Internal setter for {@link #getToolResultTokens()} without chain call utility. */
	protected final void internalSetToolResultTokens(int value) {
		_toolResultTokens = value;
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

	/**
	 * The skills whose tools this request carried, in the order they were loaded.
	 *
	 * <p>
	 * The part of {@link #getToolDefinitionTokens()} a {@code loadSkill} added, and the only part of the
	 * tool array that can still change within a conversation.
	 * </p>
	 */
	public final java.util.List<String> getLoadedSkills() {
		return _loadedSkills;
	}

	/**
	 * @see #getLoadedSkills()
	 */
	public com.top_logic.ai.chat.api.messages.ContextCompositionEvent setLoadedSkills(java.util.List<? extends String> value) {
		internalSetLoadedSkills(value);
		return this;
	}

	/** Internal setter for {@link #getLoadedSkills()} without chain call utility. */
	protected final void internalSetLoadedSkills(java.util.List<? extends String> value) {
		_loadedSkills.clear();
		_loadedSkills.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getLoadedSkills()} list.
	 */
	public com.top_logic.ai.chat.api.messages.ContextCompositionEvent addLoadedSkill(String value) {
		internalAddLoadedSkill(value);
		return this;
	}

	/** Implementation of {@link #addLoadedSkill(String)} without chain call utility. */
	protected final void internalAddLoadedSkill(String value) {
		_loadedSkills.add(value);
	}

	/**
	 * Removes a value from the {@link #getLoadedSkills()} list.
	 */
	public final void removeLoadedSkill(String value) {
		_loadedSkills.remove(value);
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
			TOOL_RESULT_TOKENS__PROP, 
			TOOL_COUNT__PROP, 
			LOADED_SKILLS__PROP);
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
			case TOOL_RESULT_TOKENS__PROP: return getToolResultTokens();
			case TOOL_COUNT__PROP: return getToolCount();
			case LOADED_SKILLS__PROP: return getLoadedSkills();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SYSTEM_PROMPT_TOKENS__PROP: internalSetSystemPromptTokens((int) value); break;
			case TOOL_DEFINITION_TOKENS__PROP: internalSetToolDefinitionTokens((int) value); break;
			case MESSAGE_TOKENS__PROP: internalSetMessageTokens((int) value); break;
			case TOOL_RESULT_TOKENS__PROP: internalSetToolResultTokens((int) value); break;
			case TOOL_COUNT__PROP: internalSetToolCount((int) value); break;
			case LOADED_SKILLS__PROP: internalSetLoadedSkills(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
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
		out.name(TOOL_RESULT_TOKENS__PROP);
		out.value(getToolResultTokens());
		out.name(TOOL_COUNT__PROP);
		out.value(getToolCount());
		out.name(LOADED_SKILLS__PROP);
		out.beginArray();
		for (String x : getLoadedSkills()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SYSTEM_PROMPT_TOKENS__PROP: setSystemPromptTokens(in.nextInt()); break;
			case TOOL_DEFINITION_TOKENS__PROP: setToolDefinitionTokens(in.nextInt()); break;
			case MESSAGE_TOKENS__PROP: setMessageTokens(in.nextInt()); break;
			case TOOL_RESULT_TOKENS__PROP: setToolResultTokens(in.nextInt()); break;
			case TOOL_COUNT__PROP: setToolCount(in.nextInt()); break;
			case LOADED_SKILLS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setLoadedSkills(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
