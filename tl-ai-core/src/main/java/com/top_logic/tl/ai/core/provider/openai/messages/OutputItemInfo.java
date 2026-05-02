package com.top_logic.tl.ai.core.provider.openai.messages;

public class OutputItemInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo create() {
		return new com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo} type in JSON format. */
	public static final String OUTPUT_ITEM_INFO__TYPE = "OutputItemInfo";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getCallId() */
	public static final String CALL_ID__PROP = "call_id";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getArguments() */
	public static final String ARGUMENTS__PROP = "arguments";

	private String _type = "";

	private String _callId = "";

	private String _name = "";

	private String _id = "";

	private String _arguments = "";

	/**
	 * Creates a {@link OutputItemInfo} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo#create()
	 */
	protected OutputItemInfo() {
		super();
	}

	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	public final String getCallId() {
		return _callId;
	}

	/**
	 * @see #getCallId()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo setCallId(String value) {
		internalSetCallId(value);
		return this;
	}

	/** Internal setter for {@link #getCallId()} without chain call utility. */
	protected final void internalSetCallId(String value) {
		_callId = value;
	}

	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	public final String getArguments() {
		return _arguments;
	}

	/**
	 * @see #getArguments()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo setArguments(String value) {
		internalSetArguments(value);
		return this;
	}

	/** Internal setter for {@link #getArguments()} without chain call utility. */
	protected final void internalSetArguments(String value) {
		_arguments = value;
	}

	@Override
	public String jsonType() {
		return OUTPUT_ITEM_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TYPE__PROP, 
			CALL_ID__PROP, 
			NAME__PROP, 
			ID__PROP, 
			ARGUMENTS__PROP);
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
			case TYPE__PROP: return getType();
			case CALL_ID__PROP: return getCallId();
			case NAME__PROP: return getName();
			case ID__PROP: return getId();
			case ARGUMENTS__PROP: return getArguments();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TYPE__PROP: internalSetType((String) value); break;
			case CALL_ID__PROP: internalSetCallId((String) value); break;
			case NAME__PROP: internalSetName((String) value); break;
			case ID__PROP: internalSetId((String) value); break;
			case ARGUMENTS__PROP: internalSetArguments((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo readOutputItemInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo result = new com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo();
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
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(CALL_ID__PROP);
		out.value(getCallId());
		out.name(NAME__PROP);
		out.value(getName());
		out.name(ID__PROP);
		out.value(getId());
		out.name(ARGUMENTS__PROP);
		out.value(getArguments());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case CALL_ID__PROP: setCallId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ARGUMENTS__PROP: setArguments(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
