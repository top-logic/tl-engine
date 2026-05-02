package com.top_logic.tl.ai.core.provider.anthropic.messages;

public class MessageDeltaInfo extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaInfo} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaInfo create() {
		return new com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaInfo();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaInfo} type in JSON format. */
	public static final String MESSAGE_DELTA_INFO__TYPE = "MessageDeltaInfo";

	/** @see #getStopReason() */
	public static final String STOP_REASON__PROP = "stop_reason";

	private String _stopReason = "";

	/**
	 * Creates a {@link MessageDeltaInfo} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaInfo#create()
	 */
	protected MessageDeltaInfo() {
		super();
	}

	public final String getStopReason() {
		return _stopReason;
	}

	/**
	 * @see #getStopReason()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaInfo setStopReason(String value) {
		internalSetStopReason(value);
		return this;
	}

	/** Internal setter for {@link #getStopReason()} without chain call utility. */
	protected final void internalSetStopReason(String value) {
		_stopReason = value;
	}

	@Override
	public String jsonType() {
		return MESSAGE_DELTA_INFO__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			STOP_REASON__PROP);
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
			case STOP_REASON__PROP: return getStopReason();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case STOP_REASON__PROP: internalSetStopReason((String) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaInfo readMessageDeltaInfo(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaInfo result = new com.top_logic.tl.ai.core.provider.anthropic.messages.MessageDeltaInfo();
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
		out.name(STOP_REASON__PROP);
		out.value(getStopReason());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case STOP_REASON__PROP: setStopReason(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			default: super.readField(in, field);
		}
	}

}
