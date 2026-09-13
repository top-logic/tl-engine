package com.top_logic.ai.chat.api.messages;

/**
 * One selectable model.
 */
public class ModelOptionDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ModelOptionDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ModelOptionDto create() {
		return new com.top_logic.ai.chat.api.messages.ModelOptionDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ModelOptionDto} type in JSON format. */
	public static final String MODEL_OPTION_DTO__TYPE = "ModelOptionDto";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getProvider() */
	public static final String PROVIDER__PROP = "provider";

	/** @see #getContextWindow() */
	public static final String CONTEXT_WINDOW__PROP = "contextWindow";

	/** @see #getMaxOutput() */
	public static final String MAX_OUTPUT__PROP = "maxOutput";

	private String _id = "";

	private String _provider = "";

	private int _contextWindow = 0;

	private int _maxOutput = 0;

	/**
	 * Creates a {@link ModelOptionDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ModelOptionDto#create()
	 */
	protected ModelOptionDto() {
		super();
	}

	/**
	 * Model identifier.
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.ai.chat.api.messages.ModelOptionDto setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * Provider key (anthropic, openai, ...).
	 */
	public final String getProvider() {
		return _provider;
	}

	/**
	 * @see #getProvider()
	 */
	public com.top_logic.ai.chat.api.messages.ModelOptionDto setProvider(String value) {
		internalSetProvider(value);
		return this;
	}

	/** Internal setter for {@link #getProvider()} without chain call utility. */
	protected final void internalSetProvider(String value) {
		_provider = value;
	}

	/**
	 * Context window in tokens.
	 */
	public final int getContextWindow() {
		return _contextWindow;
	}

	/**
	 * @see #getContextWindow()
	 */
	public com.top_logic.ai.chat.api.messages.ModelOptionDto setContextWindow(int value) {
		internalSetContextWindow(value);
		return this;
	}

	/** Internal setter for {@link #getContextWindow()} without chain call utility. */
	protected final void internalSetContextWindow(int value) {
		_contextWindow = value;
	}

	/**
	 * Maximum output tokens.
	 */
	public final int getMaxOutput() {
		return _maxOutput;
	}

	/**
	 * @see #getMaxOutput()
	 */
	public com.top_logic.ai.chat.api.messages.ModelOptionDto setMaxOutput(int value) {
		internalSetMaxOutput(value);
		return this;
	}

	/** Internal setter for {@link #getMaxOutput()} without chain call utility. */
	protected final void internalSetMaxOutput(int value) {
		_maxOutput = value;
	}

	@Override
	public String jsonType() {
		return MODEL_OPTION_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			PROVIDER__PROP, 
			CONTEXT_WINDOW__PROP, 
			MAX_OUTPUT__PROP);
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
			case ID__PROP: return getId();
			case PROVIDER__PROP: return getProvider();
			case CONTEXT_WINDOW__PROP: return getContextWindow();
			case MAX_OUTPUT__PROP: return getMaxOutput();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case PROVIDER__PROP: internalSetProvider((String) value); break;
			case CONTEXT_WINDOW__PROP: internalSetContextWindow((int) value); break;
			case MAX_OUTPUT__PROP: internalSetMaxOutput((int) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ModelOptionDto readModelOptionDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ModelOptionDto result = new com.top_logic.ai.chat.api.messages.ModelOptionDto();
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
		out.name(ID__PROP);
		out.value(getId());
		out.name(PROVIDER__PROP);
		out.value(getProvider());
		out.name(CONTEXT_WINDOW__PROP);
		out.value(getContextWindow());
		out.name(MAX_OUTPUT__PROP);
		out.value(getMaxOutput());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PROVIDER__PROP: setProvider(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CONTEXT_WINDOW__PROP: setContextWindow(in.nextInt()); break;
			case MAX_OUTPUT__PROP: setMaxOutput(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
