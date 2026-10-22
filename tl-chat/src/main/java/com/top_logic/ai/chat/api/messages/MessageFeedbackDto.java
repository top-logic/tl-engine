package com.top_logic.ai.chat.api.messages;

/**
 * One answer the user rated.
 */
public class MessageFeedbackDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.MessageFeedbackDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.MessageFeedbackDto create() {
		return new com.top_logic.ai.chat.api.messages.MessageFeedbackDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.MessageFeedbackDto} type in JSON format. */
	public static final String MESSAGE_FEEDBACK_DTO__TYPE = "MessageFeedbackDto";

	/** @see #getTurnIndex() */
	public static final String TURN_INDEX__PROP = "turnIndex";

	/** @see #getRating() */
	public static final String RATING__PROP = "rating";

	/** @see #getNote() */
	public static final String NOTE__PROP = "note";

	/** @see #getRecordedAt() */
	public static final String RECORDED_AT__PROP = "recordedAt";

	private int _turnIndex = 0;

	private String _rating = "";

	private String _note = "";

	private String _recordedAt = "";

	/**
	 * Creates a {@link MessageFeedbackDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.MessageFeedbackDto#create()
	 */
	protected MessageFeedbackDto() {
		super();
	}

	/**
	 * Zero-based index of the turn whose answer was rated.
	 */
	public final int getTurnIndex() {
		return _turnIndex;
	}

	/**
	 * @see #getTurnIndex()
	 */
	public com.top_logic.ai.chat.api.messages.MessageFeedbackDto setTurnIndex(int value) {
		internalSetTurnIndex(value);
		return this;
	}

	/** Internal setter for {@link #getTurnIndex()} without chain call utility. */
	protected final void internalSetTurnIndex(int value) {
		_turnIndex = value;
	}

	/**
	 * "up" or "down".
	 */
	public final String getRating() {
		return _rating;
	}

	/**
	 * @see #getRating()
	 */
	public com.top_logic.ai.chat.api.messages.MessageFeedbackDto setRating(String value) {
		internalSetRating(value);
		return this;
	}

	/** Internal setter for {@link #getRating()} without chain call utility. */
	protected final void internalSetRating(String value) {
		_rating = value;
	}

	/**
	 * Free text the user added; empty when they added none.
	 */
	public final String getNote() {
		return _note;
	}

	/**
	 * @see #getNote()
	 */
	public com.top_logic.ai.chat.api.messages.MessageFeedbackDto setNote(String value) {
		internalSetNote(value);
		return this;
	}

	/** Internal setter for {@link #getNote()} without chain call utility. */
	protected final void internalSetNote(String value) {
		_note = value;
	}

	/**
	 * ISO-8601 timestamp of when the rating was given.
	 */
	public final String getRecordedAt() {
		return _recordedAt;
	}

	/**
	 * @see #getRecordedAt()
	 */
	public com.top_logic.ai.chat.api.messages.MessageFeedbackDto setRecordedAt(String value) {
		internalSetRecordedAt(value);
		return this;
	}

	/** Internal setter for {@link #getRecordedAt()} without chain call utility. */
	protected final void internalSetRecordedAt(String value) {
		_recordedAt = value;
	}

	@Override
	public String jsonType() {
		return MESSAGE_FEEDBACK_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TURN_INDEX__PROP, 
			RATING__PROP, 
			NOTE__PROP, 
			RECORDED_AT__PROP);
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
			case TURN_INDEX__PROP: return getTurnIndex();
			case RATING__PROP: return getRating();
			case NOTE__PROP: return getNote();
			case RECORDED_AT__PROP: return getRecordedAt();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TURN_INDEX__PROP: internalSetTurnIndex((int) value); break;
			case RATING__PROP: internalSetRating((String) value); break;
			case NOTE__PROP: internalSetNote((String) value); break;
			case RECORDED_AT__PROP: internalSetRecordedAt((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.MessageFeedbackDto readMessageFeedbackDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.MessageFeedbackDto result = new com.top_logic.ai.chat.api.messages.MessageFeedbackDto();
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
		out.name(TURN_INDEX__PROP);
		out.value(getTurnIndex());
		out.name(RATING__PROP);
		out.value(getRating());
		out.name(NOTE__PROP);
		out.value(getNote());
		out.name(RECORDED_AT__PROP);
		out.value(getRecordedAt());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TURN_INDEX__PROP: setTurnIndex(in.nextInt()); break;
			case RATING__PROP: setRating(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case NOTE__PROP: setNote(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RECORDED_AT__PROP: setRecordedAt(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
