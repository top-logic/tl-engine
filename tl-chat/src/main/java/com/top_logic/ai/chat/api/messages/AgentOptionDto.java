package com.top_logic.ai.chat.api.messages;

/**
 * One selectable agent.
 */
public class AgentOptionDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.AgentOptionDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.AgentOptionDto create() {
		return new com.top_logic.ai.chat.api.messages.AgentOptionDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.AgentOptionDto} type in JSON format. */
	public static final String AGENT_OPTION_DTO__TYPE = "AgentOptionDto";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getLabel() */
	public static final String LABEL__PROP = "label";

	/** @see #getSummary() */
	public static final String SUMMARY__PROP = "summary";

	/** @see #getMcpServers() */
	public static final String MCP_SERVERS__PROP = "mcpServers";

	/** @see #getModel() */
	public static final String MODEL__PROP = "model";

	private String _name = "";

	private String _label = "";

	private String _summary = "";

	private final java.util.List<String> _mcpServers = new java.util.ArrayList<>();

	private String _model = "";

	/**
	 * Creates a {@link AgentOptionDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.AgentOptionDto#create()
	 */
	protected AgentOptionDto() {
		super();
	}

	/**
	 * Unique agent name - the identifier, not a label.
	 *
	 * What a request names, what a persisted session holds, and what other configuration references.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.ai.chat.api.messages.AgentOptionDto setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * What to call this agent in front of the user, in their language.
	 *
	 * Empty when the agent has no display name; the client then shows the identifier above.
	 */
	public final String getLabel() {
		return _label;
	}

	/**
	 * @see #getLabel()
	 */
	public com.top_logic.ai.chat.api.messages.AgentOptionDto setLabel(String value) {
		internalSetLabel(value);
		return this;
	}

	/** Internal setter for {@link #getLabel()} without chain call utility. */
	protected final void internalSetLabel(String value) {
		_label = value;
	}

	/**
	 * One localised line saying what this agent is for, written for the person choosing it.
	 *
	 * Empty when the agent has none; the picker then offers it by name alone. The agent's own
	 * description is a delegation instruction addressed to a model and is never sent here.
	 */
	public final String getSummary() {
		return _summary;
	}

	/**
	 * @see #getSummary()
	 */
	public com.top_logic.ai.chat.api.messages.AgentOptionDto setSummary(String value) {
		internalSetSummary(value);
		return this;
	}

	/** Internal setter for {@link #getSummary()} without chain call utility. */
	protected final void internalSetSummary(String value) {
		_summary = value;
	}

	/**
	 * MCP servers bound on the agent definition.
	 */
	public final java.util.List<String> getMcpServers() {
		return _mcpServers;
	}

	/**
	 * @see #getMcpServers()
	 */
	public com.top_logic.ai.chat.api.messages.AgentOptionDto setMcpServers(java.util.List<? extends String> value) {
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
	public com.top_logic.ai.chat.api.messages.AgentOptionDto addMcpServer(String value) {
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
	 * Resolved default model for this agent.
	 */
	public final String getModel() {
		return _model;
	}

	/**
	 * @see #getModel()
	 */
	public com.top_logic.ai.chat.api.messages.AgentOptionDto setModel(String value) {
		internalSetModel(value);
		return this;
	}

	/** Internal setter for {@link #getModel()} without chain call utility. */
	protected final void internalSetModel(String value) {
		_model = value;
	}

	@Override
	public String jsonType() {
		return AGENT_OPTION_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			NAME__PROP, 
			LABEL__PROP, 
			SUMMARY__PROP, 
			MCP_SERVERS__PROP, 
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
			case NAME__PROP: return getName();
			case LABEL__PROP: return getLabel();
			case SUMMARY__PROP: return getSummary();
			case MCP_SERVERS__PROP: return getMcpServers();
			case MODEL__PROP: return getModel();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case NAME__PROP: internalSetName((String) value); break;
			case LABEL__PROP: internalSetLabel((String) value); break;
			case SUMMARY__PROP: internalSetSummary((String) value); break;
			case MCP_SERVERS__PROP: internalSetMcpServers(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case MODEL__PROP: internalSetModel((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.AgentOptionDto readAgentOptionDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.AgentOptionDto result = new com.top_logic.ai.chat.api.messages.AgentOptionDto();
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
		out.name(NAME__PROP);
		out.value(getName());
		out.name(LABEL__PROP);
		out.value(getLabel());
		out.name(SUMMARY__PROP);
		out.value(getSummary());
		out.name(MCP_SERVERS__PROP);
		out.beginArray();
		for (String x : getMcpServers()) {
			out.value(x);
		}
		out.endArray();
		out.name(MODEL__PROP);
		out.value(getModel());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LABEL__PROP: setLabel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SUMMARY__PROP: setSummary(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
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
			case MODEL__PROP: setModel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
