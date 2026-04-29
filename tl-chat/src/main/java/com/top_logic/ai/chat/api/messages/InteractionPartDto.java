package com.top_logic.ai.chat.api.messages;

/**
 * User interaction part for questions posed to the user.
 */
public class InteractionPartDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.InteractionPartDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.InteractionPartDto create() {
		return new com.top_logic.ai.chat.api.messages.InteractionPartDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.InteractionPartDto} type in JSON format. */
	public static final String INTERACTION_PART_DTO__TYPE = "InteractionPartDto";

	/** @see #getInteractionType() */
	public static final String INTERACTION_TYPE__PROP = "interactionType";

	/** @see #getQuestion() */
	public static final String QUESTION__PROP = "question";

	/** @see #getHeader() */
	public static final String HEADER__PROP = "header";

	/** @see #getOptions() */
	public static final String OPTIONS__PROP = "options";

	/** @see #isMultiSelect() */
	public static final String MULTI_SELECT__PROP = "multiSelect";

	/** @see #getUserResponse() */
	public static final String USER_RESPONSE__PROP = "userResponse";

	/** @see #getResolvedAt() */
	public static final String RESOLVED_AT__PROP = "resolvedAt";

	private String _interactionType = "";

	private String _question = "";

	private String _header = "";

	private final java.util.List<String> _options = new java.util.ArrayList<>();

	private boolean _multiSelect = false;

	private String _userResponse = "";

	private long _resolvedAt = 0L;

	/**
	 * Creates a {@link InteractionPartDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.InteractionPartDto#create()
	 */
	protected InteractionPartDto() {
		super();
	}

	/**
	 * Type of interaction.
	 */
	public final String getInteractionType() {
		return _interactionType;
	}

	/**
	 * @see #getInteractionType()
	 */
	public com.top_logic.ai.chat.api.messages.InteractionPartDto setInteractionType(String value) {
		internalSetInteractionType(value);
		return this;
	}

	/** Internal setter for {@link #getInteractionType()} without chain call utility. */
	protected final void internalSetInteractionType(String value) {
		_interactionType = value;
	}

	/**
	 * The question posed to the user.
	 */
	public final String getQuestion() {
		return _question;
	}

	/**
	 * @see #getQuestion()
	 */
	public com.top_logic.ai.chat.api.messages.InteractionPartDto setQuestion(String value) {
		internalSetQuestion(value);
		return this;
	}

	/** Internal setter for {@link #getQuestion()} without chain call utility. */
	protected final void internalSetQuestion(String value) {
		_question = value;
	}

	/**
	 * Optional header text.
	 */
	public final String getHeader() {
		return _header;
	}

	/**
	 * @see #getHeader()
	 */
	public com.top_logic.ai.chat.api.messages.InteractionPartDto setHeader(String value) {
		internalSetHeader(value);
		return this;
	}

	/** Internal setter for {@link #getHeader()} without chain call utility. */
	protected final void internalSetHeader(String value) {
		_header = value;
	}

	/**
	 * Available options for the user to choose from.
	 */
	public final java.util.List<String> getOptions() {
		return _options;
	}

	/**
	 * @see #getOptions()
	 */
	public com.top_logic.ai.chat.api.messages.InteractionPartDto setOptions(java.util.List<? extends String> value) {
		internalSetOptions(value);
		return this;
	}

	/** Internal setter for {@link #getOptions()} without chain call utility. */
	protected final void internalSetOptions(java.util.List<? extends String> value) {
		_options.clear();
		_options.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getOptions()} list.
	 */
	public com.top_logic.ai.chat.api.messages.InteractionPartDto addOption(String value) {
		internalAddOption(value);
		return this;
	}

	/** Implementation of {@link #addOption(String)} without chain call utility. */
	protected final void internalAddOption(String value) {
		_options.add(value);
	}

	/**
	 * Removes a value from the {@link #getOptions()} list.
	 */
	public final void removeOption(String value) {
		_options.remove(value);
	}

	/**
	 * Whether multiple options can be selected.
	 */
	public final boolean isMultiSelect() {
		return _multiSelect;
	}

	/**
	 * @see #isMultiSelect()
	 */
	public com.top_logic.ai.chat.api.messages.InteractionPartDto setMultiSelect(boolean value) {
		internalSetMultiSelect(value);
		return this;
	}

	/** Internal setter for {@link #isMultiSelect()} without chain call utility. */
	protected final void internalSetMultiSelect(boolean value) {
		_multiSelect = value;
	}

	/**
	 * The user's response.
	 */
	public final String getUserResponse() {
		return _userResponse;
	}

	/**
	 * @see #getUserResponse()
	 */
	public com.top_logic.ai.chat.api.messages.InteractionPartDto setUserResponse(String value) {
		internalSetUserResponse(value);
		return this;
	}

	/** Internal setter for {@link #getUserResponse()} without chain call utility. */
	protected final void internalSetUserResponse(String value) {
		_userResponse = value;
	}

	/**
	 * Timestamp when the interaction was resolved.
	 */
	public final long getResolvedAt() {
		return _resolvedAt;
	}

	/**
	 * @see #getResolvedAt()
	 */
	public com.top_logic.ai.chat.api.messages.InteractionPartDto setResolvedAt(long value) {
		internalSetResolvedAt(value);
		return this;
	}

	/** Internal setter for {@link #getResolvedAt()} without chain call utility. */
	protected final void internalSetResolvedAt(long value) {
		_resolvedAt = value;
	}

	@Override
	public String jsonType() {
		return INTERACTION_PART_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			INTERACTION_TYPE__PROP, 
			QUESTION__PROP, 
			HEADER__PROP, 
			OPTIONS__PROP, 
			MULTI_SELECT__PROP, 
			USER_RESPONSE__PROP, 
			RESOLVED_AT__PROP);
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
			case INTERACTION_TYPE__PROP: return getInteractionType();
			case QUESTION__PROP: return getQuestion();
			case HEADER__PROP: return getHeader();
			case OPTIONS__PROP: return getOptions();
			case MULTI_SELECT__PROP: return isMultiSelect();
			case USER_RESPONSE__PROP: return getUserResponse();
			case RESOLVED_AT__PROP: return getResolvedAt();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case INTERACTION_TYPE__PROP: internalSetInteractionType((String) value); break;
			case QUESTION__PROP: internalSetQuestion((String) value); break;
			case HEADER__PROP: internalSetHeader((String) value); break;
			case OPTIONS__PROP: internalSetOptions(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case MULTI_SELECT__PROP: internalSetMultiSelect((boolean) value); break;
			case USER_RESPONSE__PROP: internalSetUserResponse((String) value); break;
			case RESOLVED_AT__PROP: internalSetResolvedAt((long) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.InteractionPartDto readInteractionPartDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.InteractionPartDto result = new com.top_logic.ai.chat.api.messages.InteractionPartDto();
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
		out.name(INTERACTION_TYPE__PROP);
		out.value(getInteractionType());
		out.name(QUESTION__PROP);
		out.value(getQuestion());
		out.name(HEADER__PROP);
		out.value(getHeader());
		out.name(OPTIONS__PROP);
		out.beginArray();
		for (String x : getOptions()) {
			out.value(x);
		}
		out.endArray();
		out.name(MULTI_SELECT__PROP);
		out.value(isMultiSelect());
		out.name(USER_RESPONSE__PROP);
		out.value(getUserResponse());
		out.name(RESOLVED_AT__PROP);
		out.value(getResolvedAt());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case INTERACTION_TYPE__PROP: setInteractionType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case QUESTION__PROP: setQuestion(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case HEADER__PROP: setHeader(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case OPTIONS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setOptions(newValue);
			}
			break;
			case MULTI_SELECT__PROP: setMultiSelect(in.nextBoolean()); break;
			case USER_RESPONSE__PROP: setUserResponse(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case RESOLVED_AT__PROP: setResolvedAt(in.nextLong()); break;
			default: super.readField(in, field);
		}
	}

}
