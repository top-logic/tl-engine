package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * One LLM conversation message.
 */
public class PersistedMessage extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage} type in JSON format. */
	public static final String PERSISTED_MESSAGE__TYPE = "PersistedMessage";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getTimestamp() */
	public static final String TIMESTAMP__PROP = "timestamp";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getUserData() */
	public static final String USER_DATA__PROP = "userData";

	/** @see #getAssistantData() */
	public static final String ASSISTANT_DATA__PROP = "assistantData";

	/** @see #getToolResultData() */
	public static final String TOOL_RESULT_DATA__PROP = "toolResultData";

	/** @see #getSystemData() */
	public static final String SYSTEM_DATA__PROP = "systemData";

	/** @see #getMarkerData() */
	public static final String MARKER_DATA__PROP = "markerData";

	private String _id = "";

	private long _timestamp = 0L;

	private String _type = "";

	private com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto _userData = null;

	private com.top_logic.tl.ai.core.session.persist.messages.AssistantMessageDto _assistantData = null;

	private com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto _toolResultData = null;

	private com.top_logic.tl.ai.core.session.persist.messages.SystemMessageDto _systemData = null;

	private com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto _markerData = null;

	/**
	 * Creates a {@link PersistedMessage} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage#create()
	 */
	protected PersistedMessage() {
		super();
	}

	/**
	 * Unique message identifier.
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * Timestamp when this message was created.
	 */
	public final long getTimestamp() {
		return _timestamp;
	}

	/**
	 * @see #getTimestamp()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage setTimestamp(long value) {
		internalSetTimestamp(value);
		return this;
	}

	/** Internal setter for {@link #getTimestamp()} without chain call utility. */
	protected final void internalSetTimestamp(long value) {
		_timestamp = value;
	}

	/**
	 * Discriminator for the message type.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * User message data.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto getUserData() {
		return _userData;
	}

	/**
	 * @see #getUserData()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage setUserData(com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto value) {
		internalSetUserData(value);
		return this;
	}

	/** Internal setter for {@link #getUserData()} without chain call utility. */
	protected final void internalSetUserData(com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto value) {
		_userData = value;
	}

	/**
	 * Checks, whether {@link #getUserData()} has a value.
	 */
	public final boolean hasUserData() {
		return _userData != null;
	}

	/**
	 * Assistant message data.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.AssistantMessageDto getAssistantData() {
		return _assistantData;
	}

	/**
	 * @see #getAssistantData()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage setAssistantData(com.top_logic.tl.ai.core.session.persist.messages.AssistantMessageDto value) {
		internalSetAssistantData(value);
		return this;
	}

	/** Internal setter for {@link #getAssistantData()} without chain call utility. */
	protected final void internalSetAssistantData(com.top_logic.tl.ai.core.session.persist.messages.AssistantMessageDto value) {
		_assistantData = value;
	}

	/**
	 * Checks, whether {@link #getAssistantData()} has a value.
	 */
	public final boolean hasAssistantData() {
		return _assistantData != null;
	}

	/**
	 * Tool result message data.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto getToolResultData() {
		return _toolResultData;
	}

	/**
	 * @see #getToolResultData()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage setToolResultData(com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto value) {
		internalSetToolResultData(value);
		return this;
	}

	/** Internal setter for {@link #getToolResultData()} without chain call utility. */
	protected final void internalSetToolResultData(com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto value) {
		_toolResultData = value;
	}

	/**
	 * Checks, whether {@link #getToolResultData()} has a value.
	 */
	public final boolean hasToolResultData() {
		return _toolResultData != null;
	}

	/**
	 * System message data.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.SystemMessageDto getSystemData() {
		return _systemData;
	}

	/**
	 * @see #getSystemData()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage setSystemData(com.top_logic.tl.ai.core.session.persist.messages.SystemMessageDto value) {
		internalSetSystemData(value);
		return this;
	}

	/** Internal setter for {@link #getSystemData()} without chain call utility. */
	protected final void internalSetSystemData(com.top_logic.tl.ai.core.session.persist.messages.SystemMessageDto value) {
		_systemData = value;
	}

	/**
	 * Checks, whether {@link #getSystemData()} has a value.
	 */
	public final boolean hasSystemData() {
		return _systemData != null;
	}

	/**
	 * Compaction marker data.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto getMarkerData() {
		return _markerData;
	}

	/**
	 * @see #getMarkerData()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage setMarkerData(com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto value) {
		internalSetMarkerData(value);
		return this;
	}

	/** Internal setter for {@link #getMarkerData()} without chain call utility. */
	protected final void internalSetMarkerData(com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto value) {
		_markerData = value;
	}

	/**
	 * Checks, whether {@link #getMarkerData()} has a value.
	 */
	public final boolean hasMarkerData() {
		return _markerData != null;
	}

	@Override
	public String jsonType() {
		return PERSISTED_MESSAGE__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			TIMESTAMP__PROP, 
			TYPE__PROP, 
			USER_DATA__PROP, 
			ASSISTANT_DATA__PROP, 
			TOOL_RESULT_DATA__PROP, 
			SYSTEM_DATA__PROP, 
			MARKER_DATA__PROP);
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
			case ID__PROP: return getId();
			case TIMESTAMP__PROP: return getTimestamp();
			case TYPE__PROP: return getType();
			case USER_DATA__PROP: return getUserData();
			case ASSISTANT_DATA__PROP: return getAssistantData();
			case TOOL_RESULT_DATA__PROP: return getToolResultData();
			case SYSTEM_DATA__PROP: return getSystemData();
			case MARKER_DATA__PROP: return getMarkerData();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case TIMESTAMP__PROP: internalSetTimestamp((long) value); break;
			case TYPE__PROP: internalSetType((String) value); break;
			case USER_DATA__PROP: internalSetUserData((com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto) value); break;
			case ASSISTANT_DATA__PROP: internalSetAssistantData((com.top_logic.tl.ai.core.session.persist.messages.AssistantMessageDto) value); break;
			case TOOL_RESULT_DATA__PROP: internalSetToolResultData((com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto) value); break;
			case SYSTEM_DATA__PROP: internalSetSystemData((com.top_logic.tl.ai.core.session.persist.messages.SystemMessageDto) value); break;
			case MARKER_DATA__PROP: internalSetMarkerData((com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage readPersistedMessage(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage result = new com.top_logic.tl.ai.core.session.persist.messages.PersistedMessage();
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
		out.name(ID__PROP);
		out.value(getId());
		out.name(TIMESTAMP__PROP);
		out.value(getTimestamp());
		out.name(TYPE__PROP);
		out.value(getType());
		if (hasUserData()) {
			out.name(USER_DATA__PROP);
			getUserData().writeTo(out);
		}
		if (hasAssistantData()) {
			out.name(ASSISTANT_DATA__PROP);
			getAssistantData().writeTo(out);
		}
		if (hasToolResultData()) {
			out.name(TOOL_RESULT_DATA__PROP);
			getToolResultData().writeTo(out);
		}
		if (hasSystemData()) {
			out.name(SYSTEM_DATA__PROP);
			getSystemData().writeTo(out);
		}
		if (hasMarkerData()) {
			out.name(MARKER_DATA__PROP);
			getMarkerData().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TIMESTAMP__PROP: setTimestamp(in.nextLong()); break;
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case USER_DATA__PROP: setUserData(com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto.readUserMessageDto(in)); break;
			case ASSISTANT_DATA__PROP: setAssistantData(com.top_logic.tl.ai.core.session.persist.messages.AssistantMessageDto.readAssistantMessageDto(in)); break;
			case TOOL_RESULT_DATA__PROP: setToolResultData(com.top_logic.tl.ai.core.session.persist.messages.ToolResultMessageDto.readToolResultMessageDto(in)); break;
			case SYSTEM_DATA__PROP: setSystemData(com.top_logic.tl.ai.core.session.persist.messages.SystemMessageDto.readSystemMessageDto(in)); break;
			case MARKER_DATA__PROP: setMarkerData(com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto.readCompactionMarkerDto(in)); break;
			default: super.readField(in, field);
		}
	}

}
