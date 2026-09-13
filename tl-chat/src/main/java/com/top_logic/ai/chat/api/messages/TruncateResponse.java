package com.top_logic.ai.chat.api.messages;

/**
 * Response body for POST /chat/truncate and POST /chat/regenerate.
 */
public class TruncateResponse extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.TruncateResponse} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.TruncateResponse create() {
		return new com.top_logic.ai.chat.api.messages.TruncateResponse();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.TruncateResponse} type in JSON format. */
	public static final String TRUNCATE_RESPONSE__TYPE = "TruncateResponse";

	/** @see #getStatus() */
	public static final String STATUS__PROP = "status";

	/** @see #getRemovedTurns() */
	public static final String REMOVED_TURNS__PROP = "removedTurns";

	/** @see #getText() */
	public static final String TEXT__PROP = "text";

	/** @see #getTaskId() */
	public static final String TASK_ID__PROP = "taskId";

	private String _status = "";

	private int _removedTurns = 0;

	private String _text = "";

	private String _taskId = "";

	/**
	 * Creates a {@link TruncateResponse} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.TruncateResponse#create()
	 */
	protected TruncateResponse() {
		super();
	}

	/**
	 * "ok", "busy" or "error".
	 */
	public final String getStatus() {
		return _status;
	}

	/**
	 * @see #getStatus()
	 */
	public com.top_logic.ai.chat.api.messages.TruncateResponse setStatus(String value) {
		internalSetStatus(value);
		return this;
	}

	/** Internal setter for {@link #getStatus()} without chain call utility. */
	protected final void internalSetStatus(String value) {
		_status = value;
	}

	/**
	 * Number of turns removed.
	 */
	public final int getRemovedTurns() {
		return _removedTurns;
	}

	/**
	 * @see #getRemovedTurns()
	 */
	public com.top_logic.ai.chat.api.messages.TruncateResponse setRemovedTurns(int value) {
		internalSetRemovedTurns(value);
		return this;
	}

	/** Internal setter for {@link #getRemovedTurns()} without chain call utility. */
	protected final void internalSetRemovedTurns(int value) {
		_removedTurns = value;
	}

	/**
	 * The user text of the first removed turn, so the client can offer it for editing.
	 */
	public final String getText() {
		return _text;
	}

	/**
	 * @see #getText()
	 */
	public com.top_logic.ai.chat.api.messages.TruncateResponse setText(String value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(String value) {
		_text = value;
	}

	/**
	 * Task ID of the run started by a regenerate; empty for a plain truncate.
	 */
	public final String getTaskId() {
		return _taskId;
	}

	/**
	 * @see #getTaskId()
	 */
	public com.top_logic.ai.chat.api.messages.TruncateResponse setTaskId(String value) {
		internalSetTaskId(value);
		return this;
	}

	/** Internal setter for {@link #getTaskId()} without chain call utility. */
	protected final void internalSetTaskId(String value) {
		_taskId = value;
	}

	@Override
	public String jsonType() {
		return TRUNCATE_RESPONSE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			STATUS__PROP, 
			REMOVED_TURNS__PROP, 
			TEXT__PROP, 
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
			case STATUS__PROP: return getStatus();
			case REMOVED_TURNS__PROP: return getRemovedTurns();
			case TEXT__PROP: return getText();
			case TASK_ID__PROP: return getTaskId();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case STATUS__PROP: internalSetStatus((String) value); break;
			case REMOVED_TURNS__PROP: internalSetRemovedTurns((int) value); break;
			case TEXT__PROP: internalSetText((String) value); break;
			case TASK_ID__PROP: internalSetTaskId((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.TruncateResponse readTruncateResponse(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.TruncateResponse result = new com.top_logic.ai.chat.api.messages.TruncateResponse();
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
		out.name(STATUS__PROP);
		out.value(getStatus());
		out.name(REMOVED_TURNS__PROP);
		out.value(getRemovedTurns());
		out.name(TEXT__PROP);
		out.value(getText());
		out.name(TASK_ID__PROP);
		out.value(getTaskId());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case STATUS__PROP: setStatus(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case REMOVED_TURNS__PROP: setRemovedTurns(in.nextInt()); break;
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TASK_ID__PROP: setTaskId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
