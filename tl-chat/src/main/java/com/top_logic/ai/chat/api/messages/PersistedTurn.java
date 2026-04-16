package com.top_logic.ai.chat.api.messages;

/**
 * A single turn in a conversation, representing one user-assistant exchange.
 */
public class PersistedTurn extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.PersistedTurn} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.PersistedTurn create() {
		return new com.top_logic.ai.chat.api.messages.PersistedTurn();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.PersistedTurn} type in JSON format. */
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

	/** @see #getUserParts() */
	public static final String USER_PARTS__PROP = "userParts";

	/** @see #getAssistantParts() */
	public static final String ASSISTANT_PARTS__PROP = "assistantParts";

	private String _id = "";

	private int _turnNumber = 0;

	private long _timestamp = 0L;

	private String _model = "";

	private String _provider = "";

	private com.top_logic.ai.chat.api.messages.TokenUsageDto _usage = null;

	private final java.util.List<com.top_logic.ai.chat.api.messages.PersistedPart> _userParts = new java.util.ArrayList<>();

	private final java.util.List<com.top_logic.ai.chat.api.messages.PersistedPart> _assistantParts = new java.util.ArrayList<>();

	/**
	 * Creates a {@link PersistedTurn} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.PersistedTurn#create()
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
	public com.top_logic.ai.chat.api.messages.PersistedTurn setId(String value) {
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
	public com.top_logic.ai.chat.api.messages.PersistedTurn setTurnNumber(int value) {
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
	public com.top_logic.ai.chat.api.messages.PersistedTurn setTimestamp(long value) {
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
	public com.top_logic.ai.chat.api.messages.PersistedTurn setModel(String value) {
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
	public com.top_logic.ai.chat.api.messages.PersistedTurn setProvider(String value) {
		internalSetProvider(value);
		return this;
	}

	/** Internal setter for {@link #getProvider()} without chain call utility. */
	protected final void internalSetProvider(String value) {
		_provider = value;
	}

	/**
	 * Token usage for this turn.
	 */
	public final com.top_logic.ai.chat.api.messages.TokenUsageDto getUsage() {
		return _usage;
	}

	/**
	 * @see #getUsage()
	 */
	public com.top_logic.ai.chat.api.messages.PersistedTurn setUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto value) {
		internalSetUsage(value);
		return this;
	}

	/** Internal setter for {@link #getUsage()} without chain call utility. */
	protected final void internalSetUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto value) {
		_usage = value;
	}

	/**
	 * Checks, whether {@link #getUsage()} has a value.
	 */
	public final boolean hasUsage() {
		return _usage != null;
	}

	/**
	 * Parts contributed by the user in this turn.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.PersistedPart> getUserParts() {
		return _userParts;
	}

	/**
	 * @see #getUserParts()
	 */
	public com.top_logic.ai.chat.api.messages.PersistedTurn setUserParts(java.util.List<? extends com.top_logic.ai.chat.api.messages.PersistedPart> value) {
		internalSetUserParts(value);
		return this;
	}

	/** Internal setter for {@link #getUserParts()} without chain call utility. */
	protected final void internalSetUserParts(java.util.List<? extends com.top_logic.ai.chat.api.messages.PersistedPart> value) {
		if (value == null) throw new IllegalArgumentException("Property 'userParts' cannot be null.");
		_userParts.clear();
		_userParts.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getUserParts()} list.
	 */
	public com.top_logic.ai.chat.api.messages.PersistedTurn addUserPart(com.top_logic.ai.chat.api.messages.PersistedPart value) {
		internalAddUserPart(value);
		return this;
	}

	/** Implementation of {@link #addUserPart(com.top_logic.ai.chat.api.messages.PersistedPart)} without chain call utility. */
	protected final void internalAddUserPart(com.top_logic.ai.chat.api.messages.PersistedPart value) {
		_userParts.add(value);
	}

	/**
	 * Removes a value from the {@link #getUserParts()} list.
	 */
	public final void removeUserPart(com.top_logic.ai.chat.api.messages.PersistedPart value) {
		_userParts.remove(value);
	}

	/**
	 * Parts contributed by the assistant in this turn.
	 */
	public final java.util.List<com.top_logic.ai.chat.api.messages.PersistedPart> getAssistantParts() {
		return _assistantParts;
	}

	/**
	 * @see #getAssistantParts()
	 */
	public com.top_logic.ai.chat.api.messages.PersistedTurn setAssistantParts(java.util.List<? extends com.top_logic.ai.chat.api.messages.PersistedPart> value) {
		internalSetAssistantParts(value);
		return this;
	}

	/** Internal setter for {@link #getAssistantParts()} without chain call utility. */
	protected final void internalSetAssistantParts(java.util.List<? extends com.top_logic.ai.chat.api.messages.PersistedPart> value) {
		if (value == null) throw new IllegalArgumentException("Property 'assistantParts' cannot be null.");
		_assistantParts.clear();
		_assistantParts.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getAssistantParts()} list.
	 */
	public com.top_logic.ai.chat.api.messages.PersistedTurn addAssistantPart(com.top_logic.ai.chat.api.messages.PersistedPart value) {
		internalAddAssistantPart(value);
		return this;
	}

	/** Implementation of {@link #addAssistantPart(com.top_logic.ai.chat.api.messages.PersistedPart)} without chain call utility. */
	protected final void internalAddAssistantPart(com.top_logic.ai.chat.api.messages.PersistedPart value) {
		_assistantParts.add(value);
	}

	/**
	 * Removes a value from the {@link #getAssistantParts()} list.
	 */
	public final void removeAssistantPart(com.top_logic.ai.chat.api.messages.PersistedPart value) {
		_assistantParts.remove(value);
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
			USER_PARTS__PROP, 
			ASSISTANT_PARTS__PROP);
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
			case USER_PARTS__PROP: return getUserParts();
			case ASSISTANT_PARTS__PROP: return getAssistantParts();
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
			case USAGE__PROP: internalSetUsage((com.top_logic.ai.chat.api.messages.TokenUsageDto) value); break;
			case USER_PARTS__PROP: internalSetUserParts(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.PersistedPart.class, value)); break;
			case ASSISTANT_PARTS__PROP: internalSetAssistantParts(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.ai.chat.api.messages.PersistedPart.class, value)); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.PersistedTurn readPersistedTurn(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.PersistedTurn result = new com.top_logic.ai.chat.api.messages.PersistedTurn();
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
		out.name(USER_PARTS__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.PersistedPart x : getUserParts()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(ASSISTANT_PARTS__PROP);
		out.beginArray();
		for (com.top_logic.ai.chat.api.messages.PersistedPart x : getAssistantParts()) {
			x.writeTo(out);
		}
		out.endArray();
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TURN_NUMBER__PROP: setTurnNumber(in.nextInt()); break;
			case TIMESTAMP__PROP: setTimestamp(in.nextLong()); break;
			case MODEL__PROP: setModel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PROVIDER__PROP: setProvider(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case USAGE__PROP: setUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto.readTokenUsageDto(in)); break;
			case USER_PARTS__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.PersistedPart> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.PersistedPart.readPersistedPart(in));
				}
				in.endArray();
				setUserParts(newValue);
			}
			break;
			case ASSISTANT_PARTS__PROP: {
				java.util.List<com.top_logic.ai.chat.api.messages.PersistedPart> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(com.top_logic.ai.chat.api.messages.PersistedPart.readPersistedPart(in));
				}
				in.endArray();
				setAssistantParts(newValue);
			}
			break;
			default: super.readField(in, field);
		}
	}

}
