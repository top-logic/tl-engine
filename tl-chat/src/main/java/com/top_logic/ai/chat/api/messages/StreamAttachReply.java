package com.top_logic.ai.chat.api.messages;

/**
 * Reply to the command of {@code ChatControl} that starts sending the conversation's frames to
 * the window's event stream.
 */
public class StreamAttachReply extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.StreamAttachReply} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.StreamAttachReply create() {
		return new com.top_logic.ai.chat.api.messages.StreamAttachReply();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.StreamAttachReply} type in JSON format. */
	public static final String STREAM_ATTACH_REPLY__TYPE = "StreamAttachReply";

	/** @see #isAttached() */
	public static final String ATTACHED__PROP = "attached";

	private boolean _attached = false;

	/**
	 * Creates a {@link StreamAttachReply} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.StreamAttachReply#create()
	 */
	protected StreamAttachReply() {
		super();
	}

	/**
	 * Whether the stream was started.
	 */
	public final boolean isAttached() {
		return _attached;
	}

	/**
	 * @see #isAttached()
	 */
	public com.top_logic.ai.chat.api.messages.StreamAttachReply setAttached(boolean value) {
		internalSetAttached(value);
		return this;
	}

	/** Internal setter for {@link #isAttached()} without chain call utility. */
	protected final void internalSetAttached(boolean value) {
		_attached = value;
	}

	@Override
	public String jsonType() {
		return STREAM_ATTACH_REPLY__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			ATTACHED__PROP);
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
			case ATTACHED__PROP: return isAttached();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case ATTACHED__PROP: internalSetAttached((boolean) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.StreamAttachReply readStreamAttachReply(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.StreamAttachReply result = new com.top_logic.ai.chat.api.messages.StreamAttachReply();
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
		out.name(ATTACHED__PROP);
		out.value(isAttached());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case ATTACHED__PROP: setAttached(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
