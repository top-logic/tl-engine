package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * A marker recording that history was compacted at this point.
 */
public class CompactionMarkerDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto} type in JSON format. */
	public static final String COMPACTION_MARKER_DTO__TYPE = "CompactionMarkerDto";

	/** @see #getMessagesSummarized() */
	public static final String MESSAGES_SUMMARIZED__PROP = "messagesSummarized";

	/** @see #getTokensBefore() */
	public static final String TOKENS_BEFORE__PROP = "tokensBefore";

	/** @see #getTrigger() */
	public static final String TRIGGER__PROP = "trigger";

	private int _messagesSummarized = 0;

	private int _tokensBefore = 0;

	private String _trigger = "";

	/**
	 * Creates a {@link CompactionMarkerDto} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto#create()
	 */
	protected CompactionMarkerDto() {
		super();
	}

	/**
	 * How many messages were replaced.
	 */
	public final int getMessagesSummarized() {
		return _messagesSummarized;
	}

	/**
	 * @see #getMessagesSummarized()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto setMessagesSummarized(int value) {
		internalSetMessagesSummarized(value);
		return this;
	}

	/** Internal setter for {@link #getMessagesSummarized()} without chain call utility. */
	protected final void internalSetMessagesSummarized(int value) {
		_messagesSummarized = value;
	}

	/**
	 * Token count before the compaction.
	 */
	public final int getTokensBefore() {
		return _tokensBefore;
	}

	/**
	 * @see #getTokensBefore()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto setTokensBefore(int value) {
		internalSetTokensBefore(value);
		return this;
	}

	/** Internal setter for {@link #getTokensBefore()} without chain call utility. */
	protected final void internalSetTokensBefore(int value) {
		_tokensBefore = value;
	}

	/**
	 * What triggered the compaction.
	 */
	public final String getTrigger() {
		return _trigger;
	}

	/**
	 * @see #getTrigger()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto setTrigger(String value) {
		internalSetTrigger(value);
		return this;
	}

	/** Internal setter for {@link #getTrigger()} without chain call utility. */
	protected final void internalSetTrigger(String value) {
		_trigger = value;
	}

	@Override
	public String jsonType() {
		return COMPACTION_MARKER_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			MESSAGES_SUMMARIZED__PROP, 
			TOKENS_BEFORE__PROP, 
			TRIGGER__PROP);
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
			case MESSAGES_SUMMARIZED__PROP: return getMessagesSummarized();
			case TOKENS_BEFORE__PROP: return getTokensBefore();
			case TRIGGER__PROP: return getTrigger();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case MESSAGES_SUMMARIZED__PROP: internalSetMessagesSummarized((int) value); break;
			case TOKENS_BEFORE__PROP: internalSetTokensBefore((int) value); break;
			case TRIGGER__PROP: internalSetTrigger((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto readCompactionMarkerDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto result = new com.top_logic.tl.ai.core.session.persist.messages.CompactionMarkerDto();
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
		out.name(MESSAGES_SUMMARIZED__PROP);
		out.value(getMessagesSummarized());
		out.name(TOKENS_BEFORE__PROP);
		out.value(getTokensBefore());
		out.name(TRIGGER__PROP);
		out.value(getTrigger());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case MESSAGES_SUMMARIZED__PROP: setMessagesSummarized(in.nextInt()); break;
			case TOKENS_BEFORE__PROP: setTokensBefore(in.nextInt()); break;
			case TRIGGER__PROP: setTrigger(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
