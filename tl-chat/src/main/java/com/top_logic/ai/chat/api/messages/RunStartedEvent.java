package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for run started.
 */
public class RunStartedEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.RunStartedEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.RunStartedEvent create() {
		return new com.top_logic.ai.chat.api.messages.RunStartedEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.RunStartedEvent} type in JSON format. */
	public static final String RUN_STARTED_EVENT__TYPE = "RunStartedEvent";

	/** @see #getRunId() */
	public static final String RUN_ID__PROP = "runId";

	/** @see #getModel() */
	public static final String MODEL__PROP = "model";

	/** @see #getUsedTokens() */
	public static final String USED_TOKENS__PROP = "usedTokens";

	/** @see #getContextWindow() */
	public static final String CONTEXT_WINDOW__PROP = "contextWindow";

	private String _runId = "";

	private String _model = "";

	private int _usedTokens = 0;

	private int _contextWindow = 0;

	/**
	 * Creates a {@link RunStartedEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.RunStartedEvent#create()
	 */
	protected RunStartedEvent() {
		super();
	}

	/**
	 * The run ID.
	 */
	public final String getRunId() {
		return _runId;
	}

	/**
	 * @see #getRunId()
	 */
	public com.top_logic.ai.chat.api.messages.RunStartedEvent setRunId(String value) {
		internalSetRunId(value);
		return this;
	}

	/** Internal setter for {@link #getRunId()} without chain call utility. */
	protected final void internalSetRunId(String value) {
		_runId = value;
	}

	/**
	 * The model being used.
	 */
	public final String getModel() {
		return _model;
	}

	/**
	 * @see #getModel()
	 */
	public com.top_logic.ai.chat.api.messages.RunStartedEvent setModel(String value) {
		internalSetModel(value);
		return this;
	}

	/** Internal setter for {@link #getModel()} without chain call utility. */
	protected final void internalSetModel(String value) {
		_model = value;
	}

	/**
	 * Estimated tokens already in the conversation.
	 */
	public final int getUsedTokens() {
		return _usedTokens;
	}

	/**
	 * @see #getUsedTokens()
	 */
	public com.top_logic.ai.chat.api.messages.RunStartedEvent setUsedTokens(int value) {
		internalSetUsedTokens(value);
		return this;
	}

	/** Internal setter for {@link #getUsedTokens()} without chain call utility. */
	protected final void internalSetUsedTokens(int value) {
		_usedTokens = value;
	}

	/**
	 * Model context window in tokens.
	 */
	public final int getContextWindow() {
		return _contextWindow;
	}

	/**
	 * @see #getContextWindow()
	 */
	public com.top_logic.ai.chat.api.messages.RunStartedEvent setContextWindow(int value) {
		internalSetContextWindow(value);
		return this;
	}

	/** Internal setter for {@link #getContextWindow()} without chain call utility. */
	protected final void internalSetContextWindow(int value) {
		_contextWindow = value;
	}

	@Override
	public String jsonType() {
		return RUN_STARTED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			RUN_ID__PROP, 
			MODEL__PROP, 
			USED_TOKENS__PROP, 
			CONTEXT_WINDOW__PROP);
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
			case RUN_ID__PROP: return getRunId();
			case MODEL__PROP: return getModel();
			case USED_TOKENS__PROP: return getUsedTokens();
			case CONTEXT_WINDOW__PROP: return getContextWindow();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case RUN_ID__PROP: internalSetRunId((String) value); break;
			case MODEL__PROP: internalSetModel((String) value); break;
			case USED_TOKENS__PROP: internalSetUsedTokens((int) value); break;
			case CONTEXT_WINDOW__PROP: internalSetContextWindow((int) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.RunStartedEvent readRunStartedEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.RunStartedEvent result = new com.top_logic.ai.chat.api.messages.RunStartedEvent();
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
		out.name(RUN_ID__PROP);
		out.value(getRunId());
		out.name(MODEL__PROP);
		out.value(getModel());
		out.name(USED_TOKENS__PROP);
		out.value(getUsedTokens());
		out.name(CONTEXT_WINDOW__PROP);
		out.value(getContextWindow());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case RUN_ID__PROP: setRunId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MODEL__PROP: setModel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case USED_TOKENS__PROP: setUsedTokens(in.nextInt()); break;
			case CONTEXT_WINDOW__PROP: setContextWindow(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
