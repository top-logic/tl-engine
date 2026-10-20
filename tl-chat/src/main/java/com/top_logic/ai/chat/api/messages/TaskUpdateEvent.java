package com.top_logic.ai.chat.api.messages;

/**
 * SSE event for task list update. Carries the full task list snapshot.
 */
public class TaskUpdateEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.TaskUpdateEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.TaskUpdateEvent create() {
		return new com.top_logic.ai.chat.api.messages.TaskUpdateEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.TaskUpdateEvent} type in JSON format. */
	public static final String TASK_UPDATE_EVENT__TYPE = "TaskUpdateEvent";

	/** @see #getTasksJson() */
	public static final String TASKS_JSON__PROP = "tasksJson";

	private String _tasksJson = "";

	/**
	 * Creates a {@link TaskUpdateEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.TaskUpdateEvent#create()
	 */
	protected TaskUpdateEvent() {
		super();
	}

	/**
	 * Full task list as JSON (<code>PersistedTaskStore</code> format from <code>TaskStore.toJson()</code>).
	 */
	public final String getTasksJson() {
		return _tasksJson;
	}

	/**
	 * @see #getTasksJson()
	 */
	public com.top_logic.ai.chat.api.messages.TaskUpdateEvent setTasksJson(String value) {
		internalSetTasksJson(value);
		return this;
	}

	/** Internal setter for {@link #getTasksJson()} without chain call utility. */
	protected final void internalSetTasksJson(String value) {
		_tasksJson = value;
	}

	@Override
	public String jsonType() {
		return TASK_UPDATE_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TASKS_JSON__PROP);
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
			case TASKS_JSON__PROP: return getTasksJson();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TASKS_JSON__PROP: internalSetTasksJson((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.TaskUpdateEvent readTaskUpdateEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.TaskUpdateEvent result = new com.top_logic.ai.chat.api.messages.TaskUpdateEvent();
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
		out.name(TASKS_JSON__PROP);
		out.value(getTasksJson());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TASKS_JSON__PROP: setTasksJson(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
