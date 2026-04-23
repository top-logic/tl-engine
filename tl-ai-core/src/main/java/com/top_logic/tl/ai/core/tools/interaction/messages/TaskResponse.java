package com.top_logic.tl.ai.core.tools.interaction.messages;

/**
 * Response returned by createTask and updateTask operations.
 */
public class TaskResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.interaction.messages.TaskResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.interaction.messages.TaskResponse create() {
		return new com.top_logic.tl.ai.core.tools.interaction.messages.TaskResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.interaction.messages.TaskResponse} type in JSON format. */
	public static final String TASK_RESPONSE__TYPE = "TaskResponse";

	/** @see #getTaskId() */
	public static final String TASK_ID__PROP = "taskId";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	/** @see #getSubject() */
	public static final String SUBJECT__PROP = "subject";

	private String _taskId = "";

	private String _status = "";

	private String _subject = "";

	/**
	 * Creates a {@link TaskResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.interaction.messages.TaskResponse#create()
	 */
	protected TaskResponse() {
		super();
	}

	/**
	 * Unique identifier of the task.
	 */
	public final String getTaskId() {
		return _taskId;
	}

	/**
	 * @see #getTaskId()
	 */
	public com.top_logic.tl.ai.core.tools.interaction.messages.TaskResponse setTaskId(String value) {
		internalSetTaskId(value);
		return this;
	}

	/** Internal setter for {@link #getTaskId()} without chain call utility. */
	protected final void internalSetTaskId(String value) {
		_taskId = value;
	}

	/**
	 * Current status of the task (pending, in_progress, completed).
	 */
	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.tl.ai.core.tools.interaction.messages.TaskResponse setStatus(String value) {
		internalSetStatus(value);
		return this;
	}

	/** Internal setter for {@link #getStatus()} without chain call utility. */
	protected final void internalSetStatus(String value) {
		_status = value;
	}

	/**
	 * Brief title of the task.
	 */
	public final String getSubject() {
		return _subject;
	}

	/**
	 * @see #getSubject()
	 */
	public com.top_logic.tl.ai.core.tools.interaction.messages.TaskResponse setSubject(String value) {
		internalSetSubject(value);
		return this;
	}

	/** Internal setter for {@link #getSubject()} without chain call utility. */
	protected final void internalSetSubject(String value) {
		_subject = value;
	}

	@Override
	public String jsonType() {
		return TASK_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TASK_ID__PROP, 
			STATUS__PROP, 
			SUBJECT__PROP);
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
			case STATUS__PROP: return getStatus();
			case SUBJECT__PROP: return getSubject();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TASK_ID__PROP: internalSetTaskId((String) value); break;
			case STATUS__PROP: internalSetStatus((String) value); break;
			case SUBJECT__PROP: internalSetSubject((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.interaction.messages.TaskResponse readTaskResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.interaction.messages.TaskResponse result = new com.top_logic.tl.ai.core.tools.interaction.messages.TaskResponse();
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
		out.name(STATUS__PROP);
		out.value(getStatus());
		out.name(SUBJECT__PROP);
		out.value(getSubject());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TASK_ID__PROP: setTaskId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SUBJECT__PROP: setSubject(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
