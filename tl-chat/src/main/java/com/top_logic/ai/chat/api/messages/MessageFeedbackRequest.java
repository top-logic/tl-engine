package com.top_logic.ai.chat.api.messages;

/**
 * Request body for POST /chat/feedback.
 */
public class MessageFeedbackRequest extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.MessageFeedbackRequest} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.MessageFeedbackRequest create() {
		return new com.top_logic.ai.chat.api.messages.MessageFeedbackRequest();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.MessageFeedbackRequest} type in JSON format. */
	public static final String MESSAGE_FEEDBACK_REQUEST__TYPE = "MessageFeedbackRequest";

	/** @see #getSessionId() */
	public static final String SESSION_ID__PROP = "sessionId";

	/** @see #getTurnIndex() */
	public static final String TURN_INDEX__PROP = "turnIndex";

	/** @see #getRating() */
	public static final String RATING__PROP = "rating";

	/** @see #getNote() */
	public static final String NOTE__PROP = "note";

	private String _sessionId = "";

	private int _turnIndex = 0;

	private String _rating = "";

	private String _note = "";

	/**
	 * Creates a {@link MessageFeedbackRequest} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.MessageFeedbackRequest#create()
	 */
	protected MessageFeedbackRequest() {
		super();
	}

	/**
	 * Session the rated answer belongs to.
	 */
	public final String getSessionId() {
		return _sessionId;
	}

	/**
	 * @see #getSessionId()
	 */
	public com.top_logic.ai.chat.api.messages.MessageFeedbackRequest setSessionId(String value) {
		internalSetSessionId(value);
		return this;
	}

	/** Internal setter for {@link #getSessionId()} without chain call utility. */
	protected final void internalSetSessionId(String value) {
		_sessionId = value;
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
	public com.top_logic.ai.chat.api.messages.MessageFeedbackRequest setTurnIndex(int value) {
		internalSetTurnIndex(value);
		return this;
	}

	/** Internal setter for {@link #getTurnIndex()} without chain call utility. */
	protected final void internalSetTurnIndex(int value) {
		_turnIndex = value;
	}

	/**
	 * "up", "down", or empty to withdraw an earlier rating.
	 */
	public final String getRating() {
		return _rating;
	}

	/**
	 * @see #getRating()
	 */
	public com.top_logic.ai.chat.api.messages.MessageFeedbackRequest setRating(String value) {
		internalSetRating(value);
		return this;
	}

	/** Internal setter for {@link #getRating()} without chain call utility. */
	protected final void internalSetRating(String value) {
		_rating = value;
	}

	/**
	 * Free text the user added; optional.
	 */
	public final String getNote() {
		return _note;
	}

	/**
	 * @see #getNote()
	 */
	public com.top_logic.ai.chat.api.messages.MessageFeedbackRequest setNote(String value) {
		internalSetNote(value);
		return this;
	}

	/** Internal setter for {@link #getNote()} without chain call utility. */
	protected final void internalSetNote(String value) {
		_note = value;
	}

	@Override
	public String jsonType() {
		return MESSAGE_FEEDBACK_REQUEST__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			SESSION_ID__PROP, 
			TURN_INDEX__PROP, 
			RATING__PROP, 
			NOTE__PROP);
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
			case SESSION_ID__PROP: return getSessionId();
			case TURN_INDEX__PROP: return getTurnIndex();
			case RATING__PROP: return getRating();
			case NOTE__PROP: return getNote();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case SESSION_ID__PROP: internalSetSessionId((String) value); break;
			case TURN_INDEX__PROP: internalSetTurnIndex((int) value); break;
			case RATING__PROP: internalSetRating((String) value); break;
			case NOTE__PROP: internalSetNote((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.MessageFeedbackRequest readMessageFeedbackRequest(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.MessageFeedbackRequest result = new com.top_logic.ai.chat.api.messages.MessageFeedbackRequest();
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
		out.name(SESSION_ID__PROP);
		out.value(getSessionId());
		out.name(TURN_INDEX__PROP);
		out.value(getTurnIndex());
		out.name(RATING__PROP);
		out.value(getRating());
		out.name(NOTE__PROP);
		out.value(getNote());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case SESSION_ID__PROP: setSessionId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TURN_INDEX__PROP: setTurnIndex(in.nextInt()); break;
			case RATING__PROP: setRating(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case NOTE__PROP: setNote(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
