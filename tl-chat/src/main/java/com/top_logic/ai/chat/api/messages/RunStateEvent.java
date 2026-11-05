package com.top_logic.ai.chat.api.messages;

/**
 * SSE run_state event payload: the run of the conversation a window shows, as the server sees it
 * now.
 *
 * <p>
 * The stream replays nothing, so this is what a window that attaches - a reloaded page, a second
 * tab, a stream that was attached again after a silence - and a window that opens another chat
 * learn the run from: whether the agent is still working, the progress it has made, the approvals
 * and questions waiting for the user, and the background subagents with what they spend from.
 * </p>
 */
public class RunStateEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.RunStateEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.RunStateEvent create() {
		return new com.top_logic.ai.chat.api.messages.RunStateEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.RunStateEvent} type in JSON format. */
	public static final String RUN_STATE_EVENT__TYPE = "RunStateEvent";

	/** @see #getSessionId() */
	public static final String SESSION_ID__PROP = "sessionId";

	/** @see #isProcessing() */
	public static final String PROCESSING__PROP = "processing";

	/** @see #getTasksJson() */
	public static final String TASKS_JSON__PROP = "tasksJson";

	/** @see #getInteractions() */
	public static final String INTERACTIONS__PROP = "interactions";

	/** @see #getSubagents() */
	public static final String SUBAGENTS__PROP = "subagents";

	/** @see #getBudget() */
	public static final String BUDGET__PROP = "budget";

	private String _sessionId = "";

	private boolean _processing = false;

	private String _tasksJson = "";

	private final java.util.List<com.top_logic.ai.chat.api.messages.PendingInteractionDto> _interactions = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.ai.chat.api.messages.SubagentJobDto> _subagents = new java.util.ArrayList<>();

	private com.top_logic.ai.chat.api.messages.SubagentBudgetDto _budget = null;

	/**
	 * Creates a {@link RunStateEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.RunStateEvent#create()
	 */
	protected RunStateEvent() {
		super();
	}

	/**
	 * The chat this state is about; empty while the window shows no saved chat.
	 */
	public final String getSessionId() {
		return _sessionId;
	}

	/**
	 * @see #getSessionId()
	 */
	public com.top_logic.ai.chat.api.messages.RunStateEvent setSessionId(String value) {
		internalSetSessionId(value);
		return this;
	}

	/** Internal setter for {@link #getSessionId()} without chain call utility. */
	protected final void internalSetSessionId(String value) {
		_sessionId = value;
	}

	/**
	 * Whether the agent is still working on this chat - in this window or in another window of the
	 * account.
	 */
	public final boolean isProcessing() {
		return _processing;
	}

	/**
	 * @see #isProcessing()
	 */
	public com.top_logic.ai.chat.api.messages.RunStateEvent setProcessing(boolean value) {
		internalSetProcessing(value);
		return this;
	}

	/** Internal setter for {@link #isProcessing()} without chain call utility. */
	protected final void internalSetProcessing(boolean value) {
		_processing = value;
	}

	/**
	 * The chat's task list as JSON, in the format the <code>task_update</code> frame carries
	 * (<code>TaskStore.toJson()</code>); empty when there is none.
	 */
	public final String getTasksJson() {
		return _tasksJson;
	}

	/**
	 * @see #getTasksJson()
	 */
	public com.top_logic.ai.chat.api.messages.RunStateEvent setTasksJson(String value) {
		internalSetTasksJson(value);
		return this;
	}

	/** Internal setter for {@link #getTasksJson()} without chain call utility. */
	protected final void internalSetTasksJson(String value) {
		_tasksJson = value;
	}

	/**
	 * The approvals and questions of this chat waiting for the acting account, oldest first.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.PendingInteractionDto> getInteractions() {
		return _interactions;
	}

	/**
	 * @see #getInteractions()
	 */
	public com.top_logic.ai.chat.api.messages.RunStateEvent setInteractions(java.util.List<? extends com.top_logic.ai.chat.api.messages.PendingInteractionDto> value) {
		internalSetInteractions(value);
		return this;
	}

	/** Internal setter for {@link #getInteractions()} without chain call utility. */
	protected final void internalSetInteractions(java.util.List<? extends com.top_logic.ai.chat.api.messages.PendingInteractionDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'interactions' cannot be null.");
		_interactions.clear();
		_interactions.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getInteractions()} list.
	 */
	public com.top_logic.ai.chat.api.messages.RunStateEvent addInteraction(com.top_logic.ai.chat.api.messages.PendingInteractionDto value) {
		internalAddInteraction(value);
		return this;
	}

	/** Implementation of {@link #addInteraction(com.top_logic.ai.chat.api.messages.PendingInteractionDto)} without chain call utility. */
	protected final void internalAddInteraction(com.top_logic.ai.chat.api.messages.PendingInteractionDto value) {
		_interactions.add(value);
	}

	/**
	 * Removes a value from the {@link #getInteractions()} list.
	 */
	public final void removeInteraction(com.top_logic.ai.chat.api.messages.PendingInteractionDto value) {
		_interactions.remove(value);
	}

	/**
	 * Every background subagent the chat's registry knows, whatever its status.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.SubagentJobDto> getSubagents() {
		return _subagents;
	}

	/**
	 * @see #getSubagents()
	 */
	public com.top_logic.ai.chat.api.messages.RunStateEvent setSubagents(java.util.List<? extends com.top_logic.ai.chat.api.messages.SubagentJobDto> value) {
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
	public com.top_logic.ai.chat.api.messages.RunStateEvent addSubagent(com.top_logic.ai.chat.api.messages.SubagentJobDto value) {
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
	public com.top_logic.ai.chat.api.messages.RunStateEvent setBudget(com.top_logic.ai.chat.api.messages.SubagentBudgetDto value) {
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
		return RUN_STATE_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SESSION_ID__PROP, 
			PROCESSING__PROP, 
			TASKS_JSON__PROP, 
			INTERACTIONS__PROP, 
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
			case SESSION_ID__PROP: return getSessionId();
			case PROCESSING__PROP: return isProcessing();
			case TASKS_JSON__PROP: return getTasksJson();
			case INTERACTIONS__PROP: return getInteractions();
			case SUBAGENTS__PROP: return getSubagents();
			case BUDGET__PROP: return getBudget();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SESSION_ID__PROP: internalSetSessionId((String) value); break;
			case PROCESSING__PROP: internalSetProcessing((boolean) value); break;
			case TASKS_JSON__PROP: internalSetTasksJson((String) value); break;
			case INTERACTIONS__PROP: internalSetInteractions(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.PendingInteractionDto.class, value)); break;
			case SUBAGENTS__PROP: internalSetSubagents(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.SubagentJobDto.class, value)); break;
			case BUDGET__PROP: internalSetBudget((com.top_logic.ai.chat.api.messages.SubagentBudgetDto) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.RunStateEvent readRunStateEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.RunStateEvent result = new com.top_logic.ai.chat.api.messages.RunStateEvent();
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
		out.name(PROCESSING__PROP);
		out.value(isProcessing());
		out.name(TASKS_JSON__PROP);
		out.value(getTasksJson());
		out.name(INTERACTIONS__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.PendingInteractionDto x : getInteractions()) {
			x.writeTo(out);
		}
		out.endArray();
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
			case SESSION_ID__PROP: setSessionId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PROCESSING__PROP: setProcessing(in.nextBoolean()); break;
			case TASKS_JSON__PROP: setTasksJson(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case INTERACTIONS__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.PendingInteractionDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.PendingInteractionDto.readPendingInteractionDto(in));
				}
				in.endArray();
				setInteractions(newValue);
			}
			break;
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
