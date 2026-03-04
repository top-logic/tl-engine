package com.top_logic.ai.service.tools.task.messages;

/**
 * Detailed task information returned by getTask.
 */
public class TaskDetail extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.service.tools.task.messages.TaskDetail} instance.
	 */
	public static com.top_logic.ai.service.tools.task.messages.TaskDetail create() {
		return new com.top_logic.ai.service.tools.task.messages.TaskDetail();
	}

	/** Identifier for the {@link com.top_logic.ai.service.tools.task.messages.TaskDetail} type in JSON format. */
	public static final String TASK_DETAIL__TYPE = "TaskDetail";

	/** @see #getTaskId() */
	public static final String TASK_ID__PROP = "taskId";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	/** @see #getSubject() */
	public static final String SUBJECT__PROP = "subject";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	/** @see #getActiveForm() */
	public static final String ACTIVE_FORM__PROP = "activeForm";

	/** @see #getOwner() */
	public static final String OWNER__PROP = "owner";

	/** @see #getBlocks() */
	public static final String BLOCKS__PROP = "blocks";

	/** @see #getBlockedBy() */
	public static final String BLOCKED_BY__PROP = "blockedBy";

	private String _taskId = "";

	private String _status = "";

	private String _subject = "";

	private String _description = "";

	private String _activeForm = "";

	private String _owner = "";

	private final java.util.List<String> _blocks = new java.util.ArrayList<>();

	private final java.util.List<String> _blockedBy = new java.util.ArrayList<>();

	/**
	 * Creates a {@link TaskDetail} instance.
	 *
	 * @see com.top_logic.ai.service.tools.task.messages.TaskDetail#create()
	 */
	protected TaskDetail() {
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
	public com.top_logic.ai.service.tools.task.messages.TaskDetail setTaskId(String value) {
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
	public com.top_logic.ai.service.tools.task.messages.TaskDetail setStatus(String value) {
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
	public com.top_logic.ai.service.tools.task.messages.TaskDetail setSubject(String value) {
		internalSetSubject(value);
		return this;
	}

	/** Internal setter for {@link #getSubject()} without chain call utility. */
	protected final void internalSetSubject(String value) {
		_subject = value;
	}

	/**
	 * Detailed description of what needs to be done.
	 */
	public final String getDescription() {
		return _description;
	}

	/**
	 * @see #getDescription()
	 */
	public com.top_logic.ai.service.tools.task.messages.TaskDetail setDescription(String value) {
		internalSetDescription(value);
		return this;
	}

	/** Internal setter for {@link #getDescription()} without chain call utility. */
	protected final void internalSetDescription(String value) {
		_description = value;
	}

	/**
	 * Present continuous form shown in spinner when task is in progress.
	 */
	public final String getActiveForm() {
		return _activeForm;
	}

	/**
	 * @see #getActiveForm()
	 */
	public com.top_logic.ai.service.tools.task.messages.TaskDetail setActiveForm(String value) {
		internalSetActiveForm(value);
		return this;
	}

	/** Internal setter for {@link #getActiveForm()} without chain call utility. */
	protected final void internalSetActiveForm(String value) {
		_activeForm = value;
	}

	/**
	 * Agent or user assigned to the task.
	 */
	public final String getOwner() {
		return _owner;
	}

	/**
	 * @see #getOwner()
	 */
	public com.top_logic.ai.service.tools.task.messages.TaskDetail setOwner(String value) {
		internalSetOwner(value);
		return this;
	}

	/** Internal setter for {@link #getOwner()} without chain call utility. */
	protected final void internalSetOwner(String value) {
		_owner = value;
	}

	/**
	 * Task IDs that this task blocks (cannot start until this one completes).
	 */
	public final java.util.List<String> getBlocks() {
		return _blocks;
	}

	/**
	 * @see #getBlocks()
	 */
	public com.top_logic.ai.service.tools.task.messages.TaskDetail setBlocks(java.util.List<? extends String> value) {
		internalSetBlocks(value);
		return this;
	}

	/** Internal setter for {@link #getBlocks()} without chain call utility. */
	protected final void internalSetBlocks(java.util.List<? extends String> value) {
		_blocks.clear();
		_blocks.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getBlocks()} list.
	 */
	public com.top_logic.ai.service.tools.task.messages.TaskDetail addBlock(String value) {
		internalAddBlock(value);
		return this;
	}

	/** Implementation of {@link #addBlock(String)} without chain call utility. */
	protected final void internalAddBlock(String value) {
		_blocks.add(value);
	}

	/**
	 * Removes a value from the {@link #getBlocks()} list.
	 */
	public final void removeBlock(String value) {
		_blocks.remove(value);
	}

	/**
	 * Task IDs that must complete before this task can start.
	 */
	public final java.util.List<String> getBlockedBy() {
		return _blockedBy;
	}

	/**
	 * @see #getBlockedBy()
	 */
	public com.top_logic.ai.service.tools.task.messages.TaskDetail setBlockedBy(java.util.List<? extends String> value) {
		internalSetBlockedBy(value);
		return this;
	}

	/** Internal setter for {@link #getBlockedBy()} without chain call utility. */
	protected final void internalSetBlockedBy(java.util.List<? extends String> value) {
		_blockedBy.clear();
		_blockedBy.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getBlockedBy()} list.
	 */
	public com.top_logic.ai.service.tools.task.messages.TaskDetail addBlockedBy(String value) {
		internalAddBlockedBy(value);
		return this;
	}

	/** Implementation of {@link #addBlockedBy(String)} without chain call utility. */
	protected final void internalAddBlockedBy(String value) {
		_blockedBy.add(value);
	}

	/**
	 * Removes a value from the {@link #getBlockedBy()} list.
	 */
	public final void removeBlockedBy(String value) {
		_blockedBy.remove(value);
	}

	@Override
	public String jsonType() {
		return TASK_DETAIL__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TASK_ID__PROP, 
			STATUS__PROP, 
			SUBJECT__PROP, 
			DESCRIPTION__PROP, 
			ACTIVE_FORM__PROP, 
			OWNER__PROP, 
			BLOCKS__PROP, 
			BLOCKED_BY__PROP);
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
			case DESCRIPTION__PROP: return getDescription();
			case ACTIVE_FORM__PROP: return getActiveForm();
			case OWNER__PROP: return getOwner();
			case BLOCKS__PROP: return getBlocks();
			case BLOCKED_BY__PROP: return getBlockedBy();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TASK_ID__PROP: internalSetTaskId((String) value); break;
			case STATUS__PROP: internalSetStatus((String) value); break;
			case SUBJECT__PROP: internalSetSubject((String) value); break;
			case DESCRIPTION__PROP: internalSetDescription((String) value); break;
			case ACTIVE_FORM__PROP: internalSetActiveForm((String) value); break;
			case OWNER__PROP: internalSetOwner((String) value); break;
			case BLOCKS__PROP: internalSetBlocks(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case BLOCKED_BY__PROP: internalSetBlockedBy(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.service.tools.task.messages.TaskDetail readTaskDetail(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.service.tools.task.messages.TaskDetail result = new com.top_logic.ai.service.tools.task.messages.TaskDetail();
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
		out.name(DESCRIPTION__PROP);
		out.value(getDescription());
		out.name(ACTIVE_FORM__PROP);
		out.value(getActiveForm());
		out.name(OWNER__PROP);
		out.value(getOwner());
		out.name(BLOCKS__PROP);
		out.beginArray();
		for (String x : getBlocks()) {
			out.value(x);
		}
		out.endArray();
		out.name(BLOCKED_BY__PROP);
		out.beginArray();
		for (String x : getBlockedBy()) {
			out.value(x);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TASK_ID__PROP: setTaskId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SUBJECT__PROP: setSubject(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ACTIVE_FORM__PROP: setActiveForm(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case OWNER__PROP: setOwner(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case BLOCKS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setBlocks(newValue);
			}
			break;
			case BLOCKED_BY__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setBlockedBy(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
