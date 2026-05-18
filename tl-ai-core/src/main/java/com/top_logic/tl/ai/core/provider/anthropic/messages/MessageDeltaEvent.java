package com.top_logic.tl.ai.core.provider.anthropic.messages;

/**
 * Payload of the {@code message_delta} SSE event.
 */
public class MessageDeltaEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaEvent} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaEvent create() {
		return new com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaEvent();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaEvent} type in JSON format. */
	public static final String MESSAGE_DELTA_EVENT__TYPE = "MessageDeltaEvent";

	/** @see #getDelta() */
	public static final String DELTA__PROP = "delta";

	/** @see #getUsage() */
	public static final String USAGE__PROP = "usage";

	private com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaInfo _delta = null;

	private com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo _usage = null;

	/**
	 * Creates a {@link MessageDeltaEvent} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaEvent#create()
	 */
	protected MessageDeltaEvent() {
		super();
	}

	/**
	 * The message delta information.
	 */
	public final com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaInfo getDelta() {
		return _delta;
	}

	/**
	 * @see #getDelta()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaEvent setDelta(com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaInfo value) {
		internalSetDelta(value);
		return this;
	}

	/** Internal setter for {@link #getDelta()} without chain call utility. */
	protected final void internalSetDelta(com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaInfo value) {
		_delta = value;
	}

	/**
	 * Checks, whether {@link #getDelta()} has a value.
	 */
	public final boolean hasDelta() {
		return _delta != null;
	}

	/**
	 * Updated usage statistics.
	 */
	public final com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo getUsage() {
		return _usage;
	}

	/**
	 * @see #getUsage()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaEvent setUsage(com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo value) {
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
		return MESSAGE_DELTA_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			DELTA__PROP, 
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
			case DELTA__PROP: return getDelta();
			case USAGE__PROP: return getUsage();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case DELTA__PROP: internalSetDelta((com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaInfo) value); break;
			case USAGE__PROP: internalSetUsage((com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaEvent readMessageDeltaEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaEvent result = new com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaEvent();
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
		if (hasDelta()) {
			out.name(DELTA__PROP);
			getDelta().writeTo(out);
		}
		if (hasUsage()) {
			out.name(USAGE__PROP);
			getUsage().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case DELTA__PROP: setDelta(com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaInfo.readMessageDeltaInfo(in)); break;
			case USAGE__PROP: setUsage(com.top_logic.tl.ai.core.provider.anthropic.messages.UsageInfo.readUsageInfo(in)); break;
			default: super.readField(in, field);
		}
	}

}
