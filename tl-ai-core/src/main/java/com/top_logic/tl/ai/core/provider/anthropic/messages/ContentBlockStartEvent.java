package com.top_logic.tl.ai.core.provider.anthropic.messages;

/**
 * Payload of the {@code content_block_start} SSE event.
 */
public class ContentBlockStartEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStartEvent} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStartEvent create() {
		return new com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStartEvent();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStartEvent} type in JSON format. */
	public static final String CONTENT_BLOCK_START_EVENT__TYPE = "ContentBlockStartEvent";

	/** @see #getIndex() */
	public static final String INDEX__PROP = "index";

	/** @see #getContentBlock() */
	public static final String CONTENT_BLOCK__PROP = "content_block";

	private int _index = 0;

	private com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo _contentBlock = null;

	/**
	 * Creates a {@link ContentBlockStartEvent} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStartEvent#create()
	 */
	protected ContentBlockStartEvent() {
		super();
	}

	/**
	 * The index of the content block within the response.
	 */
	public final int getIndex() {
		return _index;
	}

	/**
	 * @see #getIndex()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStartEvent setIndex(int value) {
		internalSetIndex(value);
		return this;
	}

	/** Internal setter for {@link #getIndex()} without chain call utility. */
	protected final void internalSetIndex(int value) {
		_index = value;
	}

	/**
	 * The content block metadata.
	 */
	public final com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo getContentBlock() {
		return _contentBlock;
	}

	/**
	 * @see #getContentBlock()
	 */
	public com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStartEvent setContentBlock(com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo value) {
		internalSetContentBlock(value);
		return this;
	}

	/** Internal setter for {@link #getContentBlock()} without chain call utility. */
	protected final void internalSetContentBlock(com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo value) {
		_contentBlock = value;
	}

	/**
	 * Checks, whether {@link #getContentBlock()} has a value.
	 */
	public final boolean hasContentBlock() {
		return _contentBlock != null;
	}

	@Override
	public String jsonType() {
		return CONTENT_BLOCK_START_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			INDEX__PROP, 
			CONTENT_BLOCK__PROP);
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
			case CONTENT_BLOCK__PROP: return getContentBlock();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case INDEX__PROP: internalSetIndex((int) value); break;
			case CONTENT_BLOCK__PROP: internalSetContentBlock((com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStartEvent readContentBlockStartEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStartEvent result = new com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockStartEvent();
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
		if (hasContentBlock()) {
			out.name(CONTENT_BLOCK__PROP);
			getContentBlock().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case INDEX__PROP: setIndex(in.nextInt()); break;
			case CONTENT_BLOCK__PROP: setContentBlock(com.top_logic.tl.ai.core.provider.anthropic.messages.ContentBlockInfo.readContentBlockInfo(in)); break;
			default: super.readField(in, field);
		}
	}

}
