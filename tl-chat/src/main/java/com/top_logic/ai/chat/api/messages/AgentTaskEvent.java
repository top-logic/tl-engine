package com.top_logic.ai.chat.api.messages;

/**
 * SSE event payload for agent task create/update events.
 */
public class AgentTaskEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.AgentTaskEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.AgentTaskEvent create() {
		return new com.top_logic.ai.chat.api.messages.AgentTaskEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.AgentTaskEvent} type in JSON format. */
	public static final String AGENT_TASK_EVENT__TYPE = "AgentTaskEvent";

	/** @see #getTaskId() */
	public static final String TASK_ID__PROP = "taskId";

	/** @see #getSubject() */
	public static final String SUBJECT__PROP = "subject";

	/** @see #getActiveForm() */
	public static final String ACTIVE_FORM__PROP = "activeForm";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	/** @see #getOwner() */
	public static final String OWNER__PROP = "owner";

	private String _taskId = "";

	private String _subject = "";

	private String _activeForm = "";

	private String _status = "";

	private String _owner = "";

	/**
	 * Creates a {@link AgentTaskEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.AgentTaskEvent#create()
	 */
	protected AgentTaskEvent() {
		super();
	}

	/**
	 * Task ID.
	 */
	public final String getTaskId() {
		return _taskId;
	}

	/**
	 * @see #getTaskId()
	 */
	public com.top_logic.ai.chat.api.messages.AgentTaskEvent setTaskId(String value) {
		internalSetTaskId(value);
		return this;
	}

	/** Internal setter for {@link #getTaskId()} without chain call utility. */
	protected final void internalSetTaskId(String value) {
		_taskId = value;
	}

	/**
	 * Task subject/title.
	 */
	public final String getSubject() {
		return _subject;
	}

	/**
	 * @see #getSubject()
	 */
	public com.top_logic.ai.chat.api.messages.AgentTaskEvent setSubject(String value) {
		internalSetSubject(value);
		return this;
	}

	/** Internal setter for {@link #getSubject()} without chain call utility. */
	protected final void internalSetSubject(String value) {
		_subject = value;
	}

	/**
	 * Present continuous form for UI spinner display.
	 */
	public final String getActiveForm() {
		return _activeForm;
	}

	/**
	 * @see #getActiveForm()
	 */
	public com.top_logic.ai.chat.api.messages.AgentTaskEvent setActiveForm(String value) {
		internalSetActiveForm(value);
		return this;
	}

	/** Internal setter for {@link #getActiveForm()} without chain call utility. */
	protected final void internalSetActiveForm(String value) {
		_activeForm = value;
	}

	/**
	 * Current status: "pending", "in_progress", "completed", "deleted".
	 */
	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.ai.chat.api.messages.AgentTaskEvent setStatus(String value) {
		internalSetStatus(value);
		return this;
	}

	/** Internal setter for {@link #getStatus()} without chain call utility. */
	protected final void internalSetStatus(String value) {
		_status = value;
	}

	/**
	 * Assigned agent name, or empty.
	 */
	public final String getOwner() {
		return _owner;
	}

	/**
	 * @see #getOwner()
	 */
	public com.top_logic.ai.chat.api.messages.AgentTaskEvent setOwner(String value) {
		internalSetOwner(value);
		return this;
	}

	/** Internal setter for {@link #getOwner()} without chain call utility. */
	protected final void internalSetOwner(String value) {
		_owner = value;
	}

	@Override
	public String jsonType() {
		return AGENT_TASK_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TASK_ID__PROP, 
			SUBJECT__PROP, 
			ACTIVE_FORM__PROP, 
			STATUS__PROP, 
			OWNER__PROP);
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
			case TASK_ID__PROP: return getTaskId();
			case SUBJECT__PROP: return getSubject();
			case ACTIVE_FORM__PROP: return getActiveForm();
			case STATUS__PROP: return getStatus();
			case OWNER__PROP: return getOwner();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TASK_ID__PROP: internalSetTaskId((String) value); break;
			case SUBJECT__PROP: internalSetSubject((String) value); break;
			case ACTIVE_FORM__PROP: internalSetActiveForm((String) value); break;
			case STATUS__PROP: internalSetStatus((String) value); break;
			case OWNER__PROP: internalSetOwner((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.AgentTaskEvent readAgentTaskEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.AgentTaskEvent result = new com.top_logic.ai.chat.api.messages.AgentTaskEvent();
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
		out.name(TASK_ID__PROP);
		out.value(getTaskId());
		out.name(SUBJECT__PROP);
		out.value(getSubject());
		out.name(ACTIVE_FORM__PROP);
		out.value(getActiveForm());
		out.name(STATUS__PROP);
		out.value(getStatus());
		out.name(OWNER__PROP);
		out.value(getOwner());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TASK_ID__PROP: setTaskId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SUBJECT__PROP: setSubject(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ACTIVE_FORM__PROP: setActiveForm(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case OWNER__PROP: setOwner(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
