package com.top_logic.ai.chat.api.messages;

/**
 * SSE task status event payload.
 */
public class TaskStatusEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.TaskStatusEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.TaskStatusEvent create() {
		return new com.top_logic.ai.chat.api.messages.TaskStatusEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.TaskStatusEvent} type in JSON format. */
	public static final String TASK_STATUS_EVENT__TYPE = "TaskStatusEvent";

	/** @see #getTaskId() */
	public static final String TASK_ID__PROP = "taskId";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	/** @see #getErrorMessage() */
	public static final String ERROR_MESSAGE__PROP = "errorMessage";

	private String _taskId = "";

	private String _status = "";

	private String _description = "";

	private String _errorMessage = "";

	/**
	 * Creates a {@link TaskStatusEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.TaskStatusEvent#create()
	 */
	protected TaskStatusEvent() {
		super();
	}

	/**
	 * Task identifier.
	 */
	public final String getTaskId() {
		return _taskId;
	}

	/**
	 * @see #getTaskId()
	 */
	public com.top_logic.ai.chat.api.messages.TaskStatusEvent setTaskId(String value) {
		internalSetTaskId(value);
		return this;
	}

	/** Internal setter for {@link #getTaskId()} without chain call utility. */
	protected final void internalSetTaskId(String value) {
		_taskId = value;
	}

	/**
	 * Task status string (e.g. "in_progress", "completed", "error").
	 */
	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.ai.chat.api.messages.TaskStatusEvent setStatus(String value) {
		internalSetStatus(value);
		return this;
	}

	/** Internal setter for {@link #getStatus()} without chain call utility. */
	protected final void internalSetStatus(String value) {
		_status = value;
	}

	/**
	 * Human-readable task description.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.ai.chat.api.messages.TaskStatusEvent setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	/**
	 * Error message if status is "error"; null otherwise.
	 */
	public final String getErrorMessage() {
		return _errorMessage;
	}

	/**
	 * @see #getErrorMessage()
	 */
	public com.top_logic.ai.chat.api.messages.TaskStatusEvent setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	/** Internal setter for {@link #getErrorMessage()} without chain call utility. */
	protected final void internalSetErrorMessage(String value) {
		_errorMessage = value;
	}

	@Override
	public String jsonType() {
		return TASK_STATUS_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TASK_ID__PROP, 
			STATUS__PROP, 
			DESCRIPTION__PROP, 
			ERROR_MESSAGE__PROP);
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
			case DESCRIPTION__PROP: return getDescription();
			case ERROR_MESSAGE__PROP: return getErrorMessage();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TASK_ID__PROP: internalSetTaskId((String) value); break;
			case STATUS__PROP: internalSetStatus((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
			case ERROR_MESSAGE__PROP: internalSetErrorMessage((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.TaskStatusEvent readTaskStatusEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.TaskStatusEvent result = new com.top_logic.ai.chat.api.messages.TaskStatusEvent();
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
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
		out.name(ERROR_MESSAGE__PROP);
		out.value(getErrorMessage());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TASK_ID__PROP: setTaskId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ERROR_MESSAGE__PROP: setErrorMessage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
