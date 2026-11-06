package com.top_logic.ai.chat.api.messages;

/**
 * Reply to a command of {@code ChatControl} that cancels something: a run, a prompt improvement,
 * a subagent.
 */
public class CancelReply extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.ai.chat.api.messages.CancelReply} instance.
	 */
	public static com.top_logic.ai.chat.api.messages.CancelReply create() {
		return new com.top_logic.ai.chat.api.messages.CancelReply();
	}

	/** Identifier for the {@link com.top_logic.ai.chat.api.messages.CancelReply} type in JSON format. */
	public static final String CANCEL_REPLY__TYPE = "CancelReply";

	/** @see #isCancelled() */
	public static final String CANCELLED__PROP = "cancelled";

	private boolean _cancelled = false;

	/**
	 * Creates a {@link CancelReply} instance.
	 *
	 * @see com.top_logic.ai.chat.api.messages.CancelReply#create()
	 */
	protected CancelReply() {
		super();
	}

	/**
	 * Whether something was cancelled.
	 */
	public final boolean isCancelled() {
		return _cancelled;
	}

	/**
	 * @see #isCancelled()
	 */
	public com.top_logic.ai.chat.api.messages.CancelReply setCancelled(boolean value) {
		internalSetCancelled(value);
		return this;
	}

	/** Internal setter for {@link #isCancelled()} without chain call utility. */
	protected final void internalSetCancelled(boolean value) {
		_cancelled = value;
	}

	@Override
	public String jsonType() {
		return CANCEL_REPLY__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			CANCELLED__PROP);
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
			case CANCELLED__PROP: return isCancelled();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case CANCELLED__PROP: internalSetCancelled((boolean) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.ai.chat.api.messages.CancelReply readCancelReply(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.ai.chat.api.messages.CancelReply result = new com.top_logic.ai.chat.api.messages.CancelReply();
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
		out.name(CANCELLED__PROP);
		out.value(isCancelled());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case CANCELLED__PROP: setCancelled(in.nextBoolean()); break;
			default: super.readField(in, field);
		}
	}

}
