package com.top_logic.ai.chat.api.messages;

/**
 * SSE connected event payload.
 */
public class ConnectedEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.ConnectedEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.ConnectedEvent create() {
		return new com.top_logic.ai.chat.api.messages.ConnectedEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.ConnectedEvent} type in JSON format. */
	public static final String CONNECTED_EVENT__TYPE = "ConnectedEvent";

	/** @see #getMessageSeq() */
	public static final String MESSAGE_SEQ__PROP = "messageSeq";

	/** @see #getTimelineSeq() */
	public static final String TIMELINE_SEQ__PROP = "timelineSeq";

	/** @see #isProcessing() */
	public static final String PROCESSING__PROP = "processing";

	/** @see #getTaskId() */
	public static final String TASK_ID__PROP = "taskId";

	private int _messageSeq = 0;

	private int _timelineSeq = 0;

	private boolean _processing = false;

	private String _taskId = "";

	/**
	 * Creates a {@link ConnectedEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.ConnectedEvent#create()
	 */
	protected ConnectedEvent() {
		super();
	}

	/**
	 * Current message sequence number.
	 */
	public final int getMessageSeq() {
		return _messageSeq;
	}

	/**
	 * @see #getMessageSeq()
	 */
	public com.top_logic.ai.chat.api.messages.ConnectedEvent setMessageSeq(int value) {
		internalSetMessageSeq(value);
		return this;
	}

	/** Internal setter for {@link #getMessageSeq()} without chain call utility. */
	protected final void internalSetMessageSeq(int value) {
		_messageSeq = value;
	}

	/**
	 * Current timeline sequence number.
	 */
	public final int getTimelineSeq() {
		return _timelineSeq;
	}

	/**
	 * @see #getTimelineSeq()
	 */
	public com.top_logic.ai.chat.api.messages.ConnectedEvent setTimelineSeq(int value) {
		internalSetTimelineSeq(value);
		return this;
	}

	/** Internal setter for {@link #getTimelineSeq()} without chain call utility. */
	protected final void internalSetTimelineSeq(int value) {
		_timelineSeq = value;
	}

	/**
	 * Whether an AI task is currently in progress.
	 */
	public final boolean isProcessing() {
		return _processing;
	}

	/**
	 * @see #isProcessing()
	 */
	public com.top_logic.ai.chat.api.messages.ConnectedEvent setProcessing(boolean value) {
		internalSetProcessing(value);
		return this;
	}

	/** Internal setter for {@link #isProcessing()} without chain call utility. */
	protected final void internalSetProcessing(boolean value) {
		_processing = value;
	}

	/**
	 * Current task ID, or null if none.
	 */
	public final String getTaskId() {
		return _taskId;
	}

	/**
	 * @see #getTaskId()
	 */
	public com.top_logic.ai.chat.api.messages.ConnectedEvent setTaskId(String value) {
		internalSetTaskId(value);
		return this;
	}

	/** Internal setter for {@link #getTaskId()} without chain call utility. */
	protected final void internalSetTaskId(String value) {
		_taskId = value;
	}

	@Override
	public String jsonType() {
		return CONNECTED_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			MESSAGE_SEQ__PROP, 
			TIMELINE_SEQ__PROP, 
			PROCESSING__PROP, 
			TASK_ID__PROP);
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
			case MESSAGE_SEQ__PROP: return getMessageSeq();
			case TIMELINE_SEQ__PROP: return getTimelineSeq();
			case PROCESSING__PROP: return isProcessing();
			case TASK_ID__PROP: return getTaskId();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case MESSAGE_SEQ__PROP: internalSetMessageSeq((int) value); break;
			case TIMELINE_SEQ__PROP: internalSetTimelineSeq((int) value); break;
			case PROCESSING__PROP: internalSetProcessing((boolean) value); break;
			case TASK_ID__PROP: internalSetTaskId((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.ConnectedEvent readConnectedEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.ConnectedEvent result = new com.top_logic.ai.chat.api.messages.ConnectedEvent();
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
		out.name(MESSAGE_SEQ__PROP);
		out.value(getMessageSeq());
		out.name(TIMELINE_SEQ__PROP);
		out.value(getTimelineSeq());
		out.name(PROCESSING__PROP);
		out.value(isProcessing());
		out.name(TASK_ID__PROP);
		out.value(getTaskId());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case MESSAGE_SEQ__PROP: setMessageSeq(in.nextInt()); break;
			case TIMELINE_SEQ__PROP: setTimelineSeq(in.nextInt()); break;
			case PROCESSING__PROP: setProcessing(in.nextBoolean()); break;
			case TASK_ID__PROP: setTaskId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
