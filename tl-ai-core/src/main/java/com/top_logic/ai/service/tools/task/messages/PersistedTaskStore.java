package com.top_logic.ai.service.tools.task.messages;

/**
 * Full task store state for persistence.
 */
public class PersistedTaskStore extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.task.messages.PersistedTaskStore} instance.
	 */
	public static com.top_logic.ai.service.tools.task.messages.PersistedTaskStore create() {
		return new com.top_logic.ai.service.tools.task.messages.PersistedTaskStore();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.task.messages.PersistedTaskStore} type in JSON format. */
	public static final String PERSISTED_TASK_STORE__TYPE = "PersistedTaskStore";

	/** @see #getHighWaterMark() */
	public static final String HIGH_WATER_MARK__PROP = "highWaterMark";

	/** @see #getTasks() */
	public static final String TASKS__PROP = "tasks";

	private int _highWaterMark = 0;

	private final java.util.List<com.top_logic.ai.service.tools.task.messages.PersistedTask> _tasks = new java.util.ArrayList<>();

	/**
	 * Creates a {@link PersistedTaskStore} instance.
	 *
	 * @see com.top_logic.ai.service.tools.task.messages.PersistedTaskStore#create()
	 */
	protected PersistedTaskStore() {
		super();
	}

	/**
	 * Highest task ID ever assigned (prevents ID reuse after deletion).
	 */
	public final int getHighWaterMark() {
		return _highWaterMark;
	}

	/**
	 * @see #getHighWaterMark()
	 */
	public com.top_logic.ai.service.tools.task.messages.PersistedTaskStore setHighWaterMark(int value) {
		internalSetHighWaterMark(value);
		return this;
	}

	/** Internal setter for {@link #getHighWaterMark()} without chain call utility. */
	protected final void internalSetHighWaterMark(int value) {
		_highWaterMark = value;
	}

	/**
	 * All tasks in creation order.
	 */
	public final java.util.List<com.top_logic.ai.service.tools.task.messages.PersistedTask> getTasks() {
		return _tasks;
	}

	/**
	 * @see #getTasks()
	 */
	public com.top_logic.ai.service.tools.task.messages.PersistedTaskStore setTasks(java.util.List<? extends com.top_logic.ai.service.tools.task.messages.PersistedTask> value) {
		internalSetTasks(value);
		return this;
	}

	/** Internal setter for {@link #getTasks()} without chain call utility. */
	protected final void internalSetTasks(java.util.List<? extends com.top_logic.ai.service.tools.task.messages.PersistedTask> value) {
		if (value == null) throw new IllegalArgumentException("Property 'tasks' cannot be null.");
		_tasks.clear();
		_tasks.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getTasks()} list.
	 */
	public com.top_logic.ai.service.tools.task.messages.PersistedTaskStore addTask(com.top_logic.ai.service.tools.task.messages.PersistedTask value) {
		internalAddTask(value);
		return this;
	}

	/** Implementation of {@link #addTask(com.top_logic.ai.service.tools.task.messages.PersistedTask)} without chain call utility. */
	protected final void internalAddTask(com.top_logic.ai.service.tools.task.messages.PersistedTask value) {
		_tasks.add(value);
	}

	/**
	 * Removes a value from the {@link #getTasks()} list.
	 */
	public final void removeTask(com.top_logic.ai.service.tools.task.messages.PersistedTask value) {
		_tasks.remove(value);
	}

	@Override
	public String jsonType() {
		return PERSISTED_TASK_STORE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			HIGH_WATER_MARK__PROP, 
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
			case HIGH_WATER_MARK__PROP: return getHighWaterMark();
			case TASKS__PROP: return getTasks();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case HIGH_WATER_MARK__PROP: internalSetHighWaterMark((int) value); break;
			case TASKS__PROP: internalSetTasks(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.service.tools.task.messages.PersistedTask.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.task.messages.PersistedTaskStore readPersistedTaskStore(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.task.messages.PersistedTaskStore result = new com.top_logic.ai.service.tools.task.messages.PersistedTaskStore();
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
		out.name(HIGH_WATER_MARK__PROP);
		out.value(getHighWaterMark());
		out.name(TASKS__PROP);
		out.beginArray();
		for (com.top_logic.ai.service.tools.task.messages.PersistedTask x : getTasks()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case HIGH_WATER_MARK__PROP: setHighWaterMark(in.nextInt()); break;
			case TASKS__PROP: {
				java.util.List<com.top_logic.ai.service.tools.task.messages.PersistedTask> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.service.tools.task.messages.PersistedTask.readPersistedTask(in));
				}
				in.endArray();
				setTasks(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
