package com.top_logic.tl.ai.core.tools.authoring.messages;

/**
 * Result of listing knowledge-base agents.
 */
public class AgentListResult extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.authoring.messages.AgentListResult} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.AgentListResult create() {
		return new com.top_logic.tl.ai.core.tools.authoring.messages.AgentListResult();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.authoring.messages.AgentListResult} type in JSON format. */
	public static final String AGENT_LIST_RESULT__TYPE = "AgentListResult";

	/** @see #getAgents() */
	public static final String AGENTS__PROP = "agents";

	private final java.util.List<com.top_logic.tl.ai.core.tools.authoring.messages.AgentSummary> _agents = new java.util.ArrayList<>();

	/**
	 * Creates a {@link AgentListResult} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.authoring.messages.AgentListResult#create()
	 */
	protected AgentListResult() {
		super();
	}

	/**
	 * The knowledge-base agents.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.authoring.messages.AgentSummary> getAgents() {
		return _agents;
	}

	/**
	 * @see #getAgents()
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.AgentListResult setAgents(java.util.List<? extends com.top_logic.tl.ai.core.tools.authoring.messages.AgentSummary> value) {
		internalSetAgents(value);
		return this;
	}

	/** Internal setter for {@link #getAgents()} without chain call utility. */
	protected final void internalSetAgents(java.util.List<? extends com.top_logic.tl.ai.core.tools.authoring.messages.AgentSummary> value) {
		if (value == null) throw new IllegalArgumentException("Property 'agents' cannot be null.");
		_agents.clear();
		_agents.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getAgents()} list.
	 */
	public com.top_logic.tl.ai.core.tools.authoring.messages.AgentListResult addAgent(com.top_logic.tl.ai.core.tools.authoring.messages.AgentSummary value) {
		internalAddAgent(value);
		return this;
	}

	/** Implementation of {@link #addAgent(com.top_logic.tl.ai.core.tools.authoring.messages.AgentSummary)} without chain call utility. */
	protected final void internalAddAgent(com.top_logic.tl.ai.core.tools.authoring.messages.AgentSummary value) {
		_agents.add(value);
	}

	/**
	 * Removes a value from the {@link #getAgents()} list.
	 */
	public final void removeAgent(com.top_logic.tl.ai.core.tools.authoring.messages.AgentSummary value) {
		_agents.remove(value);
	}

	@Override
	public String jsonType() {
		return AGENT_LIST_RESULT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			AGENTS__PROP);
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
			case AGENTS__PROP: return getAgents();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case AGENTS__PROP: internalSetAgents(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.authoring.messages.AgentSummary.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.authoring.messages.AgentListResult readAgentListResult(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.authoring.messages.AgentListResult result = new com.top_logic.tl.ai.core.tools.authoring.messages.AgentListResult();
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
		out.name(AGENTS__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.authoring.messages.AgentSummary x : getAgents()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case AGENTS__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.authoring.messages.AgentSummary> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.authoring.messages.AgentSummary.readAgentSummary(in));
				}
				in.endArray();
				setAgents(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
