package com.top_logic.ai.chat.api.messages;

/**
 * One message the user wrote to a subagent.
 */
public class SubagentMessageDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.SubagentMessageDto} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.SubagentMessageDto create() {
		return new com.top_logic.ai.chat.api.messages.SubagentMessageDto();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.SubagentMessageDto} type in JSON format. */
	public static final String SUBAGENT_MESSAGE_DTO__TYPE = "SubagentMessageDto";

	/** @see #getId() */
	public static final String ID__PROP = "id";

	/** @see #getText() */
	public static final String TEXT__PROP = "text";

	private String _id = "";

	private String _text = "";

	/**
	 * Creates a {@link SubagentMessageDto} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.SubagentMessageDto#create()
	 */
	protected SubagentMessageDto() {
		super();
	}

	/**
	 * The message's id, as the answer to the request named it.
	 */
	public final String getId() {
		return _id;
	}

	/**
	 * @see #getId()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentMessageDto setId(String value) {
		internalSetId(value);
		return this;
	}

	/** Internal setter for {@link #getId()} without chain call utility. */
	protected final void internalSetId(String value) {
		_id = value;
	}

	/**
	 * What the user wrote.
	 */
	public final String getText() {
		return _text;
	}

	/**
	 * @see #getText()
	 */
	public com.top_logic.ai.chat.api.messages.SubagentMessageDto setText(String value) {
		internalSetText(value);
		return this;
	}

	/** Internal setter for {@link #getText()} without chain call utility. */
	protected final void internalSetText(String value) {
		_text = value;
	}

	@Override
	public String jsonType() {
		return SUBAGENT_MESSAGE_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ID__PROP, 
			TEXT__PROP);
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
			case TEXT__PROP: return getText();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ID__PROP: internalSetId((String) value); break;
			case TEXT__PROP: internalSetText((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.SubagentMessageDto readSubagentMessageDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.SubagentMessageDto result = new com.top_logic.ai.chat.api.messages.SubagentMessageDto();
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
		out.name(TEXT__PROP);
		out.value(getText());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ID__PROP: setId(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case TEXT__PROP: setText(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
