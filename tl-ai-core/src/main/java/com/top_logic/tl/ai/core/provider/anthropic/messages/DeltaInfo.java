package com.top_logic.tl.ai.core.provider.anthropic.messages;

public class DeltaInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo create() {
		return new com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo} type in JSON format. */
	public static final String DELTA_INFO__TYPE = "DeltaInfo";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getText() */
	public static final String TEXT__PROP = "text";

	/** @see #getPartialJson() */
	public static final String PARTIAL_JSON__PROP = "partial_json";

	private String _type = "";

	private String _text = "";

	private String _partialJson = "";

	/**
	 * Creates a {@link DeltaInfo} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo#create()
	 */
	protected DeltaInfo() {
		super();
	}

	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	public final String getText() {
		return _text;
	}

	/**
	 * @see #getText()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo setText(String value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(String value) {
		_text = value;
	}

	public final String getPartialJson() {
		return _partialJson;
	}

	/**
	 * @see #getPartialJson()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo setPartialJson(String value) {
		internalSetPartialJson(value);
		return this;
	}

	/** Internal setter for {@link #getPartialJson()} without chain call utility. */
	protected final void internalSetPartialJson(String value) {
		_partialJson = value;
	}

	@Override
	public String jsonType() {
		return DELTA_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TYPE__PROP, 
			TEXT__PROP, 
			PARTIAL_JSON__PROP);
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
			case TEXT__PROP: return getText();
			case PARTIAL_JSON__PROP: return getPartialJson();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TYPE__PROP: internalSetType((String) value); break;
			case TEXT__PROP: internalSetText((String) value); break;
			case PARTIAL_JSON__PROP: internalSetPartialJson((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo readDeltaInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo result = new com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo();
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
		out.name(TEXT__PROP);
		out.value(getText());
		out.name(PARTIAL_JSON__PROP);
		out.value(getPartialJson());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case PARTIAL_JSON__PROP: setPartialJson(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
