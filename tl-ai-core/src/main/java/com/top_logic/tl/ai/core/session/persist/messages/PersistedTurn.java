package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * A single turn in a conversation, representing one user-assistant exchange.
 */
public class PersistedTurn extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn} type in JSON format. */
	public static final String PERSISTED_TURN__TYPE = "PersistedTurn";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getTurnNumber() */
	public static final String TURN_NUMBER__PROP = "turnNumber";

	/** @see #getTimestamp() */
	public static final String TIMESTAMP__PROP = "timestamp";

	/** @see #getModel() */
	public static final String MODEL__PROP = "model";

	/** @see #getProvider() */
	public static final String PROVIDER__PROP = "provider";

	/** @see #getUsage() */
	public static final String USAGE__PROP = "usage";

	/** @see #getLastIterationUsage() */
	public static final String LAST_ITERATION_USAGE__PROP = "lastIterationUsage";

	/** @see #getContextComposition() */
	public static final String CONTEXT_COMPOSITION__PROP = "contextComposition";

	/** @see #getTurnContext() */
	public static final String TURN_CONTEXT__PROP = "turnContext";

	/** @see #getSources() */
	public static final String SOURCES__PROP = "sources";

	/** @see #getUserParts() */
	public static final String USER_PARTS__PROP = "userParts";

	/** @see #getAssistantParts() */
	public static final String ASSISTANT_PARTS__PROP = "assistantParts";

	/** @see #getContentHtml() */
	public static final String CONTENT_HTML__PROP = "contentHtml";

	private String _id = "";

	private int _turnNumber = 0;

	private long _timestamp = 0L;

	private String _model = "";

	private String _provider = "";

	private com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto _usage = null;

	private com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto _lastIterationUsage = null;

	private com.top_logic.tl.ai.core.session.persist.messages.ContextCompositionDto _contextComposition = null;

	private String _turnContext = "";

	private final java.util.List<com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto> _sources = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedPart> _userParts = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedPart> _assistantParts = new java.util.ArrayList<>();

	private String _contentHtml = "";

	/**
	 * Creates a {@link PersistedTurn} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn#create()
	 */
	protected PersistedTurn() {
		super();
	}

	/**
	 * Unique turn identifier.
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * Sequential turn number within the conversation.
	 */
	public final int getTurnNumber() {
		return _turnNumber;
	}

	/**
	 * @see #getTurnNumber()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn setTurnNumber(int value) {
		internalSetTurnNumber(value);
		return this;
	}

	/** Internal setter for {@link #getTurnNumber()} without chain call utility. */
	protected final void internalSetTurnNumber(int value) {
		_turnNumber = value;
	}

	/**
	 * Timestamp when this turn started.
	 */
	public final long getTimestamp() {
		return _timestamp;
	}

	/**
	 * @see #getTimestamp()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn setTimestamp(long value) {
		internalSetTimestamp(value);
		return this;
	}

	/** Internal setter for {@link #getTimestamp()} without chain call utility. */
	protected final void internalSetTimestamp(long value) {
		_timestamp = value;
	}

	/**
	 * The model used for this turn.
	 */
	public final String getModel() {
		return _model;
	}

	/**
	 * @see #getModel()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn setModel(String value) {
		internalSetModel(value);
		return this;
	}

	/** Internal setter for {@link #getModel()} without chain call utility. */
	protected final void internalSetModel(String value) {
		_model = value;
	}

	/**
	 * The provider used for this turn.
	 */
	public final String getProvider() {
		return _provider;
	}

	/**
	 * @see #getProvider()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn setProvider(String value) {
		internalSetProvider(value);
		return this;
	}

	/** Internal setter for {@link #getProvider()} without chain call utility. */
	protected final void internalSetProvider(String value) {
		_provider = value;
	}

	/**
	 * Token usage for this turn, summed over every agent-loop iteration it took.
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto getUsage() {
		return _usage;
	}

	/**
	 * @see #getUsage()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn setUsage(com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto value) {
		internalSetUsage(value);
		return this;
	}

	/** Internal setter for {@link #getUsage()} without chain call utility. */
	protected final void internalSetUsage(com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto value) {
		_usage = value;
	}

	/**
	 * Checks, whether {@link #getUsage()} has a value.
	 */
	public final boolean hasUsage() {
		return _usage != null;
	}

	/**
	 * Token usage of the LAST agent-loop iteration of this turn.
	 *
	 * <p>
	 * Its prompt size (input + cache read + cache write) is the exact context the model saw at the
	 * end of the turn, which is what a context-usage display must show. {@link #getUsage()} cannot
	 * serve that purpose: every iteration resends the whole conversation, so its summed prompt size
	 * counts the conversation once per iteration. Absent in transcripts written before this field
	 * existed.
	 * </p>
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto getLastIterationUsage() {
		return _lastIterationUsage;
	}

	/**
	 * @see #getLastIterationUsage()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn setLastIterationUsage(com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto value) {
		internalSetLastIterationUsage(value);
		return this;
	}

	/** Internal setter for {@link #getLastIterationUsage()} without chain call utility. */
	protected final void internalSetLastIterationUsage(com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto value) {
		_lastIterationUsage = value;
	}

	/**
	 * Checks, whether {@link #getLastIterationUsage()} has a value.
	 */
	public final boolean hasLastIterationUsage() {
		return _lastIterationUsage != null;
	}

	/**
	 * What the prompt of this turn's last request was made of.
	 *
	 * <p>
	 * Estimates from the agent loop's token estimator, recorded so a re-opened chat can show the
	 * same breakdown a live one does. Absent in transcripts written before this field existed.
	 * </p>
	 */
	public final com.top_logic.tl.ai.core.session.persist.messages.ContextCompositionDto getContextComposition() {
		return _contextComposition;
	}

	/**
	 * @see #getContextComposition()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn setContextComposition(com.top_logic.tl.ai.core.session.persist.messages.ContextCompositionDto value) {
		internalSetContextComposition(value);
		return this;
	}

	/** Internal setter for {@link #getContextComposition()} without chain call utility. */
	protected final void internalSetContextComposition(com.top_logic.tl.ai.core.session.persist.messages.ContextCompositionDto value) {
		_contextComposition = value;
	}

	/**
	 * Checks, whether {@link #getContextComposition()} has a value.
	 */
	public final boolean hasContextComposition() {
		return _contextComposition != null;
	}

	/**
	 * What the host stated about this turn - the facts that change while the conversation runs.
	 *
	 * <p>
	 * Sent to the model beside the user's text and stored apart from it, so a reader of
	 * {@link #getUserParts()} gets what the user wrote and nothing else. Empty when the host stated
	 * no per-turn fact, and in transcripts written before this field existed.
	 * </p>
	 */
	public final String getTurnContext() {
		return _turnContext;
	}

	/**
	 * @see #getTurnContext()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn setTurnContext(String value) {
		internalSetTurnContext(value);
		return this;
	}

	/** Internal setter for {@link #getTurnContext()} without chain call utility. */
	protected final void internalSetTurnContext(String value) {
		_turnContext = value;
	}

	/**
	 * What the run of this turn read - the sources its answer could point at, cited or not.
	 *
	 * <p>
	 * Empty in transcripts written before this field existed.
	 * </p>
	 */
	public final java.util.List<com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto> getSources() {
		return _sources;
	}

	/**
	 * @see #getSources()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn setSources(java.util.List<? extends com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto> value) {
		internalSetSources(value);
		return this;
	}

	/** Internal setter for {@link #getSources()} without chain call utility. */
	protected final void internalSetSources(java.util.List<? extends com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto> value) {
		if (value == null) throw new IllegalArgumentException("Property 'sources' cannot be null.");
		_sources.clear();
		_sources.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getSources()} list.
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn addSource(com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto value) {
		internalAddSource(value);
		return this;
	}

	/** Implementation of {@link #addSource(com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto)} without chain call utility. */
	protected final void internalAddSource(com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto value) {
		_sources.add(value);
	}

	/**
	 * Removes a value from the {@link #getSources()} list.
	 */
	public final void removeSource(com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto value) {
		_sources.remove(value);
	}

	/**
	 * Parts contributed by the user in this turn.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedPart> getUserParts() {
		return _userParts;
	}

	/**
	 * @see #getUserParts()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn setUserParts(java.util.List<? extends com.top_logic.tl.ai.core.session.persist.messages.PersistedPart> value) {
		internalSetUserParts(value);
		return this;
	}

	/** Internal setter for {@link #getUserParts()} without chain call utility. */
	protected final void internalSetUserParts(java.util.List<? extends com.top_logic.tl.ai.core.session.persist.messages.PersistedPart> value) {
		if (value == null) throw new IllegalArgumentException("Property 'userParts' cannot be null.");
		_userParts.clear();
		_userParts.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getUserParts()} list.
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn addUserPart(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart value) {
		internalAddUserPart(value);
		return this;
	}

	/** Implementation of {@link #addUserPart(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart)} without chain call utility. */
	protected final void internalAddUserPart(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart value) {
		_userParts.add(value);
	}

	/**
	 * Removes a value from the {@link #getUserParts()} list.
	 */
	public final void removeUserPart(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart value) {
		_userParts.remove(value);
	}

	/**
	 * Parts contributed by the assistant in this turn.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedPart> getAssistantParts() {
		return _assistantParts;
	}

	/**
	 * @see #getAssistantParts()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn setAssistantParts(java.util.List<? extends com.top_logic.tl.ai.core.session.persist.messages.PersistedPart> value) {
		internalSetAssistantParts(value);
		return this;
	}

	/** Internal setter for {@link #getAssistantParts()} without chain call utility. */
	protected final void internalSetAssistantParts(java.util.List<? extends com.top_logic.tl.ai.core.session.persist.messages.PersistedPart> value) {
		if (value == null) throw new IllegalArgumentException("Property 'assistantParts' cannot be null.");
		_assistantParts.clear();
		_assistantParts.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getAssistantParts()} list.
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn addAssistantPart(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart value) {
		internalAddAssistantPart(value);
		return this;
	}

	/** Implementation of {@link #addAssistantPart(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart)} without chain call utility. */
	protected final void internalAddAssistantPart(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart value) {
		_assistantParts.add(value);
	}

	/**
	 * Removes a value from the {@link #getAssistantParts()} list.
	 */
	public final void removeAssistantPart(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart value) {
		_assistantParts.remove(value);
	}

	/**
	 * Server-rendered HTML of the assistant's text content. Computed at load time, not persisted.
	 */
	public final String getContentHtml() {
		return _contentHtml;
	}

	/**
	 * @see #getContentHtml()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn setContentHtml(String value) {
		internalSetContentHtml(value);
		return this;
	}

	/** Internal setter for {@link #getContentHtml()} without chain call utility. */
	protected final void internalSetContentHtml(String value) {
		_contentHtml = value;
	}

	@Override
	public String jsonType() {
		return PERSISTED_TURN__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			TURN_NUMBER__PROP, 
			TIMESTAMP__PROP, 
			MODEL__PROP, 
			PROVIDER__PROP, 
			USAGE__PROP, 
			LAST_ITERATION_USAGE__PROP, 
			CONTEXT_COMPOSITION__PROP, 
			TURN_CONTEXT__PROP, 
			SOURCES__PROP, 
			USER_PARTS__PROP, 
			ASSISTANT_PARTS__PROP, 
			CONTENT_HTML__PROP);
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
			case TURN_NUMBER__PROP: return getTurnNumber();
			case TIMESTAMP__PROP: return getTimestamp();
			case MODEL__PROP: return getModel();
			case PROVIDER__PROP: return getProvider();
			case USAGE__PROP: return getUsage();
			case LAST_ITERATION_USAGE__PROP: return getLastIterationUsage();
			case CONTEXT_COMPOSITION__PROP: return getContextComposition();
			case TURN_CONTEXT__PROP: return getTurnContext();
			case SOURCES__PROP: return getSources();
			case USER_PARTS__PROP: return getUserParts();
			case ASSISTANT_PARTS__PROP: return getAssistantParts();
			case CONTENT_HTML__PROP: return getContentHtml();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case TURN_NUMBER__PROP: internalSetTurnNumber((int) value); break;
			case TIMESTAMP__PROP: internalSetTimestamp((long) value); break;
			case MODEL__PROP: internalSetModel((String) value); break;
			case PROVIDER__PROP: internalSetProvider((String) value); break;
			case USAGE__PROP: internalSetUsage((com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto) value); break;
			case LAST_ITERATION_USAGE__PROP: internalSetLastIterationUsage((com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto) value); break;
			case CONTEXT_COMPOSITION__PROP: internalSetContextComposition((com.top_logic.tl.ai.core.session.persist.messages.ContextCompositionDto) value); break;
			case TURN_CONTEXT__PROP: internalSetTurnContext((String) value); break;
			case SOURCES__PROP: internalSetSources(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto.class, value)); break;
			case USER_PARTS__PROP: internalSetUserParts(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart.class, value)); break;
			case ASSISTANT_PARTS__PROP: internalSetAssistantParts(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart.class, value)); break;
			case CONTENT_HTML__PROP: internalSetContentHtml((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn readPersistedTurn(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn result = new com.top_logic.tl.ai.core.session.persist.messages.PersistedTurn();
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
		out.name(TURN_NUMBER__PROP);
		out.value(getTurnNumber());
		out.name(TIMESTAMP__PROP);
		out.value(getTimestamp());
		out.name(MODEL__PROP);
		out.value(getModel());
		out.name(PROVIDER__PROP);
		out.value(getProvider());
		if (hasUsage()) {
			out.name(USAGE__PROP);
			getUsage().writeTo(out);
		}
		if (hasLastIterationUsage()) {
			out.name(LAST_ITERATION_USAGE__PROP);
			getLastIterationUsage().writeTo(out);
		}
		if (hasContextComposition()) {
			out.name(CONTEXT_COMPOSITION__PROP);
			getContextComposition().writeTo(out);
		}
		out.name(TURN_CONTEXT__PROP);
		out.value(getTurnContext());
		out.name(SOURCES__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto x : getSources()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(USER_PARTS__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.session.persist.messages.PersistedPart x : getUserParts()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(ASSISTANT_PARTS__PROP);
		out.beginArray();
		for (com.top_logic.tl.ai.core.session.persist.messages.PersistedPart x : getAssistantParts()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(CONTENT_HTML__PROP);
		out.value(getContentHtml());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TURN_NUMBER__PROP: setTurnNumber(in.nextInt()); break;
			case TIMESTAMP__PROP: setTimestamp(in.nextLong()); break;
			case MODEL__PROP: setModel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PROVIDER__PROP: setProvider(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case USAGE__PROP: setUsage(com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto.readTokenUsageDto(in)); break;
			case LAST_ITERATION_USAGE__PROP: setLastIterationUsage(com.top_logic.tl.ai.core.session.persist.messages.TokenUsageDto.readTokenUsageDto(in)); break;
			case CONTEXT_COMPOSITION__PROP: setContextComposition(com.top_logic.tl.ai.core.session.persist.messages.ContextCompositionDto.readContextCompositionDto(in)); break;
			case TURN_CONTEXT__PROP: setTurnContext(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case SOURCES__PROP: {
				java.util.List<com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.session.persist.messages.ReadSourceDto.readReadSourceDto(in));
				}
				in.endArray();
				setSources(newValue);
			}
			break;
			case USER_PARTS__PROP: {
				java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedPart> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart.readPersistedPart(in));
				}
				in.endArray();
				setUserParts(newValue);
			}
			break;
			case ASSISTANT_PARTS__PROP: {
				java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedPart> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.tl.ai.core.session.persist.messages.PersistedPart.readPersistedPart(in));
				}
				in.endArray();
				setAssistantParts(newValue);
			}
			break;
			case CONTENT_HTML__PROP: setContentHtml(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
