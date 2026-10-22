package com.top_logic.tl.ai.core.provider.anthropic.messages;

/**
 * Metadata about a content block in an Anthropic response.
 */
public class ContentBlockInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo create() {
		return new com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo} type in JSON format. */
	public static final String CONTENT_BLOCK_INFO__TYPE = "ContentBlockInfo";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getName() */
	public static final String NAME__PROP = "name";

	private String _type = "";

	private String _id = "";

	private String _name = "";

	/**
	 * Creates a {@link ContentBlockInfo} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo#create()
	 */
	protected ContentBlockInfo() {
		super();
	}

	/**
	 * The block type (text, tool_use, thinking).
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * The block identifier.
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * The tool name (for tool_use blocks).
	 */
	public final String getName() {
		return _name;
	}

	/**
	 * @see #getName()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo setName(String value) {
		internalSetName(value);
		return this;
	}

	/** Internal setter for {@link #getName()} without chain call utility. */
	protected final void internalSetName(String value) {
		_name = value;
	}

	@Override
	public String jsonType() {
		return CONTENT_BLOCK_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TYPE__PROP, 
			ID__PROP, 
			NAME__PROP);
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
			case ID__PROP: return getId();
			case NAME__PROP: return getName();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TYPE__PROP: internalSetType((String) value); break;
			case ID__PROP: internalSetId((String) value); break;
			case NAME__PROP: internalSetName((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo readContentBlockInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo result = new com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo();
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
		out.name(ID__PROP);
		out.value(getId());
		out.name(NAME__PROP);
		out.value(getName());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case NAME__PROP: setName(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
