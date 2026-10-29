package com.top_logic.tl.ai.core.session.persist.messages;

/**
 * A message from the user.
 */
public class UserMessageDto extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto} instance.
	 */
	public static com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto create() {
		return new com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto} type in JSON format. */
	public static final String USER_MESSAGE_DTO__TYPE = "UserMessageDto";

	/** @see #getContent() */
	public static final String CONTENT__PROP = "content";

	/** @see #getOrigin() */
	public static final String ORIGIN__PROP = "origin";

	private final java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedContent> _content = new java.util.ArrayList<>();

	private String _origin = "";

	/**
	 * Creates a {@link UserMessageDto} instance.
	 *
	 * @see com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto#create()
	 */
	protected UserMessageDto() {
		super();
	}

	/**
	 * The message content.
	 */
	public final java.util.List<com.top_logic.tl.ai.core.session.persist.messages.PersistedContent> getContent() {
		return _content;
	}

	/**
	 * @see #getContent()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto setContent(java.util.List<? extends com.top_logic.tl.ai.core.session.persist.messages.PersistedContent> value) {
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
	public com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto addContent(com.top_logic.tl.ai.core.session.persist.messages.PersistedContent value) {
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
	 * Who wrote the message, when it was not the user: the name of the message's origin, for
	 * example "FRAMEWORK". Empty for a message of the user.
	 */
	public final String getOrigin() {
		return _origin;
	}

	/**
	 * @see #getOrigin()
	 */
	public com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto setOrigin(String value) {
		internalSetOrigin(value);
		return this;
	}

	/** Internal setter for {@link #getOrigin()} without chain call utility. */
	protected final void internalSetOrigin(String value) {
		_origin = value;
	}

	@Override
	public String jsonType() {
		return USER_MESSAGE_DTO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			CONTENT__PROP, 
			ORIGIN__PROP);
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
			case ORIGIN__PROP: return getOrigin();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case CONTENT__PROP: internalSetContent(de.haumacher.msgbuf.util.Conversions.asList(com.top_logic.tl.ai.core.session.persist.messages.PersistedContent.class, value)); break;
			case ORIGIN__PROP: internalSetOrigin((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto readUserMessageDto(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto result = new com.top_logic.tl.ai.core.session.persist.messages.UserMessageDto();
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
		out.beginArray();
		for (com.top_logic.tl.ai.core.session.persist.messages.PersistedContent x : getContent()) {
			x.writeTo(out);
		}
		out.endArray();
		out.name(ORIGIN__PROP);
		out.value(getOrigin());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
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
			case ORIGIN__PROP: setOrigin(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
