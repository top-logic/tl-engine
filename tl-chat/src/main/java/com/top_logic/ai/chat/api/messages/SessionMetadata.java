package com.top_logic.ai.chat.api.messages;

/**
 * Session metadata for persistence.
 */
public class SessionMetadata extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.SessionMetadata} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.SessionMetadata create() {
		return new com.top_logic.ai.chat.api.messages.SessionMetadata();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.SessionMetadata} type in JSON format. */
	public static final String SESSION_METADATA__TYPE = "SessionMetadata";

	/** @see #getApprovedTools() */
	public static final String APPROVED_TOOLS__PROP = "approvedTools";

	/** @see #getCumulativeUsage() */
	public static final String CUMULATIVE_USAGE__PROP = "cumulativeUsage";

	/** @see #getLastModel() */
	public static final String LAST_MODEL__PROP = "lastModel";

	/** @see #getApprovalMode() */
	public static final String APPROVAL_MODE__PROP = "approvalMode";

	/** @see #getTasksJson() */
	public static final String TASKS_JSON__PROP = "tasksJson";

	/** @see #getLoadedSkills() */
	public static final String LOADED_SKILLS__PROP = "loadedSkills";

	/** @see #getFeedback() */
	public static final String FEEDBACK__PROP = "feedback";

	/** @see #getLastEffort() */
	public static final String LAST_EFFORT__PROP = "lastEffort";

	/** @see #getLastSeen() */
	public static final String LAST_SEEN__PROP = "lastSeen";

	private final java.util.List<String> _approvedTools = new java.util.ArrayList<>();

	private com.top_logic.ai.chat.api.messages.TokenUsageDto _cumulativeUsage = null;

	private String _lastModel = "";

	private String _approvalMode = "";

	private String _tasksJson = "";

	private final java.util.List<String> _loadedSkills = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.ai.chat.api.messages.MessageFeedbackDto> _feedback = new java.util.ArrayList<>();

	private String _lastEffort = "";

	private long _lastSeen = 0L;

	/**
	 * Creates a {@link SessionMetadata} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.SessionMetadata#create()
	 */
	protected SessionMetadata() {
		super();
	}

	/**
	 * Tool names approved for this session.
	 */
	public final java.util.List<String> getApprovedTools() {
		return _approvedTools;
	}

	/**
	 * @see #getApprovedTools()
	 */
	public com.top_logic.ai.chat.api.messages.SessionMetadata setApprovedTools(java.util.List<? extends String> value) {
		internalSetApprovedTools(value);
		return this;
	}

	/** Internal setter for {@link #getApprovedTools()} without chain call utility. */
	protected final void internalSetApprovedTools(java.util.List<? extends String> value) {
		_approvedTools.clear();
		_approvedTools.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getApprovedTools()} list.
	 */
	public com.top_logic.ai.chat.api.messages.SessionMetadata addApprovedTool(String value) {
		internalAddApprovedTool(value);
		return this;
	}

	/** Implementation of {@link #addApprovedTool(String)} without chain call utility. */
	protected final void internalAddApprovedTool(String value) {
		_approvedTools.add(value);
	}

	/**
	 * Removes a value from the {@link #getApprovedTools()} list.
	 */
	public final void removeApprovedTool(String value) {
		_approvedTools.remove(value);
	}

	/**
	 * Cumulative token usage.
	 */
	public final com.top_logic.ai.chat.api.messages.TokenUsageDto getCumulativeUsage() {
		return _cumulativeUsage;
	}

	/**
	 * @see #getCumulativeUsage()
	 */
	public com.top_logic.ai.chat.api.messages.SessionMetadata setCumulativeUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto value) {
		internalSetCumulativeUsage(value);
		return this;
	}

	/** Internal setter for {@link #getCumulativeUsage()} without chain call utility. */
	protected final void internalSetCumulativeUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto value) {
		_cumulativeUsage = value;
	}

	/**
	 * Checks, whether {@link #getCumulativeUsage()} has a value.
	 */
	public final boolean hasCumulativeUsage() {
		return _cumulativeUsage != null;
	}

	/**
	 * Last model used.
	 */
	public final String getLastModel() {
		return _lastModel;
	}

	/**
	 * @see #getLastModel()
	 */
	public com.top_logic.ai.chat.api.messages.SessionMetadata setLastModel(String value) {
		internalSetLastModel(value);
		return this;
	}

	/** Internal setter for {@link #getLastModel()} without chain call utility. */
	protected final void internalSetLastModel(String value) {
		_lastModel = value;
	}

	/**
	 * Current approval mode (e.g. AUTO_APPROVE, ALWAYS_ASK).
	 */
	public final String getApprovalMode() {
		return _approvalMode;
	}

	/**
	 * @see #getApprovalMode()
	 */
	public com.top_logic.ai.chat.api.messages.SessionMetadata setApprovalMode(String value) {
		internalSetApprovalMode(value);
		return this;
	}

	/** Internal setter for {@link #getApprovalMode()} without chain call utility. */
	protected final void internalSetApprovalMode(String value) {
		_approvalMode = value;
	}

	/**
	 * Serialized <code>TaskStore</code> JSON (<code>PersistedTaskStore</code> format).
	 */
	public final String getTasksJson() {
		return _tasksJson;
	}

	/**
	 * @see #getTasksJson()
	 */
	public com.top_logic.ai.chat.api.messages.SessionMetadata setTasksJson(String value) {
		internalSetTasksJson(value);
		return this;
	}

	/** Internal setter for {@link #getTasksJson()} without chain call utility. */
	protected final void internalSetTasksJson(String value) {
		_tasksJson = value;
	}

	/**
	 * Skill IDs loaded in this session, for replay on restore.
	 */
	public final java.util.List<String> getLoadedSkills() {
		return _loadedSkills;
	}

	/**
	 * @see #getLoadedSkills()
	 */
	public com.top_logic.ai.chat.api.messages.SessionMetadata setLoadedSkills(java.util.List<? extends String> value) {
		internalSetLoadedSkills(value);
		return this;
	}

	/** Internal setter for {@link #getLoadedSkills()} without chain call utility. */
	protected final void internalSetLoadedSkills(java.util.List<? extends String> value) {
		_loadedSkills.clear();
		_loadedSkills.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getLoadedSkills()} list.
	 */
	public com.top_logic.ai.chat.api.messages.SessionMetadata addLoadedSkill(String value) {
		internalAddLoadedSkill(value);
		return this;
	}

	/** Implementation of {@link #addLoadedSkill(String)} without chain call utility. */
	protected final void internalAddLoadedSkill(String value) {
		_loadedSkills.add(value);
	}

	/**
	 * Removes a value from the {@link #getLoadedSkills()} list.
	 */
	public final void removeLoadedSkill(String value) {
		_loadedSkills.remove(value);
	}

	/**
	 * Answer ratings the user gave in this session.
	 *
	 * <p>
	 * Kept here rather than in the transcript because a transcript is written per run and a rating
	 * arrives after it: this bag is the session's own, is rewritten whole, and is therefore also
	 * where an evaluation reads the ratings back from.
	 * </p>
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.MessageFeedbackDto> getFeedback() {
		return _feedback;
	}

	/**
	 * @see #getFeedback()
	 */
	public com.top_logic.ai.chat.api.messages.SessionMetadata setFeedback(java.util.List<? extends com.top_logic.ai.chat.api.messages.MessageFeedbackDto> value) {
		internalSetFeedback(value);
		return this;
	}

	/** Internal setter for {@link #getFeedback()} without chain call utility. */
	protected final void internalSetFeedback(java.util.List<? extends com.top_logic.ai.chat.api.messages.MessageFeedbackDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'feedback' cannot be null.");
		_feedback.clear();
		_feedback.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getFeedback()} list.
	 */
	public com.top_logic.ai.chat.api.messages.SessionMetadata addFeedback(com.top_logic.ai.chat.api.messages.MessageFeedbackDto value) {
		internalAddFeedback(value);
		return this;
	}

	/** Implementation of {@link #addFeedback(com.top_logic.ai.chat.api.messages.MessageFeedbackDto)} without chain call utility. */
	protected final void internalAddFeedback(com.top_logic.ai.chat.api.messages.MessageFeedbackDto value) {
		_feedback.add(value);
	}

	/**
	 * Removes a value from the {@link #getFeedback()} list.
	 */
	public final void removeFeedback(com.top_logic.ai.chat.api.messages.MessageFeedbackDto value) {
		_feedback.remove(value);
	}

	/**
	 * Effort level last chosen in this session.
	 */
	public final String getLastEffort() {
		return _lastEffort;
	}

	/**
	 * @see #getLastEffort()
	 */
	public com.top_logic.ai.chat.api.messages.SessionMetadata setLastEffort(String value) {
		internalSetLastEffort(value);
		return this;
	}

	/** Internal setter for {@link #getLastEffort()} without chain call utility. */
	protected final void internalSetLastEffort(String value) {
		_lastEffort = value;
	}

	/**
	 * When a window of the owner last showed this chat, in milliseconds since the epoch; 0 if never.
	 *
	 * <p>
	 * What tells a subagent's result the user has not looked at yet from one they have: the chat
	 * list marks a chat whose subagent ended after this.
	 * </p>
	 */
	public final long getLastSeen() {
		return _lastSeen;
	}

	/**
	 * @see #getLastSeen()
	 */
	public com.top_logic.ai.chat.api.messages.SessionMetadata setLastSeen(long value) {
		internalSetLastSeen(value);
		return this;
	}

	/** Internal setter for {@link #getLastSeen()} without chain call utility. */
	protected final void internalSetLastSeen(long value) {
		_lastSeen = value;
	}

	@Override
	public String jsonType() {
		return SESSION_METADATA__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			APPROVED_TOOLS__PROP, 
			CUMULATIVE_USAGE__PROP, 
			LAST_MODEL__PROP, 
			APPROVAL_MODE__PROP, 
			TASKS_JSON__PROP, 
			LOADED_SKILLS__PROP, 
			FEEDBACK__PROP, 
			LAST_EFFORT__PROP, 
			LAST_SEEN__PROP);
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
			case APPROVED_TOOLS__PROP: return getApprovedTools();
			case CUMULATIVE_USAGE__PROP: return getCumulativeUsage();
			case LAST_MODEL__PROP: return getLastModel();
			case APPROVAL_MODE__PROP: return getApprovalMode();
			case TASKS_JSON__PROP: return getTasksJson();
			case LOADED_SKILLS__PROP: return getLoadedSkills();
			case FEEDBACK__PROP: return getFeedback();
			case LAST_EFFORT__PROP: return getLastEffort();
			case LAST_SEEN__PROP: return getLastSeen();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case APPROVED_TOOLS__PROP: internalSetApprovedTools(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case CUMULATIVE_USAGE__PROP: internalSetCumulativeUsage((com.top_logic.ai.chat.api.messages.TokenUsageDto) value); break;
			case LAST_MODEL__PROP: internalSetLastModel((String) value); break;
			case APPROVAL_MODE__PROP: internalSetApprovalMode((String) value); break;
			case TASKS_JSON__PROP: internalSetTasksJson((String) value); break;
			case LOADED_SKILLS__PROP: internalSetLoadedSkills(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case FEEDBACK__PROP: internalSetFeedback(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.MessageFeedbackDto.class, value)); break;
			case LAST_EFFORT__PROP: internalSetLastEffort((String) value); break;
			case LAST_SEEN__PROP: internalSetLastSeen((long) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.SessionMetadata readSessionMetadata(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.SessionMetadata result = new com.top_logic.ai.chat.api.messages.SessionMetadata();
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
		out.name(APPROVED_TOOLS__PROP);
		out.beginArray();
		for (String x : getApprovedTools()) {
			out.value(x);
		}
		out.endArray();
		if (hasCumulativeUsage()) {
			out.name(CUMULATIVE_USAGE__PROP);
			getCumulativeUsage().writeTo(out);
		}
		out.name(LAST_MODEL__PROP);
		out.value(getLastModel());
		out.name(APPROVAL_MODE__PROP);
		out.value(getApprovalMode());
		out.name(TASKS_JSON__PROP);
		out.value(getTasksJson());
		out.name(LOADED_SKILLS__PROP);
		out.beginArray();
		for (String x : getLoadedSkills()) {
			out.value(x);
		}
		out.endArray();
		out.name(FEEDBACK__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.MessageFeedbackDto x : getFeedback()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(LAST_EFFORT__PROP);
		out.value(getLastEffort());
		out.name(LAST_SEEN__PROP);
		out.value(getLastSeen());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case APPROVED_TOOLS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setApprovedTools(newValue);
			}
			break;
			case CUMULATIVE_USAGE__PROP: setCumulativeUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto.readTokenUsageDto(in)); break;
			case LAST_MODEL__PROP: setLastModel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case APPROVAL_MODE__PROP: setApprovalMode(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TASKS_JSON__PROP: setTasksJson(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LOADED_SKILLS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setLoadedSkills(newValue);
			}
			break;
			case FEEDBACK__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.MessageFeedbackDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.MessageFeedbackDto.readMessageFeedbackDto(in));
				}
				in.endArray();
				setFeedback(newValue);
			}
			break;
			case LAST_EFFORT__PROP: setLastEffort(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case LAST_SEEN__PROP: setLastSeen(in.nextLong()); break;
			default: super.readField(in, field);
		}
	}

}
