package com.top_logic.tl.ai.core.provider.anthropic.messages;

/**
 * Payload of the {@code content_block_delta} SSE event.
 */
public class ContentBlockDeltaEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockDeltaEvent} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockDeltaEvent create() {
		return new com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockDeltaEvent();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockDeltaEvent} type in JSON format. */
	public static final String CONTENT_BLOCK_DELTA_EVENT__TYPE = "ContentBlockDeltaEvent";

	/** @see #getIndex() */
	public static final String INDEX__PROP = "index";

	/** @see #getDelta() */
	public static final String DELTA__PROP = "delta";

	private int _index = 0;

	private com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo _delta = null;

	/**
	 * Creates a {@link ContentBlockDeltaEvent} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockDeltaEvent#create()
	 */
	protected ContentBlockDeltaEvent() {
		super();
	}

	/**
	 * The index of the content block being updated.
	 */
	public final int getIndex() {
		return _index;
	}

	/**
	 * @see #getIndex()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockDeltaEvent setIndex(int value) {
		internalSetIndex(value);
		return this;
	}

	/** Internal setter for {@link #getIndex()} without chain call utility. */
	protected final void internalSetIndex(int value) {
		_index = value;
	}

	/**
	 * The delta payload.
	 */
	public final com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo getDelta() {
		return _delta;
	}

	/**
	 * @see #getDelta()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockDeltaEvent setDelta(com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo value) {
		internalSetDelta(value);
		return this;
	}

	/** Internal setter for {@link #getDelta()} without chain call utility. */
	protected final void internalSetDelta(com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo value) {
		_delta = value;
	}

	/**
	 * Checks, whether {@link #getDelta()} has a value.
	 */
	public final boolean hasDelta() {
		return _delta != null;
	}

	@Override
	public String jsonType() {
		return CONTENT_BLOCK_DELTA_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			INDEX__PROP, 
			DELTA__PROP);
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
			case DELTA__PROP: return getDelta();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case INDEX__PROP: internalSetIndex((int) value); break;
			case DELTA__PROP: internalSetDelta((com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockDeltaEvent readContentBlockDeltaEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockDeltaEvent result = new com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockDeltaEvent();
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
		if (hasDelta()) {
			out.name(DELTA__PROP);
			getDelta().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case INDEX__PROP: setIndex(in.nextInt()); break;
			case DELTA__PROP: setDelta(com.top_logic.tl.ai.core.provider.anthropic.messages.DeltaInfo.readDeltaInfo(in)); break;
			default: super.readField(in, field);
		}
	}

}
