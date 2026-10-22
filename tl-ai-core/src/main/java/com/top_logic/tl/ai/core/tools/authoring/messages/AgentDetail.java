package com.top_logic.tl.ai.core.tools.authoring.messages;

/**
 * Full detail of a single knowledge-base agent.
 */
public class AgentDetail extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail create() {
		return new com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail} type in JSON format. */
	public static final String AGENT_DETAIL__TYPE = "AgentDetail";

	/** @see #isExists() */
	public static final String EXISTS__PROP = "exists";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	/** @see #getSystemPrompt() */
	public static final String SYSTEM_PROMPT__PROP = "systemPrompt";

	/** @see #getTools() */
	public static final String TOOLS__PROP = "tools";

	/** @see #getMcpServers() */
	public static final String MCP_SERVERS__PROP = "mcpServers";

	/** @see #getSkills() */
	public static final String SKILLS__PROP = "skills";

	/** @see #getDelegations() */
	public static final String DELEGATIONS__PROP = "delegations";

	private boolean _exists = false;

	private String _name = "";

	private String _description = "";

	private String _systemPrompt = "";

	private final java.util.List<String> _tools = new java.util.ArrayList<>();

	private final java.util.List<String> _mcpServers = new java.util.ArrayList<>();

	private final java.util.List<String> _skills = new java.util.ArrayList<>();

	private final java.util.List<String> _delegations = new java.util.ArrayList<>();

	/**
	 * Creates a {@link AgentDetail} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail#create()
	 */
	protected AgentDetail() {
		super();
	}

	/**
	 * Whether an agent with this name exists in the knowledge base.
	 */
	public final boolean isExists() {
		return _exists;
	}

	/**
	 * @see #isExists()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail setExists(boolean value) {
		internalSetExists(value);
		return this;
	}

	/** Internal setter for {@link #isExists()} without chain call utility. */
	protected final void internalSetExists(boolean value) {
		_exists = value;
	}

	/**
	 * Agent name.
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	/**
	 * Agent description.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	/**
	 * System prompt.
	 */
	public final String getSystemPrompt() {
		return _systemPrompt;
	}

	/**
	 * @see #getSystemPrompt()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail setSystemPrompt(String value) {
		internalSetSystemPrompt(value);
		return this;
	}

	/** Internal setter for {@link #getSystemPrompt()} without chain call utility. */
	protected final void internalSetSystemPrompt(String value) {
		_systemPrompt = value;
	}

	/**
	 * Qualified tool names.
	 */
	public final java.util.List<String> getTools() {
		return _tools;
	}

	/**
	 * @see #getTools()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail setTools(java.util.List<? extends String> value) {
		internalSetTools(value);
		return this;
	}

	/** Internal setter for {@link #getTools()} without chain call utility. */
	protected final void internalSetTools(java.util.List<? extends String> value) {
		_tools.clear();
		_tools.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getTools()} list.
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail addTool(String value) {
		internalAddTool(value);
		return this;
	}

	/** Implementation of {@link #addTool(String)} without chain call utility. */
	protected final void internalAddTool(String value) {
		_tools.add(value);
	}

	/**
	 * Removes a value from the {@link #getTools()} list.
	 */
	public final void removeTool(String value) {
		_tools.remove(value);
	}

	/**
	 * MCP server names.
	 */
	public final java.util.List<String> getMcpServers() {
		return _mcpServers;
	}

	/**
	 * @see #getMcpServers()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail setMcpServers(java.util.List<? extends String> value) {
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
	public com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail addMcpServer(String value) {
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
	 * Skill names.
	 */
	public final java.util.List<String> getSkills() {
		return _skills;
	}

	/**
	 * @see #getSkills()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail setSkills(java.util.List<? extends String> value) {
		internalSetSkills(value);
		return this;
	}

	/** Internal setter for {@link #getSkills()} without chain call utility. */
	protected final void internalSetSkills(java.util.List<? extends String> value) {
		_skills.clear();
		_skills.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getSkills()} list.
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail addSkill(String value) {
		internalAddSkill(value);
		return this;
	}

	/** Implementation of {@link #addSkill(String)} without chain call utility. */
	protected final void internalAddSkill(String value) {
		_skills.add(value);
	}

	/**
	 * Removes a value from the {@link #getSkills()} list.
	 */
	public final void removeSkill(String value) {
		_skills.remove(value);
	}

	/**
	 * Names of agents this agent may delegate to.
	 */
	public final java.util.List<String> getDelegations() {
		return _delegations;
	}

	/**
	 * @see #getDelegations()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail setDelegations(java.util.List<? extends String> value) {
		internalSetDelegations(value);
		return this;
	}

	/** Internal setter for {@link #getDelegations()} without chain call utility. */
	protected final void internalSetDelegations(java.util.List<? extends String> value) {
		_delegations.clear();
		_delegations.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getDelegations()} list.
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail addDelegation(String value) {
		internalAddDelegation(value);
		return this;
	}

	/** Implementation of {@link #addDelegation(String)} without chain call utility. */
	protected final void internalAddDelegation(String value) {
		_delegations.add(value);
	}

	/**
	 * Removes a value from the {@link #getDelegations()} list.
	 */
	public final void removeDelegation(String value) {
		_delegations.remove(value);
	}

	@Override
	public String jsonType() {
		return AGENT_DETAIL__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			EXISTS__PROP, 
			NAME__PROP, 
			DESCRIPTION__PROP, 
			SYSTEM_PROMPT__PROP, 
			TOOLS__PROP, 
			MCP_SERVERS__PROP, 
			SKILLS__PROP, 
			DELEGATIONS__PROP);
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
			case EXISTS__PROP: return isExists();
			case NAME__PROP: return getName();
			case DESCRIPTION__PROP: return getDescription();
			case SYSTEM_PROMPT__PROP: return getSystemPrompt();
			case TOOLS__PROP: return getTools();
			case MCP_SERVERS__PROP: return getMcpServers();
			case SKILLS__PROP: return getSkills();
			case DELEGATIONS__PROP: return getDelegations();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case EXISTS__PROP: internalSetExists((boolean) value); break;
			case NAME__PROP: internalSetName((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
			case SYSTEM_PROMPT__PROP: internalSetSystemPrompt((String) value); break;
			case TOOLS__PROP: internalSetTools(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case MCP_SERVERS__PROP: internalSetMcpServers(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case SKILLS__PROP: internalSetSkills(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case DELEGATIONS__PROP: internalSetDelegations(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail readAgentDetail(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail result = new com.top_logic.tl.ai.core.tools.authoring.messages.AgentDetail();
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
		out.name(EXISTS__PROP);
		out.value(isExists());
		out.name(NAME__PROP);
		out.value(getName());
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
		out.name(SYSTEM_PROMPT__PROP);
		out.value(getSystemPrompt());
		out.name(TOOLS__PROP);
		out.beginArray();
		for (String x : getTools()) {
			out.value(x);
		}
		out.endArray();
		out.name(MCP_SERVERS__PROP);
		out.beginArray();
		for (String x : getMcpServers()) {
			out.value(x);
		}
		out.endArray();
		out.name(SKILLS__PROP);
		out.beginArray();
		for (String x : getSkills()) {
			out.value(x);
		}
		out.endArray();
		out.name(DELEGATIONS__PROP);
		out.beginArray();
		for (String x : getDelegations()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case EXISTS__PROP: setExists(in.nextBoolean()); break;
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SYSTEM_PROMPT__PROP: setSystemPrompt(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TOOLS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setTools(newValue);
			}
			break;
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
			case SKILLS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setSkills(newValue);
			}
			break;
			case DELEGATIONS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setDelegations(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
