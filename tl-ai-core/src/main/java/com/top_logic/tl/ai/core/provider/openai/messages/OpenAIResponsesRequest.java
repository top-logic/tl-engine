package com.top_logic.tl.ai.core.provider.openai.messages;

/**
 * OpenAI Responses API request body.
 */
public class OpenAIResponsesRequest extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest create() {
		return new com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest} type in JSON format. */
	public static final String OPEN_AIRESPONSES_REQUEST__TYPE = "OpenAIResponsesRequest";

	/** @see #getModel() */
	public static final String MODEL__PROP = "model";

	/** @see #getMaxOutputTokens() */
	public static final String MAX_OUTPUT_TOKENS__PROP = "max_output_tokens";

	/** @see #isStream() */
	public static final String STREAM__PROP = "stream";

	/** @see #isStore() */
	public static final String STORE__PROP = "store";

	/** @see #getInstructions() */
	public static final String INSTRUCTIONS__PROP = "instructions";

	/** @see #getReasoning() */
	public static final String REASONING__PROP = "reasoning";

	/** @see #getInput() */
	public static final String INPUT__PROP = "input";

	/** @see #getTools() */
	public static final String TOOLS__PROP = "tools";

	/** @see #getText() */
	public static final String TEXT__PROP = "text";

	/** @see #getToolChoice() */
	public static final String TOOL_CHOICE__PROP = "tool_choice";

	private String _model = "";

	private int _maxOutputTokens = 0;

	private boolean _stream = false;

	private boolean _store = false;

	private Object _instructions = null;

	private Object _reasoning = null;

	private Object _input = null;

	private Object _tools = null;

	private Object _text = null;

	private Object _toolChoice = null;

	/**
	 * Creates a {@link OpenAIResponsesRequest} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest#create()
	 */
	protected OpenAIResponsesRequest() {
		super();
	}

	public final String getModel() {
		return _model;
	}

	/**
	 * @see #getModel()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest setModel(String value) {
		internalSetModel(value);
		return this;
	}

	/** Internal setter for {@link #getModel()} without chain call utility. */
	protected final void internalSetModel(String value) {
		_model = value;
	}

	public final int getMaxOutputTokens() {
		return _maxOutputTokens;
	}

	/**
	 * @see #getMaxOutputTokens()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest setMaxOutputTokens(int value) {
		internalSetMaxOutputTokens(value);
		return this;
	}

	/** Internal setter for {@link #getMaxOutputTokens()} without chain call utility. */
	protected final void internalSetMaxOutputTokens(int value) {
		_maxOutputTokens = value;
	}

	public final boolean isStream() {
		return _stream;
	}

	/**
	 * @see #isStream()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest setStream(boolean value) {
		internalSetStream(value);
		return this;
	}

	/** Internal setter for {@link #isStream()} without chain call utility. */
	protected final void internalSetStream(boolean value) {
		_stream = value;
	}

	public final boolean isStore() {
		return _store;
	}

	/**
	 * @see #isStore()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest setStore(boolean value) {
		internalSetStore(value);
		return this;
	}

	/** Internal setter for {@link #isStore()} without chain call utility. */
	protected final void internalSetStore(boolean value) {
		_store = value;
	}

	public final Object getInstructions() {
		return _instructions;
	}

	/**
	 * @see #getInstructions()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest setInstructions(Object value) {
		internalSetInstructions(value);
		return this;
	}

	/** Internal setter for {@link #getInstructions()} without chain call utility. */
	protected final void internalSetInstructions(Object value) {
		_instructions = value;
	}

	/**
	 * Checks, whether {@link #getInstructions()} has a value.
	 */
	public final boolean hasInstructions() {
		return _instructions != null;
	}

	public final Object getReasoning() {
		return _reasoning;
	}

	/**
	 * @see #getReasoning()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest setReasoning(Object value) {
		internalSetReasoning(value);
		return this;
	}

	/** Internal setter for {@link #getReasoning()} without chain call utility. */
	protected final void internalSetReasoning(Object value) {
		_reasoning = value;
	}

	/**
	 * Checks, whether {@link #getReasoning()} has a value.
	 */
	public final boolean hasReasoning() {
		return _reasoning != null;
	}

	public final Object getInput() {
		return _input;
	}

	/**
	 * @see #getInput()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest setInput(Object value) {
		internalSetInput(value);
		return this;
	}

	/** Internal setter for {@link #getInput()} without chain call utility. */
	protected final void internalSetInput(Object value) {
		_input = value;
	}

	/**
	 * Checks, whether {@link #getInput()} has a value.
	 */
	public final boolean hasInput() {
		return _input != null;
	}

	public final Object getTools() {
		return _tools;
	}

	/**
	 * @see #getTools()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest setTools(Object value) {
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

	public final Object getText() {
		return _text;
	}

	/**
	 * @see #getText()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest setText(Object value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(Object value) {
		_text = value;
	}

	/**
	 * Checks, whether {@link #getText()} has a value.
	 */
	public final boolean hasText() {
		return _text != null;
	}

	public final Object getToolChoice() {
		return _toolChoice;
	}

	/**
	 * @see #getToolChoice()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest setToolChoice(Object value) {
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
		return OPEN_AIRESPONSES_REQUEST__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			MODEL__PROP, 
			MAX_OUTPUT_TOKENS__PROP, 
			STREAM__PROP, 
			STORE__PROP, 
			INSTRUCTIONS__PROP, 
			REASONING__PROP, 
			INPUT__PROP, 
			TOOLS__PROP, 
			TEXT__PROP, 
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
			case MAX_OUTPUT_TOKENS__PROP: return getMaxOutputTokens();
			case STREAM__PROP: return isStream();
			case STORE__PROP: return isStore();
			case INSTRUCTIONS__PROP: return getInstructions();
			case REASONING__PROP: return getReasoning();
			case INPUT__PROP: return getInput();
			case TOOLS__PROP: return getTools();
			case TEXT__PROP: return getText();
			case TOOL_CHOICE__PROP: return getToolChoice();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case MODEL__PROP: internalSetModel((String) value); break;
			case MAX_OUTPUT_TOKENS__PROP: internalSetMaxOutputTokens((int) value); break;
			case STREAM__PROP: internalSetStream((boolean) value); break;
			case STORE__PROP: internalSetStore((boolean) value); break;
			case INSTRUCTIONS__PROP: internalSetInstructions((Object) value); break;
			case REASONING__PROP: internalSetReasoning((Object) value); break;
			case INPUT__PROP: internalSetInput((Object) value); break;
			case TOOLS__PROP: internalSetTools((Object) value); break;
			case TEXT__PROP: internalSetText((Object) value); break;
			case TOOL_CHOICE__PROP: internalSetToolChoice((Object) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest readOpenAIResponsesRequest(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest result = new com.top_logic.tl.ai.core.provider.openai.messages.OpenAIResponsesRequest();
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
		out.name(MAX_OUTPUT_TOKENS__PROP);
		out.value(getMaxOutputTokens());
		out.name(STREAM__PROP);
		out.value(isStream());
		out.name(STORE__PROP);
		out.value(isStore());
		if (hasInstructions()) {
			out.name(INSTRUCTIONS__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getInstructions());
		}
		if (hasReasoning()) {
			out.name(REASONING__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getReasoning());
		}
		if (hasInput()) {
			out.name(INPUT__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getInput());
		}
		if (hasTools()) {
			out.name(TOOLS__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getTools());
		}
		if (hasText()) {
			out.name(TEXT__PROP);
			de.haumacher.msgbuf.json.JsonUtil.writeJsonValue(out, getText());
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
			case MAX_OUTPUT_TOKENS__PROP: setMaxOutputTokens(in.nextInt()); break;
			case STREAM__PROP: setStream(in.nextBoolean()); break;
			case STORE__PROP: setStore(in.nextBoolean()); break;
			case INSTRUCTIONS__PROP: setInstructions(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case REASONING__PROP: setReasoning(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case INPUT__PROP: setInput(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case TOOLS__PROP: setTools(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			case TOOL_CHOICE__PROP: setToolChoice(de.haumacher.msgbuf.json.JsonUtil.nextJsonValue(in)); break;
			default: super.readField(in, field);
		}
	}

}
