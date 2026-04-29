package com.top_logic.ai.chat.api.messages;

/**
 * Current (in-progress or just-finished) task.
 */
public class CurrentTaskDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.CurrentTaskDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.CurrentTaskDto create() {
		return new com.top_logic.ai.chat.api.messages.CurrentTaskDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.CurrentTaskDto} type in JSON format. */
	public static final String CURRENT_TASK_DTO__TYPE = "CurrentTaskDto";

	/** @see #getTaskId() */
	public static final String TASK_ID__PROP = "taskId";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	/** @see #getDescription() */
	public static final String DESCRIPTION__PROP = "description";

	/** @see #getErrorMessage() */
	public static final String ERROR_MESSAGE__PROP = "errorMessage";

	/** @see #getTimeline() */
	public static final String TIMELINE__PROP = "timeline";

	/** @see #getTotalUsage() */
	public static final String TOTAL_USAGE__PROP = "totalUsage";

	/** @see #getThinkingText() */
	public static final String THINKING_TEXT__PROP = "thinkingText";

	private String _taskId = "";

	private String _status = "";

	private String _description = "";

	private String _errorMessage = "";

	private final java.util.List<com.top_logic.ai.chat.api.messages.TimelineEventDto> _timeline = new java.util.ArrayList<>();

	private com.top_logic.ai.chat.api.messages.TokenUsageDto _totalUsage = null;

	private String _thinkingText = "";

	/**
	 * Creates a {@link CurrentTaskDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.CurrentTaskDto#create()
	 */
	protected CurrentTaskDto() {
		super();
	}

	/**
	 * Unique task identifier.
	 */
	public final String getTaskId() {
		return _taskId;
	}

	/**
	 * @see #getTaskId()
	 */
	public com.top_logic.ai.chat.api.messages.CurrentTaskDto setTaskId(String value) {
		internalSetTaskId(value);
		return this;
	}

	/** Internal setter for {@link #getTaskId()} without chain call utility. */
	protected final void internalSetTaskId(String value) {
		_taskId = value;
	}

	/**
	 * "in_progress", "completed", "error".
	 */
	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.ai.chat.api.messages.CurrentTaskDto setStatus(String value) {
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
	public com.top_logic.ai.chat.api.messages.CurrentTaskDto setDescription(String value) {
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
	public com.top_logic.ai.chat.api.messages.CurrentTaskDto setErrorMessage(String value) {
		internalSetErrorMessage(value);
		return this;
	}

	/** Internal setter for {@link #getErrorMessage()} without chain call utility. */
	protected final void internalSetErrorMessage(String value) {
		_errorMessage = value;
	}

	/**
	 * List of timeline events (tool calls and agent calls) for this task.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.TimelineEventDto> getTimeline() {
		return _timeline;
	}

	/**
	 * @see #getTimeline()
	 */
	public com.top_logic.ai.chat.api.messages.CurrentTaskDto setTimeline(java.util.List<? extends com.top_logic.ai.chat.api.messages.TimelineEventDto> value) {
		internalSetTimeline(value);
		return this;
	}

	/** Internal setter for {@link #getTimeline()} without chain call utility. */
	protected final void internalSetTimeline(java.util.List<? extends com.top_logic.ai.chat.api.messages.TimelineEventDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'timeline' cannot be null.");
		_timeline.clear();
		_timeline.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getTimeline()} list.
	 */
	public com.top_logic.ai.chat.api.messages.CurrentTaskDto addTimeline(com.top_logic.ai.chat.api.messages.TimelineEventDto value) {
		internalAddTimeline(value);
		return this;
	}

	/** Implementation of {@link #addTimeline(com.top_logic.ai.chat.api.messages.TimelineEventDto)} without chain call utility. */
	protected final void internalAddTimeline(com.top_logic.ai.chat.api.messages.TimelineEventDto value) {
		_timeline.add(value);
	}

	/**
	 * Removes a value from the {@link #getTimeline()} list.
	 */
	public final void removeTimeline(com.top_logic.ai.chat.api.messages.TimelineEventDto value) {
		_timeline.remove(value);
	}

	/**
	 * Total token usage for this task's run.
	 */
	public final com.top_logic.ai.chat.api.messages.TokenUsageDto getTotalUsage() {
		return _totalUsage;
	}

	/**
	 * @see #getTotalUsage()
	 */
	public com.top_logic.ai.chat.api.messages.CurrentTaskDto setTotalUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto value) {
		internalSetTotalUsage(value);
		return this;
	}

	/** Internal setter for {@link #getTotalUsage()} without chain call utility. */
	protected final void internalSetTotalUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto value) {
		_totalUsage = value;
	}

	/**
	 * Checks, whether {@link #getTotalUsage()} has a value.
	 */
	public final boolean hasTotalUsage() {
		return _totalUsage != null;
	}

	/**
	 * Accumulated extended-thinking text from the LLM.
	 */
	public final String getThinkingText() {
		return _thinkingText;
	}

	/**
	 * @see #getThinkingText()
	 */
	public com.top_logic.ai.chat.api.messages.CurrentTaskDto setThinkingText(String value) {
		internalSetThinkingText(value);
		return this;
	}

	/** Internal setter for {@link #getThinkingText()} without chain call utility. */
	protected final void internalSetThinkingText(String value) {
		_thinkingText = value;
	}

	@Override
	public String jsonType() {
		return CURRENT_TASK_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TASK_ID__PROP, 
			STATUS__PROP, 
			DESCRIPTION__PROP, 
			ERROR_MESSAGE__PROP, 
			TIMELINE__PROP, 
			TOTAL_USAGE__PROP, 
			THINKING_TEXT__PROP);
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
			case TIMELINE__PROP: return getTimeline();
			case TOTAL_USAGE__PROP: return getTotalUsage();
			case THINKING_TEXT__PROP: return getThinkingText();
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
			case TIMELINE__PROP: internalSetTimeline(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.TimelineEventDto.class, value)); break;
			case TOTAL_USAGE__PROP: internalSetTotalUsage((com.top_logic.ai.chat.api.messages.TokenUsageDto) value); break;
			case THINKING_TEXT__PROP: internalSetThinkingText((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.CurrentTaskDto readCurrentTaskDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.CurrentTaskDto result = new com.top_logic.ai.chat.api.messages.CurrentTaskDto();
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
		out.name(TIMELINE__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.TimelineEventDto x : getTimeline()) {
			x.writeTo(out);
		}
		out.endArray();
		if (hasTotalUsage()) {
			out.name(TOTAL_USAGE__PROP);
			getTotalUsage().writeTo(out);
		}
		out.name(THINKING_TEXT__PROP);
		out.value(getThinkingText());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TASK_ID__PROP: setTaskId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DESCRIPTION__PROP: setDescription(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ERROR_MESSAGE__PROP: setErrorMessage(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TIMELINE__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.TimelineEventDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.TimelineEventDto.readTimelineEventDto(in));
				}
				in.endArray();
				setTimeline(newValue);
			}
			break;
			case TOTAL_USAGE__PROP: setTotalUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto.readTokenUsageDto(in)); break;
			case THINKING_TEXT__PROP: setThinkingText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
