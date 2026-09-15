package com.top_logic.ai.chat.api.messages;

/**
 * Request body for POST /chat/regenerate: answer the same message again.
 */
public class RegenerateRequest extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.RegenerateRequest} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.RegenerateRequest create() {
		return new com.top_logic.ai.chat.api.messages.RegenerateRequest();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.RegenerateRequest} type in JSON format. */
	public static final String REGENERATE_REQUEST__TYPE = "RegenerateRequest";

	/** @see #getSessionId() */
	public static final String SESSION_ID__PROP = "sessionId";

	/** @see #getAgent() */
	public static final String AGENT__PROP = "agent";

	/** @see #getModel() */
	public static final String MODEL__PROP = "model";

	/** @see #isMcpOverride() */
	public static final String MCP_OVERRIDE__PROP = "mcpOverride";

	/** @see #getMcpServers() */
	public static final String MCP_SERVERS__PROP = "mcpServers";

	/** @see #getEffort() */
	public static final String EFFORT__PROP = "effort";

	private String _sessionId = "";

	private String _agent = "";

	private String _model = "";

	private boolean _mcpOverride = false;

	private final java.util.List<String> _mcpServers = new java.util.ArrayList<>();

	private String _effort = "";

	/**
	 * Creates a {@link RegenerateRequest} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.RegenerateRequest#create()
	 */
	protected RegenerateRequest() {
		super();
	}

	/**
	 * Session whose last turn is to be answered again.
	 */
	public final String getSessionId() {
		return _sessionId;
	}

	/**
	 * @see #getSessionId()
	 */
	public com.top_logic.ai.chat.api.messages.RegenerateRequest setSessionId(String value) {
		internalSetSessionId(value);
		return this;
	}

	/** Internal setter for {@link #getSessionId()} without chain call utility. */
	protected final void internalSetSessionId(String value) {
		_sessionId = value;
	}

	/**
	 * Agent for the new run; empty keeps the chat entry point.
	 */
	public final String getAgent() {
		return _agent;
	}

	/**
	 * @see #getAgent()
	 */
	public com.top_logic.ai.chat.api.messages.RegenerateRequest setAgent(String value) {
		internalSetAgent(value);
		return this;
	}

	/** Internal setter for {@link #getAgent()} without chain call utility. */
	protected final void internalSetAgent(String value) {
		_agent = value;
	}

	/**
	 * Model for the new run; empty keeps the agent's assignment.
	 */
	public final String getModel() {
		return _model;
	}

	/**
	 * @see #getModel()
	 */
	public com.top_logic.ai.chat.api.messages.RegenerateRequest setModel(String value) {
		internalSetModel(value);
		return this;
	}

	/** Internal setter for {@link #getModel()} without chain call utility. */
	protected final void internalSetModel(String value) {
		_model = value;
	}

	/**
	 * Whether the named MCP servers replace the agent's configured ones.
	 */
	public final boolean isMcpOverride() {
		return _mcpOverride;
	}

	/**
	 * @see #isMcpOverride()
	 */
	public com.top_logic.ai.chat.api.messages.RegenerateRequest setMcpOverride(boolean value) {
		internalSetMcpOverride(value);
		return this;
	}

	/** Internal setter for {@link #isMcpOverride()} without chain call utility. */
	protected final void internalSetMcpOverride(boolean value) {
		_mcpOverride = value;
	}

	/**
	 * MCP servers for the new run.
	 */
	public final java.util.List<String> getMcpServers() {
		return _mcpServers;
	}

	/**
	 * @see #getMcpServers()
	 */
	public com.top_logic.ai.chat.api.messages.RegenerateRequest setMcpServers(java.util.List<? extends String> value) {
		internalSetMcpServers(value);
		return this;
	}

	/** Internal setter for {@link #getMcpServers()} without chain call utility. */
	protected final void internalSetMcpServers(java.util.List<? extends String> value) {
		_mcpServers.clear();
		_mcpServers.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getMcpServers()} list.
	 */
	public com.top_logic.ai.chat.api.messages.RegenerateRequest addMcpServer(String value) {
		internalAddMcpServer(value);
		return this;
	}

	/** Implementation of {@link #addMcpServer(String)} without chain call utility. */
	protected final void internalAddMcpServer(String value) {
		_mcpServers.add(value);
	}

	/**
	 * Removes a value from the {@link #getMcpServers()} list.
	 */
	public final void removeMcpServer(String value) {
		_mcpServers.remove(value);
	}

	/**
	 * How hard the model may think for the new run.
	 */
	public final String getEffort() {
		return _effort;
	}

	/**
	 * @see #getEffort()
	 */
	public com.top_logic.ai.chat.api.messages.RegenerateRequest setEffort(String value) {
		internalSetEffort(value);
		return this;
	}

	/** Internal setter for {@link #getEffort()} without chain call utility. */
	protected final void internalSetEffort(String value) {
		_effort = value;
	}

	@Override
	public String jsonType() {
		return REGENERATE_REQUEST__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SESSION_ID__PROP, 
			AGENT__PROP, 
			MODEL__PROP, 
			MCP_OVERRIDE__PROP, 
			MCP_SERVERS__PROP, 
			EFFORT__PROP);
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
			case SESSION_ID__PROP: return getSessionId();
			case AGENT__PROP: return getAgent();
			case MODEL__PROP: return getModel();
			case MCP_OVERRIDE__PROP: return isMcpOverride();
			case MCP_SERVERS__PROP: return getMcpServers();
			case EFFORT__PROP: return getEffort();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SESSION_ID__PROP: internalSetSessionId((String) value); break;
			case AGENT__PROP: internalSetAgent((String) value); break;
			case MODEL__PROP: internalSetModel((String) value); break;
			case MCP_OVERRIDE__PROP: internalSetMcpOverride((boolean) value); break;
			case MCP_SERVERS__PROP: internalSetMcpServers(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case EFFORT__PROP: internalSetEffort((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.RegenerateRequest readRegenerateRequest(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.RegenerateRequest result = new com.top_logic.ai.chat.api.messages.RegenerateRequest();
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
		out.name(SESSION_ID__PROP);
		out.value(getSessionId());
		out.name(AGENT__PROP);
		out.value(getAgent());
		out.name(MODEL__PROP);
		out.value(getModel());
		out.name(MCP_OVERRIDE__PROP);
		out.value(isMcpOverride());
		out.name(MCP_SERVERS__PROP);
		out.beginArray();
		for (String x : getMcpServers()) {
			out.value(x);
		}
		out.endArray();
		out.name(EFFORT__PROP);
		out.value(getEffort());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SESSION_ID__PROP: setSessionId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case AGENT__PROP: setAgent(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MODEL__PROP: setModel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MCP_OVERRIDE__PROP: setMcpOverride(in.nextBoolean()); break;
			case MCP_SERVERS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setMcpServers(newValue);
			}
			break;
			case EFFORT__PROP: setEffort(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
