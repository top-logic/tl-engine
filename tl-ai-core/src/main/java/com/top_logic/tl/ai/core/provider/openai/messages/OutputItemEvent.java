package com.top_logic.tl.ai.core.provider.openai.messages;

/**
 * {@code response.output_item.added} and {@code response.output_item.done}.
 */
public class OutputItemEvent extends de.haumacher.msgbuf.data.AbstractDataObject implements de.haumacher.msgbuf.data.ReflectiveDataObject {

	/**
	 * Creates a {@link com.top_logic.tl.ai.core.provider.openai.messages.OutputItemEvent} instance.
	 */
	public static com.top_logic.tl.ai.core.provider.openai.messages.OutputItemEvent create() {
		return new com.top_logic.tl.ai.core.provider.openai.messages.OutputItemEvent();
	}

	/** Identifier for the {@link com.top_logic.tl.ai.core.provider.openai.messages.OutputItemEvent} type in JSON format. */
	public static final String OUTPUT_ITEM_EVENT__TYPE = "OutputItemEvent";

	/** @see #getType() */
	public static final String TYPE__PROP = "type";

	/** @see #getOutputIndex() */
	public static final String OUTPUT_INDEX__PROP = "output_index";

	/** @see #getItem() */
	public static final String ITEM__PROP = "item";

	private String _type = "";

	private int _outputIndex = 0;

	private com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo _item = null;

	/**
	 * Creates a {@link OutputItemEvent} instance.
	 *
	 * @see com.top_logic.tl.ai.core.provider.openai.messages.OutputItemEvent#create()
	 */
	protected OutputItemEvent() {
		super();
	}

	/**
	 * The event type identifier.
	 */
	public final String getType() {
		return _type;
	}

	/**
	 * @see #getType()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OutputItemEvent setType(String value) {
		internalSetType(value);
		return this;
	}

	/** Internal setter for {@link #getType()} without chain call utility. */
	protected final void internalSetType(String value) {
		_type = value;
	}

	/**
	 * The index of the output item.
	 */
	public final int getOutputIndex() {
		return _outputIndex;
	}

	/**
	 * @see #getOutputIndex()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OutputItemEvent setOutputIndex(int value) {
		internalSetOutputIndex(value);
		return this;
	}

	/** Internal setter for {@link #getOutputIndex()} without chain call utility. */
	protected final void internalSetOutputIndex(int value) {
		_outputIndex = value;
	}

	/**
	 * The output item details.
	 */
	public final com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo getItem() {
		return _item;
	}

	/**
	 * @see #getItem()
	 */
	public com.top_logic.tl.ai.core.provider.openai.messages.OutputItemEvent setItem(com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo value) {
		internalSetItem(value);
		return this;
	}

	/** Internal setter for {@link #getItem()} without chain call utility. */
	protected final void internalSetItem(com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo value) {
		_item = value;
	}

	/**
	 * Checks, whether {@link #getItem()} has a value.
	 */
	public final boolean hasItem() {
		return _item != null;
	}

	@Override
	public String jsonType() {
		return OUTPUT_ITEM_EVENT__TYPE;
	}

	static final java.util.List<String> PROPERTIES;
	static {
		java.util.List<String> local = java.util.Arrays.asList(
			TYPE__PROP, 
			OUTPUT_INDEX__PROP, 
			ITEM__PROP);
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
			case TYPE__PROP: return getType();
			case OUTPUT_INDEX__PROP: return getOutputIndex();
			case ITEM__PROP: return getItem();
			default: return null;
		}
	}

	@Override
	public void set(String field, Object value) {
		switch (field) {
			case TYPE__PROP: internalSetType((String) value); break;
			case OUTPUT_INDEX__PROP: internalSetOutputIndex((int) value); break;
			case ITEM__PROP: internalSetItem((com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo) value); break;
		}
	}

	/** Reads a new instance from the given reader. */
	public static com.top_logic.tl.ai.core.provider.openai.messages.OutputItemEvent readOutputItemEvent(de.haumacher.msgbuf.json.JsonReader in) throws java.io.IOException {
		com.top_logic.tl.ai.core.provider.openai.messages.OutputItemEvent result = new com.top_logic.tl.ai.core.provider.openai.messages.OutputItemEvent();
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
		out.name(TYPE__PROP);
		out.value(getType());
		out.name(OUTPUT_INDEX__PROP);
		out.value(getOutputIndex());
		if (hasItem()) {
			out.name(ITEM__PROP);
			getItem().writeTo(out);
		}
	}

	@Override
	protected void readField(de.haumacher.msgbuf.json.JsonReader in, String field) throws java.io.IOException {
		switch (field) {
			case TYPE__PROP: setType(de.haumacher.msgbuf.json.JsonUtil.nextStringOptional(in)); break;
			case OUTPUT_INDEX__PROP: setOutputIndex(in.nextInt()); break;
			case ITEM__PROP: setItem(com.top_logic.tl.ai.core.provider.openai.messages.OutputItemInfo.readOutputItemInfo(in)); break;
			default: super.readField(in, field);
		}
	}

}
