package com.top_logic.ai.chat.api.messages;

/**
 * Answer of GET /chat/subagents: the subagents of this window's chat, and what they spend from.
 */
public class SubagentListResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.SubagentListResponse} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.SubagentListResponse create() {
		return new com.top_logic.ai.chat.api.messages.SubagentListResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.SubagentListResponse} type in JSON format. */
	public static final String SUBAGENT_LIST_RESPONSE__TYPE = "SubagentListResponse";

	/** @see #getSubagents() */
	public static final String SUBAGENTS__PROP = "subagents";

	/** @see #getBudget() */
	public static final String BUDGET__PROP = "budget";

	private final java.util.List<com.top_logic.ai.chat.api.messages.SubagentJobDto> _subagents = new java.util.ArrayList<>();

	private com.top_logic.ai.chat.api.messages.SubagentBudgetDto _budget = null;

	/**
	 * Creates a {@link SubagentListResponse} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.SubagentListResponse#create()
	 */
	protected SubagentListResponse() {
		super();
	}

	/**
	 * Every subagent the chat's registry knows, whatever its status.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.SubagentJobDto> getSubagents() {
		return _subagents;
	}

	/**
	 * @see #getSubagents()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentListResponse setSubagents(java.util.List<? extends com.top_logic.ai.chat.api.messages.SubagentJobDto> value) {
		internalSetSubagents(value);
		return this;
	}

	/** Internal setter for {@link #getSubagents()} without chain call utility. */
	protected final void internalSetSubagents(java.util.List<? extends com.top_logic.ai.chat.api.messages.SubagentJobDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'subagents' cannot be null.");
		_subagents.clear();
		_subagents.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getSubagents()} list.
	 */
	public com.top_logic.ai.chat.api.messages.SubagentListResponse addSubagent(com.top_logic.ai.chat.api.messages.SubagentJobDto value) {
		internalAddSubagent(value);
		return this;
	}

	/** Implementation of {@link #addSubagent(com.top_logic.ai.chat.api.messages.SubagentJobDto)} without chain call utility. */
	protected final void internalAddSubagent(com.top_logic.ai.chat.api.messages.SubagentJobDto value) {
		_subagents.add(value);
	}

	/**
	 * Removes a value from the {@link #getSubagents()} list.
	 */
	public final void removeSubagent(com.top_logic.ai.chat.api.messages.SubagentJobDto value) {
		_subagents.remove(value);
	}

	/**
	 * The budget the subagents share; absent when no subagent's run is bounded.
	 */
	public final com.top_logic.ai.chat.api.messages.SubagentBudgetDto getBudget() {
		return _budget;
	}

	/**
	 * @see #getBudget()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentListResponse setBudget(com.top_logic.ai.chat.api.messages.SubagentBudgetDto value) {
		internalSetBudget(value);
		return this;
	}

	/** Internal setter for {@link #getBudget()} without chain call utility. */
	protected final void internalSetBudget(com.top_logic.ai.chat.api.messages.SubagentBudgetDto value) {
		_budget = value;
	}

	/**
	 * Checks, whether {@link #getBudget()} has a value.
	 */
	public final boolean hasBudget() {
		return _budget != null;
	}

	@Override
	public String jsonType() {
		return SUBAGENT_LIST_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SUBAGENTS__PROP, 
			BUDGET__PROP);
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
			case SUBAGENTS__PROP: return getSubagents();
			case BUDGET__PROP: return getBudget();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SUBAGENTS__PROP: internalSetSubagents(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.SubagentJobDto.class, value)); break;
			case BUDGET__PROP: internalSetBudget((com.top_logic.ai.chat.api.messages.SubagentBudgetDto) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.SubagentListResponse readSubagentListResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.SubagentListResponse result = new com.top_logic.ai.chat.api.messages.SubagentListResponse();
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
		out.name(SUBAGENTS__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.SubagentJobDto x : getSubagents()) {
			x.writeTo(out);
		}
		out.endArray();
		if (hasBudget()) {
			out.name(BUDGET__PROP);
			getBudget().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SUBAGENTS__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.SubagentJobDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.SubagentJobDto.readSubagentJobDto(in));
				}
				in.endArray();
				setSubagents(newValue);
			}
			break;
			case BUDGET__PROP: setBudget(com.top_logic.ai.chat.api.messages.SubagentBudgetDto.readSubagentBudgetDto(in)); break;
			default: super.readField(in, field);
		}
	}

}
