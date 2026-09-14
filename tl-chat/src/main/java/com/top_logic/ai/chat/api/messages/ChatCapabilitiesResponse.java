package com.top_logic.ai.chat.api.messages;

/**
 * Catalog of agents, models and MCP servers the chat UI can offer.
 */
public class ChatCapabilitiesResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse create() {
		return new com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse} type in JSON format. */
	public static final String CHAT_CAPABILITIES_RESPONSE__TYPE = "ChatCapabilitiesResponse";

	/** @see #getEntryPointAgent() */
	public static final String ENTRY_POINT_AGENT__PROP = "entryPointAgent";

	/** @see #getAgents() */
	public static final String AGENTS__PROP = "agents";

	/** @see #getModels() */
	public static final String MODELS__PROP = "models";

	/** @see #getMcpServers() */
	public static final String MCP_SERVERS__PROP = "mcpServers";

	/** @see #isPromptImproverAvailable() */
	public static final String PROMPT_IMPROVER_AVAILABLE__PROP = "promptImproverAvailable";

	private String _entryPointAgent = "";

	private final java.util.List<com.top_logic.ai.chat.api.messages.AgentOptionDto> _agents = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.ai.chat.api.messages.ModelOptionDto> _models = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.ai.chat.api.messages.McpServerOptionDto> _mcpServers = new java.util.ArrayList<>();

	private boolean _promptImproverAvailable = false;

	/**
	 * Creates a {@link ChatCapabilitiesResponse} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse#create()
	 */
	protected ChatCapabilitiesResponse() {
		super();
	}

	/**
	 * Configured chat entry-point agent name.
	 */
	public final String getEntryPointAgent() {
		return _entryPointAgent;
	}

	/**
	 * @see #getEntryPointAgent()
	 */
	public com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse setEntryPointAgent(String value) {
		internalSetEntryPointAgent(value);
		return this;
	}

	/** Internal setter for {@link #getEntryPointAgent()} without chain call utility. */
	protected final void internalSetEntryPointAgent(String value) {
		_entryPointAgent = value;
	}

	/**
	 * Agents the current user may start a chat with.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.AgentOptionDto> getAgents() {
		return _agents;
	}

	/**
	 * @see #getAgents()
	 */
	public com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse setAgents(java.util.List<? extends com.top_logic.ai.chat.api.messages.AgentOptionDto> value) {
		internalSetAgents(value);
		return this;
	}

	/** Internal setter for {@link #getAgents()} without chain call utility. */
	protected final void internalSetAgents(java.util.List<? extends com.top_logic.ai.chat.api.messages.AgentOptionDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'agents' cannot be null.");
		_agents.clear();
		_agents.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getAgents()} list.
	 */
	public com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse addAgent(com.top_logic.ai.chat.api.messages.AgentOptionDto value) {
		internalAddAgent(value);
		return this;
	}

	/** Implementation of {@link #addAgent(com.top_logic.ai.chat.api.messages.AgentOptionDto)} without chain call utility. */
	protected final void internalAddAgent(com.top_logic.ai.chat.api.messages.AgentOptionDto value) {
		_agents.add(value);
	}

	/**
	 * Removes a value from the {@link #getAgents()} list.
	 */
	public final void removeAgent(com.top_logic.ai.chat.api.messages.AgentOptionDto value) {
		_agents.remove(value);
	}

	/**
	 * Models configured on LLM providers.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.ModelOptionDto> getModels() {
		return _models;
	}

	/**
	 * @see #getModels()
	 */
	public com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse setModels(java.util.List<? extends com.top_logic.ai.chat.api.messages.ModelOptionDto> value) {
		internalSetModels(value);
		return this;
	}

	/** Internal setter for {@link #getModels()} without chain call utility. */
	protected final void internalSetModels(java.util.List<? extends com.top_logic.ai.chat.api.messages.ModelOptionDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'models' cannot be null.");
		_models.clear();
		_models.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getModels()} list.
	 */
	public com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse addModel(com.top_logic.ai.chat.api.messages.ModelOptionDto value) {
		internalAddModel(value);
		return this;
	}

	/** Implementation of {@link #addModel(com.top_logic.ai.chat.api.messages.ModelOptionDto)} without chain call utility. */
	protected final void internalAddModel(com.top_logic.ai.chat.api.messages.ModelOptionDto value) {
		_models.add(value);
	}

	/**
	 * Removes a value from the {@link #getModels()} list.
	 */
	public final void removeModel(com.top_logic.ai.chat.api.messages.ModelOptionDto value) {
		_models.remove(value);
	}

	/**
	 * MCP servers known to the registry.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.McpServerOptionDto> getMcpServers() {
		return _mcpServers;
	}

	/**
	 * @see #getMcpServers()
	 */
	public com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse setMcpServers(java.util.List<? extends com.top_logic.ai.chat.api.messages.McpServerOptionDto> value) {
		internalSetMcpServers(value);
		return this;
	}

	/** Internal setter for {@link #getMcpServers()} without chain call utility. */
	protected final void internalSetMcpServers(java.util.List<? extends com.top_logic.ai.chat.api.messages.McpServerOptionDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'mcpServers' cannot be null.");
		_mcpServers.clear();
		_mcpServers.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getMcpServers()} list.
	 */
	public com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse addMcpServer(com.top_logic.ai.chat.api.messages.McpServerOptionDto value) {
		internalAddMcpServer(value);
		return this;
	}

	/** Implementation of {@link #addMcpServer(com.top_logic.ai.chat.api.messages.McpServerOptionDto)} without chain call utility. */
	protected final void internalAddMcpServer(com.top_logic.ai.chat.api.messages.McpServerOptionDto value) {
		_mcpServers.add(value);
	}

	/**
	 * Removes a value from the {@link #getMcpServers()} list.
	 */
	public final void removeMcpServer(com.top_logic.ai.chat.api.messages.McpServerOptionDto value) {
		_mcpServers.remove(value);
	}

	/**
	 * Whether the prompt improver would run for this account.
	 *
	 * <p>
	 * False when the model it runs on, or a server it declares, is forbidden to a role of this
	 * account: the improver is outside the agent allowlist - the product chose it, not the user - but
	 * the other two sections still decide, so its run would be refused. A client that offers the
	 * button anyway offers a refusal.
	 * </p>
	 */
	public final boolean isPromptImproverAvailable() {
		return _promptImproverAvailable;
	}

	/**
	 * @see #isPromptImproverAvailable()
	 */
	public com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse setPromptImproverAvailable(boolean value) {
		internalSetPromptImproverAvailable(value);
		return this;
	}

	/** Internal setter for {@link #isPromptImproverAvailable()} without chain call utility. */
	protected final void internalSetPromptImproverAvailable(boolean value) {
		_promptImproverAvailable = value;
	}

	@Override
	public String jsonType() {
		return CHAT_CAPABILITIES_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ENTRY_POINT_AGENT__PROP, 
			AGENTS__PROP, 
			MODELS__PROP, 
			MCP_SERVERS__PROP, 
			PROMPT_IMPROVER_AVAILABLE__PROP);
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
			case ENTRY_POINT_AGENT__PROP: return getEntryPointAgent();
			case AGENTS__PROP: return getAgents();
			case MODELS__PROP: return getModels();
			case MCP_SERVERS__PROP: return getMcpServers();
			case PROMPT_IMPROVER_AVAILABLE__PROP: return isPromptImproverAvailable();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ENTRY_POINT_AGENT__PROP: internalSetEntryPointAgent((String) value); break;
			case AGENTS__PROP: internalSetAgents(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.AgentOptionDto.class, value)); break;
			case MODELS__PROP: internalSetModels(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.ModelOptionDto.class, value)); break;
			case MCP_SERVERS__PROP: internalSetMcpServers(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.McpServerOptionDto.class, value)); break;
			case PROMPT_IMPROVER_AVAILABLE__PROP: internalSetPromptImproverAvailable((boolean) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse readChatCapabilitiesResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse result = new com.top_logic.ai.chat.api.messages.ChatCapabilitiesResponse();
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
		out.name(ENTRY_POINT_AGENT__PROP);
		out.value(getEntryPointAgent());
		out.name(AGENTS__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.AgentOptionDto x : getAgents()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(MODELS__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.ModelOptionDto x : getModels()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(MCP_SERVERS__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.McpServerOptionDto x : getMcpServers()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(PROMPT_IMPROVER_AVAILABLE__PROP);
		out.value(isPromptImproverAvailable());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ENTRY_POINT_AGENT__PROP: setEntryPointAgent(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case AGENTS__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.AgentOptionDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.AgentOptionDto.readAgentOptionDto(in));
				}
				in.endArray();
				setAgents(newValue);
			}
			break;
			case MODELS__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.ModelOptionDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.ModelOptionDto.readModelOptionDto(in));
				}
				in.endArray();
				setModels(newValue);
			}
			break;
			case MCP_SERVERS__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.McpServerOptionDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.McpServerOptionDto.readMcpServerOptionDto(in));
				}
				in.endArray();
				setMcpServers(newValue);
			}
			break;
			case PROMPT_IMPROVER_AVAILABLE__PROP: setPromptImproverAvailable(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
