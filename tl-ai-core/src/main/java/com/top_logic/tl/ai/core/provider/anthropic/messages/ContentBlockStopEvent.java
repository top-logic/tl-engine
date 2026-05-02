package com.top_logic.tl.ai.core.provider.anthropic.messages;

/**
 * Payload of the {@code content_block_stop} SSE event.
 */
public class ContentBlockStopEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStopEvent} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStopEvent create() {
		return new com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStopEvent();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStopEvent} type in JSON format. */
	public static final String CONTENT_BLOCK_STOP_EVENT__TYPE = "ContentBlockStopEvent";

	/** @see #getIndex() */
	public static final String INDEX__PROP = "index";

	private int _index = 0;

	/**
	 * Creates a {@link ContentBlockStopEvent} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStopEvent#create()
	 */
	protected ContentBlockStopEvent() {
		super();
	}

	public final int getIndex() {
		return _index;
	}

	/**
	 * @see #getIndex()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStopEvent setIndex(int value) {
		internalSetIndex(value);
		return this;
	}

	/** Internal setter for {@link #getIndex()} without chain call utility. */
	protected final void internalSetIndex(int value) {
		_index = value;
	}

	@Override
	public String jsonType() {
		return CONTENT_BLOCK_STOP_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			INDEX__PROP);
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
			case INDEX__PROP: return getIndex();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case INDEX__PROP: internalSetIndex((int) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStopEvent readContentBlockStopEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStopEvent result = new com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStopEvent();
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
		out.name(INDEX__PROP);
		out.value(getIndex());
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case INDEX__PROP: setIndex(in.nextInt()); break;
			default: super.readField(in, field);
		}
	}

}
