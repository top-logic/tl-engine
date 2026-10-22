package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * A system message in the message history.
 */
public class SystemMessageDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.SystemMessageDto} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.SystemMessageDto create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.SystemMessageDto();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.SystemMessageDto} type in JSON format. */
	public static final String SYSTEM_MESSAGE_DTO__TYPE = "SystemMessageDto";

	/** @see #getContent() */
	public static final String CONTENT__PROP = "content";

	private String _content = "";

	/**
	 * Creates a {@link SystemMessageDto} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.SystemMessageDto#create()
	 */
	protected SystemMessageDto() {
		super();
	}

	/**
	 * The message text.
	 */
	public final String getContent() {
		return _content;
	}

	/**
	 * @see #getContent()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.SystemMessageDto setContent(String value) {
		internalSetContent(value);
		return this;
	}

	/** Internal setter for {@link #getContent()} without chain call utility. */
	protected final void internalSetContent(String value) {
		_content = value;
	}

	@Override
	public String jsonType() {
		return SYSTEM_MESSAGE_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			CONTENT__PROP);
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
			case CONTENT__PROP: return getContent();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case CONTENT__PROP: internalSetContent((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.SystemMessageDto readSystemMessageDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.SystemMessageDto result = new com.top_logic.tl.ai.core.session.persist.messages.SystemMessageDto();
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
		out.name(CONTENT__PROP);
		out.value(getContent());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CONTENT__PROP: setContent(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
