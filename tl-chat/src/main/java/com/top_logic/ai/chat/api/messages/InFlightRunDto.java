package com.top_logic.ai.chat.api.messages;

/**
 * Where the run a snapshot was taken of had got to.
 */
public class InFlightRunDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.InFlightRunDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.InFlightRunDto create() {
		return new com.top_logic.ai.chat.api.messages.InFlightRunDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.InFlightRunDto} type in JSON format. */
	public static final String IN_FLIGHT_RUN_DTO__TYPE = "InFlightRunDto";

	/** @see #getStreamedChars() */
	public static final String STREAMED_CHARS__PROP = "streamedChars";

	private long _streamedChars = 0L;

	/**
	 * Creates a {@link InFlightRunDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.InFlightRunDto#create()
	 */
	protected InFlightRunDto() {
		super();
	}

	/**
	 * How many characters of text the run had streamed, recorded or not, when the snapshot was
	 * taken. A text delta whose <code>end</code> is not beyond this is already in the snapshot.
	 */
	public final long getStreamedChars() {
		return _streamedChars;
	}

	/**
	 * @see #getStreamedChars()
	 */
	public com.top_logic.ai.chat.api.messages.InFlightRunDto setStreamedChars(long value) {
		internalSetStreamedChars(value);
		return this;
	}

	/** Internal setter for {@link #getStreamedChars()} without chain call utility. */
	protected final void internalSetStreamedChars(long value) {
		_streamedChars = value;
	}

	@Override
	public String jsonType() {
		return IN_FLIGHT_RUN_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			STREAMED_CHARS__PROP);
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
			case STREAMED_CHARS__PROP: return getStreamedChars();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case STREAMED_CHARS__PROP: internalSetStreamedChars((long) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.InFlightRunDto readInFlightRunDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.InFlightRunDto result = new com.top_logic.ai.chat.api.messages.InFlightRunDto();
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
		out.name(STREAMED_CHARS__PROP);
		out.value(getStreamedChars());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case STREAMED_CHARS__PROP: setStreamedChars(in.nextLong()); break;
			default: super.readField(in, field);
		}
	}

}
