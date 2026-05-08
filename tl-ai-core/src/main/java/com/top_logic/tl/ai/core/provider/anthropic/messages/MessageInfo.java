package com.top_logic.tl.ai.core.provider.anthropic.messages;

/**
 * Metadata about an Anthropic message response.
 */
public class MessageInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo create() {
		return new com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo} type in JSON format. */
	public static final String MESSAGE_INFO__TYPE = "MessageInfo";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getModel() */
	public static final String MODEL__PROP = "model";

	/** @see #getUsage() */
	public static final String USAGE__PROP = "usage";

	private String _id = "";

	private String _model = "";

	private com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo _usage = null;

	/**
	 * Creates a {@link MessageInfo} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo#create()
	 */
	protected MessageInfo() {
		super();
	}

	/**
	 * The unique message identifier.
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * The model that produced the response.
	 */
	public final String getModel() {
		return _model;
	}

	/**
	 * @see #getModel()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo setModel(String value) {
		internalSetModel(value);
		return this;
	}

	/** Internal setter for {@link #getModel()} without chain call utility. */
	protected final void internalSetModel(String value) {
		_model = value;
	}

	/**
	 * Token usage statistics.
	 */
	public final com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo getUsage() {
		return _usage;
	}

	/**
	 * @see #getUsage()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo setUsage(com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo value) {
		internalSetUsage(value);
		return this;
	}

	/** Internal setter for {@link #getUsage()} without chain call utility. */
	protected final void internalSetUsage(com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo value) {
		_usage = value;
	}

	/**
	 * Checks, whether {@link #getUsage()} has a value.
	 */
	public final boolean hasUsage() {
		return _usage != null;
	}

	@Override
	public String jsonType() {
		return MESSAGE_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			MODEL__PROP, 
			USAGE__PROP);
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
			case MODEL__PROP: return getModel();
			case USAGE__PROP: return getUsage();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case MODEL__PROP: internalSetModel((String) value); break;
			case USAGE__PROP: internalSetUsage((com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo readMessageInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo result = new com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo();
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
		out.name(MODEL__PROP);
		out.value(getModel());
		if (hasUsage()) {
			out.name(USAGE__PROP);
			getUsage().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case MODEL__PROP: setModel(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case USAGE__PROP: setUsage(com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo.readUsageInfo(in)); break;
			default: super.readField(in, field);
		}
	}

}
