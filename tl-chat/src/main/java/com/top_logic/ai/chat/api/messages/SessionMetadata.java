package com.top_logic.ai.chat.api.messages;

/**
 * Session metadata for persistence.
 */
public class SessionMetadata extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.SessionMetadata} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.SessionMetadata create() {
		return new com.top_logic.ai.chat.api.messages.SessionMetadata();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.SessionMetadata} type in JSON format. */
	public static final String SESSION_METADATA__TYPE = "SessionMetadata";

	/** @see #getApprovedTools() */
	public static final String APPROVED_TOOLS__PROP = "approvedTools";

	/** @see #getCumulativeUsage() */
	public static final String CUMULATIVE_USAGE__PROP = "cumulativeUsage";

	/** @see #getLastModel() */
	public static final String LAST_MODEL__PROP = "lastModel";

	private final java.util.List<String> _approvedTools = new java.util.ArrayList<>();

	private com.top_logic.ai.chat.api.messages.TokenUsageDto _cumulativeUsage = null;

	private String _lastModel = "";

	/**
	 * Creates a {@link SessionMetadata} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.SessionMetadata#create()
	 */
	protected SessionMetadata() {
		super();
	}

	/**
	 * Tool names approved for this session.
	 */
	public final java.util.List<String> getApprovedTools() {
		return _approvedTools;
	}

	/**
	 * @see #getApprovedTools()
	 */
	public com.top_logic.ai.chat.api.messages.SessionMetadata setApprovedTools(java.util.List<? extends String> value) {
		internalSetApprovedTools(value);
		return this;
	}

	/** Internal setter for {@link #getApprovedTools()} without chain call utility. */
	protected final void internalSetApprovedTools(java.util.List<? extends String> value) {
		_approvedTools.clear();
		_approvedTools.addAll(value);
	}

	/**
	 * Adds a value to the {@link #getApprovedTools()} list.
	 */
	public com.top_logic.ai.chat.api.messages.SessionMetadata addApprovedTool(String value) {
		internalAddApprovedTool(value);
		return this;
	}

	/** Implementation of {@link #addApprovedTool(String)} without chain call utility. */
	protected final void internalAddApprovedTool(String value) {
		_approvedTools.add(value);
	}

	/**
	 * Removes a value from the {@link #getApprovedTools()} list.
	 */
	public final void removeApprovedTool(String value) {
		_approvedTools.remove(value);
	}

	/**
	 * Cumulative token usage.
	 */
	public final com.top_logic.ai.chat.api.messages.TokenUsageDto getCumulativeUsage() {
		return _cumulativeUsage;
	}

	/**
	 * @see #getCumulativeUsage()
	 */
	public com.top_logic.ai.chat.api.messages.SessionMetadata setCumulativeUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto value) {
		internalSetCumulativeUsage(value);
		return this;
	}

	/** Internal setter for {@link #getCumulativeUsage()} without chain call utility. */
	protected final void internalSetCumulativeUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto value) {
		_cumulativeUsage = value;
	}

	/**
	 * Checks, whether {@link #getCumulativeUsage()} has a value.
	 */
	public final boolean hasCumulativeUsage() {
		return _cumulativeUsage != null;
	}

	/**
	 * Last model used.
	 */
	public final String getLastModel() {
		return _lastModel;
	}

	/**
	 * @see #getLastModel()
	 */
	public com.top_logic.ai.chat.api.messages.SessionMetadata setLastModel(String value) {
		internalSetLastModel(value);
		return this;
	}

	/** Internal setter for {@link #getLastModel()} without chain call utility. */
	protected final void internalSetLastModel(String value) {
		_lastModel = value;
	}

	@Override
	public String jsonType() {
		return SESSION_METADATA__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			APPROVED_TOOLS__PROP, 
			CUMULATIVE_USAGE__PROP, 
			LAST_MODEL__PROP);
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
			case APPROVED_TOOLS__PROP: return getApprovedTools();
			case CUMULATIVE_USAGE__PROP: return getCumulativeUsage();
			case LAST_MODEL__PROP: return getLastModel();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case APPROVED_TOOLS__PROP: internalSetApprovedTools(de.haumacher.msgbuf.util.Conversions.asList(String.class, value)); break;
			case CUMULATIVE_USAGE__PROP: internalSetCumulativeUsage((com.top_logic.ai.chat.api.messages.TokenUsageDto) value); break;
			case LAST_MODEL__PROP: internalSetLastModel((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.SessionMetadata readSessionMetadata(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.SessionMetadata result = new com.top_logic.ai.chat.api.messages.SessionMetadata();
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
		out.name(APPROVED_TOOLS__PROP);
		out.beginArray();
		for (String x : getApprovedTools()) {
			out.value(x);
		}
		out.endArray();
		if (hasCumulativeUsage()) {
			out.name(CUMULATIVE_USAGE__PROP);
			getCumulativeUsage().writeTo(out);
		}
		out.name(LAST_MODEL__PROP);
		out.value(getLastModel());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case APPROVED_TOOLS__PROP: {
				java.util.List<String> newValue = new java.util.ArrayList<>();
				in.beginArray();
				while (in.hasNext()) {
					newValue.add(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in));
				}
				in.endArray();
				setApprovedTools(newValue);
			}
			break;
			case CUMULATIVE_USAGE__PROP: setCumulativeUsage(com.top_logic.ai.chat.api.messages.TokenUsageDto.readTokenUsageDto(in)); break;
			case LAST_MODEL__PROP: setLastModel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
