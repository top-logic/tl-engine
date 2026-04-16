package com.top_logic.ai.chat.api.messages;

/**
 * System event part.
 */
public class SystemPartDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.SystemPartDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.SystemPartDto create() {
		return new com.top_logic.ai.chat.api.messages.SystemPartDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.SystemPartDto} type in JSON format. */
	public static final String SYSTEM_PART_DTO__TYPE = "SystemPartDto";

	/** @see #getEventType() */
	public static final String EVENT_TYPE__PROP = "eventType";

	/** @see #getDetails() */
	public static final String DETAILS__PROP = "details";

	private String _eventType = "";

	private String _details = "";

	/**
	 * Creates a {@link SystemPartDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.SystemPartDto#create()
	 */
	protected SystemPartDto() {
		super();
	}

	/**
	 * The type of system event.
	 */
	public final String getEventType() {
		return _eventType;
	}

	/**
	 * @see #getEventType()
	 */
	public com.top_logic.ai.chat.api.messages.SystemPartDto setEventType(String value) {
		internalSetEventType(value);
		return this;
	}

	/** Internal setter for {@link #getEventType()} without chain call utility. */
	protected final void internalSetEventType(String value) {
		_eventType = value;
	}

	/**
	 * Optional details about the event.
	 */
	public final String getDetails() {
		return _details;
	}

	/**
	 * @see #getDetails()
	 */
	public com.top_logic.ai.chat.api.messages.SystemPartDto setDetails(String value) {
		internalSetDetails(value);
		return this;
	}

	/** Internal setter for {@link #getDetails()} without chain call utility. */
	protected final void internalSetDetails(String value) {
		_details = value;
	}

	@Override
	public String jsonType() {
		return SYSTEM_PART_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			EVENT_TYPE__PROP, 
			DETAILS__PROP);
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
			case EVENT_TYPE__PROP: return getEventType();
			case DETAILS__PROP: return getDetails();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case EVENT_TYPE__PROP: internalSetEventType((String) value); break;
			case DETAILS__PROP: internalSetDetails((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.SystemPartDto readSystemPartDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.SystemPartDto result = new com.top_logic.ai.chat.api.messages.SystemPartDto();
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
		out.name(EVENT_TYPE__PROP);
		out.value(getEventType());
		out.name(DETAILS__PROP);
		out.value(getDetails());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case EVENT_TYPE__PROP: setEventType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case DETAILS__PROP: setDetails(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
