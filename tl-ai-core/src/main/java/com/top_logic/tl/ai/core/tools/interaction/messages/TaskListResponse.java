package com.top_logic.tl.ai.core.tools.interaction.messages;

/**
 * Response containing the full list of tasks, returned by <code>listTasks</code>.
 */
public class TaskListResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.tools.interaction.messages.TaskListResponse} instance.
	 */
	public static com.top_logic.tl.ai.core.tools.interaction.messages.TaskListResponse create() {
		return new com.top_logic.tl.ai.core.tools.interaction.messages.TaskListResponse();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.tools.interaction.messages.TaskListResponse} type in JSON format. */
	public static final String TASK_LIST_RESPONSE__TYPE = "TaskListResponse";

	/** @see #getTasks() */
	public static final String TASKS__PROP = "tasks";

	private final java.util.List<com.top_logic.tl.ai.core.tools.interaction.messages.TaskSummary> _tasks = new java.util.ArrayList<>();

	/**
	 * Creates a {@link TaskListResponse} instance.
	 *
	 * @see com.top_logic.tl.ai.core.tools.interaction.messages.TaskListResponse#create()
	 */
	protected TaskListResponse() {
		super();
	}

	/**
	 * Array of task summaries.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.tools.interaction.messages.TaskSummary> getTasks() {
		return _tasks;
	}

	/**
	 * @see #getTasks()
	 */
	public com.top_logic.tl.ai.core.tools.interaction.messages.TaskListResponse setTasks(java.util.List<? extends com.top_logic.tl.ai.core.tools.interaction.messages.TaskSummary> value) {
		internalSetTasks(value);
		return this;
	}

	/** Internal setter for {@link #getTasks()} without chain call utility. */
	protected final void internalSetTasks(java.util.List<? extends com.top_logic.tl.ai.core.tools.interaction.messages.TaskSummary> value) {
		if (value == null) throw new IllegalArgumentException("Property 'tasks' cannot be null.");
		_tasks.clear();
		_tasks.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getTasks()} list.
	 */
	public com.top_logic.tl.ai.core.tools.interaction.messages.TaskListResponse addTask(com.top_logic.tl.ai.core.tools.interaction.messages.TaskSummary value) {
		internalAddTask(value);
		return this;
	}

	/** Implementation of {@link #addTask(com.top_logic.tl.ai.core.tools.interaction.messages.TaskSummary)} without chain call utility. */
	protected final void internalAddTask(com.top_logic.tl.ai.core.tools.interaction.messages.TaskSummary value) {
		_tasks.add(value);
	}

	/**
	 * Removes a value from the {@link #getTasks()} list.
	 */
	public final void removeTask(com.top_logic.tl.ai.core.tools.interaction.messages.TaskSummary value) {
		_tasks.remove(value);
	}

	@Override
	public String jsonType() {
		return TASK_LIST_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TASKS__PROP);
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
			case TASKS__PROP: return getTasks();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TASKS__PROP: internalSetTasks(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.tools.interaction.messages.TaskSummary.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.tools.interaction.messages.TaskListResponse readTaskListResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.tools.interaction.messages.TaskListResponse result = new com.top_logic.tl.ai.core.tools.interaction.messages.TaskListResponse();
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
		out.name(TASKS__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.tools.interaction.messages.TaskSummary x : getTasks()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TASKS__PROP: {
				java.util.List<com.top_logic.tl.ai.core.tools.interaction.messages.TaskSummary> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.tools.interaction.messages.TaskSummary.readTaskSummary(in));
				}
				in.endArray();
				setTasks(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
