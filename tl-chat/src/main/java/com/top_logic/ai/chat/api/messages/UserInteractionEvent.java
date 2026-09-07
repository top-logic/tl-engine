package com.top_logic.ai.chat.api.messages;

/**
 * SSE user interaction event payload.
 */
public class UserInteractionEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.UserInteractionEvent} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.UserInteractionEvent create() {
		return new com.top_logic.ai.chat.api.messages.UserInteractionEvent();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.UserInteractionEvent} type in JSON format. */
	public static final String USER_INTERACTION_EVENT__TYPE = "UserInteractionEvent";

	/** @see #getRequestId() */
	public static final String REQUEST_ID__PROP = "requestId";

	/** @see #getQuestion() */
	public static final String QUESTION__PROP = "question";

	/** @see #getQuestionHtml() */
	public static final String QUESTION_HTML__PROP = "questionHtml";

	/** @see #getHeader() */
	public static final String HEADER__PROP = "header";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getOptions() */
	public static final String OPTIONS__PROP = "options";

	/** @see #isMultiSelect() */
	public static final String MULTI_SELECT__PROP = "multiSelect";

	/** @see #getRemainingMs() */
	public static final String REMAINING_MS__PROP = "remainingMs";

	private String _requestId = "";

	private String _question = "";

	private String _questionHtml = "";

	private String _header = "";

	private String _type = "";

	private final java.util.List<com.top_logic.ai.chat.api.messages.InteractionOptionDto> _options = new java.util.ArrayList<>();

	private boolean _multiSelect = false;

	private long _remainingMs = 0L;

	/**
	 * Creates a {@link UserInteractionEvent} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.UserInteractionEvent#create()
	 */
	protected UserInteractionEvent() {
		super();
	}

	/**
	 * Unique interaction request identifier (e.g. "interaction-1").
	 */
	public final String getRequestId() {
		return _requestId;
	}

	/**
	 * @see #getRequestId()
	 */
	public com.top_logic.ai.chat.api.messages.UserInteractionEvent setRequestId(String value) {
		internalSetRequestId(value);
		return this;
	}

	/** Internal setter for {@link #getRequestId()} without chain call utility. */
	protected final void internalSetRequestId(String value) {
		_requestId = value;
	}

	/**
	 * The question to present to the user (raw markdown).
	 */
	public final String getQuestion() {
		return _question;
	}

	/**
	 * @see #getQuestion()
	 */
	public com.top_logic.ai.chat.api.messages.UserInteractionEvent setQuestion(String value) {
		internalSetQuestion(value);
		return this;
	}

	/** Internal setter for {@link #getQuestion()} without chain call utility. */
	protected final void internalSetQuestion(String value) {
		_question = value;
	}

	/**
	 * Pre-rendered HTML for the question.
	 */
	public final String getQuestionHtml() {
		return _questionHtml;
	}

	/**
	 * @see #getQuestionHtml()
	 */
	public com.top_logic.ai.chat.api.messages.UserInteractionEvent setQuestionHtml(String value) {
		internalSetQuestionHtml(value);
		return this;
	}

	/** Internal setter for {@link #getQuestionHtml()} without chain call utility. */
	protected final void internalSetQuestionHtml(String value) {
		_questionHtml = value;
	}

	/**
	 * Short label (e.g. "Approval", "Clarify", "Choose").
	 */
	public final String getHeader() {
		return _header;
	}

	/**
	 * @see #getHeader()
	 */
	public com.top_logic.ai.chat.api.messages.UserInteractionEvent setHeader(String value) {
		internalSetHeader(value);
		return this;
	}

	/** Internal setter for {@link #getHeader()} without chain call utility. */
	protected final void internalSetHeader(String value) {
		_header = value;
	}

	/**
	 * Interaction type: "approval", "text", or "choice".
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.ai.chat.api.messages.UserInteractionEvent setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * Available options for "choice" interactions.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.InteractionOptionDto> getOptions() {
		return _options;
	}

	/**
	 * @see #getOptions()
	 */
	public com.top_logic.ai.chat.api.messages.UserInteractionEvent setOptions(java.util.List<? extends com.top_logic.ai.chat.api.messages.InteractionOptionDto> value) {
		internalSetOptions(value);
		return this;
	}

	/** Internal setter for {@link #getOptions()} without chain call utility. */
	protected final void internalSetOptions(java.util.List<? extends com.top_logic.ai.chat.api.messages.InteractionOptionDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'options' cannot be null.");
		_options.clear();
		_options.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getOptions()} list.
	 */
	public com.top_logic.ai.chat.api.messages.UserInteractionEvent addOption(com.top_logic.ai.chat.api.messages.InteractionOptionDto value) {
		internalAddOption(value);
		return this;
	}

	/** Implementation of {@link #addOption(com.top_logic.ai.chat.api.messages.InteractionOptionDto)} without chain call utility. */
	protected final void internalAddOption(com.top_logic.ai.chat.api.messages.InteractionOptionDto value) {
		_options.add(value);
	}

	/**
	 * Removes a value from the {@link #getOptions()} list.
	 */
	public final void removeOption(com.top_logic.ai.chat.api.messages.InteractionOptionDto value) {
		_options.remove(value);
	}

	/**
	 * Whether multiple selections are allowed (for "choice" type).
	 */
	public final boolean isMultiSelect() {
		return _multiSelect;
	}

	/**
	 * @see #isMultiSelect()
	 */
	public com.top_logic.ai.chat.api.messages.UserInteractionEvent setMultiSelect(boolean value) {
		internalSetMultiSelect(value);
		return this;
	}

	/** Internal setter for {@link #isMultiSelect()} without chain call utility. */
	protected final void internalSetMultiSelect(boolean value) {
		_multiSelect = value;
	}

	/**
	 * Milliseconds left for the user to answer, counted from the moment this event was sent.
	 * Zero when the request has no time left. A re-emitted question (the chat was reopened)
	 * carries what is left of its deadline, not the full one.
	 */
	public final long getRemainingMs() {
		return _remainingMs;
	}

	/**
	 * @see #getRemainingMs()
	 */
	public com.top_logic.ai.chat.api.messages.UserInteractionEvent setRemainingMs(long value) {
		internalSetRemainingMs(value);
		return this;
	}

	/** Internal setter for {@link #getRemainingMs()} without chain call utility. */
	protected final void internalSetRemainingMs(long value) {
		_remainingMs = value;
	}

	@Override
	public String jsonType() {
		return USER_INTERACTION_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			REQUEST_ID__PROP, 
			QUESTION__PROP, 
			QUESTION_HTML__PROP, 
			HEADER__PROP, 
			TYPE__PROP, 
			OPTIONS__PROP, 
			MULTI_SELECT__PROP, 
			REMAINING_MS__PROP);
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
			case REQUEST_ID__PROP: return getRequestId();
			case QUESTION__PROP: return getQuestion();
			case QUESTION_HTML__PROP: return getQuestionHtml();
			case HEADER__PROP: return getHeader();
			case TYPE__PROP: return getType();
			case OPTIONS__PROP: return getOptions();
			case MULTI_SELECT__PROP: return isMultiSelect();
			case REMAINING_MS__PROP: return getRemainingMs();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case REQUEST_ID__PROP: internalSetRequestId((String) value); break;
			case QUESTION__PROP: internalSetQuestion((String) value); break;
			case QUESTION_HTML__PROP: internalSetQuestionHtml((String) value); break;
			case HEADER__PROP: internalSetHeader((String) value); break;
			case TYPE__PROP: internalSetType((String) value); break;
			case OPTIONS__PROP: internalSetOptions(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.InteractionOptionDto.class, value)); break;
			case MULTI_SELECT__PROP: internalSetMultiSelect((boolean) value); break;
			case REMAINING_MS__PROP: internalSetRemainingMs((long) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.UserInteractionEvent readUserInteractionEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.UserInteractionEvent result = new com.top_logic.ai.chat.api.messages.UserInteractionEvent();
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
		out.name(REQUEST_ID__PROP);
		out.value(getRequestId());
		out.name(QUESTION__PROP);
		out.value(getQuestion());
		out.name(QUESTION_HTML__PROP);
		out.value(getQuestionHtml());
		out.name(HEADER__PROP);
		out.value(getHeader());
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(OPTIONS__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.InteractionOptionDto x : getOptions()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(MULTI_SELECT__PROP);
		out.value(isMultiSelect());
		out.name(REMAINING_MS__PROP);
		out.value(getRemainingMs());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case REQUEST_ID__PROP: setRequestId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case QUESTION__PROP: setQuestion(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case QUESTION_HTML__PROP: setQuestionHtml(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HEADER__PROP: setHeader(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case OPTIONS__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.InteractionOptionDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.InteractionOptionDto.readInteractionOptionDto(in));
				}
				in.endArray();
				setOptions(newValue);
			}
			break;
			case MULTI_SELECT__PROP: setMultiSelect(in.nextBoolean()); break;
			case REMAINING_MS__PROP: setRemainingMs(in.nextLong()); break;
			default: super.readField(in, field);
		}
	}

}
