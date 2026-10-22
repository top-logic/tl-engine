package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * The lifecycle state of a tool call.
 */
public class ToolStateDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto} type in JSON format. */
	public static final String TOOL_STATE_DTO__TYPE = "ToolStateDto";

	/** @see #getKind() */
	public static final String KIND__PROP = "kind";

	/** @see #getArguments() */
	public static final String ARGUMENTS__PROP = "arguments";

	/** @see #getStartTime() */
	public static final String START_TIME__PROP = "startTime";

	/** @see #getEndTime() */
	public static final String END_TIME__PROP = "endTime";

	/** @see #getResult() */
	public static final String RESULT__PROP = "result";

	/** @see #isCompacted() */
	public static final String COMPACTED__PROP = "compacted";

	/** @see #getSummary() */
	public static final String SUMMARY__PROP = "summary";

	/** @see #getTruncationHint() */
	public static final String TRUNCATION_HINT__PROP = "truncationHint";

	/** @see #getContent() */
	public static final String CONTENT__PROP = "content";

	/** @see #getError() */
	public static final String ERROR__PROP = "error";

	/** @see #isSynthetic() */
	public static final String SYNTHETIC__PROP = "synthetic";

	private String _kind = "";

	private String _arguments = "";

	private long _startTime = 0L;

	private long _endTime = 0L;

	private String _result = "";

	private boolean _compacted = false;

	private String _summary = "";

	private int _truncationHint = 0;

	private final java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedContent> _content = new java.util.ArrayList<>();

	private String _error = "";

	private boolean _synthetic = false;

	/**
	 * Creates a {@link ToolStateDto} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto#create()
	 */
	protected ToolStateDto() {
		super();
	}

	/**
	 * Discriminator for the state.
	 */
	public final String getKind() {
		return _kind;
	}

	/**
	 * @see #getKind()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto setKind(String value) {
		internalSetKind(value);
		return this;
	}

	/** Internal setter for {@link #getKind()} without chain call utility. */
	protected final void internalSetKind(String value) {
		_kind = value;
	}

	/**
	 * JSON-encoded arguments, for the pending and running states.
	 */
	public final String getArguments() {
		return _arguments;
	}

	/**
	 * @see #getArguments()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto setArguments(String value) {
		internalSetArguments(value);
		return this;
	}

	/** Internal setter for {@link #getArguments()} without chain call utility. */
	protected final void internalSetArguments(String value) {
		_arguments = value;
	}

	/**
	 * When execution started.
	 */
	public final long getStartTime() {
		return _startTime;
	}

	/**
	 * @see #getStartTime()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto setStartTime(long value) {
		internalSetStartTime(value);
		return this;
	}

	/** Internal setter for {@link #getStartTime()} without chain call utility. */
	protected final void internalSetStartTime(long value) {
		_startTime = value;
	}

	/**
	 * When execution ended.
	 */
	public final long getEndTime() {
		return _endTime;
	}

	/**
	 * @see #getEndTime()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto setEndTime(long value) {
		internalSetEndTime(value);
		return this;
	}

	/** Internal setter for {@link #getEndTime()} without chain call utility. */
	protected final void internalSetEndTime(long value) {
		_endTime = value;
	}

	/**
	 * The result text, for the completed state.
	 */
	public final String getResult() {
		return _result;
	}

	/**
	 * @see #getResult()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto setResult(String value) {
		internalSetResult(value);
		return this;
	}

	/** Internal setter for {@link #getResult()} without chain call utility. */
	protected final void internalSetResult(String value) {
		_result = value;
	}

	/**
	 * Whether the result was shortened, for the completed state.
	 */
	public final boolean isCompacted() {
		return _compacted;
	}

	/**
	 * @see #isCompacted()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto setCompacted(boolean value) {
		internalSetCompacted(value);
		return this;
	}

	/** Internal setter for {@link #isCompacted()} without chain call utility. */
	protected final void internalSetCompacted(boolean value) {
		_compacted = value;
	}

	/**
	 * Summary of a shortened result, for the completed state.
	 */
	public final String getSummary() {
		return _summary;
	}

	/**
	 * @see #getSummary()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto setSummary(String value) {
		internalSetSummary(value);
		return this;
	}

	/** Internal setter for {@link #getSummary()} without chain call utility. */
	protected final void internalSetSummary(String value) {
		_summary = value;
	}

	/**
	 * How much of the result was dropped, for the completed state.
	 */
	public final int getTruncationHint() {
		return _truncationHint;
	}

	/**
	 * @see #getTruncationHint()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto setTruncationHint(int value) {
		internalSetTruncationHint(value);
		return this;
	}

	/** Internal setter for {@link #getTruncationHint()} without chain call utility. */
	protected final void internalSetTruncationHint(int value) {
		_truncationHint = value;
	}

	/**
	 * Structured result content, for the completed state.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedContent> getContent() {
		return _content;
	}

	/**
	 * @see #getContent()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto setContent(java.util.List<? extends com.top_logic.tl.ai.core.session.persist.messages.PersistedContent> value) {
		internalSetContent(value);
		return this;
	}

	/** Internal setter for {@link #getContent()} without chain call utility. */
	protected final void internalSetContent(java.util.List<? extends com.top_logic.tl.ai.core.session.persist.messages.PersistedContent> value) {
		if (value == null) throw new IllegalArgumentException("Property 'content' cannot be null.");
		_content.clear();
		_content.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getContent()} list.
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto addContent(com.top_logic.tl.ai.core.session.persist.messages.PersistedContent value) {
		internalAddContent(value);
		return this;
	}

	/** Implementation of {@link #addContent(com.top_logic.tl.ai.core.session.persist.messages.PersistedContent)} without chain call utility. */
	protected final void internalAddContent(com.top_logic.tl.ai.core.session.persist.messages.PersistedContent value) {
		_content.add(value);
	}

	/**
	 * Removes a value from the {@link #getContent()} list.
	 */
	public final void removeContent(com.top_logic.tl.ai.core.session.persist.messages.PersistedContent value) {
		_content.remove(value);
	}

	/**
	 * The error text, for the failed state.
	 */
	public final String getError() {
		return _error;
	}

	/**
	 * @see #getError()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto setError(String value) {
		internalSetError(value);
		return this;
	}

	/** Internal setter for {@link #getError()} without chain call utility. */
	protected final void internalSetError(String value) {
		_error = value;
	}

	/**
	 * Whether the failure was synthesized rather than reported by the tool.
	 */
	public final boolean isSynthetic() {
		return _synthetic;
	}

	/**
	 * @see #isSynthetic()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto setSynthetic(boolean value) {
		internalSetSynthetic(value);
		return this;
	}

	/** Internal setter for {@link #isSynthetic()} without chain call utility. */
	protected final void internalSetSynthetic(boolean value) {
		_synthetic = value;
	}

	@Override
	public String jsonType() {
		return TOOL_STATE_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			KIND__PROP, 
			ARGUMENTS__PROP, 
			START_TIME__PROP, 
			END_TIME__PROP, 
			RESULT__PROP, 
			COMPACTED__PROP, 
			SUMMARY__PROP, 
			TRUNCATION_HINT__PROP, 
			CONTENT__PROP, 
			ERROR__PROP, 
			SYNTHETIC__PROP);
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
			case KIND__PROP: return getKind();
			case ARGUMENTS__PROP: return getArguments();
			case START_TIME__PROP: return getStartTime();
			case END_TIME__PROP: return getEndTime();
			case RESULT__PROP: return getResult();
			case COMPACTED__PROP: return isCompacted();
			case SUMMARY__PROP: return getSummary();
			case TRUNCATION_HINT__PROP: return getTruncationHint();
			case CONTENT__PROP: return getContent();
			case ERROR__PROP: return getError();
			case SYNTHETIC__PROP: return isSynthetic();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case KIND__PROP: internalSetKind((String) value); break;
			case ARGUMENTS__PROP: internalSetArguments((String) value); break;
			case START_TIME__PROP: internalSetStartTime((long) value); break;
			case END_TIME__PROP: internalSetEndTime((long) value); break;
			case RESULT__PROP: internalSetResult((String) value); break;
			case COMPACTED__PROP: internalSetCompacted((boolean) value); break;
			case SUMMARY__PROP: internalSetSummary((String) value); break;
			case TRUNCATION_HINT__PROP: internalSetTruncationHint((int) value); break;
			case CONTENT__PROP: internalSetContent(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.session.persist.messages.PersistedContent.class, value)); break;
			case ERROR__PROP: internalSetError((String) value); break;
			case SYNTHETIC__PROP: internalSetSynthetic((boolean) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto readToolStateDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto result = new com.top_logic.tl.ai.core.session.persist.messages.ToolStateDto();
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
		out.name(KIND__PROP);
		out.value(getKind());
		out.name(ARGUMENTS__PROP);
		out.value(getArguments());
		out.name(START_TIME__PROP);
		out.value(getStartTime());
		out.name(END_TIME__PROP);
		out.value(getEndTime());
		out.name(RESULT__PROP);
		out.value(getResult());
		out.name(COMPACTED__PROP);
		out.value(isCompacted());
		out.name(SUMMARY__PROP);
		out.value(getSummary());
		out.name(TRUNCATION_HINT__PROP);
		out.value(getTruncationHint());
		out.name(CONTENT__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.session.persist.messages.PersistedContent x : getContent()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(ERROR__PROP);
		out.value(getError());
		out.name(SYNTHETIC__PROP);
		out.value(isSynthetic());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case KIND__PROP: setKind(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ARGUMENTS__PROP: setArguments(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case START_TIME__PROP: setStartTime(in.nextLong()); break;
			case END_TIME__PROP: setEndTime(in.nextLong()); break;
			case RESULT__PROP: setResult(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case COMPACTED__PROP: setCompacted(in.nextBoolean()); break;
			case SUMMARY__PROP: setSummary(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TRUNCATION_HINT__PROP: setTruncationHint(in.nextInt()); break;
			case CONTENT__PROP: {
				java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedContent> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.session.persist.messages.PersistedContent.readPersistedContent(in));
				}
				in.endArray();
				setContent(newValue);
			}
			break;
			case ERROR__PROP: setError(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SYNTHETIC__PROP: setSynthetic(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
