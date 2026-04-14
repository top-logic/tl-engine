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

	private String _runId = "";

	private String _model = "";

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

	@Override
	public String jsonType() {
		return RUN_STARTED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			RUN_ID__PROP, 
			MODEL__PROP);
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
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case RUN_ID__PROP: internalSetRunId((String) value); break;
			case MODEL__PROP: internalSetModel((String) value); break;
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
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case RUN_ID__PROP: setRunId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MODEL__PROP: setModel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
