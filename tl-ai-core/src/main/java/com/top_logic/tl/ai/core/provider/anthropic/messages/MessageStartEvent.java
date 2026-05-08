package com.top_logic.tl.ai.core.provider.anthropic.messages;

/**
 * Payload of the {@code message_start} SSE event.
 */
public class MessageStartEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.anthropic.messages.MessageStartEvent} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.MessageStartEvent create() {
		return new com.top_logic.tl.ai.core.provider.anthropic.messages.MessageStartEvent();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.anthropic.messages.MessageStartEvent} type in JSON format. */
	public static final String MESSAGE_START_EVENT__TYPE = "MessageStartEvent";

	/** @see #getMessage() */
	public static final String MESSAGE__PROP = "message";

	private com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo _message = null;

	/**
	 * Creates a {@link MessageStartEvent} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.anthropic.messages.MessageStartEvent#create()
	 */
	protected MessageStartEvent() {
		super();
	}

	/**
	 * The message metadata.
	 */
	public final com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo getMessage() {
		return _message;
	}

	/**
	 * @see #getMessage()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.MessageStartEvent setMessage(com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo value) {
		internalSetMessage(value);
		return this;
	}

	/** Internal setter for {@link #getMessage()} without chain call utility. */
	protected final void internalSetMessage(com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo value) {
		_message = value;
	}

	/**
	 * Checks, whether {@link #getMessage()} has a value.
	 */
	public final boolean hasMessage() {
		return _message != null;
	}

	@Override
	public String jsonType() {
		return MESSAGE_START_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			MESSAGE__PROP);
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
			case MESSAGE__PROP: return getMessage();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case MESSAGE__PROP: internalSetMessage((com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.MessageStartEvent readMessageStartEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.anthropic.messages.MessageStartEvent result = new com.top_logic.tl.ai.core.provider.anthropic.messages.MessageStartEvent();
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
		if (hasMessage()) {
			out.name(MESSAGE__PROP);
			getMessage().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case MESSAGE__PROP: setMessage(com.top_logic.tl.ai.core.provider.anthropic.messages.MessageInfo.readMessageInfo(in)); break;
			default: super.readField(in, field);
		}
	}

}
